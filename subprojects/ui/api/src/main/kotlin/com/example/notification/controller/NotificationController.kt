package com.example.notification.controller


import com.example.notification.factory.NotificationEventFactory
import com.example.notification.request.NotificationLogRequest
import com.example.notification.service.NotificationPublisher
import com.example.notification.request.NotificationSendRequest
import com.example.notification.response.NotificationLogResponse
import com.example.notification.service.log.NotificationLogService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationPublisher: NotificationPublisher,
    private val notificationLogService: NotificationLogService
) {
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

    @GetMapping("/logs")
    fun getNotificationLogs(
        @RequestBody request: NotificationLogRequest
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