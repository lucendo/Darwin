/*
 * Created on 08-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class IndexItemSearcher {
  private String indexdir;
  private IndexSearcher indexsearcher;
  private IndexForceOpener forceopener;
  private boolean expectforce;

  public void setIndexDirectory(String indexdir) {
    this.indexdir = indexdir;
  }

  public void setExpectForce(boolean expectforce) {
    this.expectforce = expectforce;
  }
  
  public void setForceOpener(IndexForceOpener forceopener) {
    this.forceopener = forceopener;
    initImpl();
  }
  
  private void initImpl() {
    try {
      if (expectforce) forceopener.forceOpenIndex();
      indexsearcher = new IndexSearcher(indexdir);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error opening index directory " + indexdir + " for searching");
    } 
  }
  
  public void open() {
    if (!expectforce) {
      initImpl();
    }
  }
  
  public IndexSearcher getIndexSearcher() {
    return indexsearcher;
  }
  
  public void close() {
    try {
      indexsearcher.close();
    }
    catch (Exception e) {
      Logger.log.error("Error closing searcher: ", e);
    }
  }
  
  public DocHit[] getHits(Query query) {
    try {
      DocHit[] togo = null;
      long time = System.currentTimeMillis();
      for (int i = 0; i < 1; ++i) {
        Hits hits = indexsearcher.search(query);
        togo = new DocHit[hits.length()];
        int index = 0;
        for (Iterator hitit = hits.iterator(); hitit.hasNext();) {
          togo[index++] = new DocHit((Hit) hitit.next());
        }
      }
      // System.out.println("1 searches for " + togo.length + " in "
      // + (System.currentTimeMillis() - time) + "ms");
      return togo;
    }
    catch (IOException e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error searching for query " + query);
    }
  }

  /**
   * Returns all hits for a particular "Page" item, identified by ID and by page
   * sequence number.
   * 
   * @param ID
   * @param pageseq
   * @return
   */

  public DocHit[] getPageHit(String ID, int pageseq) {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(DocFields.ITEMID, ID)), Occur.MUST);
    query.add(new TermQuery(new Term(DocFields.PAGESEQ_START, Integer
        .toString(pageseq))), Occur.MUST);
    return getHits(query);
  }

  public DocHit[] getItemHit(String ID) {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(DocFields.TYPE, DocFields.TYPE_ITEM)),
        Occur.MUST);
    query.add(new TermQuery(new Term("identifier", ID)), Occur.MUST);
    return getHits(query);
  }
  
}
