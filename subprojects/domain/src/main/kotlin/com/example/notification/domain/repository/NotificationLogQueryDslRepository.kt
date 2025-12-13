package com.example.notification.domain.repository

import com.example.notification.domain.entity.log.NotificationLog
import com.example.notification.domain.entity.log.QNotificationLog.notificationLog
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.enums.NotificationStatus
import com.example.notification.domain.extension.fetchPage
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface NotificationLogQueryDslRepository {
    fun findRecentLogs(
        requesterId: String,
        from: LocalDate,
        to: LocalDate,
        status: NotificationStatus?,
        channel: Channel?,
        userId: String?,
        pageable: Pageable
    ): Page<NotificationLog>
}

class NotificationLogQueryDslRepositoryImpl: QuerydslRepositorySupport(NotificationLog::class.java), NotificationLogQueryDslRepository {
    private fun statusEq(status: NotificationStatus?): BooleanExpression? {
        return status?.let { notificationLog.status.eq(it) }
    }

    private fun channelEq(channel: Channel?): BooleanExpression? {
        return channel?.let { notificationLog.channel.eq(it) }
    }

    private fun userIdEq(userId: String?): BooleanExpression? {
        return userId?.let { notificationLog.userId.eq(userId) }
    }

    override fun findRecentLogs(
        requesterId: String,
        from: LocalDate,
        to: LocalDate,
        status: NotificationStatus?,
        channel: Channel?,
        userId: String?,
        pageable: Pageable
    ): Page<NotificationLog> {
        return from(notificationLog)
            .where(
                notificationLog.requesterId.eq(requesterId),
                notificationLog.createdAt.goe(from.atStartOfDay()), // 조회 시작 날짜의 하루의 시작부터
                notificationLog.createdAt.loe(to.atTime(23, 59, 59)), // 조회 끝 날짜의 하루의 끝까지
                statusEq(status),
                channelEq(channel),
                userIdEq(userId)
            )
            .orderBy(notificationLog.createdAt.desc())
            .fetchPage(pageable, querydsl)
    }
}