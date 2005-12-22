/*
 * Created on 13-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.streamutil.write.PrintStreamPOS;
import uk.org.ponder.stringutil.StringList;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class CheckTree {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: CheckTree <directory name>");
      System.exit(-1);
    }
    long time = System.currentTimeMillis();
    ItemCollection collection = new ItemCollection();
    StringList parseerrors = TreeLoader.scanTree(args[0], collection);
    CheckStatistics stats = new CheckStatistics();
    stats.time = System.currentTimeMillis() - time;
    stats.errors = parseerrors;
    ItemChecks.checkCollection(collection, stats);
    
    PrintStreamPOS pos = new PrintStreamPOS(System.out);
    stats.report(pos);
  }
}
