package com.mobile_client.services

import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class LeaderBoardService private constructor() {
    companion object {
        val instance: LeaderBoardService by lazy { LeaderBoardService() }
    }

    private val client = OkHttpClient()
    private val gson = AppGson
    private val baseUrl = "${ENVIRONMENT}/api/ranking"

    suspend fun getMoneyLeaderboard(): List<LeaderboardEntry> {
        return fetchLeaderboard("/money")
    }

    suspend fun getPlayTimeLeaderboard(): List<LeaderboardEntry> {
        val entries = fetchLeaderboard("/playtime")
        return entries.sortedByDescending { entry ->
            val stats = entry.stats ?: return@sortedByDescending 0.0
            (stats.CTFGamesPlayed + stats.classicGamesPlayed) * stats.averageGameTime
        }
    }

    suspend fun getGamesWonLeaderboard(): List<LeaderboardEntry> {
        return fetchLeaderboard("/wins")
    }

    suspend fun getBattlesWin() : List<LeaderboardEntry> {
        return fetchLeaderboard("/battleswins")
    }

    private suspend fun fetchLeaderboard(endpoint: String): List<LeaderboardEntry> {
        return withContext(Dispatchers.IO) {
            try {
                val token = AccountService.instance.token ?: return@withContext emptyList()
                val request = Request.Builder()
                    .url("$baseUrl$endpoint")
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
                    gson.fromJson<List<LeaderboardEntry>>(body, type)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
