package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "خضر", "فواكه", "مواد غذائية", "أخرى"
    val barcode: String = "",
    val costPrice: Double, // سعر الشراء (د.ج)
    val salePrice: Double, // سعر البيع (د.ج)
    val stockQuantity: Double, // الكمية الحالية
    val unit: String = "كغ", // "كغ", "حبة", "علبة", "كيس"
    val iconKey: String = "grocery", // مفتاح الأيقونة المحلية
    val customImageUri: String? = null, // مسار صورة مختارة من الجهاز
    val minStockAlert: Double = 5.0, // تنبيه نقص المخزون
    val updatedAt: Long = System.currentTimeMillis()
)
