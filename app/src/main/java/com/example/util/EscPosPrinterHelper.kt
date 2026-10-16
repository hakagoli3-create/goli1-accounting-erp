package com.example.util

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * ESC/POS Thermal Printer Commands Generator and Bluetooth Print Formatter.
 * Generates standard ESC/POS bytes for 58mm and 80mm thermal receipt printers.
 */
object EscPosPrinterHelper {

    val ESC: Byte = 0x1B
    val GS: Byte = 0x1D
    val LF: Byte = 0x0A

    val CMD_INIT: ByteArray = byteArrayOf(ESC, 0x40) // Initialize printer
    val CMD_ALIGN_LEFT: ByteArray = byteArrayOf(ESC, 0x61, 0x00)
    val CMD_ALIGN_CENTER: ByteArray = byteArrayOf(ESC, 0x61, 0x01)
    val CMD_ALIGN_RIGHT: ByteArray = byteArrayOf(ESC, 0x61, 0x02)
    val CMD_BOLD_ON: ByteArray = byteArrayOf(ESC, 0x45, 0x01)
    val CMD_BOLD_OFF: ByteArray = byteArrayOf(ESC, 0x45, 0x00)
    val CMD_DOUBLE_SIZE_ON: ByteArray = byteArrayOf(GS, 0x21, 0x11)
    val CMD_NORMAL_SIZE: ByteArray = byteArrayOf(GS, 0x21, 0x00)
    val CMD_CUT_PAPER: ByteArray = byteArrayOf(GS, 0x56, 0x42, 0x00) // Full cut with feed

    data class PrintableInvoice(
        val invoiceNumber: String,
        val invoiceDate: String,
        val customerName: String,
        val customerPhone: String,
        val warehouseName: String,
        val items: List<PrintableItem>,
        val subtotalAmount: Long,
        val discountAmount: Long,
        val taxAmount: Long,
        val totalAmount: Long,
        val paymentMethod: String,
        val note: String = ""
    )

    data class PrintableItem(
        val name: String,
        val quantity: Double,
        val unit: String,
        val unitPrice: Long,
        val totalPrice: Long
    )

    /**
     * Build ESC/POS binary stream for sending over Bluetooth/USB socket.
     */
    fun buildEscPosBytes(invoice: PrintableInvoice, printerWidthChars: Int = 32): ByteArray {
        val stream = ByteArrayOutputStream()

        stream.write(CMD_INIT)
        stream.write(CMD_ALIGN_CENTER)
        stream.write(CMD_DOUBLE_SIZE_ON)
        stream.write(CMD_BOLD_ON)
        stream.write("مدیریت حسابداری گلی\n".toByteArray(Charset.forName("UTF-8")))
        stream.write(CMD_NORMAL_SIZE)
        stream.write(CMD_BOLD_OFF)
        stream.write("فاکتور رسمی فروش\n".toByteArray(Charset.forName("UTF-8")))
        stream.write(makeDivider(printerWidthChars).toByteArray(Charset.forName("UTF-8")))

        stream.write(CMD_ALIGN_RIGHT)
        stream.write("شماره فاکتور: ${invoice.invoiceNumber}\n".toByteArray(Charset.forName("UTF-8")))
        stream.write("تاریخ: ${invoice.invoiceDate}\n".toByteArray(Charset.forName("UTF-8")))
        stream.write("مشتری: ${invoice.customerName}\n".toByteArray(Charset.forName("UTF-8")))
        if (invoice.customerPhone.isNotBlank()) {
            stream.write("تلفن: ${invoice.customerPhone}\n".toByteArray(Charset.forName("UTF-8")))
        }
        stream.write("انبار مبدا: ${invoice.warehouseName}\n".toByteArray(Charset.forName("UTF-8")))
        stream.write(makeDivider(printerWidthChars).toByteArray(Charset.forName("UTF-8")))

        // Item Header
        stream.write("شرح کالا | تعداد | فیمت | جمع کل\n".toByteArray(Charset.forName("UTF-8")))
        stream.write(makeDivider(printerWidthChars, '-').toByteArray(Charset.forName("UTF-8")))

        for (item in invoice.items) {
            val qStr = NumberFormatUtils.formatQuantity(item.quantity)
            val pStr = NumberFormatUtils.formatPriceOnlyNumber(item.unitPrice)
            val tStr = NumberFormatUtils.formatPriceOnlyNumber(item.totalPrice)
            stream.write("${item.name}\n".toByteArray(Charset.forName("UTF-8")))
            stream.write("  $qStr ${item.unit} * $pStr = $tStr\n".toByteArray(Charset.forName("UTF-8")))
        }

        stream.write(makeDivider(printerWidthChars).toByteArray(Charset.forName("UTF-8")))
        stream.write(CMD_ALIGN_LEFT)
        stream.write("جمع اقلام: ${NumberFormatUtils.formatPrice(invoice.subtotalAmount)}\n".toByteArray(Charset.forName("UTF-8")))
        if (invoice.discountAmount > 0) {
            stream.write("تخفیف: ${NumberFormatUtils.formatPrice(invoice.discountAmount)}\n".toByteArray(Charset.forName("UTF-8")))
        }
        if (invoice.taxAmount > 0) {
            stream.write("مالیات ارزش افزوده: ${NumberFormatUtils.formatPrice(invoice.taxAmount)}\n".toByteArray(Charset.forName("UTF-8")))
        }

        stream.write(CMD_BOLD_ON)
        stream.write("مبلغ نهایی: ${NumberFormatUtils.formatPrice(invoice.totalAmount)}\n".toByteArray(Charset.forName("UTF-8")))
        stream.write(CMD_BOLD_OFF)
        stream.write("روش پرداخت: ${invoice.paymentMethod}\n".toByteArray(Charset.forName("UTF-8")))

        if (invoice.note.isNotBlank()) {
            stream.write("یادداشت: ${invoice.note}\n".toByteArray(Charset.forName("UTF-8")))
        }

        stream.write(CMD_ALIGN_CENTER)
        stream.write(makeDivider(printerWidthChars).toByteArray(Charset.forName("UTF-8")))
        stream.write("با تشکر از خرید و اعتماد شما\n".toByteArray(Charset.forName("UTF-8")))
        stream.write("نرم‌افزار جامع حسابداری گلی\n\n\n".toByteArray(Charset.forName("UTF-8")))

        stream.write(CMD_CUT_PAPER)

        return stream.toByteArray()
    }

    /**
     * Produces a clean monospaced plain-text receipt for on-screen preview or sharing.
     */
    fun buildPlainTextReceipt(invoice: PrintableInvoice): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("            مدیریت حسابداری گلی           ")
        sb.appendLine("             فاکتور رسمی فروش             ")
        sb.appendLine("==========================================")
        sb.appendLine("شماره فاکتور: ${invoice.invoiceNumber}")
        sb.appendLine("تاریخ صدور : ${invoice.invoiceDate}")
        sb.appendLine("خریدار     : ${invoice.customerName}")
        if (invoice.customerPhone.isNotBlank()) {
            sb.appendLine("شماره تماس : ${invoice.customerPhone}")
        }
        sb.appendLine("انبار      : ${invoice.warehouseName}")
        sb.appendLine("------------------------------------------")
        sb.appendLine("ردیف | نام کالا | تعداد | فی | جمع (تومان)")
        sb.appendLine("------------------------------------------")

        invoice.items.forEachIndexed { index, item ->
            val rowNum = NumberFormatUtils.toPersianDigits((index + 1).toString())
            val qStr = NumberFormatUtils.formatQuantity(item.quantity)
            val pStr = NumberFormatUtils.formatPriceOnlyNumber(item.unitPrice)
            val tStr = NumberFormatUtils.formatPriceOnlyNumber(item.totalPrice)
            sb.appendLine("$rowNum- ${item.name}")
            sb.appendLine("   $qStr ${item.unit} × $pStr = $tStr تومان")
        }

        sb.appendLine("==========================================")
        sb.appendLine("جمع کل اقلام : ${NumberFormatUtils.formatPrice(invoice.subtotalAmount)}")
        if (invoice.discountAmount > 0) {
            sb.appendLine("تخفیف        : ${NumberFormatUtils.formatPrice(invoice.discountAmount)}")
        }
        if (invoice.taxAmount > 0) {
            sb.appendLine("مالیات (۱۰٪) : ${NumberFormatUtils.formatPrice(invoice.taxAmount)}")
        }
        sb.appendLine("------------------------------------------")
        sb.appendLine("مبلغ قابل پرداخت: ${NumberFormatUtils.formatPrice(invoice.totalAmount)}")
        sb.appendLine("حروف: ${NumberFormatUtils.numberToPersianWords(invoice.totalAmount)} تومان")
        sb.appendLine("روش تسویه    : ${invoice.paymentMethod}")
        if (invoice.note.isNotBlank()) {
            sb.appendLine("توضیحات      : ${invoice.note}")
        }
        sb.appendLine("==========================================")
        sb.appendLine("        از حسن انتخاب شما سپاسگزاریم       ")
        sb.appendLine("      نرم‌افزار یکپارچه حسابداری گلی       ")
        return sb.toString()
    }

    private fun makeDivider(width: Int, char: Char = '='): String {
        return char.toString().repeat(width) + "\n"
    }
}
