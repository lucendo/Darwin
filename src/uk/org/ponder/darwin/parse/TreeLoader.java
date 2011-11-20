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
import uk.org.ponder.streamutil.StreamCloseUtil;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.util.Logger;

/**
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
public class TreeLoader {

  public static StringList scanTree(String root, ItemCollection collection) {
    Logger.log.warn("Beginning scan in directory " + root);
    File rootdir = new File(root);
    StringList files = FileUtil.getListing(rootdir, FileUtil.FILE_MASK);
    Logger.log.info("Scanning " + files.size() + " files in directory "
        + rootdir);
    StringList allerrors = new StringList();
    AccretingParseReceiver apr = new AccretingParseReceiver(collection);
    for (int i = 0; i < files.size(); ++i) {
      String filename = files.stringAt(i);
      // Logger.log.warn("Examining filename " + filename);
      //filename = FileUtil.getCanonicalPath(filename);
      //Logger.log.warn("Canonical filename " + filename);
      String extension = Extensions.getExtension(filename);
      if (Extensions.isContentFile(extension)) {
        Logger.log.warn("Parsing content file " + filename);
        File f = new File(filename);
        FileInputStream fis = null;
        try {
          fis = new FileInputStream(f);
          ContentParser parse = new ContentParser();
          StringList thiserrors = parse.parse(fis, filename, apr);
          for (int er = 0; er < thiserrors.size(); ++er) {
            String thiser = "Error parsing file " + filename
                + thiserrors.stringAt(er);
            thiserrors.set(er, thiser);
          }
          allerrors.addAll(thiserrors);
        }
        catch (Exception e) {
          Logger.log.warn("Error parsing file " + filename + ": "
              + e.getMessage(), e);
        }
        finally {
          StreamCloseUtil.closeInputStream(fis);
        }
      }
      else if (Extensions.isImageFile(extension)) {
        try {
//          Logger.log.info("Registering image file " + filename);
          ImageFilename imagefile = ImageFilename.parse(filename);
          if (imagefile == null) { // it is a "figure" part of a book
            continue;
          }
          ItemDetails details = collection.getItemSafe(imagefile.ID);
          PageInfo pageinfo = details.acquirePageInfoSafe(imagefile.pageseq);
          pageinfo.imagefile = filename;
        }
        catch (Exception e) {
          allerrors.add("Skipping unrecognised image filename " + filename);
          Logger.log.warn("Skipping unrecognised image filename " + filename
              + ": " + e);
        }
      }
      else if (Extensions.isPDFFile(extension)) {
        try {
          PDFFilename pdffile = PDFFilename.parse(filename);
          ItemDetails details = collection.getItemSafe(pdffile.ID);
          details.pdffile = filename;
          details.haspdf = true;
        }
        catch (Exception e) {
          allerrors.add("Skipping unrecognised PDF filename " + filename);
          Logger.log.warn("Skipping unrecognised PDF filename " + filename
              + ": " + e);
        }
      }
      else {
        Logger.log.warn("Warning: file " + filename
            + " with unrecognised extension was skipped");
      }
    }
    return allerrors;
  }
}