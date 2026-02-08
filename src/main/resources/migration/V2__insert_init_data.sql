-- ============================================
-- INSERT DATA FOR SPHERES, CATEGORIES, SUBCATEGORIES
-- ============================================

-- 1. ВСТАВКА СФЕР (SPHERES)
-- ============================================

INSERT INTO spheres (name_ru, name_en, name_ky, is_active, created_at)
VALUES ('IT-сфера', 'IT Sphere', 'IT чөйрөсү', true, NOW()),
       ('Продажи', 'Sales', 'Сатуу', true, NOW()),
       ('Строительство', 'Construction', 'Курулуш', true, NOW()),
       ('Маркетинг', 'Marketing', 'Маркетинг', true, NOW()),
       ('Швейная отрасль', 'Sewing Industry', 'Тигүү өнөр жайы', true, NOW()),
       ('Логистика', 'Logistics', 'Логистика', true, NOW()),
       ('Общепит', 'Food Service', 'Тамак-аш кызматы', true, NOW());

-- ============================================
-- 2. ВСТАВКА КАТЕГОРИЙ (CATEGORIES)
-- ============================================

-- IT-сфера (sphere_id = 1)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Разработка', 'Development', 'Иштеп чыгуу', 1, true, NOW()),
       ('Кибербезопасность', 'Cybersecurity', 'Киберкоопсуздук', 1, true, NOW()),
       ('Дизайн UX/UI', 'UX/UI Design', 'UX/UI Дизайн', 1, true, NOW()),
       ('DATA', 'DATA', 'DATA', 1, true, NOW());

-- Продажи (sphere_id = 2)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Стратегия, аналитика и управление', 'Strategy, Analytics and Management', 'Стратегия, аналитика жана башкаруу',
        2, true, NOW()),
       ('Работа с клиентами', 'Customer Service', 'Кардарлар менен иштөө', 2, true, NOW());

-- Строительство (sphere_id = 3)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Общестроительные работы', 'General Construction', 'Жалпы курулуш иштери', 3, true, NOW()),
       ('Отделочные работы', 'Finishing Works', 'Бүтүрүү иштери', 3, true, NOW()),
       ('Монтажники', 'Installation Workers', 'Монтаждоочулар', 3, true, NOW());

-- Маркетинг (sphere_id = 4)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Стратегия и аналитика', 'Strategy and Analytics', 'Стратегия жана аналитика', 4, true, NOW()),
       ('Контент и копирайтинг', 'Content and Copywriting', 'Мазмун жана копирайтинг', 4, true, NOW()),
       ('Цифровая реклама и трафик', 'Digital Advertising and Traffic', 'Санариптик жарнама жана трафик', 4, true,
        NOW()),
       ('SMM', 'SMM', 'SMM', 4, true, NOW()),
       ('Видео, дизайн и креатив', 'Video, Design and Creative', 'Видео, дизайн жана креатив', 4, true, NOW()),
       ('PR-специалисты', 'PR Specialists', 'PR адистер', 4, true, NOW());

-- Швейная отрасль (sphere_id = 5)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Подготовительное производство', 'Preparatory Production', 'Даярдоо өндүрүшү', 5, true, NOW()),
       ('Швейный цех', 'Sewing Workshop', 'Тигүү цехи', 5, true, NOW()),
       ('Отделочный и завершающий цех', 'Finishing Workshop', 'Бүтүрүү цехи', 5, true, NOW()),
       ('Дизайн и менеджмент', 'Design and Management', 'Дизайн жана башкаруу', 5, true, NOW());

-- Логистика (sphere_id = 6)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Транспортная логистика', 'Transportation Logistics', 'Транспорттук логистика', 6, true, NOW()),
       ('Складская логистика', 'Warehouse Logistics', 'Кампа логистикасы', 6, true, NOW()),
       ('Закупка и распределение', 'Procurement and Distribution', 'Сатып алуу жана бөлүштүрүү', 6, true, NOW()),
       ('Аналитика и оптимизация', 'Analytics and Optimization', 'Аналитика жана оптимизациялоо', 6, true, NOW()),
       ('Управление и стратегия', 'Management and Strategy', 'Башкаруу жана стратегия', 6, true, NOW());

-- Общепит (sphere_id = 7)
INSERT INTO categories (name_ru, name_en, name_ky, sphere_id, is_active, created_at)
VALUES ('Кухня', 'Kitchen', 'Ашкана', 7, true, NOW()),
       ('Зал и обслуживание гостей', 'Hall and Guest Service', 'Зал жана конокторду тейлөө', 7, true, NOW()),
       ('Бар и напитки', 'Bar and Beverages', 'Бар жана суусундуктар', 7, true, NOW()),
       ('Управление и администрация', 'Management and Administration', 'Башкаруу жана администрация', 7, true, NOW()),
       ('Вспомогательные сотрудники', 'Support Staff', 'Жардамчы кызматкерлер', 7, true, NOW());

-- ============================================
-- 3. ВСТАВКА ПОДКАТЕГОРИЙ (SUBCATEGORIES)
-- ============================================

-- IT-сфера → Разработка (category_id = 1)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Frontend', 'Frontend', 'Frontend', 1, true, NOW()),
       ('Backend', 'Backend', 'Backend', 1, true, NOW()),
       ('Fullstack', 'Fullstack', 'Fullstack', 1, true, NOW()),
       ('Разработчик игр', 'Game Developer', 'Оюн иштеп чыгуучу', 1, true, NOW()),
       ('Мобильный разработчик', 'Mobile Developer', 'Мобилдик иштеп чыгуучу', 1, true, NOW()),
       ('Разработчик встраиваемых систем', 'Embedded Systems Developer', 'Камтылган системалар иштеп чыгуучу', 1, true,
        NOW());

-- IT-сфера → Кибербезопасность (category_id = 2)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Пентестер', 'Pentester', 'Пентестер', 2, true, NOW()),
       ('Аналитик SOC', 'SOC Analyst', 'SOC Аналитик', 2, true, NOW()),
       ('Специалист IR', 'IR Specialist', 'IR Адис', 2, true, NOW()),
       ('Информационная безопасность', 'Information Security', 'Маалыматтык коопсуздук', 2, true, NOW());

-- IT-сфера → Дизайн UX/UI (category_id = 3)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('UI-Дизайнер', 'UI Designer', 'UI-Дизайнер', 3, true, NOW()),
       ('UX-Дизайнер', 'UX Designer', 'UX-Дизайнер', 3, true, NOW()),
       ('UX-исследователь', 'UX Researcher', 'UX-изилдөөчү', 3, true, NOW());

-- IT-сфера → DATA (category_id = 4)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Data scientist', 'Data Scientist', 'Data Scientist', 4, true, NOW()),
       ('Data Аналитик', 'Data Analyst', 'Data Аналитик', 4, true, NOW()),
       ('Data инженер', 'Data Engineer', 'Data Инженер', 4, true, NOW()),
       ('Инженер по машинному обучению', 'Machine Learning Engineer', 'Машиналык үйрөнүү инженери', 4, true, NOW());

-- Продажи → Стратегия, аналитика и управление (category_id = 5)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Руководитель продаж', 'Sales Manager', 'Сатуу жетекчиси', 5, true, NOW()),
       ('Операционный менеджер', 'Operations Manager', 'Операциялык менеджер', 5, true, NOW()),
       ('Аналитик продаж', 'Sales Analyst', 'Сатуу аналитиги', 5, true, NOW());

-- Продажи → Работа с клиентами (category_id = 6)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Менеджер продаж', 'Sales Manager', 'Сатуу менеджери', 6, true, NOW()),
       ('Старший менеджер продаж', 'Senior Sales Manager', 'Улук сатуу менеджери', 6, true, NOW()),
       ('Менеджер по работе с партнерами', 'Partner Manager', 'Өнөктөштөр менен иштөө менеджери', 6, true, NOW());

-- Строительство → Общестроительные работы (category_id = 7)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Каменщик', 'Mason', 'Таш калоочу', 7, true, NOW()),
       ('Монолитчик', 'Monolithic Worker', 'Монолитчик', 7, true, NOW()),
       ('Монтажник ЖБИ', 'Concrete Installer', 'ЖБИ монтаждоочу', 7, true, NOW());

-- Строительство → Отделочные работы (category_id = 8)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Штукатур', 'Plasterer', 'Сылагычы', 8, true, NOW()),
       ('Маляр', 'Painter', 'Боёкчу', 8, true, NOW()),
       ('Плиточник', 'Tiler', 'Плитка салуучу', 8, true, NOW()),
       ('Наливные полы', 'Self-leveling Floors', 'Куюлуучу полдор', 8, true, NOW());

-- Строительство → Монтажники (category_id = 9)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Электромонтажник', 'Electrician', 'Электромонтаждоочу', 9, true, NOW()),
       ('Сантехник', 'Plumber', 'Сантехник', 9, true, NOW()),
       ('Сварщик', 'Welder', 'Ширетүүчү', 9, true, NOW()),
       ('Кровельщик', 'Roofer', 'Чатыр жабуучу', 9, true, NOW()),
       ('Фасадчик', 'Facade Worker', 'Фасад иштөөчү', 9, true, NOW()),
       ('Монтаж окон и дверей', 'Window and Door Installer', 'Терезе жана эшик монтаждоочу', 9, true, NOW()),
       ('Гипсокартонщик', 'Drywall Installer', 'Гипсокартон иштөөчү', 9, true, NOW());

-- Маркетинг → Стратегия и аналитика (category_id = 10)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Маркетолог-аналитик', 'Marketing Analyst', 'Маркетолог-аналитик', 10, true, NOW()),
       ('CRM-аналитик', 'CRM Analyst', 'CRM-аналитик', 10, true, NOW()),
       ('Продуктовый маркетолог', 'Product Marketer', 'Продукт маркетологу', 10, true, NOW()),
       ('Маркетинг менеджер', 'Marketing Manager', 'Маркетинг менеджер', 10, true, NOW());

-- Маркетинг → Контент и копирайтинг (category_id = 11)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Копирайтер', 'Copywriter', 'Копирайтер', 11, true, NOW()),
       ('Контент менеджер', 'Content Manager', 'Мазмун менеджери', 11, true, NOW()),
       ('Контент маркетолог', 'Content Marketer', 'Мазмун маркетологу', 11, true, NOW()),
       ('SEO-копирайтер', 'SEO Copywriter', 'SEO-копирайтер', 11, true, NOW());

-- Маркетинг → Цифровая реклама и трафик (category_id = 12)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Таргетолог', 'Targeting Specialist', 'Таргетолог', 12, true, NOW()),
       ('Трафик менеджер', 'Traffic Manager', 'Трафик менеджер', 12, true, NOW()),
       ('Партнерский маркетинг', 'Affiliate Marketing', 'Өнөктөштүк маркетинг', 12, true, NOW());

-- Маркетинг → SMM (category_id = 13)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('SMM-специалист', 'SMM Specialist', 'SMM-адис', 13, true, NOW()),
       ('Комьюнити менеджер', 'Community Manager', 'Комьюнити менеджер', 13, true, NOW()),
       ('Стратег SMM', 'SMM Strategist', 'SMM стратег', 13, true, NOW());

-- Маркетинг → Видео, дизайн и креатив (category_id = 14)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Мобилограф', 'Mobile Videographer', 'Мобилограф', 14, true, NOW()),
       ('Видеограф', 'Videographer', 'Видеограф', 14, true, NOW()),
       ('Дизайнер маркетолог', 'Marketing Designer', 'Маркетолог дизайнер', 14, true, NOW()),
       ('Маркетолог креативщик', 'Creative Marketer', 'Креативдүү маркетолог', 14, true, NOW());

-- Маркетинг → PR-специалисты (category_id = 15)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Бренд менеджер', 'Brand Manager', 'Бренд менеджер', 15, true, NOW()),
       ('PR менеджер', 'PR Manager', 'PR менеджер', 15, true, NOW()),
       ('Event менеджер', 'Event Manager', 'Event менеджер', 15, true, NOW());

-- Швейная отрасль → Подготовительное производство (category_id = 16)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Технолог конструктор', 'Design Technologist', 'Технолог конструктор', 16, true, NOW()),
       ('Раскройщик', 'Cutter', 'Кескич', 16, true, NOW()),
       ('Нормировщик', 'Norms Specialist', 'Нормировщик', 16, true, NOW()),
       ('Специалист ОТК', 'QC Specialist', 'ОТК адиси', 16, true, NOW());

-- Швейная отрасль → Швейный цех (category_id = 17)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Швея', 'Seamstress', 'Тигүүчү', 17, true, NOW()),
       ('Бригадир швейного цеха', 'Sewing Shop Foreman', 'Тигүү цехинин бригадири', 17, true, NOW()),
       ('Портной', 'Tailor', 'Тигинчи', 17, true, NOW()),
       ('Лекальщик', 'Pattern Maker', 'Лекалчы', 17, true, NOW());

-- Швейная отрасль → Отделочный и завершающий цех (category_id = 18)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Утюжник', 'Presser', 'Үтүк басуучу', 18, true, NOW()),
       ('ОТК на выходе', 'Final QC', 'Чыгуудагы ОТК', 18, true, NOW()),
       ('Упаковщик', 'Packer', 'Кутучу', 18, true, NOW());

-- Швейная отрасль → Дизайн и менеджмент (category_id = 19)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Дизайнер одежды', 'Fashion Designer', 'Кийим дизайнери', 19, true, NOW()),
       ('Технолог производства', 'Production Technologist', 'Өндүрүш технологу', 19, true, NOW()),
       ('Менеджер по производству', 'Production Manager', 'Өндүрүш менеджери', 19, true, NOW()),
       ('Мерчендайзер', 'Merchandiser', 'Мерчендайзер', 19, true, NOW()),
       ('Специалист по закупкам', 'Procurement Specialist', 'Сатып алуу боюнча адис', 19, true, NOW());

-- Логистика → Транспортная логистика (category_id = 20)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Диспетчер грузоперевозок', 'Freight Dispatcher', 'Жүк ташуу диспетчери', 20, true, NOW()),
       ('Специалист ВЭД', 'Foreign Trade Specialist', 'ВЭД адиси', 20, true, NOW()),
       ('Экспедитор склада и офиса', 'Warehouse Expeditor', 'Кампа экспедитору', 20, true, NOW()),
       ('Специалист страховки', 'Insurance Specialist', 'Камсыздандыруу адиси', 20, true, NOW());

-- Логистика → Складская логистика (category_id = 21)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Менеджер склада', 'Warehouse Manager', 'Кампа менеджери', 21, true, NOW()),
       ('Специалист по запасам', 'Inventory Specialist', 'Запастар адиси', 21, true, NOW()),
       ('Логист-операционист', 'Operations Logistician', 'Логист-операционист', 21, true, NOW()),
       ('Кладовщик', 'Storekeeper', 'Кампачы', 21, true, NOW());

-- Логистика → Закупка и распределение (category_id = 22)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Менеджер по закупкам', 'Procurement Manager', 'Сатып алуу менеджери', 22, true, NOW()),
       ('Специалист по планированию цепочки поставок', 'Supply Chain Planner', 'Жеткирүү чынжырын пландоочу', 22, true,
        NOW()),
       ('Менеджер по распределению', 'Distribution Manager', 'Бөлүштүрүү менеджери', 22, true, NOW());

-- Логистика → Аналитика и оптимизация (category_id = 23)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Логистический аналитик', 'Logistics Analyst', 'Логистикалык аналитик', 23, true, NOW()),
       ('Специалист по логистическим системам', 'Logistics Systems Specialist', 'Логистикалык системалар адиси', 23,
        true, NOW()),
       ('Специалист по процессам', 'Process Specialist', 'Процесстер адиси', 23, true, NOW());

-- Логистика → Управление и стратегия (category_id = 24)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Руководитель логистического отдела', 'Logistics Department Head', 'Логистика бөлүмүнүн жетекчиси', 24, true,
        NOW()),
       ('Менеджер проекта в логистике', 'Logistics Project Manager', 'Логистикада долбоор менеджери', 24, true, NOW()),
       ('Консультант в логистике', 'Logistics Consultant', 'Логистикадагы консультант', 24, true, NOW());

-- Общепит → Кухня (category_id = 25)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Шеф-повар', 'Chef', 'Баш ашпоз', 25, true, NOW()),
       ('Су-шеф', 'Sous Chef', 'Су-шеф', 25, true, NOW()),
       ('Повар-универсал', 'Universal Cook', 'Универсал ашпоз', 25, true, NOW()),
       ('Горячий повар', 'Hot Cook', 'Ысык ашпоз', 25, true, NOW()),
       ('Холодный повар', 'Cold Cook', 'Муздак ашпоз', 25, true, NOW()),
       ('Кондитер', 'Pastry Chef', 'Кондитер', 25, true, NOW()),
       ('Повар на фритюре', 'Fryer Cook', 'Фритюрдогу ашпоз', 25, true, NOW()),
       ('Повар-стажер', 'Cook Trainee', 'Ашпоз-стажер', 25, true, NOW());

-- Общепит → Зал и обслуживание гостей (category_id = 26)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Управляющий залом', 'Floor Manager', 'Зал башкаруучу', 26, true, NOW()),
       ('Старший официант', 'Senior Waiter', 'Улук даяр', 26, true, NOW()),
       ('Официант', 'Waiter', 'Даяр', 26, true, NOW()),
       ('Хостес', 'Hostess', 'Хостес', 26, true, NOW());

-- Общепит → Бар и напитки (category_id = 27)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Бармен', 'Bartender', 'Бармен', 27, true, NOW()),
       ('Бариста', 'Barista', 'Бариста', 27, true, NOW()),
       ('Бар менеджер', 'Bar Manager', 'Бар менеджер', 27, true, NOW()),
       ('Бармен эксперт', 'Expert Bartender', 'Эксперт бармен', 27, true, NOW());

-- Общепит → Управление и администрация (category_id = 28)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Управляющий рестораном', 'Restaurant Manager', 'Ресторан башкаруучу', 28, true, NOW()),
       ('Менеджер по закупкам', 'Procurement Manager', 'Сатып алуу менеджери', 28, true, NOW()),
       ('Мерчендайзер общепита', 'Food Service Merchandiser', 'Тамак-аш мерчендайзери', 28, true, NOW());

-- Общепит → Вспомогательные сотрудники (category_id = 29)
INSERT INTO subcategories (name_ru, name_en, name_ky, category_id, is_active, created_at)
VALUES ('Мойщик посуды', 'Dishwasher', 'Идиш жуучу', 29, true, NOW()),
       ('Уборщик', 'Cleaner', 'Тазалоочу', 29, true, NOW()),
       ('Кассир', 'Cashier', 'Кассир', 29, true, NOW());

-- ============================================
-- ГОТОВО! ✅
-- ============================================