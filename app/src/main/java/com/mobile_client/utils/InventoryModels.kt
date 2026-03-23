package com.mobile_client.utils

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
