package com.example.notification.domain.repository

import com.example.notification.domain.entity.log.NotificationLog
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface NotificationLogRepository : JpaRepository<NotificationLog, Long>, NotificationLogQueryDslRepository {
    fun findFirstByEventId(eventId: String): NotificationLog

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select l from NotificationLog l
        where l.status = 'RESERVED'
          and l.sendAt <= :now
        order by l.createdAt
    """
    )
    fun findReservableLogs(
        @Param("now") now: LocalDateTime
    ): List<NotificationLog>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select l
        from NotificationLog l
        where l.status = 'FAIL'
        order by l.createdAt
    """
    )
    fun findRetryableFailedLogs(now: LocalDateTime): List<NotificationLog>
}