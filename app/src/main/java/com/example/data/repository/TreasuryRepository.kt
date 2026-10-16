package com.example.data.repository

import com.example.data.dao.TreasuryDao
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.Flow

class TreasuryRepository(private val dao: TreasuryDao) {

    val allCashDesks: Flow<List<CashDeskEntity>> = dao.getAllCashDesks()
    val allBankAccounts: Flow<List<BankAccountEntity>> = dao.getAllBankAccounts()
    val totalCashBalance: Flow<Long?> = dao.getTotalCashBalance()
    val totalBankBalance: Flow<Long?> = dao.getTotalBankBalance()

    val allCheques: Flow<List<ChequeEntity>> = dao.getAllCheques()
    val receivableCheques: Flow<List<ChequeEntity>> = dao.getChequesByType("RECEIVABLE")
    val payableCheques: Flow<List<ChequeEntity>> = dao.getChequesByType("PAYABLE")
    val activeCheques: Flow<List<ChequeEntity>> = dao.getActivePendingCheques()

    fun getDueOrOverdueCheques(cutoffEpoch: Long): Flow<List<ChequeEntity>> =
        dao.getDueOrOverdueCheques(cutoffEpoch)

    fun getDueAlertsCount(): Flow<Int> {
        val sevenDaysAhead = JalaliDate.now().addDays(7).toEpochMillis()
        return dao.getDueOrOverdueChequesCount(sevenDaysAhead)
    }

    suspend fun insertCashDesk(cashDesk: CashDeskEntity): Long = dao.insertCashDesk(cashDesk)
    suspend fun updateCashDesk(cashDesk: CashDeskEntity) = dao.updateCashDesk(cashDesk)
    suspend fun deleteCashDesk(cashDesk: CashDeskEntity) = dao.deleteCashDesk(cashDesk)
    suspend fun updateCashBalance(id: Long, delta: Long) = dao.updateCashBalance(id, delta)

    suspend fun insertBankAccount(account: BankAccountEntity): Long = dao.insertBankAccount(account)
    suspend fun updateBankAccount(account: BankAccountEntity) = dao.updateBankAccount(account)
    suspend fun deleteBankAccount(account: BankAccountEntity) = dao.deleteBankAccount(account)
    suspend fun updateBankBalance(id: Long, delta: Long) = dao.updateBankBalance(id, delta)

    suspend fun insertCheque(cheque: ChequeEntity): Long = dao.insertCheque(cheque)
    suspend fun updateCheque(cheque: ChequeEntity) = dao.updateCheque(cheque)
    suspend fun deleteCheque(cheque: ChequeEntity) = dao.deleteCheque(cheque)
}
