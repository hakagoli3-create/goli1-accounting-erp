package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.PartyEntity
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceSettlementDialog(
    totalAmount: Long,
    cashDesks: List<CashDeskEntity>,
    bankAccounts: List<BankAccountEntity>,
    selectedParty: PartyEntity?,
    onDismiss: () -> Unit,
    onConfirmSettlement: (
        paymentType: String,
        cashAmount: Long,
        posAmount: Long,
        chequeAmount: Long,
        creditAmount: Long,
        cashDeskId: Long,
        bankAccountId: Long,
        chequeNumber: String,
        sayadNumber: String,
        chequeDueDate: String,
        chequeBankName: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf("CASH") } // "CASH", "POS", "CREDIT", "CHEQUE", "MIXED"

    var selectedCashDeskId by remember {
        mutableLongStateOf(cashDesks.firstOrNull()?.id ?: 1L)
    }
    var selectedBankAccountId by remember {
        mutableLongStateOf(bankAccounts.firstOrNull()?.id ?: 1L)
    }

    var cashInput by remember { mutableStateOf(totalAmount.toString()) }
    var posInput by remember { mutableStateOf("0") }
    var chequeInput by remember { mutableStateOf("0") }
    var creditInput by remember { mutableStateOf("0") }

    var chequeNumber by remember { mutableStateOf("") }
    var sayadNumber by remember { mutableStateOf("") }
    var chequeDueDate by remember { mutableStateOf(JalaliDate.now().addDays(30).toStandardString()) }
    var chequeBankName by remember { mutableStateOf("بانک ملی ایران") }
    var notes by remember { mutableStateOf("") }

    var cashDeskExpanded by remember { mutableStateOf(false) }
    var bankAccountExpanded by remember { mutableStateOf(false) }

    fun updateAmountsForMode(newMode: String) {
        mode = newMode
        when (newMode) {
            "CASH" -> {
                cashInput = totalAmount.toString()
                posInput = "0"
                chequeInput = "0"
                creditInput = "0"
            }
            "POS" -> {
                cashInput = "0"
                posInput = totalAmount.toString()
                chequeInput = "0"
                creditInput = "0"
            }
            "CREDIT" -> {
                cashInput = "0"
                posInput = "0"
                chequeInput = "0"
                creditInput = totalAmount.toString()
            }
            "CHEQUE" -> {
                cashInput = "0"
                posInput = "0"
                chequeInput = totalAmount.toString()
                creditInput = "0"
            }
            "MIXED" -> {
                // Keep current inputs or split evenly
            }
        }
    }

    val cashVal = cashInput.toLongOrNull() ?: 0L
    val posVal = posInput.toLongOrNull() ?: 0L
    val chequeVal = chequeInput.toLongOrNull() ?: 0L
    val creditVal = creditInput.toLongOrNull() ?: 0L
    val totalPaid = cashVal + posVal + chequeVal + creditVal
    val remainingBalance = totalAmount - totalPaid

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تسویه و پرداخت فاکتور",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "مبلغ قابل پرداخت: ${NumberFormatUtils.formatPrice(totalAmount)}",
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

                Spacer(modifier = Modifier.height(14.dp))

                // Settlement Type Chips
                Text(
                    text = "شیوه تسویه حساب:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        "CASH" to "نقدی (صندوق)",
                        "POS" to "پوز / بانک",
                        "CHEQUE" to "چک صیادی",
                        "CREDIT" to "نسیه"
                    )
                    modes.forEach { (m, label) ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { updateAmountsForMode(m) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("settlement_chip_$m")
                        )
                    }
                }

                FilterChip(
                    selected = mode == "MIXED",
                    onClick = { updateAmountsForMode("MIXED") },
                    label = { Text("تسویه ترکیبی چندگانه (نقد + پوز + چک + نسیه)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("settlement_chip_MIXED")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Inputs for Cash / Bank / Cheque / Credit
                if (mode == "MIXED" || mode == "CASH") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("واریز نقدی به صندوق", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (mode == "MIXED") {
                                OutlinedTextField(
                                    value = cashInput,
                                    onValueChange = { cashInput = it },
                                    label = { Text("مبلغ نقد (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            // Cash Desk selector
                            ExposedDropdownMenuBox(
                                expanded = cashDeskExpanded,
                                onExpandedChange = { cashDeskExpanded = it }
                            ) {
                                val currentDesk = cashDesks.find { it.id == selectedCashDeskId } ?: cashDesks.firstOrNull()
                                OutlinedTextField(
                                    value = currentDesk?.name ?: "انتخاب صندوق",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("صندوق مقصد") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cashDeskExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = cashDeskExpanded,
                                    onDismissRequest = { cashDeskExpanded = false }
                                ) {
                                    cashDesks.forEach { desk ->
                                        DropdownMenuItem(
                                            text = { Text("${desk.name} (مانده: ${NumberFormatUtils.formatPrice(desk.currentBalance)})") },
                                            onClick = {
                                                selectedCashDeskId = desk.id
                                                cashDeskExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (mode == "MIXED" || mode == "POS") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("پرداخت از طریق کارتخوان / بانک", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (mode == "MIXED") {
                                OutlinedTextField(
                                    value = posInput,
                                    onValueChange = { posInput = it },
                                    label = { Text("مبلغ پوز / بانک (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            // Bank Account selector
                            ExposedDropdownMenuBox(
                                expanded = bankAccountExpanded,
                                onExpandedChange = { bankAccountExpanded = it }
                            ) {
                                val currentBank = bankAccounts.find { it.id == selectedBankAccountId } ?: bankAccounts.firstOrNull()
                                OutlinedTextField(
                                    value = currentBank?.bankName ?: "انتخاب حساب بانکی",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("حساب بانکی / پایانه پوز") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankAccountExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = bankAccountExpanded,
                                    onDismissRequest = { bankAccountExpanded = false }
                                ) {
                                    bankAccounts.forEach { bank ->
                                        DropdownMenuItem(
                                            text = { Text("${bank.bankName} - کارت: ${bank.cardNumber} (مانده: ${NumberFormatUtils.formatPrice(bank.currentBalance)})") },
                                            onClick = {
                                                selectedBankAccountId = bank.id
                                                bankAccountExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (mode == "MIXED" || mode == "CHEQUE") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("اطلاعات چک صیادی", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (mode == "MIXED") {
                                OutlinedTextField(
                                    value = chequeInput,
                                    onValueChange = { chequeInput = it },
                                    label = { Text("مبلغ چک (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = chequeNumber,
                                    onValueChange = { chequeNumber = it },
                                    label = { Text("شماره سریال چک") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = sayadNumber,
                                    onValueChange = { sayadNumber = it },
                                    label = { Text("شناسه ۱۶ رقمی صیادی") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = chequeDueDate,
                                    onValueChange = { chequeDueDate = it },
                                    label = { Text("سررسید (۱۴۰۵/۰۲/۱۵)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = chequeBankName,
                                    onValueChange = { chequeBankName = it },
                                    label = { Text("نام بانک صادرکننده") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (mode == "MIXED" || mode == "CREDIT") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedParty != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("فروش نسیه و مانده حساب مشتری", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedParty == null) {
                                Text(
                                    text = "توجه: برای ثبت نسیه، حتماً باید یک مشتری از لیست CRM انتخاب شده باشد.",
                                    color = AccountingCreditRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "مشتری: ${selectedParty.name} | سقف اعتبار: ${NumberFormatUtils.formatPrice(selectedParty.creditLimit)} | بدهی فعلی: ${NumberFormatUtils.formatPrice(selectedParty.currentBalance)}",
                                    fontSize = 11.sp
                                )
                            }
                            if (mode == "MIXED") {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = creditInput,
                                    onValueChange = { creditInput = it },
                                    label = { Text("مبلغ نسیه (تومان)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Balance Summary
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingBalance == 0L) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع ارقام وارد شده:", fontSize = 12.sp)
                            Text(NumberFormatUtils.formatPrice(totalPaid), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مانده تسویه نشده:", fontSize = 12.sp)
                            Text(
                                text = if (remainingBalance == 0L) "تسویه کامل (تراز)" else NumberFormatUtils.formatPrice(remainingBalance),
                                color = if (remainingBalance == 0L) AccountingDebitGreen else AccountingCreditRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (mode == "CREDIT" || (mode == "MIXED" && creditVal > 0)) {
                            if (selectedParty == null) {
                                Toast.makeText(context, "لطفاً ابتدا مشتری را برای فروش نسیه انتخاب کنید.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!selectedParty.canAcceptCredit(creditVal)) {
                                Toast.makeText(context, "مبلغ نسیه از سقف اعتبار مشتری فراتر است!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        if (remainingBalance != 0L && mode == "MIXED") {
                            Toast.makeText(context, "مجموع پرداختی با مبلغ فاکتور برابر نیست!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onConfirmSettlement(
                            mode,
                            cashVal,
                            posVal,
                            chequeVal,
                            creditVal,
                            selectedCashDeskId,
                            selectedBankAccountId,
                            chequeNumber,
                            sayadNumber,
                            chequeDueDate,
                            chequeBankName,
                            notes
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_settlement_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت نهایی و صدور فاکتور")
                }
            }
        }
    }
}
