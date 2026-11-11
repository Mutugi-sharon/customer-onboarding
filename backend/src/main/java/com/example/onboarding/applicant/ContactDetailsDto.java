package com.example.onboarding.applicant;

import lombok.Data;
import java.util.UUID;

@Data
public class ContactDetailsDto {
    private UUID applicantId;
    private String email;
    private String phoneNumber;
    private String county;
    private String town;
    private String residentialAddress;
    private String postalAddress;
}