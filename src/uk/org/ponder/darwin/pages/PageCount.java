/*
 * Created on 5 Oct 2006
 */
package uk.org.ponder.darwin.pages;

public class PageCount {
  private Integer id;
  private String URL;
  private String URLhash;
  private int count;
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }
  public int getCount() {
    return count;
  }
  public void setCount(int count) {
    this.count = count;
  }
  public String getURL() {
    return URL;
  }
  public void setURL(String url) {
    URL = url;
  }
  public String getURLHash() {
    return URLhash;
  }
  public void setURLHash(String lhash) {
    URLhash = lhash;
  }
  
  public PageCount() {}
  
  public PageCount(String URL, String URLhash) {
    this.URL = URL;
    this.URLhash = URLhash;
    count = 1;
  }
}
