package com.example.notification.domain.config

import com.example.notification.domain.repository.NotificationLogQueryDslRepositoryImpl
import com.example.notification.domain.repository.NotificationLogRepository
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
class QuerydslTestConfig {

    @PersistenceContext
    lateinit var em: EntityManager

    @Bean
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(em)
    }

    @Bean
    fun notificationLogQueryDslRepositoryImpl(): NotificationLogQueryDslRepositoryImpl  {
        return NotificationLogQueryDslRepositoryImpl ()
    }
}