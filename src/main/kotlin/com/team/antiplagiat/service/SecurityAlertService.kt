package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val securityAlertLogger = KotlinLogging.logger {}

@Service
class SecurityAlertService(
    private val resendService: ResendService
) {

    fun sendLoginAlert(user: User) {
        securityAlertLogger.info { "Отправка security alert о входе для пользователя ${user.id}" }
        val loginTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.now().atOffset(ZoneOffset.UTC))

        resendService.send(
            to = user.email,
            subject = "Security Alert: вход в аккаунт Antiplagiat",
            html = """
                <h2>Вход в аккаунт</h2>
                <p>Здравствуйте, ${user.login}.</p>
                <p>В ваш аккаунт Antiplagiat был выполнен вход.</p>
                <p><strong>Время входа:</strong> $loginTime UTC</p>
                <p>Если это были не вы, смените пароль как можно скорее.</p>
            """.trimIndent()
        )
    }
}
