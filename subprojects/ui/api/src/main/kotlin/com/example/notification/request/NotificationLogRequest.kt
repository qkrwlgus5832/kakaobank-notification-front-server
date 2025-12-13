package com.example.notification.request

import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.service.log.condition.NotificationLogSearchCondition
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class NotificationLogRequest(
    val requesterId: String,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    val from: LocalDate?,
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    val to: LocalDate?,
    val status: String? = null,
    val channel: String? = null,
    val userId: String? = null,
    val page: Int = 0,
    val size: Int = 20
) {
    fun toCondition(
        requesterId: String,
        from: LocalDate?,
        to: LocalDate,
        status: String?,
        channel: String?,
        userId: String?
    ): NotificationLogSearchCondition {
        return NotificationLogSearchCondition(
            requesterId = requesterId,
            from = from,
            to = to,
            status = status?.let { NotificationStatus.valueOf(it) },
            channel = channel?.let { Channel.valueOf(it) },
            userId = userId
        )
    }
}