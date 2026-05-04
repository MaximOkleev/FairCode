# ----------Stage 1: build ----------
#используем официальный образ с Gradle и JDK 17
FROM gradle:8.5-jdk17 AS builder

#рабочая директория
WORKDIR /app

#копируем все исходники
COPY . .

#собираем приложение (исключаем тесты для ускорения)
#используем ./gradlew для гарантии правильной версии Gradle
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test --no-daemon

# ---------- Stage 2: runtime ----------
#лёгкий образ с JRE 17 (alpine — минимальный размер)
FROM eclipse-temurin:17-jre-alpine-3.23

# создаём непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# копируем собранный jar из builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# переключаемся на непривилегированного пользователя после копирования
USER appuser

#открываем порт приложения
EXPOSE 8080

#запускаем
ENTRYPOINT ["java", "-jar", "app.jar"]