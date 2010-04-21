package com.vuzit;

import org.w3c.dom.*;

/**
 * Class for manipulating events from Vuzit.  
 */
public class Event extends Base
{
  // Private class variables
  private String webId = null;
  private String event = null;
  private String remoteHost = null;
  private String referer = null;
  private String userAgent = null;
  private String custom = null;
  private java.util.Date requestedAt = null;
  private int page = -1;
  private int duration = -1;

  // Public instance data members

  /**
   * Returns the web id of the document.  
   */
  public String getId() {
    return webId;
  }

  /**
   * Returns the event type of the request.  
   */
  public String getEvent() {
    return event;
  }

  /**
   * Returns the remote host of the request.  
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * Returns the referer of the request.  
   */
  public String getReferer() {
    return referer;
  }

  /**
   * Returns the user agent of the request.  
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Returns the custom tag of the request.  
   */
  public String getCustom() {
    return custom;
  }

  /**
   * Returns the request time of the request.  
   */
  public java.util.Date getRequestedAt() {
    return requestedAt;
  }
 
  /**
   * Returns the page of the request.  
   */
  public int getPage() {
    return page;
  }

  /**
   * Returns the duration of the request.  
   */
  public int getDuration() {
    return duration;
  }


  // Public static methods

  /**
   * Loads up all events for the given document. 
   */
  public static Event[] findAll(String webId)
  {
    return findAll(webId, new OptionList());
  }

  /**
   * Loads up all events according to the query options. 
   */
  public static Event[] findAll(String webId, OptionList options)
  {
    if(webId == null) {
      throw new ClientException("webId cannot be null");
    }
    Event[] result = null;

    OptionList parameters = postParameters(options, "show", webId);
    parameters.add("web_id", webId);
    String url = parametersToUrl("events.xml", parameters);
    java.net.HttpURLConnection connection = httpConnection(url, "GET");

    try
    {
      connection.connect();

      Element element = xmlRootNode(connection.getInputStream(), "events");
      if(element == null) {
        throw new ClientException("Response returned incorrect XML");
      }

      NodeList nameList = element.getChildNodes();
      java.util.ArrayList list = new java.util.ArrayList();

      for(int i = 0; i < nameList.getLength(); i++)
      {
        Node node = nameList.item(i);
        if(node.getNodeName().equals("event")) {
          list.add(nodeToEvent((Element)node));
        }
      }

      result = new Event[list.size()];
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
  private static Event nodeToEvent(Element element)
  {
    Event result = new Event();

    result.webId = nodeValue(element, "web_id");
    result.event = nodeValue(element, "event");
    result.remoteHost = nodeValue(element, "remote_host");
    result.referer = nodeValue(element, "referer");
    result.userAgent = nodeValue(element, "user_agent");
    result.custom = nodeValue(element, "custom");
    result.requestedAt = new java.util.Date(
                                 nodeValueLong(element, "requested_at") * 1000);
    result.page = nodeValueInt(element, "page");
    result.duration = nodeValueInt(element, "duration");

    return result;
  }
}
