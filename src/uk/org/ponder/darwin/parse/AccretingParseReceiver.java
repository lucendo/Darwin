/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * A parser receiver which accepts parsed data and accretes into a global
 * collection of known items and details.
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class AccretingParseReceiver implements ParseReceiver {
  private static final int NO_PAGE = -1;
  public AccretingParseReceiver(ItemCollection collection) {
    this.collection = collection;
  }
  
  int currentpage = NO_PAGE;
  
  private ItemCollection collection;
  private ItemDetails details;
  private ContentInfo content;
  
  private String contentpath;
  
  public void beginFile(String contentpath) {
    this.contentpath = contentpath;
  }
  
  public void beginEditable(String editclass) {
  }

  public void endEditable(String editclass) {
  }

  public void text(XmlPullParser parser, int token, CharWrap text) {
  }

  public void protoTag(String tagname, String clazz, HashMap attrmap, boolean isempty) {
  }
  
  public void endTag(String tagname) {
  }
  
  public void metObject(Object tagobj) {
    if (tagobj instanceof DocumentTag) {
      DocumentTag doc = (DocumentTag) tagobj;
      
      if (details != null) {
        throw new UniversalRuntimeException("duplicate tag with attribute " + Attributes.DOCUMENT_CLASS);
       }
      details = collection.getItemSafe(doc.ID);
      content = new ContentInfo();
      content.itemID = details.ID;
      content.filename = contentpath;
      content.firstpage = doc.firstpage;
      content.lastpage = doc.lastpage;
      currentpage = doc.firstpage - 1;
      details.addContentInfo(content);
      collection.storeContent(content);
    }
    else if (tagobj instanceof PageTag) {
      PageTag page = (PageTag) tagobj;
      if (content == null) {
        throw new UniversalRuntimeException("Page tag met in " + contentpath + " without document class first");
      }
      // This section commented out to 
//      if (currentpage != NO_PAGE && currentpage != page.pageseq - 1) {
//        throw new UniversalRuntimeException("Non-consecutive page numbers - " + page.pageseq + 
//            " follows page " + currentpage);
//      }
      if (page.pageseq == PageTag.NO_PAGE) {
        page.pageseq = currentpage + 1;
      }
      else if (page.pageseq < content.firstpage || page.pageseq > content.lastpage) {
        throw new UniversalRuntimeException("Page " + page.pageseq + " is outside the range "
           + content.firstpage + "-" + content.lastpage + " advertised ");
      }
     
      currentpage = page.pageseq;
      PageInfo pageinfo = details.acquirePageInfoSafe(page.pageseq);
      pageinfo.contentfile = contentpath;
      pageinfo.text = page.pagetext;
    }
  }

  public void endFile() {
    if (content == null) {
      throw new UniversalRuntimeException("Document ended without dar:class=\"document\" attribute seen");
    }
    details = null;
    currentpage = 0;
    content = null;
  }


}
