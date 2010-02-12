
import com.vuzit.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import jargs.gnu.CmdLineParser;

/**
 * Class for the VuzitCL command-line test application.  
 */
public class VuzitCL
{
  // Main executable code 

  /**
   * Main code area.  
   */
  public static void main(String[] args)
  {
    if(args.length < 2) {
      printUsageGeneral();
      System.exit(0);
    }

    String command = args[0];

    if(command.equals("upload")) {
      uploadCommand(args);
    }
    else if (command.equals("help")) {
      helpCommand(args);
    }
    else if (command.equals("load")) {
      loadCommand(args);
    }
    else if (command.equals("search")) {
      searchCommand(args);
    }
    else if (command.equals("delete")) {
      deleteCommand(args);
    }
    else {
      print("Unknown command option: " + args[0]);
    }
  }


  // Command code

  /**
   * Runs the delete command.
   */
  private static void deleteCommand(String[] args)
  {
    CmdLineParser parser = parserLoad();
    globalParametersLoad(parser, args);
    String id = lastOption(args);

    // TODO: This doesn't work yet so make it function correctly
    if(id == null) {
      printError("No ID supplied");
    }

    try {
      com.vuzit.Document.destroy(id);
      System.out.println("DELETED: " + id);
    } catch (ClientException ce) {
      printError("Delete failed: " + ce.getMessage());
    }
  }

  /**
   * Loads the parameters that are global through all of the 
   * different sub-commands. 
   */
  private static void globalParametersLoad(CmdLineParser parser, String[] args)
  {
    CmdLineParser.Option keys = parser.addStringOption('k', "keys");
    parseArguments(parser, args);

    String keyValues = (String)parser.getOptionValue(keys);
    if(keyValues == null) {
      printError("The --keys flag is a required flag");
    }

    String[] keyList = keyValues.split(",");
    com.vuzit.Service.setPublicKey(keyList[0]);
    com.vuzit.Service.setPrivateKey(keyList[1]);
    com.vuzit.Service.setUserAgent("VuzitCL Java 2.0.0");

    // service-url command
    CmdLineParser.Option serviceUrl = parser.addStringOption('u', "service-url");
    String serviceUrlValue = (String)parser.getOptionValue(serviceUrl);
    if(serviceUrlValue != null) {
      com.vuzit.Service.setServiceUrl(serviceUrlValue);
    }
  }

  /**
   * Runs the load command.
   */
  private static void loadCommand(String[] args)
  {
    CmdLineParser parser = parserLoad();
    globalParametersLoad(parser, args);
    String id = lastOption(args);

    // TODO: This doesn't work yet so make it function correctly
    if(id == null) {
      printError("No ID supplied");
    }

    com.vuzit.Document document;
    try {
      document = com.vuzit.Document.find(id);

      print("LOADED: " + document.getId());
      print("title: " + document.getTitle());
      print("subject: " + document.getSubject());
      print("pages: " + document.getPageCount());
      print("width: " + document.getPageWidth());
      print("height: " + document.getPageHeight());
      print("size: " + document.getFileSize());
      print("status: " + document.getStatus());
      print("download url: " + com.vuzit.Document.downloadUrl(id, "pdf"));
    } catch (ClientException ce) {
      printError("Load failed: " + ce.getMessage());
    }
  }
  
  /**
   * Returns the last option of the class. 
   */
  private static String lastOption(String[] args)
  {
    return args[args.length - 1];
  }

  /**
   * Parses the arguments of the list
   */
  private static void parseArguments(CmdLineParser parser, String[] args)
  {
    try {
      parser.parse(args);
    }
    catch (CmdLineParser.OptionException e) {
      printError(e.getMessage());
    }
  }

  /**
   * Loads up a default parser.  
   */
  private static CmdLineParser parserLoad()
  {
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option keys = parser.addStringOption('k', "keys");
    CmdLineParser.Option serviceUrl = parser.addStringOption('s', "service-url");

    return parser;
  }

  /**
   * Runs the search command.
   */
  private static void searchCommand(String[] args)
  {
    CmdLineParser parser = parserLoad();
    CmdLineParser.Option query = parser.addStringOption('q', "query");
    CmdLineParser.Option limit = parser.addStringOption('l', "limit");
    CmdLineParser.Option offset = parser.addStringOption('o', "offset");
    globalParametersLoad(parser, args);
    parseArguments(parser, args);

    com.vuzit.OptionList list = new com.vuzit.OptionList();

    String queryValue = (String)parser.getOptionValue(query);
    if(queryValue != null) {
      list.add("query", queryValue);
    }

    String limitValue = (String)parser.getOptionValue(limit);
    if(limitValue != null) {
      list.add("limit", limitValue);
    }

    String offsetValue = (String)parser.getOptionValue(offset);
    if(offsetValue != null) {
      list.add("offset", offsetValue);
    }

    com.vuzit.Document document;
    try {
      com.vuzit.Document[] docs = com.vuzit.Document.findAll(list);

      print(docs.length + " documents found");

      for (int i = 0; i < docs.length; i++)
      {
        document = docs[i];

        print("LOADED [" + (i + 1) + "]: " + document.getId());
        print("title: " + document.getTitle());
        print("pages: " + document.getPageCount());
        print("size: " + document.getFileSize());
        print("excerpt: " + document.getExcerpt());
        print("");
      }

    } catch (ClientException ce) {
      printError("Load failed: " + ce.getMessage());
    }
  }

  /**
   * Runs the upload command.
   */
  private static void uploadCommand(String[] args)
  {
    CmdLineParser parser = parserLoad();
    CmdLineParser.Option secure = parser.addBooleanOption('s', "secure");
    CmdLineParser.Option pdf = parser.addBooleanOption('p', "download-pdf");
    CmdLineParser.Option doc = parser.addBooleanOption('d', "download-document");
    globalParametersLoad(parser, args);
    String path = lastOption(args);

    parseArguments(parser, args);

    com.vuzit.OptionList options = new com.vuzit.OptionList();
    Boolean secureValue = (Boolean)parser.getOptionValue(secure);
    if(secureValue != null) {
      options.add("secure", secureValue);
    }

    Boolean pdfValue = (Boolean)parser.getOptionValue(pdf);
    if(pdfValue != null) {
      options.add("download_pdf", pdfValue);
    }

    Boolean docValue = (Boolean)parser.getOptionValue(doc);
    if(docValue != null) {
      options.add("download_document", docValue);
    }

    try {
      com.vuzit.Document document = com.vuzit.Document.upload(path, options);
      print("UPLOADED: " + document.getId());
    } catch (ClientException ce) {
      printError("Upload failed: " + ce.getMessage());
    }
  }


  // Print and help messages

  /**
   * Runs the help command.
   */
  private static void helpCommand(String[] args)
  {
    String option = lastOption(args);

    if(option.equals("upload")) {
      printUsageUpload();
    }
    else if(option.equals("load")) {
      printUsageLoad();
    }
    else if(option.equals("delete")) {
      printUsageDelete();
    }
    else if(option.equals("search")) {
      printUsageSearch();
    }
    else {
      print("Unknown option: " + option);
    }
  }

  /**
   * Prints a message to the command line.  
   */
  private static void print(String message)
  {
    System.out.println(message);
  }

  /**
   * Prints an error message to the command line.  
   */
  private static void printError(String message)
  {
     System.err.println(message);
     printUsageGeneral();
     System.exit(2);
  }

  /**
   * Prints delete usage flags. 
   */
  private static void printUsageDelete()
  {
    print("delete: Delete a document");
    print("usage: delete [OPTIONS] WEB_ID");
    print("");
    print("Valid options:");
    print("  none");
    print("");
    printUsageGlobal();
  }

  /**
   * Prints the general usage flags. 
   */
  private static void printUsageGeneral()
  {
    print("VuzitCL - Vuzit Command Line");
    print("Usage: vuzitcl -k PUBLIC_KEY,PRIVATE_KEY [OPTIONS]");
    print("");
    print("Type 'vuzitcl help <subcommand>' for help on a specific subcommand.");
    print("");
    print("Available sub-commands:");
    print("");
    print("  delete");
    print("  load");
    print("  search");
    print("  upload");
    print("  help");
  }

  /**
   * Prints the global usage flags. 
   */
  private static void printUsageGlobal()
  {
     print("Global Options:");
     print("  -k, --keys=PUB_KEY,PRIV_KEY    Developer API keys - REQUIRED");
     print("  -u, --service-url=URL          Sets the service URL (e.g. http://domain.com)");
  }

  /**
   * Prints load usage flags. 
   */
  private static void printUsageLoad()
  {
    print("load: Loads a document");
    print("usage: load [OPTIONS] WEB_ID");
    print("");
    print("Valid options:");
    print("  none");
    print("");
    printUsageGlobal();
  }

  /**
   * Prints search usage flags. 
   */
  private static void printUsageSearch()
  {
    print("search: Search for documents");
    print("usage: search [OPTIONS]");
    print("");
    print("Valid options:");
    print("  -q, --query         Query keywords");
    print("  -l, --limit         Limits the results to a number");
    print("  -o, --offset        Offsets the results at this number");
    print("");
    printUsageGlobal();
  }

  /**
   * Prints the usage information for the application.  
   */
  private static void printUsageUpload()
  {
    print("upload: Upload a file to Vuzit.");
    print("usage: upload [OPTIONS] PATH");
    print("");
    print("Valid options:");
    print("  -s, --secure                   Make the document secure (not public)");
    print("  -p, --download-pdf             Make the PDF downloadable");
    print("  -d, --download-document        Make the original document downloadable");
    print("");
    printUsageGlobal();
  }
}
