package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * اشخاص، مشتریان و تامین‌کنندگان (CRM & Business Parties)
 * به همراه کنترل دقیق سقف اعتبار ریالی (Credit Limit)
 */
@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String, // کد تفصیلی
    val name: String,
    val companyName: String = "",
    val mobile: String = "",
    val phone: String = "",
    val address: String = "",
    val nationalCode: String = "", // کد ملی
    val economicCode: String = "", // کد اقتصادی
    val partyType: String = "CUSTOMER", // "CUSTOMER", "SUPPLIER", "BOTH"
    val creditLimit: Long = 0L, // سقف اعتبار به تومان (۰ یعنی بدون سقف یا نقد)
    val currentBalance: Long = 0L, // مانده حساب فعلی (مثبت = بدهکار به ما، منفی = بستانکار از ما)
    val isActive: Boolean = true,
    val notes: String = ""
) {
    /**
     * بررسی آیا مانده بدهی مشتری از سقف اعتبار عبور کرده یا نه
     */
    fun isCreditLimitExceeded(): Boolean {
        if (creditLimit <= 0L) return false
        return currentBalance > creditLimit
    }

    /**
     * بررسی آیا مشتری می‌تواند خرید نسیه جدید به مبلغ مشخص داشته باشد
     */
    fun canAcceptCredit(additionalDebt: Long): Boolean {
        if (creditLimit <= 0L) return true // بدون محدودیت
        return (currentBalance + additionalDebt) <= creditLimit
    }

    /**
     * مانده اعتبار باقی‌مانده مجاز
     */
    fun remainingCredit(): Long {
        if (creditLimit <= 0L) return Long.MAX_VALUE
        val rem = creditLimit - currentBalance
        return if (rem > 0) rem else 0L
    }
}
