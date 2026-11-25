
package com.example.onboarding.applicant;

import com.example.onboarding.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
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

    @Test
    @Sql("/sql/staff-test-data.sql")
    void review_withInvalidStatus_returnsBadRequest() throws Exception {
        String token = reviewerToken();

        mvc.perform(patch("/api/applicants/{id}/review", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "INVALID",
                                    "reason": "Test reason"
                                }
                                """))
                .andExpect(status().isBadRequest());
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

    @Disabled("Fails until P2-05: missing columns")
    @Test
    void listApplicants_returnsApplicants() throws Exception {
        mvc.perform(get("/api/applicants/list"))
                .andExpect(status().isOk());
    }

    @Disabled("Fails until P2-05: missing columns")
    @Test
    void getApplicantById_withExistingApplicant_returnsApplicantDetails()
            throws Exception {

        mvc.perform(get("/api/applicants/{id}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Disabled("Fails until P2-05: missing columns")
    @Test
    void review_withApprovedStatus_updatesApplicant() throws Exception {

        mvc.perform(patch("/api/applicants/{id}/review", UUID.randomUUID())
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
