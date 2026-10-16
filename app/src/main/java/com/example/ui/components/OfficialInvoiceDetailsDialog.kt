package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceWithItems
import com.example.data.model.StoreProfile
import com.example.ui.theme.AccountingCreditRed
import com.example.ui.theme.AccountingDebitGreen
import com.example.ui.theme.PersianGold
import com.example.util.NumberFormatUtils

@Composable
fun OfficialInvoiceDetailsDialog(
    invoiceWithItems: InvoiceWithItems,
    storeProfile: StoreProfile,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onShareText: () -> Unit,
    onPrintReceipt: () -> Unit
) {
    val inv = invoiceWithItems.invoice
    val items = invoiceWithItems.items

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "صورتحساب رسمی فروش کالا و خدمات",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${storeProfile.name} | شماره: ${inv.invoiceNumber} | تاریخ: ${inv.jalaliDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar (Multi-format Export)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier.weight(1f).testTag("export_invoice_pdf_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f).testTag("export_invoice_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PersianGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اکسل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onShareText,
                        modifier = Modifier.weight(1f).testTag("share_invoice_text_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("متن", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onPrintReceipt,
                        modifier = Modifier.weight(1f).testTag("print_invoice_thermal_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فیش", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seller & Buyer Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("فروشنده: ${storeProfile.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("تلفن: ${storeProfile.phone}", fontSize = 12.sp)
                        }
                        Text("آدرس: ${storeProfile.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("خریدار: ${inv.partyName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("تماس: ${inv.partyPhone.ifEmpty { "نقدی" }}", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table of items
                Text("اقلام فاکتور (${items.size} ردیف):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کالا و تعداد", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("فی", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                            Text("مبلغ کل", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f))
                        }

                        items.forEachIndexed { idx, itm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text("${idx + 1}. ${itm.productName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("${itm.quantity} ${itm.unitName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(NumberFormatUtils.formatPrice(itm.unitPrice), fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                                Text(NumberFormatUtils.formatPrice(itm.totalPrice), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.3f))
                            }
                            if (idx < items.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Totals & Settlement
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("جمع اقلام:", fontSize = 12.sp)
                            Text(NumberFormatUtils.formatPrice(inv.subtotalAmount), fontSize = 12.sp)
                        }
                        if (inv.discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("تخفیف:", fontSize = 12.sp)
                                Text(NumberFormatUtils.formatPrice(inv.discountAmount), color = AccountingDebitGreen, fontSize = 12.sp)
                            }
                        }
                        if (inv.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("مالیات بر ارزش افزوده:", fontSize = 12.sp)
                                Text(NumberFormatUtils.formatPrice(inv.taxAmount), fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ کل قابل پرداخت:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(NumberFormatUtils.formatPrice(inv.totalAmount), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        val settlementSummary = buildString {
                            append("شیوه تسویه: ${inv.paymentType}")
                            if (inv.cashAmount > 0) append(" | نقد: ${NumberFormatUtils.formatPrice(inv.cashAmount)}")
                            if (inv.posAmount > 0) append(" | پوز: ${NumberFormatUtils.formatPrice(inv.posAmount)}")
                            if (inv.chequeAmount > 0) append(" | چک: ${NumberFormatUtils.formatPrice(inv.chequeAmount)}")
                            if (inv.creditAmount > 0) append(" | نسیه: ${NumberFormatUtils.formatPrice(inv.creditAmount)}")
                        }
                        Text(
                            text = settlementSummary,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
