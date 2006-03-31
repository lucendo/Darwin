/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import uk.org.ponder.stringutil.CharWrap;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public interface ParseReceiver {
  public void beginEditable(String editclass);
  public void endEditable(String editclass);
  // called with the literal text of EVERY token
  public void text(XmlPullParser parser, int token, CharWrap text);
  public void metObject(Object tagobj);
  public void beginFile(String contentpath);
  public void endFile();
  // called BEFORE Receiver.metObject.
  public void protoTag(String tagname, String clazz, HashMap attrmap, boolean isempty);
  public void endTag(String tagname);
}
