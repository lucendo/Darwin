/*
 * Created on 13-Nov-2005
 */
package uk.org.ponder.darwin.parse;

import uk.org.ponder.darwin.item.CheckStatistics;
import uk.org.ponder.streamutil.write.PrintStreamPOS;

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
    ItemCollectionManager manager = new ItemCollectionManager();
    manager.setContentRoot(args[0]);
    manager.index();
   
    CheckStatistics stats = manager.statistics;
    
    PrintStreamPOS pos = new PrintStreamPOS(System.out);
    stats.report(pos);
  }
}
