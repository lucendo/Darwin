/*
 * Created on 27-Mar-2006
 */
package test;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import uk.org.ponder.darwin.parse.FlatteningReader;
import uk.org.ponder.streamutil.StreamCopyUtil;

public class TestFlatten {
  public static void main(String[] args) {
    String filename = 
      "E:\\flowtalk-jakarta-tomcat-5.5.9\\webapps\\Darwin\\converted\\1835_letters_F1.html";
    try {
      Reader fr = new InputStreamReader(new FileInputStream(filename), "UTF-8");
      FlatteningReader flat = new FlatteningReader(fr);
      String outstring = StreamCopyUtil.readerToString(flat);
      System.out.println(outstring);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}
