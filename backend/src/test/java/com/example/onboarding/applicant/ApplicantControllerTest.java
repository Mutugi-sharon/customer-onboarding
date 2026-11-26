
package com.example.onboarding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApplicantControllerTest extends IntegrationTest {

               @Autowired
               private JdbcTemplate jdbcTemplate;
    @Test
    @Sql("/sql/staff-test-data.sql")
    void listApplicants_returnsApplicants() throws Exception {
        String token = reviewerToken();

        mvc.perform(get("/api/applicants/list")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Sql("/sql/staff-test-data.sql")
    void review_withRejectedStatusAndNoReason_returnsBadRequest()
            throws Exception {

        String token = reviewerToken();

        mvc.perform(patch("/api/applicants/{id}/review", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "REJECTED",
                                    "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("A rejection reason is required."));
    }

    @Test

@Sql({"/sql/staff-test-data.sql", "/sql/applicant-test-data.sql"})
void getApplicantById_withExistingApplicant_returnsApplicantDetails()
        throws Exception {

    String token = reviewerToken();

    UUID applicantId = jdbcTemplate.queryForObject(
            "SELECT id FROM applicants WHERE email = ?",
            UUID.class,
            "test.applicant@example.com"
    );

    mvc.perform(get("/api/applicants/{id}", applicantId)
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
}

 @Test
@Sql({"/sql/staff-test-data.sql", "/sql/applicant-test-data.sql"})
void review_withApprovedStatus_updatesApplicant() throws Exception {

    String token = reviewerToken();

    UUID applicantId = jdbcTemplate.queryForObject(
            "SELECT id FROM applicants WHERE email = ?",
            UUID.class,
            "test.applicant@example.com"
    );

    mvc.perform(patch("/api/applicants/{id}/review", applicantId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "status": "APPROVED",
                                "reason": ""
                            }
                            """))
            .andExpect(status().isOk());
}

    private String reviewerToken() throws Exception {

        MvcResult result = mvc.perform(post("/api/auth/staff-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "staffId": "test-reviewer",
                                    "password": "StaffPass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Matcher matcher = Pattern.compile(
                "\"token\"\\s*:\\s*\"([^\"]+)\""
        ).matcher(result.getResponse().getContentAsString());

        assertTrue(matcher.find(), "Staff login should return a JWT token");

        return matcher.group(1);
    }
}
