# Сборка и запуск
```bash
docker compose up
```
Описание:

- Собирает образ приложения (если он ещё не собран)

- Создаёт сеть проекта

- Запускает контейнер postgres

- Ожидает прохождения healthcheck базы данных

- Запускает контейнер app

- Выводит логи в терминал
```bash
docker compose up -d
```
Флаг:

- `-d` — запуск контейнеров в фоновом режиме (detached mode)

Контейнеры продолжают работать после закрытия терминала.
```bash
docker compose up --build
```
Флаг:

- `--build` — принудительно пересобрать образ перед запуском

Используется при изменении кода или Dockerfile.
```bash
docker compose build
```
Описание:

- Собирает образ приложения согласно Dockerfile

- Не запускает контейнеры
```bash
docker compose build --no-cache
```
Флаг:
- `--no-cache` — игнорировать кэш Docker при сборке

Полезно при проблемах со слоями или зависимостями.

# Остановка и управление контейнерами
```bash
docker compose down
```
Описание:

- Останавливает контейнеры

- Удаляет контейнеры

- Удаляет сеть проекта

- Не удаляет volumes
```bash
docker compose down -v
```
Флаг:

- `-v` — удалить volumes

Удаляет данные PostgreSQL.
```bash
docker compose stop
```
Описание:

- Останавливает контейнеры

- Не удаляет контейнеры и сеть
```bash
docker compose start
```
Описание:

- Запускает ранее остановленные контейнеры

- Не выполняет пересборку

# Просмотр состояния и логов
```bash
docker compose ps
```
Описание:

- Показывает статус контейнеров текущего проекта
```bash
docker ps
```
Описание:

- Показывает запущенные контейнеры
```bash
docker ps -a
```
Флаг:

- `-a` — показать все контейнеры, включая остановленные
```bash
docker compose logs app
```
Описание:

- Показывает логи сервиса app
```bash
docker compose logs postgres
```
Описание:

- Показывает логи сервиса postgres
```bash
docker compose logs -f app
```
Флаг:

- `-f` — следить за логами в реальном времени

# Работа с образами
```bash
docker images
```
Описание:

- Показывает список локальных Docker-образов

## docker rmi <image_id>

Описание:

- Удаляет указанный образ

# Очистка Docker
```bash
docker system prune
```
Описание:

- Удаляет неиспользуемые контейнеры

- Удаляет неиспользуемые сети

- Не удаляет volumes и используемые образы
```bash
docker system prune -a
```

Флаг:

- `-a` — удалить все неиспользуемые образы
```bash
docker system prune -a --volumes
```

Флаг:

- `--volumes` — удалить неиспользуемые volumes

# Работа внутри контейнеров
```bash
docker exec -it antiplagiat-app sh
```
Описание:

- Открывает интерактивную shell-сессию внутри контейнера приложения

Флаги:

- `-i` — интерактивный режим

- `-t` — выделить псевдо-TTY
```bash
docker exec -it antiplagiat-db sh
```
Описание:

- Открывает shell внутри контейнера базы данных

# Доступ к сервисам

После запуска приложение доступно по адресу:

`http://localhost:8080`

PostgreSQL доступен:

- Host: localhost

- Port: 5432

- Database: antiplagiat

- Username: postgres

- Password: postgres

# Типовой рабочий процесс
```bash
docker compose down -v
docker compose build --no-cache
docker compose up -d
docker compose logs -f app
```

Описание:

- Остановить старые контейнеры

- Пересобрать образ

- Запустить в фоне

- Проверить логи приложения