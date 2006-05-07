/*
 * Created on 02-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVReader;

public class ItemCSVReader {
  public int idfield = -1;
  public String[] fieldnames;

  public CSVReader reader;

  public ItemCSVReader(String path) {
    InputStream is;
    try {
      is = new FileInputStream(path);

      Reader r = new InputStreamReader(is, "UTF-8");
      reader = new CSVReader(r);
      fieldnames = reader.readNext();
      for (int i = 0; i < fieldnames.length; ++i) {
        if (fieldnames[i].equals(DocFields.ITEM_IDENTIFIER)) {
          fieldnames[i] = DocFields.ITEMID;
          idfield = i;
        }
      }
      if (idfield == -1) {
        throw new IOException("Item identifier field "
            + DocFields.ITEM_IDENTIFIER
            + " could not be found on first line of file");
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error opening CSV file "
          + path + " for reading");
    }
  }

}
