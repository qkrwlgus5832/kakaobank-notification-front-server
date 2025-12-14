package com.example.notification.controller


import com.example.notification.factory.NotificationEventFactory
import com.example.notification.request.NotificationLogRequest
import com.example.notification.service.NotificationPublisher
import com.example.notification.request.NotificationSendRequest
import com.example.notification.response.NotificationLogResponse
import com.example.notification.service.log.NotificationLogService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@Tag(name = "Notification API", description = "알림 발송/조회 API")
@RequestMapping("/notifications")
class NotificationController(
    private val notificationPublisher: NotificationPublisher,
    private val notificationLogService: NotificationLogService
) {
    @Operation(summary = "알림 발송/예약", description = "알림 발송을 등록합니다 (즉시 혹은 예약)")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [
            Content(
                mediaType  = "application/json",
                examples = [
                    ExampleObject(
                        name = "즉시 발송",
                        summary = "즉시 알림 발송 예제",
                        value = """
                    {
                      "channel": "KAKAO",
                      "title": "제목입니다",
                      "message": "내용입니다",
                      "target": "user123",
                      "requesterId": "user124"
                    }
                    """
                    ),
                    ExampleObject(
                        name = "발송 예약",
                        summary = "알림 발송 예약 예제",
                        value = """
                    {
                      "channel": "KAKAO",
                      "title": "제목입니다",
                      "message": "내용입니다",
                      "target": "user123",
                      "requesterId": "user124",
                      "reserveTime": "202603051800"
                    }
                    """
                    )
                ]
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "요청이 정상적으로 수락되었으며 비동기 처리됩니다."
            )
        ]
    )
    @PostMapping
    fun send(
        @RequestBody request: NotificationSendRequest
    ): ResponseEntity<Void> {
        val event = NotificationEventFactory.from(request)

        if (request.reserveTime == null) { // 즉시 전송이라면
            notificationLogService.saveInstantNotification(event)
            notificationPublisher.publish(event)
        } else {
            notificationLogService.saveReserveNotification(event)
        }
        return ResponseEntity.accepted().build()
    }

    @Operation(summary = "알림 내역 조회", description = "알림 등록/결과 내역을 조회합니다")
    @GetMapping("/logs")
    fun getNotificationLogs(
        @ModelAttribute request: NotificationLogRequest
    ): ResponseEntity<Page<NotificationLogResponse>> {

        val logsPage = notificationLogService.getRecentLogs(
            request.toCondition(
                requesterId = request.requesterId,
                from = request.from,
                to = request.to ?: LocalDate.now(),
                status = request.status,
                channel = request.channel,
                userId = request.userId
            ),
            page = request.page,
            size = request.size
        )

        return ResponseEntity.ok(
            logsPage.map { NotificationLogResponse.from(it) }
        )
    }
}