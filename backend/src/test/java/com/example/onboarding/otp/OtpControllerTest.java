
package com.example.onboarding.otp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.onboarding.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.mockito.ArgumentCaptor;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtpControllerTest extends IntegrationTest {

    @Test
    void sendOtp_withValidEmail_sendsSixDigitCode() throws Exception {

        String email = "otp-" + UUID.randomUUID() + "@example.com";

        String otp = sendOtpAndCaptureCode(email);

        assertTrue(
                otp.matches("\\d{6}"),
                "The emailed OTP should contain exactly six digits"
        );
    }

    @Test
    void verifyOtp_withCorrectCode_returnsVerificationToken()
            throws Exception {

        String email = "verify-" + UUID.randomUUID() + "@example.com";

        String otp = sendOtpAndCaptureCode(email);

        mvc.perform(post("/api/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s",
                                    "otp": "%s"
                                }
                                """.formatted(email, otp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Email verified successfully."))
                .andExpect(jsonPath("$.verificationToken").isNotEmpty());
    }

    private String sendOtpAndCaptureCode(String email)
            throws Exception {

        mvc.perform(post("/api/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "%s"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("OTP sent successfully to your email."));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(email, message.getTo()[0]);

        Matcher matcher = Pattern.compile(
                "(?m)^([0-9]{6})$"
        ).matcher(message.getText());
        assertTrue(matcher.find(), "Email should contain a six-digit OTP");

        return matcher.group(1);
    }
}
