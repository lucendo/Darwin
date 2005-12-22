/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.util.UniversalRuntimeException;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 *  
 */
public class DocumentTag {
  public String ID;
  public int firstpage;
  public int lastpage;

  /**
   * Parse the supplied string representing a page range in the form 35-78,
   * representing an inclusive page range, into the supplied ContentInfo object.
   * 
   * @param seqpagerange
   * @param ci
   */
  public void applySeqText(String seqpagerange) {

    int hypindex = seqpagerange.indexOf('-');
    if (hypindex == -1) {
      throw new UniversalRuntimeException(
          "seqpagerange attribute does not contain hyphen");
    }
    String prevtext = seqpagerange.substring(0, hypindex);
    try {
      firstpage = Integer.parseInt(prevtext);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, prevtext
          + " cannot be interpreted as a page number");
    }
    String subtext = seqpagerange.substring(hypindex + 1);
    try {
      lastpage = Integer.parseInt(subtext);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, subtext
          + " cannot be interpreted as a page number");
    }
    if (firstpage > lastpage) {
      throw new UniversalRuntimeException("Final page " + lastpage
          + " is earlier than initial page " + firstpage);
    }

  }
}