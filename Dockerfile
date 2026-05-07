# ---------- Stage 1: runtime ----------
#лёгкий образ с JRE 17 (alpine — минимальный размер)
FROM eclipse-temurin:17-jre-alpine-3.23

# создаём непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# создаём директорию для логов и отдаём её appuser
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# копируем уже собранный bootJar из build/libs
COPY build/libs/antiplagiat-0.0.1-SNAPSHOT.jar app.jar

# копируем конфигурации мониторинга рядом с приложением
COPY src/main/resources/monitoring /app/monitoring

# Создаём папку для логов и даём права appuser
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

# переключаемся на непривилегированного пользователя после копирования
USER appuser

#открываем порт приложения
EXPOSE 8080

#запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]