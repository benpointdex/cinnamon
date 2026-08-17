-- Enable pgvector extension for embedding similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. Tenants table: Multi-tenancy, auth, and request quotas
CREATE TABLE tenants (
    tenant_id                      VARCHAR(64) PRIMARY KEY,
    name                           VARCHAR(255) NOT NULL,
    email                          VARCHAR(255) NOT NULL,
    api_key_hash                   VARCHAR(64) NOT NULL UNIQUE,
    email_verified                 BOOLEAN NOT NULL DEFAULT false,
    verification_code              VARCHAR(6),
    verification_code_expires_at   TIMESTAMP,
    daily_request_limit            INTEGER NOT NULL DEFAULT 50,
    created_at                     TIMESTAMP NOT NULL DEFAULT now()
);

-- 2. Code units table: Fingerprints, embeddings, and file pointers (no source code stored)
CREATE TABLE code_units (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    repository    VARCHAR(255) NOT NULL,
    file_path     VARCHAR(1024) NOT NULL,
    function_name VARCHAR(255) NOT NULL,
    content_hash  VARCHAR(64) NOT NULL,
    embedding     vector(384),
    author_tool   VARCHAR(64),
    line_count    INTEGER,
    last_modified TIMESTAMP,
    CONSTRAINT uq_code_units_instance UNIQUE (tenant_id, repository, file_path, function_name)
);

-- 3. Duplicate findings table: Confirmed near-duplicates and LLM judgment reasons
CREATE TABLE duplicate_findings (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    repository            VARCHAR(255) NOT NULL,
    new_file_path         VARCHAR(1024),
    new_function_name     VARCHAR(255),
    matched_file_path     VARCHAR(1024),
    matched_function_name VARCHAR(255),
    similarity_score      DOUBLE PRECISION,
    confirmed_duplicate   BOOLEAN,
    judgment_reasoning    TEXT,
    flagged_at            TIMESTAMP NOT NULL DEFAULT now(),
    commit_sha            VARCHAR(64)
);

-- 4. Ingestion jobs table: Tracks background file indexing status
CREATE TABLE ingestion_jobs (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   VARCHAR(64) NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    repository                  VARCHAR(255) NOT NULL,
    status                      VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    total_files                 INTEGER,
    processed_files             INTEGER NOT NULL DEFAULT 0,
    functions_indexed           INTEGER NOT NULL DEFAULT 0,
    functions_skipped_unchanged INTEGER NOT NULL DEFAULT 0,
    started_at                  TIMESTAMP,
    completed_at                TIMESTAMP
);

-- Indexes for vector similarity search and fast tenant/repository lookups
CREATE INDEX idx_code_units_embedding ON code_units USING hnsw (embedding vector_cosine_ops);
CREATE INDEX idx_code_units_lookup ON code_units (tenant_id, repository, content_hash);
CREATE INDEX idx_duplicate_findings_repo ON duplicate_findings (tenant_id, repository);
CREATE INDEX idx_ingestion_jobs_status ON ingestion_jobs (tenant_id, status);
