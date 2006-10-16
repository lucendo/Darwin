/*
 * Created on 15-Oct-2006
 */
package uk.org.ponder.darwin.lucene;

import uk.org.ponder.stringutil.CharWrap;

public class TokenExpander {
  static class CharExpand {
    public char c;
    public String expand;

    public CharExpand(char c, String expand) {
      this.c = c;
      this.expand = expand;
    }
  }

  public static CharExpand[] expands = new CharExpand[] {
      new CharExpand('ä', "ae"), new CharExpand('ü', "ue"),
      new CharExpand('ö', "oe"), new CharExpand('ß', "ss") };

  // This unusual strategy avoiding lookup has been demonstrated more efficient
  // on a variety of JVMs
  public static String expandToken(CharWrap toexpand) {
    for (int i = 0; i < toexpand.size; ++i) {
      char c = toexpand.storage[i];
      if (c == 'ä' || c == 'ü' || c == 'ö' || c == 'ß') {
        return expandDefinite(toexpand);
      }
    }
    return null;
  }

  private static String expandDefinite(CharWrap toexpand) {
    CharWrap exd = new CharWrap(toexpand.size * 2);

    for (int i = 0; i < toexpand.size; ++i) {
      char c = toexpand.storage[i];
      switch (c) {
      case 'ä':
        exd.append("ae");
        break;
      case 'ü':
        exd.append("ue");
        break;
      case 'ö':
        exd.append("oe");
        break;
      case 'ß':
        exd.append("ss");
        break;
      default:
        exd.append(c);
      }
    }
    return exd.toString();
  }

}
