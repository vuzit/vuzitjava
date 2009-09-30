
package com.vuzit;

/**
 * Class for manipulating documents from Vuzit.  
 */
public class Document extends Base
{
  // Private class variables
  private String id = null;
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
    return id;
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
   * Loads a document by the web id.  
   */
  public static Document findById(String webId)
  {
    Document result = new Document();
    result.id = "test";

    // TODO: Add the loading code in here

    return result;
  }
}
