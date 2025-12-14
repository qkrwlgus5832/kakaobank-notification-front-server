package com.example.notification.common

import com.example.notification.ResultCode
import com.example.notification.SendResponse
import com.example.notification.properties.NotificationSenderProperties
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class NotificationSenderTest {

    private val webClient = mockk<WebClient>()
    private val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
    private val requestBodySpec = mockk<WebClient.RequestBodySpec>()
    private val responseSpec = mockk<WebClient.ResponseSpec>()

    private val properties = NotificationSenderProperties(
        server = NotificationSenderProperties.Server(
            host = "localhost",
            port = 8089
        )
    )

    @Test
    fun `첫 요청에 SUCCESS면 바로 성공을 반환한다`() {
        // given
        val response = SendResponse(ResultCode.SUCCESS)

        mockWebClient(response)

        // when
        val result = NotificationSender.send(
            request = "request",
            path = "/send",
            webClient = webClient,
            notificationSenderProperties = properties,
            maxRetry = 3,
            delayMillis = 0
        )

        // then
        assertThat(result).isEqualTo(ResultCode.SUCCESS)
    }

    @Test
    fun `모든 재시도가 실패하면 FAIL을 반환한다`() {
        // given
        mockWebClientException()

        // when
        val result = NotificationSender.send(
            request = "request",
            path = "/send",
            webClient = webClient,
            notificationSenderProperties = properties,
            maxRetry = 3,
            delayMillis = 0
        )

        // then
        assertThat(result).isEqualTo(ResultCode.FAIL)
    }

    @Test
    fun `중간에 성공하면 즉시 SUCCESS를 반환한다`() {
        // given
        val failResponse = SendResponse(ResultCode.FAIL)
        val successResponse = SendResponse(ResultCode.SUCCESS)

        mockWebClientSequence(
            failResponse,
            failResponse,
            successResponse
        )

        // when
        val result = NotificationSender.send(
            request = "request",
            path = "/send",
            webClient = webClient,
            notificationSenderProperties = properties,
            maxRetry = 5,
            delayMillis = 0
        )

        // then
        assertThat(result).isEqualTo(ResultCode.SUCCESS)
    }

    @Test
    fun `실패일경우 즉시 FAIL를 반환한다`() {
        // given
        val failResponse = SendResponse(ResultCode.FAIL)

        mockWebClientSequence(
            failResponse
        )

        // when
        val result = NotificationSender.send(
            request = "request",
            path = "/send",
            webClient = webClient,
            notificationSenderProperties = properties,
            maxRetry = 5,
            delayMillis = 0
        )

        // then
        assertThat(result).isEqualTo(ResultCode.FAIL)
    }

    private fun mockWebClient(response: SendResponse) {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        every {
            responseSpec.onStatus(any(), any())
        } returns responseSpec
        every {
            responseSpec.bodyToMono(SendResponse::class.java)
        } returns Mono.just(response)
    }

    private fun mockWebClientException() {
        every { webClient.post() } throws RuntimeException("network error")
    }

    private fun mockWebClientSequence(vararg responses: SendResponse) {
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        every {
            responseSpec.onStatus(any(), any())
        } returns responseSpec
        every {
            responseSpec.bodyToMono(SendResponse::class.java)
        } returnsMany responses.map { Mono.just(it) }
    }
}