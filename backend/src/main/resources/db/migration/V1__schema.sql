CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TYPE user_role   AS ENUM ('ADMIN','OPERATOR','READONLY');
CREATE TYPE user_status AS ENUM ('PENDING','ACTIVE','SUSPENDED');
CREATE TYPE dev_status  AS ENUM ('PENDING_APPROVAL','ONLINE','OFFLINE','REVOKED');
CREATE TYPE exec_status AS ENUM ('PENDING','OK','ERROR','TIMEOUT','DENIED');

CREATE TABLE users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         CITEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role          user_role NOT NULL DEFAULT 'OPERATOR',
  status        user_status NOT NULL DEFAULT 'PENDING',
  mfa_enabled   BOOLEAN NOT NULL DEFAULT FALSE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at TIMESTAMPTZ
);

CREATE TABLE refresh_tokens (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  TEXT NOT NULL UNIQUE,
  family_id   UUID NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked_at  TIMESTAMPTZ
);

CREATE TABLE devices (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  hostname        TEXT NOT NULL,
  os              TEXT NOT NULL,
  arch            TEXT NOT NULL,
  ip_address      INET,
  agent_version   TEXT,
  fingerprint     TEXT NOT NULL UNIQUE,
  credential_hash TEXT,
  status          dev_status NOT NULL DEFAULT 'PENDING_APPROVAL',
  owner_id        UUID NOT NULL REFERENCES users(id),
  approved_at     TIMESTAMPTZ,
  last_seen_at    TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE enrollment_codes (
  code       TEXT PRIMARY KEY,
  created_by UUID NOT NULL REFERENCES users(id),
  expires_at TIMESTAMPTZ NOT NULL,
  used_at    TIMESTAMPTZ,
  device_id  UUID REFERENCES devices(id)
);

CREATE TABLE command_executions (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id   UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
  user_id     UUID NOT NULL REFERENCES users(id),
  verb        TEXT NOT NULL,
  args        TEXT,
  status      exec_status NOT NULL DEFAULT 'PENDING',
  exit_code   INT,
  output      TEXT,
  duration_ms INT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE file_operations (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id  UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
  user_id    UUID NOT NULL REFERENCES users(id),
  operation  TEXT NOT NULL,
  path       TEXT NOT NULL,
  size_bytes BIGINT,
  sha256     TEXT,
  result     TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
  id         BIGSERIAL PRIMARY KEY,
  actor_id   UUID REFERENCES users(id),
  actor_ip   INET,
  device_id  UUID REFERENCES devices(id) ON DELETE SET NULL,
  action     TEXT NOT NULL,
  result     TEXT NOT NULL,
  detail     JSONB,
  prev_hash  TEXT,
  hash       TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX devices_status_idx ON devices (status);
CREATE INDEX exec_device_idx   
