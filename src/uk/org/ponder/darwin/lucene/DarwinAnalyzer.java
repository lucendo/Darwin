/*
 * Created on 20-Mar-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import uk.org.ponder.streamutil.read.ReadInputStream;
import uk.org.ponder.streamutil.read.ReaderRIS;
import uk.org.ponder.stringutil.CharWrap;

/** A very simple "analyzer" that simply breaks sections of word/non-word
 * characters. 
 * @author Antranig Basman (amb26@ponder.org.uk)
 *
 */

public class DarwinAnalyzer extends Analyzer {
  public static final int STATE_OUTSIDE = 0;
  public static final int STATE_WORD = 1;
  
  ReadInputStream ris;
  int readpos;
  boolean intag = false;
  
  // can't   sea;   Magellan:   "that
  public static boolean isWordChar(char c) {
    return c == '\'' || Character.isLetter(c) || Character.isDigit(c);
  }
  
  public static boolean isCoreWordChar(char c) {
    return Character.isLetter(c) || Character.isDigit(c);
  }
  
  public TokenStream tokenStream(String fieldName, Reader reader) {
    ris = new ReaderRIS(reader);
    readpos = 0;
    intag = false;
    return new TokenStream() {
      public Token next() {
        CharWrap build = new CharWrap();
        int start = readpos;
        int state = STATE_OUTSIDE;
        tokenout: while (true) {
          char c = ris.get();
          if (ris.EOF()) break;
          ++readpos;
          if (intag && c == '>') intag = false;
          if (!intag && c == '<') intag = true;
          switch (state) {
          case STATE_OUTSIDE:
            if (isCoreWordChar(c) && !intag) {
              build.append(Character.toLowerCase(c));
              start = readpos - 1;
              state = STATE_WORD;
            }
            break;
          case STATE_WORD:
            if (!isWordChar(c)) {
              state = STATE_OUTSIDE;
              break tokenout;
            }
            else {
              // normalise by apostrophe removal
              if (c != '\'') {
                build.append(Character.toLowerCase(c));
              }
            }
          }
        }
        return build.size == 0? null : new Token(build.toString(), start, readpos - 1);
      }
      
    };
    
    
  }
}
