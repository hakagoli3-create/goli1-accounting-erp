package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.viewmodel.InventoryViewModel
import com.example.util.NumberFormatUtils

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel
) {
    val context = LocalContext.current
    val warehouses by viewModel.allWarehouses.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCardexProduct by viewModel.selectedProductCardex.collectAsStateWithLifecycle()
    val cardexMovements by viewModel.cardexMovements.collectAsStateWithLifecycle()

    var showAddProductDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "مدیریت انبارها و کالاها",
                subtitle = "موجودی ترکیبی کارتن/عدد، قیمت‌گذاری چندسطحی و کاردکس"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "تعریف کالا", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Warehouses row
            if (warehouses.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(warehouses) { wh ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${wh.name} (${wh.code})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("جستجوی کالا، کد یا بارکد...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_product_input")
            )

            // Category filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isSelected = selectedCategory == null
                    Card(
                        modifier = Modifier.clickable { viewModel.selectCategory(null) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "همه گروه‌ها",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Card(
                        modifier = Modifier.clickable { viewModel.selectCategory(cat) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Product List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(products) { product ->
                    val isLowStock = product.currentStockInSecondary <= product.minStockThreshold

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectProductForCardex(product) }
                            .testTag("product_item_${product.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "کد: ${product.code} | بارکد: ${NumberFormatUtils.toPersianDigits(product.barcode)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isLowStock) {
                                    Box(
                                        modifier = Modifier
                                            .background(AccountingCreditRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = AccountingCreditRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "کسری موجودی",
                                                color = AccountingCreditRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dual unit stock display
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "موجودی انبار:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = product.formatStockDisplay() + " (ضریب هر ${product.primaryUnit} = ${NumberFormatUtils.formatQuantity(product.conversionRate)} ${product.secondaryUnit})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) AccountingCreditRed else MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Multi-tier prices
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "قیمت مصرف‌کننده (خرده):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = NumberFormatUtils.formatPrice(product.retailPrice),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "قیمت عمده‌فروشی:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = NumberFormatUtils.formatPrice(product.wholesalePrice),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onConfirm = { code, barcode, name, cat, pUnit, sUnit, rate, stock, minStock, buy, retail, whole, partner, desc ->
                viewModel.addProduct(
                    code, barcode, name, cat, pUnit, sUnit, rate, stock, minStock, buy, retail, whole, partner, desc
                ) {
                    Toast.makeText(context, "کالای جدید با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                    showAddProductDialog = false
                }
            }
        )
    }

    // Stock Cardex Dialog
    selectedCardexProduct?.let { product ->
        StockCardexDialog(
            product = product,
            movements = cardexMovements,
            onDismiss = { viewModel.selectProductForCardex(null) }
        )
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        barcode: String,
        name: String,
        category: String,
        primaryUnit: String,
        secondaryUnit: String,
        conversionRate: Double,
        initialStockSecondary: Double,
        minStock: Double,
        buyPrice: Long,
        retailPrice: Long,
        wholesalePrice: Long,
        partnerPrice: Long,
        description: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var primaryUnit by remember { mutableStateOf("کارتن") }
    var secondaryUnit by remember { mutableStateOf("عدد") }
    var conversionRateText by remember { mutableStateOf("12") }
    var initialStockText by remember { mutableStateOf("24") }
    var minStockText by remember { mutableStateOf("12") }
    var buyPriceText by remember { mutableStateOf("100000") }
    var retailPriceText by remember { mutableStateOf("140000") }
    var wholesalePriceText by remember { mutableStateOf("125000") }
    var partnerPriceText by remember { mutableStateOf("120000") }
    var description by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعریف کالا و قیمت‌گذاری چندسطحی",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام کالا") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد کالا") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("بارکد") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("دسته‌بندی (مثال: لبنیات، غلات)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "واحدهای سنجش و ضریب تبدیل",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = primaryUnit,
                        onValueChange = { primaryUnit = it },
                        label = { Text("واحد اصلی (کارتن)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = secondaryUnit,
                        onValueChange = { secondaryUnit = it },
                        label = { Text("واحد فرعی (عدد)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = conversionRateText,
                        onValueChange = { conversionRateText = it },
                        label = { Text("ضریب تبدیل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "قیمت‌گذاری چندسطحی (به تومان برای واحد فرعی)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = buyPriceText,
                        onValueChange = { buyPriceText = it },
                        label = { Text("قیمت خرید") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = retailPriceText,
                        onValueChange = { retailPriceText = it },
                        label = { Text("قیمت خرده") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wholesalePriceText,
                        onValueChange = { wholesalePriceText = it },
                        label = { Text("قیمت عمده") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = partnerPriceText,
                        onValueChange = { partnerPriceText = it },
                        label = { Text("قیمت همکار") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = initialStockText,
                        onValueChange = { initialStockText = it },
                        label = { Text("موجودی اولیه ($secondaryUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it },
                        label = { Text("نقطه سفارش ($secondaryUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val rate = conversionRateText.toDoubleOrNull() ?: 1.0
                        val stock = initialStockText.toDoubleOrNull() ?: 0.0
                        val minStock = minStockText.toDoubleOrNull() ?: 10.0
                        val buy = buyPriceText.toLongOrNull() ?: 0L
                        val retail = retailPriceText.toLongOrNull() ?: 0L
                        val whole = wholesalePriceText.toLongOrNull() ?: 0L
                        val partner = partnerPriceText.toLongOrNull() ?: 0L

                        onConfirm(
                            code, barcode, name, category, primaryUnit, secondaryUnit,
                            rate, stock, minStock, buy, retail, whole, partner, description
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_product_button")
                ) {
                    Text("ثبت و ذخیره کالا در انبار", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockCardexDialog(
    product: ProductEntity,
    movements: List<com.example.data.model.InventoryMovementEntity>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "کاردکس کالا: ${product.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "موجودی لحظه‌ای: ${product.formatStockDisplay()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (movements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("گردشی برای این کالا ثبت نشده است.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movements) { mov ->
                            val isEntry = mov.quantitySecondary > 0
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isEntry) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${mov.documentType} - ${mov.referenceNumber}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "تاریخ: ${mov.jalaliDate} | ${mov.description}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = (if (isEntry) "+" else "") + NumberFormatUtils.formatQuantity(mov.quantitySecondary) + " ${product.secondaryUnit}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isEntry) AccountingDebitGreen else AccountingCreditRed
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
