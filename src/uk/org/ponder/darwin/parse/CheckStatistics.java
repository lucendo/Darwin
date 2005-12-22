/*
 * Created on 12-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.streamutil.write.PrintOutputStream;
import uk.org.ponder.stringutil.StringList;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class CheckStatistics {
  public StringList errors = new StringList();
  int contents;
  int images;
  int pages;
  long time;
  public void report(PrintOutputStream pos) {
    pos.println("\nRead " + pages + " pages, " + images + " images from " + contents + " content files in " + time + "ms");
    int cerrors = errors.size();
    pos.print(cerrors == 1? "There was 1 error" : "There were " + cerrors + " errors");
    pos.println(cerrors == 0? "" : ":");
    for (int i = 0; i < errors.size(); ++ i) {
      pos.println(errors.stringAt(i));
    }
  }
}
