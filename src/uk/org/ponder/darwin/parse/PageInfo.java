/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Information about a single content page. 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class PageInfo {
  // a reasonable maximum to avoid overflowing memory elsewhere.
  public static final int PAGE_MAX = 10000;
  public int sequence;
  public String text;
  public String imagefile;
  public String contentfile;
  public int startoffset, endoffset;
  
  public static int parsePageSeq(String pageseq) {
    try {
      int togo = Integer.parseInt(pageseq);
      if (togo < 0 || togo > PAGE_MAX) {
        throw new UniversalRuntimeException("");
      }
      return togo;
    }
    catch (Exception e) {
      throw new UniversalRuntimeException("Page number " + pageseq + " is not valid ");
    }
  }
}
