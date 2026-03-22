package com.rpn.salatetime.domain.repository

import java.io.File

interface ImageRepository {
    suspend fun cacheImage(url: String, forceRefresh: Boolean = false, prefix: String = ""): File?
    fun getCachedFile(url: String): File?
    fun evictCachedFile(url: String)
    suspend fun preload(urls: List<String>)
}