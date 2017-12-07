package io.ktor.client

import io.ktor.client.engine.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.client.utils.*


object HttpClientFactory {
    fun createDefault(backendFactory: HttpClientEngineFactory<*>, block: ClientConfig.() -> Unit = {}): HttpClient {
        val backend = backendFactory.create()

        return ClientConfig(backend).apply {
            install("backend") {
                requestPipeline.intercept(HttpRequestPipeline.Send) { builder ->
                    val request = (builder as? HttpRequestBuilder)?.build() ?: return@intercept

                    if (request.body !is HttpMessageBody) error("Body can't be processed: ${request.body}")
                    val response = backend.makeRequest(request)
                    if (response.body !is HttpMessageBody) error("Client backend response has wrong format: ${request.body}")

                    proceedWith(HttpClientCall(request, response, context))
                }

                responsePipeline.intercept(HttpResponsePipeline.After) { container ->
                    container.response.close()
                }
            }

            install(HttpPlainText)
            install(HttpIgnoreBody)
            block()
        }.build()
    }
}
