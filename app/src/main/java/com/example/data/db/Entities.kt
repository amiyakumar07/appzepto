package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val deliveryTimeMins: String,
    val distanceKm: Double,
    val offerText: String,
    val featuredDish: String,
    val emoji: String
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val emoji: String,
    val isVeg: Boolean,
    val isNonVeg: Boolean,
    val isSpicy: Boolean,
    val inStock: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val itemId: String,
    val restaurantId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val emoji: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: String,
    val restaurantName: String,
    val itemsSummary: String, // e.g. "Burger x1, Fries x2"
    val totalAmount: Double,
    val couponApplied: String?,
    val discountAmount: Double,
    val status: String, // "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED"
    val paymentMethod: String,
    val createdAt: Long = System.currentTimeMillis(),
    val address: String,
    val driverId: String? = null,
    val driverEarnings: Double = 0.0
)

@Entity(tableName = "driver_payouts")
data class DriverPayoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val tripPay: Double,
    val bonusPay: Double,
    val distanceKm: Double,
    val durationMins: Int,
    val timestamp: Long = System.currentTimeMillis()
)
