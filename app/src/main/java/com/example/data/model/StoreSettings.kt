package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole(
    val title: String,
    val description: String
) {
    MANAGER(
        title = "مدیر کل (Full Access)",
        description = "دسترسی کامل به سود/زیان، ویرایش قیمت‌ها، حذف اسناد و تنظیمات"
    ),
    SELLER(
        title = "فروشنده / صندوقدار",
        description = "دسترسی به صندوق POS، صدور فاکتور و مشتریان (بدون سود و حذف)"
    ),
    VISITOR(
        title = "ویزیتور / بازاریاب",
        description = "دسترسی به کاتالوگ عمده، ثبت فاکتور مشتریان و مانده حساب آنها"
    );

    val canViewProfit: Boolean get() = this == MANAGER
    val canDeleteDocuments: Boolean get() = this == MANAGER
    val canEditCostPrices: Boolean get() = this == MANAGER
    val canAccessAccountingLedger: Boolean get() = this == MANAGER || this == SELLER
    val canAccessTreasuryBalances: Boolean get() = this == MANAGER
    val canAccessSettings: Boolean get() = this == MANAGER
    val canIssueInvoices: Boolean get() = true
}

data class StoreProfile(
    val name: String = "حسابداری و بازرگانی گلی",
    val managerName: String = "آقای گلی",
    val phone: String = "021-88990011",
    val mobile: String = "09121112233",
    val address: String = "تهران، بازار بزرگ، سرای امید، پلاک ۴۴",
    val nationalCode: String = "10103456789",
    val economicCode: String = "411234567891",
    val defaultBankCard: String = "6037-9975-1234-5678",
    val defaultSheba: String = "IR120170000000123456789012",
    val taxRatePercent: Int = 10,
    val wholesaleDiscountLimitPercent: Int = 25
)

enum class CloudSyncStatus(val title: String) {
    ONLINE_SYNCED("همگام‌سازی ابری فعال و آنلاین"),
    SYNCING("در حال ارسال و دریافت اطلاعات ابری..."),
    OFFLINE_LOCAL("کارکرد آفلاین محلی (ذخیره در دیتابیس دستگاه)"),
    FAILED("خطا در ارتباط با سرور ابری")
}

class AppSecurityAndSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("goli_erp_settings", Context.MODE_PRIVATE)

    private val _currentRole = MutableStateFlow(
        UserRole.valueOf(prefs.getString("user_role", UserRole.MANAGER.name) ?: UserRole.MANAGER.name)
    )
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _storeProfile = MutableStateFlow(
        StoreProfile(
            name = prefs.getString("store_name", "حسابداری و بازرگانی گلی") ?: "حسابداری و بازرگانی گلی",
            managerName = prefs.getString("manager_name", "آقای گلی") ?: "آقای گلی",
            phone = prefs.getString("store_phone", "021-88990011") ?: "021-88990011",
            mobile = prefs.getString("store_mobile", "09121112233") ?: "09121112233",
            address = prefs.getString("store_address", "تهران، بازار بزرگ، سرای امید، پلاک ۴۴") ?: "تهران، بازار بزرگ، سرای امید، پلاک ۴۴",
            nationalCode = prefs.getString("store_national", "10103456789") ?: "10103456789",
            economicCode = prefs.getString("store_economic", "411234567891") ?: "411234567891",
            defaultBankCard = prefs.getString("store_card", "6037-9975-1234-5678") ?: "6037-9975-1234-5678",
            defaultSheba = prefs.getString("store_sheba", "IR120170000000123456789012") ?: "IR120170000000123456789012",
            taxRatePercent = prefs.getInt("tax_rate", 10),
            wholesaleDiscountLimitPercent = prefs.getInt("wholesale_discount_limit", 25)
        )
    )
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.OFFLINE_LOCAL)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getString("last_sync_time", "هنوز همگام‌سازی نشده") ?: "هنوز همگام‌سازی نشده")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        prefs.edit().putString("user_role", role.name).apply()
    }

    fun updateProfile(profile: StoreProfile) {
        _storeProfile.value = profile
        prefs.edit()
            .putString("store_name", profile.name)
            .putString("manager_name", profile.managerName)
            .putString("store_phone", profile.phone)
            .putString("store_mobile", profile.mobile)
            .putString("store_address", profile.address)
            .putString("store_national", profile.nationalCode)
            .putString("store_economic", profile.economicCode)
            .putString("store_card", profile.defaultBankCard)
            .putString("store_sheba", profile.defaultSheba)
            .putInt("tax_rate", profile.taxRatePercent)
            .putInt("wholesale_discount_limit", profile.wholesaleDiscountLimitPercent)
            .apply()
    }

    fun setSyncStatus(status: CloudSyncStatus, time: String? = null) {
        _syncStatus.value = status
        if (time != null) {
            _lastSyncTime.value = time
            prefs.edit().putString("last_sync_time", time).apply()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppSecurityAndSettingsManager? = null

        fun getInstance(context: Context): AppSecurityAndSettingsManager {
            return INSTANCE ?: synchronized(this) {
                val inst = AppSecurityAndSettingsManager(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }
}
