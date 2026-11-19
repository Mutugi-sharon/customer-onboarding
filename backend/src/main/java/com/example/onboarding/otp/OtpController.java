
package com.example.onboarding.otp;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    // Request an OTP by email
    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        try {
            otpService.sendOtp(request.email());

            return ResponseEntity.ok(
                    Map.of("message", "OTP sent successfully to your email.")
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // Verify the OTP
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        try {
            String verificationToken = otpService.verifyOtp(
                    request.email(),
                    request.otp()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Email verified successfully.",
                            "verificationToken", verificationToken
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // Request DTOs
    public record SendOtpRequest(
            @NotBlank
            @Email
            String email
    ) {}

    public record VerifyOtpRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            @Pattern(regexp = "\\d{6}", message = "OTP must contain exactly 6 digits")
            String otp
    ) {}
}
