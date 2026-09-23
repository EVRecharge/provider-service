CREATE TABLE IF NOT EXISTS providers (
    provider_id SERIAL PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    contact_person_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    rate_per_hour DOUBLE PRECISION NOT NULL,
    charger_type VARCHAR(50),
    max_concurrent_charges INTEGER,
    oauth_provider VARCHAR(50),
    oauth_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_providers_email ON providers(email);
CREATE INDEX IF NOT EXISTS idx_providers_city ON providers(city);
CREATE INDEX IF NOT EXISTS idx_providers_location ON providers(latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_providers_active ON providers(is_active);
