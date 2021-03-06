/*
 * Created on 02-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.darwin.dates.ProtoDate;
import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.search.DocTypeInterpreter;
import uk.org.ponder.darwin.search.FieldTypeInfo;
import uk.org.ponder.darwin.search.ItemCSVReader;
import uk.org.ponder.darwin.search.ItemFieldRegistry;
import uk.org.ponder.darwin.search.ItemFieldTables;
import uk.org.ponder.darwin.search.ItemFields;
import uk.org.ponder.intutil.Algorithms;
import uk.org.ponder.intutil.intVector;
import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.stringutil.FilenameUtil;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

// currently not Spring-configured since we are imagining indexing use from
// the command line initially.
public class ItemIndexUpdater implements DBFieldGetter {
  
  private static org.apache.log4j.Logger dblog = org.apache.log4j.Logger.getLogger("dblog");
  
  private IndexBuilder builder;
  private String itemdir;
  private Map subtablemap;
  private ItemCollection itemcollection;
  private boolean updateindex;
  private DocTypeInterpreter doctypeinterpreter;

  public void setItemDirectory(String itemdir) {
    this.itemdir = itemdir;
  }

  public void setIndexBuilder(IndexBuilder builder) {
    this.builder = builder;
  }

  public void setItemFieldTables(ItemFieldTables fieldtables) {
    this.subtablemap = fieldtables.getTableMap();
  }

  public void setItemCollection(ItemCollection itemcollection) {
    this.itemcollection = itemcollection;
  }

  public void setUpdateIndex(boolean updateindex) {
    this.updateindex = updateindex;
  }

  public void setDocTypeInterpreter(DocTypeInterpreter doctypeinterpreter) {
    this.doctypeinterpreter = doctypeinterpreter;
  }
  
  private Map readyfields;

  public String[] getFields(String itemid) {
    return (String[]) readyfields.get(itemid);
  }


  private void addField(StringList paramfields, intVector fieldtypes, String fieldname) {
    paramfields.add(fieldname);
    FieldTypeInfo fti = (FieldTypeInfo) ItemFieldRegistry.byParam.get(fieldname);
    fieldtypes.addElement(fti.fieldtype);
  }
  
  public void update() {
    long time = System.currentTimeMillis();
    readyfields = new HashMap();
    if (updateindex) {
      builder.beginUpdates();
    }
    try {
      ItemCSVReader reader = new ItemCSVReader(itemdir + FilenameUtil.filesep
          + "Item.txt", true);
      if (reader.idfield == -1) {
        throw new IOException("Item identifier field " + ItemFields.IDENTIFIER
            + " could not be found on first line of file");
      }

      StringList paramfields = new StringList();
      intVector fieldtypes = new intVector(20);
      int[] destiny = Algorithms.fill(reader.fieldnames.length, -1);
      Map[] lookmaps = new Map[reader.fieldnames.length];
      for (int i = 0; i < lookmaps.length; ++i) {
        FieldTypeInfo info = (FieldTypeInfo) ItemFieldRegistry.byDBField
            .get(reader.fieldnames[i]);
        if (info != null) {
          destiny[i] = paramfields.size();
          paramfields.add(info.paramname == null? info.DBfieldname : info.paramname);
          fieldtypes.addElement(info.fieldtype);

          if (info.indirectname != null) {
            lookmaps[i] = (Map) subtablemap.get(info.indirectname);
          }
        }
      }
      // Add all sythesized fields to the type map
      addField(paramfields, fieldtypes, "havepdf");
      addField(paramfields, fieldtypes, "published");
      addField(paramfields, fieldtypes, "manuscript");
      addField(paramfields, fieldtypes, "startdate");
      addField(paramfields, fieldtypes, "enddate");
      addField(paramfields, fieldtypes, "searchid");
      addField(paramfields, fieldtypes, "searchtitle");
      addField(paramfields, fieldtypes, "sorttitle");
      addField(paramfields, fieldtypes, "allfields");

      String[] paramnames = paramfields.toStringArray();
      int[] fieldtypearr = fieldtypes.asArray();
      int iddest = destiny[reader.idfield];

      builder.setMapping(paramnames, iddest, fieldtypearr);
      builder.setDBFieldGetter(this);

      int HAVE_TEXT_IND = ArrayUtil.indexOf(paramnames, "havetext");
      int HAVE_IMG_IND = ArrayUtil.indexOf(paramnames, "haveimages");
      int HAVE_PDF_IND = ArrayUtil.indexOf(paramnames, "havepdf");
      int ITEM_IND = ArrayUtil.indexOf(paramnames, "identifier");
      int DOCTYPE_IND = ArrayUtil.indexOf(paramnames, "documenttype");
      int MANUSCRIPT_IND = ArrayUtil.indexOf(paramnames, "manuscript");
      int PUBLISHED_IND = ArrayUtil.indexOf(paramnames, "published");
      
      int START_DATE_IND = ArrayUtil.indexOf(paramnames, "startdate");
      int END_DATE_IND = ArrayUtil.indexOf(paramnames, "enddate");
      int SEARCH_ID_IND = ArrayUtil.indexOf(paramnames, "searchid");
      int SEARCH_TITLE_IND = ArrayUtil.indexOf(paramnames, "searchtitle");
      int SORT_TITLE_IND = ArrayUtil.indexOf(paramnames, "sorttitle");
      int ALL_FIELDS_IND = ArrayUtil.indexOf(paramnames, "allfields");

      int DATE_ORIG_IND = ArrayUtil.indexOf(reader.fieldnames, ItemFields.DATE);
      
      int invaliddates = 0;
      int duplicates = 0;
      int total = 0;
      
      while (true) {
        String[] fields = reader.reader.readNext();
        if (fields == null)
          break;
        String[] redfields = new String[paramnames.length];

        for (int i = 0; i < lookmaps.length; ++i) {
          int thisdest = destiny[i];
          if (thisdest != -1) {
            String field = fields[i];
            // this field will be indexed at all
            if (lookmaps[i] != null) {
              if (field.trim().length() > 0 && !field.equals("0") &&
                  !field.equals(ItemFields.SYNTHESIZED)) {

                // there is a subtable, look up the resolved value
                String lookup = (String) lookmaps[i].get(field);
                if (lookup == null) {
                  Logger.log.warn("Warning: value " + field + " for "
                      + reader.fieldnames[i] + " could not be looked up for "
                      + fields[reader.idfield]);
                }
                redfields[thisdest] = lookup;
              }
            }
            else {
              redfields[thisdest] = field;
            }
          }
        }
        String id = redfields[ITEM_IND];
        ItemDetails details = itemcollection.getItem(id);

        redfields[HAVE_TEXT_IND] = (details != null && details.hastext) ? "true" : "false";
        redfields[HAVE_IMG_IND] = (details != null && details.hasimage) ? "true" : "false";
        redfields[HAVE_PDF_IND] = (details != null && details.haspdf) ? "true" : "false";
        
        String doctype = redfields[DOCTYPE_IND];
        redfields[MANUSCRIPT_IND] = doctypeinterpreter.isConciseType(doctype)? "false" : "true";
        redfields[PUBLISHED_IND] = doctypeinterpreter.isConciseType(doctype)? "true" : "false";

        ProtoDate protodate = new ProtoDate(id, fields[DATE_ORIG_IND]);
        redfields[START_DATE_IND] = protodate.startdate;
        redfields[END_DATE_IND] = protodate.enddate;
        redfields[SEARCH_ID_IND] = id;
        String searchtitle = computeTitle(reader.fieldnames, fields, false);
        if (searchtitle.equals("")) {
          dblog.warn("Warning: item " + id + " has all four potential title fields blank");
        }
        redfields[SEARCH_TITLE_IND] = searchtitle;
        String sorttitle = computeTitle(reader.fieldnames, fields, true);
        redfields[SORT_TITLE_IND] = sorttitle;
        String allfields = computeAllFields(redfields, fieldtypes);
        redfields[ALL_FIELDS_IND] = allfields;
        if (protodate.enddate == null) ++ invaliddates;
        
        if (readyfields.get(id) != null) {
          dblog.warn("Warning: duplicate item with ID " + id);
          ++ duplicates;
        }
        readyfields.put(id, redfields);

        if (updateindex) {
          builder.checkItem(redfields);
        }
        ++ total;
      }
      dblog.warn(invaliddates + " invalid dates, " + duplicates + " duplicate entries found in "
          + total + " items");
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error reading item file ");
    }
    finally {
      builder.endUpdates();
    }
   
    long delay = System.currentTimeMillis() - time;
    DecimalFormat df = new DecimalFormat("0.000");
    long size = builder.indexedbytes;
    System.out.println("Indexed " + size + " bytes in " + delay + " ms: "
        + df.format((size / (delay * 1000.0))) + "Mb/s");
  }

  private String computeAllFields(String[] redfields, intVector fieldtypes) {
    CharWrap allfields = new CharWrap();
    for (int i = 0; i < fieldtypes.size(); ++ i) {
      if (redfields[i] != null && fieldtypes.intAt(i) == FieldTypeInfo.TYPE_FREE_STRING) {
        allfields.append(redfields[i]).append(" ");
      }
    }
    return allfields.toString();
  }
  
  private String computeTitle(String[] fieldnames, String[] fields, boolean display) {
    // If it is ever worth it, reform fields to be a Map. Currently a [] so
    // that it may be stored "compactly" so that full text index can find it
    // quickly. We imagine indexing will occur infrequently.
    String title = "";
    int EXACT_IND = ArrayUtil.indexOf(fieldnames, ItemFields.TITLE);
    title = accumTitle(title, fields[EXACT_IND], display);

    int ATTRIB_IND = ArrayUtil.indexOf(fieldnames, ItemFields.ATTRIBTITLE);
    title = accumTitle(title, fields[ATTRIB_IND], display);
    
    int DESC_IND = ArrayUtil.indexOf(fieldnames, ItemFields.DESCRIPTION);
    title = accumTitle(title, fields[DESC_IND], display);
    
    int NAME_IND = ArrayUtil.indexOf(fieldnames, ItemFields.NAME);
    title = accumTitle(title, fields[NAME_IND], display);
    title = title.trim();
    
    return title;
  }

  private String accumTitle(String title, String addTitle, boolean display) {
    if (addTitle != null) {
        title = display? (title.equals("")? addTitle: title) : title + " " + addTitle;
    }
    return title;
  }

}
