/*
 * Created on 04-Apr-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.parse.ContentParser;
import uk.org.ponder.darwin.parse.PageFlatteningParseReceiver;
import uk.org.ponder.darwin.parse.PageReceiver;
import uk.org.ponder.darwin.parse.PageTag;
import uk.org.ponder.util.UniversalRuntimeException;

public class ContentIndexUpdater {
  private IndexBuilder builder;
  private ItemCollection collection;

  public void setIndexBuilder(IndexBuilder builder) {
    this.builder = builder;
  }

  public void setItemCollection(ItemCollection collection) {
    this.collection = collection;
  }

  private void checkContent(final ContentInfo contentinfo) {
    System.out.println("Checking path " + contentinfo.filename);
    ContentParser parser = new ContentParser();
    String path = contentinfo.filename;
    PageFlatteningParseReceiver pfpr = new PageFlatteningParseReceiver();
    pfpr.setPageReceiver(new PageReceiver() {

      public void receivePage(PageTag pagetag) {
        System.out.println("Got page " + pagetag.pageseq + " with "
            + pagetag.pagetext.length() + " characters");
        builder.checkPage(contentinfo, pagetag);
      }
    });
    try {
      FileInputStream fis = new FileInputStream(path);
      parser.parse(fis, path, pfpr);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error opening file at path " + path);
    }
  }

  public void update() {
    builder.open();
    long time = System.currentTimeMillis();
    builder.beginUpdates();
    try {
      Collection itemcoll = collection.getItems();
      for (Iterator itemit = itemcoll.iterator(); itemit.hasNext();) {
        ItemDetails item = (ItemDetails) itemit.next();
        for (int content = 0; content < item.contents.size(); ++content) {
          ContentInfo contentinfo = (ContentInfo) item.contents.get(content);
          checkContent(contentinfo);
        }
      }
    }
    finally {
      builder.endUpdates();
    }
    long delay = System.currentTimeMillis() - time;
    DecimalFormat df = new DecimalFormat("0.000");
    long size = builder.indexedbytes;
    System.out.println("Indexed " + size + " bytes in " + delay + " ms: "
        + df.format((size / (delay * 1000.0))) + "Mb/s");
  }

}
