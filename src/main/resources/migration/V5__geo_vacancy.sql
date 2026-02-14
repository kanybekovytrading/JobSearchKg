ALTER TABLE vacancies
    ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE resumes
    ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION;

-- Add index for better query performance
CREATE INDEX idx_vacancies_location ON vacancies(latitude, longitude);
CREATE INDEX idx_resumes_location ON resumes(latitude, longitude);