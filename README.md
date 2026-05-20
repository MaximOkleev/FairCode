# Описание

Программа предназначена для управления решениями задач и проверки их на плагиат.

Приложение предоставляет REST API для работы с пользователями, задачами, контестами и решениями.

Для документации API подключён Swagger/OpenAPI.

## Мониторинг

Инструкция по Prometheus и Grafana находится в `monitoring/README.md`.

Ключевые адреса:

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Метрики приложения: `http://localhost:8080/actuator/prometheus`

Логи приложения пишутся в:

- `logs/app.json`
- `logs/business.log`
- `logs/debug.log`

# Безопасность и JWT

Приложение поддерживает аутентификацию без сохранения состояния по JWT. Чтобы включить её, задайте переменную окружения или свойство приложения:

- `APP_SECURITY_ENABLED=true` (или `app.security.enabled=true`)
- `APP_SECURITY_JWT_SECRET` — секретный ключ для подписи токенов (по умолчанию `dev-secret-change-me`)
- `APP_SECURITY_JWT_EXPIRATION_SECONDS` — время жизни токена в секундах (по умолчанию `86400`)

Когда безопасность включена, эндпоинты под `/api/**` требуют валидный заголовок `Authorization: Bearer <token>`. Токены выдаются через эндпоинты аутентификации (регистрация/вход).

Примеры команд curl
--------------

# Регистрация
```
curl -s -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

# Подтверждение email и получение токена
```
curl -s "http://localhost:8080/api/auth/verify-email?token=<verification_token>"
```

# Вход после подтверждения
```
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user","password":"password123"}'
```

Регистрация не выдаёт токен доступа сразу. Токен выдаётся после подтверждения email или после входа в систему.

# Использование токена для вызова защищённого эндпоинта
```
curl -s http://localhost:8080/api/users -H "Authorization: Bearer <token>"
```

# Основной функционал

- Создание и просмотр пользователей;
- Создание и редактирование задач;
- Создание и управление контестами;
- Создание, просмотр и обновление статуса решений;
- Просмотр документации OpenAPI/Swagger;

# Как это работает

1. Клиент отправляет запрос в REST API;
2. Контроллер валидирует входные данные;
3. Сервис обращается к PostgreSQL через JPA;
4. Интерфейс Swagger UI позволяет просматривать и тестировать доступные эндпоинты.

# Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- JSON OpenAPI: `http://localhost:8080/v3/api-docs`
