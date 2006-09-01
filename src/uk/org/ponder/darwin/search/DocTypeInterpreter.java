/*
 * Created on 18-May-2006
 */
package uk.org.ponder.darwin.search;

import java.util.Map;

import uk.org.ponder.stringutil.StringSet;

public class DocTypeInterpreter {
  public static final int BOOK = 1;
  public static final int PAMPHLET = 2;
  public static final int BOOK_CHAPTER = 3;
  public static final int INTRODUCTION = 4;
  public static final int PREFACE = 5;
  public static final int PERIODICAL_CONTRIBUTION = 6;
  public static final int DARWIN_EXTRACT = 7;
  public static final int OFFPRINT = 8;
  public static final int ABSTRACT = 9;
  public static final int CORRESPONDENCE = 10;
  public static final int DATASHEET = 11;
  public static final int DRAFT = 12;
  public static final int FIGURE = 13;
  public static final int LEGAL = 14;
  public static final int MISCELLANEOUS = 15;
  public static final int NOTE = 16;
  public static final int PHOTO = 17;
  public static final int PRINTED = 18;
  public static final int REVIEW = 19;
  
  private StringSet concises;
  
  // map of String Integer to String name.
  private Map doctypes;
  
  public void setItemFieldTables(ItemFieldTables fieldtables) {
    FieldTypeInfo typeinfo = (FieldTypeInfo) ItemFieldRegistry.byDBField.get(ItemFields.PART_DOC_ID); 
    doctypes = (Map) fieldtables.getTableMap().get(typeinfo.indirectname);
    concises = new StringSet();
    for (int i = 1; i <= 8; ++ i) {
      concises.add(doctypes.get(Integer.toString(i)));
    }
    concises.add(doctypes.get("19"));
  }
  
  public boolean isType(String stringval, int type) {
    return doctypes.get(Integer.toString(type)).equals(stringval);
  }
  
  public boolean isConciseType(String stringval) {
    return concises.contains(stringval);
  }
}
