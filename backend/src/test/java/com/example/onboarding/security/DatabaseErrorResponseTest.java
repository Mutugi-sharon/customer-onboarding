
package com.example.onboarding.security;

import com.example.onboarding.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

class DatabaseErrorResponseTest extends IntegrationTest {

    @Disabled("Must-fix: enabled by P2-10")
    @Test
    @Sql("/sql/staff-test-data.sql")
    void databaseError_returnsGeneric500WithoutSql() throws Exception {

        // Log in as a reviewer.
        MvcResult loginResult = mvc.perform(
                        post("/api/auth/staff-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "staffId": "test-reviewer",
                                            "password": "StaffPass123!"
                                        }
                                        """))
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper
                .builder()
                .build()
                .readTree(loginBody)
                .get("token")
                .asText();

        // Request the applicant list, which currently triggers a database error.
        MvcResult result = mvc.perform(
                        get("/api/applicants/list")
                                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String lowerCaseBody = responseBody.toLowerCase();

        assertEquals(
                500,
                result.getResponse().getStatus(),
                "Database errors should return HTTP 500"
        );

        assertFalse(
                lowerCaseBody.contains("select"),
                "Response must not expose SQL SELECT statements"
        );

        assertFalse(
                lowerCaseBody.contains("column"),
                "Response must not expose database column details"
        );
    }
}
