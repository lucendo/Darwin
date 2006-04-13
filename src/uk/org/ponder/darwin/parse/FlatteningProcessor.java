/*
 * Created on 04-Apr-2006
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.stringutil.CharReceiver;

public class FlatteningProcessor {
  private boolean prevspace;
  private CharReceiver receiver;
  
  public void init() {
    prevspace = false;
  }

  public void setCharReceiver(CharReceiver receiver) {
    this.receiver = receiver;
  }
  
  public int acceptChars(char[] chars, int start, int length) {
    int i = 0;
    for (; i < length; ++i) {
      char c = chars[start + i];
      if (Character.isWhitespace(c)) {
        prevspace = true;
      }
      else {
        if (prevspace) {
          prevspace = false;
          if (receiver.receiveChar(' ')) {
            break;
          }
          
        }
        if (receiver.receiveChar(c)) {
          ++i;
          break;
        }
      }
    }
    return i;
  }
  
}
