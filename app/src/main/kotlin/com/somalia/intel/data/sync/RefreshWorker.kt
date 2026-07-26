package com.somalia.intel.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.somalia.intel.data.RssFetcher
import com.somalia.intel.data.db.AppDatabase
import com.somalia.intel.data.repository.NewsRepository

/**
 * WorkManager worker — runs every 15 minutes in the background to keep
 * the Room cache fresh even when the app is not in the foreground.
 */
class RefreshWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db      = AppDatabase.getInstance(applicationContext)
        val repo    = NewsRepository(db, RssFetcher())
        return repo.refresh()
            .map { Result.success() }
            .getOrElse { Result.retry() }
    }
}
