
package com.vuzit;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataOutputStream;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * Vuzit web client base class.  
 */
public abstract class Base
{
  /**
   * Returns the child node text value of an element.  
   */
  protected static String nodeValue(Element element, String childName)
  {
    String result = null;

    NodeList nameList = element.getElementsByTagName(childName);

    if(nameList.getLength() > 0)
    {
      Element nameElement = (Element)nameList.item(0);

      NodeList textList = nameElement.getChildNodes();

      if(textList.getLength() > 0) {
        result = ((Node)textList.item(0)).getNodeValue().trim();
      }
    }

    return result;
  }

  /**
   * Returns the child node integer value of an element.  
   */
  protected static int nodeValueInt(Element element, String childName)
  {
    String text = nodeValue(element, childName);

    return (text == null) ? -1 : Integer.parseInt(text);
  }

  /**
   * Returns a HTTP connection based upon the URL and request method.  
   */
  protected static HttpURLConnection httpConnection(String url, String method)
  {
    HttpURLConnection result = null;

    try {
      java.net.URL address = new java.net.URL(url);
      result = (HttpURLConnection)address.openConnection();
      result.setRequestProperty("User-agent", Service.getUserAgent());
      result.setRequestMethod(method);
      result.setDoOutput(true);
      // 60 second timeout to prevent timeouts with large requests,
      // a busy server or IIS delays
      result.setReadTimeout(60 * 1000); 
    } catch (java.net.MalformedURLException e) {
      e.printStackTrace();
    } catch (java.net.ProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   *  Changes an array (hash table) of parameters to a URL with a default 
   *  extension of "XML". 
   */
  protected static String parametersToUrl(String resource, OptionList params, 
                                          String id)
  {
    return parametersToUrl(resource, params, id, "xml");
  }


  /**
   *  Changes an array (hash table) of parameters to a url. 
   */
  protected static String parametersToUrl(String resource, OptionList params, 
                                          String id, String extension)
  {
    StringBuilder result = new StringBuilder();

    result.append(Service.getServiceUrl()).append("/").append(resource);

    if(id != null) {
      result.append("/").append(id);
    }
    result.append(".").append(extension).append("?");

    for(java.util.Enumeration e = params.keys(); e.hasMoreElements() ;)
    {
      String key = (String)e.nextElement();
      String value = (String)params.get(key);

      // Do not add keys with null or empty values
      // TODO: Check the length of the string as well
      if(value != null)
      {
        try {
          result.append(key).append("=");
          value = java.net.URLEncoder.encode(value, "UTF-8"); 
          result.append(value);
          result.append("&");
        }
        catch(java.io.UnsupportedEncodingException uee) {
          System.err.println(uee);
        }
      }
    }

    return result.toString();
  }

  /**
   *  Returns the default HTTP post parameters array.  
   */
  protected static OptionList postParameters(OptionList options, String method, String id)
  {
    // The keys must be set before this can function
    if(Service.getPublicKey() == null) {
      throw new ClientException("The public key cannot be null");
    }
    if(Service.getPrivateKey() == null) {
      throw new ClientException("The private key cannot be null");
    }

    options.add("method", method);
    options.add("key", Service.getPublicKey());

    java.util.Date now = new java.util.Date();
    String signature = Service.signature(method, id, now, options);

    options.add("signature", signature);
    options.add("timestamp", Long.toString(Service.epochTime(now)));

    return options;
  }

  /**
   * Uploads a file via an HTTP post operation.  Returns the stream response 
   * from the HTTP server.  
   */
  protected static InputStream uploadFile(InputStream stream, String url, 
                                          String fileFormName, String contentType, 
                                          String fileName)
  {
    InputStream result = null;

    DataOutputStream dos = null;
    java.io.DataInputStream inStream = null;
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
      // Set the timeout to 5 minutes since it's possibly a large file
      connection.setReadTimeout(5 * 60 * 1000); 
      
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

  /**
   * Traps and extracts web client errors for debugging purposes.  
   */
  protected static void webClientErrorCheck(HttpURLConnection connection)
  {
    org.w3c.dom.Document doc;

    try
    {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      doc = docBuilder.parse(connection.getErrorStream());
      doc.getDocumentElement().normalize();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return;
    }

    NodeList errorList = doc.getElementsByTagName("err");
    if(errorList.getLength() > 0)
    {
      Element errorNode = (Element)errorList.item(0);
      throw new ClientException(nodeValue(errorNode, "msg"), 
                                nodeValue(errorNode, "code"));
    }
  }

  /**
   * Loads the defined root node from a block of XML.  Returns null if there is 
   * an issue or the root node could not be found. 
   */
  protected static Element xmlRootNode(InputStream stream, String nodeName)
  {
    Element result = null;

    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      org.w3c.dom.Document doc = docBuilder.parse(stream);

      doc.getDocumentElement().normalize();

      NodeList docList = doc.getElementsByTagName(nodeName);

      for(int i = 0; i < docList.getLength(); i++)
      {
        Node firstNode = docList.item(i);

        if(firstNode.getNodeType() == Node.ELEMENT_NODE)
        {
          result = (Element)firstNode;
          break;
        }
      }
    }
    catch(Exception ex)
    {
      result = null;
    }

    return result;
  }
}
