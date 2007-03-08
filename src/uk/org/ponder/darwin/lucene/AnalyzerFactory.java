/*
 * Created on 8 Mar 2007
 */
package uk.org.ponder.darwin.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;

public class AnalyzerFactory {
  public static Analyzer getAnalyzer() {
    DarwinAnalyzer defaultAnalyzer = new DarwinAnalyzer(true);
    PerFieldAnalyzerWrapper pfaw = new PerFieldAnalyzerWrapper(defaultAnalyzer);
    Analyzer flatanalyzer = new Analyzer() {
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new FlatTokenizer(reader);
      }
    };
    pfaw.addAnalyzer("searchid", flatanalyzer);
    return pfaw;
  }
}
