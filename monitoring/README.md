# Monitoring

В этой папке лежат файлы для запуска мониторинга приложения:

- `prometheus.yml` — конфигурация Prometheus
- `grafana-dashboard.json` — дашборд Grafana с бизнес-метриками
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

### Шаг 1: Добавить Data Source Prometheus

1. Открыть `http://localhost:3000`
2. Войти под `admin/admin`
3. Перейти в **Configuration → Data Sources**
4. Нажать **Add data source**
5. Выбрать **Prometheus**
6. В URL указать:

```text
http://prometheus:9090
```

7. Сохранить

### Шаг 2: Импортировать Business Metrics Dashboard

1. Перейти в **Dashboards → Import**
2. Нажать **Upload JSON file**
3. Выбрать файл `grafana-dashboard.json` из этой папки
4. Нажать **Load** и затем **Import**

Альтернативно, можно создать Dashboard вручную и добавить графики по метрикам:

### Доступные метрики приложения:

**Метрики Проблем/Задач:**
- `problem_created_total` — количество созданных задач
- `problem_updated_total` — количество обновленных задач
- `problem_deleted_total` — количество удаленных задач
- `problem_created.failed.total` — количество ошибок при создании

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

## Примеры бизнес-метрик для дашборда

Пример построения дашборда вручную:

1. **Всего задач в системе** → метрика: `problem_created_total`
2. **Всего контестов** → метрика: `contest_created`
3. **Всего пользователей** → метрика: `registration.success`
4. **История создания задач за последний час** → метрика: `increase(problem_created_total[1h])`
5. **История создания контестов за последний час** → метрика: `increase(contest_created[1h])`
6. **Активные задачи** → метрика: `problem_created_total - problem_deleted_total`
7. **Ошибки при создании задач** → метрика: `increase(problem_created.failed.total[1h])`
8. **Статистика регистрации** → метрики: `increase(registration.success[1h])` и `increase(registration.failure[1h])`

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


