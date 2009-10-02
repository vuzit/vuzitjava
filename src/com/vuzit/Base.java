
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
import org.w3c.dom.*;

public class Base
{
  /**
   * Returns the child node text value of an element.  
   */
  protected static String childNodeValue(Element element, String childName)
  {
    String result = null;

    NodeList nameList = element.getElementsByTagName(childName);
    Element nameElement = (Element)nameList.item(0);

    NodeList textList = nameElement.getChildNodes();

    if(textList.getLength() > 0) {
      result = ((Node)textList.item(0)).getNodeValue().trim();
    }
    else {
      result = "";
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

      result.append(key).append("=");

      try {
        value = URLEncoder.encode(value, "UTF-8"); 
      }
      catch(UnsupportedEncodingException uee) {
        System.err.println(uee);
      }
      result.append(value);
      
      result.append("&");
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
}
