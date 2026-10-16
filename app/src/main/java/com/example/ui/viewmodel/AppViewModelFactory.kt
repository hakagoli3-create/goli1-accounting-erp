package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.AccountingRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import com.example.data.repository.TreasuryRepository

class AppViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val accountingRepository: AccountingRepository by lazy {
        AccountingRepository(database.accountingDao())
    }

    val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(database.inventoryDao())
    }

    val partyRepository: PartyRepository by lazy {
        PartyRepository(database.partyDao())
    }

    val treasuryRepository: TreasuryRepository by lazy {
        TreasuryRepository(database.treasuryDao())
    }

    val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(
            invoiceDao = database.invoiceDao(),
            inventoryDao = database.inventoryDao(),
            partyDao = database.partyDao(),
            treasuryDao = database.treasuryDao(),
            accountingDao = database.accountingDao()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(
                    invoiceRepo = invoiceRepository,
                    treasuryRepo = treasuryRepository,
                    inventoryRepo = inventoryRepository,
                    partyRepo = partyRepository,
                    accountingRepo = accountingRepository
                ) as T
            }
            modelClass.isAssignableFrom(AccountingViewModel::class.java) -> {
                AccountingViewModel(accountingRepository) as T
            }
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> {
                InventoryViewModel(inventoryRepository) as T
            }
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> {
                InvoiceViewModel(
                    invoiceRepo = invoiceRepository,
                    inventoryRepo = inventoryRepository,
                    partyRepo = partyRepository,
                    treasuryRepo = treasuryRepository
                ) as T
            }
            modelClass.isAssignableFrom(TreasuryViewModel::class.java) -> {
                TreasuryViewModel(treasuryRepository, accountingRepository) as T
            }
            modelClass.isAssignableFrom(PartyViewModel::class.java) -> {
                PartyViewModel(partyRepository, invoiceRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
