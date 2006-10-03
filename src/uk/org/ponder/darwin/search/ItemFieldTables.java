/*
 * Created on 11-May-2006
 */
package uk.org.ponder.darwin.search;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import uk.org.ponder.stringutil.FilenameUtil;
import uk.org.ponder.util.Logger;

public class ItemFieldTables {
  private String itemdir;

  public void setItemDirectory(String itemdir) {
    this.itemdir = itemdir;
  }

  private Map subtablemap;
  
  public void init() {
    subtablemap = new HashMap();
    for (int i = 0; i < ItemFieldRegistry.tables.length; ++i) {
      String tablename = ItemFieldRegistry.tables[i];
      String tablepath = itemdir + FilenameUtil.filesep + tablename + ".txt";
      FieldTypeInfo fti = (FieldTypeInfo) ItemFieldRegistry.byColumn.get(tablename);
      ItemCSVReader reader = null;
      try {
        reader = new ItemCSVReader(tablepath, false);
        TreeMap thisstm = new TreeMap();
        while (true) {
          String[] fields = reader.reader.readNext();
          if (fields == null)
            break;
          thisstm.put(fields[0], fields[fti.indirectcol]);
        }
        subtablemap.put(tablename, thisstm);
      }
      catch (Exception e) {
        Logger.log.warn("Error reading CSV file " + tablename, e);
      }
      finally {
        if (reader != null && reader.reader != null) {
          try {
            reader.reader.close();
          }
          catch (Throwable t) {
            Logger.log.warn("Error closing CSV file " + tablename, t);
          }
        }
      }
    }
  }

  public Map getTableMap() {
    return subtablemap;
  }
  
}
