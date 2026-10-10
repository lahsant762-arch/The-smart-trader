package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SaleEntity
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MerchantViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsAndFilesScreen(viewModel: MerchantViewModel) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val receipt by viewModel.currentReceipt.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isImportDialogOpen by remember { mutableStateOf(false) }

    val totalSales = sales.sumOf { it.totalAmount }
    val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalRevenue = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }

    // Net Profit estimate: Total Sales revenue - total operational expenses
    val netEstimatedProfit = totalSales - totalExpenses

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row: التقارير والفواتير vs الملفات وتصدير CSV
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("المبيعات والتقارير", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("إدارة الملفات (CSV / إكسيل)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTabIndex == 0) {
            // Financial Report & Invoices
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Financial KPIs Cards
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "الملخص المالي العام للمحل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("إجمالي المبيعات", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        text = "${totalSales.toInt()} د.ج",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Column {
                                    Text("المصاريف التشغيلية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        text = "${totalExpenses.toInt()} د.ج",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = ExpenseRed
                                    )
                                }

                                Column {
                                    Text("الربح الصافي التقديري", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(
                                        text = "${netEstimatedProfit.toInt()} د.ج",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = ProfitGreen
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "سجل فواتير المبيعات السابقة (${sales.size} فاتورة):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (sales.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد مبيعات مسجلة حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(sales, key = { it.saleId }) { sale ->
                        SaleInvoiceCard(
                            sale = sale,
                            onViewReceipt = {
                                viewModel.showReceiptForSale(sale.saleId)
                            }
                        )
                    }
                }
            }
        } else {
            // Files & CSV Import/Export Screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "تصدير واستيراد البيانات (Excel & Google Sheets):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "يمكنك تصدير جداول البيانات بصيغة CSV متوافقة مع إكسيل باللغة العربية ومشاركتها عبر الواتساب أو حفظها دون إنترنت.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    // Export Products Card
                    FileActionCard(
                        title = "تصدير جرد السلع والمخزون (CSV)",
                        description = "يحتوي على جميع السلع، أسعار الشراء، أسعار البيع، الكمية وقيمة رأس المال.",
                        buttonText = "تصدير ومشاركة السلع",
                        icon = Icons.Default.FileDownload,
                        onAction = {
                            val csv = viewModel.exportProductsCsv()
                            shareCsvText(context, "جرد_السلع_والمخزون.csv", csv)
                        }
                    )
                }

                item {
                    // Export Sales Card
                    FileActionCard(
                        title = "تصدير سجل المبيعات والفواتير (CSV)",
                        description = "يحتوي على أرقام الفواتير، التواريخ، أسماء الزبائن، والمدفوعات.",
                        buttonText = "تصدير ومشاركة المبيعات",
                        icon = Icons.Default.FileDownload,
                        onAction = {
                            val csv = viewModel.exportSalesCsv()
                            shareCsvText(context, "سجل_مبيعات_المحل.csv", csv)
                        }
                    )
                }

                item {
                    // Export Expenses Card
                    FileActionCard(
                        title = "تصدير المصاريف والمداخيل (CSV)",
                        description = "يحتوي على كافة الحركات المالية، التدفق النقدي، والتصنيفات.",
                        buttonText = "تصدير ومشاركة الحركات المالية",
                        icon = Icons.Default.FileDownload,
                        onAction = {
                            val csv = viewModel.exportTransactionsCsv()
                            shareCsvText(context, "سجل_المصاريف_والمداخيل.csv", csv)
                        }
                    )
                }

                item {
                    // Import Products Card
                    FileActionCard(
                        title = "استيراد سلع من ملف CSV / نص",
                        description = "إضافة سلع ومخزون دفعياً من نص أو ملف بتنسيق الاسم، السعر، والكمية.",
                        buttonText = "فتح نافذة الاستيراد",
                        icon = Icons.Default.FileUpload,
                        isPrimary = true,
                        onAction = {
                            isImportDialogOpen = true
                        }
                    )
                }
            }
        }
    }

    // Receipt Dialog for past sale
    receipt?.let { activeReceipt ->
        ReceiptDialog(
            receipt = activeReceipt,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Import Dialog
    if (isImportDialogOpen) {
        var csvInput by remember {
            mutableStateOf(
                "الاسم,سعر البيع,الكمية,التصنيف\n" +
                "خوخ تركي,450,25,فواكه\n" +
                "مانجو استوائي,700,15,فواكه\n" +
                "تمر دقلة نور,600,30,مواد غذائية\n" +
                "حذاء جلدي,2800,8,أخرى"
            )
        }

        AlertDialog(
            onDismissRequest = { isImportDialogOpen = false },
            title = { Text("استيراد سلع من CSV", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "ألصق أسطر CSV بتنسيق (الاسم, السعر, الكمية, التصنيف):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importProductsCsv(csvInput) { imported ->
                            Toast.makeText(context, "تم استيراد $imported سلعة بنجاح!", Toast.LENGTH_SHORT).show()
                            isImportDialogOpen = false
                        }
                    }
                ) {
                    Text("استيراد الآن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isImportDialogOpen = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SaleInvoiceCard(
    sale: SaleEntity,
    onViewReceipt: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    val dateStr = dateFormat.format(Date(sale.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sale.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${sale.totalAmount.toInt()} د.ج",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "الزبون: ${sale.customerName} • ${sale.paymentMethod}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onViewReceipt) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "عرض الإيصال",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FileActionCard(
    title: String,
    description: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean = false,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

private fun shareCsvText(context: Context, filename: String, csvContent: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, filename)
        putExtra(Intent.EXTRA_TEXT, csvContent)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة ملف $filename عبر:"))
}
