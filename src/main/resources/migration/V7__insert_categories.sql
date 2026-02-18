-- ============================================
-- V2: ДОБАВЛЕНИЕ "ДРУГОЕ" В СУЩЕСТВУЮЩИЕ КАТЕГОРИИ
--     + НОВЫЕ СФЕРЫ: БЬЮТИ, ДОСТАВКА, ТОРГОВЛЯ, ОХРАНА
-- Используем подзапросы — без hardcoded ID!
-- ============================================


-- ============================================
-- 1. "ДРУГОЕ" ПОДКАТЕГОРИЯ В СУЩЕСТВУЮЩИЕ КАТЕГОРИИ
-- ============================================

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Разработка';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Кибербезопасность';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Дизайн UX/UI';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'DATA';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Стратегия, аналитика и управление';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Работа с клиентами';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Общестроительные работы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Отделочные работы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Монтажники';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Стратегия и аналитика';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Контент и копирайтинг';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Цифровая реклама и трафик';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'SMM';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Видео, дизайн и креатив';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'PR-специалисты';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Подготовительное производство';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Швейный цех';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Отделочный и завершающий цех';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Дизайн и менеджмент';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Транспортная логистика';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Складская логистика';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Закупка и распределение';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Аналитика и оптимизация';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Управление и стратегия';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Кухня';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Зал и обслуживание гостей';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Бар и напитки';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Управление и администрация';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Вспомогательные сотрудники';


-- ============================================
-- 2. "ДРУГОЕ" КАТЕГОРИЯ В СУЩЕСТВУЮЩИЕ СФЕРЫ
-- ============================================

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'IT-сфера';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Продажи';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Строительство';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Маркетинг';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Швейная отрасль';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Логистика';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Общепит';

-- "Другое" подкатегория для новых категорий "Другое" существующих сфер
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', c.id, true, NOW()
FROM categories c
         JOIN spheres s ON c.sphere_id = s.id
WHERE c.name_ru = 'Другое'
  AND s.name_ru IN ('IT-сфера', 'Продажи', 'Строительство', 'Маркетинг', 'Швейная отрасль', 'Логистика', 'Общепит');


-- ============================================
-- 3. НОВАЯ СФЕРА: БЬЮТИ И ЗДОРОВЬЕ
-- ============================================

INSERT INTO spheres (name_ru, name_en, name_ky, is_active, created_at)
VALUES ('Бьюти и здоровье', 'Beauty and Health', 'Сулуулук жана саламаттык', true, NOW());

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Волосы', 'Hair', 'Чач', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Ногти', 'Nails', 'Тырмак', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Косметология и уход', 'Cosmetology and Care', 'Косметология жана күтүм', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Массаж и SPA', 'Massage and SPA', 'Массаж жана SPA', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Фитнес и спорт', 'Fitness and Sports', 'Фитнес жана спорт', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Перманентный макияж и татуаж', 'Permanent Makeup and Tattoo', 'Туруктуу макияж жана татуаж', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Бьюти и здоровье';

-- Волосы
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Парикмахер', 'Hairdresser', 'Чач тарачы', id, true, NOW() FROM categories WHERE name_ru = 'Волосы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Колорист', 'Colorist', 'Колорист', id, true, NOW() FROM categories WHERE name_ru = 'Волосы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Стилист', 'Stylist', 'Стилист', id, true, NOW() FROM categories WHERE name_ru = 'Волосы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Барбер', 'Barber', 'Барбер', id, true, NOW() FROM categories WHERE name_ru = 'Волосы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Волосы';

-- Ногти
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Мастер маникюра', 'Manicurist', 'Маникюр мастери', id, true, NOW() FROM categories WHERE name_ru = 'Ногти';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Мастер педикюра', 'Pedicurist', 'Педикюр мастери', id, true, NOW() FROM categories WHERE name_ru = 'Ногти';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Nail-дизайнер', 'Nail Designer', 'Nail-дизайнер', id, true, NOW() FROM categories WHERE name_ru = 'Ногти';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Ногти';

-- Косметология и уход
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Косметолог', 'Cosmetologist', 'Косметолог', id, true, NOW() FROM categories WHERE name_ru = 'Косметология и уход';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Визажист', 'Makeup Artist', 'Визажист', id, true, NOW() FROM categories WHERE name_ru = 'Косметология и уход';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Эстетист', 'Esthetician', 'Эстетист', id, true, NOW() FROM categories WHERE name_ru = 'Косметология и уход';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Косметология и уход';

-- Массаж и SPA
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Массажист', 'Massage Therapist', 'Массажчы', id, true, NOW() FROM categories WHERE name_ru = 'Массаж и SPA';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'SPA-специалист', 'SPA Specialist', 'SPA-адис', id, true, NOW() FROM categories WHERE name_ru = 'Массаж и SPA';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Массаж и SPA';

-- Фитнес и спорт
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Фитнес-тренер', 'Fitness Trainer', 'Фитнес-тренер', id, true, NOW() FROM categories WHERE name_ru = 'Фитнес и спорт';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Персональный тренер', 'Personal Trainer', 'Жеке тренер', id, true, NOW() FROM categories WHERE name_ru = 'Фитнес и спорт';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Инструктор йоги', 'Yoga Instructor', 'Йога инструктору', id, true, NOW() FROM categories WHERE name_ru = 'Фитнес и спорт';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Фитнес и спорт';

-- Перманентный макияж и татуаж
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Мастер перманентного макияжа', 'Permanent Makeup Artist', 'Туруктуу макияж мастери', id, true, NOW() FROM categories WHERE name_ru = 'Перманентный макияж и татуаж';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Татуировщик', 'Tattoo Artist', 'Татуировкачы', id, true, NOW() FROM categories WHERE name_ru = 'Перманентный макияж и татуаж';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Мастер эпиляции', 'Epilation Specialist', 'Эпиляция мастери', id, true, NOW() FROM categories WHERE name_ru = 'Перманентный макияж и татуаж';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Перманентный макияж и татуаж';

-- Другое (Бьюти)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', c.id, true, NOW()
FROM categories c JOIN spheres s ON c.sphere_id = s.id
WHERE c.name_ru = 'Другое' AND s.name_ru = 'Бьюти и здоровье';


-- ============================================
-- 4. НОВАЯ СФЕРА: ДОСТАВКА И КУРЬЕРСТВО
-- ============================================

INSERT INTO spheres (name_ru, name_en, name_ky, is_active, created_at)
VALUES ('Доставка и курьерство', 'Delivery and Courier', 'Жеткирүү жана курьер кызматы', true, NOW());

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Курьеры', 'Couriers', 'Курьерлер', id, true, NOW() FROM spheres WHERE name_ru = 'Доставка и курьерство';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Водители доставки', 'Delivery Drivers', 'Жеткирүү айдоочулары', id, true, NOW() FROM spheres WHERE name_ru = 'Доставка и курьерство';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Операторы и диспетчеры', 'Operators and Dispatchers', 'Операторлор жана диспетчерлер', id, true, NOW() FROM spheres WHERE name_ru = 'Доставка и курьерство';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Управление доставкой', 'Delivery Management', 'Жеткирүүнү башкаруу', id, true, NOW() FROM spheres WHERE name_ru = 'Доставка и курьерство';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Доставка и курьерство';

-- Курьеры
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Пеший курьер', 'Foot Courier', 'Жөө курьер', id, true, NOW() FROM categories WHERE name_ru = 'Курьеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Велокурьер', 'Bike Courier', 'Велокурьер', id, true, NOW() FROM categories WHERE name_ru = 'Курьеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Курьер на мотоцикле', 'Motorcycle Courier', 'Мотоциклдеги курьер', id, true, NOW() FROM categories WHERE name_ru = 'Курьеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Курьер на авто', 'Car Courier', 'Автодогу курьер', id, true, NOW() FROM categories WHERE name_ru = 'Курьеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Курьеры';

-- Водители доставки
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Водитель-экспедитор', 'Driver-Expeditor', 'Айдоочу-экспедитор', id, true, NOW() FROM categories WHERE name_ru = 'Водители доставки';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Водитель грузовика', 'Truck Driver', 'Жүк унаа айдоочусу', id, true, NOW() FROM categories WHERE name_ru = 'Водители доставки';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Водитель такси', 'Taxi Driver', 'Такси айдоочусу', id, true, NOW() FROM categories WHERE name_ru = 'Водители доставки';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Водители доставки';

-- Операторы и диспетчеры
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Оператор колл-центра', 'Call Center Operator', 'Колл-центр операторы', id, true, NOW() FROM categories WHERE name_ru = 'Операторы и диспетчеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Диспетчер доставки', 'Delivery Dispatcher', 'Жеткирүү диспетчери', id, true, NOW() FROM categories WHERE name_ru = 'Операторы и диспетчеры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Операторы и диспетчеры';

-- Управление доставкой
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Менеджер доставки', 'Delivery Manager', 'Жеткирүү менеджери', id, true, NOW() FROM categories WHERE name_ru = 'Управление доставкой';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Руководитель курьерской службы', 'Courier Service Head', 'Курьер кызматынын жетекчиси', id, true, NOW() FROM categories WHERE name_ru = 'Управление доставкой';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Управление доставкой';

-- Другое (Доставка)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', c.id, true, NOW()
FROM categories c JOIN spheres s ON c.sphere_id = s.id
WHERE c.name_ru = 'Другое' AND s.name_ru = 'Доставка и курьерство';


-- ============================================
-- 5. НОВАЯ СФЕРА: ТОРГОВЛЯ И РИТЕЙЛ
-- ============================================

INSERT INTO spheres (name_ru, name_en, name_ky, is_active, created_at)
VALUES ('Торговля и ритейл', 'Trade and Retail', 'Соода жана ритейл', true, NOW());

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Продавцы', 'Sellers', 'Сатуучулар', id, true, NOW() FROM spheres WHERE name_ru = 'Торговля и ритейл';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Кассиры', 'Cashiers', 'Кассирлер', id, true, NOW() FROM spheres WHERE name_ru = 'Торговля и ритейл';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Мерчендайзинг', 'Merchandising', 'Мерчендайзинг', id, true, NOW() FROM spheres WHERE name_ru = 'Торговля и ритейл';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Управление магазином', 'Store Management', 'Дүкөн башкаруу', id, true, NOW() FROM spheres WHERE name_ru = 'Торговля и ритейл';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Торговля и ритейл';

-- Продавцы
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Продавец-консультант', 'Sales Consultant', 'Сатуучу-кеңешчи', id, true, NOW() FROM categories WHERE name_ru = 'Продавцы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Старший продавец', 'Senior Seller', 'Улук сатуучу', id, true, NOW() FROM categories WHERE name_ru = 'Продавцы';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Продавцы';

-- Кассиры
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Кассир', 'Cashier', 'Кассир', id, true, NOW() FROM categories WHERE name_ru = 'Кассиры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Старший кассир', 'Senior Cashier', 'Улук кассир', id, true, NOW() FROM categories WHERE name_ru = 'Кассиры';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Кассиры';

-- Мерчендайзинг
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Мерчендайзер', 'Merchandiser', 'Мерчендайзер', id, true, NOW() FROM categories WHERE name_ru = 'Мерчендайзинг';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Визуальный мерчендайзер', 'Visual Merchandiser', 'Визуалдык мерчендайзер', id, true, NOW() FROM categories WHERE name_ru = 'Мерчендайзинг';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Мерчендайзинг';

-- Управление магазином
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Управляющий магазином', 'Store Manager', 'Дүкөн башкаруучу', id, true, NOW() FROM categories WHERE name_ru = 'Управление магазином';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Администратор', 'Administrator', 'Администратор', id, true, NOW() FROM categories WHERE name_ru = 'Управление магазином';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Управление магазином';

-- Другое (Торговля)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', c.id, true, NOW()
FROM categories c JOIN spheres s ON c.sphere_id = s.id
WHERE c.name_ru = 'Другое' AND s.name_ru = 'Торговля и ритейл';


-- ============================================
-- 6. НОВАЯ СФЕРА: ОХРАНА И БЕЗОПАСНОСТЬ
-- ============================================

INSERT INTO spheres (name_ru, name_en, name_ky, is_active, created_at)
VALUES ('Охрана и безопасность', 'Security and Protection', 'Коргоо жана коопсуздук', true, NOW());

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Охранники', 'Security Guards', 'Коргоочулар', id, true, NOW() FROM spheres WHERE name_ru = 'Охрана и безопасность';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Управление безопасностью', 'Security Management', 'Коопсуздукту башкаруу', id, true, NOW() FROM spheres WHERE name_ru = 'Охрана и безопасность';

INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM spheres WHERE name_ru = 'Охрана и безопасность';

-- Охранники
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Охранник', 'Security Guard', 'Коргоочу', id, true, NOW() FROM categories WHERE name_ru = 'Охранники';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Контролёр', 'Controller', 'Контролёр', id, true, NOW() FROM categories WHERE name_ru = 'Охранники';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Телохранитель', 'Bodyguard', 'Телохранитель', id, true, NOW() FROM categories WHERE name_ru = 'Охранники';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Охранники';

-- Управление безопасностью
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Начальник охраны', 'Security Chief', 'Коргоо башчысы', id, true, NOW() FROM categories WHERE name_ru = 'Управление безопасностью';

INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', id, true, NOW() FROM categories WHERE name_ru = 'Управление безопасностью';

-- Другое (Охрана)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
SELECT 'Другое', 'Other', 'Башка', c.id, true, NOW()
FROM categories c JOIN spheres s ON c.sphere_id = s.id
WHERE c.name_ru = 'Другое' AND s.name_ru = 'Охрана и безопасность';


-- ============================================
-- ГОТОВО! ✅
-- ============================================