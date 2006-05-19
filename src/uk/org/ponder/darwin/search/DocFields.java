/*
 * Created on 10-Mar-2006
 */
package uk.org.ponder.darwin.search;

/** Fields as derived from the full text collection, applied to page items in the Lucene
 * index. The DB derived fields are defined in ItemFieldRegistry.
 * @author Antranig Basman (amb26@ponder.org.uk)
 *
 */

public class DocFields {
  public static final String TYPE = "type";
  
  public static final String TYPE_PAGE = "page";
  public static final String TYPE_ITEM = "item";
  
  public static final String FILEDATE = "filedate";
  public static final String ITEMID = "itemID";
  public static final String PAGESEQ_START = "pageseq";
  public static final String PAGESEQ_END = "pageseq-end";
  
  public static final String TEXT = "text";
  public static final String FLAT_TEXT = "flat-text";
  
  public static final String HAS_TEXT = "has-text";
  public static final String HAS_IMAGES = "has-images";
  
}
