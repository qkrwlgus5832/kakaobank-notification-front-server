package com.example.notification.scheduler

import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.repository.NotificationLogRepository
import com.example.notification.service.NotificationPublisher
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import kotlin.test.Test

class ReservedNotificationSchedulerTest {

    private lateinit var scheduler: ReservedNotificationScheduler

    private val logRepository: NotificationLogRepository = mockk()
    private val notificationPublisher: NotificationPublisher = mockk()

    @BeforeEach
    fun setUp() {
        scheduler = ReservedNotificationScheduler(
            logRepository,
            notificationPublisher
        )
    }

    @Test
    fun `예약 시간이 지난 알림들을 조회하여 publish 한다`() {
        // given
        val now = LocalDateTime.now()

        val log1 = NotificationLog(
            userId = "user1",
            requesterId = "req1",
            channel = Channel.EMAIL,
            status = NotificationStatus.PENDING,
            sendAt = now.minusMinutes(5),
            eventId = "event-1",
            title = "title1",
            contents = "content1",
            retryCount = 0
        )

        val log2 = NotificationLog(
            userId = "user2",
            requesterId = "req2",
            channel = Channel.SMS,
            status = NotificationStatus.PENDING,
            sendAt = now.minusMinutes(1),
            eventId = "event-2",
            title = "title2",
            contents = "content2",
            retryCount = 0
        )

        every {
            logRepository.findReservableLogs(any())
        } returns listOf(log1, log2)

        justRun {
            notificationPublisher.publish(any())
        }

        // when
        scheduler.sendReservedNotifications()

        // then
        verify(exactly = 1) {
            logRepository.findReservableLogs(any())
        }

        verify(exactly = 2) {
            notificationPublisher.publish(any())
        }
    }

    @Test
    fun `예약 발송 대상이 없으면 publish 하지 않는다`() {
        // given
        every {
            logRepository.findReservableLogs(any())
        } returns emptyList()

        // when
        scheduler.sendReservedNotifications()

        // then
        verify(exactly = 1) {
            logRepository.findReservableLogs(any())
        }

        verify(exactly = 0) {
            notificationPublisher.publish(any())
        }
    }
}