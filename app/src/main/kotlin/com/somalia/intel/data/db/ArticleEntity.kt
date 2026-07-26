package com.somalia.intel.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.somalia.intel.data.NewsArticle

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url:         String,
    val title:                   String,
    val summary:                 String,
    val imageUrl:                String?,
    val source:                  String,
    val journalist:              String?,
    val publishedAt:             String,
    val category:                String,
    val fetchedAt:               Long = System.currentTimeMillis(),
)

fun ArticleEntity.toNewsArticle() = NewsArticle(
    title       = title,
    summary     = summary,
    url         = url,
    imageUrl    = imageUrl,
    source      = source,
    journalist  = journalist,
    publishedAt = publishedAt,
    category    = category,
)

fun NewsArticle.toEntity() = ArticleEntity(
    url         = url,
    title       = title,
    summary     = summary,
    imageUrl    = imageUrl,
    source      = source,
    journalist  = journalist,
    publishedAt = publishedAt,
    category    = category,
)
