package com.example.notification.service.log.condition

import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import java.time.LocalDate

data class NotificationLogSearchCondition(
    val requesterId: String,
    val from: LocalDate?,
    val to: LocalDate,
    val status: NotificationStatus? = null,
    val channel: Channel? = null,
    val userId: String? = null
)