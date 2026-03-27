package com.mobile_client.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile_client.services.LeaderBoardService
import com.mobile_client.services.SocketService
import com.mobile_client.utils.LeaderboardEntry
import com.mobile_client.utils.LeaderboardEvents
import com.mobile_client.utils.LeaderboardFilterType
import com.mobile_client.utils.LeaderboardSortType
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val service = LeaderBoardService.instance
    var sortType = mutableStateOf(LeaderboardSortType.WINS)
    var filterType = mutableStateOf(LeaderboardFilterType.GLOBAL)
    var searchQuery = mutableStateOf("")
    var entries = mutableStateOf<List<LeaderboardEntry>>(emptyList())
    var isLoading = mutableStateOf(false)

    fun initializeSocketListeners() {
        val socket = SocketService.instance.socket ?: return
        socket.off(LeaderboardEvents.UPDATE_LEADERBOARD)
        socket.on(LeaderboardEvents.UPDATE_LEADERBOARD) {
            fetchLeaderboard()
        }
    }

    fun removeSocketListeners() {
        val socket = SocketService.instance.socket ?: return
        socket.off(LeaderboardEvents.UPDATE_LEADERBOARD)
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            isLoading.value = true
            entries.value = when (sortType.value) {
                LeaderboardSortType.WINS -> service.getGamesWonLeaderboard()
                LeaderboardSortType.MONEY -> service.getMoneyLeaderboard()
                LeaderboardSortType.PLAYTIME -> service.getPlayTimeLeaderboard()
                LeaderboardSortType.BATTLES_WINS -> service.getBattlesWin()
            }
            isLoading.value = false
        }
    }

    fun getDisplayValue(entry: LeaderboardEntry): String {
        return when (sortType.value) {
            LeaderboardSortType.WINS -> "${entry.stats?.gamesWon ?: 0}"
            LeaderboardSortType.MONEY -> "${entry.money}$"
            LeaderboardSortType.PLAYTIME -> {
                val stats = entry.stats ?: return "0s"
                val totalSeconds = ((stats.CTFGamesPlayed + stats.classicGamesPlayed) * stats.averageGameTime).toLong()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                "${minutes}m ${seconds}s"
            }
            LeaderboardSortType.BATTLES_WINS -> "${entry.stats?.battlesWins ?: 0}"
        }
    }
}
