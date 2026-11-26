
package com.example.onboarding.security;

import com.example.onboarding.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicantSchemaMigrationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void migrations_createEveryColumnTheCodeUses() {

        assertAll(
                "Applicant database schema",

                () -> assertTrue(
                        columnExists("kyc_status"),
                        "Missing applicants.kyc_status column"
                ),

                () -> assertTrue(
                        columnExists("account_number"),
                        "Missing applicants.account_number column"
                ),

                () -> assertTrue(
                        columnExists("application_rejection_reason"),
                        "Missing applicants.application_rejection_reason column"
                ),

                () -> assertTrue(
                        sequenceExists(),
                        "Missing applicant_account_number_seq sequence"
                )
        );
    }

    private boolean columnExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = current_schema()
                  AND table_name = 'applicants'
                  AND column_name = ?
                """,
                Integer.class,
                columnName
        );

        return count != null && count > 0;
    }

    private boolean sequenceExists() {
        Boolean exists = jdbcTemplate.queryForObject(
                """
                SELECT to_regclass('applicant_account_number_seq') IS NOT NULL
                """,
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }
}
