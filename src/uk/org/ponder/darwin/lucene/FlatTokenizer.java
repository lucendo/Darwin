/*
 * Created on 8 Mar 2007
 */
package uk.org.ponder.darwin.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;

public class FlatTokenizer extends CharTokenizer {

  public FlatTokenizer(Reader in) {
    super(in);
  }
  
  protected boolean isTokenChar(char c) {
    return !Character.isWhitespace(c);
  }

  protected char normalize(char c) {
    return Character.toLowerCase(c);
  }
  
}
