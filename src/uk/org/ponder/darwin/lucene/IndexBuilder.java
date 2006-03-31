/*
 * Created on 10-Mar-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.parse.FlatteningReader;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class IndexBuilder {
  
  private String indexdir;
  private Analyzer analyzer;
  private IndexModifier indexmodifier;
  private IndexSearcher indexsearcher;
  private boolean forcereindex = false;
  
  public long indexedbytes;

  public void setIndexDirectory(String indexdir) {
    this.indexdir = indexdir;
  }

  public void setAnalyser(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  public void setForceReindex(boolean forcereindex) {
    this.forcereindex = forcereindex;
  }
  
  public void open() {
    try {
      File f = new File(indexdir);
      if (!f.exists()) {
        f.mkdirs();
      }
      indexmodifier = new IndexModifier(f, analyzer, !IndexReader.indexExists(f));
      indexsearcher = new IndexSearcher(indexdir);
      indexedbytes = 0;
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error opening index directory " + indexdir + " for writing");
    }
  }

  public void close() {
    try {
      indexmodifier.close();
    }
    catch (Exception e) {
      Logger.log.error("Error closing modifier: ", e);
    }
    try {
      indexsearcher.close();
    }
    catch (Exception e) {
      Logger.log.error("Error closing searcher: ", e);
    }
  }

  public DocHit[] getHit(String ID, int pageseq) {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(DocFields.ITEMID, ID)), Occur.MUST);
    query.add(new TermQuery(new Term(DocFields.PAGESEQ_START, Integer.toString(pageseq))),
        Occur.MUST);
    try {
      Hits hits = indexsearcher.search(query);
      DocHit[] togo = new DocHit[hits.length()];
      int index = 0;
      for (Iterator hitit = hits.iterator(); hitit.hasNext();) {
        togo[index++] = new DocHit((Hit)hitit.next());
      }
      return togo;
    }
    catch (IOException e) {
      throw UniversalRuntimeException.accumulate(e, "Error searching for page "
          + pageseq + " of " + ID);
    }
  }

  public static final Reader agglomeratedReader(String path) {
    try {
      FileInputStream fis = new FileInputStream(path);
      Reader r = new InputStreamReader(fis, "UTF-8");
      return new BufferedReader(new FlatteningReader(r));
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error opening file at path " + path);
    }
  }

  private List updatelist = new ArrayList();
  
  public void beginUpdates() {
  }
  
  public void checkPage(ContentInfo contentinfo) {
    File f = new File(contentinfo.filename);
    if (!upToDate(f, contentinfo.itemID, contentinfo.firstpage)) {
      updatelist.add(contentinfo);
    }
  }
  
  
  public void addPage(ContentInfo contentinfo) {
    File f = new File(contentinfo.filename);
    Document doc = new Document();
    doc.add(new Field(DocFields.FILEDATE, Long.toString(f.lastModified()),
        Store.YES, Index.NO));
    doc.add(new Field(DocFields.ITEMID, contentinfo.itemID, Store.YES, Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.PAGESEQ_START, Integer.toString(contentinfo.firstpage),
        Store.YES, Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.PAGESEQ_END, Integer.toString(contentinfo.lastpage),
        Store.YES, Index.UN_TOKENIZED));
    long size = f.length();
 
    doc.add(new Field("text", agglomeratedReader(contentinfo.filename),
        TermVector.WITH_POSITIONS_OFFSETS));
    
    try {
      indexmodifier.addDocument(doc);
      indexedbytes += size;
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error adding document");
    }
  }
  public void endUpdates() {
    try {
      for (int i = 0; i < updatelist.size(); ++ i) {
        ContentInfo todel = (ContentInfo) updatelist.get(i);
        DocHit[] olds = getHit(todel.itemID, todel.firstpage);
        for (int j = 0; j < olds.length; ++ j) {
          indexmodifier.deleteDocument(olds[j].docid);
        }
      }
      for (int i = 0; i < updatelist.size(); ++ i) {
        ContentInfo todel = (ContentInfo) updatelist.get(i);
        addPage(todel);
      }
      indexmodifier.flush();
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error updating index");
    }
    finally {
      updatelist.clear();
    }
  }
  
  public IndexSearcher getSearcher() {
    return indexsearcher;
  }

  public boolean upToDate(File file, String ID, int pageseq) {
    if (forcereindex) return false;
    long modtime = file.lastModified();

    DocHit[] olds = getHit(ID, pageseq);
    long oldtime = 0;
    if (olds.length > 0) {
      oldtime = Long.parseLong(olds[0].document.get(DocFields.FILEDATE));
    }
    return oldtime == modtime;
  }
}
