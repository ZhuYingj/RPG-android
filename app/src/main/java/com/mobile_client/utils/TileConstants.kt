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
        Ice,
        @SerializedName("1")
        Normal,
        @SerializedName("2")
        Water,
        @SerializedName("3")
        Wall,
        @SerializedName("4")
        OpenDoor,
        @SerializedName("5")
        ClosedDoor,
        @SerializedName("6")
        OpenAutoDoor,
        @SerializedName("7")
        ClosedAutoDoor,
        @SerializedName("8")
        Bush,
        @SerializedName("9")
        Flower,
        @SerializedName("20") //new type
        OpenedBush
    }

    enum class Items {
        @SerializedName("10")
        None,
        @SerializedName("11")
        Object1,
        @SerializedName("12")
        Object2,
        @SerializedName("13")
        Object3,
        @SerializedName("14") // was 10
        Object4,
        @SerializedName("15")
        Object5,
        @SerializedName("16")
        Object6,
        @SerializedName("17")
        ObjectRandom,
        @SerializedName("18")
        Spawn,
        @SerializedName("19")
        Flag
    }
}
