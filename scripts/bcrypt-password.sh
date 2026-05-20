#!/bin/bash
# bcrypt-password.sh - Утилита для генерации bcrypt хешей паролей

# Требует установки java и возможности запуска Spring утилит
# Или используйте Kotlin REPL

if [ -z "$1" ]; then
    echo "Usage: $0 <password>"
    echo "Example: $0 my_secure_password123"
    exit 1
fi

PASSWORD=$1

# Спосбо 1: Через Kotlin REPL (если установлен Kotlin)
if command -v kotlin &> /dev/null; then
    echo "Генерируем bcrypt хеш через Kotlin..."
    kotlin -classpath "build/libs/*" << EOF
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

val encoder = BCryptPasswordEncoder(10)
val hash = encoder.encode("$PASSWORD")
println("Bcrypt hash: \$hash")
EOF
    exit 0
fi

# Способ 2: Через PostgreSQL pgcrypto (если БД доступна)
if command -v psql &> /dev/null; then
    echo "Генерируем bcrypt хеш через PostgreSQL (требует подключение)..."
    psql -U postgres -d antiplagiat -c "SELECT crypt('$PASSWORD', gen_salt('bf', 10));" 2>/dev/null

    if [ $? -eq 0 ]; then
        exit 0
    fi
fi

# Способ 3: Online (НЕ рекомендуется для production!)
echo ""
echo "⚠️  Требуется установка Java/Kotlin или доступ к PostgreSQL"
echo ""
echo "Альтернативные способы:"
echo "1. Используйте PostgreSQL: SELECT crypt('$PASSWORD', gen_salt('bf', 10));"
echo "2. Используйте online: https://bcrypt-generator.com/ (ТОЛЬКО для тестирования!)"
echo "3. Используйте Java программу с Spring Security"
echo ""
echo "Или установите Kotlin:"
echo "  - Windows: choco install kotlin"
echo "  - macOS: brew install kotlin"
echo "  - Linux: https://kotlinlang.org/docs/tutorials/command-line.html"

