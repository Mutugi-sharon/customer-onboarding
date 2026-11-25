INSERT INTO staff (
    staff_id,
    full_name,
    password_hash,
    role,
    is_active
)
VALUES
(
    'test-reviewer',
    'Test Reviewer',
    crypt('StaffPass123!', gen_salt('bf')),
    'REVIEWER',
    TRUE
),
(
    'inactive-reviewer',
    'Inactive Reviewer',
    crypt('StaffPass123!', gen_salt('bf')),
    'REVIEWER',
    FALSE
)
ON CONFLICT (staff_id)
DO UPDATE SET
    full_name = EXCLUDED.full_name,
    password_hash = EXCLUDED.password_hash,
    role = EXCLUDED.role,
    is_active = EXCLUDED.is_active;
