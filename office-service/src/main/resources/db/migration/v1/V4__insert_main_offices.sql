-- V3__fill_offices_and_addresses.sql

-- Вставляем адреса (пропускаем если уже есть)
INSERT INTO office_bank.addresses (id, address, zip_code, city, country_id, created_at, updated_at)
SELECT
    gen_random_uuid()::VARCHAR,
    address,
    zip_code,
    city,
    (SELECT id FROM office_bank.countries WHERE code = 'RUS'),
    NOW() AT TIME ZONE 'utc',
    NOW() AT TIME ZONE 'utc'
FROM (VALUES
          ('ул. Тверская, д. 1', '125009', 'Moscow'),
          ('Невский проспект, д. 28', '191186', 'Saint Petersburg'),
          ('ул. Баумана, д. 15', '420111', 'Kazan'),
          ('ул. Ленинградская, д. 45', '443010', 'Samara')
     ) AS v(address, zip_code, city)
WHERE NOT EXISTS (
    SELECT 1 FROM office_bank.addresses a
    WHERE a.address = v.address AND a.city = v.city
);

-- Вставляем офисы (пропускаем если уже есть)
INSERT INTO office_bank.offices (id, name, main, contact_phone, schedule_json, active, address_id, owner_id, created_at, updated_at)
SELECT
    gen_random_uuid()::VARCHAR,
    name,
    true,
    contact_phone,
    schedule_json::JSONB,
    true,
    (SELECT id FROM office_bank.addresses WHERE city = city_name AND address = address_name),
    'system_owner',
    NOW() AT TIME ZONE 'utc',
    NOW() AT TIME ZONE 'utc'
FROM (VALUES
          ('Центральный офис Москва', true, '+7 (495) 111-11-11', '{"monday": {"open": "09:00", "close": "21:00"}, "tuesday": {"open": "09:00", "close": "21:00"}, "wednesday": {"open": "09:00", "close": "21:00"}, "thursday": {"open": "09:00", "close": "21:00"}, "friday": {"open": "09:00", "close": "21:00"}, "saturday": {"open": "10:00", "close": "18:00"}, "sunday": {"open": "10:00", "close": "18:00"}}'::text, 'ул. Тверская, д. 1', 'Moscow'),
          ('Офис Санкт-Петербург', false,'+7 (812) 222-22-22', '{"monday": {"open": "09:00", "close": "20:00"}, "tuesday": {"open": "09:00", "close": "20:00"}, "wednesday": {"open": "09:00", "close": "20:00"}, "thursday": {"open": "09:00", "close": "20:00"}, "friday": {"open": "09:00", "close": "20:00"}, "saturday": {"open": "10:00", "close": "17:00"}, "sunday": {"open": "10:00", "close": "17:00"}}'::text, 'Невский проспект, д. 28', 'Saint Petersburg'),
          ('Офис Казань', false,'+7 (843) 333-33-33', '{"monday": {"open": "09:00", "close": "19:00"}, "tuesday": {"open": "09:00", "close": "19:00"}, "wednesday": {"open": "09:00", "close": "19:00"}, "thursday": {"open": "09:00", "close": "19:00"}, "friday": {"open": "09:00", "close": "19:00"}, "saturday": {"open": "10:00", "close": "16:00"}, "sunday": {"open": "10:00", "close": "16:00"}}'::text, 'ул. Баумана, д. 15', 'Kazan'),
          ('Офис Самара', false,'+7 (846) 444-44-44', '{"monday": {"open": "09:00", "close": "19:00"}, "tuesday": {"open": "09:00", "close": "19:00"}, "wednesday": {"open": "09:00", "close": "19:00"}, "thursday": {"open": "09:00", "close": "19:00"}, "friday": {"open": "09:00", "close": "19:00"}, "saturday": {"open": "10:00", "close": "16:00"}, "sunday": {"open": "10:00", "close": "16:00"}}'::text, 'ул. Ленинградская, д. 45', 'Samara')
     ) AS v(name, contact_phone, schedule_json, address_name, city_name)
WHERE NOT EXISTS (
    SELECT 1 FROM office_bank.offices o
    WHERE o.name = v.name
);