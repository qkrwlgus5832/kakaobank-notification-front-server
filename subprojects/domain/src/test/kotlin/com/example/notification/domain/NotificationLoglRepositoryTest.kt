package com.example.notification.domain

import com.example.notification.domain.config.QuerydslTestConfig
import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.repository.NotificationLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes=[QuerydslTestConfig::class])
@EntityScan(basePackages = ["com.example.notification.domain"])
@EnableJpaRepositories(basePackages = ["com.example.notification.domain"])
@Import(QuerydslTestConfig::class)
@Transactional
class NotificationLogRepositoryTest {
    @Autowired
    lateinit var notificationLogRepository: NotificationLogRepository

    companion object {
        private const val REQUESTER_ID = "requesterId"
    }
    private fun saveLog(
        requesterId: String = REQUESTER_ID,
        userId: String = "userId",
        status: NotificationStatus = NotificationStatus.SUCCESS,
        channel: Channel = Channel.KAKAO,
        createdAt: LocalDateTime
    ): NotificationLog {
        val log = NotificationLog(
            requesterId = requesterId,
            userId = userId,
            status = status,
            channel = channel,
            eventId = UUID.randomUUID().toString(),
            sendAt = LocalDateTime.now(),
            createdAt = createdAt
        )
        return notificationLogRepository.save(log)
    }

    @Test
    fun `requesterId와 날짜 범위로 로그를 조회한다`() {
        // given
        val today = LocalDate.now()

        saveLog(createdAt = today.atTime(9, 0))
        saveLog(createdAt = today.atTime(12, 0))
        saveLog(
            requesterId = "other",
            createdAt = today.atTime(15, 0)
        ) // 제외 대상

        // when
        val result = notificationLogRepository.findRecentLogs(
            requesterId = REQUESTER_ID,
            from = today,
            to = today,
            status = null,
            channel = null,
            userId = null,
            pageable = PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(2)
    }

    @Test
    fun `status와 channel 조건으로 필터링된다`() {
        // given
        val today = LocalDate.now()

        saveLog(
            status = NotificationStatus.SUCCESS,
            channel = Channel.KAKAO,
            createdAt = today.atTime(10, 0)
        )

        saveLog(
            status = NotificationStatus.FAIL,
            channel = Channel.EMAIL,
            createdAt = today.atTime(11, 0)
        )

        // when
        val result = notificationLogRepository.findRecentLogs(
            requesterId = REQUESTER_ID,
            from = today,
            to = today,
            status = NotificationStatus.SUCCESS,
            channel = Channel.KAKAO,
            userId = null,
            pageable = PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().status)
            .isEqualTo(NotificationStatus.SUCCESS)
    }

    @Test
    fun `userId 조건이 있으면 해당 유저만 조회된다`() {
        // given
        val today = LocalDate.now()

        saveLog(userId = "user-1", createdAt = today.atTime(10, 0))
        saveLog(userId = "user-2", createdAt = today.atTime(11, 0))

        // when
        val result = notificationLogRepository.findRecentLogs(
            requesterId = REQUESTER_ID,
            from = today,
            to = today,
            status = null,
            channel = null,
            userId = "user-1",
            pageable = PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().userId).isEqualTo("user-1")
    }

    @Test
    fun `createdAt 내림차순으로 정렬되고 paging 된다`() {
        // given
        val today = LocalDate.now()

        saveLog(createdAt = today.atTime(9, 0))
        saveLog(createdAt = today.atTime(10, 0))
        saveLog(createdAt = today.atTime(11, 0))

        // when
        val page = notificationLogRepository.findRecentLogs(
            requesterId = REQUESTER_ID,
            from = today,
            to = today,
            status = null,
            channel = null,
            userId = null,
            pageable = PageRequest.of(0, 2)
        )

        // then
        assertThat(page.content).hasSize(2)
        assertThat(page.content[0].createdAt)
            .isAfter(page.content[1].createdAt)
    }
}