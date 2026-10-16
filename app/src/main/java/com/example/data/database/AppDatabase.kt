package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountingDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.InvoiceDao
import com.example.data.dao.PartyDao
import com.example.data.dao.TreasuryDao
import com.example.data.model.AccountGroupEntity
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.WarehouseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountGroupEntity::class,
        GeneralAccountEntity::class,
        SubsidiaryAccountEntity::class,
        JournalEntryEntity::class,
        JournalItemEntity::class,
        WarehouseEntity::class,
        ProductEntity::class,
        InventoryMovementEntity::class,
        PartyEntity::class,
        CashDeskEntity::class,
        BankAccountEntity::class,
        ChequeEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountingDao(): AccountingDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun partyDao(): PartyDao
    abstract fun treasuryDao(): TreasuryDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goli_accounting_erp.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default Iranian chart of accounts, warehouses, products, and parties
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).let { appDb ->
                                    DatabaseSeeder.seedDefaultData(appDb)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
