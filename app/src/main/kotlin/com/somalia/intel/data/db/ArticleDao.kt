package com.somalia.intel.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY fetchedAt DESC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY fetchedAt DESC LIMIT 1000")
    suspend fun getAll(): List<ArticleEntity>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    /** Remove articles older than [cutoffMs] to prevent unbounded cache growth */
    @Query("DELETE FROM articles WHERE fetchedAt < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int
}
