/*
 * Created on 30-Oct-2005
 */
package uk.org.ponder.darwin.parse;

import java.io.EOFException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
// http://www.extreme.indiana.edu/viewcvs/XPP3/java/src/java/mxp1_min/org/xmlpull/mxp1/MXParser.java?rev=1.48&content-type=text/vnd.viewcvs-markup

import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Parses a complete, single content file in XHTML, expecting page and sectional
 * markup using the dar: format, as well as DreamWeaver template comment
 * markers, of the form #BeginEditable &c.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ContentParser extends BaseParser {
  Object pendingbody = null;
  CharWrap pendingbodytext = new CharWrap();

  ParseReceiver receiver;
  StringList errors;

  public void init() {
    currenteditableclass = null;
    pendingbody = null;
    errors = new StringList();
  }

  private void signalError(String string) {
    int line = parser.getLineNumber();
    int column = parser.getColumnNumber();
    String message = " at line " + line + ", column " + column + ":\n  "
        + string;
    errors.add(message);
  }

  /**
   * Investigates an open tag for one of the special dar: attributes, either a
   * document, page or section.
   */
  private void checkOpenTag(boolean isempty) {
    String tagname = parser.getName();
    HashMap attrmap = new HashMap();
    int attrs = parser.getAttributeCount();
    for (int i = 0; i < attrs; ++i) {
      String attrname = parser.getAttributeName(i);
      String attrvalue = parser.getAttributeValue(i);
      attrmap.put(attrname, attrvalue);
    }
    String clazz = (String) attrmap.get(Attributes.CLASS_ATTR);
    receiver.protoTag(tagname, clazz, attrmap, isempty);
    if (clazz != null) {
      if (clazz.equals(Attributes.DOCUMENT_CLASS)) {
        String ID = getAttrExpected(attrmap, Attributes.ID_ATTR);
        String seqrange = getAttrExpected(attrmap, Attributes.SEQPAGERANGE_ATTR);
        DocumentTag doctag = new DocumentTag();
        doctag.ID = ID;
        doctag.applySeqText(seqrange);
        receiver.metObject(doctag);
      }
      else if (clazz.equals(Attributes.PAGE_CLASS)) {
        PageTag pendingpage = new PageTag();
        String pageseq = (String) attrmap.get(Attributes.PAGESEQ_ATTR);
        // getAttrExpected(attrmap, Attributes.PAGESEQ_ATTR);
        if (pageseq != null) {
          pendingpage.pageseq = PageInfo.parsePageSeq(pageseq);
        }
        pendingbody = pendingpage;

        // wait until close tag to emit pending page.
      }
      else {
        Logger.log.warn("Unrecognised dar:class: " + clazz);
      }
    }
  }

  public StringList parse(InputStream xmlstream, String contentpath,
      ParseReceiver receiver) {
    long time = System.currentTimeMillis();

    init();
    this.receiver = receiver;
    receiver.beginFile(contentpath);

    parser = new MXParser();
    try {
      // parser.setFeature(FEATURE_XML_ROUNDTRIP, true);
      parser.setInput(xmlstream, "UTF-8");
      while (true) {
        try {
          int token = parser.nextToken();
          if (token == XmlPullParser.END_DOCUMENT)
            break;
          CharWrap tokenchars = renderToken(token);
          receiver.text(parser, token, tokenchars);
          // if (this.currenteditableclass != null) {
          if (token == XmlPullParser.START_TAG) {
            boolean isempty = parser.isEmptyElementTag();
            checkOpenTag(isempty);
            if (isempty) {
              parser.next();
            }
          }
          else if (token == XmlPullParser.END_TAG) {
            receiver.endTag(parser.getName());
          }
          if (pendingbody != null) {
            pendingBody(token);
          }
          // }
          if (token == XmlPullParser.COMMENT) {
            String oldclass = currenteditableclass;
            boolean ischange = testComment();
            if (ischange && oldclass == null) {
              receiver.beginEditable(currenteditableclass);
            }
            else if (ischange && oldclass != null) {
              receiver.endEditable(oldclass);
            }
          }
        }
        catch (EOFException eofe) {
          break;
        }
        catch (Exception e) {
          Logger.log.log(Level.WARN, "Error handling token", e);
          signalError(e.getMessage());
          if (e instanceof EOFException) {
            break;
          }
        }

      }

    }
    catch (Throwable t) {
      throw UniversalRuntimeException.accumulate(t, "Error parsing file: ");
    }
    finally {
      try {
        receiver.endFile();
      }
      catch (Exception e) {
        Logger.log.log(Level.WARN, "Error parsing file", e);
        signalError(e.getMessage());
      }
    }
    // System.out.println("in " +(System.currentTimeMillis() - time) + "ms");
    return errors;
  }

  private void pendingBody(int token) {
    if (token == XmlPullParser.TEXT) {
      char[] chars = parser.getTextCharacters(limits);
      pendingbodytext.append(chars, limits[0], limits[1]);
    }
    else if (token == XmlPullParser.END_TAG) {
      PageTag togo = (PageTag) pendingbody;
      togo.pagenotext = pendingbodytext.toString();
      pendingbodytext.clear();
      pendingbody = null;
      receiver.metObject(togo);
    }
  }

}