# Описание

Программа предназначена для управления решениями задач и проверки их на плагиат.

Приложение предоставляет REST API для работы с пользователями, задачами, контестами и решениями.

Для документации API подключен Swagger/OpenAPI.

## Мониторинг

Инструкция по Prometheus и Grafana лежит в `monitoring/README.md`.

Ключевые адреса:

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Метрики приложения: `http://localhost:8080/actuator/prometheus`

Логи приложения пишутся в:

- `logs/app.json`
- `logs/business.log`
- `logs/debug.log`

# Security and JWT

The application supports stateless JWT authentication. To enable it set the environment variable or application property:

- `APP_SECURITY_ENABLED=true` (or `app.security.enabled=true`)
- `APP_SECURITY_JWT_SECRET` — secret key for signing tokens (defaults to `dev-secret-change-me`)
- `APP_SECURITY_JWT_EXPIRATION_SECONDS` — token lifetime in seconds (defaults to `86400`)

When security is enabled, endpoints under `/api/**` require a valid `Authorization: Bearer <token>` header. Tokens are issued by authentication endpoints (registration/login).

Examples (curl)
--------------

# Register and get token
```
curl -s -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

# Login and get token
```
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user","password":"password123"}'
```

# Use token to call protected endpoint
```
curl -s http://localhost:8080/api/users -H "Authorization: Bearer <token>"
```

# Основной функционал

- Создание и просмотр пользователей;
- Создание и редактирование задач;
- Создание и управление контестами;
- Создание, просмотр и обновление статуса решений;
- Просмотр OpenAPI/Swagger документации;

# Как это работает

1. Клиент отправляет запрос в REST API;
2. Контроллер валидирует входные данные;
3. Сервис обращается к PostgreSQL через JPA;
4. Swagger UI позволяет просматривать и тестировать доступные эндпоинты.

# Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
