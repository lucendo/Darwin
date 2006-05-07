/*
 * Created on 02-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.text.DecimalFormat;

import uk.org.ponder.util.UniversalRuntimeException;

public class ItemIndexUpdater {
  private IndexBuilder builder;
  private String itemfile;

  public void setIndexBuilder(IndexBuilder builder) {
    this.builder = builder;
  }

  public void setItemFile(String itemfile) {
    this.itemfile = itemfile;
  }

  public void update() {
    long time = System.currentTimeMillis();
    builder.beginUpdates();
    try {
      ItemCSVReader reader = new ItemCSVReader(itemfile);
      while (true) {
        String[] fields = reader.reader.readNext();
        if (fields == null)
          break;
        builder.checkItem(reader.fieldnames, fields, reader.idfield);
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error reading item file ");
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
