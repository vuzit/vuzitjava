
package com.vuzit;

/**
 * Traps web client exception errors.  
 */
public class WebClientException extends java.lang.Exception
{
  // Private instance variables
  private int code = -1;

  // Constructors

  public WebClientException(String message)
  {
    super(message);
  }

  public WebClientException(String message, String code)
  {
    super("Code [" + code + "], Message: " + message);
    this.code = Integer.parseInt(code);
  }

  // Instance methods

  /**
   * Returns the error code. 
   */
  public int getCode()
  {
    return code;
  }
}
