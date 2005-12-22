/*
 * Created on 31-Oct-2005
 */

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class CheckFiles {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: CheckFiles <directory root>");
      System.exit(-1);
    }
    String dirname = args[0];
  }
}
