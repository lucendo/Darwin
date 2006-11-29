/*
 * Created on 07-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.darwin.search.FieldTypeInfo;
import uk.org.ponder.darwin.search.ItemFieldRegistry;
import uk.org.ponder.darwin.search.SearchParams;
import uk.org.ponder.saxalizer.MethodAnalyser;
import uk.org.ponder.saxalizer.SAXAccessMethod;
import uk.org.ponder.saxalizer.SAXalizerMappingContext;
import uk.org.ponder.stringutil.CharWrap;

public class QueryBuilder {
  private SAXalizerMappingContext mappingcontext;

  public void setMappingContext(SAXalizerMappingContext mappingcontext) {
    this.mappingcontext = mappingcontext;
  }

  public static Sort convertSort(SearchParams params) {
    List fields = new ArrayList();
    if (params.sort.equals(SearchParams.SORT_RELEVANCE)) {
      fields.add(SortField.FIELD_SCORE);
      fields.add(SortField.FIELD_DOC);
    }
    else {
      if (params.sort.equals(SearchParams.SORT_DATEASC)) {
        fields.add(new SortField("startdate", SortField.STRING, false));
      }
      else if (params.sort.equals(SearchParams.SORT_DATEDESC)) {
        fields.add(new SortField("enddate", SortField.STRING, true));
      }
      else if (params.sort.equals(SearchParams.SORT_TITLE)) {
        fields.add(new SortField("sorttitle", SortField.STRING, false));
      }
      fields.add(SortField.FIELD_SCORE);
    }
    SortField[] fieldarr = (SortField[]) fields.toArray(new SortField[fields
        .size()]);
    return new Sort(fieldarr);
  }

  public Query freeQuery(String freetext) throws ParseException {
    QueryParser qp2 = new QueryParser(DocFields.TEXT, new DarwinAnalyzer(false));
    qp2.setDefaultOperator(Operator.AND);
    Query q2 = qp2.parse(freetext);
    return q2;
  }

  private String resolveDate(String value, boolean end) {
    CharWrap togo = new CharWrap();
    char fillchar = end ? '9'
        : '0';
    for (int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i);
      if (Character.isDigit(c)) {
        togo.append(c);
        if (togo.size() == 4) {
          togo.append(fillchar);
        }
      }
    }

    for (int i = 9 - togo.size(); i > 0; --i) {
      togo.append(fillchar);
    }
    return togo.toString();
  }

  public Query convertQuery(SearchParams params) throws ParseException {
    MethodAnalyser ma = mappingcontext.getAnalyser(SearchParams.class);
    List filters = new ArrayList();
    boolean freetext = false;

    BooleanQuery togo = new BooleanQuery();

    for (int i = 0; i < ma.allgetters.length; ++i) {
      SAXAccessMethod sam = ma.allgetters[i];
      String field = sam.tagname;
      Object valueo = sam.getChildObject(params);
      String value = null;
      if (valueo instanceof String) {
        value = (String) valueo;
      }
      else if (valueo instanceof Boolean) {
        if (((Boolean) valueo).booleanValue())
          value = "true";
      }
      if (valueo != null && !valueo.equals("")) {
        if (field.equals("sort")) {
          // ignore
        }
        else if (field.equals("manuscripts")) {

        }
        else if (field.equals("dateafter")) {
          // see http://wiki.apache.org/jakarta-lucene/FilteringOptions
          String resolved = resolveDate(value, false);
          RangeFilter filter = new RangeFilter("startdate", resolved, null,
              true, false);
          filters.add(filter);
        }
        else if (field.equals("datebefore")) {
          String resolved = resolveDate(value, true);
          RangeFilter filter = new RangeFilter("enddate", null, resolved,
              false, true);
          filters.add(filter);
        }
        else if (field.equals("searchid")) {
          String tofind = value.trim().toLowerCase();
          togo.add(new TermQuery(new Term(field, tofind)), Occur.MUST);
        }
        else if (!field.equals("freetext")) {
          FieldTypeInfo info = (FieldTypeInfo) ItemFieldRegistry.byParam
              .get(field);
          if (info.fieldtype == FieldTypeInfo.TYPE_FREE_STRING) {
            QueryParser qp3 = new QueryParser(field, new DarwinAnalyzer(false));
            qp3.setDefaultOperator(Operator.AND);
            Query q2 = qp3.parse(field + ":" + value);
            togo.add(q2, Occur.MUST);
          }
          else if (info.fieldtype == FieldTypeInfo.TYPE_KEYWORD
              || info.fieldtype == FieldTypeInfo.TYPE_BOOLEAN) {
            if (valueo instanceof String[]) {
              BooleanQuery ors = new BooleanQuery();
              String[] valuel = (String[]) valueo;
              for (int j = 0; j < valuel.length; ++j) {
                ors
                    .add(new TermQuery(new Term(field, valuel[j])),
                        Occur.SHOULD);
              }
              togo.add(ors, Occur.MUST);
            }
            else {
              togo.add(new TermQuery(new Term(field, value)), Occur.MUST);
            }
          }
        }
        else {
          QueryParser qp2 = new QueryParser(DocFields.TEXT, new DarwinAnalyzer(false));
          qp2.setDefaultOperator(Operator.AND);
          Query q2 = qp2.parse(value);
          togo.add(q2, Occur.MUST);
          freetext = true;
        }
      }
    }
    
    if (!freetext) {
      togo.add(new TermQuery(new Term(DocFields.TYPE, DocFields.TYPE_ITEM)), Occur.MUST);
    }
    
    Query toreallygo = togo;
    for (int i = 0; i < filters.size(); ++i) {
      toreallygo = new FilteredQuery(toreallygo, (Filter) filters.get(i));
    }
    return toreallygo;
  }

}
