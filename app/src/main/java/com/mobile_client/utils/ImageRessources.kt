package com.mobile_client.utils

import com.mobile_client.screens.R

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

    val avatarToImage = mapOf(
        PlayerAvatars.Carrot to R.drawable.avatar1,
        PlayerAvatars.Cabbage to R.drawable.avatar2,
        PlayerAvatars.Pepper to R.drawable.avatar3,
        PlayerAvatars.Brocoli to R.drawable.avatar4,
        PlayerAvatars.Corn to R.drawable.avatar5,
        PlayerAvatars.Beet to R.drawable.avatar6,
        PlayerAvatars.Potato to R.drawable.avatar7,
        PlayerAvatars.Mushroom to R.drawable.avatar8,
        PlayerAvatars.Onion to R.drawable.avatar9,
        PlayerAvatars.Cauliflower to R.drawable.avatar10,
        PlayerAvatars.Celery to R.drawable.avatar11,
        PlayerAvatars.Tomato to R.drawable.avatar12,
    )
}
