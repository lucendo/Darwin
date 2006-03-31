/*
 * Created on 30-Mar-2006
 */
package uk.org.ponder.darwin.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hit;

import uk.org.ponder.util.UniversalRuntimeException;

/** A quiet and non-lazy version of Lucene Hit **/

public class DocHit {
  public Document document;
  public int docid;
  public DocHit(Hit hit) {
    try {
    document = hit.getDocument();
    docid = hit.getId();
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Error loading hit from " + hit);
    }
  }
}
