/*
 * Created on 13-May-2006
 */
package uk.org.ponder.darwin.graveyard;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.darwin.search.ItemFields;
import uk.org.ponder.intutil.IntEHInnaBox;
import uk.org.ponder.intutil.intVector;
import uk.org.ponder.util.UniversalRuntimeException;

public class CrossIndex {

  public static class CrossIndexEntry {
    int dbitem;
    int docitem;
    intVector docpages = new intVector(10);
  }

  private IndexReader reader;

  public void setIndexReader(IndexReader reader) {
    this.reader = reader;
  }

  IntEHInnaBox dbitemtopages;
  
  Map crossmap;

  private CrossIndexEntry get(String ident) {
    CrossIndexEntry togo = (CrossIndexEntry) crossmap.get(ident);
    if (togo == null) {
      togo = new CrossIndexEntry();
      crossmap.put(ident, togo);
    }
    return togo;
  }

  public void init() {
    
    crossmap = new HashMap();
    dbitemtopages = new IntEHInnaBox(1024);
    
    int maxdoc = reader.maxDoc();

    try {
      for (int i = 0; i < maxdoc; ++i) {
        if (reader.isDeleted(i))
          continue;
        Document doc = reader.document(i);
        String dbident = doc.get(ItemFields.IDENTIFIER);
        if (dbident != null) {
          CrossIndexEntry entry = get(dbident);
          entry.dbitem = i;
        }
        String itemident = doc.get(DocFields.ITEMID);
        if (itemident != null) {
          CrossIndexEntry entry = get(itemident);
          if (doc.get(DocFields.TYPE).equals(DocFields.TYPE_ITEM)) {
            entry.docitem = i;
          }
          else {
            entry.docpages.addElement(i);
          }
        }
      }
      
      for (Iterator valit = crossmap.values().iterator(); valit.hasNext();) {
        CrossIndexEntry entry = (CrossIndexEntry) valit.next();
        if (entry.docpages.size() > 0) {
          for (int i = 0; i < entry.docpages.size(); ++ i) {
            dbitemtopages.put(IntEHInnaBox.hash(entry.dbitem),
                entry.docpages.intAt(i));
          }
        }
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error building cross-index");
    }
  }
}
