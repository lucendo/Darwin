/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.util.UniversalRuntimeException;

/**
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
    // TODO Auto-generated method stub
    
  }

  public void endEditable(String editclass) {
    // TODO Auto-generated method stub
    
  }

  public void text(char[] buffer, int start, int length) {
    // TODO Auto-generated method stub    
  }

  public void text(String text) {
    // TODO Auto-generated method stub
    
  }

  public void metObject(Object tagobj) {
    if (tagobj instanceof DocumentTag) {
      DocumentTag doc = (DocumentTag) tagobj;
      
      if (details != null) {
        throw new UniversalRuntimeException("duplicate tag with attribute " + Attributes.DOCUMENT_CLASS);
       }
      details = collection.getItemSafe(doc.ID);
      content = new ContentInfo();
      content.filename = contentpath;
      content.firstpage = doc.firstpage;
      content.lastpage = doc.lastpage;
      details.addContentInfo(content);
      collection.storeContent(content);
    }
    else if (tagobj instanceof PageTag) {
      PageTag page = (PageTag) tagobj;
      if (currentpage != NO_PAGE && currentpage != page.pageseq - 1) {
        throw new UniversalRuntimeException("Non-consecutive page numbers - " + page.pageseq + 
            " follows page " + currentpage);
      }
      if (page.pageseq < content.firstpage || page.pageseq > content.lastpage) {
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
