package com.rpn.mosquetime.data.mapper

import com.rpn.mosquetime.data.local.entity.MosqueMessageEntity
import com.rpn.mosquetime.domain.Mapper
import com.rpn.mosquetime.domain.model.Message


// Message Mapper
class MessageMapper : Mapper<List<MosqueMessageEntity>, List<Message>> {

    override fun mapToDomain(entity: List<MosqueMessageEntity>): List<com.rpn.mosquetime.domain.model.Message> {
        return entity.map { messageEntity ->
            com.rpn.mosquetime.domain.model.Message(
                label = messageEntity.messageKey,
                message = messageEntity.localImagePath ?: messageEntity.messageUrl
            )
        }
    }

    override fun mapToEntity(domain: List<com.rpn.mosquetime.domain.model.Message>): List<MosqueMessageEntity> {
        return domain.mapIndexed { index, message ->
            MosqueMessageEntity(
                id = "msg_$index",
                mosqueId = "", // Should be provided separately
                messageKey = message.label,
                messageUrl = message.message,
                localImagePath = message.message,
                orderIndex = index
            )
        }
    }
}
