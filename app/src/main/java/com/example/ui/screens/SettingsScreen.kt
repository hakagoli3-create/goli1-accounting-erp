package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.CloudSyncStatus
import com.example.data.model.StoreProfile
import com.example.data.model.UserRole
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.theme.PersianGold
import com.example.util.JalaliDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    securityManager: AppSecurityAndSettingsManager,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()
    val storeProfile by securityManager.storeProfile.collectAsStateWithLifecycle()
    val syncStatus by securityManager.syncStatus.collectAsStateWithLifecycle()
    val lastSyncTime by securityManager.lastSyncTime.collectAsStateWithLifecycle()

    var name by remember(storeProfile) { mutableStateOf(storeProfile.name) }
    var managerName by remember(storeProfile) { mutableStateOf(storeProfile.managerName) }
    var phone by remember(storeProfile) { mutableStateOf(storeProfile.phone) }
    var mobile by remember(storeProfile) { mutableStateOf(storeProfile.mobile) }
    var address by remember(storeProfile) { mutableStateOf(storeProfile.address) }
    var nationalCode by remember(storeProfile) { mutableStateOf(storeProfile.nationalCode) }
    var economicCode by remember(storeProfile) { mutableStateOf(storeProfile.economicCode) }
    var defaultBankCard by remember(storeProfile) { mutableStateOf(storeProfile.defaultBankCard) }
    var defaultSheba by remember(storeProfile) { mutableStateOf(storeProfile.defaultSheba) }
    var taxRate by remember(storeProfile) { mutableStateOf(storeProfile.taxRatePercent.toString()) }

    var isSyncing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "تنظیمات فروشگاه و سطوح دسترسی",
                subtitle = "مدیریت مشخصات رسمی، نقش کاربران (RBAC) و همگام‌سازی ابری"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme & Display Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "حالت تیره (Dark Mode)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "پالت مات زغالی با کنتراست بالا",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onToggleDarkTheme,
                        modifier = Modifier.testTag("toggle_dark_theme_switch")
                    )
                }
            }

            // User Roles (RBAC) Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = PersianGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نقش فعال کاربر (کنترل دسترسی RBAC)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "با انتخاب هر نقش، دسترسی بخش‌های حساس مانند سود و زیان، حذف اسناد و قیمت‌های خرید کنترل می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            val isSelected = currentRole == role
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    securityManager.switchRole(role)
                                    Toast.makeText(context, "نقش به «${role.title}» تغییر یافت.", Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(
                                        text = role.title.split(" ")[0],
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("role_chip_${role.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "دسترسی‌های فعال برای ${currentRole.title}:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• مشاهده سود و زیان مالی: ${if (currentRole.canViewProfit) "مجاز" else "مسدود"}\n" +
                                        "• حذف فاکتور و اسناد: ${if (currentRole.canDeleteDocuments) "مجاز" else "مسدود"}\n" +
                                        "• مشاهده قیمت خرید کالاها: ${if (currentRole.canEditCostPrices) "مجاز" else "محدود به فروش"}\n" +
                                        "• دسترسی به موجودی نقدینگی خزانه‌داری: ${if (currentRole.canAccessTreasuryBalances) "مجاز" else "مسدود"}",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Cloud Sync Section (همگام‌سازی ابری و پایگاه داده محلی Room)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = AccountingDebitGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "پایگاه داده و همگام‌سازی ابری",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "دیتابیس محلی قدرتمند Room + قابلیت اتصال ابری",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "وضعیت: ${syncStatus.title}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (syncStatus == CloudSyncStatus.ONLINE_SYNCED) AccountingDebitGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "آخرین همگام‌سازی موفق: $lastSyncTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isSyncing = true
                            securityManager.setSyncStatus(CloudSyncStatus.SYNCING)
                            scope.launch {
                                delay(1200) // شبیه‌سازی اتصال به سرور ابری و چک کردن صف
                                val now = JalaliDate.now().format(includeTime = true)
                                securityManager.setSyncStatus(CloudSyncStatus.ONLINE_SYNCED, now)
                                isSyncing = false
                                Toast.makeText(context, "همگام‌سازی کلیه اطلاعات با فضای ابری با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth().testTag("sync_cloud_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSyncing) "در حال ارسال و دریافت اطلاعات..." else "همگام‌سازی اطلاعات با فضای ابری")
                    }
                }
            }

            // Store Profile Settings Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشخصات بازرگانی و سربرگ فاکتور",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام فروشگاه / شرکت بازرگانی") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = managerName,
                            onValueChange = { managerName = it },
                            label = { Text("نام مدیریت") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("تلفن ثابت") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("شماره همراه") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = nationalCode,
                            onValueChange = { nationalCode = it },
                            label = { Text("شناسه / کد ملی") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس دقیق فروشگاه و انبار") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = defaultBankCard,
                        onValueChange = { defaultBankCard = it },
                        label = { Text("شماره کارت واریزی پیش‌فرض (پاورقی فاکتور)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = defaultSheba,
                        onValueChange = { defaultSheba = it },
                        label = { Text("شماره شبا حساب بانکی") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        label = { Text("نرخ مالیات بر ارزش افزوده (درصد)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val profile = StoreProfile(
                                name = name.ifBlank { "حسابداری و بازرگانی گلی" },
                                managerName = managerName,
                                phone = phone,
                                mobile = mobile,
                                address = address,
                                nationalCode = nationalCode,
                                economicCode = economicCode,
                                defaultBankCard = defaultBankCard,
                                defaultSheba = defaultSheba,
                                taxRatePercent = taxRate.toIntOrNull() ?: 10
                            )
                            securityManager.updateProfile(profile)
                            Toast.makeText(context, "اطلاعات فروشگاه با موفقیت ذخیره شد.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_store_profile_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ذخیره تغییرات مشخصات فروشگاه")
                    }
                }
            }
        }
    }
}
