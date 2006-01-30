/*
 * Created on 27-Jan-2006
 */
package uk.org.ponder.darwin.parse;

public class URLMapper {
  private String contentroot;
  private String externalroot;

  public void setContentRoot(String contentroot) {
    this.contentroot = contentroot;
  }
  
  public void setExternalContentRoot(String externalroot) {
    this.externalroot = externalroot;
  }
  
  public String fileToURL(String filename) {
    if (!filename.startsWith(contentroot)) {
      throw new IllegalArgumentException("Filename " + filename 
          + " without prefix " + contentroot + " cannot be mapped");
    }
    String stub = filename.substring(contentroot.length());
    stub = stub.replace('\\', '/');
    return externalroot + stub;
  }
  
  public String relURLToExternal(String relURL, String pagepath) {
    String extpage = fileToURL(pagepath);
    int lastslashpos = extpage.lastIndexOf('/');
    String extstub = extpage.substring(0, lastslashpos + 1);
    return extstub + relURL;
  }
}
