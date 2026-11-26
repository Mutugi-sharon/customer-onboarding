
package com.example.onboarding.security;

import com.example.onboarding.IntegrationTest;
import com.example.onboarding.applicant.ApplicantService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantAuthorizationTest extends IntegrationTest {

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ApplicantService applicantService;

    @Disabled("Must-fix: enabled by P2-07")
    @Test
    void applicantToken_cannotListApplicants() throws Exception {

        when(applicantService.listApplicants())
                .thenReturn(List.of());

        String applicantToken = createApplicantToken();

        mvc.perform(get("/api/applicants/list")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isForbidden());
    }

    @Disabled("Must-fix: enabled by P2-07")
    @Test
    void applicantToken_cannotApproveOwnApplication() throws Exception {

        when(applicantService.reviewApplicant(
                any(UUID.class),
                eq("APPROVED"),
                eq("")
        )).thenReturn(1);

        String applicantToken = createApplicantToken();

        mvc.perform(patch("/api/applicants/{id}/review", UUID.randomUUID())
                        .header("Authorization", "Bearer " + applicantToken)
                        .contentType("application/json")
                        .content("""
                                {
                                    "status": "APPROVED",
                                    "reason": ""
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private String createApplicantToken() {

        return jwtService.generateToken(
                "test-applicant",
                Map.of("role", "APPLICANT")
        );
    }
}
