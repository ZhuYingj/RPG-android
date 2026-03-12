package com.mobile_client.utils

import com.google.gson.annotations.SerializedName

object TileConstants {
    enum class MapSize {
        Small,
        Medium,
        Large
    }

    enum class Types {
        @SerializedName("0")
        Ice,           // Changed order!
        @SerializedName("1")
        Normal,
        @SerializedName("2")
        Water,
        @SerializedName("3")
        Wall,
        @SerializedName("4")
        OpenDoor,
        @SerializedName("5")
        ClosedDoor
    }

    enum class Items {
        @SerializedName("6")
        None,
        @SerializedName("7")
        Object1,
        @SerializedName("8")
        Object2,
        @SerializedName("9")
        Object3,
        @SerializedName("10")
        Object4,
        @SerializedName("11")
        Object5,
        @SerializedName("12")
        Object6,
        @SerializedName("13")
        ObjectRandom,
        @SerializedName("14")
        Spawn,
        @SerializedName("16")   // Note: skips 15?
        Flag
    }
}
