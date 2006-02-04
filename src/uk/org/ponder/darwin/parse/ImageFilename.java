/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ImageFilename {
  public String ID;
  public int pageseq;
  public String stump;

  public static ImageFilename parse(String filename) {
    try {
      ImageFilename togo = new ImageFilename();
      int lastdotpos = filename.lastIndexOf('.');
      int lastunderpos = filename.lastIndexOf('_', lastdotpos - 1);
      int nextunderpos = filename.lastIndexOf('_', lastunderpos - 1);
      togo.stump = filename.substring(0, nextunderpos);
      String pageseq = filename.substring(lastunderpos + 1, lastdotpos);
      if (pageseq.startsWith("fig")) return null;
      togo.pageseq = PageInfo.parsePageSeq(pageseq);
      togo.ID = filename.substring(nextunderpos + 1, lastunderpos);
      return togo;
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error parsing image filename " + filename);
    }
  }
}
