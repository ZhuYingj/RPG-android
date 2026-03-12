package com.mobile_client.utils

data class FriendRequest(
    val id: String,
    val username: String,
    val avatar: String
)

data class SearchableUser(
    val username: String,
    val avatar: String
)

enum class FriendsTab(val label: String) {
    FRIENDS("Amis"),
    REQUESTS("Requêtes"),
    SENT("Envoyé")
}
