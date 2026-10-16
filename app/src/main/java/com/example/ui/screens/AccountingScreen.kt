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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
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
import com.example.ui.components.ErpTopAppBar
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.VoucherItemDraft
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("درخت حساب‌ها", "اسناد مالی", "ثبت سند دوبل", "تراز آزمایشی")

    val groups by viewModel.allGroups.collectAsStateWithLifecycle()
    val generals by viewModel.allGenerals.collectAsStateWithLifecycle()
    val subsidiaries by viewModel.allSubsidiaries.collectAsStateWithLifecycle()
    val journalEntries by viewModel.allJournalEntries.collectAsStateWithLifecycle()
    val trialBalance by viewModel.trialBalance.collectAsStateWithLifecycle()

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
                    title = "حسابداری مالی چندسطحی",
                    subtitle = "کدینگ، ثبت اسناد دوبل، تراز آزمایشی و دفاتر"
                )
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
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
                1 -> JournalEntriesContent(journalEntries)
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
                            selectedTab = 1 // Switch to entries tab after success
                        }
                    }
                )
                3 -> TrialBalanceContent(trialBalance)
            }
        }
    }
}

@Composable
private fun AccountTreeContent(
    groups: List<com.example.data.model.AccountGroupEntity>,
    generals: List<com.example.data.model.GeneralAccountEntity>,
    subsidiaries: List<com.example.data.model.SubsidiaryAccountEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups) { group ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = NumberFormatUtils.toPersianDigits(group.code.toString()),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "گروه: ${group.title}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = if (group.nature == "DEBIT") "ماهیت بدهکار" else "ماهیت بستانکار",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (group.nature == "DEBIT") AccountingDebitGreen else AccountingCreditRed
                        )
                    }

                    val groupGenerals = generals.filter { it.groupCode == group.code }
                    groupGenerals.forEach { gen ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "کل [${NumberFormatUtils.toPersianDigits(gen.code.toString())}]: ${gen.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                val genSubs = subsidiaries.filter { it.generalCode == gen.code }
                                genSubs.forEach { sub ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp, start = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "• معین [${NumberFormatUtils.toPersianDigits(sub.code.toString())}]: ${sub.title}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun JournalEntriesContent(
    entries: List<com.example.data.model.JournalEntryEntity>
) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("هیچ سند حسابداری ثبت نشده است.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "سند شماره ${NumberFormatUtils.toPersianDigits(entry.voucherNumber.toString())}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(AccountingDebitGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "تراز شده",
                                    color = AccountingDebitGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تاریخ: ${entry.jalaliDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "ثبت نهایی",
                        tint = AccountingDebitGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateJournalVoucherContent(
    subsidiaries: List<com.example.data.model.SubsidiaryAccountEntity>,
    generals: List<com.example.data.model.GeneralAccountEntity>,
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
    var selectedSub by remember { mutableStateOf<com.example.data.model.SubsidiaryAccountEntity?>(null) }
    var itemDesc by remember { mutableStateOf("") }
    var itemDebitText by remember { mutableStateOf("") }
    var itemCreditText by remember { mutableStateOf("") }

    val totalDebit = draftItems.sumOf { it.debit }
    val totalCredit = draftItems.sumOf { it.credit }
    val isBalanced = draftItems.isNotEmpty() && totalDebit == totalCredit

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مشخصات عمومی سند",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftDate,
                        onValueChange = onDateChange,
                        label = { Text("تاریخ سند شمسی (مثال: ۱۴۰۴/۰۷/۲۵)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_date_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftDesc,
                        onValueChange = onDescChange,
                        label = { Text("شرح کلی سند حسابداری") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_desc_input")
                    )
                }
            }
        }

        // Add line item card
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

        // Items list
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (item.debit > 0) {
                                Text(
                                    text = "بدهکار: ${NumberFormatUtils.formatPrice(item.debit)}",
                                    color = AccountingDebitGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            if (item.credit > 0) {
                                Text(
                                    text = "بستانکار: ${NumberFormatUtils.formatPrice(item.credit)}",
                                    color = AccountingCreditRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    IconButton(onClick = { onRemoveItem(index) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف سطر", tint = Color.Gray)
                    }
                }
            }
        }

        // Summary Balance Status & Submit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanced) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مجموع بدهکار:", fontWeight = FontWeight.Bold)
                        Text(
                            text = NumberFormatUtils.formatPrice(totalDebit),
                            color = AccountingDebitGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "مجموع بستانکار:", fontWeight = FontWeight.Bold)
                        Text(
                            text = NumberFormatUtils.formatPrice(totalCredit),
                            color = AccountingCreditRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isBalanced) AccountingDebitGreen else AccountingCreditRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBalanced) "سند کاملاً تراز است." else "سند تراز نیست (اختلاف: ${NumberFormatUtils.formatPrice(Math.abs(totalDebit - totalCredit))})",
                                color = if (isBalanced) AccountingDebitGreen else AccountingCreditRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onSubmit,
                        enabled = isBalanced && draftItems.size >= 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_voucher_button")
                    ) {
                        Text("ثبت قطعی سند حسابداری", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrialBalanceContent(
    trialBalance: List<com.example.data.model.TrialBalanceRow>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "تراز آزمایشی مانده حساب‌های معین",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(trialBalance) { row ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${row.subsidiaryCode} - ${row.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = row.groupTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مانده بدهکار: ${NumberFormatUtils.formatPrice(row.balanceDebit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (row.balanceDebit > 0) AccountingDebitGreen else Color.Gray
                        )
                        Text(
                            text = "مانده بستانکار: ${NumberFormatUtils.formatPrice(row.balanceCredit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (row.balanceCredit > 0) AccountingCreditRed else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
