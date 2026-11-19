
package com.example.onboarding.applicant;

import com.example.onboarding.otp.OtpService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicantService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public ApplicantService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            OtpService otpService) {

        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public List<GenderDto> getGender() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genders WHERE is_active = TRUE",
                new BeanPropertyRowMapper<>(GenderDto.class)
        );
    }

    public List<NationalityDto> getNationalities() {
        return jdbcTemplate.query(
                """
                SELECT id, code, code_2 AS code2, name
                FROM nationalities
                WHERE is_active = TRUE
                ORDER BY name
                """,
                new BeanPropertyRowMapper<>(NationalityDto.class)
        );
    }

    public int savePersonalDetails(PersonalDetailsDto a) {
        String sql = """
                INSERT INTO applicants (
                    full_name, date_of_birth, nationality, gender,
                    email, phone_number, birth_certificate_link, national_id_link,
                    identification_type, identification_number
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                a.getFullName(),
                a.getDateOfBirth(),
                a.getNationality(),
                a.getGender(),
                a.getEmail(),
                a.getPhoneNumber(),
                a.getBirthCertificateLink(),
                a.getNationalIdLink(),
                a.getIdentificationType(),
                a.getIdentificationNumber()
        );
    }

    // CHECK DUPLICATE REGISTRATION DETAILS
    public Map<String, Boolean> checkDuplicates(
            String email,
            String phoneNumber,
            String identificationNumber) {

        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase();

        String normalizedPhone = phoneNumber == null
                ? ""
                : phoneNumber.trim();

        String normalizedId = identificationNumber == null
                ? ""
                : identificationNumber.trim();

        boolean emailExists = !normalizedEmail.isBlank()
                && exists(
                """
                SELECT COUNT(*)
                FROM applicants
                WHERE LOWER(TRIM(email)) = ?
                """,
                normalizedEmail
        );

        boolean phoneExists = !normalizedPhone.isBlank()
                && exists(
                """
                SELECT COUNT(*)
                FROM applicants
                WHERE TRIM(phone_number) = ?
                """,
                normalizedPhone
        );

        boolean identificationNumberExists = !normalizedId.isBlank()
                && exists(
                """
                SELECT COUNT(*)
                FROM applicants
                WHERE TRIM(identification_number) = ?
                """,
                normalizedId
        );

        return Map.of(
                "emailExists", emailExists,
                "phoneExists", phoneExists,
                "identificationNumberExists",
                identificationNumberExists
        );
    }

    private boolean exists(String sql, String value) {
        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                value
        );

        return count != null && count > 0;
    }

    @Transactional
    public UUID registerApplicant(RegisterRequest req) {

        String email = req.getEmail().trim().toLowerCase();
        String phoneNumber = req.getPhoneNumber().trim();
        String identificationNumber =
                req.getIdentificationNumber().trim();

        // CHECK DUPLICATES BEFORE CONSUMING OTP
        Map<String, Boolean> duplicates = checkDuplicates(
                email,
                phoneNumber,
                identificationNumber
        );

        if (duplicates.get("emailExists")) {
            throw new IllegalArgumentException(
                    "Email already exists."
            );
        }

        if (duplicates.get("phoneExists")) {
            throw new IllegalArgumentException(
                    "Phone number already exists."
            );
        }

        if (duplicates.get("identificationNumberExists")) {
            throw new IllegalArgumentException(
                    "National ID already exists."
            );
        }

        // VERIFY EMAIL OTP
        boolean verified = otpService.consumeVerificationToken(
                email,
                req.getVerificationToken()
        );

        if (!verified) {
            throw new IllegalArgumentException(
                    "Email verification is required."
            );
        }

        String hash = passwordEncoder.encode(req.getPassword());

        String type = (
                req.getIdentificationType() == null
                        || req.getIdentificationType().isBlank()
        ) ? "NATIONAL_ID" : req.getIdentificationType();

        String sql = """
                INSERT INTO applicants (
                    full_name, date_of_birth, nationality, gender,
                    email, phone_number, identification_type, identification_number,
                    county, town, residential_address, postal_address,
                    password_hash, onboarding_step, application_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        return jdbcTemplate.queryForObject(
                sql,
                UUID.class,
                req.getFullName(),
                req.getDateOfBirth(),
                req.getNationality(),
                req.getGender(),
                email,
                phoneNumber,
                type,
                identificationNumber,
                req.getCounty(),
                req.getTown(),
                req.getResidentialAddress(),
                req.getPostalAddress(),
                hash,
                "COMPLETED",
                "PENDING_REVIEW"
        );
    }

    public List<Map<String, Object>> listApplicants() {
        String sql = """
                SELECT id,
                       full_name AS "fullName",
                       email,
                       phone_number AS "phoneNumber",
                       county,
                       town,
                       application_status AS "applicationStatus",
                       kyc_status AS "kycStatus",
                       created_at AS "createdAt",
                       account_number AS "accountNumber"
                FROM applicants
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public int reviewApplicant(UUID id, String status, String reason) {

        String kycStatus;

        if ("APPROVED".equals(status)) {
            kycStatus = "APPROVED";
        } else if ("REJECTED".equals(status)) {
            kycStatus = "REJECTED";
        } else {
            kycStatus = "PENDING";
        }

        return jdbcTemplate.update(
                """
                UPDATE applicants
                SET application_status = ?,
                    kyc_status = ?,
                    application_rejection_reason = ?,
                    account_number = CASE
                        WHEN ? = 'APPROVED'
                        THEN COALESCE(
                            account_number,
                            nextval('applicant_account_number_seq')::text
                        )
                        ELSE account_number
                    END
                WHERE id = ?
                """,
                status,
                kycStatus,
                "REJECTED".equals(status) ? reason : null,
                status,
                id
        );
    }

    public int saveContactDetails(ContactDetailsDto c) {
        String sql = """
                UPDATE applicants SET
                    email = ?,
                    phone_number = ?,
                    county = ?,
                    town = ?,
                    residential_address = ?,
                    postal_address = ?
                WHERE id = ?
                """;

        return jdbcTemplate.update(
                sql,
                c.getEmail(),
                c.getPhoneNumber(),
                c.getCounty(),
                c.getTown(),
                c.getResidentialAddress(),
                c.getPostalAddress(),
                c.getApplicantId()
        );
    }

    public Map<String, Object> getApplicantById(UUID id) {
        String sql = """
                SELECT id,
                       full_name,
                       date_of_birth,
                       nationality,
                       gender,
                       email,
                       phone_number,
                       identification_type,
                       identification_number,
                       created_at,
                       county,
                       town,
                       residential_address,
                       postal_address,
                       application_status,
                       account_number,
                       kyc_status,
                       application_rejection_reason,
                       onboarding_step,
                       national_id_link,
                       kra_link,
                       photo_link,
                       address_proof_link,
                       income_link,
                       employment_letter_link
                FROM applicants
                WHERE id = ?
                """;

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(sql, id);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public void updateDocuments(
            UUID id,
            String nationalIdLink,
            String kraLink,
            String photoLink,
            String addressProofLink,
            String incomeLink,
            String employmentLetterLink) {

        jdbcTemplate.update(
                """
                UPDATE applicants SET
                    national_id_link = COALESCE(?, national_id_link),
                    kra_link = COALESCE(?, kra_link),
                    photo_link = COALESCE(?, photo_link),
                    address_proof_link = COALESCE(?, address_proof_link),
                    income_link = COALESCE(?, income_link),
                    employment_letter_link = COALESCE(?, employment_letter_link)
                WHERE id = ?
                """,
                nationalIdLink,
                kraLink,
                photoLink,
                addressProofLink,
                incomeLink,
                employmentLetterLink,
                id
        );
    }
}
