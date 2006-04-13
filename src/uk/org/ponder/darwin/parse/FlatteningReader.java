/*
 * Created on 12-Mar-2006
 */
package uk.org.ponder.darwin.parse;

import java.io.IOException;
import java.io.Reader;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import uk.org.ponder.stringutil.CharReceiver;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Agglomerates text
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */

public class FlatteningReader extends Reader {
  private Reader basereader;

  public void close() throws IOException {
    basereader.close();
  }

  CharWrap readbuffer = new CharWrap();
  boolean inbody = false;
  private FlatteningProcessor processor = new FlatteningProcessor();

  // The data being accumulated for the benefit of the current read.
  char[] immbuffer;
  int immoff;
  int immlen;

  int immpos;
  boolean EOF = false;

  protected int[] limits = new int[2];
  protected XmlPullParser parser;

  public int read(char[] cbuf, int off, int len) {
    if (EOF)
      return -1;
    immbuffer = cbuf;
    immoff = off;
    immlen = len;
    immpos = immoff;
    // firstly attempt to satisfy text from buffer.
    if (readbuffer.size > 0) {
      limits[0] = readbuffer.offset;
      limits[1] = readbuffer.size;
      int consumed = processor.acceptChars(readbuffer.storage, limits[0],
          limits[1]);
      readbuffer.offset += consumed;
      readbuffer.size -= consumed;
      if (readbuffer.size == 0)
        readbuffer.offset = 0;
    }
    // if we completely consumed the buffer, go to file.
    if (readbuffer.size == 0 && immpos < (immlen + immoff)) {
      parse();
    }
    return immpos - immoff;
  }

  // accumulate "extra" read characters in order to satisfy a future read.
  private boolean bufferExcess(char[] chars, int written) {
    int length = limits[1];
    int start = limits[0];
    int excess = length - written;
    if (excess > 0) {
      readbuffer.append(chars, start + written, excess);
      return true;
    }
    return false;
  }

  private void parse() {
    try {
      while (true) {
        int token;

        token = parser.nextToken();

        if (token == XmlPullParser.END_DOCUMENT) {
          EOF = true;
          break;
        }
        if (token == XmlPullParser.START_TAG) {
          if (parser.getName().equals("body")) {
            inbody = true;
          }
        }

        else if (token == XmlPullParser.END_TAG) {
          if (parser.getName().equals("body")) {
            inbody = false;
          }
        }
        else if (token == XmlPullParser.TEXT && inbody) {
          char[] chars = parser.getTextCharacters(limits);
          int written = processor.acceptChars(chars, limits[0], limits[1]);
          bufferExcess(chars, written);
          if (immpos == immlen)
            break;
        }
      }
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error parsing text at line " + parser.getLineNumber() + " column "
              + parser.getColumnNumber());
    }
  }

  public FlatteningReader(Reader basereader) {
    this.basereader = basereader;
    parser = new MXParser();
    try {
      parser.setInput(basereader);
    }
    catch (XmlPullParserException e) {
      throw UniversalRuntimeException.accumulate(e,
          "Error setting input stream");
    }

    inbody = false;
    processor.setCharReceiver(new CharReceiver() {

      public boolean receiveChar(char c) {
        immbuffer[immpos] = c;
        immpos++;
        return immpos == (immlen + immoff);
      }
    });
    processor.init();
    EOF = false;
  }

}
