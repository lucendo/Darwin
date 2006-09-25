/*
 * Created on 23-Sep-2006
 */
package uk.org.ponder.darwin.dates;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.stringutil.CharWrap;

public class ProtoDate {
  private static org.apache.log4j.Logger dblog = org.apache.log4j.Logger.getLogger("dblog");
  
  public String startdate;
  public String enddate;
  public String dispalydate;
  public boolean uncertain;
  public boolean imprecise;
  public boolean editorial;

  public ProtoDate(String id, String gillmadness) {
    try {
      parseGillMadness(id, gillmadness);
    }
    catch (Exception e) {
      dblog.warn("Invalid date " + gillmadness + " for item " + id
          + ": " + e.getMessage());
    }
  }

  // Some samples of madness:
  // [1864].08.25--[1864].08.26
  // [1865.02.]15
  // 1882
  // 00.00.14

  // target: YYYYUMMDD
  private void parseGillMadness(String id, String gillmadness) {
    uncertain = gillmadness.indexOf('?') != -1;
    imprecise = gillmadness.indexOf("ca") != -1;
    editorial = gillmadness.indexOf('[') != -1;

    if (gillmadness.indexOf("nd") != -1) {
      startdate = "999999999";
      enddate = "000000000";
      return;
    }

    String reduced = reduceMadness(gillmadness);
    String[] split = reduced.split("-");
    if (split.length == 0 || split.length > 2) {
      throw new IllegalArgumentException("More than 2 date components");
    }
    startdate = parseDate(split[0], true);
    enddate = parseDate(split.length == 2 ? split[1]
        : split[0], false);

  }

  private char uChar(boolean start) {
    return (uncertain || imprecise) ^ start ? '0'
        : '1';
  }

  // returns 8-digit dates
  private String parseDate(String date, boolean start) {
    if (date.charAt(0) == '.') date = date.substring(1);
    if (date.charAt(date.length() - 1) == '.') date = date.substring(0, date.length() - 1);
    
    String[] comps = date.split("\\.", -1);
    if (comps.length < 1 || comps.length > 3) {
      throw new IllegalArgumentException("Invalid date component " + date
          + "(more than 3 components)");
    }
    if (Integer.parseInt(comps[0]) == 0) {
      comps[0] = "0000";
    }
    if (comps.length == 2) {
      comps = (String[]) ArrayUtil.append(comps, "00");
    }
    if (comps[0].length() != 4) {
      throw new IllegalArgumentException("Invalid year component " + comps[0]
          + ": does not have 4 digits and is not 00");
    }
    if (comps.length == 1) {
      return comps[0] + uChar(start) + "0000";
    }

    if (comps[1].length() != 2) {
      throw new IllegalArgumentException("Invalid month component " + comps[1]
          + ": does not have 2 digits");
    }
    int month = Integer.parseInt(comps[1]);
    if (month > 12) {
      throw new IllegalArgumentException("Invalid month component " + comps[1]
          + ": month value greater than 12");
    }
    
    if (comps[2].length() != 2) {
      throw new IllegalArgumentException("Invalid day component " + comps[1]
          + ": does not have 2 digits");
    }
    int day = Integer.parseInt(comps[1]);
    if (day > 31) {
      throw new IllegalArgumentException("Invalid day component " + comps[1]
          + ": month value greater than 31");
    }
    
    return comps[0] + uChar(start) + comps[1] + comps[2];
  }

  private String reduceMadness(String gillmadness) {
    CharWrap reduced = new CharWrap();
    boolean doneminus = false;
    for (int i = 0; i < gillmadness.length(); ++i) {
      char c = gillmadness.charAt(i);
      if (Character.isDigit(c) || c == '.') {
        reduced.append(c);
      }
      if (c == '-' && !doneminus) {
        reduced.append(c);
        doneminus = true;
      }
    }
    return reduced.toString();
  }

}
