package com.rpn.mosquetime.domain.repository

import java.io.File

interface ImageRepository {
    suspend fun cacheImage(url: String): File?
    fun getCachedFile(url: String): File?
}