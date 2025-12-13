package com.example.notification.domain.entity.log

import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "notification_log",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_notification_event_id",
            columnNames = ["event_id"]
        )
    ],
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
        Index(name = "idx_status_send_at", columnList = "status, send_at"),
        Index(name = "idx_request_user_id_created_at", columnList="requester_id, created_at")
    ]
)
class NotificationLog(
    /** 수신자 */
    @Column(name = "user_id", nullable = false)
    val userId: String,

    /** 요청자 */
    @Column(name = "requester_id", nullable = false)
    val requesterId: String,

    /** 알림 채널 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val channel: Channel,

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: NotificationStatus = NotificationStatus.PENDING,

    var title: String = "",

    var contents: String = "",

    /** 발송 시각 (즉시/예약 공통) */
    @Column(name = "send_at", nullable = false)
    val sendAt: LocalDateTime,

    @Column(name = "event_id", nullable = false)
    val eventId: String,

    /** 재시도 횟수 */
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun markSuccess() {
        this.status = NotificationStatus.SUCCESS
    }

    fun markFail() {
        this.status = NotificationStatus.FAIL
        this.retryCount++
    }

    fun markPending() {
        this.status = NotificationStatus.PENDING
    }
}