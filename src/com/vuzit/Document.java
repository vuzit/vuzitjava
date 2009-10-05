
package com.vuzit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.HttpURLConnection;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

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
        throw new WebClientException("HTTP error: [" + 
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
   * Loads a document by the web id.  
   */
  public static Document findById(String webId)
  {
    Document result = new Document();

    java.util.Hashtable parameters = postParameters("show", webId);
    String url = parametersToUrl("documents", parameters, webId);
    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    try
    {
      connection.connect();

      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      org.w3c.dom.Document doc = docBuilder.parse(connection.getInputStream());

      doc.getDocumentElement().normalize();

      NodeList docList = doc.getElementsByTagName("document");

      for(int i = 0; i < docList.getLength(); i++)
      {
         Node firstNode = docList.item(i);

         if(firstNode.getNodeType() == Node.ELEMENT_NODE)
         {
           Element element = (Element)firstNode;
           result.webId = childNodeValue(element, "web_id");
           result.title = childNodeValue(element, "title");
           result.subject = childNodeValue(element, "subject");
           result.pageCount = Integer.parseInt(childNodeValue(element, "page_count"));
           result.pageWidth = Integer.parseInt(childNodeValue(element, "width"));
           result.pageHeight = Integer.parseInt(childNodeValue(element, "height"));
           result.fileSize = Integer.parseInt(childNodeValue(element, "file_size"));
         }
      }
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
   * Uploads a document vai the Vuzit service.  
   */
  public static Document upload(String path)
  {
    Document result = new Document();
    result.webId = "TODO";

    // TODO: Check if the file exists here

    return result;
  }
}
