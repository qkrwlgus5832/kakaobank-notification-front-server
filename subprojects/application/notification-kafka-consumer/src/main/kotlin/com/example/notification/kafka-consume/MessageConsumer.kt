package com.example.notification.`kafka-consume`

import com.example.notification.domain.event.NotificationEvent
import org.springframework.kafka.support.Acknowledgment

interface MessageConsumer {
    fun consume(
        event: NotificationEvent,
        acknowledgement: Acknowledgment
    )
}