/*
 * Created on Jan 18, 2006
 */
package uk.org.ponder.darwin.parse;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.springframework.core.io.InputStreamSource;
import org.xmlpull.v1.XmlPullParser;

import uk.org.ponder.streamutil.StreamCopyUtil;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.util.UniversalRuntimeException;
import uk.org.ponder.xml.XMLUtil;
import uk.org.ponder.xml.XMLWriter;

/** Receives character events from both an upstream CONTENT ContentParser,
 * and ALSO operates its own BaseParser to parser a Template. At the same
 * time, rewrites page links to include Javascript and anchors.
 * @author Antranig Basman (amb26@ponder.org.uk)
 */
// This class is written in inestimably poor style. I apologise.
public class RenderingParseReceiver extends BaseParser implements ParseReceiver {

  private OutputStream os;
  private XMLWriter xmlw;
  private CharWrap buffer = new CharWrap();
  // The status of the CONTENT reader.
  boolean editable;
  // currenteditableclass is set ABOVE to reflect TEMPLATE reader, which we
  // are reading with our own tokens.
  private InputStreamSource templatesource;

  public void setOutputStream(OutputStream os) {
    this.os = os;
    xmlw = new XMLWriter(os);
  }

  public void setTemplateSource(InputStreamSource templatesource) {
    this.templatesource = templatesource;
  }

  // called by the CONTENT reader when tag is received.
  public void protoTag(String tagname, String clazz, HashMap attrmap) {
    flushBuffer(true);
    if (Attributes.PAGE_CLASS.equals(clazz)) {
      // I am without style
      attrmap.put("onClick", "onPageClick(this.getAttribute('dar:pageseq')); return false;");
      String pageseq = getAttrExpected(attrmap, Attributes.SEQPAGERANGE_ATTR);
      attrmap.put("name", "pageseq-" + pageseq);
    }
    xmlw.writeRaw("<" + tagname + " ");
    XMLUtil.dumpAttributes(attrmap, xmlw);
    
    xmlw.writeRaw(">");
  }
  // text received from CONTENT.
  public void text(XmlPullParser parser, int token, CharWrap text) {
    if (editable  && token != XmlPullParser.START_TAG) {
      buffer.append(text);
    }
  }

  public void beginEditable(String editclass) {
    editable = true;
  }

  public void endEditable(String editclass) {
    if (editclass.equals(Constants.TITLE)) {
      scanTemplate(Constants.BODY);
    }
    else {
      scanTemplate(null);
    }
  }

  public void metObject(Object tagobj) {

  }
// skim along the TEMPLATE until we hit an editable section, then since we
// stop scanning, the CONTENT will continue to be delivered from the outside.
  private void scanTemplate(String required) {
    while (true) {
      try {
        int token = parser.nextToken();
        if (token == XmlPullParser.COMMENT) {
          if (token == XmlPullParser.END_DOCUMENT)
            break;
          if (required != null && testComment()) {
            if (currenteditableclass.equals(required))
              return;
          }
          if (currenteditableclass == null) {
            CharWrap text = renderToken(token);
            buffer.append(text);
            flushBuffer(false);
          }
         
        }
      }
      catch (Throwable t) {
        throw UniversalRuntimeException.accumulate(t, "Error parsing file: ");
      }
    }
  }

  public void beginFile(String contentpath) {
    editable = false;
    currenteditableclass = null;
    // parser.setFeature(FEATURE_XML_ROUNDTRIP, true);
    try {
      InputStream xmlstream = templatesource.getInputStream();
      parser.setInput(xmlstream, null);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error opening template file");
    }
    scanTemplate(Constants.TITLE);
  }

  public void endFile() {
    flushBuffer(true);
  }

  private void flushBuffer(boolean unconditional) {
    // TODO: At this point we will highlight search hits, since we will
    // have ViewParameters as a dependence. Before this can happen, we need
    // to refactor ViewParameters so that the payload has no further RSF
    // dependence.
    if (unconditional || buffer.size > StreamCopyUtil.PROCESS_BUFFER_SIZE) {
      xmlw.writeRaw(buffer.storage, 0, buffer.size);
      buffer.clear();
    }
  }


}
