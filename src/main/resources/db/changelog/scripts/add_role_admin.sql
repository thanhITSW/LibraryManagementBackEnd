INSERT INTO role (name, description)
SELECT 'ADMIN', 'Administrator role'
    WHERE NOT EXISTS (
    SELECT 1 FROM role WHERE name = 'ADMIN'
)