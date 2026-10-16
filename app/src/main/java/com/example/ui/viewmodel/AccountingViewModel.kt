package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import com.example.data.repository.AccountingRepository
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}
