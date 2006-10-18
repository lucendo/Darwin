/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.parse;

import java.util.Map;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class Attributes {
  public static String CLASS_ATTR = "dar:class";
  public static String PAGESEQ_ATTR = "dar:pageseq";
  public static String SEQPAGERANGE_ATTR = "dar:seqpagerange";
  public static String ID_ATTR = "dar:id";
  
  public static String DOCUMENT_CLASS = "document";
  public static String PAGE_CLASS = "page";
  public static String SECTION_CLASS = "section";
  
  public static void cleanseDarwin(Map attrmap) {
    attrmap.remove(CLASS_ATTR);
    attrmap.remove(PAGESEQ_ATTR);
    attrmap.remove(SEQPAGERANGE_ATTR);
    attrmap.remove(ID_ATTR);
  }
}
