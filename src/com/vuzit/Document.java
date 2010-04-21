
package com.vuzit;

import java.io.InputStream;
import java.io.FileInputStream;
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
  private String excerpt = null;
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
   * Returns a short excerpt of the document.  
   */
  public String getExcerpt() {
    return excerpt;
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
    if(webId == null) {
      throw new ClientException("webId cannot be null");
    }
    OptionList parameters = postParameters(new OptionList(), "destroy", webId);
    String url = parametersToUrl("documents/" + webId + ".xml", parameters);
    java.net.HttpURLConnection connection = httpConnection(url, "DELETE");

    try
    {
      connection.connect();

      if(connection.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
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
    if(webId == null) {
      throw new ClientException("webId cannot be null");
    }
    OptionList parameters = postParameters(new OptionList(), "show", webId);
    return parametersToUrl("documents/" + webId + "." + fileExtension, parameters);
  }

  /**
   * Loads a document by the web ID.  
   */
  public static Document find(String webId)
  {
    return find(webId, new OptionList());
  }

  /**
   * Loads a document by the web ID.  
   */
  public static Document find(String webId, OptionList options)
  {
    Document result = null;

    if(webId == null) {
      throw new ClientException("webId cannot be null");
    }

    OptionList parameters = postParameters(options, "show", webId);
    String url = parametersToUrl("documents/" + webId + ".xml", parameters);
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
   * Loads up all documents according to the query options. 
   */
  public static Document[] findAll(OptionList list)
  {
    Document[] result = null;

    OptionList parameters = postParameters(list, "index", null);
    String url = parametersToUrl("documents.xml", parameters);
    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    try
    {
      connection.connect();

      Element element = xmlRootNode(connection.getInputStream(), "documents");
      if(element == null) {
        throw new ClientException("Response returned incorrect XML");
      }

      NodeList nameList = element.getElementsByTagName("document");

      result = new Document[nameList.getLength()];

      for(int i = 0; i < nameList.getLength(); i++)
      {
        Element node = (Element)nameList.item(i);
        result[i] = nodeToDocument(node);
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
    return upload(path, new OptionList());
  }

  /**
   * Uploads a document from disk via the Vuzit service.  
   */
  public static Document upload(String path, OptionList options)
  {
    Document result = null;

    java.io.File file = new java.io.File(path);
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
    } catch(java.io.FileNotFoundException e) {
      throw new ClientException("Cannot find file at path: " + path);
    }

    options.add("file_name", file.getName());
    result = upload(stream, options);

    return result;
  }

  /**
   * Uploads a document from an InputStream via the Vuzit service.  
   */
  public static Document upload(InputStream stream)
  {
    return upload(stream, new OptionList());
  }

  /**
   * Uploads a document via an InputStream with options.  
   */
  public static Document upload(InputStream stream, OptionList options)
  {
    if(stream == null) {
      throw new ClientException("stream cannot be null");
    }
    Document result = new Document();

    String fileName = "document";
    if(!options.contains("file_name")) {
      fileName = options.get("file_name");
    }

    OptionList parameters = postParameters(options, "create", null);

    String url = parametersToUrl("documents.xml", parameters);
    InputStream response = uploadFile(stream, url, "upload", null, fileName);

    Element element = xmlRootNode(response, "document");
    if(element == null) {
      throw new ClientException("Response returned incorrect XML");
    }

    result.webId = nodeValue(element, "web_id");
    try {
      response.close();
    } catch(java.io.IOException ex) {
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
    result.excerpt = nodeValue(element, "excerpt");
    result.pageCount = nodeValueInt(element, "page_count");
    result.pageWidth = nodeValueInt(element, "width");
    result.pageHeight = nodeValueInt(element, "height");
    result.fileSize = nodeValueInt(element, "file_size");
    result.status = nodeValueInt(element, "status");

    return result;
  }
}
