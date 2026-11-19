
package com.example.onboarding.applicant;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150,
            message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Identification type is required")
    private String identificationType;

    @NotBlank(message = "National ID number is required")
    @Pattern(regexp = "^\\d{9}$",
            message = "National ID must contain exactly 9 digits")
    private String identificationNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$",
            message = "Enter a valid international phone number")
    private String phoneNumber;

    private String residentialAddress;

    @NotBlank(message = "County is required")
    private String county;

    @NotBlank(message = "Town is required")
    private String town;

    private String postalAddress;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    private String password;

    @NotBlank(message = "Email verification is required")
    private String verificationToken;
}
