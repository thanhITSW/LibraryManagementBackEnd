INSERT INTO account_roles (account_id, roles_name)
SELECT a.id, r.name
FROM account a, role r
WHERE a.email = 'admin@gmail.com' AND r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM account_roles ar WHERE ar.account_id = a.id AND ar.roles_name = r.name
)