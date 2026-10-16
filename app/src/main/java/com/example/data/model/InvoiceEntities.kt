package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * فاکتورهای فروش، خرید، و صندوق فروشگاهی POS
 */
@Entity(
    tableName = "invoices",
    indices = [Index("invoiceNumber"), Index("partyId"), Index("warehouseId")]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val invoiceType: String = "SALE", // "SALE", "PURCHASE", "POS_SALE", "RETURN_SALE"
    val jalaliDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val partyId: Long? = null,
    val partyName: String = "مشتری نقدی",
    val partyPhone: String = "",
    val warehouseId: Long = 1L,
    val warehouseName: String = "انبار مرکزی",
    val subtotalAmount: Long = 0L,
    val discountAmount: Long = 0L,
    val taxAmount: Long = 0L, // مالیات ارزش افزوده (مثلا ۱۰٪)
    val totalAmount: Long = 0L,
    val paymentType: String = "CASH", // "CASH", "POS", "CREDIT", "CHEQUE", "MIXED"
    val cashAmount: Long = 0L,
    val posAmount: Long = 0L,
    val creditAmount: Long = 0L,
    val chequeAmount: Long = 0L,
    val cashDeskId: Long = 1L,
    val bankAccountId: Long = 1L,
    val pricingMode: String = "RETAIL", // "RETAIL" or "WHOLESALE"
    val isPaid: Boolean = true,
    val status: String = "FINAL", // "FINAL" or "DRAFT"
    val notes: String = ""
)

/**
 * اقلام فاکتور (Invoice Items)
 */
@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId"), Index("productId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val barcode: String = "",
    val unitName: String = "عدد", // مثلا "کارتن" یا "عدد"
    val isPrimaryUnit: Boolean = false,
    val quantity: Double = 1.0,
    val conversionRate: Double = 1.0,
    val quantityInSecondary: Double = 1.0, // مقدار محاسبه شده به واحد فرعی برای انبار
    val unitPrice: Long = 0L, // فی به ازای این واحد
    val discount: Long = 0L,
    val totalPrice: Long = 0L
)

/**
 * ساختار رابطه فاکتور به همراه لیست اقلام
 */
data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)
