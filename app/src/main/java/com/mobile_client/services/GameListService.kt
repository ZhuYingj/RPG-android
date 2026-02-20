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

class GameListService private constructor() {

    companion object {
    private const val BASE_URL = "$ENVIRONMENT/api/maps/"
        val instance: GameListService by lazy { GameListService() }
    }


    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(json.asString)
            } catch (e: Exception) {
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    format.timeZone = TimeZone.getTimeZone("UTC")
                    format.parse(json.asString)
                } catch (e2: Exception) {
                    Date()
                }
            }
        })
        .create()

    suspend fun getAllMaps(isVisible: Boolean = false): List<GameMap> {
        return try {
            val response = HttpService.get(BASE_URL)
            val mapListType = object : TypeToken<List<GameMap>>() {}.type
            val maps: List<GameMap> = gson.fromJson(response, mapListType)
            if (isVisible) maps.filter { it.isVisible } else maps
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
