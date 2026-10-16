package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.InvoiceWithItems
import com.example.data.model.ProductEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.components.ReceiptPreviewDialog
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.InvoiceViewModel
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("فاکتورهای ثبت شده", "صدور فاکتور عمده‌فروشی")

    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedParty by viewModel.selectedParty.collectAsStateWithLifecycle()
    val discount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val applyTax by viewModel.applyTax.collectAsStateWithLifecycle()
    val previewReceipt by viewModel.previewReceiptText.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    LaunchedEffect(alertMessage) {
        alertMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAlertMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "فاکتورهای رسمی و عمده‌فروشی",
                    subtitle = "کنترل سقف اعتبار مشتری، چاپ حرارتی و محاسبه اقلام کارتن/عدد"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("invoice_tab_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> InvoiceListContent(
                    invoices = invoices,
                    onPrintInvoice = { viewModel.showReceiptForInvoice(it) }
                )
                1 -> WholesaleInvoiceEditorContent(
                    products = products,
                    customers = customers,
                    cartItems = cartItems,
                    selectedParty = selectedParty,
                    discount = discount,
                    applyTax = applyTax,
                    onSelectParty = { viewModel.selectParty(it) },
                    onAddToCart = { p, isPrimary -> viewModel.addToCart(p, isPrimary = isPrimary, wholesale = true) },
                    onUpdateQuantity = { idx, q -> viewModel.updateCartItemQuantity(idx, q) },
                    onDiscountChange = { viewModel.setDiscount(it) },
                    onToggleTax = { viewModel.toggleTax(it) },
                    onCheckout = { paymentType ->
                        viewModel.checkout(paymentType) {
                            Toast.makeText(context, "فاکتور رسمی با موفقیت ثبت و تایید شد.", Toast.LENGTH_SHORT).show()
                            selectedTab = 0
                        }
                    }
                )
            }
        }
    }

    previewReceipt?.let { text ->
        ReceiptPreviewDialog(
            receiptText = text,
            onDismiss = { viewModel.closeReceiptPreview() }
        )
    }
}

@Composable
private fun InvoiceListContent(
    invoices: List<InvoiceWithItems>,
    onPrintInvoice: (InvoiceWithItems) -> Unit
) {
    if (invoices.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("هیچ فاکتوری تا کنون ثبت نشده است.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(invoices) { inv ->
            val invoice = inv.invoice
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
                            Text(
                                text = "فاکتور ${invoice.invoiceNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${invoice.jalaliDate})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onPrintInvoice(inv) },
                            modifier = Modifier.testTag("print_invoice_${invoice.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "چاپ حرارتی",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "خریدار: ${invoice.partyName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اقلام: ${NumberFormatUtils.toPersianDigits(inv.items.size.toString())} ردیف | انبار: ${invoice.warehouseName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = NumberFormatUtils.formatPrice(invoice.totalAmount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WholesaleInvoiceEditorContent(
    products: List<ProductEntity>,
    customers: List<com.example.data.model.PartyEntity>,
    cartItems: List<com.example.ui.viewmodel.CartItem>,
    selectedParty: com.example.data.model.PartyEntity?,
    discount: Long,
    applyTax: Boolean,
    onSelectParty: (com.example.data.model.PartyEntity?) -> Unit,
    onAddToCart: (ProductEntity, Boolean) -> Unit,
    onUpdateQuantity: (Int, Double) -> Unit,
    onDiscountChange: (Long) -> Unit,
    onToggleTax: (Boolean) -> Unit,
    onCheckout: (String) -> Unit
) {
    var customerMenuExpanded by remember { mutableStateOf(false) }
    var productMenuExpanded by remember { mutableStateOf(false) }
    var selectedProductToAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var usePrimaryUnit by remember { mutableStateOf(true) } // کارتن vs عدد
    var discountText by remember { mutableStateOf(if (discount > 0) discount.toString() else "") }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val taxable = if (subtotal > discount) subtotal - discount else 0L
    val tax = if (applyTax) (taxable * 0.10).toLong() else 0L
    val grandTotal = taxable + tax

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Customer Selection & Credit Limit Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طرف حساب و شرایط اعتباری",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = customerMenuExpanded,
                        onExpandedChange = { customerMenuExpanded = !customerMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedParty?.let { "${it.name} (${it.code})" } ?: "انتخاب مشتری...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("مشتری") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_customer_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = customerMenuExpanded,
                            onDismissRequest = { customerMenuExpanded = false }
                        ) {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text("${customer.name} - سقف اعتبار: ${NumberFormatUtils.formatPrice(customer.creditLimit)}") },
                                    onClick = {
                                        onSelectParty(customer)
                                        customerMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedParty?.let { party ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val isOverLimit = party.isCreditLimitExceeded() || (party.creditLimit > 0 && (party.currentBalance + grandTotal) > party.creditLimit)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverLimit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "سقف اعتبار ریالی: ${NumberFormatUtils.formatPrice(party.creditLimit)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "مانده بدهی فعلی: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                if (isOverLimit) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "هشدار: این سفارش از سقف مجاز اعتبار مشتری فراتر می‌رود!",
                                            color = AccountingCreditRed,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Wholesale Item Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "افزودن قلم کالای عمده",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = productMenuExpanded,
                        onExpandedChange = { productMenuExpanded = !productMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProductToAdd?.let { "${it.name} - فی عمده: ${NumberFormatUtils.formatPrice(it.wholesalePrice)}" } ?: "انتخاب کالا...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("کالا") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = productMenuExpanded,
                            onDismissRequest = { productMenuExpanded = false }
                        ) {
                            products.forEach { prod ->
                                DropdownMenuItem(
                                    text = { Text("${prod.name} (موجودی: ${prod.formatStockDisplay()})") },
                                    onClick = {
                                        selectedProductToAdd = prod
                                        productMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    selectedProductToAdd?.let { prod ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "واحد سنجش فروش:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { usePrimaryUnit = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (usePrimaryUnit) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                                ) {
                                    Text("واحد اصلی (${prod.primaryUnit})", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { usePrimaryUnit = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!usePrimaryUnit) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                                ) {
                                    Text("واحد فرعی (${prod.secondaryUnit})", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onAddToCart(prod, usePrimaryUnit)
                                selectedProductToAdd = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_wholesale_item_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن به اقلام فاکتور")
                        }
                    }
                }
            }
        }

        // Cart items table
        items(cartItems.size) { index ->
            val item = cartItems[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${NumberFormatUtils.formatQuantity(item.quantity)} ${item.unitName} × ${NumberFormatUtils.formatPrice(item.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = NumberFormatUtils.formatPrice(item.totalPrice),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { onUpdateQuantity(index, 0.0) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Gray)
                    }
                }
            }
        }

        // Totals & Settlement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "جمع کل اقلام:")
                        Text(text = NumberFormatUtils.formatPrice(subtotal), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = {
                            discountText = it
                            onDiscountChange(it.toLongOrNull() ?: 0L)
                        },
                        label = { Text("مبلغ تخفیف (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "مالیات بر ارزش افزوده (۱۰٪)")
                        Switch(checked = applyTax, onCheckedChange = onToggleTax)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مبلغ نهایی فاکتور:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = NumberFormatUtils.formatPrice(grandTotal),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onCheckout("CREDIT") },
                            enabled = cartItems.isNotEmpty() && selectedParty != null,
                            colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_wholesale_credit_button")
                        ) {
                            Text("ثبت نسیه (اعتباری)", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onCheckout("CHEQUE") },
                            enabled = cartItems.isNotEmpty() && selectedParty != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_wholesale_cheque_button")
                        ) {
                            Text("ثبت با چک صیادی", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
