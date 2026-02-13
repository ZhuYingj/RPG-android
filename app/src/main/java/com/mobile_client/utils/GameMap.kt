package com.mobile_client.utils

import com.google.gson.annotations.SerializedName

data class GameMap(
    @SerializedName("_id") val _id: String,
    val name: String,
    val description: String,
    val size: TileConstants.MapSize,
    val tiles: List<List<Tile>>,
    val isCaptureTheFlag: Boolean,
    val isVisible: Boolean,
    val lastModified: String
)
