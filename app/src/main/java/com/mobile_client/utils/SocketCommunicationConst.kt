package com.mobile_client.utils

class SocketCommunicationConst {
    data class SendableLobbies(
        val map: Any,
        val code: String,
        val fee: Int,
        val host: String,
        val hasFriend: Boolean,
        val hasBlockedUser: Boolean,
        val playerNumber: Int,
        val maxPlayers: Int,
        val isLocked: Boolean,
        val isGameStarted: Boolean,
    )
}
