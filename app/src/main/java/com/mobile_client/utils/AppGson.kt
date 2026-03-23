package com.mobile_client.utils

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.gson.JsonDeserializer

private val booleanAdapter = object : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        out.value(value)
    }
    override fun read(`in`: JsonReader): Boolean {
        return when (`in`.peek()) {
            com.google.gson.stream.JsonToken.BOOLEAN -> `in`.nextBoolean()
            com.google.gson.stream.JsonToken.NUMBER -> `in`.nextInt() != 0
            else -> {
                `in`.skipValue()
                false
            }
        }
    }
}

val AppGson = GsonBuilder()
    .registerTypeAdapter(PlayerTypes::class.java, object : TypeAdapter<PlayerTypes>() {
        override fun write(out: JsonWriter, value: PlayerTypes?) {
            out.value(value?.ordinal)
        }
        override fun read(`in`: JsonReader) = PlayerTypes.entries[`in`.nextInt()]
    })
    .registerTypeAdapter(Dices::class.java, object : TypeAdapter<Dices>() {
        override fun write(out: JsonWriter, value: Dices?) {
            out.value(value?.value)
        }
        override fun read(`in`: JsonReader): Dices {
            val v = `in`.nextInt()
            return Dices.entries.first { it.value == v }
        }
    })
    .registerTypeAdapter(TileConstants.MapSize::class.java, object : TypeAdapter<TileConstants.MapSize>() {
        override fun write(out: JsonWriter, value: TileConstants.MapSize?) {
            out.value(when (value) {
                TileConstants.MapSize.Small -> 10
                TileConstants.MapSize.Medium -> 15
                TileConstants.MapSize.Large -> 20
                null -> null
            })
        }
        override fun read(`in`: JsonReader): TileConstants.MapSize {
            return when (`in`.nextInt()) {
                10 -> TileConstants.MapSize.Small
                15 -> TileConstants.MapSize.Medium
                20 -> TileConstants.MapSize.Large
                else -> TileConstants.MapSize.Small
            }
        }
    })
    .registerTypeAdapter(TileConstants.Types::class.java, object : TypeAdapter<TileConstants.Types>() {
        override fun write(out: JsonWriter, value: TileConstants.Types?) {
            out.value(value?.ordinal)
        }
        override fun read(`in`: JsonReader) = TileConstants.Types.entries[`in`.nextInt()]
    })
    .registerTypeAdapter(TileConstants.Items::class.java, object : TypeAdapter<TileConstants.Items>() {
        private val valueMap = mapOf(
            10 to TileConstants.Items.None,
            11 to TileConstants.Items.Object1,
            12 to TileConstants.Items.Object2,
            13 to TileConstants.Items.Object3,
            14 to TileConstants.Items.Object4,
            15 to TileConstants.Items.Object5,
            16 to TileConstants.Items.Object6,
            17 to TileConstants.Items.ObjectRandom,
            18 to TileConstants.Items.Spawn,
            19 to TileConstants.Items.Flag
        )
        private val writeMap = valueMap.entries.associate { (k, v) -> v to k }

        override fun write(out: JsonWriter, value: TileConstants.Items?) {
            out.value(writeMap[value])
        }
        override fun read(`in`: JsonReader): TileConstants.Items {
            return valueMap[`in`.nextInt()] ?: TileConstants.Items.None
        }
    })
    .registerTypeAdapter(UserAction::class.java, object : TypeAdapter<UserAction>() {
        override fun write(out: JsonWriter, value: UserAction?) {
            out.value(value?.label)
        }
        override fun read(`in`: JsonReader): UserAction {
            val raw = `in`.nextString()
            return UserAction.entries.firstOrNull { it.label == raw }
                ?: UserAction.Connection
        }
    })
    .registerTypeAdapter(GameTypes::class.java, object : TypeAdapter<GameTypes>() {
        override fun write(out: JsonWriter, value: GameTypes?) {
            out.value(value?.name)
        }
        override fun read(`in`: JsonReader): GameTypes {
            val raw = `in`.nextString()
            return GameTypes.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
                ?: GameTypes.Classic
        }
    })
    .registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ ->
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(json.asString)
        } catch (e: Exception) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(json.asString)
            } catch (e2: Exception) {
                Date()
            }
        }
    })
    .registerTypeAdapter(Boolean::class.java, booleanAdapter)
    .registerTypeAdapter(java.lang.Boolean::class.java, booleanAdapter)
    .create()
