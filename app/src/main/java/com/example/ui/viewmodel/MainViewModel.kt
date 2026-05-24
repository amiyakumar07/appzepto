package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class CustomerScreen {
    object Home : CustomerScreen()
    data class RestaurantDetails(val restaurantId: String) : CustomerScreen()
    object Cart : CustomerScreen()
    data class OrderTracking(val orderId: Int) : CustomerScreen()
}

enum class AppRole {
    CUSTOMER, RESTAURANT, DELIVERY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FoodRepository(database.foodDao())

    // App Navigation Roles
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Customer Navigation Screens
    private val _customerScreen = MutableStateFlow<CustomerScreen>(CustomerScreen.Home)
    val customerScreen: StateFlow<CustomerScreen> = _customerScreen.asStateFlow()

    // Restaurant view state
    private val _restaurantTab = MutableStateFlow("Preparing") // Preparing, Ready, Out for delivery
    val restaurantTab: StateFlow<String> = _restaurantTab.asStateFlow()

    private val _restaurantSubTab = MutableStateFlow("Orders") // Orders, Menu/Inventory
    val restaurantSubTab: StateFlow<String> = _restaurantSubTab.asStateFlow()

    // Delivery boy state
    private val _deliveryOnline = MutableStateFlow(true)
    val deliveryOnline: StateFlow<Boolean> = _deliveryOnline.asStateFlow()

    private val _deliverySubTab = MutableStateFlow("Feed") // Feed, Pocket
    val deliverySubTab: StateFlow<String> = _deliverySubTab.asStateFlow()

    // Searches & Filters for Customer
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _vegOnly = MutableStateFlow(false)
    val vegOnly: StateFlow<Boolean> = _vegOnly.asStateFlow()

    // Active coupon code
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    // Helper filter chips
    private val _selectedTimeFilter = MutableStateFlow<String?>(null)
    val selectedTimeFilter: StateFlow<String?> = _selectedTimeFilter.asStateFlow()

    fun applyCoupon(code: String?) {
        _appliedCoupon.value = code
    }

    fun setTimeFilter(filter: String?) {
        if (_selectedTimeFilter.value == filter) {
            _selectedTimeFilter.value = null
        } else {
            _selectedTimeFilter.value = filter
        }
    }

    // Active Order ID to open tracking directly
    private val _activeOrderId = MutableStateFlow<Int?>(null)
    val activeOrderId: StateFlow<Int?> = _activeOrderId.asStateFlow()

    // Observables from repo
    val allRestaurants: StateFlow<List<RestaurantEntity>> = repository.allRestaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDriverPayouts: StateFlow<List<DriverPayoutEntity>> = repository.allDriverPayouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMenuItems: StateFlow<List<MenuItemEntity>> = repository.getAllMenuItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun navigateCustomer(screen: CustomerScreen) {
        _customerScreen.value = screen
    }

    fun setRestaurantTab(tab: String) {
        _restaurantTab.value = tab
    }

    fun setRestaurantSubTab(subTab: String) {
        _restaurantSubTab.value = subTab
    }

    fun setDeliveryOnline(online: Boolean) {
        _deliveryOnline.value = online
    }

    fun setDeliverySubTab(tab: String) {
        _deliverySubTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setVegOnly(vegOnly: Boolean) {
        _vegOnly.value = vegOnly
    }

    // Cart Operations
    fun addToCart(item: MenuItemEntity, restaurantId: String) {
        viewModelScope.launch {
            repository.insertCartItem(item.id, item.name, item.price, item.emoji, restaurantId)
        }
    }

    fun incrementCart(itemId: String) {
        viewModelScope.launch {
            repository.incrementCartItem(itemId)
        }
    }

    fun decrementCart(itemId: String) {
        viewModelScope.launch {
            repository.decrementCartItem(itemId)
        }
    }

    fun removeCart(itemId: String) {
        viewModelScope.launch {
            repository.removeCartItem(itemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Order Placement
    fun checkoutAndPlaceOrder(
        restaurantId: String,
        restaurantName: String,
        paymentMethod: String,
        address: String,
        subTotal: Double,
        couponCode: String?,
        discount: Double,
        onSuccess: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val summary = cartItems.value.joinToString { "${it.name} x${it.quantity}" }
            val total = subTotal - discount
            val orderId = repository.placeOrder(
                restaurantId = restaurantId,
                restaurantName = restaurantName,
                itemsSummary = summary,
                totalAmount = total,
                couponApplied = couponCode,
                discountAmount = discount,
                paymentMethod = paymentMethod,
                address = address
            )
            _appliedCoupon.value = null
            _activeOrderId.value = orderId.toInt()
            onSuccess(orderId.toInt())
        }
    }

    // Restaurant actions
    fun markOrderReady(orderId: Int) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "READY")
        }
    }

    fun toggleMenuItemStock(itemId: String, inStock: Boolean) {
        viewModelScope.launch {
            repository.updateMenuItemStock(itemId, inStock)
        }
    }

    // Driver actions
    fun acceptOrder(orderId: Int) {
        viewModelScope.launch {
            repository.assignDriverToOrder(orderId, "APP_BOY_1", 111.94)
        }
    }

    fun updateProgress(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    fun completeDelivery(orderId: Int, tripPay: Double, bonusPay: Double, distance: Double, duration: Int) {
        viewModelScope.launch {
            val payout = DriverPayoutEntity(
                orderId = orderId,
                tripPay = tripPay,
                bonusPay = bonusPay,
                distanceKm = distance,
                durationMins = duration
            )
            repository.completeDelivery(orderId, payout)
        }
    }
}
