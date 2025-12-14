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


class FailedNotificationRetrySchedulerTest {

    private lateinit var scheduler: FailedNotificationRetryScheduler

    private val logRepository: NotificationLogRepository = mockk()
    private val notificationPublisher: NotificationPublisher = mockk()

    @BeforeEach
    fun setUp() {
        scheduler = FailedNotificationRetryScheduler(
            logRepository,
            notificationPublisher
        )
    }

    @Test
    fun `재시도 가능한 실패 알람들을 조회하여 publish 한다`() {
        // given
        val now = LocalDateTime.now()

        val log1 = NotificationLog(
            userId = "user1",
            requesterId = "req1",
            channel = Channel.EMAIL,
            status = NotificationStatus.FAIL,
            sendAt = now.minusMinutes(10),
            eventId = "event-1",
            title = "title1",
            contents = "content1",
            retryCount = 1
        )

        val log2 = NotificationLog(
            userId = "user2",
            requesterId = "req2",
            channel = Channel.SMS,
            status = NotificationStatus.FAIL,
            sendAt = now.minusMinutes(20),
            eventId = "event-2",
            title = "title2",
            contents = "content2",
            retryCount = 2
        )

        every {
            logRepository.findReservableLogs(any())
        } returns listOf(log1, log2)

        justRun {
            notificationPublisher.publish(any())
        }

        // when
        scheduler.retryFailedNotifications()

        // then
        verify(exactly = 1) {
            logRepository.findReservableLogs(any())
        }

        verify(exactly = 2) {
            notificationPublisher.publish(any())
        }
    }

    @Test
    fun `재시도 대상이 없으면 publish 하지 않는다`() {
        // given
        every {
            logRepository.findReservableLogs(any())
        } returns emptyList()

        // when
        scheduler.retryFailedNotifications()

        // then
        verify(exactly = 1) {
            logRepository.findReservableLogs(any())
        }

        verify(exactly = 0) {
            notificationPublisher.publish(any())
        }
    }
}