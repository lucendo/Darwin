/*
 * Created on 31-Oct-2005
 */
package uk.org.ponder.darwin.parse;

import java.io.File;
import java.io.FileInputStream;

import uk.org.ponder.darwin.item.ItemCollection;
import uk.org.ponder.darwin.item.ItemDetails;
import uk.org.ponder.darwin.item.PageInfo;
import uk.org.ponder.fileutil.FileUtil;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 *  
 */
public class TreeLoader {

  public static StringList scanTree(String root, ItemCollection collection) {
    File rootdir = new File(root);
    StringList files = FileUtil.getListing(rootdir, FileUtil.FILE_MASK);
    Logger.log.info("Scanning " + files.size() + " files in directory "
        + rootdir);
    StringList allerrors = new StringList();
    AccretingParseReceiver apr = new AccretingParseReceiver(collection);
    for (int i = 0; i < files.size(); ++i) {
      String filename = files.stringAt(i);
      String extension = Extensions.getExtension(filename);
      if (Extensions.isContentFile(extension)) {
        Logger.log.warn("Parsing content file " + filename);
        File f = new File(filename);
        try {
          FileInputStream fis = new FileInputStream(f);
          ContentParser parse = new ContentParser();
          StringList thiserrors = parse.parse(fis, filename, apr);
          for (int er = 0; er < thiserrors.size(); ++ er) {
            String thiser = "Error parsing file " + filename + thiserrors.stringAt(er);
            thiserrors.set(er, thiser);
          }
          allerrors.addAll(thiserrors);
        }
        catch (Exception e) {
          Logger.log.warn("Error parsing file " + filename + ": "
              + e.getMessage(), e);
        }
      }
      else if (Extensions.isImageFile(extension)) {
        Logger.log.info("Registering image file " + filename);
        ImageFilename imagefile = ImageFilename.parse(filename);
        ItemDetails details = collection.getItemSafe(imagefile.ID);
        PageInfo pageinfo = details.acquirePageInfoSafe(imagefile.pageseq);
        pageinfo.imagefile = filename;
      }
      else {
        Logger.log.warn("Warning: file " + filename +" with unrecognised extension was skipped");
      }
    }
    return allerrors;
  }
}