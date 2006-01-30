/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.item;

import java.util.ArrayList;
import java.util.List;

import uk.org.ponder.arrayutil.ListUtil;

/**
 * Stores all the mapping information for a single "Item" as appearing in the
 * database table. 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class ItemDetails {
  public String ID;
  /** Set to <code>true</code> if thie book supplies ANY page images **/
  public boolean hasimage;
  /** Set to <code>true</code> if thie book supplies ANY textual content **/
  public boolean hastext;
  /** This is a list of PageInfo */
  public List pages = new ArrayList();
  public void addPageInfo(PageInfo pageinfo) {
    pages.add(pageinfo);
  }
  /** This is a list of ContentInfo */
  private List content = new ArrayList();
  public void addContentInfo(ContentInfo contentinfo) {
    content.add(contentinfo);
  }
  /**
   * @param pageseq
   * @return
   */
  public PageInfo acquirePageInfoSafe(int pageseq) {
    ListUtil.expandSize(pages, pageseq + 1);
    PageInfo togo = (PageInfo) pages.get(pageseq);
    if (togo == null) {
      togo = new PageInfo();
      togo.sequence = pageseq;
      pages.set(pageseq, togo);
    }
    return togo;
  }
}
