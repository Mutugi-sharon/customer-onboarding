CREATE TABLE staff (
                       id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       staff_id       VARCHAR(50)  NOT NULL UNIQUE,
                       full_name      VARCHAR(150) NOT NULL,
                       password_hash  VARCHAR(255) NOT NULL,
                       role           VARCHAR(30)  NOT NULL DEFAULT 'REVIEWER',
                       is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO staff (staff_id, full_name, password_hash, role)
VALUES (
           'reviewer',
           'Default Reviewer',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
           'REVIEWER'
       );