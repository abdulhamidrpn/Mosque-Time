package com.rpn.mosquetime.data.local.dto

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rpn.mosquetime.data.local.entity.MosqueInfoEntity
import com.rpn.mosquetime.domain.manager.SyncStatus


data class MosqueMessageDto(
    val messageKey: String,
    val messageUrl: String
)