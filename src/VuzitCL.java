
import com.vuzit.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import jargs.gnu.CmdLineParser;

public class VuzitCL
{
  /**
   * Main code area.  
   */
  public static void main(String[] args)
  {
    if(args.length < 1) {
      printUsage();
      System.exit(0);
    }

    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option keys = parser.addStringOption('k', "keys");
    CmdLineParser.Option help = parser.addBooleanOption('h', "help");
    CmdLineParser.Option load = parser.addStringOption('l', "load");
    CmdLineParser.Option destroy = parser.addStringOption('d', "delete");
    CmdLineParser.Option serviceUrl = parser.addStringOption('s', "service-url");
    CmdLineParser.Option verbose = parser.addStringOption('v', "verbose");
    CmdLineParser.Option upload = parser.addStringOption('u', "upload");
    CmdLineParser.Option isPublic = parser.addBooleanOption('p', "public");

    try {
      parser.parse(args);
    }
    catch (CmdLineParser.OptionException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(2);
    }

    // help command
    Boolean helpValue = (Boolean)parser.getOptionValue(help);
    if(helpValue != null && helpValue == true) {
      printUsage();
      System.exit(0);
    }

    // keys command
    String keyValues = (String)parser.getOptionValue(keys);
    if(keyValues == null) {
      System.err.println("The --keys flag is a required flag");
      printUsage();
      System.exit(2);
    }
    String[] keyList = keyValues.split(",");
    com.vuzit.Service.setPublicKey(keyList[0]);
    com.vuzit.Service.setPrivateKey(keyList[1]);
    com.vuzit.Service.setUserAgent("VuzitCL Java 1.0.0");

    // service-url command
    String serviceUrlValue = (String)parser.getOptionValue(serviceUrl);
    if(serviceUrlValue != null) {
      com.vuzit.Service.setServiceUrl(serviceUrlValue);
    }

    // public command
    Boolean publicValue = (Boolean)parser.getOptionValue(isPublic);
    if(publicValue == null) {
      publicValue = false;
    }
 
    // upload command
    String uploadValue = (String)parser.getOptionValue(upload);
    if(uploadValue != null) {
      try {
        // "secure" is the opposite of public
        com.vuzit.Document doc = com.vuzit.Document.upload(uploadValue, !publicValue);
        System.out.println("UPLOADED: " + doc.getId());
      } catch (ClientException ce) {
        System.out.println("Upload failed: " + ce.getMessage());
        System.exit(2);
        return;
      }
    }

    // delete command
    String destroyValue = (String)parser.getOptionValue(destroy);
    if(destroyValue != null) {
      try {
        com.vuzit.Document.destroy(destroyValue);
        System.out.println("DELETED: " + destroyValue);
      } catch (ClientException ce) {
        System.out.println("Delete failed: " + ce.getMessage());
        System.exit(2);
        return;
      }
    }

    // load command
    String loadValue = (String)parser.getOptionValue(load);
    if(loadValue != null) {
      com.vuzit.Document document;
      try {
        document = com.vuzit.Document.findById(loadValue);
      } catch (ClientException ce) {
        System.out.println("Load failed: " + ce.getMessage());
        System.exit(2);
        return;
      }
      System.out.println("LOADED: " + document.getId());
      System.out.println("title: " + document.getTitle());
      System.out.println("subject: " + document.getSubject());
      System.out.println("pages: " + document.getPageCount());
      System.out.println("width: " + document.getPageWidth());
      System.out.println("height: " + document.getPageHeight());
      System.out.println("size: " + document.getFileSize());
    }
  }

  /**
   * Prints the usage information for the application.  
   */
  private static void printUsage()
  {
    System.out.println(
      "VuzitCL - Vuzit Command Line\n" +
      "Usage: vuzitcl -k [PUBLIC_KEY],[PRIVATE_KEY] [OPTIONS]\n" +
      "\n" + 
      "Options:\n" +
      "   -k, --keys [PUB_KEY],[PRIV_KEY]  Developer API keys - REQUIRED\n" +
      "   -u, --upload [PATH]              File to upload\n" +
      "   -p, --public                     Make uploaded file public\n" +
      "   -l, --load [ID]                  Loads the document data\n" +
      "   -d, --delete [ID]                Deletes a document\n" +
      "   -s, --service-url [URL]          Sets the service URL\n" +
      "   -v, --verbose                    Prints more messages\n" +
      "   -h, --help                       Show this message\n"
      );
  }
}
