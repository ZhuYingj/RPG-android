package com.mobile_client.services
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.gson.*
import io.ktor.http.*

object HttpService {
    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    suspend fun get(url: String): String {
        val response: HttpResponse = client.get(url)
        return response.body()
    }

    suspend fun post(url: String, body: Any): HttpResponse {
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

}
