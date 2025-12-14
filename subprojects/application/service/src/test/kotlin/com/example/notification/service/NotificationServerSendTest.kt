package com.example.notification.service

import com.example.notification.EmailSender
import com.example.notification.KakaoTalkSender
import com.example.notification.ResultCode
import com.example.notification.SmsSender
import com.example.notification.domain.enums.Channel
import com.example.notification.domain.event.NotificationEvent
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID


class NotificationServerSenderTest {

    private lateinit var emailSender: EmailSender
    private lateinit var kakaoTalkSender: KakaoTalkSender
    private lateinit var smsSender: SmsSender

    private lateinit var sender: NotificationServerSender

    @BeforeEach
    fun setUp() {
        emailSender = mockk()
        kakaoTalkSender = mockk()
        smsSender = mockk()

        sender = NotificationServerSender(
            emailSender,
            kakaoTalkSender,
            smsSender
        )
    }

    @Test
    fun `SMS 채널이면 SmsSender를 호출한다`() {
        // given
        val event = mockEvent(Channel.SMS)

        every { smsSender.send(any()) } returns ResultCode.SUCCESS

        // when
        val result = sender.send(event)

        // then
        assertThat(result).isEqualTo(ResultCode.SUCCESS)

        verify { smsSender.send(any()) }
        verify(exactly = 0) { kakaoTalkSender.send(any()) }
        verify(exactly = 0) { emailSender.send(any()) }
    }

    @Test
    fun `KAKAO 채널이면 KakaoTalkSender를 호출한다`() {
        // given
        val event = mockEvent(Channel.KAKAO)

        every { kakaoTalkSender.send(any()) } returns ResultCode.SUCCESS

        // when
        val result = sender.send(event)

        // then
        assertThat(result).isEqualTo(ResultCode.SUCCESS)

        verify { kakaoTalkSender.send(any()) }
        verify(exactly = 0) { smsSender.send(any()) }
        verify(exactly = 0) { emailSender.send(any()) }
    }

    @Test
    fun `EMAIL 채널이면 EmailSender를 호출한다`() {
        // given
        val event = mockEvent(Channel.EMAIL)

        every { emailSender.send(any()) } returns ResultCode.SUCCESS

        // when
        val result = sender.send(event)

        // then
        assertThat(result).isEqualTo(ResultCode.SUCCESS)

        verify { emailSender.send(any()) }
        verify(exactly = 0) { smsSender.send(any()) }
        verify(exactly = 0) { kakaoTalkSender.send(any()) }
    }

    private fun mockEvent(channel: Channel) = NotificationEvent(
        eventId = UUID.randomUUID().toString(),
        target = "testTargetId",
        requesterId = "testRequesterId",
        channel = channel,
        title = "testTitle",
        contents = "testContents"
    )
}