package com.mobile_client.utils

data class Tile(
    val type: TileConstants.Types,
    val item: TileConstants.Items = TileConstants.Items.None
)
