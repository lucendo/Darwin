/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.item;

import java.util.Iterator;

import uk.org.ponder.util.Logger;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ItemChecks {

  public static CheckStatistics checkCollection(ItemCollection collection,
      CheckStatistics togo) {
    for (Iterator itemit = collection.getItems().iterator(); itemit.hasNext();) {
      ItemDetails details = (ItemDetails) itemit.next();
      if (details.haspdf && details.pages.size() == 0) {
         togo.errors.add("PDF file " + details.pdffile + " corresponds to item ID " + details.ID 
             + " which has no page data");
      }

      for (int i = 1; i < details.pages.size(); ++i) {
        String location = "Page with sequence " + i + " of item with ID "
            + details.ID;
        PageInfo page = (PageInfo) details.pages.get(i);
        try {
          if (page == null) {
            togo.errors.add(location + " is missing all data");
            continue;
          }
          String pagetext = page.text == null ? "(unknown)"
              : page.text;
          location = "page " + pagetext + " with sequence " + i
              + " of item with ID " + details.ID;

          ++togo.pages;
          if (page.contentfile == null && page.imagefile == null) {
            togo.errors.add("Couldn't find either image or content data for "
                + location);
          }
          if (page.contentfile == null) {
            // togo.errors.add("Could not find content file for " + location);
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
            if (details.hasimage) {
              togo.errors.add("Image missing in sequence for " + location);
            }
          }
          else {
            details.hasimage = true;
            ++togo.images;
          }
        }
        catch (Exception e) {
          togo.errors.add("Unexpected error " + e + " scanning " + location);
          Logger.log.error(e);
        }
      }
    }
    togo.contents = collection.getContents().size();
    return togo;
  }
}