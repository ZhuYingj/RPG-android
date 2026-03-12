package com.mobile_client.services

import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.Account
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

class AccountService private constructor() {
    companion object {
        val instance: AccountService by lazy { AccountService() }
    }

    private val http = HttpService.instance
    private val baseUrl = "$ENVIRONMENT/api/user/param"

    var token: String? = null
        private set

    var accountInfo: Account? = null
        private set

    val username: String
        get() = accountInfo?.username ?: ""

    fun setAccount(account: Account, token: String) {
        this.accountInfo = account
        this.token = token
    }

    suspend fun updateUsername(newUsername: String): Account {
        val current = accountInfo ?: throw Exception("Non connecté")
        if (current.username == newUsername) return current

        val response = http.post("$baseUrl/username", mapOf("username" to newUsername))
        if (response.status.value !in 200..299) {
            val errorBody = response.bodyAsText()
            val message = try {
                JSONObject(errorBody).getString("message")
            } catch (_: Exception) {
                "Échec de la mise à jour du nom d'utilisateur"
            }
            throw Exception(message)
        }

        val updated = current.copy(username = newUsername)
        accountInfo = updated
        return updated
    }

    suspend fun updateEmail(newEmail: String): Account {
        val current = accountInfo ?: throw Exception("Non connecté")
        if (current.email == newEmail) return current

        val response = http.post("$baseUrl/email", mapOf("email" to newEmail))
        if (response.status.value !in 200..299) {
            val errorBody = response.bodyAsText()
            val message = try {
                JSONObject(errorBody).getString("message")
            } catch (_: Exception) {
                "Échec de la mise à jour de l'email"
            }
            throw Exception(message)
        }

        val updated = current.copy(email = newEmail)
        accountInfo = updated
        return updated
    }

    suspend fun updateAvatar(avatar: String): Account {
        val current = accountInfo ?: throw Exception("Non connecté")
        val response = http.post("$baseUrl/avatar", mapOf("avatar" to avatar))
        if (response.status.value !in 200..299) {
            val errorBody = response.bodyAsText()
            val message = try {
                JSONObject(errorBody).getString("message")
            } catch (_: Exception) {
                "Échec de la mise à jour de l'avatar"
            }
            throw Exception(message)
        }
        val updated = current.copy(avatar = avatar)
        accountInfo = updated
        return updated
    }

    suspend fun logout(): Boolean {
        return try {
            val response = http.post("$ENVIRONMENT/api/auth/logout", emptyMap<String, String>())
            if (response.status.value in 200..299) {
                SocketService.instance.disconnect()
                clear()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }
    fun clear() {
        token = null
        accountInfo = null
    }
}
