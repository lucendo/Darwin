/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.item;

import java.util.Iterator;


/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 *  
 */
public class ItemChecks {

  public static CheckStatistics checkCollection(ItemCollection collection, CheckStatistics togo) {
    for (Iterator itemit = collection.getItems().iterator(); itemit.hasNext();) {
      ItemDetails details = (ItemDetails) itemit.next();

      for (int i = 1; i < details.pages.size(); ++i) {
        PageInfo page = (PageInfo) details.pages.get(i);
        String pagetext = page.text == null? "(unknown)" : page.text;
        String location = "page " + pagetext + " with sequence " + i + " of item with ID "
            + details.ID;
        if (page == null) {
          togo.errors.add("No information for " + location);
        }
        else {
          ++togo.pages;
          if (page.contentfile == null && page.imagefile == null) {
            togo.errors.add("Couldn't find either image or content data for " + location);
          }
          if (page.contentfile == null) {
            //togo.errors.add("Could not find content file for " + location);
          }
          else {
            details.hastext = true;
            ContentInfo info = collection.getContentInfo(page.contentfile);
            if (i < info.firstpage || i > info.lastpage) {
              togo.errors.add("Content file " + page.contentfile
                  + " contains out of range " + location);
            }
          }
          if (page.imagefile == null) {
            //togo.errors.add("Could not find image for " + location);
          }
          else {
            details.hasimage = true;
            ++togo.images;
          }
        }
      }
    }
    togo.contents = collection.getContents().size();
    return togo;
  }
}