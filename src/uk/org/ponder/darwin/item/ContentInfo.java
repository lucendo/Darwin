/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.item;

/**
 * Information on a single content file. Several of these may be attached to
 * an Item in ItemDetails.
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ContentInfo {
  /** A relative file path holding the content file **/
  public String filename;
  public int firstpage;
  public int lastpage;
}
