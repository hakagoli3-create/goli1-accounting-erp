package com.example

import com.example.data.model.PartyEntity
import com.example.util.EscPosPrinterHelper
import com.example.util.JalaliDate
import com.example.util.NumberFormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testJalaliDateConversionAndFormatting() {
        val jalali = JalaliDate.fromGregorian(2025, 3, 21) // First day of Farvardin 1404
        assertEquals(1404, jalali.year)
        assertEquals(1, jalali.month)
        assertEquals(1, jalali.day)

        val formatted = jalali.format()
        assertTrue(formatted.contains("۱۴۰۴"))
        assertTrue(formatted.contains("۰۱"))
    }

    @Test
    fun testPersianNumberFormatUtils() {
        val english = "12500000"
        val persian = NumberFormatUtils.toPersianDigits(english)
        assertEquals("۱۲۵۰۰۰۰۰", persian)

        val formattedPrice = NumberFormatUtils.formatPrice(12500000L, isToman = true)
        assertTrue(formattedPrice.contains("۱۲,۵۰۰,۰۰۰"))
        assertTrue(formattedPrice.contains("تومان"))

        val verbal = NumberFormatUtils.numberToPersianWords(12500000L)
        assertTrue(verbal.contains("دوازده میلیون"))
        assertTrue(verbal.contains("پانصد هزار"))
    }

    @Test
    fun testPartyCreditLimitLogic() {
        val customer = PartyEntity(
            code = "CUS-101",
            name = "هایپرمارکت بهارستان",
            creditLimit = 50000000L,
            currentBalance = 40000000L
        )

        assertFalse(customer.isCreditLimitExceeded())
        assertTrue(customer.canAcceptCredit(5000000L)) // 40M + 5M <= 50M
        assertFalse(customer.canAcceptCredit(15000000L)) // 40M + 15M > 50M
        assertEquals(10000000L, customer.remainingCredit())
    }

    @Test
    fun testEscPosPrinterByteGeneration() {
        val printable = EscPosPrinterHelper.PrintableInvoice(
            invoiceNumber = "INV-1001",
            invoiceDate = "۱۴۰۴/۰۷/۲۵",
            customerName = "مشتری نقدی",
            customerPhone = "09121234567",
            warehouseName = "انبار مرکزی",
            items = listOf(
                EscPosPrinterHelper.PrintableItem(
                    name = "روغن زیتون ۱ لیتری",
                    quantity = 2.0,
                    unit = "کارتن",
                    unitPrice = 3000000L,
                    totalPrice = 6000000L
                )
            ),
            subtotalAmount = 6000000L,
            discountAmount = 0L,
            taxAmount = 600000L,
            totalAmount = 6600000L,
            paymentMethod = "کارت‌خوان (پوز)"
        )

        val bytes = EscPosPrinterHelper.buildEscPosBytes(printable)
        assertTrue(bytes.isNotEmpty())

        val textReceipt = EscPosPrinterHelper.buildPlainTextReceipt(printable)
        assertTrue(textReceipt.contains("مدیریت حسابداری گلی"))
        assertTrue(textReceipt.contains("INV-1001"))
        assertTrue(textReceipt.contains("روغن زیتون ۱ لیتری"))
    }
}
