/*
 * Created on 10-Mar-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.parse.PageTag;
import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.darwin.search.FieldTypeInfo;
import uk.org.ponder.darwin.search.ItemFields;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class IndexBuilder implements IndexForceOpener {
  private boolean isopen = false;

  private String indexdir;
  private Analyzer analyzer;
  private IndexModifier indexmodifier;
  private IndexItemSearcher indexitemsearcher;
  private boolean forcereindex = false;

  private int maxpending = 1000;

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

  public void setMaxPendingDocuments(int maxpending) {
    this.maxpending = maxpending;
  }

  public void setIndexItemSearcher(IndexItemSearcher indexitemsearcher) {
    this.indexitemsearcher = indexitemsearcher;
    indexitemsearcher.setForceOpener(this);
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
    if (!isopen) {
      try {
        openWriter();
        isopen = true;

        indexedbytes = 0;
      }
      catch (Exception e) {
        throw UniversalRuntimeException.accumulate(e,
            "Error opening index directory " + indexdir + " for writing");
      }
    }
  }

  public void forceOpenIndex() {
    open();
  }

  public void close() {
    try {
      indexmodifier.close();
    }
    catch (Exception e) {
      Logger.log.error("Error closing modifier: ", e);
    }
  }

  // parallel lists of ContentInfo, and PageTag for updates that WILL be
  // processed when endUpdates is called.
  private List updatecontents = new ArrayList();
  private List updatepages = new ArrayList();
  private String[] fieldnames;
  private int idindex;
  private List updateitems = new ArrayList();
  int pending = 0;
  private int[] fieldtypes;
  private DBFieldGetter dbfieldgetter;

  public void beginUpdates() {
  }

  /**
   * Called bracketed by beginUpdates() and endUpdates() for each page to test
   * for staleness.
   * 
   * @param contentinfo
   * @param pagetag
   */
  public void checkPage(ContentInfo contentinfo, PageTag pagetag) {
    File f = new File(contentinfo.filename);
    if (!upToDate(f, contentinfo.itemID, contentinfo.firstpage)) {
      updatecontents.add(contentinfo);
      updatepages.add(pagetag);
      ++pending;
    }
  }

  public void setMapping(String[] fieldnames, int idindex, int[] fieldtypes) {
    this.fieldnames = fieldnames;
    this.idindex = idindex;
    this.fieldtypes = fieldtypes;
  }

  public void checkItem(String[] fields) {

    updateitems.add(fields);
    ++pending;
    if (pending > maxpending) {
      endUpdates(false);
      beginUpdates();
    }
  }

  private void deleteDocHits(DocHit[] olds) {
    try {
      for (int j = 0; j < olds.length; ++j) {
        indexmodifier.deleteDocument(olds[j].docid);
        // System.out.print(olds[j].docid+"..");
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException
          .accumulate(e, "Error deleting from index");
    }
  }

  public void endUpdates() {
    endUpdates(true);
  }

  /**
   * Step 1 - deletes all documents which match those referenced in
   * updatecontents, which will be deleted.
   */
  private void endUpdates(boolean finish) {
    try {
      for (int i = 0; i < updatecontents.size(); ++i) {
        ContentInfo todel = (ContentInfo) updatecontents.get(i);
        DocHit[] olds = indexitemsearcher.getPageHit(todel.itemID,
            todel.firstpage);
        deleteDocHits(olds);
      }
      for (int i = 0; i < updateitems.size(); ++i) {
        String[] fields = (String[]) updateitems.get(i);
        DocHit[] olds = indexitemsearcher.getItemHit(fields[idindex]);
        deleteDocHits(olds);
      }

      for (int i = 0; i < updatecontents.size(); ++i) {
        ContentInfo todel = (ContentInfo) updatecontents.get(i);
        addPage(todel, (PageTag) updatepages.get(i));
      }
      for (int i = 0; i < updateitems.size(); ++i) {
        String[] fields = (String[]) updateitems.get(i);
        addItem(fields);
      }
      Logger.log.warn(updateitems.size() + " items added: docCount "
          + this.indexmodifier.docCount());

      indexmodifier.flush();
      if (finish) {
        indexmodifier.optimize();
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error updating index");
    }
    finally {
      updatecontents.clear();
      updatepages.clear();
      updateitems.clear();
      pending = 0;
    }
  }

  private void addDBFields(Document doc, String[] fields) {
    for (int i = 0; i < fieldnames.length; ++i) {
      String field = fields[i];
      if (field != null && field.trim().length() > 0) {
        doc.add(new Field(fieldnames[i], fields[i], Store.YES,
            fieldtypes[i] == FieldTypeInfo.TYPE_FREE_STRING ? Index.TOKENIZED
                : Index.UN_TOKENIZED));
      }
    }
  }

  public void addItem(String[] fields) {
    Document doc = new Document();

    try {
      addDBFields(doc, fields);
      doc.add(new Field(DocFields.TYPE, DocFields.TYPE_ITEM, Store.YES,
          Index.UN_TOKENIZED));
      indexmodifier.addDocument(doc);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error adding document");
    }
  }

  /**
   * Does the work of actually adding the content for a given page.
   * 
   * @param contentinfo
   * @param pagetag
   */
  public void addPage(ContentInfo contentinfo, PageTag pagetag) {
    File f = new File(contentinfo.filename);
    Document doc = new Document();
    doc.add(new Field(DocFields.TYPE, DocFields.TYPE_PAGE, Store.YES,
        Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.FILEDATE, Long.toString(f.lastModified()),
        Store.YES, Index.NO));
    doc.add(new Field(DocFields.ITEMID, contentinfo.itemID, Store.YES,
        Index.UN_TOKENIZED));
    doc.add(new Field(DocFields.PAGESEQ_START, Integer
        .toString(pagetag.pageseq), Store.YES, Index.UN_TOKENIZED));
//    doc.add(new Field(DocFields.PAGESEQ_END, Integer
//        .toString(contentinfo.lastpage), Store.YES, Index.UN_TOKENIZED));

    doc.add(new Field(DocFields.TEXT, new StringReader(pagetag.pagetext),
        TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field(DocFields.FLAT_TEXT, pagetag.pagetext, Store.YES,
        Index.NO));

    String[] redfields = dbfieldgetter.getFields(contentinfo.itemID);
    if (redfields == null) {
      Logger.log.warn("Document with ID " + contentinfo.itemID
          + " not found in database");
    }
    else {
      addDBFields(doc, redfields);
      doc.removeFields(ItemFields.IDENTIFIER);
    }

    long size = pagetag.pagetext.length();
    try {
      Field fi = doc.getField("identifier");
      System.out.println("Added page with identifier " + fi.stringValue());
      indexmodifier.addDocument(doc);
      indexedbytes += size;
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error adding document "
          + contentinfo.itemID + " page " + contentinfo.firstpage);
    }
  }

  public boolean upToDate(File file, String ID, int pageseq) {
    if (forcereindex)
      return false;
    long modtime = file.lastModified();

    DocHit[] olds = indexitemsearcher.getPageHit(ID, pageseq);
    long oldtime = 0;
    if (olds.length > 0) {
      oldtime = Long.parseLong(olds[0].document.get(DocFields.FILEDATE));
    }
    return oldtime == modtime;
  }

  public void setDBFieldGetter(DBFieldGetter dbfieldgetter) {
    this.dbfieldgetter = dbfieldgetter;
  }

}
