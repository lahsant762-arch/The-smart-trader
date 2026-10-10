package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CashTransactionEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.model.CartItem
import com.example.data.model.ReceiptData
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MerchantRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val saleDao = database.saleDao()
    private val cashTransactionDao = database.cashTransactionDao()
    private val expenseDao = database.expenseDao()
    private val incomeDao = database.incomeDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()
    val allTransactions: Flow<List<CashTransactionEntity>> = cashTransactionDao.getAllTransactions()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allIncomes: Flow<List<IncomeEntity>> = incomeDao.getAllIncomes()

    suspend fun seedInitialProductsIfEmpty() {
        if (productDao.getCount() == 0) {
            val defaultProducts = listOf(
                ProductEntity(
                    name = "طماطم طازجة",
                    category = "خضر",
                    barcode = "619001",
                    costPrice = 110.0,
                    salePrice = 150.0,
                    stockQuantity = 45.0,
                    unit = "كغ",
                    iconKey = "tomato",
                    minStockAlert = 10.0
                ),
                ProductEntity(
                    name = "بصل أحمر محلي",
                    category = "خضر",
                    barcode = "619002",
                    costPrice = 85.0,
                    salePrice = 120.0,
                    stockQuantity = 60.0,
                    unit = "كغ",
                    iconKey = "onion",
                    minStockAlert = 15.0
                ),
                ProductEntity(
                    name = "بطاطا بيضاء",
                    category = "خضر",
                    barcode = "619003",
                    costPrice = 60.0,
                    salePrice = 85.0,
                    stockQuantity = 120.0,
                    unit = "كغ",
                    iconKey = "potato",
                    minStockAlert = 20.0
                ),
                ProductEntity(
                    name = "شفلور (قرنبيط)",
                    category = "خضر",
                    barcode = "619004",
                    costPrice = 130.0,
                    salePrice = 180.0,
                    stockQuantity = 25.0,
                    unit = "كغ",
                    iconKey = "cauliflower",
                    minStockAlert = 5.0
                ),
                ProductEntity(
                    name = "تفاح أحمر فاخر",
                    category = "فواكه",
                    barcode = "619005",
                    costPrice = 270.0,
                    salePrice = 350.0,
                    stockQuantity = 30.0,
                    unit = "كغ",
                    iconKey = "apple",
                    minStockAlert = 8.0
                ),
                ProductEntity(
                    name = "موز مستورد",
                    category = "فواكه",
                    barcode = "619006",
                    costPrice = 280.0,
                    salePrice = 340.0,
                    stockQuantity = 40.0,
                    unit = "كغ",
                    iconKey = "banana",
                    minStockAlert = 10.0
                ),
                ProductEntity(
                    name = "إجاص / كمثرى",
                    category = "فواكه",
                    barcode = "619007",
                    costPrice = 240.0,
                    salePrice = 320.0,
                    stockQuantity = 20.0,
                    unit = "كغ",
                    iconKey = "pear",
                    minStockAlert = 5.0
                ),
                ProductEntity(
                    name = "رمان بلدي",
                    category = "فواكه",
                    barcode = "619008",
                    costPrice = 180.0,
                    salePrice = 250.0,
                    stockQuantity = 35.0,
                    unit = "كغ",
                    iconKey = "pomegranate",
                    minStockAlert = 7.0
                ),
                ProductEntity(
                    name = "عنب أسود حلو",
                    category = "فواكه",
                    barcode = "619009",
                    costPrice = 220.0,
                    salePrice = 300.0,
                    stockQuantity = 25.0,
                    unit = "كغ",
                    iconKey = "grapes",
                    minStockAlert = 5.0
                ),
                ProductEntity(
                    name = "فراولة طازجة",
                    category = "فواكه",
                    barcode = "619010",
                    costPrice = 320.0,
                    salePrice = 420.0,
                    stockQuantity = 18.0,
                    unit = "كغ",
                    iconKey = "strawberry",
                    minStockAlert = 4.0
                ),
                ProductEntity(
                    name = "توت أزرق فاخر",
                    category = "فواكه",
                    barcode = "619011",
                    costPrice = 450.0,
                    salePrice = 600.0,
                    stockQuantity = 12.0,
                    unit = "علبة",
                    iconKey = "blueberry",
                    minStockAlert = 3.0
                ),
                ProductEntity(
                    name = "تين شوكي (الهندية)",
                    category = "فواكه",
                    barcode = "619012",
                    costPrice = 350.0,
                    salePrice = 500.0,
                    stockQuantity = 22.0,
                    unit = "كغ",
                    iconKey = "prickly_pear",
                    minStockAlert = 5.0
                ),
                ProductEntity(
                    name = "تين أسود طازج",
                    category = "فواكه",
                    barcode = "619013",
                    costPrice = 300.0,
                    salePrice = 400.0,
                    stockQuantity = 15.0,
                    unit = "كغ",
                    iconKey = "fig",
                    minStockAlert = 3.0
                ),
                ProductEntity(
                    name = "برتقال عصير",
                    category = "فواكه",
                    barcode = "619014",
                    costPrice = 140.0,
                    salePrice = 190.0,
                    stockQuantity = 50.0,
                    unit = "كغ",
                    iconKey = "orange",
                    minStockAlert = 10.0
                ),
                ProductEntity(
                    name = "بيض طازج",
                    category = "مواد غذائية",
                    barcode = "619015",
                    costPrice = 20.0,
                    salePrice = 25.0,
                    stockQuantity = 300.0,
                    unit = "حبة",
                    iconKey = "grocery",
                    minStockAlert = 60.0
                )
            )
            productDao.insertProducts(defaultProducts)

            // Seed initial transactions
            cashTransactionDao.insertTransaction(
                CashTransactionEntity(
                    type = "INCOME",
                    category = "إيداع رأس مال",
                    amount = 25000.0,
                    notes = "رصيد افتتاح الصندوق المبدئي",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
            cashTransactionDao.insertTransaction(
                CashTransactionEntity(
                    type = "EXPENSE",
                    category = "نقل وتوصيل",
                    amount = 2500.0,
                    notes = "تكاليف نقل الخضر والفواكه من سوق الجملة",
                    timestamp = System.currentTimeMillis() - 43200000
                )
            )
        }

        if (expenseDao.getExpensesCount() == 0) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = "نقل البضاعة من سوق الجملة",
                    category = "نقل وتوصيل",
                    amount = 2500.0,
                    notes = "تكاليف نقل الخضر والفواكه",
                    timestamp = System.currentTimeMillis() - 43200000
                )
            )
        }

        if (incomeDao.getIncomesCount() == 0) {
            incomeDao.insertIncome(
                IncomeEntity(
                    source = "إيداع رأس مال",
                    category = "إيداع رأس مال",
                    amount = 25000.0,
                    notes = "رصيد افتتاح الصندوق المبدئي",
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }

    // --- PRODUCT CRUD ---
    suspend fun saveProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long) {
        productDao.deleteProductById(id)
    }

    suspend fun adjustStock(productId: Long, delta: Double) {
        if (delta >= 0) {
            productDao.addStock(productId, delta)
        } else {
            productDao.deductStock(productId, -delta)
        }
    }

    // --- SALE CRUD ---
    suspend fun checkoutSale(
        items: List<CartItem>,
        discount: Double,
        paid: Double,
        customerName: String,
        paymentMethod: String
    ): ReceiptData {
        return database.withTransaction {
            val subtotal = items.sumOf { it.subtotal }
            val total = (subtotal - discount).coerceAtLeast(0.0)
            val change = (paid - total).coerceAtLeast(0.0)
            val dateStr = SimpleDateFormat("yyMMdd-HHmm", Locale.US).format(Date())
            val invoiceNumber = "INV-$dateStr"

            val saleEntity = SaleEntity(
                invoiceNumber = invoiceNumber,
                timestamp = System.currentTimeMillis(),
                subtotalAmount = subtotal,
                discountAmount = discount,
                totalAmount = total,
                paidAmount = paid,
                changeAmount = change,
                customerName = customerName.ifBlank { "زبون عام" },
                paymentMethod = paymentMethod
            )

            val saleId = saleDao.insertSale(saleEntity)

            val saleItemEntities = items.map { cartItem ->
                // Deduct stock from inventory
                productDao.deductStock(cartItem.product.id, cartItem.quantity)

                SaleItemEntity(
                    saleId = saleId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    unitPrice = cartItem.unitPrice,
                    costPrice = cartItem.product.costPrice,
                    quantity = cartItem.quantity,
                    unit = cartItem.product.unit,
                    subtotal = cartItem.subtotal
                )
            }

            saleDao.insertSaleItems(saleItemEntities)

            // Record cash income for the cashbox register
            cashTransactionDao.insertTransaction(
                CashTransactionEntity(
                    type = "INCOME",
                    category = "مبيعات المحل",
                    amount = total,
                    notes = "فاتورة بيع $invoiceNumber لـ ${saleEntity.customerName}",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Record in dedicated Income table
            incomeDao.insertIncome(
                IncomeEntity(
                    source = "مبيعات المحل",
                    category = "مبيعات المحل",
                    amount = total,
                    notes = "فاتورة بيع $invoiceNumber",
                    timestamp = System.currentTimeMillis()
                )
            )

            ReceiptData(
                sale = saleEntity.copy(saleId = saleId),
                items = saleItemEntities
            )
        }
    }

    suspend fun getSaleReceipt(saleId: Long): ReceiptData? {
        val sale = saleDao.getSaleById(saleId) ?: return null
        val items = saleDao.getSaleItemsDirect(saleId)
        return ReceiptData(sale, items)
    }

    suspend fun updateSale(sale: SaleEntity) {
        saleDao.updateSale(sale)
    }

    suspend fun deleteSale(sale: SaleEntity) {
        saleDao.deleteSale(sale)
    }

    suspend fun deleteSaleById(saleId: Long) {
        saleDao.deleteSaleById(saleId)
    }

    // --- EXPENSE CRUD ---
    suspend fun insertExpense(expense: ExpenseEntity): Long {
        cashTransactionDao.insertTransaction(
            CashTransactionEntity(
                type = "EXPENSE",
                category = expense.category,
                amount = expense.amount,
                notes = expense.title + if (expense.notes.isNotBlank()) " - ${expense.notes}" else "",
                timestamp = expense.timestamp
            )
        )
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    // --- INCOME CRUD ---
    suspend fun insertIncome(income: IncomeEntity): Long {
        cashTransactionDao.insertTransaction(
            CashTransactionEntity(
                type = "INCOME",
                category = income.category,
                amount = income.amount,
                notes = income.source + if (income.notes.isNotBlank()) " - ${income.notes}" else "",
                timestamp = income.timestamp
            )
        )
        return incomeDao.insertIncome(income)
    }

    suspend fun updateIncome(income: IncomeEntity) {
        incomeDao.updateIncome(income)
    }

    suspend fun deleteIncome(income: IncomeEntity) {
        incomeDao.deleteIncome(income)
    }

    suspend fun deleteIncomeById(id: Long) {
        incomeDao.deleteIncomeById(id)
    }

    // --- CASH TRANSACTIONS ---
    suspend fun recordCashTransaction(
        type: String,
        category: String,
        amount: Double,
        notes: String
    ): Long {
        if (type == "EXPENSE") {
            expenseDao.insertExpense(
                ExpenseEntity(
                    title = category,
                    category = category,
                    amount = amount,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
            )
        } else {
            incomeDao.insertIncome(
                IncomeEntity(
                    source = category,
                    category = category,
                    amount = amount,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return cashTransactionDao.insertTransaction(
            CashTransactionEntity(
                type = type,
                category = category,
                amount = amount,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCashTransaction(transaction: CashTransactionEntity) {
        cashTransactionDao.deleteTransaction(transaction)
    }

    // CSV Import / Export Utilities
    suspend fun importProductsFromCsv(csvText: String): Int {
        val lines = csvText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) return 0

        var importedCount = 0
        val startIndex = if (lines[0].contains("Name", ignoreCase = true) || lines[0].contains("الاسم", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val tokens = line.split(",").map { it.trim().trim('\"') }
            if (tokens.size >= 3) {
                val name: String
                val salePrice: Double
                val stock: Double
                val category: String

                if (tokens[0].toLongOrNull() != null && tokens.size >= 4) {
                    name = tokens[1]
                    salePrice = tokens[2].toDoubleOrNull() ?: 100.0
                    stock = tokens[3].toDoubleOrNull() ?: 10.0
                    category = if (tokens.size > 4) tokens[4] else "عام"
                } else {
                    name = tokens[0]
                    salePrice = tokens[1].toDoubleOrNull() ?: 100.0
                    stock = tokens[2].toDoubleOrNull() ?: 10.0
                    category = if (tokens.size > 3) tokens[3] else "عام"
                }

                if (name.isNotBlank()) {
                    val cost = (salePrice * 0.75).coerceAtLeast(1.0)
                    productDao.insertProduct(
                        ProductEntity(
                            name = name,
                            category = category,
                            salePrice = salePrice,
                            costPrice = cost,
                            stockQuantity = stock,
                            unit = "كغ",
                            iconKey = "grocery"
                        )
                    )
                    importedCount++
                }
            }
        }
        return importedCount
    }
}
