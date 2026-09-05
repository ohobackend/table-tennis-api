CREATE TABLE auth_session (
session_id UUID PRIMARY KEY,
user_id INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
refresh_hash VARCHAR(64) NOT NULL UNIQUE,
expires_at TIMESTAMPTZ NOT NULL,
revoked BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_auth_session_user ON auth_session(user_id);
