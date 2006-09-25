/*
 * Created on 12-Mar-2006
 */
package test;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.darwin.lucene.ContentIndexUpdater;
import uk.org.ponder.darwin.lucene.DarwinAnalyzer;
import uk.org.ponder.darwin.lucene.DarwinHighlighter;
import uk.org.ponder.darwin.lucene.ItemIndexUpdater;
import uk.org.ponder.darwin.lucene.QueryBuilder;
import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.darwin.search.SearchParams;

public class TestIndex {

  private static void testTextQuery(IndexSearcher searcher, Query q2)
      throws Exception {
    Hits hits2 = searcher.search(q2);
    // DarwinHighlighter highlighter = new DarwinHighlighter();
    System.out.println("Got " + hits2.length() + " hits for " + q2.toString()
        + ": ");
    for (int i = 0; i < hits2.length(); ++i) {
      Document doc = hits2.doc(i);
      System.out.println("ID " + doc.get(DocFields.ITEMID) + " pageseq "
          + doc.get(DocFields.PAGESEQ_START));
      String pagetext = doc.getField(DocFields.FLAT_TEXT).stringValue();
      String high = DarwinHighlighter.getHighlightedHit(q2, pagetext, searcher
          .getIndexReader());
      System.out.println(high);
    }
  }
  
  private static void testQuery(IndexSearcher searcher, QueryBuilder qb,
      SearchParams searchparams) throws Exception {

    Query q = qb.convertQuery(searchparams);
    Hits hits = searcher.search(q);
    System.out.println("Got " + hits.length() + " hits for " + q.toString()
        + ": ");
    for (int i = 0; i < hits.length(); ++i) {
      Document doc = hits.doc(i);
      System.out.println("ID " + doc.get(DocFields.ITEMID) + " pageseq "
          + doc.get(DocFields.PAGESEQ_START));
    }
  }

  // /**
  // * Directory specified by <code>org.apache.lucene.lockDir</code>
  // * or <code>java.io.tmpdir</code> system property
  // */
  // public static final String LOCK_DIR =
  // System.getProperty("org.apache.lucene.lockDir",
  // System.getProperty("java.io.tmpdir"));

  public static void main(String[] args) {

    ClassPathXmlApplicationContext cpxac = new ClassPathXmlApplicationContext(
        "conf/" + args[0]);

    PageInfo testinfo = new PageInfo();
    testinfo.contentfile = "E:\\flowtalk-jakarta-tomcat-5.5.9\\webapps\\Darwin\\converted\\1835_letters_F1.html";
    testinfo.sequence = 1;

    try {
      ItemIndexUpdater iiu = (ItemIndexUpdater) cpxac
          .getBean("itemIndexUpdater");
      QueryBuilder qb = (QueryBuilder) cpxac.getBean("queryBuilder");
      IndexSearcher searcher = (IndexSearcher) cpxac.getBean("indexSearcher");

      iiu.update(); // this must happen before we point ContextIndexUpdater at
      // it
      SearchParams searchparams = new SearchParams();
      searchparams.identifier = "F1652";
      testQuery(searcher, qb, searchparams );
      
      searchparams.identifier = "F*";
      testQuery(searcher, qb, searchparams );
      
//      searchparams.identifier = null;
//      searchparams.name = "Darwin";
//      testQuery(searcher, qb, searchparams );

      ContentIndexUpdater ciu = (ContentIndexUpdater) cpxac
          .getBean("contentIndexUpdater");

      ciu.update();

      QueryParser qp2 = new QueryParser(DocFields.TEXT, new DarwinAnalyzer());
      Query q2 = qp2.parse("iceberg");
      testTextQuery(searcher, q2);
    }
    catch (Throwable e) {
      e.printStackTrace(System.err);
      e.printStackTrace(System.out);
    }
    finally {
      cpxac.close();
    }

  }
}
