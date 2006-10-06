/*
 * Created on 5 Oct 2006
 */
package uk.org.ponder.darwin.pages;

import java.security.MessageDigest;

import uk.org.ponder.stringutil.ByteToCharBase64;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.util.UniversalRuntimeException;

public class CountUtil {
  public static final String getURLHash(String URL) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] bytes = md.digest(URL.getBytes("UTF-8"));
      CharWrap togo = new CharWrap();
      ByteToCharBase64.writeBytes(togo, bytes, 0, 18, false);
      return togo.toString();
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error hashing URL " + URL);
    }
  }
}
