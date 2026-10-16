package com.example.util

import java.util.Calendar

/**
 * Jalali (Shamsi) Date Converter and Formatter.
 * Complete astronomical conversion between Gregorian and Persian Jalali calendar.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<JalaliDate> {

    fun format(includeTime: Boolean = false, hour: Int = 0, minute: Int = 0): String {
        val yStr = NumberFormatUtils.toPersianDigits(year.toString())
        val mStr = NumberFormatUtils.toPersianDigits(month.toString().padStart(2, '0'))
        val dStr = NumberFormatUtils.toPersianDigits(day.toString().padStart(2, '0'))
        return if (includeTime) {
            val hStr = NumberFormatUtils.toPersianDigits(hour.toString().padStart(2, '0'))
            val minStr = NumberFormatUtils.toPersianDigits(minute.toString().padStart(2, '0'))
            "$yStr/$mStr/$dStr - $hStr:$minStr"
        } else {
            "$yStr/$mStr/$dStr"
        }
    }

    fun toStandardString(): String {
        return "${year}/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"
    }

    fun getMonthName(): String {
        return when (month) {
            1 -> "فروردین"
            2 -> "اردیبهشت"
            3 -> "خرداد"
            4 -> "تیر"
            5 -> "مرداد"
            6 -> "شهریور"
            7 -> "مهر"
            8 -> "آبان"
            9 -> "آذر"
            10 -> "دی"
            11 -> "بهمن"
            12 -> "اسفند"
            else -> ""
        }
    }

    fun addDays(days: Int): JalaliDate {
        val epoch = this.toEpochMillis()
        val newEpoch = epoch + (days * 86400000L)
        return fromEpochMillis(newEpoch)
    }

    fun toEpochMillis(): Long {
        val g = jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance()
        cal.set(g[0], g[1] - 1, g[2], 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override fun compareTo(other: JalaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun now(): JalaliDate {
            val cal = Calendar.getInstance()
            return fromGregorian(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromEpochMillis(millis: Long): JalaliDate {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            return fromGregorian(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromGregorian(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
            val j = gregorianToJalali(gYear, gMonth, gDay)
            return JalaliDate(j[0], j[1], j[2])
        }

        fun parse(dateStr: String): JalaliDate {
            val clean = NumberFormatUtils.toEnglishDigits(dateStr.trim())
            val parts = clean.split("/", "-")
            return if (parts.size == 3) {
                JalaliDate(
                    parts[0].toIntOrNull() ?: 1404,
                    parts[1].toIntOrNull() ?: 1,
                    parts[2].toIntOrNull() ?: 1
                )
            } else {
                now()
            }
        }

        private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
            val gDNo = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            val gy2 = if (gm > 2) gy + 1 else gy
            var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDNo[gm - 1]
            var jy = -1595 + (33 * (days / 12053))
            days %= 12053
            jy += 4 * (days / 1461)
            days %= 1461
            if (days > 365) {
                jy += (days - 1) / 365
                days = (days - 1) % 365
            }
            val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
            val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
            return intArrayOf(jy, jm, jd)
        }

        private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): IntArray {
            val jyAdjusted = jy - 979
            val jmAdjusted = jm - 1
            val jdAdjusted = jd - 1

            var jDayNo = 365 * jyAdjusted + (jyAdjusted / 33) * 8 + ((jyAdjusted % 33 + 3) / 4)
            for (i in 0 until jmAdjusted) {
                jDayNo += if (i < 6) 31 else 30
            }
            jDayNo += jdAdjusted

            var gDayNo = jDayNo + 79
            var gy = 1600 + 400 * (gDayNo / 146097)
            gDayNo %= 146097

            var leap = true
            if (gDayNo >= 36525) {
                gDayNo--
                gy += 100 * (gDayNo / 36524)
                gDayNo %= 36524

                if (gDayNo >= 365) {
                    gDayNo++
                } else {
                    leap = false
                }
            }

            gy += 4 * (gDayNo / 1461)
            gDayNo %= 1461

            if (gDayNo >= 366) {
                leap = false
                gDayNo--
                gy += gDayNo / 365
                gDayNo %= 365
            }

            val gMonthDays = intArrayOf(
                31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
            )
            var gm = 0
            while (gm < 12 && gDayNo >= gMonthDays[gm]) {
                gDayNo -= gMonthDays[gm]
                gm++
            }
            val gd = gDayNo + 1
            return intArrayOf(gy, gm + 1, gd)
        }
    }
}
