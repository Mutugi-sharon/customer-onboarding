
package com.example.onboarding.applicant;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PersonalDetailsDto {

    private String fullName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String gender;
    private String identificationType;
    private String identificationNumber;
    private String email;
    private String phoneNumber;
    private String birthCertificateLink;
    private String nationalIdLink;
}
