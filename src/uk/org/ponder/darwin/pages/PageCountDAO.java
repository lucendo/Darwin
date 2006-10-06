/*
 * Created on 5 Oct 2006
 */
package uk.org.ponder.darwin.pages;

import java.util.Date;

public interface PageCountDAO {
  public int registerAccess(String URL);
  public Date getStartDate();
}
