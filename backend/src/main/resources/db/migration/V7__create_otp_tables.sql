
-- Stores email OTP challenges
CREATE TABLE otp_challenges (
                                id BIGSERIAL PRIMARY KEY,
                                email VARCHAR(255) NOT NULL,
                                otp_hash VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMPTZ NOT NULL,
                                attempts INTEGER NOT NULL DEFAULT 0,
                                max_attempts INTEGER NOT NULL DEFAULT 5,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                last_sent_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                verified_at TIMESTAMPTZ,
                                consumed_at TIMESTAMPTZ
);

-- Makes OTP lookups by email faster
CREATE INDEX idx_otp_challenges_email
    ON otp_challenges (email);

-- Stores short-lived registration verification tokens
CREATE TABLE otp_verification_tokens (
                                         id BIGSERIAL PRIMARY KEY,
                                         email VARCHAR(255) NOT NULL,
                                         token_hash VARCHAR(255) NOT NULL UNIQUE,
                                         expires_at TIMESTAMPTZ NOT NULL,
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         consumed_at TIMESTAMPTZ
);

-- Makes verification token lookups by email faster
CREATE INDEX idx_otp_verification_email
    ON otp_verification_tokens (email);
