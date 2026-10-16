package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {

    // Account Groups
    @Query("SELECT * FROM account_groups ORDER BY code ASC")
    fun getAllAccountGroups(): Flow<List<AccountGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountGroups(groups: List<AccountGroupEntity>)

    // General Accounts
    @Query("SELECT * FROM general_accounts ORDER BY code ASC")
    fun getAllGeneralAccounts(): Flow<List<GeneralAccountEntity>>

    @Query("SELECT * FROM general_accounts WHERE groupCode = :groupCode ORDER BY code ASC")
    fun getGeneralAccountsByGroup(groupCode: Int): Flow<List<GeneralAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralAccounts(accounts: List<GeneralAccountEntity>)

    // Subsidiary Accounts
    @Query("SELECT * FROM subsidiary_accounts ORDER BY code ASC")
    fun getAllSubsidiaryAccounts(): Flow<List<SubsidiaryAccountEntity>>

    @Query("SELECT * FROM subsidiary_accounts WHERE generalCode = :generalCode ORDER BY code ASC")
    fun getSubsidiaryAccountsByGeneral(generalCode: Int): Flow<List<SubsidiaryAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubsidiaryAccounts(accounts: List<SubsidiaryAccountEntity>)

    // Journal Entries (اسناد حسابداری)
    @Query("SELECT * FROM journal_entries ORDER BY voucherNumber DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getJournalEntryById(id: Long): JournalEntryEntity?

    @Query("SELECT COALESCE(MAX(voucherNumber), 0) FROM journal_entries")
    suspend fun getMaxVoucherNumber(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    // Journal Items (آرتیکل‌های سند)
    @Query("SELECT * FROM journal_items WHERE voucherId = :voucherId")
    fun getJournalItemsByVoucher(voucherId: Long): Flow<List<JournalItemEntity>>

    @Query("SELECT * FROM journal_items WHERE voucherId = :voucherId")
    suspend fun getJournalItemsByVoucherSync(voucherId: Long): List<JournalItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalItems(items: List<JournalItemEntity>)

    @Query("SELECT SUM(debit) FROM journal_items")
    fun getTotalDebitSum(): Flow<Long?>

    @Query("SELECT SUM(credit) FROM journal_items")
    fun getTotalCreditSum(): Flow<Long?>

    @Query("SELECT * FROM journal_items ORDER BY id DESC")
    fun getAllJournalItems(): Flow<List<JournalItemEntity>>

    @Transaction
    suspend fun createVoucherWithItems(
        entry: JournalEntryEntity,
        items: List<JournalItemEntity>
    ): Long {
        val nextNumber = getMaxVoucherNumber() + 1
        val voucherId = insertJournalEntry(entry.copy(voucherNumber = nextNumber))
        val attachedItems = items.map { it.copy(voucherId = voucherId) }
        insertJournalItems(attachedItems)
        return voucherId
    }
}
