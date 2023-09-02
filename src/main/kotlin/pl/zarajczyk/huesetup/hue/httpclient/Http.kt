package pl.zarajczyk.huesetup.hue.httpclient

import java.net.HttpURLConnection
import java.net.URL
import java.security.GeneralSecurityException
import java.security.cert.X509Certificate
import javax.net.ssl.*


/**
 * Inspiration:
 * https://github.com/ZeroOne3010/yetanotherhueapi/blob/master/src/main/java/io/github/zeroone3010/yahueapi/HttpUtil.java
 *
 * Thank you!
 */
object Http {
    fun request(url: URL, body: String?, method: String, headers: Map<String, String> = emptyMap()): String {
        val connection: HttpURLConnection = if (url.protocol.equals("https")) {
            TrustEverythingManager.createAllTrustedConnection(url)
        } else {
            url.openConnection() as HttpURLConnection
        }
        connection.doOutput = true
        connection.requestMethod = method
        connection.setRequestProperty("Host", connection.url.host)
        headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
        if (body != null) {
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(body)
                writer.flush()
            }
        }
        connection.connect()
        try {
            return connection.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            val error = try {
                connection.errorStream.bufferedReader().use { it.readText() }
            } catch (e1: Exception) {
                "Unable to get error: ${e1.message}"
            }
            throw RuntimeException("HTTP Request ≪$method $url≫ ${body?.let { "(with body ≪$it≫)" }} returned error: $error", e)
        }
    }

}


class TrustEverythingManager : X509TrustManager {
    override fun getAcceptedIssuers() = arrayOf<X509Certificate>()

    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {
        // Do nothing
    }

    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {
        // Do nothing
    }

    companion object {
        @Throws(GeneralSecurityException::class)
        fun createSSLSocketFactory(): SSLSocketFactory {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(TrustEverythingManager()), null)
            return sslContext.socketFactory
        }

        private fun createHostnameVerifier(bridgeIp: String?): HostnameVerifier {
            return HostnameVerifier { hostname: String, session: SSLSession? -> bridgeIp == null || hostname == bridgeIp }
        }

        fun createAllTrustedConnection(url: URL): HttpsURLConnection {
            val connection = url.openConnection() as HttpsURLConnection
            connection.setHostnameVerifier(createHostnameVerifier(null))
            connection.setSSLSocketFactory(createSSLSocketFactory())
            return connection
        }
    }
}