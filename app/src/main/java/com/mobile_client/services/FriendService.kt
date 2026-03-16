package com.mobile_client.services

import com.mobile_client.environment.ENVIRONMENT
import io.ktor.client.statement.HttpResponse

class FriendService private constructor() {
    companion object {
        val instance: FriendService by lazy { FriendService() }
    }

    private val baseURL = "$ENVIRONMENT/api/friends/"
    private val http = HttpService.instance

    suspend fun getFriends(): String {
        return http.get(baseURL)
    }

    suspend fun getUsers(filter: String = ""): String {
        return http.get("${ENVIRONMENT}/api/user/users?filter=$filter&isFriends=false")
    }

    suspend fun sendFriendRequest(username: String): HttpResponse {
        return http.post(baseURL, mapOf("username" to username))
    }

    suspend fun unfriend(username: String): HttpResponse {
        return http.delete(baseURL, mapOf("username" to username))
    }

    suspend fun getFriendRequests(): String {
        return http.get("${baseURL}request/")
    }

    suspend fun getSentFriendRequests(): String {
        return http.get("${baseURL}sentRequest/")
    }

    suspend fun respondToRequest(id: String, isAccepted: Boolean): HttpResponse {
        return http.post("${baseURL}request", mapOf("id" to id, "isAccepted" to isAccepted))
    }

    suspend fun undoFriendRequest(id: String): HttpResponse {
        return http.delete("${baseURL}sentRequest/", mapOf("id" to id))
    }

    suspend fun blockUser(username: String): HttpResponse {
        return http.post("${baseURL}block", mapOf("username" to username))
    }

    suspend fun getBlockedUsers(): String {
        return http.get("${baseURL}block")
    }
}
