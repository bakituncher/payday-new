package com.codenzi.payday

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Query("SELECT * FROM budgets WHERE :date BETWEEN startDate AND endDate")
    fun getActiveBudgets(date: Date): Flow<List<Budget>>

    @Query("SELECT * FROM budgets ORDER BY endDate DESC")
    fun getAllBudgets(): Flow<List<Budget>>
}
