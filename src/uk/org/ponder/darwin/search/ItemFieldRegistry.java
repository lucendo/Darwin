/*
 * Created on 08-May-2006
 */
package uk.org.ponder.darwin.search;

import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.stringutil.StringList;

//"ID","No","Identifier","ConciseReference","Title","AttributedTitle","Date","DateString","Edition","Issue","Notes","Name","DC_Subject","DC_Type","LanguageID","PartOfDocumentID","PublisherID","PeriodicalID","PlacePublishedID","RelationTypeID","RelationTargetID","DevelopmentNotes","HaveDigitalText","HaveFacsimile","Place created","Description","Xref","In progress","Have reproduction permission","Copyright","Document type"
//1859,"F0002","F2","Darwin, C. R. 1836. Extracts of letters from C. Darwin, Esq., to Professor Henslow. Printed for private distribution. Entomological Magazine 3 (5): 457-460.",,"Letters on Geology",,"1836",,,"2.  1836  Extracts of letters from C. Darwin, Esq., to Professor Henslow. Printed for private distribution. Entomological Magazine, Vol. 3, No. V, Art. XLIII, pp. 457-460. Entomological extracts only from No. 1.","Darwin Charles Robert",,,1,6,,11,20,0,0,,0,1,,,,,,,

public class ItemFieldRegistry {
  public static FieldTypeInfo[] infos = new FieldTypeInfo[] {
    // ID field name is swapped on read
    new FieldTypeInfo(DocFields.ITEMID, "identifier", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.TITLE, "exacttitle", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.ATTRIBTITLE, "attributedtitle", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.CONCISE, "reference", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.LANGUAGE_ID, "language", FieldTypeInfo.TYPE_KEYWORD, "Language", 2),
    new FieldTypeInfo(ItemFields.PLACE_ID, "place", FieldTypeInfo.TYPE_FREE_STRING, "Place", 1),
    new FieldTypeInfo(ItemFields.PART_DOC_ID, "documenttype", FieldTypeInfo.TYPE_KEYWORD, "PartOfDocument", 1),
    new FieldTypeInfo(ItemFields.PUBLISHER_ID, "publisher", FieldTypeInfo.TYPE_FREE_STRING, "Publisher", 1),
    new FieldTypeInfo(ItemFields.PERIODICAL_ID, "periodical", FieldTypeInfo.TYPE_FREE_STRING, "Periodical", 1),
    new FieldTypeInfo(ItemFields.NAME, "name", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.NOTES, "notes", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.DESCRIPTION, "description", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.XREF, "xref", FieldTypeInfo.TYPE_STRING_NONINDEX),
    new FieldTypeInfo(ItemFields.HAVE_DIGITAL, "havetext", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.HAVE_FACS, "haveimages", FieldTypeInfo.TYPE_KEYWORD),
    
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "manuscript", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "published", FieldTypeInfo.TYPE_KEYWORD),
    
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "startdate", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "enddate", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "searchid", FieldTypeInfo.TYPE_STRING_SIMPLE),
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "searchtitle", FieldTypeInfo.TYPE_FREE_STRING),
    new FieldTypeInfo(ItemFields.SYNTHESIZED, "sorttitle", FieldTypeInfo.TYPE_KEYWORD),
    new FieldTypeInfo(ItemFields.DATESTRING, "displaydate", FieldTypeInfo.TYPE_STRING_NONINDEX),
  };
  
  public static Map byDBField = new HashMap();
  public static Map byParam = new HashMap(); // search parameter name
  public static Map byColumn = new HashMap();
  public static String[] tables;
  
  static {
    StringList tablelist = new StringList();
    for (int i = 0; i < infos.length; ++ i) {
      byDBField.put(infos[i].DBfieldname, infos[i]);
      String paramname = infos[i].paramname;
      if (paramname != null) {
        byParam.put(paramname, infos[i]);
      }
      if (infos[i].indirectname != null) {
        tablelist.add(infos[i].indirectname);
        byColumn.put(infos[i].indirectname, infos[i]);
      }
    }
    tables = tablelist.toStringArray();
  }
}
