package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.model.CartItem
import com.example.data.model.ReceiptData
import com.example.data.repository.MerchantRepository
import com.example.data.util.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MerchantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MerchantRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MerchantRepository(db)
        viewModelScope.launch {
            repository.seedInitialProductsIfEmpty()
        }
    }

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<CashTransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Filters & Search
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")

    // Filtered Products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        products,
        searchQuery,
        selectedCategory
    ) { all, query, category ->
        all.filter { product ->
            val matchesCategory = category == "الكل" || product.category == category
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Receipt (for dialog)
    private val _currentReceipt = MutableStateFlow<ReceiptData?>(null)
    val currentReceipt: StateFlow<ReceiptData?> = _currentReceipt.asStateFlow()

    fun dismissReceipt() {
        _currentReceipt.value = null
    }

    fun showReceiptForSale(saleId: Long) {
        viewModelScope.launch {
            _currentReceipt.value = repository.getSaleReceipt(saleId)
        }
    }

    // Cart operations
    fun addToCart(product: ProductEntity, quantity: Double = 1.0) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(product = product, quantity = quantity))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Long, delta: Double) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val existing = currentList[index]
            val newQty = existing.quantity + delta
            if (newQty <= 0.001) {
                currentList.removeAt(index)
            } else {
                currentList[index] = existing.copy(quantity = newQty)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun checkout(
        discount: Double,
        paid: Double,
        customerName: String,
        paymentMethod: String,
        onSuccess: (ReceiptData) -> Unit = {}
    ) {
        val currentItems = _cart.value
        if (currentItems.isEmpty()) return

        viewModelScope.launch {
            val receipt = repository.checkoutSale(
                items = currentItems,
                discount = discount,
                paid = paid,
                customerName = customerName,
                paymentMethod = paymentMethod
            )
            _cart.value = emptyList()
            _currentReceipt.value = receipt
            onSuccess(receipt)
        }
    }

    // Product Management
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.saveProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustStock(productId: Long, delta: Double) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta)
        }
    }

    // Cashflow Management
    fun addCashTransaction(type: String, category: String, amount: Double, notes: String) {
        viewModelScope.launch {
            repository.recordCashTransaction(type, category, amount, notes)
        }
    }

    fun deleteCashTransaction(transaction: CashTransactionEntity) {
        viewModelScope.launch {
            repository.deleteCashTransaction(transaction)
        }
    }

    // CSV Features
    fun exportProductsCsv(): String {
        return CsvExporter.generateProductsCsv(products.value)
    }

    fun exportSalesCsv(): String {
        return CsvExporter.generateSalesCsv(sales.value)
    }

    fun exportTransactionsCsv(): String {
        return CsvExporter.generateTransactionsCsv(transactions.value)
    }

    fun importProductsCsv(csvContent: String, onFinished: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importProductsFromCsv(csvContent)
            onFinished(count)
        }
    }
}
