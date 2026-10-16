package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AccountingScreen
import com.example.ui.screens.CrmScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoiceScreen
import com.example.ui.screens.PosScreen
import com.example.ui.screens.TreasuryScreen
import com.example.ui.theme.GoliAccountingTheme
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.InventoryViewModel
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.ui.viewmodel.PartyViewModel
import com.example.ui.viewmodel.TreasuryViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    data object Pos : Screen("pos", "صندوق POS", Icons.Default.PointOfSale)
    data object Invoices : Screen("invoices", "فاکتورها", Icons.Default.ReceiptLong)
    data object Accounting : Screen("accounting", "حسابداری", Icons.Default.Assignment)
    data object Treasury : Screen("treasury", "خزانه‌داری", Icons.Default.AccountBalance)
    data object Inventory : Screen("inventory", "انبار و کالا", Icons.Default.Inventory2)
    data object Crm : Screen("crm", "مشتریان", Icons.Default.People)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoliAccountingTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val context = LocalContext.current
    val factory = AppViewModelFactory(context)

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val accountingViewModel: AccountingViewModel = viewModel(factory = factory)
    val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
    val invoiceViewModel: InvoiceViewModel = viewModel(factory = factory)
    val treasuryViewModel: TreasuryViewModel = viewModel(factory = factory)
    val partyViewModel: PartyViewModel = viewModel(factory = factory)

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Pos,
        Screen.Invoices,
        Screen.Accounting,
        Screen.Treasury
    )

    val allScreens = listOf(
        Screen.Dashboard,
        Screen.Pos,
        Screen.Invoices,
        Screen.Accounting,
        Screen.Inventory,
        Screen.Treasury,
        Screen.Crm
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "مدیریت حسابداری گلی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(20.dp)
                )
                allScreens.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_${screen.route}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateToRoute = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.Pos.route) {
                        PosScreen(viewModel = invoiceViewModel)
                    }
                    composable(Screen.Invoices.route) {
                        InvoiceScreen(viewModel = invoiceViewModel)
                    }
                    composable(Screen.Accounting.route) {
                        AccountingScreen(viewModel = accountingViewModel)
                    }
                    composable(Screen.Inventory.route) {
                        InventoryScreen(viewModel = inventoryViewModel)
                    }
                    composable(Screen.Treasury.route) {
                        TreasuryScreen(viewModel = treasuryViewModel)
                    }
                    composable(Screen.Crm.route) {
                        CrmScreen(viewModel = partyViewModel)
                    }
                }
            }
        }
    }
}
