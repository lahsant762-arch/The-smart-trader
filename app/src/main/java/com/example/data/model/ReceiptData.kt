package com.example.data.model

import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity

data class ReceiptData(
    val sale: SaleEntity,
    val items: List<SaleItemEntity>
)
