package com.mobile_client.services

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.GameMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object GameListService {
    private const val BASE_URL = "$ENVIRONMENT/api/maps/"

    // Custom Gson with Date deserializer
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
            try {
                // Try ISO 8601 format first
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(json.asString)
            } catch (e: Exception) {
                try {
                    // Try without milliseconds
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    format.timeZone = TimeZone.getTimeZone("UTC")
                    format.parse(json.asString)
                } catch (e2: Exception) {
                    // Fallback to current date
                    Date()
                }
            }
        })
        .create()

    suspend fun getAllMaps(isVisible: Boolean = false): List<GameMap> {
        return try {
            val response = HttpService.get(BASE_URL)
            println("Raw response: $response")

            // Parse as JsonArray first to see raw data
            val jsonArray = gson.fromJson(response, com.google.gson.JsonArray::class.java)
            println("First map raw JSON: ${jsonArray.get(0)}")

            val mapListType = object : TypeToken<List<GameMap>>() {}.type
            val maps: List<GameMap> = gson.fromJson(response, mapListType)

            println("Parsed ${maps.size} maps successfully")
            maps.forEachIndexed { index, map ->
                println("Map $index: name=${map.name}, size=${map.size}")
            }

            if (isVisible) {
                maps.filter { it.isVisible }
            } else {
                maps
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error in getAllMaps: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            emptyList()
        }
    }
}
