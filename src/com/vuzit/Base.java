
package com.vuzit;

import java.util.Hashtable;
import java.util.Iterator;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
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
  protected static String childNodeValue(Element element, String childName)
  {
    String result = "";

    NodeList nameList = element.getElementsByTagName(childName);
    Element nameElement = (Element)nameList.item(0);

    NodeList textList = nameElement.getChildNodes();

    if(textList.getLength() > 0) {
      result = ((Node)textList.item(0)).getNodeValue().trim();
    }

    return result;
  }

  /**
   * Returns a HTTP connection based upon the URL and request method.  
   */
  protected static HttpURLConnection httpConnection(String url, String method)
  {
    HttpURLConnection result = null;

    try {
      URL address = new URL(url);
      result = (HttpURLConnection)address.openConnection();
      result.setRequestProperty("User-agent", Service.getUserAgent());
      result.setRequestMethod(method);
      result.setDoOutput(true);
      result.setReadTimeout(10000);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (ProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }

  /**
   *  Changes an array (hash table) of parameters to a url. 
   */
  protected static String parametersToUrl(String resource, Hashtable params, String id)
  {
    StringBuilder result = new StringBuilder();

    result.append(Service.getServiceUrl()).append("/").append(resource);

    if(id != null) {
      result.append("/").append(id);
    }
    result.append(".xml?");

    Iterator it = params.keySet().iterator();
    while (it.hasNext())
    {
      String key = (String)it.next();
      String value = (String)params.get(key);

      // Do not add keys with null or empty values
      // TODO: Check the length of the string as well
      if(value != null)
      {
        try {
          result.append(key).append("=");
          value = URLEncoder.encode(value, "UTF-8"); 
          result.append(value);
          result.append("&");
        }
        catch(UnsupportedEncodingException uee) {
          System.err.println(uee);
        }
      }
    }

    return result.toString();
  }

  /**
   *  Returns the default HTTP post parameters array.  
   */
  protected static Hashtable postParameters(String method, String id)
  {
    Hashtable result = new Hashtable();

    result.put("method", method);
    result.put("key", Service.getPublicKey());

    java.util.Date now = new java.util.Date();
    String signature = Service.signature(method, id, now);

    result.put("signature", signature);
    result.put("timestamp", Long.toString(Service.epochTime(now)));

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
      throw new ClientException(childNodeValue(errorNode, "msg"), 
                                childNodeValue(errorNode, "code"));
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
