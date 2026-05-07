# Monitoring

В этой папке лежат файлы для запуска мониторинга приложения:

- `prometheus.yml` — конфигурация Prometheus
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
- Grafana login/password: `admin / admin`
- Метрики приложения: `http://localhost:8080/actuator/prometheus`

## Настройка Grafana

1. Открыть `http://localhost:3000`
2. Войти под `admin/admin`
3. Добавить Data Source типа Prometheus
4. В URL указать:

```text
http://prometheus:9090
```

5. Создать Dashboard и добавить графики по метрикам контеста:
   - `contest_created_total`
   - `contest_updated_total`
   - `contest_deleted_total`
   - `contest_create_failed_duration_limit_total`
   - `contest_create_failed_invalid_admin_total`
   - `contest_update_failed_not_found_total`
   - `contest_update_failed_duration_limit_total`

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


