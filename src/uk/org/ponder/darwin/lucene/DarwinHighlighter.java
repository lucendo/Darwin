/*
 * Created on 30-Mar-2006
 */
package uk.org.ponder.darwin.lucene;


import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import uk.org.ponder.darwin.search.DocFields;
import uk.org.ponder.util.UniversalRuntimeException;

public class DarwinHighlighter {
  public static String getHighlightedHit(Query unrwquery, String pagetext, IndexReader reader) {
    try {
    Query query = unrwquery.rewrite(reader);
    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(),
        new SimpleHTMLEncoder(), new QueryScorer(query));
    highlighter.setTextFragmenter(new SimpleFragmenter(500));
    DarwinAnalyzer analyzer = new DarwinAnalyzer();
    TokenStream ts = analyzer.tokenStream(DocFields.TEXT, new StringReader(pagetext));
    return highlighter.getBestFragment(ts, pagetext);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error highlighting hit: ");
    }
  }
}
