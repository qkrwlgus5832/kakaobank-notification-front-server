package com.example.notification.controller

import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.repository.NotificationLogRepository
import com.example.notification.request.NotificationLogRequest
import com.example.notification.request.NotificationSendRequest
import com.example.notification.service.NotificationPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class NotificationControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var notificationLogRepository: NotificationLogRepository

    @MockkBean
    // 외부의존성 mockking
    lateinit var notificationPublisher: NotificationPublisher

    @Test
    fun `즉시 전송 요청 시 알림을 발행하고 202를 반환한다`() {
        // given
        every { notificationPublisher.publish(any()) } just Runs

        val request = NotificationSendRequest(
            channel = NotificationSendRequest.Channel.KAKAO,
            title = "testTitle",
            message = "testMessage",
            target = "testTarget",
            requesterId = "requesterId",
            reserveTime = null
        )

        // when & then
        mockMvc.perform(
            post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isAccepted)

        // publisher 호출 여부까지 확인 (통합 + 약간의 행위 검증)
        verify(exactly = 1) {
            notificationPublisher.publish(any())
        }
    }

    private fun saveNotificationLog() {
        // given
        val log = NotificationLog(
            requesterId = "requesterId",
            userId = "testTarget",
            status = NotificationStatus.SUCCESS,
            channel = Channel.KAKAO,
            sendAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            eventId = UUID.randomUUID().toString()
        )

        notificationLogRepository.save(log)
    }

    @Test
    fun `알림 로그를 조회한다`() {
        // given
        saveNotificationLog()

        val request = NotificationLogRequest(
            requesterId = "requesterId",
            from = LocalDate.now(),
            to = LocalDate.now(),
            status = null,
            channel = null,
            userId = null,
            page = 0,
            size = 10
        )

        // when & then
        mockMvc.perform(
            get("/notifications/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content").exists())
    }
}