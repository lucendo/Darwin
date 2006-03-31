/*
 * Created on 30-Mar-2006
 */
package uk.org.ponder.darwin.lucene;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

public class DarwinHighlighter {
  public String getHighlightedHit(Query unrwquery, TokenStream stream, IndexReader reader) {
    Query query = unrwquery.rewrite(reader);
    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter(),
        new SimpleHTMLEncoder(), new QueryScorer(query));
    return highlighter.getBestFragment(stream, te);
  
  }
}
