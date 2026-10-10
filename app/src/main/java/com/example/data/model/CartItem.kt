package com.example.data.model

import com.example.data.local.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Double = 1.0,
    val customPrice: Double? = null
) {
    val unitPrice: Double get() = customPrice ?: product.salePrice
    val subtotal: Double get() = unitPrice * quantity
}
