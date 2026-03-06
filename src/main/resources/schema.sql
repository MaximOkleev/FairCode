CREATE TABLE solutions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    language VARCHAR(50),
    file_path TEXT,
    date TIMESTAMP
);