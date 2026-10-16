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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.viewmodel.PartyViewModel
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    viewModel: PartyViewModel
) {
    val context = LocalContext.current
    val parties by viewModel.filteredParties.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedPartyForStatement by viewModel.selectedParty.collectAsStateWithLifecycle()
    val partyInvoices by viewModel.partyInvoices.collectAsStateWithLifecycle()

    var showAddPartyDialog by remember { mutableStateOf(false) }

    val tabs = listOf("مشتریان", "تامین‌کنندگان", "تجاوز از سقف اعتبار")

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "مدیریت مشتریان و طرف‌های حساب (CRM)",
                    subtitle = "کنترل سقف اعتبار ریالی، صورت‌حساب مشتری و اطلاعات تماس"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.setSelectedTab(index) },
                            text = { Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("crm_tab_$index")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPartyDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_party_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت مشتری جدید", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("جستجوی نام مشتری، شرکت، موبایل یا کد...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_party_input")
            )

            if (parties.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("هیچ طرف حسابی یافت نشد.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(parties) { party ->
                        val isOverLimit = party.isCreditLimitExceeded()
                        val creditUsagePercent = if (party.creditLimit > 0) {
                            (party.currentBalance.toFloat() / party.creditLimit.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectPartyForStatement(party) }
                                .testTag("party_item_${party.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverLimit) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = party.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (party.companyName.isNotBlank()) {
                                            Text(
                                                text = party.companyName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isOverLimit) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(AccountingCreditRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "بیش از سقف اعتبار",
                                                color = AccountingCreditRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${party.mobile}  |  ${party.phone}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Balance and Credit Limit details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "مانده حساب: " + NumberFormatUtils.formatPrice(party.currentBalance),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (party.currentBalance > 0) AccountingDebitGreen else AccountingCreditRed
                                        )
                                    )
                                    Text(
                                        text = "سقف اعتبار: " + if (party.creditLimit > 0) NumberFormatUtils.formatPrice(party.creditLimit) else "نامحدود",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                }

                                if (party.creditLimit > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { creditUsagePercent },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = if (isOverLimit) AccountingCreditRed else MaterialTheme.colorScheme.primary,
                                        trackColor = Color.LightGray.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Party Statement / Customer Ledger Modal
    selectedPartyForStatement?.let { party ->
        CustomerLedgerDialog(
            party = party,
            invoices = partyInvoices,
            onDismiss = { viewModel.selectPartyForStatement(null) }
        )
    }

    // Add Party Dialog
    if (showAddPartyDialog) {
        AddPartyDialog(
            onDismiss = { showAddPartyDialog = false },
            onConfirm = { code, name, company, mobile, phone, address, national, type, credit, notes ->
                viewModel.addParty(code, name, company, mobile, phone, address, national, type, credit, notes) {
                    Toast.makeText(context, "طرف حساب با موفقیت ثبت شد", Toast.LENGTH_SHORT).show()
                    showAddPartyDialog = false
                }
            }
        )
    }
}

@Composable
fun CustomerLedgerDialog(
    party: PartyEntity,
    invoices: List<InvoiceEntity>,
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
                            text = "صورت‌حساب: ${party.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "مانده بدهکاری: ${NumberFormatUtils.formatPrice(party.currentBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("فاکتوری برای این طرف حساب ثبت نشده است.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(invoices) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                                            text = "فاکتور ${inv.invoiceNumber} (${inv.jalaliDate})",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "تسویه: ${inv.paymentType} | انبار: ${inv.warehouseName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = NumberFormatUtils.formatPrice(inv.totalAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(
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
        }
    }
}

@Composable
fun AddPartyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        name: String,
        company: String,
        mobile: String,
        phone: String,
        address: String,
        national: String,
        type: String,
        creditLimit: Long,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var national by remember { mutableStateOf("") }
    var creditText by remember { mutableStateOf("30000000") }
    var partyType by remember { mutableStateOf("CUSTOMER") }
    var notes by remember { mutableStateOf("") }

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
                        text = "ثبت مشتری / تامین‌کننده جدید",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { partyType = "CUSTOMER" },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (partyType == "CUSTOMER") MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("مشتری", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { partyType = "SUPPLIER" },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (partyType == "SUPPLIER") MaterialTheme.colorScheme.primary else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تامین‌کننده", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_name_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("نام فروشگاه / شرکت") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("شماره موبایل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("تلفن ثابت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = creditText,
                    onValueChange = { creditText = it },
                    label = { Text("سقف اعتبار ریالی مجاز (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("party_credit_limit_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("آدرس") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) return@Button
                        val credit = creditText.toLongOrNull() ?: 0L
                        onConfirm("", name, company, mobile, phone, address, national, partyType, credit, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_party_button")
                ) {
                    Text("ثبت در دفتر مخاطبین و مشتریان", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
