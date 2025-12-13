package com.example.notification.`kafka-consume`

import com.example.notification.ResultCode
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.event.NotificationEvent
import com.example.notification.service.NotificationServerSender
import com.example.notification.service.log.NotificationLogService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class KafkaMessageConsumerImpl(
    private val notificationSender: NotificationServerSender,
    private val notificationLogService: NotificationLogService
): KafkaMessageConsumer {

    @KafkaListener(
        groupId = "task",
        topics = ["notifications-topic"]
    )
    override fun consume(
        event: NotificationEvent,
        acknowledgement: Acknowledgment
    ) {
        val log = notificationLogService.updateNotification(event)

        if (log.status == NotificationStatus.PENDING) {
            try {
                val result = notificationSender.send(event)

                if (result == ResultCode.SUCCESS) {
                    log.status = NotificationStatus.SUCCESS
                } else {
                    log.status = NotificationStatus.FAIL
                }
            } catch (exception: Exception) {
                log.status = NotificationStatus.FAIL
            }
        }
        acknowledgement.acknowledge()
    }
}