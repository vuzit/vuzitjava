package com.vuzit;

import org.w3c.dom.*;

/**
 * Class for loading page text.  
 */
public class Page extends Base
{
  // Private class variables
  private int pageNumber = -1;
  private String pageText = null;

  // Public instance data members

  /**
   * Returns the page number.  
   */
  public int getNumber() {
    return pageNumber;
  }

  /**
   * Returns the page text.  
   */
  public String getText() {
    return pageText;
  }

  // Public static methods

  /**
   * Loads up all events according to the query options. 
   */
  public static Page[] findAll(String webId, OptionList options)
  {
    Page[] result = null;

    OptionList parameters = postParameters(options, "index", webId);
    parameters.add("web_id", webId);
    String url = parametersToUrl("documents/" + webId + "/pages.xml", parameters);
    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    try
    {
      connection.connect();

      Element element = xmlRootNode(connection.getInputStream(), "pages");
      if(element == null) {
        throw new ClientException("Response returned incorrect XML");
      }

      NodeList nameList = element.getChildNodes();
      java.util.ArrayList list = new java.util.ArrayList();

      for(int i = 0; i < nameList.getLength(); i++)
      {
        Node node = nameList.item(i);
        if(node.getNodeName().equals("page")) {
          list.add(nodeToPage((Element)node));
        }
      }

      result = new Page[list.size()];
      list.toArray(result);
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

  // Private static methods

  /**
   * Converts an XML node to an Event instance. 
   */
  private static Page nodeToPage(Element element)
  {
    Page result = new Page();

    result.pageNumber = nodeValueInt(element, "number");
    result.pageText = nodeValue(element, "text");

    return result;
  }
}
