/*
 * Created on 07-May-2006
 */
package uk.org.ponder.darwin.search;

public class FieldTypeInfo {
  public static final int TYPE_FREE_STRING = 0;
  public static final int TYPE_BOOLEAN = 1;
  public static final int TYPE_STRING_INDIRECT = 2;
  public static final int TYPE_DATE = 3;
  public static final int TYPE_KEYWORD = 4;
  public static final int TYPE_STRING_NONINDEX = 5;
  public static final int TYPE_STRING_SIMPLE = 6;
  
  public String DBfieldname;
  public String paramname;
  public int fieldtype;
  
  public String indirectname;
  public int indirectcol;

  public FieldTypeInfo(String dbfieldname, String paramname, int fieldtype, String indirectname) {
    this.DBfieldname = dbfieldname;
    this.paramname = paramname;
    this.fieldtype = fieldtype;
    this.indirectname = indirectname;
  }

  public FieldTypeInfo(String dbfieldname, String paramname, int fieldtype, String indirectname, int indirectcol) {
    this.DBfieldname = dbfieldname;
    this.paramname = paramname;
    this.fieldtype = fieldtype;
    this.indirectname = indirectname;
    this.indirectcol = indirectcol;
  }

  public FieldTypeInfo(String dbfieldname, String paramname, int fieldtype) {
    this.DBfieldname = dbfieldname;
    this.paramname = paramname;
    this.fieldtype = fieldtype;
  }
  
}
