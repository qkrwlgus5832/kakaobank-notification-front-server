package com.example.notification.service

import com.example.notification.domain.enums.Channel
import com.example.notification.domain.event.NotificationEvent
import com.example.notification.infra.kafka.client.KafkaMessagePublisher
import com.example.notification.infra.kafka.properties.KafkaProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

class NotificationPublisherTest {

    private val kafkaProperties = mockk<KafkaProperties>()
    private val kafkaMessagePublisher = mockk<KafkaMessagePublisher>(relaxed = true)

    private val publisher = NotificationPublisher(
        kafkaProperties,
        kafkaMessagePublisher
    )

    @Test
    fun `이벤트를 Kafka로 publish 한다`() {
        // given
        every { kafkaProperties.notification.topic } returns "notifications-topic"

        val event = mockEvent()

        // when
        publisher.publish(event)

        // then
        verify {
            kafkaMessagePublisher.publish(
                "notifications-topic",
                "${event.channel}:${event.target}",
                event
            )
        }
    }

    private fun mockEvent(
        reserveTime: LocalDateTime = LocalDateTime.now()
    ) = NotificationEvent(
        eventId = UUID.randomUUID().toString(),
        target = "테스트유저",
        requesterId = "테스트요청아이디",
        channel = Channel.EMAIL,
        title = "제목",
        contents = "내용",
        reserveTime = reserveTime
    )
}