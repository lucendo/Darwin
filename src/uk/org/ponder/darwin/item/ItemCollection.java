/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ItemCollection {
  private SortedMap items = new TreeMap();

  private HashMap contents = new HashMap();

  public ItemDetails getItemSafe(String ID) {
    ItemDetails item = (ItemDetails) items.get(ID);
    if (item == null) {
      item = new ItemDetails();
      item.ID = ID;
      items.put(ID, item);
    }
    return item;
  }

  public ItemDetails getItem(String ID) {
    ItemDetails item = (ItemDetails) items.get(ID);
    return item;
  }

  public static final int NEXT = 1;
  public static final int PREV = -1;

  public String adjust(String ID, int direction) {
    try {
      return (String) (direction == NEXT ? 
          items.tailMap(ID+"\0").keySet().iterator().next() 
         : items.headMap(ID).lastKey());
    }
    catch (NoSuchElementException nsee) {
      return null;
    }
  }

  public Collection getItems() {
    return items.values();
  }

  public ContentInfo getContentInfo(String path) {
    return (ContentInfo) contents.get(path);
  }

  public void storeContent(ContentInfo tostore) {
    contents.put(tostore.filename, tostore);
  }

  public Collection getContents() {
    return contents.values();
  }
}
