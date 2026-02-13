CREATE TABLE free_access_tracking (
                                      id BIGSERIAL PRIMARY KEY,
                                      telegram_id BIGINT NOT NULL,
                                      search_key VARCHAR(100) NOT NULL,
                                      entity_id BIGINT NOT NULL,
                                      access_date DATE NOT NULL,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для быстрого поиска по пользователю, ключу поиска и дате
CREATE INDEX idx_telegram_search_date
    ON free_access_tracking(telegram_id, search_key, access_date);

-- Индекс для очистки старых записей
CREATE INDEX idx_access_date
    ON free_access_tracking(access_date);

-- Комментарии
COMMENT ON TABLE free_access_tracking IS 'Отслеживание бесплатного доступа к 3 резюме/вакансиям в день';
COMMENT ON COLUMN free_access_tracking.search_key IS 'Ключ комбинации фильтров: RESUME_C1_S2_CAT3_SUB4';
COMMENT ON COLUMN free_access_tracking.entity_id IS 'ID резюме или вакансии с открытым доступом';
COMMENT ON COLUMN free_access_tracking.access_date IS 'Дата, на которую предоставлен доступ';