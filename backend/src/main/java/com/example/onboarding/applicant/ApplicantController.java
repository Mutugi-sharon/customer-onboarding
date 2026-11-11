
package com.example.onboarding.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applicants")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicantController {

    @Autowired
    ApplicantService applicantService;

    @GetMapping("/gender")
    public List<GenderDto> getGender() {
        return applicantService.getGender();
    }

    @GetMapping("/nationalities")
    public List<NationalityDto> getNationalities() {
        return applicantService.getNationalities();
    }

    @PostMapping("/personal-details")
    public Map<String, Object> createApplicant(
            @RequestBody PersonalDetailsDto applicant) {

        int result = applicantService.savePersonalDetails(applicant);

        Map<String, Object> response = new HashMap<>();

        if (result > 0) {
            response.put("message", "Personal details saved successfully");
            response.put("applicant", applicant);
        }

        return response;
    }

    @PostMapping("/contact-details")
    public Map<String, Object> saveContactDetails(
            @RequestBody ContactDetailsDto contact) {

        int result = applicantService.saveContactDetails(contact);

        Map<String, Object> response = new HashMap<>();

        if (result > 0) {
            response.put("message", "Contact details saved successfully");
        } else {
            response.put("message", "Applicant not found");
        }

        return response;
    }

    @GetMapping("/{applicantId}")
    public ResponseEntity<Map<String, Object>> getApplicantById(
            @PathVariable UUID applicantId) {

        Map<String, Object> applicant =
                applicantService.getApplicantById(applicantId);

        if (applicant == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Applicant not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(applicant);
    }
}
