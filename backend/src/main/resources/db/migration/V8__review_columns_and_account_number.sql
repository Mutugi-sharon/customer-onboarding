ALTER TABLE applicants
    ADD COLUMN IF NOT EXISTS kyc_status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS account_number VARCHAR(20) UNIQUE,
    ADD COLUMN IF NOT EXISTS application_rejection_reason TEXT;

CREATE SEQUENCE IF NOT EXISTS applicant_account_number_seq
    START 1000000001;
