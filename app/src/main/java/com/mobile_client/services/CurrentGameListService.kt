package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.GameMap
import com.mobile_client.utils.SocketCommunicationConst

class CurrentGameListService private constructor() {
    companion object {
        private const val BASE_URL = "$ENVIRONMENT/api/lobby/"
        val instance: CurrentGameListService by lazy { CurrentGameListService() }
    }

    private val gson = AppGson

    suspend fun getCurrentGames(): List<SocketCommunicationConst.SendableLobbies> {
        return try {
            val response = HttpService.instance.get(BASE_URL)
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
