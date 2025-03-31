INSERT INTO account (id, email, password, first_name, last_name, dob, active)
SELECT UUID(), 'admin@gmail.com'
     , '$2a$10$oYWr60Ovjadol44Ir75quef9SNBv8csglcUK4md036G/FcO/j1DNW'
     , 'Admin', 'User', '1990-01-01', TRUE
    WHERE NOT EXISTS (
    SELECT 1 FROM account WHERE email = 'admin@gmail.com'
)