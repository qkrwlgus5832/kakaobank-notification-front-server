package com.example.notification.response

import com.example.notification.domain.entity.log.NotificationLog
import java.time.LocalDateTime

data class NotificationLogResponse(
    val id: Long,
    val userId: String,
    val channel: String,
    val status: String,
    val title: String,
    val contents: String,
    val sendAt: LocalDateTime,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: NotificationLog): NotificationLogResponse =
            NotificationLogResponse(
                id = entity.id,
                userId = entity.userId,
                channel = entity.channel.toString(),
                status = entity.status.toString(),
                title = entity.title,
                contents = entity.contents,
                sendAt = entity.sendAt,
                createdAt = entity.createdAt
            )
    }
}