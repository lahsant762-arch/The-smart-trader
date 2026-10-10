package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // عنوان أو وصف المصروف
    val category: String, // تصنيف المصروف (نقل وتوصيل، إيجار، فواتير، أجور، تلف سلع، أخرى)
    val amount: Double, // المبلغ بالدينار (د.ج)
    val notes: String = "", // تفاصيل وملاحظات إضافية
    val timestamp: Long = System.currentTimeMillis() // وقت وتاريخ التسجيل
)
