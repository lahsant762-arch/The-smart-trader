package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_transactions")
data class CashTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "EXPENSE" أو "INCOME"
    val category: String, // "إيجار", "فواتير", "نقل وتوصيل", "أجور عمال", "تلف بضاعة", "أخرى"
    val amount: Double, // المبلغ بالدينار د.ج
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
