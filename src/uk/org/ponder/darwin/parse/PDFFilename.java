package uk.org.ponder.darwin.parse;

import uk.org.ponder.util.UniversalRuntimeException;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class PDFFilename {
  public String ID;
  public String stump;

  public static PDFFilename parse(String filename) {
    try {
      PDFFilename togo = new PDFFilename();
      int lastdotpos = filename.lastIndexOf('.');
      int lastunderpos = filename.lastIndexOf('_', lastdotpos - 1);
      togo.stump = filename.substring(0, lastunderpos);
      togo.ID = filename.substring(lastunderpos + 1, lastdotpos);
      return togo;
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error parsing image filename " + filename);
    }
  }
}
