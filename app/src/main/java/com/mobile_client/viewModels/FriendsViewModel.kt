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

    private var currentFilter = ""

    fun initializeSocketListeners() {
        val socket = SocketService.instance.socket ?: return

        socket.off(FriendEvents.NEW_FRIEND)
        socket.off(FriendEvents.NEW_REQUEST)
        socket.off(FriendEvents.NEW_SENT_REQUEST)

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
    }

    fun loadAllUsers(filter: String = currentFilter) {
        currentFilter = filter
        viewModelScope.launch {
            try {
                val json = friendsApi.getUsers(filter)
                val type = object : TypeToken<List<SearchableUser>>() {}.type
                _allUsers.value = gson.fromJson(json, type)
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
                friendsApi.sendFriendRequest(username)
                _sentRequestUsernames.value = _sentRequestUsernames.value + username
                loadSentFriendRequests()
            } catch (e: Exception) {
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

    fun loadAll() {
        loadFriends()
        loadFriendRequests()
        loadSentFriendRequests()
        loadReceivedRequestUsernames()
    }
}
