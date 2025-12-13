package com.example.notification.domain.extension

import com.querydsl.jpa.JPQLQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.Querydsl

fun <T> JPQLQuery<T>.fetchPage(pageable: Pageable, querydsl: Querydsl?): Page<T> {
    return querydsl!!.applyPagination(pageable, this).fetchResults().run {
        PageImpl(results, pageable, total)
    }
}