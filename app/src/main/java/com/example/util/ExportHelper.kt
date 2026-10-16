package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.InvoiceWithItems
import com.example.data.model.StoreProfile
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object ExportHelper {

    fun shareText(context: Context, text: String, title: String = "ارسال اطلاعات") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val authority = "${context.packageName}.provider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, title)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری فایل: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * تولید PDF استاندارد و رسمی برای فاکتور فروش
     */
    fun exportInvoicePdf(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        storeProfile: StoreProfile
    ): File? {
        return try {
            val invoice = invoiceWithItems.invoice
            val items = invoiceWithItems.items

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
            }

            // Header Background
            paint.color = Color.rgb(30, 34, 42) // Matte Charcoal
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 85f, paint)

            // Header Texts
            textPaint.color = Color.WHITE
            textPaint.textSize = 15f
            textPaint.isFakeBoldText = true
            drawTextRtl(canvas, textPaint, "صورتحساب رسمی فروش کالا و خدمات", (pageWidth - 40).toFloat(), 50f)

            textPaint.textSize = 10f
            textPaint.isFakeBoldText = false
            drawTextRtl(canvas, textPaint, storeProfile.name, (pageWidth - 40).toFloat(), 70f)

            val metaText = "شماره: ${invoice.invoiceNumber}  |  تاریخ: ${invoice.jalaliDate}"
            textPaint.textSize = 9f
            canvas.drawText(metaText, 35f, 55f, textPaint)

            var y = 105f

            // Seller & Buyer Box
            paint.color = Color.rgb(245, 240, 230) // Cream/Beige
            canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 65f, paint)

            paint.color = Color.rgb(210, 200, 185)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 65f, paint)
            paint.style = Paint.Style.FILL

            textPaint.color = Color.rgb(25, 28, 33)
            textPaint.textSize = 9f
            textPaint.isFakeBoldText = true
            drawTextRtl(canvas, textPaint, "فروشنده: ${storeProfile.name} | تلفن: ${storeProfile.phone}", (pageWidth - 30).toFloat(), y + 18f)
            textPaint.isFakeBoldText = false
            drawTextRtl(canvas, textPaint, "آدرس: ${storeProfile.address} | شناسه ملی: ${storeProfile.nationalCode}", (pageWidth - 30).toFloat(), y + 33f)

            textPaint.isFakeBoldText = true
            drawTextRtl(canvas, textPaint, "خریدار: ${invoice.partyName} | تماس: ${invoice.partyPhone.ifEmpty { "نقدی" }}", (pageWidth - 30).toFloat(), y + 50f)

            y += 80f

            // Table Header
            paint.color = Color.rgb(55, 65, 80) // Dark Slate
            canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 24f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 9f
            textPaint.isFakeBoldText = true

            // Columns positions: [575 -> 535 rwo, 360 name, 300 qty, 240 unit, 140 price, 20 total]
            drawTextRtl(canvas, textPaint, "ردیف", 565f, y + 16f)
            drawTextRtl(canvas, textPaint, "شرح کالا یا خدمات", 490f, y + 16f)
            drawTextRtl(canvas, textPaint, "تعداد", 310f, y + 16f)
            drawTextRtl(canvas, textPaint, "واحد", 260f, y + 16f)
            drawTextRtl(canvas, textPaint, "فی (تومان)", 195f, y + 16f)
            drawTextRtl(canvas, textPaint, "مبلغ کل (تومان)", 90f, y + 16f)

            y += 24f
            textPaint.color = Color.BLACK
            textPaint.isFakeBoldText = false
            textPaint.textSize = 8.5f

            items.forEachIndexed { index, item ->
                val rowBg = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 246, 242)
                paint.color = rowBg
                canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 20f, paint)

                drawTextRtl(canvas, textPaint, NumberFormatUtils.toPersianDigits((index + 1).toString()), 565f, y + 14f)
                drawTextRtl(canvas, textPaint, item.productName.take(30), 490f, y + 14f)
                drawTextRtl(canvas, textPaint, NumberFormatUtils.toPersianDigits(item.quantity.toString()), 310f, y + 14f)
                drawTextRtl(canvas, textPaint, item.unitName, 260f, y + 14f)
                drawTextRtl(canvas, textPaint, NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(item.unitPrice)), 195f, y + 14f)
                drawTextRtl(canvas, textPaint, NumberFormatUtils.toPersianDigits(NumberFormatUtils.formatPrice(item.totalPrice)), 90f, y + 14f)

                y += 20f
            }

            // Separator Line
            paint.color = Color.rgb(180, 180, 180)
            canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, paint)
            y += 15f

            // Totals Box
            paint.color = Color.rgb(245, 240, 230)
            canvas.drawRect(250f, y, (pageWidth - 20).toFloat(), y + 80f, paint)

            textPaint.textSize = 9f
            drawTextRtl(canvas, textPaint, "جمع اقلام: ${NumberFormatUtils.formatPrice(invoice.subtotalAmount)}", 560f, y + 18f)
            drawTextRtl(canvas, textPaint, "تخفیف: ${NumberFormatUtils.formatPrice(invoice.discountAmount)}", 560f, y + 36f)
            drawTextRtl(canvas, textPaint, "مالیات ارزش افزوده: ${NumberFormatUtils.formatPrice(invoice.taxAmount)}", 560f, y + 54f)

            textPaint.isFakeBoldText = true
            textPaint.textSize = 10.5f
            textPaint.color = Color.rgb(180, 20, 20)
            drawTextRtl(canvas, textPaint, "مبلغ نهایی قابل پرداخت: ${NumberFormatUtils.formatPrice(invoice.totalAmount)}", 560f, y + 74f)

            // Settlement Details
            textPaint.color = Color.BLACK
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = false
            val payModeDesc = when (invoice.paymentType) {
                "CASH" -> "تسویه کامل نقدی (صندوق)"
                "POS" -> "تسویه از طریق پوز / کارتخوان بانکی"
                "CREDIT" -> "فروش نسیه و بدهکار شدن طرف حساب"
                "CHEQUE" -> "تسویه با چک صیادی"
                "MIXED" -> "تسویه ترکیبی (نقد: ${NumberFormatUtils.formatPrice(invoice.cashAmount)} | پوز: ${NumberFormatUtils.formatPrice(invoice.posAmount)} | چک: ${NumberFormatUtils.formatPrice(invoice.chequeAmount)} | نسیه: ${NumberFormatUtils.formatPrice(invoice.creditAmount)})"
                else -> invoice.paymentType
            }
            drawTextRtl(canvas, textPaint, "وضعیت تسویه: $payModeDesc", 230f, y + 25f)
            drawTextRtl(canvas, textPaint, "شماره کارت جهت واریز: ${storeProfile.defaultBankCard}", 230f, y + 45f)
            drawTextRtl(canvas, textPaint, "شماره شبا: ${storeProfile.defaultSheba}", 230f, y + 65f)

            // Signatures
            y += 110f
            drawTextRtl(canvas, textPaint, "مهر و امضای فروشنده", 480f, y)
            drawTextRtl(canvas, textPaint, "امضا و تایید خریدار", 150f, y)

            pdfDocument.finishPage(page)

            val dir = File(context.cacheDir, "invoices")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Invoice_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * تولید فایل اکسل استاندارد (CSV با UTF-8 BOM برای نمایش صحیح فارسی در نرم‌افزار Excel)
     */
    fun exportInvoiceCsv(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        storeProfile: StoreProfile
    ): File? {
        return try {
            val invoice = invoiceWithItems.invoice
            val items = invoiceWithItems.items

            val dir = File(context.cacheDir, "exports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Invoice_${invoice.invoiceNumber}.csv")

            val sb = StringBuilder()
            // UTF-8 BOM
            sb.append('\uFEFF')
            sb.append("عنوان,صورتحساب فروش کالا و خدمات ${storeProfile.name}\n")
            sb.append("شماره فاکتور,${invoice.invoiceNumber}\n")
            sb.append("تاریخ صدور,${invoice.jalaliDate}\n")
            sb.append("نام خریدار,${invoice.partyName}\n")
            sb.append("شماره تماس خریدار,${invoice.partyPhone}\n")
            sb.append("نوع تسویه,${invoice.paymentType}\n")
            sb.append("\n")
            sb.append("ردیف,کد کالا,شرح کالا,تعداد,واحد,فی به تومان,تخفیف,مبلغ کل (تومان)\n")

            items.forEachIndexed { index, item ->
                sb.append("${index + 1},")
                sb.append("${item.barcode.ifEmpty { item.productId.toString() }},")
                sb.append("\"${item.productName.replace("\"", "\"\"")}\",")
                sb.append("${item.quantity},")
                sb.append("${item.unitName},")
                sb.append("${item.unitPrice},")
                sb.append("${item.discount},")
                sb.append("${item.totalPrice}\n")
            }

            sb.append("\n")
            sb.append("جمع اقلام,,,,,${invoice.subtotalAmount}\n")
            sb.append("تخفیف کلی,,,,,${invoice.discountAmount}\n")
            sb.append("مالیات ارزش افزوده,,,,,${invoice.taxAmount}\n")
            sb.append("مبلغ نهایی قابل پرداخت,,,,,${invoice.totalAmount}\n")
            sb.append("پرداخت نقد,,,,,${invoice.cashAmount}\n")
            sb.append("پرداخت پوز,,,,,${invoice.posAmount}\n")
            sb.append("پرداخت چک,,,,,${invoice.chequeAmount}\n")
            sb.append("مانده نسیه,,,,,${invoice.creditAmount}\n")

            val fos = FileOutputStream(file)
            fos.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * خروجی گزارش مالی جامع (معین، تراز، اسناد، سود و زیان) به صورت اکسل
     */
    fun exportReportCsv(
        context: Context,
        reportTitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ): File? {
        return try {
            val dir = File(context.cacheDir, "reports")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "Report_${System.currentTimeMillis()}.csv"
            val file = File(dir, fileName)

            val sb = StringBuilder()
            sb.append('\uFEFF') // UTF-8 BOM
            sb.append("گزارش:,$reportTitle\n")
            sb.append("تاریخ ایجاد:,${JalaliDate.now().format(includeTime = true)}\n\n")

            sb.append(headers.joinToString(separator = ",") { "\"${it.replace("\"", "\"\"")}\"" })
            sb.append("\n")

            for (row in rows) {
                sb.append(row.joinToString(separator = ",") { "\"${it.replace("\"", "\"\"")}\"" })
                sb.append("\n")
            }

            val fos = FileOutputStream(file)
            fos.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * خروجی گزارش مالی جامع به صورت PDF
     */
    fun exportReportPdf(
        context: Context,
        reportTitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint()
            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
            }

            // Title Header
            paint.color = Color.rgb(30, 34, 42)
            canvas.drawRect(20f, 20f, (pageWidth - 20).toFloat(), 75f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 14f
            textPaint.isFakeBoldText = true
            drawTextRtl(canvas, textPaint, reportTitle, (pageWidth - 35).toFloat(), 45f)

            textPaint.textSize = 9f
            textPaint.isFakeBoldText = false
            drawTextRtl(canvas, textPaint, "تاریخ گزارش: ${JalaliDate.now().format(includeTime = true)}", (pageWidth - 35).toFloat(), 62f)

            var y = 95f

            // Table Header
            paint.color = Color.rgb(55, 65, 80)
            canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 22f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = true

            val colCount = headers.size
            val colWidth = (pageWidth - 40f) / colCount.toFloat()

            for (i in headers.indices) {
                val headerX = (pageWidth - 20f) - (i * colWidth) - 10f
                drawTextRtl(canvas, textPaint, headers[i], headerX, y + 15f)
            }

            y += 22f
            textPaint.color = Color.BLACK
            textPaint.isFakeBoldText = false
            textPaint.textSize = 8f

            val maxRowsOnPage = 32
            for ((rowIndex, row) in rows.take(maxRowsOnPage).withIndex()) {
                val rowBg = if (rowIndex % 2 == 0) Color.WHITE else Color.rgb(248, 246, 242)
                paint.color = rowBg
                canvas.drawRect(20f, y, (pageWidth - 20).toFloat(), y + 18f, paint)

                for (colIndex in row.indices) {
                    val cellX = (pageWidth - 20f) - (colIndex * colWidth) - 10f
                    val cellValue = row[colIndex]
                    drawTextRtl(canvas, textPaint, cellValue.take(24), cellX, y + 13f)
                }
                y += 18f
            }

            pdfDocument.finishPage(page)

            val dir = File(context.cacheDir, "reports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawTextRtl(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
        val width = paint.measureText(text)
        canvas.drawText(text, x - width, y, paint)
    }
}
