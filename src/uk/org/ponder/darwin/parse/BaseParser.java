/*
 * Created on Jan 19, 2006
 */
package uk.org.ponder.darwin.parse;

import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.util.UniversalRuntimeException;

public class BaseParser {
  protected XmlPullParser parser;
  
  CharWrap tokenbuffer = new CharWrap();
  
  protected int[] limits = new int[2];
  protected String currenteditableclass;

  public static String getAttrExpected(Map attrmap, String attrname) {
    String attr = (String) attrmap.get(attrname);
    if (attr == null) {
      throw new UniversalRuntimeException("attribute " + attrname
          + " expected ");
    }
    return attr;
  }
  
  protected boolean testComment() {
    char[] chars = parser.getTextCharacters(limits);
    String body = new String(chars, limits[0], limits[1]).trim();
    if (body.startsWith(Constants.BEGIN_EDITABLE)) {
      if (currenteditableclass != null) {
        throw new UniversalRuntimeException(
            "BeginEditable comment found in body");
      }
  
      int quot = body.indexOf('"');
      int endquot = body.lastIndexOf('"');
      if (quot == -1 || endquot == -1 || endquot <= quot) {
        throw new UniversalRuntimeException("Error in " + Constants.BEGIN_EDITABLE
            + ": could not parse quoted name");
      }
      currenteditableclass = body.substring(quot + 1, endquot);
      return true;
    }
    if (body.startsWith(Constants.END_EDITABLE)) {
      currenteditableclass = null;
      return true;
    }
    return false;
  }
 
  protected CharWrap renderToken(int token) {
    tokenbuffer.clear();
    char[] chars = parser.getTextCharacters(limits);
    switch (token) {
    case XmlPullParser.COMMENT:
      tokenbuffer.append("<!--");
      break;
    case XmlPullParser.ENTITY_REF:
      tokenbuffer.append("&");
      break;
    case XmlPullParser.CDSECT:
      tokenbuffer.append("<![CDATA[");
      break;
    case XmlPullParser.PROCESSING_INSTRUCTION:
      tokenbuffer.append("<?");
      break;
    case XmlPullParser.DOCDECL:
      tokenbuffer.append("<!DOCTYPE");
      break;
    }
    tokenbuffer.append(chars, limits[0], limits[1]);
    switch (token) {
    case XmlPullParser.COMMENT:
      tokenbuffer.append("-->");
      break;
    case XmlPullParser.ENTITY_REF:
      tokenbuffer.append(";");
      break;
    case XmlPullParser.CDSECT:
      tokenbuffer.append("]]>");
      break;
    case XmlPullParser.PROCESSING_INSTRUCTION:
      tokenbuffer.append("?>");
      break;
    case XmlPullParser.DOCDECL:
      tokenbuffer.append(">");
      break;
    }
    return tokenbuffer;
  }

}
