package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * انبارها (Warehouses)
 */
@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String,
    val name: String,
    val managerName: String = "",
    val address: String = "",
    val phone: String = ""
)

/**
 * کالاها و خدمات (Products & Goods)
 * شامل واحد اصلی و فرعی، ضریب تبدیل، و قیمت‌گذاری چندسطحی
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val code: String,
    val barcode: String,
    val name: String,
    val category: String,
    val primaryUnit: String = "کارتن", // واحد اصلی
    val secondaryUnit: String = "عدد", // واحد فرعی
    val conversionRate: Double = 1.0, // ضریب تبدیل: مثلا هر کارتن ۲۴ عدد
    val currentStockInSecondary: Double = 0.0, // کل موجودی به واحد فرعی (عدد)
    val minStockThreshold: Double = 10.0, // حداقل نقطه سفارش
    val buyPrice: Long = 0L, // قیمت خرید (به ازای واحد فرعی)
    val retailPrice: Long = 0L, // قیمت مصرف‌کننده و خرده‌فروشی
    val wholesalePrice: Long = 0L, // قیمت عمده‌فروشی
    val partnerPrice: Long = 0L, // قیمت همکاری
    val description: String = ""
) {
    /**
     * محاسبه موجودی به صورت نمایش ترکیبی: مثلا "۲ کارتن و ۵ عدد"
     */
    fun formatStockDisplay(): String {
        if (conversionRate <= 1.0) {
            val count = currentStockInSecondary.toInt()
            return "$count $primaryUnit"
        }
        val cartons = (currentStockInSecondary / conversionRate).toInt()
        val remainder = (currentStockInSecondary % conversionRate).toInt()
        return when {
            cartons > 0 && remainder > 0 -> "$cartons $primaryUnit و $remainder $secondaryUnit"
            cartons > 0 -> "$cartons $primaryUnit"
            else -> "$remainder $secondaryUnit"
        }
    }
}

/**
 * کاردکس و گردش انبار (Stock Movement & Cardex)
 */
@Entity(
    tableName = "inventory_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId"), Index("warehouseId")]
)
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val warehouseId: Long,
    val jalaliDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val documentType: String, // "PURCHASE", "SALE", "TRANSFER", "ADJUSTMENT", "RETURN_SALE"
    val referenceNumber: String,
    val quantitySecondary: Double, // مثبت برای ورود، منفی برای خروج
    val unitPrice: Long = 0L,
    val description: String = ""
)
