/*
 * Created on 27-Jan-2006
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.fileutil.FileUtil;

public class URLMapper {
  private String contentroot;
  private String externalroot;

  public void setContentRoot(String contentroot) {
    this.contentroot = contentroot;
  }
  
  public void setExternalContentRoot(String externalroot) {
    this.externalroot = externalroot;
  }
  
  /** Returns a "stub URL" (using forward slashes) of the supplied
   * absolute filename relative to the content root.
   * @param filename
   * @return
   */
  public String getStub(String filename) {
    if (!filename.startsWith(contentroot)) {
      throw new IllegalArgumentException("Filename " + filename 
          + " without prefix " + contentroot + " cannot be mapped");
    }
    String stub = filename.substring(contentroot.length());
    stub = stub.replace('\\', '/');
    return stub;
  }
  
  /** Converts an absolute file path to the external absolute path which will
   * serve it. Useful for image files.
   * @param filename
   * @return
   */
  public String fileToURL(String filename) {
    return externalroot + getStub(filename);
  }
  
  /** Given a relative URL and the absolute page path of the file,
   * return the external absolute path.
   * @param relURL
   * @param pagepath
   * @return
   */
  public String relURLToExternal(String relURL, String pagepath) {
    // Get external URL of THIS page.
    String extpage = fileToURL(pagepath);
    int lastslashpos = extpage.lastIndexOf('/');
    String extstub = extpage.substring(0, lastslashpos + 1);
    return extstub + relURL;
  }
  
  public String relURLToAbsolute(String relURL, String pagepath) {
    String stub = getStub(pagepath);
    int lastslashpos = stub.lastIndexOf('/');
    String relstubdir = stub.substring(0, lastslashpos + 1);
    String fullpath = contentroot + relstubdir + relURL;
    return FileUtil.getCanonicalPath(fullpath);
  }
}
