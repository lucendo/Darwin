/*
 * Created on 5 Oct 2006
 */
package uk.org.ponder.darwin.pages;

import java.util.Date;

public class StartDate {
  private int id;
  private Date date;
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
}
