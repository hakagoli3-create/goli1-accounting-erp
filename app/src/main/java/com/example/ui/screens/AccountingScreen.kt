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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import com.example.data.model.AccountGroupEntity
import com.example.data.model.AppSecurityAndSettingsManager
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.ProfitLossStatement
import com.example.ui.viewmodel.VoucherItemDraft
import com.example.util.ExportHelper
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel
) {
    val context = LocalContext.current
    val securityManager = remember { AppSecurityAndSettingsManager.getInstance(context) }
    val currentRole by securityManager.currentRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("درخت حساب‌ها", "اسناد مالی", "ثبت سند دوبل", "تراز آزمایشی", "سود و زیان")

    val groups by viewModel.allGroups.collectAsStateWithLifecycle()
    val generals by viewModel.allGenerals.collectAsStateWithLifecycle()
    val subsidiaries by viewModel.allSubsidiaries.collectAsStateWithLifecycle()
    val journalEntries by viewModel.allJournalEntries.collectAsStateWithLifecycle()
    val trialBalance by viewModel.trialBalance.collectAsStateWithLifecycle()
    val profitLoss by viewModel.profitLoss.collectAsStateWithLifecycle()

    val draftDesc by viewModel.draftVoucherDescription.collectAsStateWithLifecycle()
    val draftDate by viewModel.draftVoucherDate.collectAsStateWithLifecycle()
    val draftItems by viewModel.draftItems.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                ErpTopAppBar(
                    title = "حسابداری مالی چندسطحی و سود/زیان",
                    subtitle = "کدینگ ۳ سطحی، ثبت اسناد دوبل، تراز معین و گزارش سود و زیان رسمی"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("accounting_tab_$index")
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
                0 -> AccountTreeContent(groups, generals, subsidiaries)
                1 -> JournalEntriesContent(
                    entries = journalEntries,
                    onExportPdf = {
                        val headers = listOf("شماره سند", "تاریخ سند", "شرح سند", "وضعیت")
                        val rows = journalEntries.map {
                            listOf(it.voucherNumber.toString(), it.jalaliDate, it.description, if (it.isBalanced) "تراز" else "ناهمخوان")
                        }
                        val f = ExportHelper.exportReportPdf(context, "دفتر روزنامه و اسناد حسابداری", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "application/pdf", "اسناد حسابداری PDF")
                    },
                    onExportCsv = {
                        val headers = listOf("شماره سند", "تاریخ", "شرح", "وضعیت")
                        val rows = journalEntries.map {
                            listOf(it.voucherNumber.toString(), it.jalaliDate, it.description, it.status)
                        }
                        val f = ExportHelper.exportReportCsv(context, "فهرست اسناد روزنامه", headers, rows)
                        if (f != null) ExportHelper.shareFile(context, f, "text/csv", "خروجی اکسل اسناد")
                    }
                )
                2 -> CreateJournalVoucherContent(
                    subsidiaries = subsidiaries,
                    generals = generals,
                    draftDesc = draftDesc,
                    draftDate = draftDate,
                    draftItems = draftItems,
                    onDescChange = { viewModel.updateDraftDescription(it) },
                    onDateChange = { viewModel.updateDraftDate(it) },
                    onAddItem = { viewModel.addDraftItem(it) },
                    onRemoveItem = { viewModel.removeDraftItem(it) },
                    onSubmit = {
                        viewModel.submitJournalVoucher {
                            selectedTab = 1
                        }
                    }
                )
                3 -> TrialBalanceContent(
                    trialBalance = trialBalance,
                    onExportPdf = { viewModel.exportTrialBalancePdf(context) },
                    onExportCsv = { viewModel.exportTrialBalanceCsv(context) }
                )
                4 -> ProfitLossContent(
                    statement = profitLoss,
                    canViewProfit = currentRole.canViewProfit,
                    onExportPdf = { viewModel.exportProfitLossPdf(context) },
                    onExportCsv = { viewModel.exportProfitLossCsv(context) }
                )
            }
        }
    }
}

@Composable
private fun ProfitLossContent(
    statement: ProfitLossStatement,
    canViewProfit: Boolean,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    if (!canViewProfit) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccountingCreditRed, modifier = Modifier.size(48.dp))
                    Text("محدودیت دسترسی نقش کاربر", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("مشاهده صورت سود و زیان مالی تنها برای نقش «مدیر کل» مجاز است. لطفاً از بخش تنظیمات نقش خود را تغییر دهید.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("صورت سود و زیان جامع دوره", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("درآمدهای عملیاتی و حاصل از فروش:", fontSize = 13.sp)
                        Text(NumberFormatUtils.formatPrice(statement.totalRevenue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccountingDebitGreen)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("بهای تمام شده کالای فروش رفته (COGS):", fontSize = 13.sp)
                        Text("(${NumberFormatUtils.formatPrice(statement.costOfGoodsSold)})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccountingCreditRed)
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("سود (زیان) ناخالص:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = NumberFormatUtils.formatPrice(statement.grossProfit),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (statement.grossProfit >= 0) AccountingDebitGreen else AccountingCreditRed
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("هزینه‌های عمومی، اداری و توزیع:", fontSize = 13.sp)
                        Text("(${NumberFormatUtils.formatPrice(statement.operatingExpenses)})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AccountingCreditRed)
                    }

                    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("سود (زیان) خالص نهایی دوره:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = NumberFormatUtils.formatPrice(statement.netProfit),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (statement.netProfit >= 0) AccountingDebitGreen else AccountingCreditRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountTreeContent(
    groups: List<AccountGroupEntity>,
    generals: List<GeneralAccountEntity>,
    subsidiaries: List<SubsidiaryAccountEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(groups) { group ->
            var expanded by remember { mutableStateOf(true) }
            val groupGenerals = generals.filter { it.groupCode == group.code }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PersianGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${group.code} - ${group.title}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = if (group.nature == "DEBIT") "ماهیت: بدهکار" else "ماهیت: بستانکار",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (group.nature == "DEBIT") AccountingDebitGreen else AccountingCreditRed
                        )
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        groupGenerals.forEach { gen ->
                            val genSubs = subsidiaries.filter { it.generalCode == gen.code }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${gen.code} - ${gen.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    genSubs.forEach { sub ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp, horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "↳ ${sub.code} - ${sub.title}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = if (sub.nature == "DEBIT") "بدهکار" else "بستانکار",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
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
    }
}

@Composable
private fun JournalEntriesContent(
    entries: List<JournalEntryEntity>,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("هیچ سند حسابداری هنوز ثبت نشده است.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("دفتر روزنامه اسناد (${entries.size} سند)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("PDF اسناد", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Text("اکسل", fontSize = 11.sp)
                    }
                }
            }
        }

        items(entries) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سند شماره ${entry.voucherNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = entry.jalaliDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (entry.isBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (entry.isBalanced) AccountingDebitGreen else AccountingCreditRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (entry.isBalanced) "سند تراز شده" else "عدم تراز",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (entry.isBalanced) AccountingDebitGreen else AccountingCreditRed
                            )
                        }
                        Text(
                            text = "وضعیت: ${if (entry.status == "FINAL") "قطعی" else "پیش‌نویس"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateJournalVoucherContent(
    subsidiaries: List<SubsidiaryAccountEntity>,
    generals: List<GeneralAccountEntity>,
    draftDesc: String,
    draftDate: String,
    draftItems: List<VoucherItemDraft>,
    onDescChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAddItem: (VoucherItemDraft) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSub by remember { mutableStateOf<SubsidiaryAccountEntity?>(null) }
    var itemDesc by remember { mutableStateOf("") }
    var itemDebitText by remember { mutableStateOf("") }
    var itemCreditText by remember { mutableStateOf("") }

    val totalDebit = draftItems.sumOf { it.debit }
    val totalCredit = draftItems.sumOf { it.credit }
    val isBalanced = totalDebit == totalCredit && draftItems.isNotEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "اطلاعات کلی سند حسابداری",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draftDesc,
                        onValueChange = onDescChange,
                        label = { Text("شرح کلی سند (مثلاً: ثبت هزینه کرایه یا خرید ملزومات)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_description_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draftDate,
                        onValueChange = onDateChange,
                        label = { Text("تاریخ سند (هجری شمسی)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "افزودن سطر آرتیکل جدید",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSub?.let { "${it.code} - ${it.title}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("انتخاب حساب معین") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            subsidiaries.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.code} - ${sub.title}") },
                                    onClick = {
                                        selectedSub = sub
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = itemDesc,
                        onValueChange = { itemDesc = it },
                        label = { Text("شرح سطر") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemDebitText,
                            onValueChange = {
                                itemDebitText = it
                                if (it.isNotBlank()) itemCreditText = ""
                            },
                            label = { Text("بدهکار (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("debit_input")
                        )
                        OutlinedTextField(
                            value = itemCreditText,
                            onValueChange = {
                                itemCreditText = it
                                if (it.isNotBlank()) itemDebitText = ""
                            },
                            label = { Text("بستانکار (تومان)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("credit_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val sub = selectedSub ?: return@Button
                            val debit = itemDebitText.toLongOrNull() ?: 0L
                            val credit = itemCreditText.toLongOrNull() ?: 0L
                            if (debit == 0L && credit == 0L) return@Button

                            onAddItem(
                                VoucherItemDraft(
                                    subsidiaryCode = sub.code,
                                    subsidiaryTitle = sub.title,
                                    generalCode = sub.generalCode,
                                    description = itemDesc.ifBlank { draftDesc },
                                    debit = debit,
                                    credit = credit
                                )
                            )
                            itemDesc = ""
                            itemDebitText = ""
                            itemCreditText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_item_to_voucher_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("افزودن سطر به سند")
                    }
                }
            }
        }

        items(draftItems.size) { index ->
            val item = draftItems[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                            text = "${item.subsidiaryCode} - ${item.subsidiaryTitle}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            if (item.debit > 0) {
                                Text(
                                    text = "بدهکار: ${NumberFormatUtils.formatPrice(item.debit)}",
                                    color = AccountingDebitGreen,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (item.credit > 0) {
                                Text(
                                    text = "بستانکار: ${NumberFormatUtils.formatPrice(item.credit)}",
                                    color = AccountingCreditRed,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف سطر",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanced) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مجموع بدهکار:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = NumberFormatUtils.formatPrice(totalDebit),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccountingDebitGreen)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مجموع بستانکار:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = NumberFormatUtils.formatPrice(totalCredit),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AccountingCreditRed)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "وضعیت تراز دوبل:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = if (isBalanced) "سند تراز است ✓" else "اختلاف: ${NumberFormatUtils.formatPrice(Math.abs(totalDebit - totalCredit))}",
                            color = if (isBalanced) AccountingDebitGreen else AccountingCreditRed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onSubmit,
                enabled = isBalanced && draftItems.size >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_voucher_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "ثبت نهایی و صدور سند حسابداری")
            }
        }
    }
}

@Composable
private fun TrialBalanceContent(
    trialBalance: List<TrialBalanceRow>,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تراز آزمایشی چهارستونی معین",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("PDF تراز", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold)
                    ) {
                        Text("اکسل", fontSize = 11.sp)
                    }
                }
            }
        }

        items(trialBalance) { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${row.subsidiaryCode} - ${row.title}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = row.groupTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "کل: ${row.generalTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "گردش بدهکار: ${NumberFormatUtils.formatPrice(row.totalDebit)}", fontSize = 11.sp)
                            Text(text = "گردش بستانکار: ${NumberFormatUtils.formatPrice(row.totalCredit)}", fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            if (row.balanceDebit > 0) {
                                Text(
                                    text = "مانده بدهکار: ${NumberFormatUtils.formatPrice(row.balanceDebit)}",
                                    color = AccountingDebitGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            if (row.balanceCredit > 0) {
                                Text(
                                    text = "مانده بستانکار: ${NumberFormatUtils.formatPrice(row.balanceCredit)}",
                                    color = AccountingCreditRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
