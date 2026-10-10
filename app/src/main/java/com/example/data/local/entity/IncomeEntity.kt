package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val source: String, // مصدر المدخول (مبيعات، إيداع رأس مال، استرداد ديون، مداخيل أخرى)
    val category: String, // تصنيف المدخول
    val amount: Double, // المبلغ بالدينار (د.ج)
    val notes: String = "", // تفاصيل وملاحظات
    val timestamp: Long = System.currentTimeMillis() // تاريخ ووقت المعاملة
)
