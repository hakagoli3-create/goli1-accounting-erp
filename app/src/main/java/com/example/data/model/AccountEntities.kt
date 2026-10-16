package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * سطح اول: گروه حساب‌ها (Account Groups)
 * ۱: دارایی‌های جاری، ۲: دارایی‌های غیرجاری، ۳: بدهی‌های جاری، ۴: بدهی‌های بلندمدت
 * ۵: حقوق صاحبان سهام، ۶: درآمدها، ۷: بهای تمام شده، ۸: هزینه‌ها
 */
@Entity(tableName = "account_groups")
data class AccountGroupEntity(
    @PrimaryKey val code: Int,
    val title: String,
    val nature: String // "DEBIT" or "CREDIT"
)

/**
 * سطح دوم: حساب‌های کل (General Ledger Accounts)
 */
@Entity(
    tableName = "general_accounts",
    foreignKeys = [
        ForeignKey(
            entity = AccountGroupEntity::class,
            parentColumns = ["code"],
            childColumns = ["groupCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupCode")]
)
data class GeneralAccountEntity(
    @PrimaryKey val code: Int, // e.g. 101, 102, 201
    val groupCode: Int,
    val title: String,
    val nature: String
)

/**
 * سطح سوم: حساب‌های معین (Subsidiary Accounts)
 */
@Entity(
    tableName = "subsidiary_accounts",
    foreignKeys = [
        ForeignKey(
            entity = GeneralAccountEntity::class,
            parentColumns = ["code"],
            childColumns = ["generalCode"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("generalCode")]
)
data class SubsidiaryAccountEntity(
    @PrimaryKey val code: Int, // e.g. 10101, 10102
    val generalCode: Int,
    val title: String,
    val nature: String
)

/**
 * سند حسابداری (Journal Entry Voucher)
 */
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val voucherNumber: Long,
    val jalaliDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val isBalanced: Boolean = true,
    val status: String = "FINAL" // "FINAL" or "DRAFT"
)

/**
 * سطرهای آرتیکل سند حسابداری (Journal Entry Line Items)
 */
@Entity(
    tableName = "journal_items",
    foreignKeys = [
        ForeignKey(
            entity = JournalEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("voucherId"), Index("subsidiaryCode")]
)
data class JournalItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val voucherId: Long,
    val generalCode: Int,
    val subsidiaryCode: Int,
    val partyId: Long? = null, // حساب تفصیلی (طرف حساب)
    val description: String,
    val debit: Long = 0L, // بدهکار
    val credit: Long = 0L // بستانکار
)

data class TrialBalanceRow(
    val subsidiaryCode: Int,
    val title: String,
    val generalTitle: String,
    val groupTitle: String,
    val totalDebit: Long,
    val totalCredit: Long,
    val balanceDebit: Long,
    val balanceCredit: Long
)
