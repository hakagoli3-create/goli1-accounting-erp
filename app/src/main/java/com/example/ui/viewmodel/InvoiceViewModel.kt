package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceWithItems
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import com.example.util.EscPosPrinterHelper
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val product: ProductEntity,
    val isPrimaryUnit: Boolean, // آیا واحد اصلی (کارتن) است یا فرعی (عدد)
    val quantity: Double,
    val unitPrice: Long
) {
    val unitName: String get() = if (isPrimaryUnit) product.primaryUnit else product.secondaryUnit
    val quantityInSecondary: Double get() = if (isPrimaryUnit) quantity * product.conversionRate else quantity
    val totalPrice: Long get() = (quantity * unitPrice).toLong()
}

class InvoiceViewModel(
    private val invoiceRepo: InvoiceRepository,
    private val inventoryRepo: InventoryRepository,
    private val partyRepo: PartyRepository
) : ViewModel() {

    val allInvoices: StateFlow<List<InvoiceWithItems>> = invoiceRepo.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = inventoryRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<PartyEntity>> = partyRepo.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- POS & Fast Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedParty = MutableStateFlow<PartyEntity?>(null)
    val selectedParty: StateFlow<PartyEntity?> = _selectedParty.asStateFlow()

    private val _discountAmount = MutableStateFlow(0L)
    val discountAmount: StateFlow<Long> = _discountAmount.asStateFlow()

    private val _applyTax = MutableStateFlow(false)
    val applyTax: StateFlow<Boolean> = _applyTax.asStateFlow()

    // Printable Receipt State
    private val _previewReceiptText = MutableStateFlow<String?>(null)
    val previewReceiptText: StateFlow<String?> = _previewReceiptText.asStateFlow()

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    fun addToCart(product: ProductEntity, isPrimary: Boolean = false, wholesale: Boolean = false) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id && it.isPrimaryUnit == isPrimary }

        val price = if (wholesale) {
            if (isPrimary) (product.wholesalePrice * product.conversionRate).toLong() else product.wholesalePrice
        } else {
            if (isPrimary) (product.retailPrice * product.conversionRate).toLong() else product.retailPrice
        }

        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + 1.0)
        } else {
            current.add(
                CartItem(
                    product = product,
                    isPrimaryUnit = isPrimary,
                    quantity = 1.0,
                    unitPrice = price
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(index: Int, newQuantity: Double) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            if (newQuantity <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQuantity)
            }
            _cartItems.value = current
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _discountAmount.value = 0L
        _applyTax.value = false
        _selectedParty.value = null
    }

    fun selectParty(party: PartyEntity?) {
        _selectedParty.value = party
    }

    fun setDiscount(amount: Long) {
        _discountAmount.value = amount
    }

    fun toggleTax(apply: Boolean) {
        _applyTax.value = apply
    }

    fun clearAlertMessage() {
        _alertMessage.value = null
    }

    fun closeReceiptPreview() {
        _previewReceiptText.value = null
    }

    /**
     * اسکن یا جستجوی سریع بارکد در صندوق POS
     */
    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = inventoryRepo.getProductByBarcode(barcode.trim())
            if (product != null) {
                addToCart(product, isPrimary = false, wholesale = false)
            } else {
                _alertMessage.value = "کالایی با بارکد $barcode در سیستم یافت نشد."
            }
        }
    }

    /**
     * ثبت نهایی فاکتور (نقدی، پوز، نسیه، چک)
     */
    fun checkout(
        paymentType: String, // "CASH", "POS", "CREDIT", "CHEQUE"
        notes: String = "",
        onCompleted: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) {
                _alertMessage.value = "سبد خرید خالی است."
                return@launch
            }

            val subtotal = items.sumOf { it.totalPrice }
            val discount = _discountAmount.value
            val taxable = if (subtotal > discount) subtotal - discount else 0L
            val tax = if (_applyTax.value) (taxable * 0.10).toLong() else 0L
            val total = taxable + tax

            val party = _selectedParty.value

            // کنترل سقف اعتبار ریالی
            if (paymentType == "CREDIT") {
                if (party == null) {
                    _alertMessage.value = "برای خرید نسیه، انتخاب مشتری الزامی است."
                    return@launch
                }
                if (!party.canAcceptCredit(total)) {
                    val remaining = party.remainingCredit()
                    _alertMessage.value = "هشدار سقف اعتبار! مانده بدهی مشتری از سقف مجاز (${party.creditLimit} تومان) فراتر می‌رود. اعتبار باقیمانده: $remaining تومان"
                    return@launch
                }
            }

            val nextNumber = invoiceRepo.generateNextInvoiceNumber()
            val today = JalaliDate.now()

            val invoice = InvoiceEntity(
                invoiceNumber = nextNumber,
                invoiceType = if (paymentType == "CREDIT" || party != null) "SALE" else "POS_SALE",
                jalaliDate = today.toStandardString(),
                timestamp = System.currentTimeMillis(),
                partyId = party?.id,
                partyName = party?.name ?: "مشتری نقدی",
                partyPhone = party?.mobile ?: "",
                warehouseId = 1L,
                warehouseName = "انبار مرکزی تهران",
                subtotalAmount = subtotal,
                discountAmount = discount,
                taxAmount = tax,
                totalAmount = total,
                paymentType = paymentType,
                cashAmount = if (paymentType == "CASH") total else 0L,
                posAmount = if (paymentType == "POS") total else 0L,
                creditAmount = if (paymentType == "CREDIT") total else 0L,
                chequeAmount = if (paymentType == "CHEQUE") total else 0L,
                isPaid = paymentType != "CREDIT",
                notes = notes
            )

            val invoiceItems = items.map { cartItem ->
                InvoiceItemEntity(
                    invoiceId = 0L,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    barcode = cartItem.product.barcode,
                    unitName = cartItem.unitName,
                    isPrimaryUnit = cartItem.isPrimaryUnit,
                    quantity = cartItem.quantity,
                    conversionRate = cartItem.product.conversionRate,
                    quantityInSecondary = cartItem.quantityInSecondary,
                    unitPrice = cartItem.unitPrice,
                    totalPrice = cartItem.totalPrice
                )
            }

            val invoiceId = invoiceRepo.createInvoice(invoice, invoiceItems)

            // Prepare ESC/POS print preview
            val printable = EscPosPrinterHelper.PrintableInvoice(
                invoiceNumber = invoice.invoiceNumber,
                invoiceDate = today.format(includeTime = true),
                customerName = invoice.partyName,
                customerPhone = invoice.partyPhone,
                warehouseName = invoice.warehouseName,
                items = invoiceItems.map {
                    EscPosPrinterHelper.PrintableItem(
                        name = it.productName,
                        quantity = it.quantity,
                        unit = it.unitName,
                        unitPrice = it.unitPrice,
                        totalPrice = it.totalPrice
                    )
                },
                subtotalAmount = invoice.subtotalAmount,
                discountAmount = invoice.discountAmount,
                taxAmount = invoice.taxAmount,
                totalAmount = invoice.totalAmount,
                paymentMethod = when (paymentType) {
                    "CASH" -> "نقدی (صندوق)"
                    "POS" -> "کارت‌خوان (پوز)"
                    "CREDIT" -> "نسیه (اعتباری)"
                    "CHEQUE" -> "چک صیادی"
                    else -> paymentType
                },
                note = notes
            )

            _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
            clearCart()
            onCompleted(invoiceId)
        }
    }

    fun showReceiptForInvoice(invoiceWithItems: InvoiceWithItems) {
        val inv = invoiceWithItems.invoice
        val printable = EscPosPrinterHelper.PrintableInvoice(
            invoiceNumber = inv.invoiceNumber,
            invoiceDate = inv.jalaliDate,
            customerName = inv.partyName,
            customerPhone = inv.partyPhone,
            warehouseName = inv.warehouseName,
            items = invoiceWithItems.items.map {
                EscPosPrinterHelper.PrintableItem(
                    name = it.productName,
                    quantity = it.quantity,
                    unit = it.unitName,
                    unitPrice = it.unitPrice,
                    totalPrice = it.totalPrice
                )
            },
            subtotalAmount = inv.subtotalAmount,
            discountAmount = inv.discountAmount,
            taxAmount = inv.taxAmount,
            totalAmount = inv.totalAmount,
            paymentMethod = when (inv.paymentType) {
                "CASH" -> "نقدی"
                "POS" -> "کارت‌خوان پوز"
                "CREDIT" -> "نسیه"
                "CHEQUE" -> "چک"
                else -> inv.paymentType
            },
            note = inv.notes
        )
        _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
    }
}
