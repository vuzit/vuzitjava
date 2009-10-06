
package com.vuzit;

/**
 * Traps web client exception errors.  
 */
public class ClientException extends java.lang.RuntimeException
{
  // Private instance variables
  private int code = -1;

  // Constructors

  public ClientException(String message)
  {
    super(message);
  }

  public ClientException(String message, String code)
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
