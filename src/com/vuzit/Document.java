
package com.vuzit;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import org.w3c.dom.*;

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
  private int status = -1;

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
   * Returns the status of the document.  
   */
  public int getStatus() {
    return status;
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
   * Deletes a document by the web ID.  
   */
  public static void destroy(String webId)
  {
    java.util.Hashtable parameters = postParameters("destroy", webId);
    String url = parametersToUrl("documents", parameters, webId);
    java.net.HttpURLConnection connection = httpConnection(url, "DELETE");

    try
    {
      connection.connect();

      if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        // Check for a Vuzit returned error code
        webClientErrorCheck(connection);

        // If there is no other error then throw a generic HTTP error
        throw new ClientException("HTTP error: [" + 
                                  connection.getResponseCode() + "], " + 
                                  connection.getResponseMessage());
      }
    } catch (java.io.IOException e) {
      webClientErrorCheck(connection);
    } 
    finally
    {
      connection.disconnect();
      connection = null;
    }
  }

  /**
   * Returns a URL suitable for downloading a document.  
   */
  public static String downloadUrl(String webId, String fileExtension)
  {
    java.util.Hashtable parameters = postParameters("show", webId);
    return parametersToUrl("documents", parameters, webId, fileExtension);
  }

  /**
   * Loads a document by the web ID.  
   */
  public static Document find(String webId)
  {
    Document result = null;

    java.util.Hashtable parameters = postParameters("show", webId);
    String url = parametersToUrl("documents", parameters, webId);
    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    try
    {
      connection.connect();

      Element element = xmlRootNode(connection.getInputStream(), "document");
      if(element == null) {
        throw new ClientException("Response returned incorrect XML");
      }
      result = nodeToDocument(element);
    } catch (java.io.IOException e) {
      webClientErrorCheck(connection);
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally
    {
      connection.disconnect();
      connection = null;
    }

    return result;
  }

  /**
   * Deprecated method to load a document by the web ID.  
   */
  public static Document findById(String webId)
  {
    return find(webId);
  }

  /**
   * Uploads a document from disk via the Vuzit service with the security turned on.  
   */
  public static Document upload(String path)
  {
    return upload(path, true);
  }

  /**
   * Uploads a document from disk via the Vuzit service.  
   */
  public static Document upload(String path, boolean secure)
  {
    Document result = null;

    File file = new File(path);
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
    } catch(java.io.FileNotFoundException e) {
      throw new ClientException("Cannot find file at path: " + path);
    }
    result = upload(stream, null, file.getName(), secure);

    return result;
  }

  /**
   * Uploads a document from an InputStream via the Vuzit service.  
   */
  public static Document upload(InputStream stream, String fileType, 
                                String fileName, boolean secure)
  {
    Document result = new Document();

    if(fileName == null) {
      fileName = "document";
    }

    java.util.Hashtable parameters = postParameters("create", null);
    if(fileType != null) {
      parameters.put("file_type", fileType);
    }
    parameters.put("secure", (secure) ? "1" : "0");

    String url = parametersToUrl("documents", parameters, null);
    InputStream response = uploadFile(stream, url, "upload", null, fileName);

    Element element = xmlRootNode(response, "document");
    if(element == null) {
      throw new ClientException("Response returned incorrect XML");
    }

    result.webId = nodeValue(element, "web_id");
    try {
      response.close();
    } catch(IOException ex) {
    }

    return result;
  }


  // Private static methods

  /**
   * Converts an XML node to a Document instance. 
   */
  private static Document nodeToDocument(Element element)
  {
    Document result = new Document();

    result.webId = nodeValue(element, "web_id");
    result.title = nodeValue(element, "title");
    result.subject = nodeValue(element, "subject");
    result.pageCount = nodeValueInt(element, "page_count");
    result.pageWidth = nodeValueInt(element, "width");
    result.pageHeight = nodeValueInt(element, "height");
    result.fileSize = nodeValueInt(element, "file_size");
    result.status = nodeValueInt(element, "status");

    return result;
  }
}
