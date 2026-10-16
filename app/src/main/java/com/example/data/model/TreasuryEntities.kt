package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.util.JalaliDate

/**
 * صندوق‌ها و دخل نقدی
 */
@Entity(tableName = "cash_desks")
data class CashDeskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String,
    val name: String,
    val currentBalance: Long = 0L,
    val keeperName: String = "",
    val note: String = ""
)

/**
 * حساب‌های بانکی و پایانه‌های فروش (POS)
 */
@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bankName: String,
    val accountNumber: String,
    val shebaNumber: String = "",
    val cardNumber: String = "",
    val branch: String = "",
    val currentBalance: Long = 0L,
    val hasPos: Boolean = true,
    val posTerminalId: String = ""
)

/**
 * چک‌های صیادی (دریافتی و پرداختی) با قابلیت هشدار سررسید
 */
@Entity(
    tableName = "cheques",
    indices = [Index("type"), Index("status"), Index("dueDateEpochMillis")]
)
data class ChequeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chequeNumber: String,
    val sayadNumber: String, // ۱۶ رقم کد صیادی
    val type: String, // "RECEIVABLE" (چک دریافتی) or "PAYABLE" (چک پرداختی)
    val partyId: Long? = null,
    val partyName: String, // نام طرف حساب / صاحب چک / ذینفع
    val bankName: String,
    val branch: String = "",
    val amount: Long, // مبلغ به تومان
    val issueDateJalali: String,
    val dueDateJalali: String,
    val dueDateEpochMillis: Long,
    val status: String = "NORMAL", // "NORMAL" (در جریان وصول), "PASSED" (وصول شده), "BOUNCED" (برگشتی), "TRANSFERRED" (خرج شده)
    val notes: String = ""
) {
    /**
     * بررسی آیا تاریخ سررسید گذشته است
     */
    fun isOverdue(): Boolean {
        if (status != "NORMAL") return false
        val today = JalaliDate.now().toEpochMillis()
        return dueDateEpochMillis < today
    }

    /**
     * بررسی آیا سررسید همین امروز است
     */
    fun isDueToday(): Boolean {
        if (status != "NORMAL") return false
        val today = JalaliDate.now()
        val due = JalaliDate.parse(dueDateJalali)
        return today.year == due.year && today.month == due.month && today.day == due.day
    }

    /**
     * تعداد روز باقی‌مانده تا سررسید (منفی یعنی چند روز گذشته)
     */
    fun daysUntilDue(): Int {
        val todayEpoch = JalaliDate.now().toEpochMillis()
        val diff = dueDateEpochMillis - todayEpoch
        return (diff / 86400000L).toInt()
    }
}
