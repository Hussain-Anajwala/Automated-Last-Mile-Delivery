-- Core schema: users, deliveries, delivery_status_history (docs/Backend_Schema.md section 3).
-- Timestamps are timestamptz and written by the application in UTC.

CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name      VARCHAR(120) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(30)  NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_normalized CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'DISPATCHER', 'AGENT'))
);

CREATE SEQUENCE delivery_code_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE deliveries (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    delivery_code       VARCHAR(40)  NOT NULL,
    recipient_name      VARCHAR(120) NOT NULL,
    recipient_phone     VARCHAR(20)  NOT NULL,
    delivery_address    VARCHAR(500) NOT NULL,
    delivery_city       VARCHAR(100) NOT NULL,
    reference_number    VARCHAR(100),
    assigned_agent_id   BIGINT REFERENCES users (id),
    created_by_user_id  BIGINT       NOT NULL REFERENCES users (id),
    status              VARCHAR(30)  NOT NULL,
    failure_reason      VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    delivered_at        TIMESTAMPTZ,
    version             BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uk_deliveries_code UNIQUE (delivery_code),
    CONSTRAINT ck_deliveries_status
        CHECK (status IN ('CREATED', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED')),
    CONSTRAINT ck_deliveries_failed_reason
        CHECK (status <> 'FAILED' OR (failure_reason IS NOT NULL AND btrim(failure_reason) <> '')),
    CONSTRAINT ck_deliveries_delivered_at
        CHECK ((status = 'DELIVERED') = (delivered_at IS NOT NULL)),
    CONSTRAINT ck_deliveries_agent_required
        CHECK (status IN ('CREATED', 'FAILED') OR assigned_agent_id IS NOT NULL)
);

CREATE INDEX ix_deliveries_status_created ON deliveries (status, created_at);
CREATE INDEX ix_deliveries_agent_status ON deliveries (assigned_agent_id, status);
CREATE INDEX ix_deliveries_creator_created ON deliveries (created_by_user_id, created_at);

CREATE TABLE delivery_status_history (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    delivery_id         BIGINT      NOT NULL REFERENCES deliveries (id),
    changed_by_user_id  BIGINT      NOT NULL REFERENCES users (id),
    previous_status     VARCHAR(30),
    new_status          VARCHAR(30) NOT NULL,
    reason              VARCHAR(500),
    changed_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_history_new_status
        CHECK (new_status IN ('CREATED', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED')),
    CONSTRAINT ck_history_previous_status
        CHECK (previous_status IS NULL
               OR previous_status IN ('CREATED', 'ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED'))
);

CREATE INDEX ix_history_delivery_changed ON delivery_status_history (delivery_id, changed_at, id);
