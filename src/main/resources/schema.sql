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

CREATE TABLE IF NOT EXISTS plagiarism_check_runs (
    id BIGSERIAL PRIMARY KEY,
    threshold DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    checked_solutions INT NOT NULL DEFAULT 0,
    compared_pairs INT NOT NULL DEFAULT 0,
    matches INT NOT NULL DEFAULT 0,
    groups INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_plagiarism_check_runs_status_finished_at
    ON plagiarism_check_runs(status, finished_at);

CREATE TABLE IF NOT EXISTS plagiarism_matches (
    id BIGSERIAL PRIMARY KEY,
    check_run_id BIGINT NOT NULL REFERENCES plagiarism_check_runs(id) ON DELETE CASCADE,
    first_solution_id BIGINT NOT NULL REFERENCES solutions(id) ON DELETE CASCADE,
    second_solution_id BIGINT NOT NULL REFERENCES solutions(id) ON DELETE CASCADE,
    similarity DOUBLE PRECISION NOT NULL,
    threshold DOUBLE PRECISION NOT NULL,
    detected_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_plagiarism_matches_run_solution_pair UNIQUE (check_run_id, first_solution_id, second_solution_id),
    CONSTRAINT chk_plagiarism_matches_order CHECK (first_solution_id < second_solution_id)
);

CREATE INDEX IF NOT EXISTS idx_plagiarism_matches_check_run ON plagiarism_matches(check_run_id);
CREATE INDEX IF NOT EXISTS idx_plagiarism_matches_first_solution ON plagiarism_matches(first_solution_id);
CREATE INDEX IF NOT EXISTS idx_plagiarism_matches_second_solution ON plagiarism_matches(second_solution_id);
CREATE INDEX IF NOT EXISTS idx_plagiarism_matches_detected_at ON plagiarism_matches(detected_at);

CREATE TABLE IF NOT EXISTS import_jobs (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    file_name VARCHAR(255) NOT NULL,
    imported_solutions INT NOT NULL DEFAULT 0,
    created_problems INT NOT NULL DEFAULT 0,
    skipped_files INT NOT NULL DEFAULT 0,
    errors TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_import_jobs_admin_id_started_at ON import_jobs(admin_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_import_jobs_status ON import_jobs(status);

