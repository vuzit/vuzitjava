
import com.vuzit.*;
import java.util.Date;

public class VuzitCL
{
  /**
   * Main code area.  
   */
  public static void main(String[] args)
  {
    // TODO: Get parameters from command-line
    com.vuzit.Service.setPublicKey("PUBLIC_KEY");
    com.vuzit.Service.setPrivateKey("PRIVATE_KEY");

    Date date = new Date(1247967923000L);
    System.out.println(date.toString());

    String sig = com.vuzit.Service.signature("show", "1bgi", date);
    System.out.println(sig);
  }
}
