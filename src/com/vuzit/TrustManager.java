package com.vuzit;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

/**
 * Custom class for handing SSL client trust certificates.  
 */
public class TrustManager implements javax.net.ssl.X509TrustManager
{
   public void checkClientTrusted(X509Certificate chain[], String authType) 
     throws java.security.cert.CertificateException 
   {
   }

   public void checkServerTrusted(X509Certificate chain[], String authType) 
     throws java.security.cert.CertificateException 
   {
   }

   public X509Certificate[] getAcceptedIssuers()
   {
     return null;
   }
}

