
package com.example.onboarding.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApplicantService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<GenderDto> getGender() {
        try {
            return jdbcTemplate.query(
                    "SELECT id, name FROM genders WHERE is_active = TRUE",
                    new BeanPropertyRowMapper<>(GenderDto.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve genders", e);
        }
    }

    public List<NationalityDto> getNationalities() {
        try {
            return jdbcTemplate.query(
                    "SELECT id, code, code_2 AS code2, name FROM nationalities WHERE is_active = TRUE ORDER BY name",
                    new BeanPropertyRowMapper<>(NationalityDto.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve nationalities", e);
        }
    }

    public int savePersonalDetails(PersonalDetailsDto applicant) {

        String sql = """
                INSERT INTO applicants (
                    full_name,
                    date_of_birth,
                    nationality,
                    gender,
                    email,
                    phone_number,
                    birth_certificate_link,
                    national_id_link,
                    identification_type,
                    identification_number
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                applicant.getFullName(),
                applicant.getDateOfBirth(),
                applicant.getNationality(),
                applicant.getGender(),
                applicant.getEmail(),
                applicant.getPhoneNumber(),
                applicant.getBirthCertificateLink(),
                applicant.getNationalIdLink(),
                applicant.getIdentificationType(),
                applicant.getIdentificationNumber()
        );
    }

    public int saveContactDetails(ContactDetailsDto contact) {

        String sql = "UPDATE applicants " +
                "SET email = ?, " +
                "phone_number = ?, " +
                "county = ?, " +
                "town = ?, " +
                "residential_address = ?, " +
                "postal_address = ? " +
                "WHERE id = ?";

        return jdbcTemplate.update(
                sql,
                contact.getEmail(),
                contact.getPhoneNumber(),
                contact.getCounty(),
                contact.getTown(),
                contact.getResidentialAddress(),
                contact.getPostalAddress(),
                contact.getApplicantId()
        );
    }

    public Map<String, Object> getApplicantById(UUID applicantId) {

        String sql = """
                SELECT
                    id,
                    full_name,
                    date_of_birth,
                    nationality,
                    gender,
                    email,
                    phone_number,
                    birth_certificate_link,
                    national_id_link,
                    password_hash,
                    onboarding_step,
                    application_status,
                    identification_type,
                    identification_number,
                    created_at,
                    county,
                    town,
                    residential_address,
                    postal_address
                FROM applicants
                WHERE id = ?
                """;

        List<Map<String, Object>> results =
                jdbcTemplate.queryForList(sql, applicantId);

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }
}
