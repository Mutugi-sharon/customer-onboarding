
package com.example.onboarding.applicant;

import com.example.onboarding.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.servlet.MvcResult;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationControllerTest extends IntegrationTest {

    @Test
    void register_withValidDetailsAndVerifiedEmail_returnsCreated()
            throws Exception {

        String email = uniqueEmail();
        String phone = uniquePhone();
        String nationalId = uniqueNationalId();

        String token = requestVerificationToken(email);

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email, phone, nationalId, token
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").isNotEmpty())
                .andExpect(jsonPath("$.message")
                        .value("Registration successful"));
    }

    @Test
    void register_withDuplicateEmail_returnsBadRequest()
            throws Exception {

        String email = uniqueEmail();

        createApplicant(email, uniquePhone(), uniqueNationalId());

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email,
                                uniquePhone(),
                                uniqueNationalId(),
                                "unused-token"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Email already exists."));
    }

    @Test
    void register_withDuplicatePhone_returnsBadRequest()
            throws Exception {

        String phone = uniquePhone();

        createApplicant(uniqueEmail(), phone, uniqueNationalId());

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                uniqueEmail(),
                                phone,
                                uniqueNationalId(),
                                "unused-token"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Phone number already exists."));
    }

    @Test
    void register_withDuplicateNationalId_returnsBadRequest()
            throws Exception {

        String nationalId = uniqueNationalId();

        createApplicant(uniqueEmail(), uniquePhone(), nationalId);

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                uniqueEmail(),
                                uniquePhone(),
                                nationalId,
                                "unused-token"
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("National ID already exists."));
    }

    @Test
    void checkDuplicates_withExistingApplicant_returnsTrue()
            throws Exception {

        String email = uniqueEmail();
        String phone = uniquePhone();
        String nationalId = uniqueNationalId();

        createApplicant(email, phone, nationalId);

        mvc.perform(get("/api/applicants/check-duplicates")
                        .param("email", email)
                        .param("phoneNumber", phone)
                        .param("identificationNumber", nationalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailExists").value(true))
                .andExpect(jsonPath("$.phoneExists").value(true))
                .andExpect(jsonPath("$.identificationNumberExists")
                        .value(true));
    }

    private void createApplicant(
            String email,
            String phone,
            String nationalId) throws Exception {

        String token = requestVerificationToken(email);

        mvc.perform(post("/api/applicants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(
                                email, phone, nationalId, token
                        )))
                .andExpect(status().isCreated());
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

        assertEquals(email, message.getTo()[0]);

        Matcher matcher = Pattern.compile(
                "(?m)^([0-9]{6})$"
        ).matcher(message.getText());

        assertTrue(matcher.find(), "Email should contain a six-digit OTP");

        String otp = matcher.group(1);

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
