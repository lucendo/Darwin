/*
 * Created on 02-May-2006
 */
package uk.org.ponder.darwin.search;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVReader;

public class ItemCSVReader {
  public int idfield = -1;
  public String[] fieldnames;

  public CSVReader reader;

  public ItemCSVReader(String path, boolean hasfieldnames) {
    InputStream is;
    try {
      is = new FileInputStream(path);

      Reader r = new InputStreamReader(is, "UTF-8");
      reader = new CSVReader(r);
      if (hasfieldnames) {
        fieldnames = reader.readNext();

        for (int i = 0; i < fieldnames.length; ++i) {
          if (fieldnames[i].equals(ItemFields.IDENTIFIER)) {
            fieldnames[i] = DocFields.ITEMID;
            idfield = i;
          }
        }
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error opening CSV file "
          + path + " for reading");
    }
  }

}
