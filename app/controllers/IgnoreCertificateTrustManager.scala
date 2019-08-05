package controllers

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class IgnoreCertificateTrustManager extends X509TrustManager {
  override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    // do not throw anything
  }

  override def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
    // do not throw anything
  }
  
  override def getAcceptedIssuers: Array[java.security.cert.X509Certificate] = {
    Array.empty[X509Certificate]
  }
}