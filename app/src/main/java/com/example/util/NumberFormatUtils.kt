package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(text: String): String {
        return text
            .replace('۰', '0')
            .replace('۱', '1')
            .replace('۲', '2')
            .replace('۳', '3')
            .replace('۴', '4')
            .replace('۵', '5')
            .replace('۶', '6')
            .replace('۷', '7')
            .replace('۸', '8')
            .replace('۹', '9')
            .replace('٠', '0')
            .replace('١', '1')
            .replace('٢', '2')
            .replace('٣', '3')
            .replace('٤', '4')
            .replace('٥', '5')
            .replace('٦', '6')
            .replace('٧', '7')
            .replace('٨', '8')
            .replace('٩', '9')
    }

    fun formatPrice(amount: Long, isToman: Boolean = true): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = formatter.format(amount)
        val persian = toPersianDigits(formatted)
        val unit = if (isToman) "تومان" else "ریال"
        return "$persian $unit"
    }

    fun formatPriceOnlyNumber(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return toPersianDigits(formatter.format(amount))
    }

    fun formatQuantity(quantity: Double): String {
        return if (quantity % 1.0 == 0.0) {
            toPersianDigits(quantity.toInt().toString())
        } else {
            toPersianDigits(String.format(Locale.US, "%.2f", quantity))
        }
    }

    private val ones = arrayOf("", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه")
    private val teens = arrayOf(
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )
    private val tens = arrayOf("", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود")
    private val hundreds = arrayOf(
        "", "یکصد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"
    )
    private val thousands = arrayOf("", "هزار", "میلیون", "میلیارد", "تریلیون")

    fun numberToPersianWords(number: Long): String {
        if (number == 0L) return "صفر"
        if (number < 0) return "منفی " + numberToPersianWords(-number)

        var num = number
        var chunkIndex = 0
        val parts = mutableListOf<String>()

        while (num > 0) {
            val chunk = (num % 1000).toInt()
            if (chunk != 0) {
                val chunkText = convertThreeDigits(chunk)
                val suffix = thousands[chunkIndex]
                val fullChunk = if (suffix.isNotEmpty()) "$chunkText $suffix" else chunkText
                parts.add(0, fullChunk)
            }
            num /= 1000
            chunkIndex++
        }

        return parts.joinToString(" و ")
    }

    private fun convertThreeDigits(n: Int): String {
        val h = n / 100
        val remainder = n % 100
        val t = remainder / 10
        val o = remainder % 10

        val list = mutableListOf<String>()
        if (h > 0) list.add(hundreds[h])

        if (remainder in 10..19) {
            list.add(teinsText(remainder))
        } else {
            if (t > 0) list.add(tens[t])
            if (o > 0) list.add(ones[o])
        }

        return list.joinToString(" و ")
    }

    private fun teinsText(n: Int): String = teens[n - 10]
}
