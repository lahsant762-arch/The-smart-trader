package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CashTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashTransactionDao {
    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CashTransactionEntity>>

    @Query("SELECT * FROM cash_transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<CashTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CashTransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: CashTransactionEntity)

    @Query("SELECT SUM(amount) FROM cash_transactions WHERE type = :type")
    fun getTotalByType(type: String): Flow<Double?>
}
