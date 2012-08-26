/*
 * Created on 04-Apr-2006
 */
package uk.org.ponder.darwin.parse;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import uk.org.ponder.stringutil.CharReceiver;
import uk.org.ponder.stringutil.CharWrap;

public class PageFlatteningParseReceiver implements ParseReceiver {
  int currentpage = PageTag.NO_PAGE;
  private PageReceiver pagereceiver;
  private boolean editable;

  public void setPageReceiver(PageReceiver pagereceiver) {
    this.pagereceiver = pagereceiver;
  }

  public void beginEditable(String editclass) {
    editable = true;
  }

  public void endEditable(String editclass) {
    editable = false;
  }

  int[] limits = new int[2];
  private FlatteningProcessor processor = new FlatteningProcessor();
  private static char[] space = {' '};

  public void text(XmlPullParser parser, int token, CharWrap text) {
    // Troll issue of 26/08/12 - &mdash; is elided completely leading to word-run
    if (editable) {
      if (token == XmlPullParser.TEXT) {
        char[] chars = parser.getTextCharacters(limits);
        processor.acceptChars(chars, limits[0], limits[1]);
      }
      else if (token == XmlPullParser.ENTITY_REF) {
        processor.acceptChars(space, 0, 1);
      }
    }
  }

  CharWrap pendingtext = new CharWrap();
  PageTag pendingpage;

  public void metObject(Object tagobj) {
//    System.out.println("PFPR got " + tagobj);
    if (tagobj instanceof DocumentTag) {
      DocumentTag doc = (DocumentTag) tagobj;
      currentpage = doc.firstpage - 1;
    }
    else if (tagobj instanceof PageTag) {
      outPage();
      PageTag page = (PageTag) tagobj;
      // this trollish logic must now be repeated everywhere.
      if (page.pageseq == PageTag.NO_PAGE) {
        page.pageseq = currentpage + 1;
      }
      currentpage = page.pageseq;
      pendingpage = page;
    }

  }

  private void outPage() {
    if (pendingpage != null) {
      pendingpage.pagetext = pendingtext.toString();
      pagereceiver.receivePage(pendingpage);
      pendingpage = null;
      pendingtext.clear();
    }
  }

  public void beginFile(String contentpath) {
    currentpage = PageTag.NO_PAGE;
    pendingpage = null;
    pendingtext.clear();
    processor.setCharReceiver(new CharReceiver() {
      public boolean receiveChar(char c) {
        pendingtext.append(c);
        return false;
      }
    });
    processor.init();
  }

  public void endFile() {
    outPage();
  }

  public void protoTag(String tagname, String clazz, HashMap attrmap,
      boolean isempty) {
  }

  public void endTag(String tagname) {
  }

}
