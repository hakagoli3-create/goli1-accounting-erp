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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.AlertWarningOrange
import com.example.ui.viewmodel.TreasuryViewModel
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen(
    viewModel: TreasuryViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("چک‌های صیادی", "صندوق‌ها", "حساب‌های بانکی و پوز")

    val cheques by viewModel.allCheques.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showAddChequeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "خزانه‌داری، بانک و مدیریت چک‌ها",
                    subtitle = "کنترل نقدینگی، پایانه‌های پوز و هشدارهای سررسید صیادی"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("treasury_tab_$index")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddChequeDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_cheque_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت چک صیادی", tint = Color.White)
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
                0 -> ChequesContent(
                    cheques = cheques,
                    onUpdateStatus = { cheque, status -> viewModel.updateChequeStatus(cheque, status) }
                )
                1 -> CashDesksContent(cashDesks = cashDesks)
                2 -> BankAccountsContent(bankAccounts = bankAccounts)
            }
        }
    }

    if (showAddChequeDialog) {
        AddChequeDialog(
            onDismiss = { showAddChequeDialog = false },
            onConfirm = { number, sayad, type, party, bank, branch, amount, issue, due, notes ->
                viewModel.addCheque(number, sayad, type, party, bank, branch, amount, issue, due, notes) {
                    showAddChequeDialog = false
                }
            }
        )
    }
}

@Composable
private fun ChequesContent(
    cheques: List<ChequeEntity>,
    onUpdateStatus: (ChequeEntity, String) -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") } // "ALL", "RECEIVABLE", "PAYABLE", "DUE_ALERTS"

    val displayedCheques = cheques.filter { ch ->
        when (filterType) {
            "RECEIVABLE" -> ch.type == "RECEIVABLE"
            "PAYABLE" -> ch.type == "PAYABLE"
            "DUE_ALERTS" -> ch.status == "NORMAL" && (ch.isOverdue() || ch.isDueToday() || ch.daysUntilDue() in 0..7)
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterChips = listOf(
                "ALL" to "همه چک‌ها",
                "RECEIVABLE" to "دریافتی",
                "PAYABLE" to "پرداختی",
                "DUE_ALERTS" to "هشدار سررسید"
            )
            filterChips.forEach { (key, label) ->
                val isSelected = filterType == key
                Card(
                    modifier = Modifier.clickable { filterType = key },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (displayedCheques.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("هیچ چکی با این فیلتر یافت نشد.")
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(displayedCheques) { cheque ->
                var menuExpanded by remember { mutableStateOf(false) }

                val isOverdue = cheque.isOverdue()
                val isDueToday = cheque.isDueToday()
                val daysUntil = cheque.daysUntilDue()

                val cardBg = when {
                    cheque.status == "PASSED" -> Color(0xFFF1F8E9)
                    cheque.status == "BOUNCED" -> Color(0xFFFFEBEE)
                    isOverdue -> Color(0xFFFFEBEE)
                    isDueToday || daysUntil in 0..3 -> Color(0xFFFFF3E0)
                    else -> MaterialTheme.colorScheme.surface
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (cheque.type == "RECEIVABLE") AccountingDebitGreen.copy(alpha = 0.15f) else AccountingCreditRed.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (cheque.type == "RECEIVABLE") AccountingDebitGreen else AccountingCreditRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (cheque.type == "RECEIVABLE") "چک دریافتی از: ${cheque.partyName}" else "چک پرداختی به: ${cheque.partyName}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${cheque.bankName} (${cheque.branch}) | ش: ${cheque.chequeNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "منو")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("وصول شد (پاس شده)") },
                                        onClick = {
                                            onUpdateStatus(cheque, "PASSED")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("برگشت خورده") },
                                        onClick = {
                                            onUpdateStatus(cheque, "BOUNCED")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("خرج شده / واگذار شده") },
                                        onClick = {
                                            onUpdateStatus(cheque, "TRANSFERRED")
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("در جریان وصول (عادی)") },
                                        onClick = {
                                            onUpdateStatus(cheque, "NORMAL")
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sayad number
                        Text(
                            text = "شناسه ۱۶ رقمی صیاد: ${NumberFormatUtils.toPersianDigits(cheque.sayadNumber)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "تاریخ سررسید: ${cheque.dueDateJalali}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                val dueStatusText = when {
                                    cheque.status == "PASSED" -> "وصول شده"
                                    cheque.status == "BOUNCED" -> "برگشت خورده"
                                    isOverdue -> "سررسید گذشته (معوق)"
                                    isDueToday -> "سررسید امروز!"
                                    daysUntil in 1..7 -> "$daysUntil روز مانده به سررسید"
                                    else -> "در جریان وصول"
                                }
                                val statusColor = when {
                                    cheque.status == "PASSED" -> AccountingDebitGreen
                                    cheque.status == "BOUNCED" || isOverdue -> AccountingCreditRed
                                    isDueToday || daysUntil in 1..7 -> AlertWarningOrange
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Text(
                                    text = dueStatusText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Text(
                                text = NumberFormatUtils.formatPrice(cheque.amount),
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
}

@Composable
private fun CashDesksContent(
    cashDesks: List<CashDeskEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cashDesks) { desk ->
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
                        Column {
                            Text(
                                text = desk.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "کد: ${desk.code} | مسئول: ${desk.keeperName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "موجودی نقدی صندوق:")
                        Text(
                            text = NumberFormatUtils.formatPrice(desk.currentBalance),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AccountingDebitGreen
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BankAccountsContent(
    bankAccounts: List<BankAccountEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bankAccounts) { acc ->
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
                        Column {
                            Text(
                                text = "${acc.bankName} - ${acc.branch}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "شماره حساب: ${acc.accountNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1976D2).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF1976D2))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "شماره کارت: ${acc.cardNumber}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "شماره شبا: ${acc.shebaNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (acc.hasPos) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = AccountingDebitGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "دستگاه پوز متصل: ${acc.posTerminalId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccountingDebitGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "موجودی حساب بانکی:")
                        Text(
                            text = NumberFormatUtils.formatPrice(acc.currentBalance),
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

@Composable
fun AddChequeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        number: String,
        sayad: String,
        type: String,
        party: String,
        bank: String,
        branch: String,
        amount: Long,
        issueDate: String,
        dueDate: String,
        notes: String
    ) -> Unit
) {
    var chequeNumber by remember { mutableStateOf("") }
    var sayadNumber by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("RECEIVABLE") } // RECEIVABLE or PAYABLE
    var partyName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("بانک ملت") }
    var branch by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(JalaliDate.now().toStandardString()) }
    var dueDate by remember { mutableStateOf(JalaliDate.now().addDays(30).toStandardString()) }
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
                        text = "ثبت چک صیادی جدید",
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
                        onClick = { type = "RECEIVABLE" },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (type == "RECEIVABLE") AccountingDebitGreen else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("چک دریافتی", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { type = "PAYABLE" },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (type == "PAYABLE") AccountingCreditRed else Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("چک پرداختی", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sayadNumber,
                    onValueChange = { sayadNumber = it },
                    label = { Text("شناسه ۱۶ رقمی صیاد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sayad_number_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (type == "RECEIVABLE") "صاحب چک / مشتری" else "ذینفع / تامین‌کننده") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chequeNumber,
                        onValueChange = { chequeNumber = it },
                        label = { Text("شماره چک") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("بانک") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ چک به تومان") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cheque_amount_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = issueDate,
                        onValueChange = { issueDate = it },
                        label = { Text("تاریخ صدور") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("تاریخ سررسید") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull() ?: 0L
                        if (partyName.isBlank() || amount <= 0L) return@Button
                        onConfirm(chequeNumber, sayadNumber, type, partyName, bankName, branch, amount, issueDate, dueDate, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_cheque_button")
                ) {
                    Text("ثبت در دفتر چک و خزانه‌داری", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
