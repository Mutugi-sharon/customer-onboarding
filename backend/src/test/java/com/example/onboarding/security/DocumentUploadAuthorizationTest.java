
package com.example.onboarding.security;

import com.example.onboarding.IntegrationTest;
import com.example.onboarding.applicant.ApplicantService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentUploadAuthorizationTest extends IntegrationTest {

    @MockitoBean
    private ApplicantService applicantService;

    @Disabled("Must-fix: enabled by P2-09")
    @Test
    void uploadDocuments_withoutLogin_isRefused() throws Exception {

        UUID applicantId = UUID.randomUUID();

        when(applicantService.getApplicantById(applicantId))
                .thenReturn(Map.of("id", applicantId));

        mvc.perform(multipart(
                        "/api/applicants/{id}/documents",
                        applicantId
                ))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
