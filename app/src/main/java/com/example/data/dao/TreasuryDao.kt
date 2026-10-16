package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TreasuryDao {

    // Cash Desks
    @Query("SELECT * FROM cash_desks ORDER BY id ASC")
    fun getAllCashDesks(): Flow<List<CashDeskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashDesk(cashDesk: CashDeskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashDesks(cashDesks: List<CashDeskEntity>)

    @Query("UPDATE cash_desks SET currentBalance = currentBalance + :delta WHERE id = :id")
    suspend fun updateCashBalance(id: Long, delta: Long)

    @Query("SELECT SUM(currentBalance) FROM cash_desks")
    fun getTotalCashBalance(): Flow<Long?>

    @Update
    suspend fun updateCashDesk(cashDesk: CashDeskEntity)

    @Delete
    suspend fun deleteCashDesk(cashDesk: CashDeskEntity)

    // Bank Accounts
    @Query("SELECT * FROM bank_accounts ORDER BY id ASC")
    fun getAllBankAccounts(): Flow<List<BankAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccount(bankAccount: BankAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankAccounts(bankAccounts: List<BankAccountEntity>)

    @Query("UPDATE bank_accounts SET currentBalance = currentBalance + :delta WHERE id = :id")
    suspend fun updateBankBalance(id: Long, delta: Long)

    @Update
    suspend fun updateBankAccount(bankAccount: BankAccountEntity)

    @Delete
    suspend fun deleteBankAccount(bankAccount: BankAccountEntity)

    @Query("SELECT SUM(currentBalance) FROM bank_accounts")
    fun getTotalBankBalance(): Flow<Long?>

    // Cheques
    @Query("SELECT * FROM cheques ORDER BY dueDateEpochMillis ASC")
    fun getAllCheques(): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE type = :type ORDER BY dueDateEpochMillis ASC")
    fun getChequesByType(type: String): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE status = 'NORMAL' ORDER BY dueDateEpochMillis ASC")
    fun getActivePendingCheques(): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheques WHERE status = 'NORMAL' AND dueDateEpochMillis <= :cutoffEpoch ORDER BY dueDateEpochMillis ASC")
    fun getDueOrOverdueCheques(cutoffEpoch: Long): Flow<List<ChequeEntity>>

    @Query("SELECT COUNT(*) FROM cheques WHERE status = 'NORMAL' AND dueDateEpochMillis <= :cutoffEpoch")
    fun getDueOrOverdueChequesCount(cutoffEpoch: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: ChequeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheques(cheques: List<ChequeEntity>)

    @Update
    suspend fun updateCheque(cheque: ChequeEntity)

    @Delete
    suspend fun deleteCheque(cheque: ChequeEntity)
}
