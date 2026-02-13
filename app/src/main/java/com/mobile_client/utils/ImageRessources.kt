package com.mobile_client.utils

import com.mobile_client.pages.R

object ImageResources {
    val tileTypeToImage = mapOf(
        TileConstants.Types.Ice to R.drawable.glace,
        TileConstants.Types.Normal to R.drawable.grass,
        TileConstants.Types.Water to R.drawable.eau,
        TileConstants.Types.Wall to R.drawable.mur,
        TileConstants.Types.OpenDoor to R.drawable.porte_ouverte,
        TileConstants.Types.ClosedDoor to R.drawable.porte
    )

    val itemToImage = mapOf(
        TileConstants.Items.None to null,
        TileConstants.Items.Object1 to R.drawable.objet1,
        TileConstants.Items.Object2 to R.drawable.objet2,
        TileConstants.Items.Object3 to R.drawable.objet3,
        TileConstants.Items.Object4 to R.drawable.objet4,
        TileConstants.Items.Object5 to R.drawable.objet5,
        TileConstants.Items.Object6 to R.drawable.objet6,
        TileConstants.Items.ObjectRandom to R.drawable.random,
        TileConstants.Items.Spawn to R.drawable.spawn,
        TileConstants.Items.Flag to R.drawable.flag
    )

    val sizeString = mapOf(
        TileConstants.MapSize.Small to "Petite",
        TileConstants.MapSize.Medium to "Moyenne",
        TileConstants.MapSize.Large to "Grande"
    )
}
