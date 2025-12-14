package com.example.notification.service.log

import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.event.NotificationEvent
import com.example.notification.domain.repository.NotificationLogRepository
import com.example.notification.service.log.condition.NotificationLogSearchCondition
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import java.time.LocalDate
import java.util.UUID

class NotificationLogServiceTest {

    private lateinit var repository: NotificationLogRepository
    private lateinit var service: NotificationLogService

    @BeforeEach
    fun setUp() {
        repository = mockk()
        service = NotificationLogService(repository)
    }

    @Test
    fun `FAIL 상태의 로그는 updateNotification시 PENDING으로 변경된다`() {
        // given
        val event = mockEvent()
        val log = mockLog(status = NotificationStatus.FAIL)

        every { repository.findFirstByEventId(event.eventId) } returns log

        // when
        val result = service.updateNotification(event)

        assertThat(result.status).isEqualTo(NotificationStatus.PENDING)
    }

    @Test
    fun `RESERVED 상태의 로그는 updateNotification시 PENDING으로 변경된다`() {
        // given
        val event = mockEvent()
        val log = mockLog(status = NotificationStatus.RESERVED)

        every { repository.findFirstByEventId(event.eventId) } returns log

        // when
        val result = service.updateNotification(event)

        // then
        assertThat(result.status).isEqualTo(NotificationStatus.PENDING)
    }

    @Test
    fun `SUCCESS 상태의 로그는 updateNotification시 변경되지 않는다`() {
        // given
        val event = mockEvent()
        val log = mockLog(status = NotificationStatus.SUCCESS)

        every { repository.findFirstByEventId(event.eventId) } returns log

        // when
        val result = service.updateNotification(event)

        // then
        assertThat(result.status).isEqualTo(NotificationStatus.SUCCESS)
    }

    @Test
    fun `즉시 알림은 PENDING 상태로 저장된다`() {
        // given
        val event = mockEvent()

        every { repository.save(any()) } answers { firstArg() }

        // when
        val result = service.saveInstantNotification(event)

        // then
        assertThat(result.status).isEqualTo(NotificationStatus.PENDING)
        assertThat(result.eventId).isEqualTo(event.eventId)
        verify { repository.save(any()) }
    }

    @Test
    fun `예약 알림은 RESERVED 상태로 저장된다`() {
        // given
        val reserveTime = LocalDateTime.now().plusHours(1)
        val event = mockEvent(reserveTime = reserveTime)

        every { repository.save(any()) } answers { firstArg() }

        // when
        val result = service.saveReserveNotification(event)

        // then
        assertThat(result.status).isEqualTo(NotificationStatus.RESERVED)
        assertThat(result.sendAt).isEqualTo(reserveTime)
        verify { repository.save(any()) }
    }

    @Test
    fun `조회 시작 날짜가 없으면 예외가 발생한다`() {
        // given
        val condition = NotificationLogSearchCondition(
            requesterId = "requesterId",
            from = null,
            to = LocalDate.now(),
        )

        // expect
        assertThrows<IllegalArgumentException> {
            service.getRecentLogs(condition, 0, 10)
        }
    }

    @Test
    fun `조회 기간이 3개월을 초과하면 예외가 발생한다`() {
        // given
        val condition = NotificationLogSearchCondition(
            requesterId = "requesterId",
            from = LocalDate.now().minusMonths(4),
            to = LocalDate.now(),
        )

        // expect
        assertThrows<IllegalArgumentException> {
            service.getRecentLogs(condition, 0, 10)
        }
    }

    @Test
    fun `정상 조건이면 최근 로그를 조회한다`() {
        // given
        val condition = NotificationLogSearchCondition(
            requesterId = "requesterId",
            from = LocalDate.now().minusDays(7),
            to = LocalDate.now()
        )

        every {
            repository.findRecentLogs(
                requesterId = any(),
                from = any(),
                to = any(),
                status = any(),
                channel = any(),
                userId = any(),
                pageable = any()
            )
        } returns PageImpl(emptyList())

        // when
        val result = service.getRecentLogs(condition, 0, 10)

        // then
        assertThat(result.content).isEmpty()
        verify { repository.findRecentLogs(any(), any(), any(), any(), any(), any(), any()) }
    }

    private fun mockLog(status: NotificationStatus) = NotificationLog(
        userId = "user1",
        requesterId = "req1",
        channel = Channel.EMAIL,
        status = status,
        sendAt = LocalDateTime.now(),
        eventId = "event-1",
        title = "title1",
        contents = "content1",
        retryCount = 1
    )

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