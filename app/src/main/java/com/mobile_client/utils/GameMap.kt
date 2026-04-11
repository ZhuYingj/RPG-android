package com.mobile_client.utils

import com.google.gson.annotations.SerializedName

data class GameMap(
    @SerializedName("_id") val _id: String,
    val name: String,
    val description: String,
    val size: TileConstants.MapSize,
    val tiles: List<List<Tile>>,
    val isCaptureTheFlag: Boolean,
    val property: GameProperty,
    val owner: String,
    val lastModified: String,
    val actionNumber: Int,
    val isRapidElimination: Boolean
)

enum class GameProperty {
    @SerializedName("public")
    Public,
    @SerializedName("private")
    Private,
    @SerializedName("private-shared")
    PrivateShared
}
