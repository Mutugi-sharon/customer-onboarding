
package com.example.onboarding.applicant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotBlank(message = "Application status is required")
    private String status;

    private String reason;
}
