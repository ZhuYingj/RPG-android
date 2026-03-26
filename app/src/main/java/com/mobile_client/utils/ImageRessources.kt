package com.mobile_client.utils

import com.mobile_client.screens.R

object ImageResources {
    val tileTypeToImage = mapOf(
        TileConstants.Types.Ice to R.drawable.glace,
        TileConstants.Types.Normal to R.drawable.grass,
        TileConstants.Types.Water to R.drawable.eau,
        TileConstants.Types.Wall to R.drawable.mur,
        TileConstants.Types.OpenDoor to R.drawable.porte_ouverte,
        TileConstants.Types.ClosedDoor to R.drawable.porte,
        TileConstants.Types.OpenAutoDoor to R.drawable.open_autodoor,
        TileConstants.Types.ClosedAutoDoor to R.drawable.closed_autodoor,
        TileConstants.Types.Bush to R.drawable.bush_close,
        TileConstants.Types.OpenedBush to R.drawable.bush_open,
        TileConstants.Types.Flower to R.drawable.hp_flower,
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

    val sizeToPlayerNumber = mapOf(
        TileConstants.MapSize.Small to 2,
        TileConstants.MapSize.Medium to 4,
        TileConstants.MapSize.Large to 6
    )

    val avatarToImage = mapOf(
        PlayerAvatars.Carrot to R.drawable.avatar1,
        PlayerAvatars.Cabbage to R.drawable.avatar2,
        PlayerAvatars.Pepper to R.drawable.avatar3,
        PlayerAvatars.Broccoli to R.drawable.avatar4,
        PlayerAvatars.Corn to R.drawable.avatar5,
        PlayerAvatars.Beet to R.drawable.avatar6,
        PlayerAvatars.Potato to R.drawable.avatar7,
        PlayerAvatars.Mushroom to R.drawable.avatar8,
        PlayerAvatars.Onion to R.drawable.avatar9,
        PlayerAvatars.Cauliflower to R.drawable.avatar10,
        PlayerAvatars.Celery to R.drawable.avatar11,
        PlayerAvatars.Tomato to R.drawable.avatar12,
        PlayerAvatars.Pumpkin to R.drawable.avatar13,
        PlayerAvatars.Asparagus to R.drawable.avatar14,
        PlayerAvatars.Eggplant to R.drawable.avatar15,
        PlayerAvatars.Avocado to R.drawable.avatar16,
    )

    val cosmeticToImage = mapOf(
        "cosmetics/hats/wizard.png" to R.drawable.wizard,
        "cosmetics/hats/baseball_cap.png" to R.drawable.baseball_cap,
        "cosmetics/hats/cowboyhat.png" to R.drawable.cowboyhat,
        "cosmetics/hats/tuque.png" to R.drawable.tuque,
        "cosmetics/weapons/pitchfork.png" to R.drawable.pitchfork,
        "cosmetics/weapons/hoe.png" to R.drawable.hoe,
        "cosmetics/weapons/spade.png" to R.drawable.spade,
        "cosmetics/weapons/sickle.png" to R.drawable.sickle,
        "avatars/avatar13.png" to R.drawable.avatar13,
        "avatars/avatar14.png" to R.drawable.avatar14,
        "avatars/avatar15.png" to R.drawable.avatar15,
        "avatars/avatar16.png" to R.drawable.avatar16,
    )

    val tutorialStepToImage = mapOf(
        0 to R.drawable.leaderboard_background, //TODO add tutorial steps
        1 to R.drawable.step1,
        2 to R.drawable.step2,
    )
}
