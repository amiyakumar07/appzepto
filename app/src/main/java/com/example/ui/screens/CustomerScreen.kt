package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.*
import com.example.ui.viewmodel.CustomerScreen
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

// App Brand Colors
val ZomatoRed = Color(0xFFEF4F56)
val ZomatoDark = Color(0xFF1C1C1C)
val ZomatoGreen = Color(0xFF1C8A43)
val ZomatoGold = Color(0xFFF4C430)
val LightGray = Color(0xFFF4F4F6)

@Composable
fun CustomerScreenContent(viewModel: MainViewModel) {
    val screenState by viewModel.customerScreen.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "customer_screens"
        ) { state ->
            when (state) {
                is CustomerScreen.Home -> CustomerHomePage(viewModel)
                is CustomerScreen.RestaurantDetails -> RestaurantDetailsPage(viewModel, state.restaurantId)
                is CustomerScreen.Cart -> CartPage(viewModel)
                is CustomerScreen.OrderTracking -> OrderTrackingPage(viewModel, state.orderId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomePage(viewModel: MainViewModel) {
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val vegOnly by viewModel.vegOnly.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val timeFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()

    val totalItems = cart.sumOf { it.quantity }
    val totalPrice = cart.sumOf { it.price * it.quantity }

    // Carousel banner logic
    val banners = listOf(
        Pair("Get 50% OFF & FREE delivery", "on your first order under 7 km"),
        Pair("Exclusive Deal! enjoy FREE shipping", "on all orders over ₹499"),
        Pair("Special Offer! Get Express Delivery", "with every premium order this week")
    )
    var currentBannerIdx by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentBannerIdx = (currentBannerIdx + 1) % banners.size
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = ZomatoRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Madhya Pradesh",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ZomatoDark
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Select",
                                    tint = ZomatoDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Indore, bypass lane · Current Location",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Role switch indicator handled in MainActivity, but we can display simple logo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ZomatoRed.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Appzeto",
                            color = ZomatoRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar + Veg Toggle row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search dishes, cuisines, pizza...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ZomatoRed
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = LightGray,
                            unfocusedContainerColor = LightGray,
                            disabledContainerColor = LightGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .testTag("search_search_field")
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Veg toggle
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VEG ONLY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vegOnly) ZomatoGreen else Color.Gray
                        )
                        Switch(
                            checked = vegOnly,
                            onCheckedChange = { viewModel.setVegOnly(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ZomatoGreen,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = LightGray
                            ),
                            modifier = Modifier
                                .scale(0.8f)
                                .testTag("veg_only_switch")
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Floating View Cart bar
            if (totalItems > 0) {
                Surface(
                    tonalElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZomatoRed)
                            .clickable { viewModel.navigateCustomer(CustomerScreen.Cart) }
                            .padding(14.dp)
                            .testTag("floating_cart_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$totalItems ITEMS",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${"%.0f".format(totalPrice)} plus taxes",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "View Cart",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFC))
                .padding(innerPadding)
        ) {
            // Promo Banner Carousel
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFFFF3E0))))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(0.6f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ZomatoRed)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "30% LESS",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = banners[currentBannerIdx].first,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ZomatoDark,
                                lineHeight = 22.sp
                            )
                            Text(
                                text = banners[currentBannerIdx].second,
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = ZomatoDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Know more", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Banner Illustration (Visual placeholder)
                        Column(
                            modifier = Modifier.weight(0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🏍️💨",
                                fontSize = 54.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Categories horizontal list
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "WHAT'S ON YOUR MIND?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val categories = listOf(
                        Pair("Biryani", "🍛"),
                        Pair("Cake", "🧁"),
                        Pair("Chole", "🍲"),
                        Pair("Chicken", "🍗"),
                        Pair("Donut", "🍩"),
                        Pair("Pizza", "🍕"),
                        Pair("Sushi", "🍣"),
                        Pair("Dosa", "🥞")
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(categories) { cat ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { viewModel.setSearchQuery(cat.first) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, LightGray, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = cat.second, fontSize = 32.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.first,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ZomatoDark
                                )
                            }
                        }
                    }
                }
            }

            // Filter Row
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("Filters") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = timeFilter == "30",
                            onClick = { viewModel.setTimeFilter(if (timeFilter == "30") null else "30") },
                            label = { Text("Under 30 mins") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = timeFilter == "45",
                            onClick = { viewModel.setTimeFilter(if (timeFilter == "45") null else "45") },
                            label = { Text("Under 45 mins") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("Under 1km") }
                        )
                    }
                }
            }

            // Explore More grids
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "EXPLORE MORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExploreItemCard(
                            title = "Offers",
                            desc = "Flat discounts",
                            emoji = "🏷️",
                            colorBg = Color(0xFFE8F5E9),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setSearchQuery("OFF") }
                        )
                        ExploreItemCard(
                            title = "Gourmet",
                            desc = "Fine dining",
                            emoji = "✨",
                            colorBg = Color(0xFFFFF3E0),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { }
                        )
                        ExploreItemCard(
                            title = "Top 10",
                            desc = "Highest rated",
                            emoji = "🏆",
                            colorBg = Color(0xFFFFFDE7),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setSearchQuery("4.") }
                        )
                        ExploreItemCard(
                            title = "Collections",
                            desc = "Curated lists",
                            emoji = "🔖",
                            colorBg = Color(0xFFEDE7F6),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { }
                        )
                    }
                }
            }

            // Restaurants header
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "RESTAURANTS DELIVERING TO YOU",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Dynamic restaurants matching filters and searches
            val filteredRestaurants = restaurants.filter { rest ->
                val matchesSearch = searchQuery.isEmpty() || 
                        rest.name.contains(searchQuery, ignoreCase = true) || 
                        rest.featuredDish.contains(searchQuery, ignoreCase = true) ||
                        rest.offerText.contains(searchQuery, ignoreCase = true)
                
                val matchesVeg = if (vegOnly) {
                    // Check if this restaurant has any items that are Veg
                    val items = menuItems.filter { it.restaurantId == rest.id }
                    items.any { it.isVeg }
                } else true

                val matchesTime = when (timeFilter) {
                    "30" -> rest.deliveryTimeMins == "20-25" || rest.deliveryTimeMins == "15-20" || rest.deliveryTimeMins == "25-30"
                    "45" -> rest.deliveryTimeMins != "45-50"
                    else -> true
                }

                matchesSearch && matchesVeg && matchesTime
            }

            if (filteredRestaurants.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No restaurants match your filters",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Try changing search tags or clearing filters",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredRestaurants) { rest ->
                    RestaurantCard(restaurant = rest, onClick = {
                        viewModel.navigateCustomer(CustomerScreen.RestaurantDetails(rest.id))
                    })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ExploreItemCard(
    title: String,
    desc: String,
    emoji: String,
    colorBg: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorBg),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = emoji, fontSize = 24.dp.value.sp)
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ZomatoDark
                )
                Text(
                    text = desc,
                    fontSize = 8.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RestaurantCard(restaurant: RestaurantEntity, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
            .testTag("restaurant_card_${restaurant.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFEFEFEF))
            ) {
                // Large emoji placeholder representing restaurant banner beautifully
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = restaurant.emoji, fontSize = 64.sp)
                        Text(
                            text = restaurant.name,
                            fontWeight = FontWeight.Light,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Offers ribbon
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2563EB))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏷️", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = restaurant.offerText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delivery badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FREE DELIVERY",
                        color = ZomatoGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Info Details
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ZomatoDark
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ZomatoGreen)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = restaurant.rating.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "· ${restaurant.deliveryTimeMins} mins · ${restaurant.distanceKm} km",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = LightGray)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(ZomatoRed.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 8.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = restaurant.featuredDish,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantDetailsPage(viewModel: MainViewModel, restaurantId: String) {
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()

    val restaurant = restaurants.find { it.id == restaurantId } ?: return
    val itemsInThisRestaurant = menuItems.filter { it.restaurantId == restaurantId }

    // Filter controls
    var showVegOnly by remember { mutableStateOf(false) }
    var showNonVegOnly by remember { mutableStateOf(false) }
    var showSpicyOnly by remember { mutableStateOf(false) }

    val totalItems = cart.sumOf { it.quantity }
    val totalPrice = cart.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 4.dp, color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateCustomer(CustomerScreen.Home) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ZomatoDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = restaurant.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ZomatoDark
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "", tint = ZomatoGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${restaurant.rating} · ${restaurant.deliveryTimeMins} mins · ${restaurant.distanceKm} km",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Share, "", tint = ZomatoDark)
                        }
                    }

                    // Filters switches
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = showVegOnly,
                            onClick = {
                                showVegOnly = !showVegOnly
                                if (showVegOnly) showNonVegOnly = false
                            },
                            label = { Text("Veg Only") },
                            leadingIcon = { Box(modifier = Modifier.size(8.dp).background(ZomatoGreen, CircleShape)) }
                        )

                        FilterChip(
                            selected = showNonVegOnly,
                            onClick = {
                                showNonVegOnly = !showNonVegOnly
                                if (showNonVegOnly) showVegOnly = false
                            },
                            label = { Text("Non-Veg") },
                            leadingIcon = { Box(modifier = Modifier.size(8.dp).background(ZomatoRed, CircleShape)) }
                        )

                        FilterChip(
                            selected = showSpicyOnly,
                            onClick = { showSpicyOnly = !showSpicyOnly },
                            label = { Text("Spicy 🌶️") }
                        )
                    }
                    Divider(color = LightGray)
                }
            }
        },
        bottomBar = {
            if (totalItems > 0) {
                Surface(
                    tonalElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZomatoRed)
                            .clickable { viewModel.navigateCustomer(CustomerScreen.Cart) }
                            .padding(14.dp)
                            .testTag("floating_cart_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$totalItems ITEMS",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${"%.0f".format(totalPrice)} plus taxes",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "View Cart",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val filteredItems = itemsInThisRestaurant.filter {
            val matchesVeg = !showVegOnly || it.isVeg
            val matchesNonVeg = !showNonVegOnly || it.isNonVeg
            val matchesSpicy = !showSpicyOnly || it.isSpicy
            matchesVeg && matchesNonVeg && matchesSpicy
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            // Restaurant Banner Offer detail
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9F9))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Save extra on this restaurant!",
                                fontWeight = FontWeight.Bold,
                                color = ZomatoRed,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Use coupon 'GETOFF220ON599' in checkout to save ₹220 off above ₹599 orders!",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Recommended block
            item {
                Text(
                    text = "Recommended Menu items (${filteredItems.size})",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = ZomatoDark,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🥕🥦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No items match current filters",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(filteredItems) { menu ->
                    val cartItem = cart.find { it.itemId == menu.id }
                    MenuItemRow(
                        item = menu,
                        quantityInCart = cartItem?.quantity ?: 0,
                        onAdd = { viewModel.addToCart(menu, restaurantId) },
                        onIncrement = { viewModel.incrementCart(menu.id) },
                        onDecrement = { viewModel.decrementCart(menu.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItemEntity,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.65f)) {
                // Veg / Non-Veg Indicator Icon
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(
                            1.5.dp,
                            if (item.isVeg) ZomatoGreen else ZomatoRed,
                            RoundedCornerShape(2.dp)
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (item.isVeg) ZomatoGreen else ZomatoRed,
                                CircleShape
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ZomatoDark
                )

                Text(
                    text = "₹${"%.0f".format(item.price)}",
                    fontWeight = FontWeight.SemiBold,
                    color = ZomatoDark,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                if (item.isSpicy) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ZomatoRed.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Spicy 🌶️", fontSize = 9.sp, color = ZomatoRed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Food picture / Add Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.emoji, fontSize = 48.sp)
                    if (!item.inStock) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "OUT OF\nSTOCK",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (item.inStock) {
                    if (quantityInCart == 0) {
                        Button(
                            onClick = onAdd,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, ZomatoRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_add_${item.id}")
                        ) {
                            Text("ADD", color = ZomatoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ZomatoRed, RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .height(32.dp)
                        ) {
                            IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                                Text("—", color = ZomatoRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(
                                text = quantityInCart.toString(),
                                fontWeight = FontWeight.Bold,
                                color = ZomatoRed,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, "", tint = ZomatoRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
        Divider(color = LightGray, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartPage(viewModel: MainViewModel) {
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val restaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()

    val currentRestaurantId = cart.firstOrNull()?.restaurantId
    val currentRestaurantName = restaurants.find { it.id == currentRestaurantId }?.name ?: "Appzeto Partner Merchant"

    val subTotal = cart.sumOf { it.price * it.quantity }
    val discount = when (appliedCoupon) {
        "GETOFF40ON249" -> if (subTotal >= 249.0) 40.0 else 0.0
        "GETOFF220ON599" -> if (subTotal >= 599.0) 220.0 else 0.0
        else -> 0.0
    }
    val deliveryCharge = 0.0
    val grandTotal = subTotal - discount + deliveryCharge

    var showPaymentSheet by remember { mutableStateOf(false) }
    var noteToRestaurant by remember { mutableStateOf("") }
    var blockCutlery by remember { mutableStateOf(false) }

    // Simulation order placement success
    var isPlacingOrder by remember { mutableStateOf(false) }
    var placementProgress by remember { mutableStateOf("") }
    var orderPlacedSuccessfully by remember { mutableStateOf(false) }
    var createdOrderId by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    if (orderPlacedSuccessfully) {
        OrderPlacedDialog(
            orderId = createdOrderId,
            onTrack = {
                viewModel.navigateCustomer(CustomerScreen.OrderTracking(createdOrderId))
                orderPlacedSuccessfully = false
            },
            onDismiss = {
                viewModel.navigateCustomer(CustomerScreen.Home)
                orderPlacedSuccessfully = false
            }
        )
    }

    if (isPlacingOrder) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = ZomatoRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Placing Your Order",
                        fontWeight = FontWeight.Bold,
                        color = ZomatoDark,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = placementProgress,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Food Cart", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateCustomer(CustomerScreen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total to Pay", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "₹${"%.0f".format(grandTotal)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = ZomatoDark
                            )
                        }

                        Button(
                            onClick = { showPaymentSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("place_order_button")
                        ) {
                            Text("Select Payment", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cart.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🛒", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your cart is completely empty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ZomatoDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Browse delicious menu offerings on the app to add your first dish!",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { viewModel.navigateCustomer(CustomerScreen.Home) },
                    colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed)
                ) {
                    Text("Explore Restaurants", color = Color.White)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGray.copy(alpha = 0.5f))
                    .padding(innerPadding)
            ) {
                // Restaurant reference info
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏰", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Delivering in 10-15 mins to Home",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ZomatoDark
                                )
                                Text(
                                    text = "New York, 123 Main Street · Apartment 4B",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Cart items listing
                item {
                    Text(
                        text = "YOUR SELECTED ITEMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(cart) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = ZomatoDark
                                    )
                                    Text(
                                        text = "₹${"%.0f".format(item.price)} each",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Quantities
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, ZomatoRed, RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .height(30.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.decrementCart(item.itemId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("—", color = ZomatoRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = ZomatoRed,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.incrementCart(item.itemId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, "", tint = ZomatoRed, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                // Addons horizontal suggestion
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "COMPLETE YOUR MEAL WITH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val suggestions = listOf(
                            Pair("Dal Kachori", Triple(22.86, "🥞", "gd_manchurian")),
                            Pair("Rasgulla", Triple(19.0, "🧁", "addon_rasgulla")),
                            Pair("Kaju Katli", Triple(317.0, "⬜", "addon_kaju_katli")),
                            Pair("Milk Cake", Triple(250.0, "🍰", "addon_milk_cake"))
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(suggestions) { meal ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = LightGray),
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                    ) {
                                        Text(text = meal.second.second, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = meal.first,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(text = "₹${"%.2f".format(meal.second.first)}", fontSize = 10.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                viewModel.addToCart(
                                                    MenuItemEntity(
                                                        id = meal.second.third,
                                                        restaurantId = cart.first().restaurantId,
                                                        name = meal.first,
                                                        description = "Delicious addon sweet.",
                                                        price = meal.second.first,
                                                        emoji = meal.second.second,
                                                        isVeg = true,
                                                        isNonVeg = false,
                                                        isSpicy = false
                                                    ),
                                                    cart.first().restaurantId
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                                            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(28.dp)
                                        ) {
                                            Text("ADD", fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Instructions panel
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = blockCutlery,
                                    onCheckedChange = { blockCutlery = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ZomatoRed)
                                )
                                Column {
                                    Text(
                                        text = "Don't send cutlery",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Help the environment by refusing plastic spoons.",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextField(
                                value = noteToRestaurant,
                                onValueChange = { noteToRestaurant = it },
                                placeholder = { Text("Write delivery instructions (e.g., Leave at gate)", fontSize = 12.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = LightGray,
                                    unfocusedContainerColor = LightGray,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }

                // Coupons listing
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "OFFERS & COUPONS FOR YOU",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Coupon 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Save ₹220 above ₹599",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Coupon code: GETOFF220ON599",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (appliedCoupon == "GETOFF220ON599") {
                                    Button(
                                        onClick = { viewModel.applyCoupon(null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("APPLIED", fontSize = 10.sp, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.applyCoupon("GETOFF220ON599") },
                                        enabled = subTotal >= 599,
                                        colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed)
                                    ) {
                                        Text("APPLY", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightGray)

                            // Coupon 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Save ₹40 above ₹249",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Coupon code: GETOFF40ON249",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                if (appliedCoupon == "GETOFF40ON249") {
                                    Button(
                                        onClick = { viewModel.applyCoupon(null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("APPLIED", fontSize = 10.sp, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.applyCoupon("GETOFF40ON249") },
                                        enabled = subTotal >= 249,
                                        colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed)
                                    ) {
                                        Text("APPLY", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill breakdown
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "BILL BREAKDOWN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Total", fontSize = 13.sp, color = Color.DarkGray)
                                Text("₹${"%.2f".format(subTotal)}", fontSize = 13.sp, color = ZomatoDark)
                            }
                            if (discount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Applied Coupon Discount", fontSize = 13.sp, color = ZomatoGreen)
                                    Text("-₹${"%.2f".format(discount)}", fontSize = 13.sp, color = ZomatoGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Charge (FREE)", fontSize = 13.sp, color = Color.Gray)
                                Text("₹0.00", fontSize = 13.sp, color = ZomatoGreen)
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = LightGray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomatoDark)
                                Text("₹${"%.2f".format(grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ZomatoDark)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // Payment Bottom Sheet simulation
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = Color.White,
            modifier = Modifier.testTag("payment_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Payment Method",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = ZomatoDark,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Google Pay
                PaymentRow(
                    name = "Google Pay UPI",
                    icon = "🟢",
                    onClick = {
                        showPaymentSheet = false
                        coroutineScope.launch {
                            isPlacingOrder = true
                            placementProgress = "Contacting Google Pay UPI..."
                            delay(1200)
                            placementProgress = "Securing transaction with bank..."
                            delay(1000)
                            placementProgress = "Confirming order status with Food App..."
                            delay(800)
                            viewModel.checkoutAndPlaceOrder(
                                restaurantId = cart.first().restaurantId,
                                restaurantName = currentRestaurantName,
                                paymentMethod = "Google Pay UPI",
                                address = "New York, 123 Main Street",
                                subTotal = subTotal,
                                couponCode = appliedCoupon,
                                discount = discount,
                                onSuccess = { id ->
                                    createdOrderId = id
                                    isPlacingOrder = false
                                    orderPlacedSuccessfully = true
                                }
                            )
                        }
                    }
                )

                // PhonePe
                PaymentRow(
                    name = "PhonePe UPI",
                    icon = "🟣",
                    onClick = {
                        showPaymentSheet = false
                        coroutineScope.launch {
                            isPlacingOrder = true
                            placementProgress = "Launching PhonePe Portal..."
                            delay(1000)
                            placementProgress = "Checking unified PIN parameters..."
                            delay(1100)
                            placementProgress = "Finalizing dispatch with Appzeto..."
                            delay(700)
                            viewModel.checkoutAndPlaceOrder(
                                restaurantId = cart.first().restaurantId,
                                restaurantName = currentRestaurantName,
                                paymentMethod = "PhonePe UPI",
                                address = "New York, 123 Main Street",
                                subTotal = subTotal,
                                couponCode = appliedCoupon,
                                discount = discount,
                                onSuccess = { id ->
                                    createdOrderId = id
                                    isPlacingOrder = false
                                    orderPlacedSuccessfully = true
                                }
                            )
                        }
                    }
                )

                // Cash/UPI on delivery
                PaymentRow(
                    name = "Cash/UPI on Delivery (COD)",
                    icon = "💵",
                    onClick = {
                        showPaymentSheet = false
                        coroutineScope.launch {
                            isPlacingOrder = true
                            placementProgress = "Registering delivery location coordinates..."
                            delay(800)
                            placementProgress = "Authenticating client parameters..."
                            delay(600)
                            viewModel.checkoutAndPlaceOrder(
                                restaurantId = cart.first().restaurantId,
                                restaurantName = currentRestaurantName,
                                paymentMethod = "Cash on Delivery",
                                address = "New York, 123 Main Street",
                                subTotal = subTotal,
                                couponCode = appliedCoupon,
                                discount = discount,
                                onSuccess = { id ->
                                    createdOrderId = id
                                    isPlacingOrder = false
                                    orderPlacedSuccessfully = true
                                    viewModel.clearCart()
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PaymentRow(name: String, icon: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ZomatoDark,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ArrowForward, "", tint = Color.Gray)
    }
    Divider(color = LightGray)
}

@Composable
fun OrderPlacedDialog(orderId: Int, onTrack: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onTrack,
                colors = ButtonDefaults.buttonColors(containerColor = ZomatoRed),
                modifier = Modifier.fillMaxWidth().testTag("track_btn")
            ) {
                Text("Track Your Order", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Go to Homepage", color = Color.Gray, textAlign = TextAlign.Center)
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(ZomatoGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Order Placed!",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = ZomatoGreen,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Your food preparation is starting shortly. Monitor real-time progress via the live tracking dashboard.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f)
    )

    // Render Confetti directly on screen
    ConfettiOverlay()
}

@Composable
fun ConfettiOverlay() {
    val transition = rememberInfiniteTransition(label = "confetti")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_sweep"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color.Cyan)
        val rand = Random(42) // Constant seed for stable count
        for (i in 0..40) {
            val startX = rand.nextFloat() * size.width
            val speedY = 150f + rand.nextFloat() * 200f
            val currY = (sweep + startX) % size.height
            val currX = startX + sin(sweep / 50f + startX) * 20f
            val color = colors[rand.nextInt(colors.size)]
            val sizeC = 8f + rand.nextFloat() * 10f

            drawRect(
                color = color,
                topLeft = Offset(currX, currY),
                size = androidx.compose.ui.geometry.Size(sizeC, sizeC)
            )
        }
    }
}

@Composable
fun OrderTrackingPage(viewModel: MainViewModel, orderId: Int) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val order = orders.find { it.id == orderId } ?: return

    val progressState = when (order.status) {
        "PREPARING" -> 1
        "READY" -> 2
        "OUT_FOR_DELIVERY" -> 3
        "DELIVERED" -> 4
        else -> 1
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateCustomer(CustomerScreen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ZomatoDark)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = order.restaurantName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ZomatoDark
                        )
                        Text(
                            text = "Live Tracking order #${order.id}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFC))
                .padding(innerPadding)
        ) {
            // Live Interactive Map Canvas
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    var animationProgress by remember { mutableStateOf(0f) }
                    val animatedVal by animateFloatAsState(
                        targetValue = when (progressState) {
                            1 -> 0.1f
                            2 -> 0.4f
                            3 -> 0.75f
                            4 -> 1f
                            else -> 0.1f
                        },
                        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                        label = "track_bike"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background Grid Mock Map
                        drawRect(Color(0xFFE8F5E9), size = size)

                        // Dotted track road
                        val pathStart = Offset(80f, size.height / 2f)
                        val pathMid = Offset(size.width / 2f, size.height * 0.3f)
                        val pathEnd = Offset(size.width - 80f, size.height / 2f)

                        drawLine(
                            brush = Brush.horizontalGradient(listOf(ZomatoRed, ZomatoGreen)),
                            start = pathStart,
                            end = pathMid,
                            strokeWidth = 6f
                        )
                        drawLine(
                            brush = Brush.horizontalGradient(listOf(ZomatoGreen, ZomatoRed)),
                            start = pathMid,
                            end = pathEnd,
                            strokeWidth = 6f
                        )

                        // Draw Restaurant point
                        drawCircle(color = ZomatoRed, radius = 16f, center = pathStart)
                        
                        // Draw Client Home point
                        drawCircle(color = ZomatoGreen, radius = 16f, center = pathEnd)

                        // Moving Delivery bike
                        val currentBikePoint = if (animatedVal <= 0.5f) {
                            val local = animatedVal / 0.5f
                            Offset(
                                pathStart.x + (pathMid.x - pathStart.x) * local,
                                pathStart.y + (pathMid.y - pathStart.y) * local
                            )
                        } else {
                            val local = (animatedVal - 0.5f) / 0.5f
                            Offset(
                                pathMid.x + (pathEnd.x - pathMid.x) * local,
                                pathMid.y + (pathEnd.y - pathMid.y) * local
                            )
                        }

                        drawCircle(color = ZomatoDark, radius = 20f, center = currentBikePoint)
                    }

                    // Floating pointers text
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🏮 Restaurant", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("🏡 Home", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Realtime Status list
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ORDER STATUS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    StatusItem(
                        stage = 1,
                        activeStage = progressState,
                        title = "Order received & food prep started",
                        desc = "The kitchen is preparing your delicious menu items dynamically."
                    )
                    StatusItem(
                        stage = 2,
                        activeStage = progressState,
                        title = "Food packets packaged & ready",
                        desc = "Order is prepared and wait for the delivery partner to arrive."
                    )
                    StatusItem(
                        stage = 3,
                        activeStage = progressState,
                        title = "Driver Assigned & out for delivery",
                        desc = "Delivery boy 'Appzeto Rider' has picked up and is transit."
                    )
                    StatusItem(
                        stage = 4,
                        activeStage = progressState,
                        title = "Successfully delivered!",
                        desc = "Enjoy your meal! Let us know your feedback on Appzeto."
                    )
                }
            }

            // Promotional HDFC bank offers card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💳", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HDFC Bank: 10% instant cashback on all orders",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ZomatoDark
                        )
                        Text(
                            text = "Valid on orders above ₹299. Use card code: HDFC15 in payment.",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusItem(stage: Int, activeStage: Int, title: String, desc: String) {
    val isDone = activeStage >= stage
    val isActive = activeStage == stage

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isDone) ZomatoGreen else Color.LightGray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, "", tint = Color.White, modifier = Modifier.size(12.dp))
                } else {
                    Text(text = stage.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (stage < 4) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (isDone) ZomatoGreen else Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp,
                color = if (isActive) ZomatoDark else if (isDone) Color.DarkGray else Color.Gray
            )
            if (isActive) {
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
