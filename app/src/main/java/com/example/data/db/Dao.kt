package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // Restaurants
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    // Menu items
    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<MenuItemEntity>)

    @Query("UPDATE menu_items SET inStock = :inStock WHERE id = :itemId")
    suspend fun updateMenuItemStock(itemId: String, inStock: Boolean)

    // Cart Items
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("UPDATE cart_items SET quantity = :quantity WHERE itemId = :itemId")
    suspend fun updateCartQuantity(itemId: String, quantity: Int)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Int): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Query("UPDATE orders SET status = :status, driverId = :driverId, driverEarnings = :earnings WHERE id = :orderId")
    suspend fun assignOrderDriver(orderId: Int, status: String, driverId: String, earnings: Double)

    // Driver Payouts
    @Query("SELECT * FROM driver_payouts ORDER BY timestamp DESC")
    fun getAllDriverPayouts(): Flow<List<DriverPayoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriverPayout(payout: DriverPayoutEntity)
}
