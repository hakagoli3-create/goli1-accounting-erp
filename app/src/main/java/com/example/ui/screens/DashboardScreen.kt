package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.DashboardViewModel
import com.example.util.NumberFormatUtils

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRoute: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val quickActions = listOf(
        QuickAction("صندوق POS", Icons.Default.PointOfSale, MaterialTheme.colorScheme.primary, "pos"),
        QuickAction("فاکتور رسمی", Icons.AutoMirrored.Filled.ReceiptLong, PersianGold, "invoices"),
        QuickAction("حسابداری مالی", Icons.AutoMirrored.Filled.Assignment, Color(0xFF1976D2), "accounting"),
        QuickAction("خزانه‌داری و چک", Icons.Default.AccountBalance, Color(0xFF7B1FA2), "treasury"),
        QuickAction("انبار و کالا", Icons.Default.Inventory2, Color(0xFF00796B), "inventory"),
        QuickAction("مشتریان CRM", Icons.Default.People, Color(0xFFE65100), "crm"),
        QuickAction("تنظیمات و دسترسی", Icons.Default.Settings, Color(0xFF455A64), "settings")
    )

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "مدیریت حسابداری گلی",
                subtitle = "امروز: ${state.currentShamsiDate}"
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Welcome Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "سیستم یکپارچه مالی و ERP گلی",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تراز مالی لحظه‌ای، انبارداری و خزانه‌داری آنلاین",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions Horizontal Row
            item {
                Column {
                    Text(
                        text = "دسترسی سریع به ماژول‌ها",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(quickActions) { action ->
                            Card(
                                modifier = Modifier
                                    .width(105.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToRoute(action.route) }
                                    .testTag("quick_action_${action.route}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(action.color.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = action.title,
                                            tint = action.color,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = action.title,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Metric Stat Cards Grid
            item {
                Text(
                    text = "شاخص‌های کلیدی امروز",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                MetricStatCard(
                    title = "فروش امروز",
                    value = NumberFormatUtils.formatPrice(state.todaySalesAmount),
                    subValue = "${NumberFormatUtils.toPersianDigits(state.todayInvoicesCount.toString())} فاکتور صادر شده",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    iconTint = AccountingDebitGreen,
                    onClick = { onNavigateToRoute("invoices") }
                )
            }

            item {
                val totalLiquid = state.totalCashBalance + state.totalBankBalance
                MetricStatCard(
                    title = "نقدینگی کل (صندوق و بانک)",
                    value = NumberFormatUtils.formatPrice(totalLiquid),
                    subValue = "بانک: ${NumberFormatUtils.formatPrice(state.totalBankBalance)} | صندوق: ${NumberFormatUtils.formatPrice(state.totalCashBalance)}",
                    icon = Icons.Default.AccountBalance,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigateToRoute("treasury") }
                )
            }

            // Warnings Section
            item {
                Text(
                    text = "هشدارها و نیاز به توجه",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRoute("treasury") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.dueChequesAlertCount > 0) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "سررسید چک‌ها",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.dueChequesAlertCount > 0) AlertWarningOrange else MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (state.dueChequesAlertCount > 0) AlertWarningOrange else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(state.dueChequesAlertCount.toString())} فقره چک",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "تا ۷ روز آینده یا معوقه",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRoute("inventory") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.lowStockCount > 0) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "کسری موجودی",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.lowStockCount > 0) AccountingCreditRed else MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (state.lowStockCount > 0) AccountingCreditRed else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${NumberFormatUtils.toPersianDigits(state.lowStockCount.toString())} قلم کالا",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "کمتر از نقطه سفارش",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (state.overCreditPartiesCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRoute("crm") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "هشدار سقف اعتبار مشتریان",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = AccountingCreditRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${NumberFormatUtils.toPersianDigits(state.overCreditPartiesCount.toString())} مشتری از سقف اعتبار مجاز خود عبور کرده‌اند.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AccountingCreditRed
                            )
                        }
                    }
                }
            }
        }
    }
}
