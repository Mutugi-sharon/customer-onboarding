ALTER TABLE applicants ALTER COLUMN email TYPE VARCHAR(255);

ALTER TABLE applicants
    ADD COLUMN kra_link              VARCHAR(255),
    ADD COLUMN photo_link            VARCHAR(255),
    ADD COLUMN address_proof_link    VARCHAR(255),
    ADD COLUMN income_link           VARCHAR(255),
    ADD COLUMN employment_letter_link VARCHAR(255);