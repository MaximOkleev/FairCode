## 🎯 Что нужно сделать (5 минут)

### Шаг 1: Запустить приложение
```powershell
cd C:\Users\timur\Documents\AntiPlagiat
docker compose up -d
```

✅ Приложение будет доступно на `http://localhost:8080`
✅ Prometheus на `http://localhost:9090`
✅ Grafana на `http://localhost:3000`

### Шаг 2: Добавить Data Source в Grafana

1. Открыть `http://localhost:3000`
2. Login: `admin` / `admin`
3. Перейти **Configuration → Data Sources**
4. Нажать **Add data source**
5. Выбрать **Prometheus**
6. URL: `http://prometheus:9090`
7. **Save & Test** ✅

### Шаг 3: Импортировать Dashboard

1. В Grafana нажать **Dashboards → Import**
2. Нажать **Upload JSON file**
3. Выбрать файл: `monitoring/grafana-dashboard.json`
4. Нажать **Load** → **Import** ✅

### Шаг 3.1: Импортировать RED Dashboard

1. Снова открыть **Dashboards → Import**
2. Нажать **Upload JSON file**
3. Выбрать файл: `monitoring/grafana-red-dashboard.json`
4. Нажать **Load** → **Import** 

### Шаг 4: Посмотреть метрики

1. В Grafana откроется дашборд "AntiPlagiat Business Metrics"
2. Должны быть видны графики:
   - Количество созданных задач
   - Всего задач в системе
   - Количество контестов
   - Статистика регистрации
   - И еще 7 графиков...

3. Для технического мониторинга откройте дашборд "AntiPlagiat API RED Dashboard"
4. Там должны быть графики:
   - RPS
   - 4xx / 5xx ошибки
   - Доля ошибок
   - Среднее время ответа и p95

---

## 📊 Где смотреть логи

### Business Logs (INFO)
```powershell
tail -f logs/business.log
```
Содержит: создание, обновление, удаление сущностей

### Debug Logs  
```powershell
tail -f logs/debug.log
```
Содержит: значения переменных, логика ветвлений, детали операций

### Application Logs (JSON)
```powershell
tail -f logs/app.json
```
Содержит: все логи в JSON формате для парсинга

---

## 📈 Как генерировать метрики (для тестирования)

### Создать задачу (увеличит problem_created_total)
```powershell
curl -X POST http://localhost:8080/api/problems `
  -H "Content-Type: application/json" `
  -d '{"name":"Test1","description":"Desc1"}'
```

### Обновить задачу (увеличит problem_updated_total)
```powershell
curl -X PUT http://localhost:8080/api/problems/1 `
  -H "Content-Type: application/json" `
  -d '{"name":"Updated","description":"New"}'
```

### Удалить задачу (увеличит problem_deleted_total)
```powershell
curl -X DELETE http://localhost:8080/api/problems/1
```

### Посмотреть все метрики
```powershell
curl http://localhost:8080/actuator/prometheus | grep problem_
curl http://localhost:8080/actuator/prometheus | grep solution_
curl http://localhost:8080/actuator/prometheus | grep contest_
curl http://localhost:8080/actuator/prometheus | grep registration_
```

---

## 📋 Доступные метрики

### Problem Module
- `problem_created_total` - всего создано задач
- `problem_created_failed_total` - ошибок при создании
- `problem_updated_total` - всего обновлено
- `problem_deleted_total` - всего удалено
- `problem_update_failed_not_found_total` - не найдено при обновлении
- `problem_deleted_failed_total` - ошибок при удалении

### Solution Module
- `solution_created_total` - всего создано решений
- `solution_created_failed_too_many_attempts_total` - превышены попытки
- `solution_status_updated_total` - всего обновлено статусов
- `solution_status_{STATUS}_total` - по каждому статусу (WAITING, ACCEPTED и т.д.)
- `solution_deleted_total` - всего удалено
- `solution_deleted_failed_total` - ошибок при удалении

### Contest Module
- `contest_created` - созданные контесты
- `contest_updated` - обновленные контесты
- `contest_deleted` - удаленные контесты
- `contest.create.failed.*` - ошибки при создании
- `contest.update.failed.*` - ошибки при обновлении

### Registration Module
- `registration_success` - успешные регистрации
- `registration_failure` - неудачные регистрации

---

## 🔍 Примеры PromQL запросов

### Количество созданных задач за час
```promql
increase(problem_created_total[1h])
```

### Всего активных задач (созданных - удаленных)
```promql
problem_created_total - problem_deleted_total
```

### Успешные регистрации за день
```promql
increase(registration_success[24h])
```

### Ошибки при создании решений за час
```promql
increase(solution_created_failed_too_many_attempts_total[1h])
```

### Средний прирост контестов в час
```promql
rate(contest_created[1h])
```

---

## 🛠️ Остановка всего

```powershell
cd C:\Users\timur\Documents\AntiPlagiat
docker compose down
```

---

## 📚 Дополнительная информация

- **IMPLEMENTATION.md** - подробная техдокументация
- **SUMMARY.md** - сводка выполненной работы
- **CHECKLIST.md** - чек-лист требований
- **monitoring/README.md** - инструкции по мониторингу

---

## ✅ Готово!

Ваша система мониторинга полностью настроена и готова к работе. 

Начните с запуска приложения и импорта дашборда - это займет 5 минут!

Вопросы? Смотрите документацию в файлах выше.

