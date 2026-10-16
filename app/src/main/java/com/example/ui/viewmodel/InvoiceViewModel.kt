package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BankAccountEntity
import com.example.data.model.CashDeskEntity
import com.example.data.model.ChequeEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceWithItems
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.data.model.StoreProfile
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InvoiceRepository
import com.example.data.repository.PartyRepository
import com.example.data.repository.TreasuryRepository
import com.example.util.EscPosPrinterHelper
import com.example.util.ExportHelper
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
    private val partyRepo: PartyRepository,
    private val treasuryRepo: TreasuryRepository
) : ViewModel() {

    val allInvoices: StateFlow<List<InvoiceWithItems>> = invoiceRepo.allInvoicesWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = inventoryRepo.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<PartyEntity>> = partyRepo.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCashDesks: StateFlow<List<CashDeskEntity>> = treasuryRepo.allCashDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBankAccounts: StateFlow<List<BankAccountEntity>> = treasuryRepo.allBankAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Pricing Mode: RETAIL (تکی) vs WHOLESALE (عمده) ---
    private val _pricingMode = MutableStateFlow("RETAIL")
    val pricingMode: StateFlow<String> = _pricingMode.asStateFlow()

    // --- POS & Fast Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedParty = MutableStateFlow<PartyEntity?>(null)
    val selectedParty: StateFlow<PartyEntity?> = _selectedParty.asStateFlow()

    private val _discountAmount = MutableStateFlow(0L)
    val discountAmount: StateFlow<Long> = _discountAmount.asStateFlow()

    private val _applyTax = MutableStateFlow(false)
    val applyTax: StateFlow<Boolean> = _applyTax.asStateFlow()

    // Selected destination bank & cash desk
    private val _selectedCashDeskId = MutableStateFlow(1L)
    val selectedCashDeskId: StateFlow<Long> = _selectedCashDeskId.asStateFlow()

    private val _selectedBankAccountId = MutableStateFlow(1L)
    val selectedBankAccountId: StateFlow<Long> = _selectedBankAccountId.asStateFlow()

    // Printable Receipt State
    private val _previewReceiptText = MutableStateFlow<String?>(null)
    val previewReceiptText: StateFlow<String?> = _previewReceiptText.asStateFlow()

    // Selected Invoice for Detailed Official View / Export Dialog
    private val _selectedInvoiceForDetails = MutableStateFlow<InvoiceWithItems?>(null)
    val selectedInvoiceForDetails: StateFlow<InvoiceWithItems?> = _selectedInvoiceForDetails.asStateFlow()

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    fun setPricingMode(mode: String) {
        _pricingMode.value = mode
        // Refresh cart items with new pricing mode
        val isWholesale = mode == "WHOLESALE"
        val updated = _cartItems.value.map { item ->
            val p = item.product
            val price = if (isWholesale) {
                if (item.isPrimaryUnit) (p.wholesalePrice * p.conversionRate).toLong() else p.wholesalePrice
            } else {
                if (item.isPrimaryUnit) (p.retailPrice * p.conversionRate).toLong() else p.retailPrice
            }
            item.copy(unitPrice = price)
        }
        _cartItems.value = updated
    }

    fun setSelectedCashDesk(id: Long) {
        _selectedCashDeskId.value = id
    }

    fun setSelectedBankAccount(id: Long) {
        _selectedBankAccountId.value = id
    }

    fun addToCart(product: ProductEntity, isPrimary: Boolean = false, forceWholesale: Boolean? = null) {
        val isWholesale = forceWholesale ?: (_pricingMode.value == "WHOLESALE")
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id && it.isPrimaryUnit == isPrimary }

        val price = if (isWholesale) {
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

    fun toggleCartItemUnit(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            val newIsPrimary = !item.isPrimaryUnit
            val isWholesale = _pricingMode.value == "WHOLESALE"
            val price = if (isWholesale) {
                if (newIsPrimary) (item.product.wholesalePrice * item.product.conversionRate).toLong() else item.product.wholesalePrice
            } else {
                if (newIsPrimary) (item.product.retailPrice * item.product.conversionRate).toLong() else item.product.retailPrice
            }
            current[index] = item.copy(isPrimaryUnit = newIsPrimary, unitPrice = price)
            _cartItems.value = current
        }
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

    fun openInvoiceDetails(invoice: InvoiceWithItems) {
        _selectedInvoiceForDetails.value = invoice
    }

    fun closeInvoiceDetails() {
        _selectedInvoiceForDetails.value = null
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val product = inventoryRepo.getProductByBarcode(barcode.trim())
            if (product != null) {
                addToCart(product, isPrimary = false)
            } else {
                _alertMessage.value = "کالایی با بارکد $barcode در انبار یا فروشگاه یافت نشد."
            }
        }
    }

    /**
     * ثبت نهایی فاکتور همراه با تسویه پیشرفته (نقد، پوز، چک، نسیه یا ترکیبی)
     */
    fun checkoutAdvanced(
        paymentType: String, // "CASH", "POS", "CREDIT", "CHEQUE", "MIXED"
        cashAmount: Long = 0L,
        posAmount: Long = 0L,
        chequeAmount: Long = 0L,
        creditAmount: Long = 0L,
        cashDeskId: Long? = null,
        bankAccountId: Long? = null,
        chequeNumber: String = "",
        sayadNumber: String = "",
        chequeDueDate: String = "",
        chequeBankName: String = "",
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

            val effectiveCredit = when (paymentType) {
                "CREDIT" -> total
                "MIXED" -> creditAmount
                else -> 0L
            }

            // بررسی سقف اعتبار در صورت وجود بخش نسیه
            if (effectiveCredit > 0) {
                if (party == null) {
                    _alertMessage.value = "برای خرید نسیه، مشخص کردن طرف حساب (مشتری) الزامی است."
                    return@launch
                }
                if (!party.canAcceptCredit(effectiveCredit)) {
                    val remaining = party.remainingCredit()
                    _alertMessage.value = "هشدار سقف اعتبار! مانده بدهی مشتری از سقف مجاز فراتر می‌رود. مانده اعتبار مجاز: $remaining تومان"
                    return@launch
                }
            }

            val effectiveCash = when (paymentType) {
                "CASH" -> total
                "MIXED" -> cashAmount
                else -> 0L
            }
            val effectivePos = when (paymentType) {
                "POS" -> total
                "MIXED" -> posAmount
                else -> 0L
            }
            val effectiveCheque = when (paymentType) {
                "CHEQUE" -> total
                "MIXED" -> chequeAmount
                else -> 0L
            }

            val effectiveCashDesk = cashDeskId ?: _selectedCashDeskId.value
            val effectiveBank = bankAccountId ?: _selectedBankAccountId.value

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
                cashAmount = effectiveCash,
                posAmount = effectivePos,
                creditAmount = effectiveCredit,
                chequeAmount = effectiveCheque,
                cashDeskId = effectiveCashDesk,
                bankAccountId = effectiveBank,
                pricingMode = _pricingMode.value,
                isPaid = effectiveCredit == 0L,
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

            // اگر چک صیادی ثبت شده بود، در سیستم خزانه‌داری نیز به صورت اتوماتیک اضافه می‌شود
            if (effectiveCheque > 0 && chequeNumber.isNotBlank()) {
                val dueEpoch = try {
                    JalaliDate.parse(chequeDueDate).toEpochMillis()
                } catch (e: Exception) {
                    today.addDays(30).toEpochMillis()
                }
                treasuryRepo.insertCheque(
                    ChequeEntity(
                        chequeNumber = chequeNumber,
                        sayadNumber = sayadNumber,
                        type = "RECEIVABLE",
                        partyId = party?.id,
                        partyName = party?.name ?: "مشتری فاکتور $nextNumber",
                        bankName = chequeBankName.ifBlank { "بانک مشتری" },
                        branch = "مرکزی",
                        amount = effectiveCheque,
                        issueDateJalali = today.toStandardString(),
                        dueDateJalali = chequeDueDate.ifBlank { today.addDays(30).toStandardString() },
                        dueDateEpochMillis = dueEpoch,
                        status = "NORMAL",
                        notes = "دریافت چک صیادی بابت فاکتور $nextNumber"
                    )
                )
            }

            // آماده‌سازی پیش‌نمایش چاپ حرارتی
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
                paymentMethod = invoice.paymentType,
                note = notes
            )

            _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
            clearCart()
            onCompleted(invoiceId)
        }
    }

    fun showReceiptForInvoice(inv: InvoiceWithItems) {
        val invoice = inv.invoice
        val printable = EscPosPrinterHelper.PrintableInvoice(
            invoiceNumber = invoice.invoiceNumber,
            invoiceDate = invoice.jalaliDate,
            customerName = invoice.partyName,
            customerPhone = invoice.partyPhone,
            warehouseName = invoice.warehouseName,
            items = inv.items.map {
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
            paymentMethod = invoice.paymentType,
            note = invoice.notes
        )
        _previewReceiptText.value = EscPosPrinterHelper.buildPlainTextReceipt(printable)
    }

    fun exportPdf(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val file = ExportHelper.exportInvoicePdf(context, invoiceWithItems, storeProfile)
        if (file != null) {
            ExportHelper.shareFile(context, file, "application/pdf", "اشتراک‌گذاری فاکتور PDF")
        }
    }

    fun exportCsv(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val file = ExportHelper.exportInvoiceCsv(context, invoiceWithItems, storeProfile)
        if (file != null) {
            ExportHelper.shareFile(context, file, "text/csv", "خروجی اکسل فاکتور")
        }
    }

    fun shareText(context: Context, invoiceWithItems: InvoiceWithItems, storeProfile: StoreProfile) {
        val inv = invoiceWithItems.invoice
        val sb = StringBuilder()
        sb.append("🧾 صورتحساب ${storeProfile.name}\n")
        sb.append("شماره فاکتور: ${inv.invoiceNumber}\n")
        sb.append("تاریخ: ${inv.jalaliDate}\n")
        sb.append("مشتری: ${inv.partyName}\n")
        sb.append("-----------------------------\n")
        invoiceWithItems.items.forEachIndexed { i, itm ->
            sb.append("${i + 1}. ${itm.productName} | ${itm.quantity} ${itm.unitName} × ${itm.unitPrice} = ${itm.totalPrice} تومان\n")
        }
        sb.append("-----------------------------\n")
        sb.append("جمع کل: ${inv.subtotalAmount} تومان\n")
        if (inv.discountAmount > 0) sb.append("تخفیف: ${inv.discountAmount} تومان\n")
        if (inv.taxAmount > 0) sb.append("مالیات ارزش افزوده: ${inv.taxAmount} تومان\n")
        sb.append("قابل پرداخت: ${inv.totalAmount} تومان\n")
        sb.append("نوع تسویه: ${inv.paymentType}\n")
        sb.append("شماره کارت جهت واریز: ${storeProfile.defaultBankCard}\n")
        sb.append("با تشکر از حسن انتخاب شما.")
        ExportHelper.shareText(context, sb.toString(), "ارسال متنی فاکتور")
    }
}
