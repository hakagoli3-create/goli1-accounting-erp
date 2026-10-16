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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.InvoiceSettlementDialog
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val pricingMode by viewModel.pricingMode.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()

    val previewReceipt by viewModel.previewReceiptText.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    var barcodeInput by remember { mutableStateOf("") }
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var showSettlementDialog by remember { mutableStateOf(false) }

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAlertMessage()
        }
    }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxable = if (subtotal > discount) subtotal - discount else 0L
    val tax = if (applyTax) (taxable * 0.10).toLong() else 0L
    val grandTotal = taxable + tax

    Scaffold(
        topBar = {
            ErpTopAppBar(
                title = "صندوق فروشگاهی لمسی POS",
                subtitle = "اسکن بارکد، انتخاب سریع کالا، قیمت‌گذاری تکی/عمده و تسویه چندگانه"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Barcode Input & Pricing Mode Switch
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcodeInput,
                        onValueChange = { barcodeInput = it },
                        placeholder = { Text("بارکد کالا را وارد یا اسکن کنید...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_barcode_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (barcodeInput.isNotBlank()) {
                                viewModel.scanBarcode(barcodeInput)
                                barcodeInput = ""
                            }
                        },
                        modifier = Modifier.testTag("pos_scan_barcode_button")
                    ) {
                        Text("ثبت")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mode indicator / switch
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = pricingMode == "RETAIL",
                            onClick = { viewModel.setPricingMode("RETAIL") },
                            label = { Text("تکی", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = pricingMode == "WHOLESALE",
                            onClick = { viewModel.setPricingMode("WHOLESALE") },
                            label = { Text("عمده", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Main POS Split Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column (Touch Product Grid)
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxSize()
                ) {
                    Text(
                        text = "اقلام فروشگاه (${if (pricingMode == "WHOLESALE") "قیمت عمده" else "قیمت مصرف‌کننده"})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(products) { product ->
                            val displayPrice = if (pricingMode == "WHOLESALE") product.wholesalePrice else product.retailPrice
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addToCart(product, isPrimary = false)
                                    }
                                    .testTag("pos_product_item_${product.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = NumberFormatUtils.formatPrice(displayPrice),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "موجودی: ${product.formatStockDisplay()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (product.currentStockInSecondary <= product.minStockThreshold) AccountingCreditRed else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Column (Cart & Checkout Panel)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Cart Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "سبد فروش (${NumberFormatUtils.toPersianDigits(cartItems.size.toString())})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (cartItems.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearCart() }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "خالی کردن سبد", tint = Color.Gray)
                                }
                            }
                        }

                        // Customer Selection Dropdown
                        ExposedDropdownMenuBox(
                            expanded = customerMenuExpanded,
                            onExpandedChange = { customerMenuExpanded = !customerMenuExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedParty?.name ?: "مشتری نقدی (عمومی)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("طرف حساب") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = customerMenuExpanded,
                                onDismissRequest = { customerMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("مشتری نقدی (عمومی)") },
                                    onClick = {
                                        viewModel.selectParty(null)
                                        customerMenuExpanded = false
                                    }
                                )
                                customers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { Text("${customer.name} (مانده: ${NumberFormatUtils.formatPrice(customer.currentBalance)})") },
                                        onClick = {
                                            viewModel.selectParty(customer)
                                            customerMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cart Items List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(cartItems.size) { index ->
                                val item = cartItems[index]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = item.product.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { viewModel.updateCartItemQuantity(index, item.quantity - 1.0) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "کاهش", modifier = Modifier.size(16.dp))
                                                }
                                                Text(
                                                    text = NumberFormatUtils.formatQuantity(item.quantity) + " " + item.unitName,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.updateCartItemQuantity(index, item.quantity + 1.0) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزایش", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(
                                                text = NumberFormatUtils.formatPrice(item.totalPrice),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Tax & Totals Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "مالیات ارزش افزوده (۱۰٪):", style = MaterialTheme.typography.labelSmall)
                            Switch(
                                checked = applyTax,
                                onCheckedChange = { viewModel.toggleTax(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "مبلغ کل قابل پرداخت:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = NumberFormatUtils.formatPrice(grandTotal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Cash / POS or Advanced Settlement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (cartItems.isEmpty()) {
                                        Toast.makeText(context, "سبد خالی است", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.checkoutAdvanced(
                                        paymentType = "CASH",
                                        cashAmount = grandTotal
                                    ) {
                                        Toast.makeText(context, "تسویه نقدی با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("pos_fast_cash_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نقد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (cartItems.isEmpty()) {
                                        Toast.makeText(context, "سبد خالی است", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.checkoutAdvanced(
                                        paymentType = "POS",
                                        posAmount = grandTotal
                                    ) {
                                        Toast.makeText(context, "تسویه کارتخوان با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("pos_fast_pos_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پوز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (cartItems.isEmpty()) {
                                        Toast.makeText(context, "سبد خالی است", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    showSettlementDialog = true
                                },
                                modifier = Modifier.weight(1.3f).testTag("pos_advanced_settlement_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سایر / ترکیبی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettlementDialog) {
        InvoiceSettlementDialog(
            totalAmount = grandTotal,
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            selectedParty = selectedParty,
            onDismiss = { showSettlementDialog = false },
            onConfirmSettlement = { paymentType, cash, pos, cheque, credit, deskId, bankId, chNum, sayad, due, bankName, notes ->
                viewModel.checkoutAdvanced(
                    paymentType = paymentType,
                    cashAmount = cash,
                    posAmount = pos,
                    chequeAmount = cheque,
                    creditAmount = credit,
                    cashDeskId = deskId,
                    bankAccountId = bankId,
                    chequeNumber = chNum,
                    sayadNumber = sayad,
                    chequeDueDate = due,
                    chequeBankName = bankName,
                    notes = notes
                ) {
                    showSettlementDialog = false
                    Toast.makeText(context, "فاکتور با موفقیت تسویه و صادر شد.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    previewReceipt?.let { text ->
        ReceiptPreviewDialog(
            receiptText = text,
            onDismiss = { viewModel.closeReceiptPreview() }
        )
    }
}
