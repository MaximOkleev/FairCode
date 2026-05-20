FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache wget

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

ARG JAR_FILE=build/libs/antiplagiat-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
