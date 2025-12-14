package com.example.notification.`kafka-consume`

import com.example.notification.ResultCode
import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.event.NotificationEvent
import com.example.notification.service.NotificationServerSender
import com.example.notification.service.log.NotificationLogService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
class KafkaMeesageConsumerImplTest(
) {
    private lateinit var consumer: MessageConsumer

    private val sender: NotificationServerSender = mockk()

    private val acknowledgment = mockk<Acknowledgment>()

    private val notificationLogService: NotificationLogService = mockk()

    @BeforeEach
    fun init() {
        consumer = KafkaMessageConsumerImpl(sender, notificationLogService)
        justRun { acknowledgment.acknowledge() }
    }

    @Test
    fun `PENDING 상태에서 send가 성공하면 SUCCESS로 종료된다`() {
        // given
        val event = NotificationEvent(eventId = UUID.randomUUID().toString())
        val pendingLog = mockk<NotificationLog>(relaxed=true)

        every {
            notificationLogService.updateNotification(event)
        } returns pendingLog

        every {
            pendingLog.status
        } returns NotificationStatus.PENDING

        every {
            sender.send(event)
        } returns ResultCode.SUCCESS

        // when
        consumer.consume(event, acknowledgment)

        // then
        verify(exactly = 1) { sender.send(event) }
        verify(exactly = 0) { pendingLog.markFail() }
        verify(exactly = 1) { pendingLog.markSuccess() }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `PENDING 상태에서 send가 실패하면 FAIL로 종료된다`() {
        // given
        val event = NotificationEvent(eventId = UUID.randomUUID().toString())
        val pendingLog = mockk<NotificationLog>(relaxed=true)

        every {
            notificationLogService.updateNotification(event)
        } returns pendingLog

        every {
            pendingLog.status
        } returns NotificationStatus.PENDING

        every {
            sender.send(event)
        } returns ResultCode.FAIL

        // when
        consumer.consume(event, acknowledgment)

        // then
        verify(exactly = 1) { sender.send(event) }
        verify(exactly = 1) { pendingLog.markFail() }
        verify(exactly = 0) { pendingLog.markSuccess() }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `PENDING 상태에서 send 중 예외 발생 시 FAIL로 종료된다`() {
        // given
        val event = NotificationEvent(eventId = UUID.randomUUID().toString())
        val pendingLog = mockk<NotificationLog>(relaxed=true)

        every {
            notificationLogService.updateNotification(event)
        } returns pendingLog

        every {
            pendingLog.status
        } returns NotificationStatus.PENDING

        every {
            sender.send(event)
        } throws RuntimeException("test exception")

        // when
        consumer.consume(event, acknowledgment)

        // then
        verify(exactly = 1) { sender.send(event) }
        verify(exactly = 1) { pendingLog.markFail() }
        verify(exactly = 0) { pendingLog.markSuccess() }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `SUCCESS 상태의 이벤트는 send 하지 않는다`() {
        // given
        val event = NotificationEvent(eventId = UUID.randomUUID().toString())
        val pendingLog = mockk<NotificationLog>(relaxed=true)

        every {
            notificationLogService.updateNotification(event)
        } returns pendingLog

        every {
            pendingLog.status
        } returns NotificationStatus.SUCCESS

        // when
        consumer.consume(event, acknowledgment)

        // then
        verify(exactly = 0) { sender.send(event) }
        verify(exactly = 0) { pendingLog.markFail() }
        verify(exactly = 0) { pendingLog.markSuccess() }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }
}
