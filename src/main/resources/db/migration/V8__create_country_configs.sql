CREATE TABLE country_configs (
    country_code              CHAR(3) PRIMARY KEY,
    country_name              VARCHAR(100) NOT NULL,
    currency                  VARCHAR(5) NOT NULL,
    timezone                  VARCHAR(50) NOT NULL,
    academic_year_start_month INT NOT NULL DEFAULT 9,
    grading_max               INT NOT NULL DEFAULT 20,
    supports_wave             BOOLEAN NOT NULL DEFAULT FALSE,
    supports_orange_money     BOOLEAN NOT NULL DEFAULT FALSE,
    supports_mtn_momo         BOOLEAN NOT NULL DEFAULT FALSE,
    supports_airtel_money     BOOLEAN NOT NULL DEFAULT FALSE,
    supports_mpesa            BOOLEAN NOT NULL DEFAULT FALSE,
    phone_prefix              VARCHAR(5) NOT NULL,
    active                    BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO country_configs VALUES
('SEN', 'Sénégal',        'XOF', 'Africa/Dakar',    10, 20, TRUE,  TRUE,  FALSE, FALSE, FALSE, '+221', TRUE),
('CIV', 'Côte d''Ivoire', 'XOF', 'Africa/Abidjan',  9,  20, TRUE,  TRUE,  TRUE,  FALSE, FALSE, '+225', TRUE),
('MLI', 'Mali',           'XOF', 'Africa/Bamako',   10, 20, FALSE, TRUE,  FALSE, FALSE, FALSE, '+223', TRUE),
('GIN', 'Guinée',         'GNF', 'Africa/Conakry',  10, 20, FALSE, TRUE,  FALSE, FALSE, FALSE, '+224', TRUE),
('CPV', 'Cabo Verde',     'CVE', 'Atlantic/Cape_Verde', 9, 20, FALSE, FALSE, FALSE, FALSE, FALSE, '+238', TRUE),
('NGA', 'Nigeria',        'NGN', 'Africa/Lagos',    9,  100, FALSE, FALSE, TRUE,  FALSE, FALSE, '+234', TRUE),
('CMR', 'Cameroun',       'XAF', 'Africa/Douala',   9,  20, FALSE, TRUE,  TRUE,  FALSE, FALSE, '+237', TRUE),
('GHA', 'Ghana',          'GHS', 'Africa/Accra',    9,  100, FALSE, FALSE, FALSE, TRUE,  FALSE, '+233', TRUE),
('KEN', 'Kenya',          'KES', 'Africa/Nairobi',  1,  100, FALSE, FALSE, FALSE, FALSE, TRUE,  '+254', TRUE),
('MAR', 'Maroc',          'MAD', 'Africa/Casablanca', 9, 20, FALSE, FALSE, FALSE, FALSE, FALSE, '+212', TRUE);

ALTER TABLE schools ADD COLUMN IF NOT EXISTS currency VARCHAR(5) DEFAULT 'XOF';
ALTER TABLE schools ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'Africa/Dakar';
ALTER TABLE schools ADD COLUMN IF NOT EXISTS moov_api_key TEXT;
ALTER TABLE schools ADD COLUMN IF NOT EXISTS airtel_api_key TEXT;
ALTER TABLE schools ADD COLUMN IF NOT EXISTS mpesa_api_key TEXT;