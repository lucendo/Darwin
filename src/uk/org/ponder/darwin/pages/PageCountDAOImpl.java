/*
 * Created on 5 Oct 2006
 */
package uk.org.ponder.darwin.pages;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.org.ponder.util.Logger;

public class PageCountDAOImpl extends HibernateDaoSupport implements
    PageCountDAO {
  private Date startDate;

  public int registerAccess(String URL) {
    HibernateTemplate template = getHibernateTemplate();
    String hash = CountUtil.getURLHash(URL);
    List res = template
        .findByNamedParam(
            "from PageCount as count where count.URLHash = :hash", "hash",
            hash);
    int cres = res.size();
    PageCount count = null;
    if (cres == 0) {
      count = new PageCount(URL, hash);
    }
    else {
      count = (PageCount) res.get(0);
      count.setCount(count.getCount() + 1);
      template.save(count);
      if (cres != 1) {
        Logger.log.warn("Found " + cres + " results for URL query " + URL
            + " hash " + hash);
      }
    }
    return count.getCount();
  }

  public Date getStartDate() {
    if (startDate == null) {
      HibernateTemplate template = getHibernateTemplate();
      List start = template.find("from StartDate");
      if (start.size() == 0) {
        StartDate date = new StartDate();
        date.setDate(new Date());
        template.save(date);
        startDate = date.getDate();
      }
      else {
        startDate = ((StartDate) start.get(0)).getDate();
      }
    }
    return startDate;
  }

}
