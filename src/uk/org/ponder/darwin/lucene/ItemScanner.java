/*
 * Created on 24 Aug 2006
 */
package uk.org.ponder.darwin.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class ItemScanner {

  private IndexItemSearcher indexSearcher;

  public void setIndexItemSearcher(IndexItemSearcher indexSearcher) {
    this.indexSearcher = indexSearcher;
  }

  public void loadIndexItems(ItemCollection coll) {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term(DocFields.TYPE, DocFields.TYPE_ITEM)),
        Occur.MUST);
    try {
      Hits hits = indexSearcher.getIndexSearcher().search(query);
      int newitems = 0;
      for (int i = 0; i < hits.length(); ++i) {
        Document doc = hits.doc(i);
        String id = doc.getField("identifier").stringValue();
        ItemDetails details = coll.getItem(id);
        if (details == null) {
          details = coll.getItemSafe(id);
          ++newitems;
        }
      }
      Logger.log.warn("Found " + newitems +" non-content items from " 
          + hits.length() +" index items");
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error scanning index");
    }
  }
}
