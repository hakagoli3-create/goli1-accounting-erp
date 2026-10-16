package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import com.example.data.repository.AccountingRepository
import com.example.util.ExportHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VoucherItemDraft(
    val subsidiaryCode: Int,
    val subsidiaryTitle: String,
    val generalCode: Int,
    val description: String,
    val debit: Long,
    val credit: Long
)

data class ProfitLossStatement(
    val totalRevenue: Long,
    val costOfGoodsSold: Long,
    val grossProfit: Long,
    val operatingExpenses: Long,
    val netProfit: Long
)

class AccountingViewModel(
    private val repository: AccountingRepository
) : ViewModel() {

    val allGroups: StateFlow<List<AccountGroupEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenerals: StateFlow<List<GeneralAccountEntity>> = repository.allGenerals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubsidiaries: StateFlow<List<SubsidiaryAccountEntity>> = repository.allSubsidiaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJournalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trialBalance: StateFlow<List<TrialBalanceRow>> = repository.trialBalance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // محاسبه لحظه‌ای صورت سود و زیان بر اساس کدینگ استاندارد
    val profitLoss: StateFlow<ProfitLossStatement> = trialBalance.map { rows ->
        var revenue = 0L
        var cogs = 0L
        var expenses = 0L

        for (row in rows) {
            val groupCode = row.subsidiaryCode / 10000 // مثلا 60101 -> گروه ۶
            when (groupCode) {
                6 -> revenue += (row.totalCredit - row.totalDebit)
                7 -> cogs += (row.totalDebit - row.totalCredit)
                8 -> expenses += (row.totalDebit - row.totalCredit)
            }
        }
        val gross = revenue - cogs
        val net = gross - expenses
        ProfitLossStatement(
            totalRevenue = if (revenue > 0) revenue else 0L,
            costOfGoodsSold = if (cogs > 0) cogs else 0L,
            grossProfit = gross,
            operatingExpenses = if (expenses > 0) expenses else 0L,
            netProfit = net
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfitLossStatement(0, 0, 0, 0, 0))

    // State for creating a new Journal Voucher
    private val _draftVoucherDescription = MutableStateFlow("")
    val draftVoucherDescription: StateFlow<String> = _draftVoucherDescription.asStateFlow()

    private val _draftVoucherDate = MutableStateFlow(JalaliDate.now().toStandardString())
    val draftVoucherDate: StateFlow<String> = _draftVoucherDate.asStateFlow()

    private val _draftItems = MutableStateFlow<List<VoucherItemDraft>>(emptyList())
    val draftItems: StateFlow<List<VoucherItemDraft>> = _draftItems.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    fun updateDraftDescription(desc: String) {
        _draftVoucherDescription.value = desc
    }

    fun updateDraftDate(date: String) {
        _draftVoucherDate.value = date
    }

    fun addDraftItem(item: VoucherItemDraft) {
        _draftItems.value = _draftItems.value + item
    }

    fun removeDraftItem(index: Int) {
        val current = _draftItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _draftItems.value = current
        }
    }

    fun clearDraft() {
        _draftItems.value = emptyList()
        _draftVoucherDescription.value = ""
        _draftVoucherDate.value = JalaliDate.now().toStandardString()
    }

    fun clearMessage() {
        _operationMessage.value = null
    }

    fun submitJournalVoucher(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val items = _draftItems.value
            val totalDebit = items.sumOf { it.debit }
            val totalCredit = items.sumOf { it.credit }

            if (items.size < 2) {
                _operationMessage.value = "سند حسابداری باید حداقل دارای ۲ سطر باشد."
                return@launch
            }

            if (totalDebit != totalCredit) {
                _operationMessage.value = "سند تراز نیست! اختلاف تراز: ${Math.abs(totalDebit - totalCredit)} تومان"
                return@launch
            }

            val entry = JournalEntryEntity(
                voucherNumber = 0L,
                jalaliDate = _draftVoucherDate.value,
                description = _draftVoucherDescription.value.ifBlank { "سند حسابداری دستی" },
                isBalanced = true,
                status = "FINAL"
            )

            val journalItems = items.map {
                JournalItemEntity(
                    voucherId = 0L,
                    generalCode = it.generalCode,
                    subsidiaryCode = it.subsidiaryCode,
                    description = it.description,
                    debit = it.debit,
                    credit = it.credit
                )
            }

            val result = repository.createJournalVoucher(entry, journalItems)
            if (result.isSuccess) {
                _operationMessage.value = "سند حسابداری با موفقیت ثبت قطعی شد."
                clearDraft()
                onSuccess()
            } else {
                _operationMessage.value = result.exceptionOrNull()?.message ?: "خطا در ثبت سند"
            }
        }
    }

    // --- Exports for Trial Balance, Journal, and Profit & Loss ---

    fun exportTrialBalancePdf(context: Context) {
        val headers = listOf("کد معین", "عنوان حساب معین", "حساب کل", "گردش بدهکار", "گردش بستانکار", "مانده بدهکار", "مانده بستانکار")
        val rows = trialBalance.value.map { r ->
            listOf(
                r.subsidiaryCode.toString(),
                r.title,
                r.generalTitle,
                NumberFormatUtils.formatPrice(r.totalDebit),
                NumberFormatUtils.formatPrice(r.totalCredit),
                NumberFormatUtils.formatPrice(r.balanceDebit),
                NumberFormatUtils.formatPrice(r.balanceCredit)
            )
        }
        val file = ExportHelper.exportReportPdf(context, "تراز آزمایشی چهارستونی معین", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "تراز آزمایشی PDF")
        }
    }

    fun exportTrialBalanceCsv(context: Context) {
        val headers = listOf("کد معین", "عنوان معین", "حساب کل", "گردش بدهکار", "گردش بستانکار", "مانده بدهکار", "مانده بستانکار")
        val rows = trialBalance.value.map { r ->
            listOf(
                r.subsidiaryCode.toString(),
                r.title,
                r.generalTitle,
                r.totalDebit.toString(),
                r.totalCredit.toString(),
                r.balanceDebit.toString(),
                r.balanceCredit.toString()
            )
        }
        val file = ExportHelper.exportReportCsv(context, "تراز آزمایشی معین", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل تراز آزمایشی")
        }
    }

    fun exportProfitLossPdf(context: Context) {
        val pl = profitLoss.value
        val headers = listOf("ردیف", "عنوان قلم صورت مالی", "مبلغ (تومان)")
        val rows = listOf(
            listOf("۱", "درآمدهای عملیاتی و حاصل از فروش", NumberFormatUtils.formatPrice(pl.totalRevenue)),
            listOf("۲", "بهای تمام شده کالای فروش رفته (COGS)", "(${NumberFormatUtils.formatPrice(pl.costOfGoodsSold)})"),
            listOf("۳", "سود (زیان) ناخالص", NumberFormatUtils.formatPrice(pl.grossProfit)),
            listOf("۴", "هزینه‌های عمومی، اداری و توزیع", "(${NumberFormatUtils.formatPrice(pl.operatingExpenses)})"),
            listOf("۵", "سود (زیان) خالص نهایی دوره", NumberFormatUtils.formatPrice(pl.netProfit))
        )
        val file = ExportHelper.exportReportPdf(context, "صورت سود و زیان رسمی دوره مالی", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "صورت سود و زیان PDF")
        }
    }

    fun exportProfitLossCsv(context: Context) {
        val pl = profitLoss.value
        val headers = listOf("ردیف", "عنوان", "مبلغ به تومان")
        val rows = listOf(
            listOf("۱", "درآمد فروش", pl.totalRevenue.toString()),
            listOf("۲", "بهای تمام شده کالای فروش رفته", pl.costOfGoodsSold.toString()),
            listOf("۳", "سود ناخالص", pl.grossProfit.toString()),
            listOf("۴", "هزینه‌های عمومی و اداری", pl.operatingExpenses.toString()),
            listOf("۵", "سود خالص دوره", pl.netProfit.toString())
        )
        val file = ExportHelper.exportReportCsv(context, "صورت سود و زیان", headers, rows)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل سود و زیان")
        }
    }
}
