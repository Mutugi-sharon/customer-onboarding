INSERT INTO applicants (
    full_name,
    date_of_birth,
    nationality,
    gender,
    email,
    phone_number,
    identification_type,
    identification_number,
    onboarding_step,
    application_status
)
VALUES (
           'Test Applicant',
           '2000-01-15',
           'Kenyan',
           'Female',
           'test.applicant@example.com',
           '+254700000001',
           'NATIONAL_ID',
           '123456789',
           'COMPLETED',
           'PENDING_REVIEW'
       )
    ON CONFLICT DO NOTHING;