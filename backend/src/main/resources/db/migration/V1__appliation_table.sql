CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE applicants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(80) NOT NULL,
    gender VARCHAR(30),
    email VARCHAR(50) ,
    phone_number VARCHAR(15) ,
    birth_certificate_link VARCHAR(100) ,
    national_id_link VARCHAR(100) ,
    password_hash VARCHAR(100) ,
    onboarding_step VARCHAR(50) ,
    application_status VARCHAR(50) ,
    identification_type VARCHAR(30) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_applicant_identification
        UNIQUE (identification_type, identification_number)
);
