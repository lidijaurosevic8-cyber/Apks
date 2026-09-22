package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {
    @Query("SELECT * FROM benchmark_runs ORDER BY timestamp DESC")
    fun getAllRuns(): Flow<List<BenchmarkEntity>>

    @Query("SELECT * FROM benchmark_runs WHERE id = :id LIMIT 1")
    suspend fun getRunById(id: Long): BenchmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(entity: BenchmarkEntity): Long

    @Query("DELETE FROM benchmark_runs WHERE id = :id")
    suspend fun deleteRunById(id: Long)

    @Query("DELETE FROM benchmark_runs")
    suspend fun clearAll()
}
