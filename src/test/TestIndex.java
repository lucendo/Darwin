/*
 * Created on 12-Mar-2006
 */
package test;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

import uk.org.ponder.darwin.item.ContentInfo;
import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.darwin.lucene.DarwinAnalyzer;
import uk.org.ponder.darwin.lucene.DocFields;
import uk.org.ponder.darwin.lucene.IndexBuilder;
import uk.org.ponder.darwin.parse.TreeLoader;

public class TestIndex {
  // /**
  // * Directory specified by <code>org.apache.lucene.lockDir</code>
  // * or <code>java.io.tmpdir</code> system property
  // */
  // public static final String LOCK_DIR =
  // System.getProperty("org.apache.lucene.lockDir",
  // System.getProperty("java.io.tmpdir"));

  public static void main(String[] args) {
    ItemCollection items = new ItemCollection();
    TreeLoader.scanTree(
        "E:\\flowtalk-jakarta-tomcat-5.5.9\\webapps\\Darwin\\converted", items);

    IndexBuilder builder = new IndexBuilder();
    builder.setIndexDirectory("e:\\lucendo\\darwin\\index");
    builder.setAnalyser(new DarwinAnalyzer());
    builder.setForceReindex(true);

    PageInfo testinfo = new PageInfo();
    testinfo.contentfile = "E:\\flowtalk-jakarta-tomcat-5.5.9\\webapps\\Darwin\\converted\\1835_letters_F1.html";
    testinfo.sequence = 1;
    builder.open();
    try {
      long time = System.currentTimeMillis();
      builder.beginUpdates();
      try {
        Collection itemcoll = items.getItems();
        for (Iterator itemit = itemcoll.iterator(); itemit.hasNext();) {
          ItemDetails item = (ItemDetails) itemit.next();
          for (int content = 0; content < item.contents.size(); ++content) {
            ContentInfo contentinfo = (ContentInfo) item.contents.get(content);
            builder.checkPage(contentinfo);
          }
        }
      }
      finally {
        builder.endUpdates();
      }
      long delay = System.currentTimeMillis() - time;
      DecimalFormat df = new DecimalFormat("0.000");
      long size = builder.indexedbytes;
      System.out.println("Indexed " + size + " bytes in " + delay + " ms: "
          + df.format((size / (delay * 1000.0))) + "Mb/s");

      QueryParser qp = new QueryParser(DocFields.TEXT, new DarwinAnalyzer());
      Query q = qp.parse("iceberg");
      Hits hits = builder.getSearcher().search(q);
      System.out.println("Got " + hits.length() + " hits for " + q.toString()
          + ": ");
      for (int i = 0; i < hits.length(); ++i) {
        Document doc = hits.doc(i);
        System.out.println("ID " + doc.get(DocFields.ITEMID) + " pageseq "
            + doc.get(DocFields.PAGESEQ_START));
      }
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }
    finally {
      builder.close();
    }

  }
}
