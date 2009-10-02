
package com.vuzit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.HttpURLConnection;

import java.io.InputStreamReader;

/**
 * Class for manipulating documents from Vuzit.  
 */
public class Document extends Base
{
  // Private class variables
  private String webId = null;
  private String title = null;
  private String subject = null;
  private int pageCount = -1;
  private int pageWidth = -1;
  private int pageHeight = -1;
  private int fileSize = -1;

  // Public instance data members

  /**
   * Returns the web id of the document.  
   */
  public String getId() {
    return webId;
  }

  /**
   * Returns the title of the document.  
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns the subject of the document.  
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Returns the number of pages in a document.  
   */
  public int getPageCount() {
    return pageCount;
  }

  /**
   * Returns the page width of the document.  
   */
  public int getPageWidth() {
    return pageWidth;
  }

  /**
   * Returns the page height of the document.  
   */
  public int getPageHeight() {
    return pageHeight;
  }

  /**
   * Returns the file size of the document.  
   */
  public int getFileSize() {
    return fileSize;
  }

  // Public static methods

  /**
   * Loads a document by the web id.  
   */
  public static Document findById(String webId)
  {
    Document result = new Document();

    if(webId == null) {
      // TODO: Throw exception here
    }

    java.util.Hashtable parameters = postParameters("show", webId);

    String url = parametersToUrl("documents", parameters, webId);

    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    BufferedReader reader = null;
    String line = null;
    try {
      connection.connect();

      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      while ((line = reader.readLine()) != null)
      {
        System.out.print(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    finally
    {
      connection.disconnect();
      connection = null;
    }

    System.out.println(url);

    result.webId = webId;
    result.title = "Title";
    result.subject = "Subject";
    result.pageCount = 5;
    result.pageHeight = 10;
    result.pageWidth = 7;
    result.fileSize = 1024;

    return result;
  }
}
