/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.parse;

/**
 * Methods for interpreting the content types represented by different file
 * extensions.
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class Extensions {
  public static String[] contentextns = new String[] {"html", "htm", "xhtml"};
  public static String[] imgextns = new String[] {"tif", "tiff", "jpg", "png", "djvu"};
  public static boolean isContentFile(String extension) {
    for (int i = 0; i < contentextns.length; ++ i) {
      if (extension.equals(contentextns[i])) return true;
    }
    return false;
  }
  public static boolean isImageFile(String extension) {
    for (int i = 0; i < imgextns.length; ++ i) {
      if (extension.equals(imgextns[i])) return true;
    }
    return false;
  }
  
  public static final String getExtension(String filename) {
    int lastdotpos = filename.lastIndexOf('.');
    return filename.substring(lastdotpos + 1);
  }
}
