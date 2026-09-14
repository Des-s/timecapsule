CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE capsules (
    id UUID PRIMARY KEY,
    creator_id UUID NOT NULL REFERENCES users (id),
    recipient_id UUID NOT NULL REFERENCES users (id),
    title VARCHAR(200) NOT NULL,
    encrypted_content TEXT NOT NULL,
    content_version INT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL,
    logic_operator VARCHAR(16) NOT NULL DEFAULT 'AND',
    next_evaluation_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    locked_at TIMESTAMPTZ,
    unlocked_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,
    CONSTRAINT capsules_status_check CHECK (
        status IN ('DRAFT', 'LOCKED', 'UNLOCKING', 'UNLOCKED', 'EXPIRED', 'CANCELLED')
    )
);

CREATE INDEX idx_capsules_status_next_eval ON capsules (status, next_evaluation_at)
    WHERE status = 'LOCKED';

CREATE TABLE conditions (
    id UUID PRIMARY KEY,
    capsule_id UUID NOT NULL REFERENCES capsules (id) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    config JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    evaluated_at TIMESTAMPTZ,
    satisfied_at TIMESTAMPTZ,
    CONSTRAINT conditions_type_check CHECK (
        type IN ('DATE', 'LOCATION', 'IDENTITY', 'CONNECTIVITY')
    ),
    CONSTRAINT conditions_status_check CHECK (
        status IN ('PENDING', 'TRUE', 'FALSE')
    )
);

CREATE INDEX idx_conditions_capsule_id ON conditions (capsule_id);

CREATE TABLE capsule_events (
    id UUID PRIMARY KEY,
    capsule_id UUID NOT NULL REFERENCES capsules (id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    actor_id UUID REFERENCES users (id),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_capsule_events_capsule_id ON capsule_events (capsule_id, created_at);

CREATE TABLE processed_operations (
    operation_id UUID PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);
