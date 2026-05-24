package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FoodRepository(private val foodDao: FoodDao) {

    val allRestaurants: Flow<List<RestaurantEntity>> = foodDao.getAllRestaurants()
    val allOrders: Flow<List<OrderEntity>> = foodDao.getAllOrders()
    val allDriverPayouts: Flow<List<DriverPayoutEntity>> = foodDao.getAllDriverPayouts()
    val cartItems: Flow<List<CartItemEntity>> = foodDao.getCartItems()

    fun getOrderById(orderId: Int): Flow<OrderEntity?> = foodDao.getOrderById(orderId)
    fun getMenuItemsForRestaurant(restaurantId: String): Flow<List<MenuItemEntity>> = 
        foodDao.getMenuItemsForRestaurant(restaurantId)

    fun getAllMenuItems(): Flow<List<MenuItemEntity>> = foodDao.getAllMenuItems()

    suspend fun populateInitialDataIfEmpty() {
        // Check if restaurants are empty
        val list = foodDao.getAllRestaurants().first()
        if (list.isEmpty()) {
            val restaurants = listOf(
                RestaurantEntity(
                    id = "golden_dragon",
                    name = "Golden Dragon",
                    rating = 4.8,
                    deliveryTimeMins = "25-30",
                    distanceKm = 1.2,
                    offerText = "Flat ₹50 OFF above ₹199",
                    featuredDish = "Kung Pao Chicken - ₹249",
                    emoji = "🏮"
                ),
                RestaurantEntity(
                    id = "burger_paradise",
                    name = "Burger Paradise",
                    rating = 4.6,
                    deliveryTimeMins = "20-25",
                    distanceKm = 0.8,
                    offerText = "20% OFF up to ₹100",
                    featuredDish = "Classic Burger - ₹179",
                    emoji = "🍔"
                ),
                RestaurantEntity(
                    id = "sushi_master",
                    name = "Sushi Master",
                    rating = 4.9,
                    deliveryTimeMins = "30-35",
                    distanceKm = 2.1,
                    offerText = "Free Delivery above ₹499",
                    featuredDish = "Salmon Sushi Roll - ₹399",
                    emoji = "🍣"
                ),
                RestaurantEntity(
                    id = "pizza_corner",
                    name = "Pizza Corner",
                    rating = 4.7,
                    deliveryTimeMins = "15-20",
                    distanceKm = 1.5,
                    offerText = "Flat ₹40 OFF above ₹149",
                    featuredDish = "Margherita Pizza - ₹299",
                    emoji = "🍕"
                )
            )
            foodDao.insertRestaurants(restaurants)

            val menuItems = listOf(
                // Golden Dragon
                MenuItemEntity(
                    id = "gd_chicken_strips",
                    restaurantId = "golden_dragon",
                    name = "Wednesday Chicken Strips Bucket",
                    description = "12 pc Peri Peri chicken strips & 4 delicious dips (20gm each). Highly reordered.",
                    price = 416.0,
                    emoji = "🍗",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = true
                ),
                MenuItemEntity(
                    id = "gd_value_bucket",
                    restaurantId = "golden_dragon",
                    name = "Wednesday Value Special Chicken Bucket",
                    description = "7 Strips, 6 Wings, 2 Hot & Spicy Chicken pieces. Value bucket for 2.",
                    price = 549.0,
                    emoji = "🍗",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = true
                ),
                MenuItemEntity(
                    id = "gd_kung_pao",
                    restaurantId = "golden_dragon",
                    name = "Kung Pao Chicken",
                    description = "Delicious stir-fried chicken with sweet peanuts and dynamic vegetables.",
                    price = 249.0,
                    emoji = "🍛",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "gd_manchurian",
                    restaurantId = "golden_dragon",
                    name = "Manchurian Fried Rice Combo",
                    description = "Perfect combination of spicy chicken/veg manchurian sauce over fragrant fried rice.",
                    price = 199.0,
                    emoji = "🍛",
                    isVeg = true,
                    isNonVeg = false,
                    isSpicy = true
                ),

                // Burger Paradise
                MenuItemEntity(
                    id = "bp_classic_burger",
                    restaurantId = "burger_paradise",
                    name = "Classic Burger",
                    description = "Single grilled succulent crispy chicken patty with fresh lettuce, tomatoes, and mustard house mayo.",
                    price = 179.0,
                    emoji = "🍔",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "bp_cheese_burger",
                    restaurantId = "burger_paradise",
                    name = "Double Cheese Crunch Burger",
                    description = "Spicily seasoned dual premium crispy vegetable patties sandwiching extra cheddar slices.",
                    price = 229.0,
                    emoji = "🍔",
                    isVeg = true,
                    isNonVeg = false,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "bp_fries",
                    restaurantId = "burger_paradise",
                    name = "Spicy Cajun Salted Fries",
                    description = "Crispy potato matchsticks double-fried and shaken with smoky peri peri seasonings.",
                    price = 99.0,
                    emoji = "🍟",
                    isVeg = true,
                    isNonVeg = false,
                    isSpicy = true
                ),

                // Sushi Master
                MenuItemEntity(
                    id = "sm_salmon_roll",
                    restaurantId = "sushi_master",
                    name = "Salmon Sushi Roll",
                    description = "Fattiest dynamic Atlantic salmon cubes, cucumber sticks, and dollop of wasabi nori rice.",
                    price = 399.0,
                    emoji = "🍣",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "sm_california_roll",
                    restaurantId = "sushi_master",
                    name = "California Gold Sushi",
                    description = "Sweet crab meat sticks, sliced avocados, and creamy mayo toppings dressed inside katsu sesame.",
                    price = 299.0,
                    emoji = "🍣",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "sm_tempura",
                    restaurantId = "sushi_master",
                    name = "Spicy Tiger Prawn Tempura",
                    description = "Golden crisp batter-dipped tiger prawns served with sweet mirin dipping elements.",
                    price = 349.0,
                    emoji = "🍤",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = true
                ),

                // Pizza Corner
                MenuItemEntity(
                    id = "pc_margherita",
                    restaurantId = "pizza_corner",
                    name = "Classic Margherita Pizza",
                    description = "Freshly pressed herb dough topped with heavy organic Italian pizza sauce and premium pulled mozzarella.",
                    price = 299.0,
                    emoji = "🍕",
                    isVeg = true,
                    isNonVeg = false,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "pc_farmhouse",
                    restaurantId = "pizza_corner",
                    name = "Deconstructed Farmhouse Pizza",
                    description = "Heavy servings of farm-fresh capsicum, baby corn, sweet sliced onions, and earthy button mushrooms.",
                    price = 379.0,
                    emoji = "🍕",
                    isVeg = true,
                    isNonVeg = false,
                    isSpicy = false
                ),
                MenuItemEntity(
                    id = "pc_chicken_delight",
                    restaurantId = "pizza_corner",
                    name = "Chicken Delight Feast Pizza",
                    description = "Spicy chicken tikka slices, juicy red bell pepper strips, and seasoned cilantro seasonings.",
                    price = 449.0,
                    emoji = "🍕",
                    isVeg = false,
                    isNonVeg = true,
                    isSpicy = true
                )
            )
            foodDao.insertMenuItems(menuItems)
        }
    }

    suspend fun insertCartItem(itemId: String, name: String, price: Double, itemEmoji: String, restaurantId: String) {
        val currentCart = foodDao.getCartItems().first()
        // Simple rule: clear cart if adding from a different restaurant to mimic real food delivery apps
        if (currentCart.isNotEmpty() && currentCart.first().restaurantId != restaurantId) {
            foodDao.clearCart()
        }
        val existing = currentCart.find { it.itemId == itemId }
        if (existing != null) {
            foodDao.updateCartQuantity(itemId, existing.quantity + 1)
        } else {
            foodDao.insertCartItem(CartItemEntity(itemId, restaurantId, name, price, 1, itemEmoji))
        }
    }

    suspend fun incrementCartItem(itemId: String) {
        val currentCart = foodDao.getCartItems().first()
        val existing = currentCart.find { it.itemId == itemId }
        if (existing != null) {
            foodDao.updateCartQuantity(itemId, existing.quantity + 1)
        }
    }

    suspend fun decrementCartItem(itemId: String) {
        val currentCart = foodDao.getCartItems().first()
        val existing = currentCart.find { it.itemId == itemId } ?: return
        if (existing.quantity > 1) {
            foodDao.updateCartQuantity(itemId, existing.quantity - 1)
        } else {
            foodDao.deleteCartItem(existing)
        }
    }

    suspend fun removeCartItem(itemId: String) {
        val currentCart = foodDao.getCartItems().first()
        val existing = currentCart.find { it.itemId == itemId }
        if (existing != null) {
            foodDao.deleteCartItem(existing)
        }
    }

    suspend fun clearCart() = foodDao.clearCart()

    suspend fun placeOrder(
        restaurantId: String,
        restaurantName: String,
        itemsSummary: String,
        totalAmount: Double,
        couponApplied: String?,
        discountAmount: Double,
        paymentMethod: String,
        address: String
    ): Long {
        val order = OrderEntity(
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            itemsSummary = itemsSummary,
            totalAmount = totalAmount,
            couponApplied = couponApplied,
            discountAmount = discountAmount,
            status = "PREPARING",
            paymentMethod = paymentMethod,
            address = address
        )
        val orderId = foodDao.insertOrder(order)
        foodDao.clearCart()
        return orderId
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        foodDao.updateOrderStatus(orderId, status)
    }

    suspend fun assignDriverToOrder(orderId: Int, driverId: String, earnings: Double) {
        foodDao.assignOrderDriver(orderId, "OUT_FOR_DELIVERY", driverId, earnings)
    }

    suspend fun updateMenuItemStock(itemId: String, inStock: Boolean) {
        foodDao.updateMenuItemStock(itemId, inStock)
    }

    suspend fun completeDelivery(orderId: Int, payout: DriverPayoutEntity) {
        foodDao.updateOrderStatus(orderId, "DELIVERED")
        foodDao.insertDriverPayout(payout)
    }
}
