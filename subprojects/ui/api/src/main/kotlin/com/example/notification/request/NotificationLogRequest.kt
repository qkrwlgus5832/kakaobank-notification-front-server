package com.example.notification.request

import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.service.log.condition.NotificationLogSearchCondition
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class NotificationLogRequest(
    @field:Schema(description = "발송자 ID", example = "user124")
    val requesterId: String,
    @field:Schema(description = "조회 시작 날짜", type= "string", example = "20251214")
    @field:DateTimeFormat(pattern = "yyyyMMdd")
    val from: LocalDate?,
    @field:Schema(description = "조회 종료 날짜", type= "string", example = "20260313")
    @field:DateTimeFormat(pattern = "yyyyMMdd")
    val to: LocalDate?,
    @field:Schema(description = "로그 상태(SUCCESS, FAIL, RESERVED, PENDING 중 하나)", example = "SUCCESS")
    val status: String? = null,
    @field:Schema(description = "채널", example = "KAKAO")
    val channel: String? = null,
    @field:Schema(description = "수신자 ID", example = "user123")
    val userId: String? = null,
    @field:Schema(description = "페이징 처리 페이지 순번", example = "0")
    val page: Int = 0,
    @field:Schema(description = "페이징 처리 페이징 크기", example = "10")
    val size: Int = 20
) {
    fun toCondition(
        requesterId: String,
        from: LocalDate?,
        to: LocalDate,
        status: String?,
        channel: String?,
        userId: String?
    ): NotificationLogSearchCondition {
        return NotificationLogSearchCondition(
            requesterId = requesterId,
            from = from,
            to = to,
            status = status?.let { NotificationStatus.valueOf(it) },
            channel = channel?.let { Channel.valueOf(it) },
            userId = userId
        )
    }
}