CREATE TABLE nationalities (
                               id         SMALLINT     PRIMARY KEY,
                               code       CHAR(3)      NOT NULL UNIQUE,
                               code_2     CHAR(2)      NOT NULL UNIQUE,
                               name       VARCHAR(100) NOT NULL UNIQUE,
                               is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
                               created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO nationalities (id, code, code_2, name) VALUES
                                                       (1,  'KEN', 'KE', 'Kenyan'),
                                                       (2,  'UGA', 'UG', 'Ugandan'),
                                                       (3,  'TZA', 'TZ', 'Tanzanian'),
                                                       (4,  'RWA', 'RW', 'Rwandan'),
                                                       (5,  'SOM', 'SO', 'Somali'),
                                                       (6,  'ETH', 'ET', 'Ethiopian'),
                                                       (7,  'SSD', 'SS', 'South Sudanese'),
                                                       (8,  'USA', 'US', 'American'),
                                                       (9,  'GBR', 'GB', 'British'),
                                                       (10, 'IND', 'IN', 'Indian'),
                                                       (11, 'CHN', 'CN', 'Chinese'),
                                                       (12, 'OTH', 'XX', 'Other');
