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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.TreasuryViewModel
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryScreen(
    viewModel: TreasuryViewModel
) {
    val context = LocalContext.current
    val securityManager = remember { AppSecurityAndSettingsManager.getInstance(context) }
    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("چک‌های صیادی", "صندوق‌ها", "بانک‌ها و پوز", "عملیات و گزارش‌ها")

    val cheques by viewModel.allCheques.collectAsStateWithLifecycle()
    val cashDesks by viewModel.allCashDesks.collectAsStateWithLifecycle()
    val bankAccounts by viewModel.allBankAccounts.collectAsStateWithLifecycle()
    val totalCash by viewModel.totalCashBalance.collectAsStateWithLifecycle()
    val totalBank by viewModel.totalBankBalance.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showAddChequeDialog by remember { mutableStateOf(false) }
    var showAddCashDeskDialog by remember { mutableStateOf(false) }
    var showAddBankDialog by remember { mutableStateOf(false) }
    var editingCashDesk by remember { mutableStateOf<CashDeskEntity?>(null) }
    var editingBankAccount by remember { mutableStateOf<BankAccountEntity?>(null) }

    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showCapitalDialog by remember { mutableStateOf(false) }

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
                    title = "خزانه‌داری، بانک و نقدینگی",
                    subtitle = "مدیریت حساب‌های بانکی، پوز، صندوق‌ها، چک صیادی و ثبت درآمد/هزینه"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("treasury_tab_$index")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { showAddChequeDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_cheque_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت چک صیادی", tint = Color.White)
                }
                1 -> FloatingActionButton(
                    onClick = { showAddCashDeskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_cashdesk_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن صندوق", tint = Color.White)
                }
                2 -> FloatingActionButton(
                    onClick = { showAddBankDialog = true },
                    containerColor = PersianGold,
                    modifier = Modifier.testTag("add_bank_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن حساب بانکی", tint = Color.White)
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
                1 -> CashDesksContent(
                    cashDesks = cashDesks,
                    canManage = currentRole.canAccessTreasuryBalances,
                    onEdit = { editingCashDesk = it },
                    onDelete = { viewModel.deleteCashDesk(it) }
                )
                2 -> BankAccountsContent(
                    bankAccounts = bankAccounts,
                    canManage = currentRole.canAccessTreasuryBalances,
                    onEdit = { editingBankAccount = it },
                    onDelete = { viewModel.deleteBankAccount(it) }
                )
                3 -> TreasuryOperationsContent(
                    totalCash = totalCash ?: 0L,
                    totalBank = totalBank ?: 0L,
                    onOpenIncome = { showIncomeDialog = true },
                    onOpenExpense = { showExpenseDialog = true },
                    onOpenTransfer = { showTransferDialog = true },
                    onOpenCapital = { showCapitalDialog = true },
                    onExportPdf = { viewModel.exportTreasuryPdf(context) },
                    onExportCsv = { viewModel.exportTreasuryCsv(context) }
                )
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

    if (showAddCashDeskDialog) {
        AddOrEditCashDeskDialog(
            existing = null,
            onDismiss = { showAddCashDeskDialog = false },
            onConfirm = { name, code, keeper, balance ->
                viewModel.addCashDesk(name, code, keeper, balance) {
                    showAddCashDeskDialog = false
                }
            }
        )
    }

    editingCashDesk?.let { desk ->
        AddOrEditCashDeskDialog(
            existing = desk,
            onDismiss = { editingCashDesk = null },
            onConfirm = { name, code, keeper, balance ->
                viewModel.updateCashDesk(desk.copy(name = name, code = code, keeperName = keeper, currentBalance = balance)) {
                    editingCashDesk = null
                }
            }
        )
    }

    if (showAddBankDialog) {
        AddOrEditBankDialog(
            existing = null,
            onDismiss = { showAddBankDialog = false },
            onConfirm = { bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId ->
                viewModel.addBankAccount(bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId) {
                    showAddBankDialog = false
                }
            }
        )
    }

    editingBankAccount?.let { bank ->
        AddOrEditBankDialog(
            existing = bank,
            onDismiss = { editingBankAccount = null },
            onConfirm = { bankName, accNum, cardNum, sheba, branch, balance, hasPos, posId ->
                viewModel.updateBankAccount(
                    bank.copy(
                        bankName = bankName,
                        accountNumber = accNum,
                        cardNumber = cardNum,
                        shebaNumber = sheba,
                        branch = branch,
                        currentBalance = balance,
                        hasPos = hasPos,
                        posTerminalId = posId
                    )
                ) {
                    editingBankAccount = null
                }
            }
        )
    }

    if (showIncomeDialog) {
        TreasuryActionDialog(
            title = "ثبت دریافت وجه / درآمد جدید",
            actionLabel = "ثبت درآمد و صدور سند",
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            categories = listOf("فروش خدمات و متفرقه", "سود سپرده بانکی", "تسویه دستی مشتری", "سایر درآمدهای عملیاتی"),
            onDismiss = { showIncomeDialog = false },
            onConfirm = { title, amount, destType, targetId, category, notes ->
                viewModel.registerIncome(title, amount, destType, targetId, category, notes) {
                    showIncomeDialog = false
                }
            }
        )
    }

    if (showExpenseDialog) {
        TreasuryActionDialog(
            title = "ثبت پرداخت هزینه جدید",
            actionLabel = "پرداخت هزینه و صدور سند",
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            categories = listOf("حقوق و دستمزد پرسنل", "کرایه و حمل کالا", "ملزومات و قبوض اداری", "بسته‌بندی و تبلیغات", "تعمیرات و نگهداری"),
            onDismiss = { showExpenseDialog = false },
            onConfirm = { title, amount, srcType, sourceId, category, notes ->
                viewModel.registerExpense(title, amount, srcType, sourceId, category, notes) {
                    showExpenseDialog = false
                }
            }
        )
    }

    if (showTransferDialog) {
        TransferFundsDialog(
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromType, fromId, toType, toId, amount, notes ->
                viewModel.transferFunds(fromType, fromId, toType, toId, amount, notes) {
                    showTransferDialog = false
                }
            }
        )
    }

    if (showCapitalDialog) {
        CapitalInvestmentDialog(
            cashDesks = cashDesks,
            bankAccounts = bankAccounts,
            onDismiss = { showCapitalDialog = false },
            onConfirm = { investor, amount, destType, targetId ->
                viewModel.registerCapitalInvestment(investor, amount, destType, targetId) {
                    showCapitalDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChequesContent(
    cheques: List<ChequeEntity>,
    onUpdateStatus: (ChequeEntity, String) -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") }

    val displayedCheques = cheques.filter { ch ->
        when (filterType) {
            "RECEIVABLE" -> ch.type == "RECEIVABLE"
            "PAYABLE" -> ch.type == "PAYABLE"
            "DUE_ALERTS" -> ch.status == "NORMAL" && (ch.isOverdue() || ch.isDueToday() || ch.daysUntilDue() in 0..7)
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { filterType = "ALL" },
                label = { Text("همه", fontSize = 11.sp, fontWeight = if (filterType == "ALL") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = filterType == "RECEIVABLE",
                onClick = { filterType = "RECEIVABLE" },
                label = { Text("دریافتی", fontSize = 11.sp, fontWeight = if (filterType == "RECEIVABLE") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = filterType == "PAYABLE",
                onClick = { filterType = "PAYABLE" },
                label = { Text("پرداختی", fontSize = 11.sp, fontWeight = if (filterType == "PAYABLE") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = filterType == "DUE_ALERTS",
                onClick = { filterType = "DUE_ALERTS" },
                label = { Text("هشدار سررسید", fontSize = 11.sp, fontWeight = if (filterType == "DUE_ALERTS") FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.weight(1.2f)
            )
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
                    shape = RoundedCornerShape(14.dp),
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
                                val tagBg = if (cheque.type == "RECEIVABLE") AccountingDebitGreen else PersianGold
                                Text(
                                    text = if (cheque.type == "RECEIVABLE") "دریافتی" else "پرداختی",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(tagBg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${cheque.bankName} (${cheque.chequeNumber})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "عملیات")
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
                                        text = { Text("در جریان وصول (عادی)") },
                                        onClick = {
                                            onUpdateStatus(cheque, "NORMAL")
                                            menuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "طرف حساب / صاحب چک: ${cheque.partyName}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "شناسه ۱۶ رقمی صیادی: ${cheque.sayadNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سررسید: ${cheque.dueDateJalali}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
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
    cashDesks: List<CashDeskEntity>,
    canManage: Boolean,
    onEdit: (CashDeskEntity) -> Unit,
    onDelete: (CashDeskEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
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
                        if (canManage) {
                            Row {
                                IconButton(onClick = { onEdit(desk) }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(desk) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
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
    bankAccounts: List<BankAccountEntity>,
    canManage: Boolean,
    onEdit: (BankAccountEntity) -> Unit,
    onDelete: (BankAccountEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
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
                        if (canManage) {
                            Row {
                                IconButton(onClick = { onEdit(acc) }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(acc) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = AccountingCreditRed)
                                }
                            }
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
private fun TreasuryOperationsContent(
    totalCash: Long,
    totalBank: Long,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenCapital: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("کل موجودی صندوق‌ها", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(NumberFormatUtils.formatPrice(totalCash), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AccountingDebitGreen)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("کل موجودی بانک‌ها", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(NumberFormatUtils.formatPrice(totalBank), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "عملیات نقدی و دریافت/پرداخت خزانه",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenIncome,
                        modifier = Modifier.weight(1f).testTag("treasury_income_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingDebitGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت درآمد", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenExpense,
                        modifier = Modifier.weight(1f).testTag("treasury_expense_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccountingCreditRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت هزینه", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenTransfer,
                        modifier = Modifier.weight(1f).testTag("treasury_transfer_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("انتقال وجه", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenCapital,
                        modifier = Modifier.weight(1f).testTag("treasury_capital_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("سرمایه شرکا", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "خروجی چندگانه گزارش خزانه و نقدینگی",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تولید فایل رسمی PDF استاندارد و خروجی فایل Excel (CSV سازگار با اکسل) از گردش و مانده حساب‌ها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier.weight(1f).testTag("export_treasury_pdf_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("دانلود PDF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f).testTag("export_treasury_csv_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی اکسل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrEditCashDeskDialog(
    existing: CashDeskEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, keeper: String, balance: Long) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var code by remember { mutableStateOf(existing?.code ?: "CSH-${(100..999).random()}") }
    var keeper by remember { mutableStateOf(existing?.keeperName ?: "") }
    var balanceText by remember { mutableStateOf(existing?.currentBalance?.toString() ?: "0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (existing == null) "تعریف صندوق جدید" else "ویرایش صندوق", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام صندوق") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("کد صندوق") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = keeper, onValueChange = { keeper = it }, label = { Text("نام مسئول / صندوق‌دار") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = balanceText, onValueChange = { balanceText = it }, label = { Text("موجودی اولیه (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(name, code, keeper, balanceText.toLongOrNull() ?: 0L)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ذخیره صندوق")
                }
            }
        }
    }
}

@Composable
fun AddOrEditBankDialog(
    existing: BankAccountEntity?,
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, accNum: String, cardNum: String, sheba: String, branch: String, balance: Long, hasPos: Boolean, posId: String) -> Unit
) {
    var bankName by remember { mutableStateOf(existing?.bankName ?: "بانک ملت") }
    var accountNumber by remember { mutableStateOf(existing?.accountNumber ?: "") }
    var cardNumber by remember { mutableStateOf(existing?.cardNumber ?: "") }
    var shebaNumber by remember { mutableStateOf(existing?.shebaNumber ?: "") }
    var branch by remember { mutableStateOf(existing?.branch ?: "شعبه مرکزی") }
    var balanceText by remember { mutableStateOf(existing?.currentBalance?.toString() ?: "0") }
    var hasPos by remember { mutableStateOf(existing?.hasPos ?: true) }
    var posTerminalId by remember { mutableStateOf(existing?.posTerminalId ?: "POS-101") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (existing == null) "تعریف حساب بانکی و پوز" else "ویرایش حساب بانکی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("نام بانک") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = accountNumber, onValueChange = { accountNumber = it }, label = { Text("شماره حساب") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cardNumber, onValueChange = { cardNumber = it }, label = { Text("شماره ۱۶ رقمی کارت") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = shebaNumber, onValueChange = { shebaNumber = it }, label = { Text("شماره شبا (IR...)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("نام شعبه") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = balanceText, onValueChange = { balanceText = it }, label = { Text("موجودی فعلی (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("دارای پایانه پوز متصل:")
                    Switch(checked = hasPos, onCheckedChange = { hasPos = it })
                }
                if (hasPos) {
                    OutlinedTextField(value = posTerminalId, onValueChange = { posTerminalId = it }, label = { Text("کد ترمینال پوز") }, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
                            onConfirm(bankName, accountNumber, cardNumber, shebaNumber, branch, balanceText.toLongOrNull() ?: 0L, hasPos, posTerminalId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ذخیره حساب بانکی")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasuryActionDialog(
    title: String,
    actionLabel: String,
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Long, destType: String, targetId: Long, category: String, notes: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("CASH") }
    var selectedTargetId by remember { mutableStateOf(cashDesks.firstOrNull()?.id ?: 1L) }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("شرح / عنوان عملیات") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("مبلغ به تومان") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetType == "CASH",
                        onClick = {
                            targetType = "CASH"
                            selectedTargetId = cashDesks.firstOrNull()?.id ?: 1L
                        },
                        label = { Text("صندوق نقدی") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = targetType == "BANK",
                        onClick = {
                            targetType = "BANK"
                            selectedTargetId = bankAccounts.firstOrNull()?.id ?: 1L
                        },
                        label = { Text("حساب بانکی") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (targetType == "CASH") {
                    Text("انتخاب صندوق:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    cashDesks.forEach { c ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedTargetId = c.id }.padding(vertical = 4.dp)) {
                            Text(if (selectedTargetId == c.id) "● " else "○ ", color = MaterialTheme.colorScheme.primary)
                            Text("${c.name} (مانده: ${NumberFormatUtils.formatPrice(c.currentBalance)})", fontSize = 12.sp)
                        }
                    }
                } else {
                    Text("انتخاب حساب بانکی:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    bankAccounts.forEach { b ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedTargetId = b.id }.padding(vertical = 4.dp)) {
                            Text(if (selectedTargetId == b.id) "● " else "○ ", color = MaterialTheme.colorScheme.primary)
                            Text("${b.bankName} - کارت: ${b.cardNumber}", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات تکمیلی") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (subject.isNotBlank() && amt > 0) {
                            onConfirm(subject, amt, targetType, selectedTargetId, selectedCategory, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferFundsDialog(
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fromType: String, fromId: Long, toType: String, toId: Long, amount: Long, notes: String) -> Unit
) {
    var fromType by remember { mutableStateOf("CASH") }
    var fromId by remember { mutableStateOf(cashDesks.firstOrNull()?.id ?: 1L) }
    var toType by remember { mutableStateOf("BANK") }
    var toId by remember { mutableStateOf(bankAccounts.firstOrNull()?.id ?: 1L) }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("انتقال وجه بین صندوق و حساب بانکی", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Text("مبدا انتقال:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = fromType == "CASH", onClick = { fromType = "CASH"; fromId = cashDesks.firstOrNull()?.id ?: 1L }, label = { Text("از صندوق") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = fromType == "BANK", onClick = { fromType = "BANK"; fromId = bankAccounts.firstOrNull()?.id ?: 1L }, label = { Text("از بانک") }, modifier = Modifier.weight(1f))
                }

                Text("مقصد انتقال:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = toType == "CASH", onClick = { toType = "CASH"; toId = cashDesks.firstOrNull()?.id ?: 1L }, label = { Text("به صندوق") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = toType == "BANK", onClick = { toType = "BANK"; toId = bankAccounts.firstOrNull()?.id ?: 1L }, label = { Text("به بانک") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("مبلغ انتقال (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("بابت / شماره پیگیری") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (amt > 0) {
                            onConfirm(fromType, fromId, toType, toId, amt, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تایید انتقال وجه و صدور سند")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapitalInvestmentDialog(
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (investorName: String, amount: Long, destType: String, targetId: Long) -> Unit
) {
    var investor by remember { mutableStateOf("آقای گلی (موسس)") }
    var amountText by remember { mutableStateOf("") }
    var destType by remember { mutableStateOf("BANK") }
    var targetId by remember { mutableStateOf(bankAccounts.firstOrNull()?.id ?: 1L) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ثبت سرمایه اولیه و آورده نقدی شرکا", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                OutlinedTextField(value = investor, onValueChange = { investor = it }, label = { Text("نام شریک / موسس") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("مبلغ سرمایه نقدی (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = destType == "BANK", onClick = { destType = "BANK"; targetId = bankAccounts.firstOrNull()?.id ?: 1L }, label = { Text("واریز به بانک") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = destType == "CASH", onClick = { destType = "CASH"; targetId = cashDesks.firstOrNull()?.id ?: 1L }, label = { Text("ورود به صندوق") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (investor.isNotBlank() && amt > 0) {
                            onConfirm(investor, amt, destType, targetId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ثبت سرمایه در حقوق صاحبان سهام")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var type by remember { mutableStateOf("RECEIVABLE") }
    var partyName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("بانک ملت") }
    var branch by remember { mutableStateOf("مرکزی") }
    var amountText by remember { mutableStateOf("") }
    var issueDate by remember { mutableStateOf(JalaliDate.now().toStandardString()) }
    var dueDate by remember { mutableStateOf(JalaliDate.now().addDays(30).toStandardString()) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ثبت چک صیادی جدید", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == "RECEIVABLE", onClick = { type = "RECEIVABLE" }, label = { Text("چک دریافتی") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = type == "PAYABLE", onClick = { type = "PAYABLE" }, label = { Text("چک پرداختی") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = chequeNumber, onValueChange = { chequeNumber = it }, label = { Text("شماره سریال چک") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sayadNumber, onValueChange = { sayadNumber = it }, label = { Text("کد ۱۶ رقمی صیادی") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = partyName, onValueChange = { partyName = it }, label = { Text("صاحب چک / طرف حساب") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("نام بانک صادرکننده") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("مبلغ چک به تومان") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("تاریخ سررسید (۱۴۰۵/۰۲/۱۵)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("توضیحات و بابت") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val amt = amountText.toLongOrNull() ?: 0L
                        if (chequeNumber.isNotBlank() && amt > 0) {
                            onConfirm(chequeNumber, sayadNumber, type, partyName, bankName, branch, amt, issueDate, dueDate, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ثبت چک در سامانه صیاد")
                }
            }
        }
    }
}
