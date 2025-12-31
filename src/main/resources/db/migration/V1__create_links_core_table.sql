CREATE TABLE links_core (
    code VARCHAR(255) PRIMARY KEY,
    target_url TEXT NOT NULL,
    expiry_ts TIMESTAMP,
    is_disabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_links_core_expiry ON links_core(expiry_ts);
CREATE INDEX idx_links_core_disabled ON links_core(is_disabled);
