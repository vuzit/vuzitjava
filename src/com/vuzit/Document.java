
package com.vuzit;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

      Element element = xmlRootNode(connection.getInputStream(), "document");

      if(element == null) {
        throw new ClientException("Response returned incorrect XML");
      }

      result.webId = childNodeValue(element, "web_id");
      result.title = childNodeValue(element, "title");
      result.subject = childNodeValue(element, "subject");
      result.pageCount = Integer.parseInt(childNodeValue(element, "page_count"));
      result.pageWidth = Integer.parseInt(childNodeValue(element, "width"));
      result.pageHeight = Integer.parseInt(childNodeValue(element, "height"));
      result.fileSize = Integer.parseInt(childNodeValue(element, "file_size"));
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
   * Uploads a document from disk via the Vuzit service.  
   */
  public static Document upload(String path)
  {
    Document result = null;

    File file = new File(path);
    FileInputStream stream = null;

    try {
      stream = new FileInputStream(file);
    } catch(java.io.FileNotFoundException e) {
      throw new ClientException("Cannot find file at path: " + path);
    }
    result = upload(stream, null, file.getName(), true);

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

    result.webId = childNodeValue(element, "web_id");
    try {
      response.close();
    } catch(IOException ex) {
    }

    return result;
  }

  /**
   * Uploads a file via an HTTP post operation.  Returns the stream response 
   * from the HTTP server.  
   */
  private static InputStream uploadFile(InputStream stream, String url, 
                                        String fileFormName, String contentType, 
                                        String fileName)
  {
    InputStream result = null;

    DataOutputStream dos = null;
    DataInputStream inStream = null;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024;

    if(contentType == null) {
      contentType = "application/octet-stream";
    }

    java.net.HttpURLConnection connection = httpConnection(url, "POST");

    try
    {
      connection.setDoInput(true);
      connection.setUseCaches(false);
      connection.setRequestProperty("Connection", "Keep-Alive");
      connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
      
      dos = new DataOutputStream(connection.getOutputStream());

      dos.writeBytes(twoHyphens + boundary + lineEnd);
      dos.writeBytes("Content-Disposition: form-data; name=\"" + fileFormName + "\";"
                     + " filename=\"" + fileName +"\"" + lineEnd);
      dos.writeBytes(lineEnd);

      // Create a buffer of maximum size
      bytesAvailable = stream.available();
      bufferSize = Math.min(bytesAvailable, maxBufferSize);
      buffer = new byte[bufferSize];

      // Read file and write it into form...
      bytesRead = stream.read(buffer, 0, bufferSize);

      while (bytesRead > 0)
      {
        dos.write(buffer, 0, bufferSize);
        bytesAvailable = stream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        bytesRead = stream.read(buffer, 0, bufferSize);
      }

      // Send multipart form data necesssary after file data...
      dos.writeBytes(lineEnd);
      dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

      stream.close();
      dos.flush();
      dos.close();
    } catch(IOException ex) {
    }

    try {
      result = connection.getInputStream();
    } catch(IOException ex) {
      result = null;
    }

    return result;
  }
}
