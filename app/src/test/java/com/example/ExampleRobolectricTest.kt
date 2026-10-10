package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.IncomeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("التاجر الذكي", appName)
    }

    @Test
    fun `product CRUD operations`() = runBlocking {
        val dao = db.productDao()
        // Create
        val product = ProductEntity(
            name = "طماطم طازجة",
            category = "خضر",
            costPrice = 110.0,
            salePrice = 150.0,
            stockQuantity = 20.0
        )
        val id = dao.insertProduct(product)
        // Read
        val retrieved = dao.getProductById(id)
        assertNotNull(retrieved)
        assertEquals("طماطم طازجة", retrieved?.name)
        // Update
        dao.updateProduct(retrieved!!.copy(salePrice = 160.0))
        val updated = dao.getProductById(id)
        assertEquals(160.0, updated?.salePrice ?: 0.0, 0.01)
        // Delete
        dao.deleteProductById(id)
        assertNull(dao.getProductById(id))
    }

    @Test
    fun `sale CRUD operations`() = runBlocking {
        val dao = db.saleDao()
        // Create
        val sale = SaleEntity(
            invoiceNumber = "INV-2026-001",
            subtotalAmount = 1500.0,
            discountAmount = 100.0,
            totalAmount = 1400.0,
            paidAmount = 1500.0,
            changeAmount = 100.0,
            customerName = "أحمد"
        )
        val saleId = dao.insertSale(sale)
        // Read
        val retrieved = dao.getSaleById(saleId)
        assertNotNull(retrieved)
        assertEquals("INV-2026-001", retrieved?.invoiceNumber)
        // Update
        dao.updateSale(retrieved!!.copy(customerName = "أحمد بن علي"))
        val updated = dao.getSaleById(saleId)
        assertEquals("أحمد بن علي", updated?.customerName)
        // Delete
        dao.deleteSaleById(saleId)
        assertNull(dao.getSaleById(saleId))
    }

    @Test
    fun `expense CRUD operations`() = runBlocking {
        val dao = db.expenseDao()
        // Create
        val expense = ExpenseEntity(
            title = "فاتورة كهرباء",
            category = "فواتير",
            amount = 3200.0,
            notes = "شهر أكتوبر"
        )
        val id = dao.insertExpense(expense)
        // Read
        val retrieved = dao.getExpenseById(id)
        assertNotNull(retrieved)
        assertEquals(3200.0, retrieved?.amount ?: 0.0, 0.01)
        // Update
        dao.updateExpense(retrieved!!.copy(amount = 3500.0))
        val updated = dao.getExpenseById(id)
        assertEquals(3500.0, updated?.amount ?: 0.0, 0.01)
        // Delete
        dao.deleteExpenseById(id)
        assertNull(dao.getExpenseById(id))
    }

    @Test
    fun `income CRUD operations`() = runBlocking {
        val dao = db.incomeDao()
        // Create
        val income = IncomeEntity(
            source = "استثمار إضافي",
            category = "إيداع رأس مال",
            amount = 50000.0,
            notes = "توسيع المحل"
        )
        val id = dao.insertIncome(income)
        // Read
        val retrieved = dao.getIncomeById(id)
        assertNotNull(retrieved)
        assertEquals(50000.0, retrieved?.amount ?: 0.0, 0.01)
        // Update
        dao.updateIncome(retrieved!!.copy(amount = 55000.0))
        val updated = dao.getIncomeById(id)
        assertEquals(55000.0, updated?.amount ?: 0.0, 0.01)
        // Delete
        dao.deleteIncomeById(id)
        assertNull(dao.getIncomeById(id))
    }
}
