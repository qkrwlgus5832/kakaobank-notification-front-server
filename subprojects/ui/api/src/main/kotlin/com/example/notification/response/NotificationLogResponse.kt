package com.example.notification.response

import com.example.notification.domain.entity.log.NotificationLog
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class NotificationLogResponse(
    @field:Schema(description = "로그 ID", example = "1")
    val id: Long,
    @field:Schema(description = "수신자 ID", example = "user123")
    val userId: String,
    @field:Schema(description = "채널", example = "KAKAO")
    val channel: String,
    @field:Schema(description = "로그 상태", example = "SUCCESS")
    val status: String,
    @field:Schema(description = "메시지 제목", example = "제목입니다")
    val title: String,
    @field:Schema(description = "메시지 내용", example = "내용입니다")
    val contents: String,
    @field:Schema(description = "메시지 발송 예정 시각", example = "2025-12-14T20:00:00")
    val sendAt: LocalDateTime,
    @field:Schema(description = "로그 생성 시각", example = "2025-12-14T20:00:00")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: NotificationLog): NotificationLogResponse =
            NotificationLogResponse(
                id = entity.id,
                userId = entity.userId,
                channel = entity.channel.toString(),
                status = entity.status.toString(),
                title = entity.title,
                contents = entity.contents,
                sendAt = entity.sendAt,
                createdAt = entity.createdAt
            )
    }
}