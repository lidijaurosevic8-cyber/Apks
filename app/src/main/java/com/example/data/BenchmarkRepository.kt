package com.example.data

import com.example.data.db.BenchmarkDao
import com.example.data.db.BenchmarkEntity
import com.example.model.BenchmarkRunResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BenchmarkRepository(private val dao: BenchmarkDao) {

    val allRuns: Flow<List<BenchmarkRunResult>> = dao.getAllRuns().map { list ->
        list.map { it.toDomainModel() }
    }

    suspend fun getRunById(id: Long): BenchmarkRunResult? {
        return dao.getRunById(id)?.toDomainModel()
    }

    suspend fun saveRun(runResult: BenchmarkRunResult): Long {
        val entity = BenchmarkEntity.fromDomainModel(runResult)
        return dao.insertRun(entity)
    }

    suspend fun deleteRun(id: Long) {
        dao.deleteRunById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
