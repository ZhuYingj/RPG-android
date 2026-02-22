package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.GameMap

class GameListService private constructor() {
    companion object {
    private const val BASE_URL = "$ENVIRONMENT/api/maps/"
        val instance: GameListService by lazy { GameListService() }
    }

    private val gson = AppGson

    suspend fun getAllMaps(isVisible: Boolean = false): List<GameMap> {
        return try {
            val response = HttpService.instance.get(BASE_URL)
            val mapListType = object : TypeToken<List<GameMap>>() {}.type
            val maps: List<GameMap> = gson.fromJson(response, mapListType)
            if (isVisible) maps.filter { it.isVisible } else maps
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
