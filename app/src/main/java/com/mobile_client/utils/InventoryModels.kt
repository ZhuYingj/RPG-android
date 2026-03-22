package com.mobile_client.utils

enum class CosmeticType(val value: Int) {
    Consumable(0),
    Avatar(1),
    Theme(2);

    companion object {
        fun fromValue(value: Int): CosmeticType =
            entries.firstOrNull { it.value == value } ?: Consumable
    }
}

data class Cosmetic(
    val _id: String,
    val name: String,
    val description: String,
    val type: Int,
    val filePath: String,
    val price: Int = 0
)

data class InventoryCosmetic(
    val cosmeticId: String,
    val quantity: Int
)

data class InventoryDTO(
    val inventory: List<InventoryCosmetic>,
    val equipped: List<Cosmetic>
)

data class SendGift(
    val username: String,
    val cosmeticId: String
)

data class Gift(
    val username: String,
    val avatar: String,
    val cosmeticId: String,
    val id: String
)

data class GiftResponse(
    val id: String,
    val isAccepted: Boolean
)
