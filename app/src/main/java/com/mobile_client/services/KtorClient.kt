package com.mobile_client.services
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson

object HttpService {
    private val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        install(io.ktor.client.plugins.DefaultRequest) {
            contentType(ContentType.Application.Json)
            val token = AccountRepository.getToken()
            if (!token.isNullOrEmpty()) {
                headers.append("Authorization", "Bearer $token")
            }
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
