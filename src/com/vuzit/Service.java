
package com.vuzit;

import java.util.Date;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Service
{
   // Public static variables
   private static String publicKey = null;
   private static String privateKey = null;
   private static String serviceUrl = "http://vuzit.com";
   private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

   /**
    * Returns the public key.  
    */
   public static String getPublicKey()
   {
     return publicKey;
   }

   /**
    * Sets the public key.
    */
   public static void setPublicKey(String key)
   {
     publicKey = key;
   }

   /**
    * Returns the private key.  
    */
   public static String getPrivateKey()
   {
     return privateKey;
   }

   /**
    * Sets the private key.
    */
   public static void setPrivateKey(String key)
   {
     privateKey = key;
   }

   public static String signature(String service, String id, Date date)
   {
     String result;

     if(id == null) {
       id = "";
     }

     if(date == null) {
       date = new Date();
     }

     String msg = service + id + publicKey + (date.getTime() / 1000);

     try {
       result = calculateRFC2104HMAC(msg, privateKey);
     } catch (Exception e) {
       result = null;
     }

     return result;
   }

   /**
    * Computes RFC 2104-compliant HMAC signature.
    */
   private static String calculateRFC2104HMAC(String data, String key)
     throws java.security.SignatureException
   {
     String result;

     try {
       // get an hmac_sha1 key from the raw key bytes
       SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

       // get an hmac_sha1 Mac instance and initialize with the signing key
       Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
       mac.init(signingKey);

       // compute the hmac on input data bytes
       byte[] rawHmac = mac.doFinal(data.getBytes());

       // base64-encode the hmac
       result = Base64.encodeBytes(rawHmac);

     } catch (Exception e) {
       throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
     }

     return result;
   }

}
