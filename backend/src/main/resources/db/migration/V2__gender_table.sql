CREATE TABLE genders (
                         id         SMALLINT     PRIMARY KEY,
                         code       VARCHAR(20)  NOT NULL UNIQUE,
                         name       VARCHAR(50)  NOT NULL UNIQUE,
                         is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO genders (id, code, name) VALUES
                                         (1, 'MALE',   'Male'),
                                         (2, 'FEMALE', 'Female'),
                                         (3, 'OTHER',  'Other'),
                                         (4, 'PNS',    'Prefer not to say');