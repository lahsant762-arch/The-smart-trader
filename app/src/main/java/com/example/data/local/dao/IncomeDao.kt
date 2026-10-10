package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.IncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    // CREATE (إضافة مدخول أو مجموعة مداخيل)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomes(incomes: List<IncomeEntity>): List<Long>

    // READ (استعلام وقراءة المداخيل)
    @Query("SELECT * FROM incomes ORDER BY timestamp DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE id = :id LIMIT 1")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("SELECT * FROM incomes WHERE category = :category ORDER BY timestamp DESC")
    fun getIncomesByCategory(category: String): Flow<List<IncomeEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM incomes")
    fun getTotalIncomes(): Flow<Double>

    @Query("SELECT COUNT(*) FROM incomes")
    suspend fun getIncomesCount(): Int

    // UPDATE (تعديل وتحديث المدخول)
    @Update
    suspend fun updateIncome(income: IncomeEntity)

    // DELETE (حذف المدخول)
    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteIncomeById(id: Long)
}
