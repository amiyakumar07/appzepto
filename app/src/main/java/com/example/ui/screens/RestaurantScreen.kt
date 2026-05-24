package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.OrderEntity
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreenContent(viewModel: MainViewModel) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val menuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val rTab by viewModel.restaurantTab.collectAsStateWithLifecycle()
    val rSubTab by viewModel.restaurantSubTab.collectAsStateWithLifecycle()

    var restaurantOnline by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B)) // Elegant Slate Dark theme for Restaurateur
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header with switch online
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Appzeto Partner Hub",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (restaurantOnline) Color.Green else Color.Red,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (restaurantOnline) "Accepting orders" else "Offline",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (restaurantOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (restaurantOnline) Color.Green else Color.Red
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = restaurantOnline,
                            onCheckedChange = { restaurantOnline = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Green,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Red
                            ),
                            modifier = Modifier.scale(0.81f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub navigation tabs: Orders vs Menu/Inventory
                Row(modifier = Modifier.fillMaxWidth()) {
                    SubNavButton(
                        text = "Orders",
                        isActive = rSubTab == "Orders",
                        onClick = { viewModel.setRestaurantSubTab("Orders") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SubNavButton(
                        text = "Menu & Inventory",
                        isActive = rSubTab == "Menu/Inventory",
                        onClick = { viewModel.setRestaurantSubTab("Menu/Inventory") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (rSubTab == "Orders") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
                    .padding(innerPadding)
            ) {
                // Secondary level status tabs (Preparing, Ready, Out for delivery)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatusTab(title = "Preparing", active = rTab == "Preparing", count = orders.count { it.status == "PREPARING" }, onClick = { viewModel.setRestaurantTab("Preparing") })
                    StatusTab(title = "Ready", active = rTab == "Ready", count = orders.count { it.status == "READY" }, onClick = { viewModel.setRestaurantTab("Ready") })
                    StatusTab(title = "Out for delivery", active = rTab == "Out", count = orders.count { it.status == "OUT_FOR_DELIVERY" }, onClick = { viewModel.setRestaurantTab("Out") })
                }

                val currentStatusFilter = when (rTab) {
                    "Preparing" -> "PREPARING"
                    "Ready" -> "READY"
                    else -> "OUT_FOR_DELIVERY"
                }
                val filteredOrders = orders.filter { it.status == currentStatusFilter }

                if (filteredOrders.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📜", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders currently under '$rTab'",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Newly placed client bookings automatically populate here.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { ord ->
                            RestaurantOrderCard(order = ord, onStatusAction = {
                                if (ord.status == "PREPARING") {
                                    viewModel.markOrderReady(ord.id)
                                }
                            })
                        }
                    }
                }
            }
        } else {
            // Menu/Inventory toggles list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(innerPadding)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Toggle product availability below to sync live stock with the client frontpage.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }

                items(menuItems) { dish ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dish.emoji, fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = dish.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "₹${dish.price}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Stock available Switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (dish.inStock) "IN STOCK" else "OUT OF STOCK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dish.inStock) Color(0xFF10B981) else Color.Red
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = dish.inStock,
                                onCheckedChange = { viewModel.toggleMenuItemStock(dish.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFF10B981)
                                ),
                                modifier = Modifier
                                    .scale(0.8f)
                                    .testTag("in_stock_switch_${dish.id}")
                            )
                        }
                    }
                    Divider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }
}

@Composable
fun SubNavButton(text: String, isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.White else Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = text,
            color = if (isActive) Color(0xFF1E293B) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun StatusTab(title: String, active: Boolean, count: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (active) Color(0xFF1E293B) else Color.Gray
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(ZomatoRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = count.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .background(if (active) ZomatoRed else Color.Transparent)
        )
    }
}

@Composable
fun RestaurantOrderCard(order: OrderEntity, onStatusAction: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER ID #${order.id}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Customer address: ${order.address}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (order.status) {
                                "PREPARING" -> Color(0xFFFFF3E0)
                                "READY" -> Color(0xFFFFFDE7)
                                else -> Color(0xFFE8F5E9)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            "PREPARING" -> Color(0xFFE65100)
                            "READY" -> Color(0xFFF4C430)
                            else -> Color(0xFF1B5E20)
                        }
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "DISPATCHING ITEMS:",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Text(
                text = order.itemsSummary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                modifier = Modifier.padding(vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total billing value", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = "₹${"%.2f".format(order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                if (order.status == "PREPARING") {
                    Button(
                        onClick = onStatusAction,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("action_prepare_btn")
                    ) {
                        Text("Mark as Ready", fontSize = 12.sp, color = Color.White)
                    }
                } else if (order.status == "READY") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, "", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Waiting for pickup...",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, "", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Picked up by driver",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


