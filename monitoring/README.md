# Мониторинг

В этой папке лежат файлы для запуска мониторинга приложения:

- `prometheus.yml` — конфигурация Prometheus
- `grafana-dashboard.json` — дашборд Grafana с бизнес-метриками
- `grafana-red-dashboard.json` — дашборд Grafana для RED-метрик API (Rate, Errors, Duration)
- конфигурация логирования Spring Boot лежит в `src/main/resources/monitoring/logback-spring.xml`

Корневой `docker-compose.yaml` теперь содержит и приложение, и мониторинг.

## Что уже есть в приложении

В проекте уже подключены:

- Spring Boot Actuator
- Micrometer Prometheus Registry
- Logstash Logback Encoder

Метрики доступны на:

- `http://localhost:8080/actuator/prometheus`

## Как запустить мониторинг

Из корня проекта поднять весь стек:

```powershell
cd D:\AntiPlagiat
docker compose up -d
```

## Доступные адреса

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Логин/пароль Grafana: `admin / admin`
- Метрики приложения: `http://localhost:8080/actuator/prometheus`

## Настройка Grafana

### Шаг 1: Добавить источник данных Prometheus

1. Открыть `http://localhost:3000`
2. Войти под `admin/admin`
3. Перейти в **Конфигурация → Источники данных**
4. Нажать **Добавить источник данных**
5. Выбрать **Prometheus**
6. В URL указать:

```text
http://prometheus:9090
```

7. Сохранить

### Шаг 2: Импортировать дашборд бизнес-метрик

1. Перейти в **Панели → Импорт**
2. Нажать **Загрузить JSON-файл**
3. Выбрать файл `grafana-dashboard.json` из этой папки
4. Нажать **Загрузить** и затем **Импортировать**

### Шаг 3: Импортировать RED-дашборд API

1. Перейти в **Панели → Импорт**
2. Нажать **Загрузить JSON-файл**
3. Выбрать файл `grafana-red-dashboard.json` из этой папки
4. Нажать **Загрузить** и затем **Импортировать**

Альтернативно можно создать дашборд вручную и добавить графики по метрикам:

### Доступные метрики приложения:

**Метрики проблем/задач:**
- `problem.created.total` — количество созданных задач
- `problem.created.failed.total` — количество ошибок при создании
- `problem.updated.total` — количество обновлений задач
- `problem.update.failed.not_found.total` — попытки обновить несуществующую задачу
- `problem.deleted.total` — количество удалённых задач
- `problem.deleted.failed.total` — количество ошибок при удалении

**Метрики Контестов:**
- `contest_created` — количество созданных контестов
- `contest_updated` — количество обновленных контестов
- `contest_deleted` — количество удаленных контестов
- `contest.create.failed.duration_limit` — ошибки при создании (превышение длительности)
- `contest.create.failed.invalid_admin` — ошибки при создании (неправильный администратор)
- `contest.update.failed.not_found` — ошибки при обновлении (контест не найден)
- `contest.update.failed.duration_limit` — ошибки при обновлении (превышение длительности)

**Метрики Регистрации:**
- `registration.success` — количество успешных регистраций
- `registration.failure` — количество неудачных регистраций

**Метрики Аутентификации:**
- `auth.login.success` — успешные входы
- `auth.login.failed.total` — все ошибки входа
- `auth.login.failed.not_found` — пользователь не найден
- `auth.login.failed.invalid_password` — неверный пароль
- `auth.login.failed.email_not_verified` — почта не подтверждена

**Метрики Верификации Email:**
- `email.verification.sent` — отправленные письма
- `email.verification.rate_limited` — блокировка по лимиту
- `email.verification.token.generated` — сгенерированные токены
- `email.verification.success` — успешная верификация
- `email.verification.failed.invalid_token` — неверный токен
- `email.verification.failed.used_token` — повторное использование токена
- `email.verification.failed.expired_token` — истекший токен
- `email.verification.user_not_found` — пользователь не найден
- `email.verification.skipped.already_verified` — пропуск, почта уже подтверждена

**Метрики Пользователей:**
- `user.created.total` — создано пользователей
- `user.read.total` — успешные чтения
- `user.read.not_found.total` — чтения с отсутствием пользователя
- `user.read.all.total` — запросы списка пользователей
- `user.updated.total` — обновления профиля
- `user.update.not_found.total` — попытка обновить несуществующего
- `user.deleted.total` — удаление профиля

## Примеры бизнес-метрик для дашборда

Пример построения дашборда вручную:

1. **Создано задач** → метрика: `problem.created.total`
2. **Создано контестов** → метрика: `contest.created`
3. **Всего пользователей** → метрика: `registration.success`
4. **История создания задач за последний час** → метрика: `increase(problem.created.total[1h])`
5. **История создания контестов за последний час** → метрика: `increase(contest.created[1h])`
6. **Активные задачи** → метрика: `problem.created.total - problem.deleted.total`
7. **Ошибки при создании задач** → метрика: `increase(problem.created.failed.total[1h])`
8. **Статистика регистрации** → метрики: `increase(registration.success[1h])` и `increase(registration.failure[1h])`

## RED-метрики API

### Что показывает RED-дашборд

1. **Rate** — количество запросов в секунду (`RPS`)
2. **Errors** — количество `4xx` и `5xx` ошибок, а также их доля в процентах
3. **Duration** — среднее время ответа и `p95`

### Используемые метрики Micrometer / Actuator

- `http_server_requests_seconds_count`
- `http_server_requests_seconds_sum`
- `http_server_requests_seconds_bucket`

### Примеры запросов PromQL

- `sum(rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m]))`
- `sum(rate(http_server_requests_seconds_count{status=~"4..",uri!~"/actuator.*"}[5m]))`
- `sum(rate(http_server_requests_seconds_count{status=~"5..",uri!~"/actuator.*"}[5m]))`
- `100 * sum(rate(http_server_requests_seconds_count{status=~"4..|5..",uri!~"/actuator.*"}[5m])) / sum(rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m]))`
- `sum(rate(http_server_requests_seconds_sum{uri!~"/actuator.*"}[5m])) / sum(rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m]))`
- `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[5m])) by (le))`

## Проверка вручную

Посмотреть метрики:

```powershell
curl http://localhost:8080/actuator/prometheus
```

Посмотреть логи:

- `logs/app.json`
- `logs/business.log`
- `logs/debug.log`

Остановить мониторинг:

```powershell
cd D:\AntiPlagiat
docker compose down
```


