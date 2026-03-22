package com.mobile_client.services

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.mobile_client.environment.ENVIRONMENT
import com.mobile_client.utils.AppGson
import com.mobile_client.utils.Cosmetic
import com.mobile_client.utils.InventoryDTO
import io.ktor.client.call.body

class CosmeticService private constructor() {
    companion object {
        val instance: CosmeticService by lazy { CosmeticService() }
    }

    private val http = HttpService.instance
    private val gson = AppGson

    private var allCosmetics: MutableMap<String, Cosmetic> = mutableMapOf()

    fun getCosmetic(cosmeticId: String): Cosmetic? {
        return allCosmetics[cosmeticId]
    }

    suspend fun loadShop(): List<Cosmetic> {
        return try {
            val json = http.get("$ENVIRONMENT/api/shop/")
            val type = object : TypeToken<List<Cosmetic>>() {}.type
            val cosmetics: List<Cosmetic> = gson.fromJson(json, type)
            cosmetics.forEach { allCosmetics[it._id] = it }
            Log.d("InventoryService", "Shop loaded: ${cosmetics.size} cosmetics")
            cosmetics
        } catch (e: Exception) {
            Log.e("InventoryService", "Failed to load shop", e)
            emptyList()
        }
    }

    suspend fun loadInventory(): InventoryDTO {
        return try {
            val json = http.get("$ENVIRONMENT/api/inventory/")
            val dto: InventoryDTO = gson.fromJson(json, InventoryDTO::class.java)
            dto.equipped.forEach { allCosmetics[it._id] = it }
            dto
        } catch (e: Exception) {
            Log.e("InventoryService", "Failed to load inventory", e)
            InventoryDTO(inventory = emptyList(), equipped = emptyList())
        }
    }

    suspend fun equipItem(cosmeticId: String): InventoryDTO? {
        return try {
            val response = http.post("$ENVIRONMENT/api/inventory/$cosmeticId", emptyMap<String, String>())
            if (response.status.value in 200..299) {
                val json: String = response.body()
                val dto: InventoryDTO = gson.fromJson(json, InventoryDTO::class.java)
                dto.equipped.forEach { allCosmetics[it._id] = it }
                dto
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("InventoryService", "Failed to equip item", e)
            null
        }
    }

    suspend fun buyItem(cosmeticId: String): Boolean {
        return try {
            val response = http.post("$ENVIRONMENT/api/shop/$cosmeticId", emptyMap<String, String>())
            Log.d("InventoryService", "buyItem response status: ${response.status}")
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.e("InventoryService", "Failed to buy item", e)
            false
        }
    }

    suspend fun giftItem(cosmeticId: String, targetUserId: String): Boolean {
        return try {
            val response = http.post(
                "$ENVIRONMENT/api/inventory/gift",
                mapOf("cosmeticId" to cosmeticId, "targetUserId" to targetUserId)
            )
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.e("InventoryService", "Failed to gift item", e)
            false
        }
    }
}
