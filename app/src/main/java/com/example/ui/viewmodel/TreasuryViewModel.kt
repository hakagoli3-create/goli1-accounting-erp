package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.repository.TreasuryRepository
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TreasuryViewModel(
    private val repository: TreasuryRepository
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
            onSuccess()
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
                    hasPos = hasPos
                )
            )
            onSuccess()
        }
    }
}
