package com.vuzit;

/**
 * Class for handling optional parameters for methods.  These options 
 * can then be directly applied to the web service parameterse.  
 */
public class OptionList
{
  private java.util.Hashtable list = new java.util.Hashtable();

  /**
   * Returns true if the list contains the given key. 
   */
  public Boolean contains(String key)
  {
    return list.containsKey(key);
  }

  /**
   * Returns the number of keys in the list. 
   */
  public int count()
  {
    return list.size();
  }

  /**
   * Loads a value from the list. 
   */
  public String get(String key)
  {
    return (String)list.get(key);
  }

  /**
   * Returns the keys in the list. 
   */
  public java.util.Enumeration keys()
  {
    return list.keys();
  }

  /**
   * Adds a string value. 
   */
  public OptionList add(String key, String value)
  {
    list.put(key, value);

    return this;
  }
  /**
   * Adds a Boolean value. 
   */
  public OptionList add(String key, Boolean value)
  {
    list.put(key, (value == true) ? "1" : "0");

    return this;
  }
  /**
   * Adds an integer value. 
   */
  public OptionList add(String key, int value)
  {
    list.put(key, Integer.toString(value));

    return this;
  }
}
