
package com.example.onboarding.otp;

import com.example.onboarding.email.EmailService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class OtpService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            JdbcTemplate jdbcTemplate,
            EmailService emailService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
    }

    // Generate and send a six-digit OTP
    @Transactional
    public void sendOtp(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        // Prevent resending within 60 seconds
        List<Map<String, Object>> recent =
                jdbcTemplate.queryForList(
                        """
                        SELECT last_sent_at
                        FROM otp_challenges
                        WHERE email = ?
                        ORDER BY created_at DESC
                        LIMIT 1
                        """,
                        normalizedEmail
                );

        if (!recent.isEmpty()) {
            Timestamp lastSent =
                    (Timestamp) recent.get(0).get("last_sent_at");

            if (lastSent.toInstant()
                    .isAfter(Instant.now().minusSeconds(60))) {
                throw new IllegalArgumentException(
                        "Please wait 60 seconds before requesting another OTP."
                );
            }
        }

        // Invalidate previous unused OTPs
        jdbcTemplate.update(
                """
                UPDATE otp_challenges
                SET consumed_at = CURRENT_TIMESTAMP
                WHERE email = ?
                  AND consumed_at IS NULL
                """,
                normalizedEmail
        );

        // Generate a secure six-digit OTP
        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // Store only the OTP hash
        String otpHash = hashValue(otp);

        jdbcTemplate.update(
                """
                INSERT INTO otp_challenges
                (email, otp_hash, expires_at, attempts, max_attempts)
                VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '5 minutes', 0, 5)
                """,
                normalizedEmail,
                otpHash
        );

        // Send OTP to the applicant
        emailService.sendOtpEmail(normalizedEmail, otp);
    }

    // Verify OTP and issue a short-lived verification token
    @Transactional
    public String verifyOtp(String email, String enteredOtp) {

        String normalizedEmail = email.trim().toLowerCase();

        if (enteredOtp == null ||
                !enteredOtp.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "Enter a valid six-digit OTP."
            );
        }

        List<Map<String, Object>> challenges =
                jdbcTemplate.queryForList(
                        """
                        SELECT id, otp_hash, attempts, max_attempts
                        FROM otp_challenges
                        WHERE email = ?
                          AND consumed_at IS NULL
                          AND verified_at IS NULL
                          AND expires_at > CURRENT_TIMESTAMP
                        ORDER BY created_at DESC
                        LIMIT 1
                        """,
                        normalizedEmail
                );

        if (challenges.isEmpty()) {
            throw new IllegalArgumentException(
                    "OTP has expired or was not found. Please request a new one."
            );
        }

        Map<String, Object> challenge = challenges.get(0);

        long challengeId =
                ((Number) challenge.get("id")).longValue();

        String storedHash =
                (String) challenge.get("otp_hash");

        int attempts =
                ((Number) challenge.get("attempts")).intValue();

        int maxAttempts =
                ((Number) challenge.get("max_attempts")).intValue();

        if (attempts >= maxAttempts) {
            throw new IllegalArgumentException(
                    "Too many incorrect attempts. Request a new OTP."
            );
        }

        String enteredHash = hashValue(enteredOtp);

        boolean matches = MessageDigest.isEqual(
                storedHash.getBytes(StandardCharsets.UTF_8),
                enteredHash.getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {

            jdbcTemplate.update(
                    """
                    UPDATE otp_challenges
                    SET attempts = attempts + 1,
                        consumed_at = CASE
                            WHEN attempts + 1 >= max_attempts
                            THEN CURRENT_TIMESTAMP
                            ELSE consumed_at
                        END
                    WHERE id = ?
                    """,
                    challengeId
            );

            throw new IllegalArgumentException(
                    "Incorrect OTP. Please check the code and try again."
            );
        }

        // Mark OTP as successfully verified and consumed
        jdbcTemplate.update(
                """
                UPDATE otp_challenges
                SET verified_at = CURRENT_TIMESTAMP,
                    consumed_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """,
                challengeId
        );

        // Generate a secure random verification token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        String verificationToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(tokenBytes);

        String tokenHash = hashValue(verificationToken);

        // Verification token expires after 10 minutes
        jdbcTemplate.update(
                """
                INSERT INTO otp_verification_tokens
                (email, token_hash, expires_at)
                VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '10 minutes')
                """,
                normalizedEmail,
                tokenHash
        );

        return verificationToken;
    }

    // Consume the verification token during final registration
    @Transactional
    public boolean consumeVerificationToken(
            String email,
            String verificationToken
    ) {

        if (email == null || verificationToken == null ||
                verificationToken.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String tokenHash = hashValue(verificationToken);

        int updated = jdbcTemplate.update(
                """
                UPDATE otp_verification_tokens
                SET consumed_at = CURRENT_TIMESTAMP
                WHERE email = ?
                  AND token_hash = ?
                  AND consumed_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                """,
                normalizedEmail,
                tokenHash
        );

        return updated == 1;
    }

    // SHA-256 hashing
    private String hashValue(String value) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to securely hash verification data.",
                    exception
            );
        }
    }
}
