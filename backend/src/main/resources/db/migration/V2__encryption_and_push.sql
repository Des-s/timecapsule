ALTER TABLE capsules
    ADD COLUMN encryption_version INT NOT NULL DEFAULT 0,
    ADD COLUMN recipient_key_envelope TEXT;

CREATE TABLE device_push_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expo_push_token VARCHAR(512) NOT NULL,
    platform VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT device_push_tokens_user_token UNIQUE (user_id, expo_push_token)
);

CREATE INDEX idx_device_push_tokens_user_id ON device_push_tokens (user_id);
