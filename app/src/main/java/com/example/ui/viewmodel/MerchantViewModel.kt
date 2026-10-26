package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.model.CartItem
import com.example.data.model.ReceiptData
import com.example.data.remote.FirestoreSyncRepository
import com.example.data.repository.MerchantRepository
import com.example.data.util.CsvExporter
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.example.data.remote.FirebaseAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val text: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

class MerchantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MerchantRepository
    val syncRepo: FirestoreSyncRepository = FirestoreSyncRepository(application)
    val aiRepo: FirebaseAiRepository = FirebaseAiRepository()

    // AI States
    private val _storeAnalysisState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val storeAnalysisState: StateFlow<AiUiState> = _storeAnalysisState.asStateFlow()

    private val _marketingState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val marketingState: StateFlow<AiUiState> = _marketingState.asStateFlow()

    private val _chatState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val chatState: StateFlow<AiUiState> = _chatState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MerchantRepository(db)
        viewModelScope.launch {
            repository.seedInitialProductsIfEmpty()
        }

        Firebase.auth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
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

    // Firebase Auth & Cloud Sync
    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        syncRepo.signInWithGoogle(context, credentialManager, scope, onSuccess, onError)
    }

    fun signOut(
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        syncRepo.signOut(credentialManager, scope, onComplete)
    }

    fun syncAllToCloud(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = syncRepo.backupLocalDataToCloud(
                products = products.value,
                sales = sales.value,
                expenses = repository.allExpenses.firstOrNull() ?: emptyList(),
                incomes = repository.allIncomes.firstOrNull() ?: emptyList()
            )
            res.fold(
                onSuccess = { msg -> onComplete(true, msg) },
                onFailure = { err -> onComplete(false, err.localizedMessage ?: "فشل المزامنة") }
            )
        }
    }

    // --- FIREBASE AI LOGIC (GEMINI 3.8 FLASH) INFERENCE ---

    fun runStoreAnalysis() {
        viewModelScope.launch {
            _storeAnalysisState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                aiRepo.analyzeStorePerformanceStream(
                    products = products.value,
                    sales = sales.value,
                    transactions = transactions.value
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _storeAnalysisState.value = AiUiState.Streaming(accumulated.toString())
                }
                _storeAnalysisState.value = AiUiState.Success(accumulated.toString())
            } catch (e: CancellationException) {
                throw e
            } catch (e: TimeoutCancellationException) {
                _storeAnalysisState.value = AiUiState.Error("انتهت مهلة استجابة Gemini. يرجى التحقق من اتصال الإنترنت والمحاولة ثانية.")
            } catch (e: Exception) {
                _storeAnalysisState.value = AiUiState.Error("خطأ في الاتصال بالذكاء الاصطناعي: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun generateMarketingOffer(
        productName: String,
        price: Double,
        discountPercent: Int,
        category: String
    ) {
        viewModelScope.launch {
            _marketingState.value = AiUiState.Loading
            val accumulated = StringBuilder()
            try {
                aiRepo.generateMarketingOfferStream(
                    productName = productName,
                    price = price,
                    discountPercent = discountPercent,
                    category = category
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _marketingState.value = AiUiState.Streaming(accumulated.toString())
                }
                _marketingState.value = AiUiState.Success(accumulated.toString())
            } catch (e: CancellationException) {
                throw e
            } catch (e: TimeoutCancellationException) {
                _marketingState.value = AiUiState.Error("انتهت المهلة. يرجى إعادة المحاولة.")
            } catch (e: Exception) {
                _marketingState.value = AiUiState.Error("فشل توليد العرض: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun askMerchantAssistant(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _chatState.value = AiUiState.Loading
            val storeContext = "المحل يحتوي على ${products.value.size} سلعة بإجمالي ${sales.value.size} فاتورة بيع."
            val accumulated = StringBuilder()
            try {
                aiRepo.askGeminiStream(question, storeContext).collect { chunk ->
                    accumulated.append(chunk)
                    _chatState.value = AiUiState.Streaming(accumulated.toString())
                }
                _chatState.value = AiUiState.Success(accumulated.toString())
            } catch (e: CancellationException) {
                throw e
            } catch (e: TimeoutCancellationException) {
                _chatState.value = AiUiState.Error("انتهت مهلة الإجابة. يرجى إعادة المحاولة.")
            } catch (e: Exception) {
                _chatState.value = AiUiState.Error("حدث خطأ: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    fun resetAiChat() {
        _chatState.value = AiUiState.Idle
    }

    // --- JSON & RTDB DATA IMPORT ---

    fun importRtdbExportData(onFinished: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importRtdbDataset()
            onFinished(count)
        }
    }

    fun importProductsFromJson(jsonText: String, onFinished: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importProductsFromJson(jsonText)
            onFinished(count)
        }
    }
}

