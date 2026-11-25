
package com.example.onboarding.auth;

import com.example.onboarding.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTest {

    @Test
    void login_withValidApplicantCredentials_returnsToken()
            throws Exception {

        String email = uniqueEmail();
        String phone = uniquePhone();
        String nationalId = uniqueNationalId();

        // Complete OTP verification and applicant registration first.
        String verificationToken = requestVerificationToken(email);

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email, phone, nationalId, verificationToken
                        )))
                .andExpect(status().isCreated());

        // Log in using the newly registered applicant.
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.subjectId").isNotEmpty())
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.name").value("Test Applicant"));
    }

    @Test
    void login_withInvalidCredentials_returnsUnauthorized()
            throws Exception {

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "unknown-%s@example.com",
                                    "password": "WrongPassword123!"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email or password"));
    }

    @Test
    @Sql("/sql/staff-test-data.sql")
    void staffLogin_withValidCredentials_returnsToken()
            throws Exception {

        mvc.perform(post("/api/auth/staff-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "staffId": "test-reviewer",
                                    "password": "StaffPass123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.subjectId").isNotEmpty())
                .andExpect(jsonPath("$.role").value("REVIEWER"))
                .andExpect(jsonPath("$.name").value("Test Reviewer"));
    }

    @Test
    @Sql("/sql/staff-test-data.sql")
    void staffLogin_withInactiveAccount_returnsUnauthorized()
            throws Exception {

        mvc.perform(post("/api/auth/staff-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "staffId": "inactive-reviewer",
                                    "password": "StaffPass123!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Staff login failed"));
    }

    private String requestVerificationToken(String email)
            throws Exception {

        mvc.perform(post("/api/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk());

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        Matcher otpMatcher = Pattern.compile(
                "(?m)^([0-9]{6})$"
        ).matcher(message.getText());

        assertTrue(otpMatcher.find(),
                "Email should contain a six-digit OTP");

        String otp = otpMatcher.group(1);

        MvcResult result = mvc.perform(post("/api/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "otp": "%s"
                                }
                                """.formatted(email, otp)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        Matcher tokenMatcher = Pattern.compile(
                "\"verificationToken\"\\s*:\\s*\"([^\"]+)\""
        ).matcher(responseBody);

        assertTrue(tokenMatcher.find(),
                "Response should contain a verification token");

        return tokenMatcher.group(1);
    }

    private String registrationJson(
            String email,
            String phone,
            String nationalId,
            String token) {

        return """
                {
                    "fullName": "Test Applicant",
                    "dateOfBirth": "2000-01-15",
                    "nationality": "Kenyan",
                    "gender": "Female",
                    "identificationType": "NATIONAL_ID",
                    "identificationNumber": "%s",
                    "email": "%s",
                    "phoneNumber": "%s",
                    "county": "Nairobi",
                    "town": "Westlands",
                    "residentialAddress": "Test Address",
                    "postalAddress": "00100",
                    "password": "Password123!",
                    "verificationToken": "%s"
                }
                """.formatted(nationalId, email, phone, token);
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    private String uniquePhone() {
        long number = Math.floorMod(
                UUID.randomUUID().getLeastSignificantBits(),
                100_000_000L
        );
        return "+2547" + String.format("%08d", number);
    }

    private String uniqueNationalId() {
        long number = Math.floorMod(
                UUID.randomUUID().getLeastSignificantBits(),
                1_000_000_000L
        );
        return String.format("%09d", number);
    }
}
