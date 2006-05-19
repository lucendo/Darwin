/*
 * Created on 07-May-2006
 */
package uk.org.ponder.darwin.lucene;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.darwin.search.FieldTypeInfo;
import uk.org.ponder.darwin.search.ItemFieldRegistry;
import uk.org.ponder.darwin.search.ItemFields;
import uk.org.ponder.darwin.search.SearchParams;
import uk.org.ponder.saxalizer.MethodAnalyser;
import uk.org.ponder.saxalizer.SAXAccessMethod;
import uk.org.ponder.saxalizer.SAXalizerMappingContext;

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
        fields.add(new SortField(ItemFields.DATE, SortField.STRING, false));
      }
      else if (params.sort.equals(SearchParams.SORT_DATEDESC)) {
        fields.add(new SortField(ItemFields.DATE, SortField.STRING, true));
      }
      else if (params.sort.equals(SearchParams.SORT_TITLE)) {
        fields.add(new SortField(ItemFields.ATTRIBTITLE, SortField.STRING,
            false));
      }
      fields.add(SortField.FIELD_SCORE);
    }
    SortField[] fieldarr = (SortField[]) fields.toArray(new SortField[fields
        .size()]);
    return new Sort(fieldarr);
  }

  public Query freeQuery(String freetext) throws ParseException {
    QueryParser qp2 = new QueryParser(DocFields.TEXT, new DarwinAnalyzer());
    Query q2 = qp2.parse(freetext);
    return q2;
  }

  public Query convertQuery(SearchParams params) throws ParseException {
    MethodAnalyser ma = mappingcontext.getAnalyser(SearchParams.class);
    QueryParser qp2 = new QueryParser(DocFields.TEXT, new DarwinAnalyzer());

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
        else if (!field.equals("freetext")) {
          FieldTypeInfo info = (FieldTypeInfo) ItemFieldRegistry.byParam
              .get(field);
          if (info.fieldtype == FieldTypeInfo.TYPE_FREE_STRING) {
            Query q2 = qp2.parse(field + ":" + value);
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
          Query q2 = qp2.parse(value);
          togo.add(q2, Occur.MUST);
        }
      }
    }
    return togo;
  }
}
