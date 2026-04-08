package com.mobile_client.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile_client.services.FriendService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.FriendEvents
import com.mobile_client.utils.FriendRequest
import com.mobile_client.utils.SearchableUser
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {
    private val friendsApi = FriendService.instance
    private val gson = Gson()

    private val _friends = MutableStateFlow<List<SearchableUser>>(emptyList())
    val friends: StateFlow<List<SearchableUser>> = _friends.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    private val _sentFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val sentFriendRequests: StateFlow<List<FriendRequest>> = _sentFriendRequests.asStateFlow()

    private val _allUsers = MutableStateFlow<List<SearchableUser>>(emptyList())
    val allUsers: StateFlow<List<SearchableUser>> = _allUsers.asStateFlow()

    private val _sentRequestUsernames = MutableStateFlow<Set<String>>(emptySet())
    val sentRequestUsernames: StateFlow<Set<String>> = _sentRequestUsernames.asStateFlow()

    private val _receivedRequestIds = MutableStateFlow<Map<String, String>>(emptyMap())
    val receivedRequestIds: StateFlow<Map<String, String>> = _receivedRequestIds.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<String>>(emptyList())
    val blockedUsers: StateFlow<List<String>> = _blockedUsers.asStateFlow()

    private val _blockedByUsers = MutableStateFlow<List<String>>(emptyList())
    val blockedByUsers: StateFlow<List<String>> = _blockedByUsers.asStateFlow()

    private val _pendingBlock = MutableStateFlow<Set<String>>(emptySet())
    val pendingBlock: StateFlow<Set<String>> = _pendingBlock.asStateFlow()
    private var currentFilter = ""

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun initializeSocketListeners() {
        val socket = SocketService.instance.socket ?: return

        socket.off(FriendEvents.NEW_FRIEND)
        socket.off(FriendEvents.NEW_REQUEST)
        socket.off(FriendEvents.NEW_SENT_REQUEST)
        socket.off(FriendEvents.NEW_BLOCK)

        socket.on(FriendEvents.NEW_FRIEND) {
            loadFriends()
            loadAllUsers()
            loadSentRequestUsernames()
        }
        socket.on(FriendEvents.NEW_REQUEST) {
            loadFriendRequests()
            loadReceivedRequestUsernames()
        }
        socket.on(FriendEvents.NEW_SENT_REQUEST) {
            loadSentFriendRequests()
            loadSentRequestUsernames()
        }

        socket.on(FriendEvents.NEW_BLOCK) {
            loadBlockedUsers()
            loadFriends()
            loadAllUsers()
            loadSentFriendRequests()
            loadSentRequestUsernames()
        }
    }

    fun loadAllUsers(filter: String = currentFilter) {
        currentFilter = filter
        viewModelScope.launch {
            try {
                val json = friendsApi.getUsers(filter)
                val type = object : TypeToken<List<SearchableUser>>() {}.type
                val users: List<SearchableUser> = gson.fromJson(json, type)
                _allUsers.value = users
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeSocketListeners() {
        val socket = SocketService.instance.socket ?: return
        socket.off(FriendEvents.NEW_FRIEND)
        socket.off(FriendEvents.NEW_REQUEST)
        socket.off(FriendEvents.NEW_SENT_REQUEST)
        socket.off(FriendEvents.NEW_BLOCK)
    }

    fun loadFriends() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getFriends()
                val type = object : TypeToken<List<SearchableUser>>() {}.type
                _friends.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getFriendRequests()
                val type = object : TypeToken<List<FriendRequest>>() {}.type
                _friendRequests.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSentFriendRequests() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getSentFriendRequests()
                val type = object : TypeToken<List<FriendRequest>>() {}.type
                _sentFriendRequests.value = gson.fromJson(json, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadSentRequestUsernames() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getSentFriendRequests()
                val type = object : TypeToken<List<FriendRequest>>() {}.type
                val requests: List<FriendRequest> = gson.fromJson(json, type)
                _sentRequestUsernames.value = requests.map { it.username }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendFriendRequest(username: String) {
        viewModelScope.launch {
            try {
                val response = friendsApi.sendFriendRequest(username)
                if (response.status.value in 200..299) {
                    _sentRequestUsernames.value = _sentRequestUsernames.value + username
                    loadSentFriendRequests()
                    _snackbarMessage.value = "Demande d'ami envoyée à $username"
                } else {
                    val body = response.bodyAsText()
                    val errorMsg = try {
                        gson.fromJson(body, Map::class.java)["message"]?.toString() ?: "Erreur inconnue"
                    } catch (e: Exception) {
                        body
                    }
                    _snackbarMessage.value = errorMsg
                }
            } catch (e: Exception) {
                _snackbarMessage.value = e.message ?: "Erreur réseau"
                e.printStackTrace()
            }
        }
    }

    fun acceptFriendRequest(id: String) {
        viewModelScope.launch {
            try {
                friendsApi.respondToRequest(id, true)
                loadFriends()
                loadFriendRequests()
                loadReceivedRequestUsernames()
                loadAllUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun denyFriendRequest(id: String) {
        viewModelScope.launch {
            try {
                friendsApi.respondToRequest(id, false)
                loadFriendRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFriend(username: String) {
        viewModelScope.launch {
            try {
                friendsApi.unfriend(username)
                loadFriends()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun undoFriendRequest(id: String) {
        viewModelScope.launch {
            try {
                friendsApi.undoFriendRequest(id)
                loadSentFriendRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadReceivedRequestUsernames() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getFriendRequests()
                val type = object : TypeToken<List<FriendRequest>>() {}.type
                val requests: List<FriendRequest> = gson.fromJson(json, type)
                _receivedRequestIds.value = requests.associate { it.username to it.id }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadBlockedUsers() {
        viewModelScope.launch {
            try {
                val json = friendsApi.getBlockedUsers()
                val type = object : TypeToken<Map<String, List<String>>>() {}.type
                val data: Map<String, List<String>> = gson.fromJson(json, type)
                _blockedUsers.value = data["blocked"] ?: emptyList()
                _blockedByUsers.value = data["blockedBy"] ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun blockUser(username: String) {
        viewModelScope.launch {
            try {
                _pendingBlock.value = _pendingBlock.value + username
                friendsApi.blockUser(username)
                loadFriends()
                loadBlockedUsers()
                loadAllUsers()
                loadSentFriendRequests()
                loadSentRequestUsernames()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _pendingBlock.value = _pendingBlock.value - username
            }
        }
    }

    fun loadAll() {
        loadFriends()
        loadFriendRequests()
        loadSentFriendRequests()
        loadReceivedRequestUsernames()
        loadBlockedUsers()
    }
}
