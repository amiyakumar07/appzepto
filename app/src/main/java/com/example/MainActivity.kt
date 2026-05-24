package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CustomerScreenContent
import com.example.ui.screens.DeliveryScreenContent
import com.example.ui.screens.RestaurantScreenContent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Persistent Floating Role Switcher Panel at bottom!
                        // This allows instant testing/switching between all 3 personas of the video
                        Surface(
                            shadowElevation = 8.dp,
                            tonalElevation = 6.dp,
                            color = Color(0xFF0F172A), // Elegant dark footer
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TAP TO TEST PLATFORM PERSPECTIVES (3-IN-1 SIMULATOR):",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(32.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RoleButton(
                                        title = "Customer 🛒",
                                        active = currentRole == AppRole.CUSTOMER,
                                        activeColor = Color(0xFFEF4F56), // Zomato Red
                                        onClick = { viewModel.setRole(AppRole.CUSTOMER) },
                                        modifier = Modifier.weight(1f).testTag("select_customer_role")
                                    )
                                    RoleButton(
                                        title = "Restaurant 🏮",
                                        active = currentRole == AppRole.RESTAURANT,
                                        activeColor = Color(0xFF10B981), // Emerald Green
                                        onClick = { viewModel.setRole(AppRole.RESTAURANT) },
                                        modifier = Modifier.weight(1f).testTag("select_restaurant_role")
                                    )
                                    RoleButton(
                                        title = "Delivery 🚴",
                                        active = currentRole == AppRole.DELIVERY,
                                        activeColor = Color(0xFF2563EB), // Blue
                                        onClick = { viewModel.setRole(AppRole.DELIVERY) },
                                        modifier = Modifier.weight(1f).testTag("select_delivery_role")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        AnimatedContent(
                            targetState = currentRole,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                            },
                            label = "role_content"
                        ) { role ->
                            when (role) {
                                AppRole.CUSTOMER -> CustomerScreenContent(viewModel)
                                AppRole.RESTAURANT -> RestaurantScreenContent(viewModel)
                                AppRole.DELIVERY -> DeliveryScreenContent(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleButton(
    title: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (active) activeColor else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (active) Color.White else Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
