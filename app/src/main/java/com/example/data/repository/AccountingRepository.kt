package com.example.data.repository

import com.example.data.dao.AccountingDao
import com.example.data.model.AccountGroupEntity
import com.example.data.model.GeneralAccountEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.JournalItemEntity
import com.example.data.model.SubsidiaryAccountEntity
import com.example.data.model.TrialBalanceRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AccountingRepository(private val dao: AccountingDao) {

    val allGroups: Flow<List<AccountGroupEntity>> = dao.getAllAccountGroups()
    val allGenerals: Flow<List<GeneralAccountEntity>> = dao.getAllGeneralAccounts()
    val allSubsidiaries: Flow<List<SubsidiaryAccountEntity>> = dao.getAllSubsidiaryAccounts()
    val allJournalEntries: Flow<List<JournalEntryEntity>> = dao.getAllJournalEntries()
    val totalDebitSum: Flow<Long?> = dao.getTotalDebitSum()
    val totalCreditSum: Flow<Long?> = dao.getTotalCreditSum()

    fun getGeneralAccountsByGroup(groupCode: Int): Flow<List<GeneralAccountEntity>> =
        dao.getGeneralAccountsByGroup(groupCode)

    fun getSubsidiariesByGeneral(generalCode: Int): Flow<List<SubsidiaryAccountEntity>> =
        dao.getSubsidiaryAccountsByGeneral(generalCode)

    fun getJournalItems(voucherId: Long): Flow<List<JournalItemEntity>> =
        dao.getJournalItemsByVoucher(voucherId)

    suspend fun getJournalItemsSync(voucherId: Long): List<JournalItemEntity> =
        dao.getJournalItemsByVoucherSync(voucherId)

    /**
     * ثبت سند دوبل حسابداری با اعتبارسنجی تراز بودن (مجموع بدهکار = مجموع بستانکار)
     */
    suspend fun createJournalVoucher(
        entry: JournalEntryEntity,
        items: List<JournalItemEntity>
    ): Result<Long> {
        val totalDebit = items.sumOf { it.debit }
        val totalCredit = items.sumOf { it.credit }

        if (totalDebit != totalCredit) {
            return Result.failure(
                IllegalArgumentException("سند تراز نیست! مجموع بدهکار ($totalDebit) با مجموع بستانکار ($totalCredit) برابر نمی‌باشد.")
            )
        }

        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("سند باید حداقل شامل ۲ سطر آرتیکل باشد."))
        }

        val voucherId = dao.createVoucherWithItems(entry.copy(isBalanced = true), items)
        return Result.success(voucherId)
    }

    /**
     * گزارش تراز آزمایشی کل/معین با محاسبه مانده بدهکار و بستانکار
     */
    val trialBalance: Flow<List<TrialBalanceRow>> = combine(
        dao.getAllSubsidiaryAccounts(),
        dao.getAllGeneralAccounts(),
        dao.getAllAccountGroups(),
        dao.getAllJournalItems()
    ) { subs, generals, groups, items ->
        val generalMap = generals.associateBy { it.code }
        val groupMap = groups.associateBy { it.code }

        subs.map { sub ->
            val gen = generalMap[sub.generalCode]
            val grp = gen?.let { groupMap[it.groupCode] }

            val subItems = items.filter { it.subsidiaryCode == sub.code }
            val totalDebit = subItems.sumOf { it.debit }
            val totalCredit = subItems.sumOf { it.credit }

            val balance = totalDebit - totalCredit
            val balanceDebit = if (balance > 0) balance else 0L
            val balanceCredit = if (balance < 0) -balance else 0L

            TrialBalanceRow(
                subsidiaryCode = sub.code,
                title = sub.title,
                generalTitle = gen?.title ?: "نامشخص",
                groupTitle = grp?.title ?: "نامشخص",
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                balanceDebit = balanceDebit,
                balanceCredit = balanceCredit
            )
        }.filter { it.totalDebit > 0 || it.totalCredit > 0 }
    }
}
