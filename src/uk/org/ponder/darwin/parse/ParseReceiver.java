/*
 * Created on 09-Nov-2005
 */
package uk.org.ponder.darwin.parse;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public interface ParseReceiver {
  public void beginEditable(String editclass);
  public void endEditable(String editclass);
  public void text(char[] buffer, int start, int length);
  public void text(String text);
  public void metObject(Object tagobj);
  public void beginFile(String contentpath);
  public void endFile();
}
