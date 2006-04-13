/*
 * Created on 10-Mar-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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
import org.apache.lucene.store.FSDirectory;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.parse.PageTag;
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

  public void openWriter() {
    File f = new File(indexdir);
    if (!f.exists()) {
      f.mkdirs();
    }
    try {
      indexmodifier = new IndexModifier(f, analyzer, !IndexReader
          .indexExists(f));
    }
    catch (Exception e) {
      try {
        IndexReader.unlock(FSDirectory.getDirectory(f, false));
        indexmodifier = new IndexModifier(f, analyzer, !IndexReader
            .indexExists(f));
      }
      catch (Exception e2) {
        throw UniversalRuntimeException.accumulate(e2,
            "2nd-time exception trying to build index in " + f);
      }
    }
  }

  public void open() {
    try {
      openWriter();
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
    query.add(new TermQuery(new Term(DocFields.PAGESEQ_START, Integer
        .toString(pageseq))), Occur.MUST);
    try {
      DocHit[] togo = null;
      long time = System.currentTimeMillis();
      for (int i = 0; i < 1; ++ i) {
      Hits hits = indexsearcher.search(query);
      togo = new DocHit[hits.length()];
      int index = 0;
      for (Iterator hitit = hits.iterator(); hitit.hasNext();) {
        togo[index++] = new DocHit((Hit) hitit.next());
      }
      }
      System.out.println("1 searches for " + togo.length + " in " + (System.currentTimeMillis() - time) + "ms");
      return togo;
    }
    catch (IOException e) {
      throw UniversalRuntimeException.accumulate(e, "Error searching for page "
          + pageseq + " of " + ID);
    }
  }

  private List updatecontents = new ArrayList();
  private List updatepages = new ArrayList();

  public void beginUpdates() {
  }

  public void checkPage(ContentInfo contentinfo, PageTag pagetag) {
    File f = new File(contentinfo.filename);
    if (!upToDate(f, contentinfo.itemID, contentinfo.firstpage)) {
      updatecontents.add(contentinfo);
      updatepages.add(pagetag);
    }
  }

  public void addPage(ContentInfo contentinfo, PageTag pagetag) {
    File f = new File(contentinfo.filename);
    Document doc = new Document();
    doc.add(new Field(DocFields.FILEDATE, Long.toString(f.lastModified()),
        Store.YES, Index.NO));
    doc.add(new Field(DocFields.ITEMID, contentinfo.itemID, Store.YES,
        Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.PAGESEQ_START, Integer
        .toString(contentinfo.firstpage), Store.YES, Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.PAGESEQ_END, Integer
        .toString(contentinfo.lastpage), Store.YES, Index.UN_TOKENIZED));
    long size = f.length();

    doc.add(new Field(DocFields.TEXT, new StringReader(pagetag.pagetext),
        TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field(DocFields.FLAT_TEXT, pagetag.pagetext, Store.YES, Index.NO));

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
      for (int i = 0; i < updatecontents.size(); ++i) {
        ContentInfo todel = (ContentInfo) updatecontents.get(i);
        DocHit[] olds = getHit(todel.itemID, todel.firstpage);
        for (int j = 0; j < olds.length; ++j) {
          indexmodifier.deleteDocument(olds[j].docid);
        }
      }
      for (int i = 0; i < updatecontents.size(); ++i) {
        ContentInfo todel = (ContentInfo) updatecontents.get(i);
        addPage(todel, (PageTag) updatepages.get(i));
      }
      indexmodifier.flush();
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error updating index");
    }
    finally {
      updatecontents.clear();
    }
  }

  public IndexSearcher getSearcher() {
    return indexsearcher;
  }

  public boolean upToDate(File file, String ID, int pageseq) {
    if (forcereindex)
      return false;
    long modtime = file.lastModified();

    DocHit[] olds = getHit(ID, pageseq);
    long oldtime = 0;
    if (olds.length > 0) {
      oldtime = Long.parseLong(olds[0].document.get(DocFields.FILEDATE));
    }
    return oldtime == modtime;
  }
}
