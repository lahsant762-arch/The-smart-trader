package com.example.data.util

import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    // Prefix UTF-8 BOM so Excel opens Arabic correctly
    private const val BOM = "\uFEFF"

    fun generateProductsCsv(products: List<ProductEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("المعرف,اسم السلعة,التصنيف,الباركود,سعر الشراء (د.ج),سعر البيع (د.ج),الكمية المتوفرة,الوحدة,قيمة المخزون (د.ج)\n")
        products.forEach { p ->
            val stockValue = p.costPrice * p.stockQuantity
            sb.append("${p.id},\"${p.name}\",\"${p.category}\",\"${p.barcode}\",${p.costPrice},${p.salePrice},${p.stockQuantity},\"${p.unit}\",$stockValue\n")
        }
        return sb.toString()
    }

    fun generateSalesCsv(sales: List<SaleEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("رقم الفاتورة,التاريخ والوقت,اسم الزبون,المجموع الجزئي (د.ج),الخصم (د.ج),الصافي المدفوع (د.ج),طريقة الدفع\n")
        sales.forEach { s ->
            val dateStr = dateFormat.format(Date(s.timestamp))
            sb.append("\"${s.invoiceNumber}\",\"$dateStr\",\"${s.customerName}\",${s.subtotalAmount},${s.discountAmount},${s.totalAmount},\"${s.paymentMethod}\"\n")
        }
        return sb.toString()
    }

    fun generateTransactionsCsv(transactions: List<CashTransactionEntity>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        sb.append("المعرف,النوع,التصنيف,المبلغ (د.ج),التاريخ والوقت,ملاحظات\n")
        transactions.forEach { t ->
            val typeStr = if (t.type == "EXPENSE") "مصروف" else "مدخول"
            val dateStr = dateFormat.format(Date(t.timestamp))
            sb.append("${t.id},\"$typeStr\",\"${t.category}\",${t.amount},\"$dateStr\",\"${t.notes}\"\n")
        }
        return sb.toString()
    }
}
