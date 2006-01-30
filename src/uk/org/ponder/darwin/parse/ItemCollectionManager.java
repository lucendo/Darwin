/*
 * Created on 25-Jan-2006
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.darwin.item.CheckStatistics;
import uk.org.ponder.darwin.item.ItemChecks;
import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.streamutil.write.StringPOS;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;

public class ItemCollectionManager {
  private ItemCollection collection;
  public StringList errors;
  public CheckStatistics statistics;
  
  public boolean busy;
  private String contentroot;
  
  public void setContentRoot(String contentroot) {
    this.contentroot = contentroot;
  }
  
  public ItemCollection getItemCollection() {
    return collection;
  }
  
  public void index() {
    busy = true;
    long time = System.currentTimeMillis();
    ItemCollection newcollection = new ItemCollection();
    StringList parseerrors = TreeLoader.scanTree(contentroot, newcollection);
    
    collection = newcollection;
    statistics = new CheckStatistics();
    statistics.time = System.currentTimeMillis() - time;
    statistics.errors = parseerrors;
    ItemChecks.checkCollection(newcollection, statistics);
    StringPOS pos = new StringPOS();
    statistics.report(pos);
    Logger.log.warn(pos.toString());
    busy = false;
  }
}
