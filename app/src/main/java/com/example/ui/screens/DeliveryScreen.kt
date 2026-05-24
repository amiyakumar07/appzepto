package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.DriverPayoutEntity
import com.example.data.db.OrderEntity
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreenContent(viewModel: MainViewModel) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val driverPayouts by viewModel.allDriverPayouts.collectAsStateWithLifecycle()
    val deliveryOnline by viewModel.deliveryOnline.collectAsStateWithLifecycle()
    val subTab by viewModel.deliverySubTab.collectAsStateWithLifecycle()

    val totalEarnings = driverPayouts.sumOf { it.tripPay + it.bonusPay }

    // Active order assigned to driver
    val activeDriverOrder = orders.find { it.driverId == "APP_BOY_1" && it.status != "DELIVERED" }

    // Unassigned orders that are READY for pickup (acts as the delivery feed)
    val availableReadyOrders = orders.filter { it.status == "READY" && it.driverId == null }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A)) // Sleek dark slate theme
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ZomatoRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚴", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Appzeto Pilot Console",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "ID: Pilot #599",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Online toggle switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (deliveryOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (deliveryOnline) Color.Green else Color.Red
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = deliveryOnline,
                            onCheckedChange = { viewModel.setDeliveryOnline(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color.Green,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Red
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Sub navigation
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.setDeliverySubTab("Feed") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == "Feed") Color.White else Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "Delivery Feed",
                            color = if (subTab == "Feed") Color(0xFF0F172A) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.setDeliverySubTab("Pocket") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == "Pocket") Color.White else Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "Pocket & Logs",
                            color = if (subTab == "Pocket") Color(0xFF0F172A) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (subTab == "Feed") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
                    .padding(innerPadding)
            ) {
                if (!deliveryOnline) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("💤", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You are currently offline",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Turn 'ONLINE' in top switch to list hot bookings and track routing schedules.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (activeDriverOrder != null) {
                    // Active delivery trip in progress!
                    ActiveTripPanel(order = activeDriverOrder, viewModel = viewModel)
                } else {
                    // Hotspots & Incoming Orders Feed
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Hotspots map indicator
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(Color(0xFFE2E8F0)) // Light gray grid
                                    // Highlight center zone
                                    drawCircle(
                                        color = Color.Yellow.copy(alpha = 0.3f),
                                        radius = 120f,
                                        center = Offset(size.width / 2f, size.height / 2f)
                                    )
                                    drawCircle(
                                        color = Color.Red.copy(alpha = 0.15f),
                                        radius = 60f,
                                        center = Offset(size.width / 2f, size.height / 2f)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📍 Indore Bypass Active zone", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Simulating current location GPS. Ready to pick requests.", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }

                        // Feed of pending orders
                        Text(
                            text = "AVAILABLE PICKUP JOBS (${availableReadyOrders.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )

                        if (availableReadyOrders.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🔔", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready orders radar is active",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Place custom food orders in 'Customer Mode', set restaurant to mark them 'Ready', and watch bookings stream here instantly!",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(availableReadyOrders) { ord ->
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
                                                        text = "🔥 JOB OFFER DISPATCH",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 12.sp,
                                                        color = ZomatoRed
                                                    )
                                                    Text(
                                                        text = "Pickup Restaurant: ${ord.restaurantName}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }
                                                Text(
                                                    text = "₹111.94 est",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                            }

                                            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("Est Distance", fontSize = 11.sp, color = Color.Gray)
                                                    Text("8.8 kms total", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column {
                                                    Text("Est Payout Structure", fontSize = 11.sp, color = Color.Gray)
                                                    Text("Trip: ₹71.62 + Bonus ₹5", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Button(
                                                onClick = { viewModel.acceptOrder(ord.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(44.dp)
                                                    .testTag("accept_trip_button_${ord.id}")
                                            ) {
                                                Text("ACCEPT FORWARDED ORDER", color = Color.White, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Pocket / Earnings summary tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(innerPadding)
            ) {
                // Large balance card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("POCKET BALANCE STATUS", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${"%.2f".format(totalEarnings + 428.04)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Deposits", color = Color.Gray, fontSize = 10.sp)
                                    Text("₹500.00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Available cash limit", color = Color.Gray, fontSize = 10.sp)
                                    Text("₹1571.96", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "HISTORIC Payout details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (driverPayouts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No payouts executed yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                } else {
                    items(driverPayouts) { pay ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Trip Order ID #${pay.orderId} Complete",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${pay.distanceKm} kms over ${pay.durationMins} mins",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "+₹${"%.2f".format(pay.tripPay + pay.bonusPay)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                // Extra secondary services lists mimicking video
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "MORE SERVICES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    val services = listOf(
                        Pair("₹10 Payout", "Withdraw directly to bank UPI"),
                        Pair("Customer tips log", "Review customer gratuity payouts"),
                        Pair("Deductions invoice", "Inspect company platform assets fee"),
                        Pair("Fuel payments", "Refill reimbursement coupons")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        services.forEach { service ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = service.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = service.second, fontSize = 11.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.KeyboardArrowRight, "", tint = Color.Gray)
                            }
                            Divider(color = Color(0xFFF1F5F9))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun ActiveTripPanel(order: OrderEntity, viewModel: MainViewModel) {
    var tripStatus by remember { mutableStateOf("ACCEPTED") } // ACCEPTED -> AT_RESTAURANT -> DELIVERED

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "ACTIVE ROUTING DISPATCH",
                fontWeight = FontWeight.Black,
                color = ZomatoRed,
                fontSize = 11.sp
            )

            Text(
                text = "Order Trip #${order.id}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )

            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            // Pickup Node
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(ZomatoRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏮", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("PICKUP MERCHANT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("401, 4th Floor, Pushparatna solitar building, Indore", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Drop Node
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Green, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏡", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("DELIVERY RECIPIENT HOME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("Customer: Rahul Mehta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(order.address, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display dynamic checklist action
            when (tripStatus) {
                "ACCEPTED" -> {
                    Button(
                        onClick = {
                            tripStatus = "AT_RESTAURANT"
                            // Notify restaurant status to OUT_FOR_DELIVERY
                            viewModel.updateProgress(order.id, "OUT_FOR_DELIVERY")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_pickup_check")
                    ) {
                        Text("REACHED PICKUP POINT", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                "AT_RESTAURANT" -> {
                    Button(
                        onClick = {
                            tripStatus = "DELIVERED"
                            viewModel.completeDelivery(
                                orderId = order.id,
                                tripPay = 71.62,
                                bonusPay = 5.0,
                                distance = 8.8,
                                duration = 38
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_drop_check")
                    ) {
                        Text("REACHED DROP-OFF COMPLETED", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                "DELIVERED" -> {
                    // Show payout congrats card mirroring end of video
                    CongratsPayoutCard(onDismiss = {
                        // Resets to let other deliveries pop up
                        tripStatus = "ACCEPTED"
                    })
                }
            }
        }
    }
}

@Composable
fun CongratsPayoutCard(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🎉", fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Great job! Delivery complete 👍",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = Color(0xFF10B981)
        )
        Text(
            text = "Total credit payout assigned",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Payout stats details
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PayoutValueRow("Trip pay base rate", "₹71.62")
                PayoutValueRow("Long distance return premium", "₹5.00")
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(8.dp))
                PayoutValueRow("Final credit net sum", "₹76.62", highlight = true)
                Spacer(modifier = Modifier.height(8.dp))
                PayoutValueRow("Trip mileage distance", "8.8 kms")
                PayoutValueRow("Trip duration timing", "38 mins")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text("GET NEXT DISPATCH", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PayoutValueRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (highlight) 13.sp else 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Color(0xFF0F172A) else Color.Gray
        )
        Text(
            text = value,
            fontSize = if (highlight) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFF10B981) else Color(0xFF0F172A)
        )
    }
}
