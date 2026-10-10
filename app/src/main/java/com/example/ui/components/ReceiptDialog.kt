package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ReceiptData
import com.example.ui.theme.ProfitGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReceiptDialog(
    receipt: ReceiptData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)
    val formattedDate = dateFormat.format(Date(receipt.sale.timestamp))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with checkmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ProfitGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "تمت عملية البيع بنجاح",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ProfitGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Paper Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "متجر التاجر الذكي للخضر والفواكه",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "فاتورة بيع رقم: ${receipt.sale.invoiceNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "الزبون: ${receipt.sale.customerName} | طريقة الدفع: ${receipt.sale.paymentMethod}",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Items header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "المادة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "الكمية", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "السعر", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "المجموع", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            thickness = 0.5.dp
                        )

                        // Items list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((receipt.items.size * 28).coerceAtMost(160).dp)
                        ) {
                            items(receipt.items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.productName,
                                        fontSize = 12.sp,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = "${item.quantity} ${item.unit}",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${item.unitPrice.toInt()}",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${item.subtotal.toInt()} د.ج",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Totals Summary
                        ReceiptRow(
                            label = "المجموع الجزئي:",
                            value = "${receipt.sale.subtotalAmount.toInt()} د.ج"
                        )
                        if (receipt.sale.discountAmount > 0) {
                            ReceiptRow(
                                label = "الخصم:",
                                value = "- ${receipt.sale.discountAmount.toInt()} د.ج",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        ReceiptRow(
                            label = "المجموع النهائي:",
                            value = "${receipt.sale.totalAmount.toInt()} د.ج",
                            isBold = true,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        ReceiptRow(
                            label = "المدفوع من الزبون:",
                            value = "${receipt.sale.paidAmount.toInt()} د.ج"
                        )
                        if (receipt.sale.changeAmount > 0) {
                            ReceiptRow(
                                label = "المتبقي للزبون (الصرف):",
                                value = "${receipt.sale.changeAmount.toInt()} د.ج",
                                isBold = true,
                                color = ProfitGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "شكراً لزيارتكم! مرحباً بكم دائماً.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share & Print/Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareReceiptAsText(context, receipt)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إتمام")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

private fun shareReceiptAsText(context: Context, receipt: ReceiptData) {
    val sb = StringBuilder()
    sb.append("🛒 متجر التاجر الذكي - إيصال بيع\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("رقم الفاتورة: ${receipt.sale.invoiceNumber}\n")
    sb.append("الزبون: ${receipt.sale.customerName}\n")
    sb.append("طريقة الدفع: ${receipt.sale.paymentMethod}\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    receipt.items.forEach { item ->
        sb.append("• ${item.productName}: ${item.quantity} ${item.unit} × ${item.unitPrice.toInt()} = ${item.subtotal.toInt()} د.ج\n")
    }
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    if (receipt.sale.discountAmount > 0) {
        sb.append("الخصم: ${receipt.sale.discountAmount.toInt()} د.ج\n")
    }
    sb.append("المجموع النهائي: ${receipt.sale.totalAmount.toInt()} د.ج\n")
    sb.append("المدفوع: ${receipt.sale.paidAmount.toInt()} د.ج\n")
    if (receipt.sale.changeAmount > 0) {
        sb.append("الباقي للزبون: ${receipt.sale.changeAmount.toInt()} د.ج\n")
    }
    sb.append("شكراً لتسوقكم معنا!\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "إيصال بيع ${receipt.sale.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الإيصال عبر:"))
}
