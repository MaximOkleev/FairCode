@echo off
REM bcrypt-password.bat - Windows утилита для генерации bcrypt хешей паролей

if "%1"=="" (
    echo Usage: bcrypt-password.bat ^<password^>
    echo Example: bcrypt-password.bat my_secure_password123
    exit /b 1
)

setlocal enabledelayedexpansion
set PASSWORD=%1

echo.
echo Генерируем bcrypt хеш пароля...
echo.

REM Спосбо 1: Через PostgreSQL psql (если установлен)
where psql >nul 2>nul
if %errorlevel% equ 0 (
    echo Используем PostgreSQL для генерации bcrypt хеша...
    echo.
    psql -U postgres -d antiplagiat -c "SELECT crypt('!PASSWORD!', gen_salt('bf', 10));"

    if %errorlevel% equ 0 (
        exit /b 0
    )
)

REM Спосбо 2: Через Java/Spring утилиту
where java >nul 2>nul
if %errorlevel% equ 0 (
    echo Используем Java для генерации bcrypt хеша...
    echo.

    REM Создаем временный Java файл
    set TEMP_FILE=%temp%\BcryptGenerator.java

    (
    echo import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    echo public class BcryptGenerator {
    echo     public static void main(String[] args) {
    echo         BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    echo         String hash = encoder.encode("%PASSWORD%");
    echo         System.out.println("Bcrypt hash: " + hash);
    echo     }
    echo }
    ) > !TEMP_FILE!

    echo Компилируем и запускаем...
    javac !TEMP_FILE! 2>nul

    if exist "%temp%\BcryptGenerator.class" (
        java -cp "%temp%;build/libs/*" BcryptGenerator
        del /q "%temp%\BcryptGenerator.class" >nul 2>&1
    )

    del /q !TEMP_FILE! >nul 2>&1
    exit /b 0
)

REM Способ 3: Использовать это для локального тестирования в Python (если установлен)
where python >nul 2>nul
if %errorlevel% equ 0 (
    echo Используем Python для генерации bcrypt хеша...
    echo.
    python -c "import crypt; print('Bcrypt hash:', crypt.crypt('%PASSWORD%', crypt.METHOD_BLOWFISH))"
    exit /b 0
)

echo.
echo ^^! Требуется одно из:
echo   1. PostgreSQL (psql.exe)
echo   2. Java Development Kit (javac.exe)
echo   3. Python (python.exe)
echo   4. Git Bash или WSL с утилитами bash
echo.
echo Установите одно из указанных выше или используйте:
echo   - Online: https://bcrypt-generator.com/ (ТОЛЬКО для тестирования!)
echo   - PostgreSQL: SELECT crypt('password', gen_salt('bf', 10));
echo.
exit /b 1

