package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceWithItemsById(id: Long): InvoiceWithItems?

    @Query("SELECT * FROM invoices WHERE partyId = :partyId ORDER BY id DESC")
    fun getInvoicesForParty(partyId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT COALESCE(MAX(id), 0) FROM invoices")
    suspend fun getMaxInvoiceId(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE invoiceType IN ('SALE', 'POS_SALE')")
    fun getTotalSalesSum(): Flow<Long?>

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE invoiceType IN ('SALE', 'POS_SALE') AND jalaliDate = :todayJalali")
    fun getTodaySalesSum(todayJalali: String): Flow<Long?>

    @Query("SELECT COUNT(*) FROM invoices WHERE invoiceType IN ('SALE', 'POS_SALE') AND jalaliDate = :todayJalali")
    fun getTodayInvoiceCount(todayJalali: String): Flow<Int>

    @Transaction
    suspend fun createInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val invoiceId = insertInvoice(invoice)
        val attached = items.map { it.copy(invoiceId = invoiceId) }
        insertInvoiceItems(attached)
        return invoiceId
    }
}
