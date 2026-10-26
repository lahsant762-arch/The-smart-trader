package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.CashflowScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.ReportsAndFilesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MerchantViewModel
import com.example.util.configureAppCheck

enum class MainTab(val title: String, val icon: ImageVector) {
    POS("نقطة البيع", Icons.Default.PointOfSale),
    INVENTORY("السلع والمخزون", Icons.Default.Inventory2),
    CASHFLOW("المصاريف", Icons.Default.AccountBalanceWallet),
    AI_ADVISOR("المستشار الذكي", Icons.Default.AutoAwesome),
    REPORTS("التقارير", Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MerchantViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check before UI or ViewModels operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Ensure RTL layout for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                    val tabs = MainTab.values()

                    val cart by viewModel.cart.collectAsStateWithLifecycle()
                    val cartCount = cart.size

                    // Handle device back button
                    if (selectedTabIndex != 0) {
                        BackHandler {
                            selectedTabIndex = 0
                        }
                    }

                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "التاجر الذكي",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = tabs[selectedTabIndex].title,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                                    // Mode Indicator Badge (Cloud or Offline)
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (currentUser != null)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (currentUser != null) Icons.Default.Assessment else Icons.Default.WifiOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (currentUser != null) "سحابي + محلي" else "محلي 100%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                tabs.forEachIndexed { index, tab ->
                                    val isSelected = selectedTabIndex == index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectedTabIndex = index },
                                        icon = {
                                            if (tab == MainTab.POS && cartCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge { Text("$cartCount") }
                                                    }
                                                ) {
                                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                                }
                                            } else {
                                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (tabs[selectedTabIndex]) {
                                MainTab.POS -> PosScreen(viewModel = viewModel)
                                MainTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                                MainTab.CASHFLOW -> CashflowScreen(viewModel = viewModel)
                                MainTab.AI_ADVISOR -> AiAdvisorScreen(viewModel = viewModel)
                                MainTab.REPORTS -> ReportsAndFilesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
