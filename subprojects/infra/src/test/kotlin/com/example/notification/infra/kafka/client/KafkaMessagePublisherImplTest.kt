package com.example.notification.infra.kafka.client

import com.example.notification.infra.kafka.properties.KafkaProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.concurrent.CompletableFuture
import kotlin.test.Test

class KafkaMessagePublisherImplTest {

    val kafkaTemplate: KafkaTemplate<String, Any> = mockk()

    val kafkaProperties: KafkaProperties = mockk()

    lateinit var publisher: KafkaMessagePublisherImpl

    @BeforeEach
    fun setUp() {
        publisher = KafkaMessagePublisherImpl(
            kafkaTemplate = kafkaTemplate,
            kafkaProperties = kafkaProperties
        )
    }

    @Test
    fun `메시지를 정상적으로 publish 한다`() {
        // given
        val topic = "test-topic"
        val key = "test-key"
        val message = "testMessage"

        val recordMetadata = mockk<RecordMetadata> {
            every { offset() } returns 10L
        }

        val sendResult = mockk<SendResult<String, Any>> {
            every { this@mockk.recordMetadata } returns recordMetadata
        }

        val future = mockk<CompletableFuture<SendResult<String, Any>>> {
            every { get() } returns sendResult
        }

        every { kafkaTemplate.send(topic, key, message) } returns future

        // when & then (예외 없이 실행되면 성공)
        publisher.publish(topic, key, message)

        verify(exactly = 1) {
            kafkaTemplate.send(topic, key, message)
            future.get()
        }
    }

    @Test
    fun `publish 중 예외가 발생하면 예외를 다시 던진다`() {
        // given
        val topic = "test-topic"
        val key = "test-key"
        val message = "hello"

        val future = mockk<CompletableFuture<SendResult<String, Any>>> {
            every { get() } throws RuntimeException("kafka error")
        }

        every { kafkaTemplate.send(topic, key, message) } returns future

        // when & then
        assertThatThrownBy {
            publisher.publish(topic, key, message)
        }.isInstanceOf(RuntimeException::class.java)
            .hasMessage("kafka error")

        verify(exactly = 1) {
            kafkaTemplate.send(topic, key, message)
            future.get()
        }
    }
}