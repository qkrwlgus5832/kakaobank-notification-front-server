package com.example.notification.request

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@Schema(description="알림 발송/예약 요청(예약시간 필드 값 유무에 따라 즉시/예약이 달라집니다")
data class NotificationSendRequest (
    @field:Schema(description = "채널")
    val channel: Channel,
    @field:Schema(description = "메시지 제목", example = "제목입니다")
    val title: String,
    @field:Schema(description = "메시지 내용", example = "내용입니다")
    val message: String,
    @field:Schema(description = "수신자 ID", example = "user123")
    val target: String,
    @field:Schema(description = "발송자 ID", example = "user123")
    val requesterId: String,
    @field:Schema(description = "예약시간(예약일 경우 필수값)", type="string", example = "202512141800")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmm")
    val reserveTime: LocalDateTime?
) {
    @Schema(
        description = "알림 채널",
        example = "KAKAO",
    )
    enum class Channel {
        KAKAO,
        SMS,
        EMAIL,
    }
}