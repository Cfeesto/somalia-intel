package com.somalia.intel.data.repository

import com.somalia.intel.data.NewsArticle
import com.somalia.intel.data.RssFetcher
import com.somalia.intel.data.db.AppDatabase
import com.somalia.intel.data.db.toEntity
import com.somalia.intel.data.db.toNewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(private val db: AppDatabase, private val fetcher: RssFetcher) {

    /** Live-updating stream from Room — UI observes this */
    val articles: Flow<List<NewsArticle>> = db.articleDao()
        .observeAll()
        .map { entities -> entities.map { it.toNewsArticle() } }

    /**
     * Fetch fresh articles from all RSS sources and write to Room.
     * Old articles beyond 7 days are pruned to cap DB size.
     */
    suspend fun refresh(): Result<Int> = runCatching {
        val fresh = fetcher.fetchAll()
        db.articleDao().upsertAll(fresh.map { it.toEntity() })
        val cutoff = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
        db.articleDao().deleteOlderThan(cutoff)
        fresh.size
    }
}
