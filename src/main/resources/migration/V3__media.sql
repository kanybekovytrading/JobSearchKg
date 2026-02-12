-- liquibase formatted sql

-- changeset author:create_resume_media_table
CREATE TABLE resume_media (
                              id BIGSERIAL PRIMARY KEY,
                              resume_id BIGINT NOT NULL,
                              media_type VARCHAR(20) NOT NULL,
                              file_url VARCHAR(500) NOT NULL,
                              file_name VARCHAR(255) NOT NULL,
                              file_size BIGINT,
                              display_order INTEGER,
                              uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_resume_media_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

CREATE INDEX idx_resume_media_resume_id ON resume_media(resume_id);
CREATE INDEX idx_resume_media_order ON resume_media(resume_id, display_order);

-- changeset author:create_vacancy_media_table
CREATE TABLE vacancy_media (
                               id BIGSERIAL PRIMARY KEY,
                               vacancy_id BIGINT NOT NULL,
                               media_type VARCHAR(20) NOT NULL,
                               file_url VARCHAR(500) NOT NULL,
                               file_name VARCHAR(255) NOT NULL,
                               file_size BIGINT,
                               display_order INTEGER,
                               uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_vacancy_media_vacancy FOREIGN KEY (vacancy_id) REFERENCES vacancies(id) ON DELETE CASCADE
);

CREATE INDEX idx_vacancy_media_vacancy_id ON vacancy_media(vacancy_id);
CREATE INDEX idx_vacancy_media_order ON vacancy_media(vacancy_id, display_order);

COMMENT ON TABLE resume_media IS 'Медиа файлы (фото/видео) для резюме';
COMMENT ON TABLE vacancy_media IS 'Медиа файлы (фото/видео) для вакансий';
COMMENT ON COLUMN resume_media.media_type IS 'Тип медиа: PHOTO или VIDEO';
COMMENT ON COLUMN vacancy_media.media_type IS 'Тип медиа: PHOTO или VIDEO';