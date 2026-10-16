package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.repository.AccountingRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TreasuryViewModel(
    private val repository: TreasuryRepository,
    private val accountingRepo: AccountingRepository
) : ViewModel() {

    val allCashDesks: StateFlow<List<CashDeskEntity>> = repository.allCashDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankAccounts: StateFlow<List<BankAccountEntity>> = repository.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCheques: StateFlow<List<ChequeEntity>> = repository.allCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receivableCheques: StateFlow<List<ChequeEntity>> = repository.receivableCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payableCheques: StateFlow<List<ChequeEntity>> = repository.payableCheques
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val sevenDaysAhead = JalaliDate.now().addDays(7).toEpochMillis()
    val dueAlertCheques: StateFlow<List<ChequeEntity>> = repository.getDueOrOverdueCheques(sevenDaysAhead)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCashBalance: StateFlow<Long?> = repository.totalCashBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalBankBalance: StateFlow<Long?> = repository.totalBankBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun addCheque(
        chequeNumber: String,
        sayadNumber: String,
        type: String,
        partyName: String,
        bankName: String,
        branch: String,
        amount: Long,
        issueDate: String,
        dueDate: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val dueJalali = JalaliDate.parse(dueDate)
            val cheque = ChequeEntity(
                chequeNumber = chequeNumber,
                sayadNumber = sayadNumber,
                type = type,
                partyName = partyName,
                bankName = bankName,
                branch = branch,
                amount = amount,
                issueDateJalali = issueDate,
                dueDateJalali = dueDate,
                dueDateEpochMillis = dueJalali.toEpochMillis(),
                status = "NORMAL",
                notes = notes
            )
            repository.insertCheque(cheque)
            _statusMessage.value = "چک صیادی با موفقیت در سامانه ثبت شد."
            onSuccess()
        }
    }

    fun updateChequeStatus(cheque: ChequeEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateCheque(cheque.copy(status = newStatus))
            _statusMessage.value = "وضعیت چک به روزرسانی شد."
        }
    }

    fun addCashDesk(name: String, code: String, keeper: String, initialBalance: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.insertCashDesk(
                CashDeskEntity(
                    code = code,
                    name = name,
                    keeperName = keeper,
                    currentBalance = initialBalance
                )
            )
            _statusMessage.value = "صندوق جدید $name اضافه شد."
            onSuccess()
        }
    }

    fun updateCashDesk(cashDesk: CashDeskEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateCashDesk(cashDesk)
            _statusMessage.value = "اطلاعات صندوق به روزرسانی شد."
            onSuccess()
        }
    }

    fun deleteCashDesk(cashDesk: CashDeskEntity) {
        viewModelScope.launch {
            repository.deleteCashDesk(cashDesk)
            _statusMessage.value = "صندوق ${cashDesk.name} حذف شد."
        }
    }

    fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardNumber: String,
        sheba: String,
        branch: String,
        initialBalance: Long,
        hasPos: Boolean,
        posTerminalId: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insertBankAccount(
                BankAccountEntity(
                    bankName = bankName,
                    accountNumber = accountNumber,
                    cardNumber = cardNumber,
                    shebaNumber = sheba,
                    branch = branch,
                    currentBalance = initialBalance,
                    hasPos = hasPos,
                    posTerminalId = posTerminalId
                )
            )
            _statusMessage.value = "حساب بانکی $bankName اضافه شد."
            onSuccess()
        }
    }

    fun updateBankAccount(account: BankAccountEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateBankAccount(account)
            _statusMessage.value = "اطلاعات حساب بانکی به روزرسانی شد."
            onSuccess()
        }
    }

    fun deleteBankAccount(account: BankAccountEntity) {
        viewModelScope.launch {
            repository.deleteBankAccount(account)
            _statusMessage.value = "حساب بانکی ${account.bankName} حذف شد."
        }
    }

    /**
     * ثبت عملیات دریافت وجه / درآمد
     */
    fun registerIncome(
        title: String,
        amount: Long,
        destinationType: String, // "CASH" or "BANK"
        targetId: Long,
        category: String, // "فروش خدمات", "سایر درآمدها", "دریافت از مشتری"
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ درآمد باید بیشتر از صفر باشد."
                return@launch
            }
            if (destinationType == "CASH") {
                repository.updateCashBalance(targetId, amount)
            } else {
                repository.updateBankBalance(targetId, amount)
            }

            // سند دوبل حسابداری
            val today = JalaliDate.now()
            val subCode = if (destinationType == "CASH") 10101 else 10102
            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = subCode,
                    description = "دریافت $title به ${if (destinationType == "CASH") "صندوق" else "بانک"}",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 601,
                    subsidiaryCode = 60101,
                    description = "ثبت درآمد $title - $category",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "سند دریافت درآمد: $title",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "دریافت وجه $title با موفقیت ثبت شد."
            onSuccess()
        }
    }

    /**
     * ثبت هزینه / پرداخت
     */
    fun registerExpense(
        title: String,
        amount: Long,
        sourceType: String, // "CASH" or "BANK"
        sourceId: Long,
        category: String, // "حقوق و دستمزد", "کرایه و حمل", "ملزومات و قبوض", "سایر هزینه‌ها"
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ هزینه باید بیشتر از صفر باشد."
                return@launch
            }
            if (sourceType == "CASH") {
                repository.updateCashBalance(sourceId, -amount)
            } else {
                repository.updateBankBalance(sourceId, -amount)
            }

            // سند دوبل حسابداری
            val today = JalaliDate.now()
            val subCode = if (sourceType == "CASH") 10101 else 10102
            val expSubCode = if (category.contains("حقوق")) 80101 else 80102
            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 801,
                    subsidiaryCode = expSubCode,
                    description = "هزینه $title ($category)",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = subCode,
                    description = "پرداخت از ${if (sourceType == "CASH") "صندوق" else "بانک"}",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "سند پرداخت هزینه: $title",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "پرداخت هزینه $title با موفقیت ثبت شد."
            onSuccess()
        }
    }

    /**
     * ثبت سرمایه اولیه و آورده نقدی شرکا
     */
    fun registerCapitalInvestment(
        investorName: String,
        amount: Long,
        destinationType: String,
        targetId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) return@launch
            if (destinationType == "CASH") {
                repository.updateCashBalance(targetId, amount)
            } else {
                repository.updateBankBalance(targetId, amount)
            }
            val today = JalaliDate.now()
            val subCode = if (destinationType == "CASH") 10101 else 10102
            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = subCode,
                    description = "آورده نقدی سرمایه توسط $investorName",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 501,
                    subsidiaryCode = 50101,
                    description = "افزایش سرمایه و سهم‌الشرکه $investorName",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "سند ثبت سرمایه اولیه / آورده شرکا: $investorName",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "ثبت سرمایه $investorName با موفقیت انجام شد."
            onSuccess()
        }
    }

    /**
     * انتقال وجه بین صندوق و حساب بانکی
     */
    fun transferFunds(
        fromType: String, // "CASH" or "BANK"
        fromId: Long,
        toType: String, // "CASH" or "BANK"
        toId: Long,
        amount: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _statusMessage.value = "مبلغ انتقال باید معتبر باشد."
                return@launch
            }
            if (fromType == "CASH") repository.updateCashBalance(fromId, -amount)
            else repository.updateBankBalance(fromId, -amount)

            if (toType == "CASH") repository.updateCashBalance(toId, amount)
            else repository.updateBankBalance(toId, amount)

            val today = JalaliDate.now()
            val fromSub = if (fromType == "CASH") 10101 else 10102
            val toSub = if (toType == "CASH") 10101 else 10102

            val journalItems = listOf(
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = toSub,
                    description = "انتقال وجه وارده به ${if (toType == "CASH") "صندوق" else "بانک"}",
                    debit = amount,
                    credit = 0L
                ),
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = 101,
                    subsidiaryCode = fromSub,
                    description = "خروج وجه از ${if (fromType == "CASH") "صندوق" else "بانک"}",
                    debit = 0L,
                    credit = amount
                )
            )
            accountingRepo.createJournalVoucher(
                JournalEntryEntity(
                    voucherNumber = 0L,
                    jalaliDate = today.toStandardString(),
                    description = "سند انتقال وجه بین حساب‌ها/صندوق‌ها",
                    isBalanced = true,
                    status = "FINAL"
                ),
                journalItems
            )
            _statusMessage.value = "انتقال وجه با موفقیت انجام شد."
            onSuccess()
        }
    }

    fun exportTreasuryPdf(context: Context) {
        val headers = listOf("ردیف", "عنوان منبع / حساب", "نوع", "مشخصات / شماره", "موجودی (تومان)")
        val rows = mutableListOf<List<String>>()

        var r = 1
        allCashDesks.value.forEach { c ->
            rows.add(listOf(r++.toString(), c.name, "صندوق نقدی", "مسئول: ${c.keeperName}", NumberFormatUtils.formatPrice(c.currentBalance)))
        }
        allBankAccounts.value.forEach { b ->
            rows.add(listOf(r++.toString(), b.bankName, "حساب بانکی", "کارت: ${b.cardNumber}", NumberFormatUtils.formatPrice(b.currentBalance)))
        }
        val file = ExportHelper.exportReportPdf(context, "گزارش موجودی نقد، بانک و صندوق‌ها", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "گزارش خزانه‌داری PDF")
        }
    }

    fun exportTreasuryCsv(context: Context) {
        val headers = listOf("ردیف", "عنوان", "نوع", "شماره حساب/کارت", "موجودی (تومان)")
        val rows = mutableListOf<List<String>>()
        var r = 1
        allCashDesks.value.forEach { c ->
            rows.add(listOf(r++.toString(), c.name, "صندوق", c.code, c.currentBalance.toString()))
        }
        allBankAccounts.value.forEach { b ->
            rows.add(listOf(r++.toString(), b.bankName, "بانک", b.cardNumber.ifEmpty { b.accountNumber }, b.currentBalance.toString()))
        }
        val file = ExportHelper.exportReportCsv(context, "گزارش نقدینگی خزانه‌داری", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل خزانه‌داری")
        }
    }
}
