package com.mobile_client.services

import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson

class TutorialService private constructor() {
    companion object {
        val instance: TutorialService by lazy { TutorialService() }
    }

    private val http = HttpService.instance
    private val baseUrl = "$ENVIRONMENT/api/user/tutorial"

    suspend fun getProgress(): Int {
        return try {
            val response = http.get(baseUrl)
            val json = AppGson.fromJson(response, Map::class.java)
            (json["currentStep"] as? Double)?.toInt() ?: 0
        } catch (e: Exception) {
            println("TutorialService getProgress error: ${e.message}")
            0
        }
    }

    suspend fun updateProgress(step: Int): Boolean {
        return try {
            val response = http.post(baseUrl, mapOf("currentStep" to step))
            response.status.value in 200..299
        } catch (e: Exception) {
            println("TutorialService updateProgress error: ${e.message}")
            false
        }
    }
}
