package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.GameProperty
import com.mobile_client.utils.SocketCommunicationConst
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.format
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class GameListService private constructor() {
    companion object {
        private const val BASE_MAPS_URL = "$ENVIRONMENT/api/maps/"
        private const val BASE_LOBBY_URL = "$ENVIRONMENT/api/lobby/"
        val instance: GameListService by lazy { GameListService() }
    }

    private val gson = AppGson
    private val http = HttpService.instance

    suspend fun getAllMaps(): List<GameMap> {
        return try {
            val response = http.get(BASE_MAPS_URL)
            val mapListType = object : TypeToken<List<GameMap>>() {}.type
            gson.fromJson(response, mapListType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getCurrentGames(): List<SocketCommunicationConst.SendableLobbies> {
        return try {
            val response = http.get(BASE_LOBBY_URL)
            val listType = object : TypeToken<List<SocketCommunicationConst.SendableLobbies>>() {}.type
            val raw: List<SocketCommunicationConst.SendableLobbies> = gson.fromJson(response, listType)
            raw.map { lobby ->
                val mapJson = gson.toJson(lobby.map)
                val gameMap = gson.fromJson(mapJson, GameMap::class.java)
                lobby.copy(map = gameMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
