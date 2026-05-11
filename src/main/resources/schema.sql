CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'BASIC')),
    reset_token VARCHAR(255),
    reset_token_expiry TIMESTAMP
);

-- Таблица для хранения токенов верификации email
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_email_verification_tokens_token_hash ON email_verification_tokens(token_hash);

-- Инициализация тестовых пользователей для разработки/тестирования
-- Пароль: admin123
-- BCrypt хеш: $2a$10$7.6Z/LL/6AvQxZ7pI/lUWOD5f1hK9W8K2z3L3J8R9V5M2X7Y9Z1a
INSERT INTO users (login, email, email_verified, password_hash, role)
SELECT 'admin', 'admin@example.com', true, '$2a$10$7.6Z/LL/6AvQxZ7pI/lUWOD5f1hK9W8K2z3L3J8R9V5M2X7Y9Z1a', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'admin');

INSERT INTO users (login, email, email_verified, password_hash, role)
SELECT 'user1', 'user1@example.com', true, '$2a$10$7.6Z/LL/6AvQxZ7pI/lUWOD5f1hK9W8K2z3L3J8R9V5M2X7Y9Z1a', 'BASIC'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE login = 'user1');

CREATE TABLE IF NOT EXISTS contests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    admin_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL,
    duration INT NOT NULL
);

CREATE TABLE IF NOT EXISTS problems (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS contest_problems (
    contest_id BIGINT NOT NULL REFERENCES contests(id) ON DELETE CASCADE,
    problem_id BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    PRIMARY KEY (contest_id, problem_id)
);

CREATE TABLE IF NOT EXISTS solutions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    problem_id BIGINT NOT NULL REFERENCES problems(id) ON DELETE CASCADE,
    language VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    file_path TEXT NOT NULL,
    code TEXT,
    created_at TIMESTAMP NOT NULL
);