/*
 * Created on 07-May-2006
 */
package uk.org.ponder.darwin.search;

public class SearchParams {
  
  public static final String SORT_RELEVANCE = "relevance";
  public static final String SORT_DATEASC = "date-ascending";
  public static final String SORT_DATEDESC = "date-descending";
  public static final String SORT_TITLE = "title";
  public static final String SORT_IDENTIFIER = "identifier";
  
  public static final String[] SORT_OPTIONS = new String[] {
    SORT_RELEVANCE, SORT_DATEASC, SORT_DATEDESC, SORT_TITLE, SORT_IDENTIFIER
  };
  
  public static final String[] SORT_OPTIONS_NAMES = new String[] {
    "Relevance", "Oldest To Most Recent", "Most Recent To Oldest", "Title", "Identifier"
  };
  
  public String freetext;
  public String searchid;
  public String dateafter;
  public String datebefore;
  public String reference;
  public String[] language;
  public String[] documenttype;
  public String place;
  public String searchtitle;
  public String publisher;
  public String periodical;
  public String description;
  public String name;
  public String allfields;
  
  public Boolean manuscript;
  public Boolean published;
  
  public Boolean havetext;
  public Boolean haveimages;
  
  public String sort = SORT_DATEASC;
  }
