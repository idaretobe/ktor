package io.ktor.client

import javax.net.ssl.*


open class HttpClientBackendConfig {
    var sslContext: SSLContext? = null
}