
package com.example.onboarding.applicant;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/applicants")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicantController {

    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @GetMapping("/gender")
    public List<GenderDto> getGender() {
        return applicantService.getGender();
    }

    @GetMapping("/nationalities")
    public List<NationalityDto> getNationalities() {
        return applicantService.getNationalities();
    }

    // CHECK DUPLICATE REGISTRATION DETAILS
    @GetMapping("/check-duplicates")
    public ResponseEntity<Map<String, Boolean>> checkDuplicates(
            @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam String identificationNumber) {

        return ResponseEntity.ok(
                applicantService.checkDuplicates(
                        email,
                        phoneNumber,
                        identificationNumber
                )
        );
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

        return Map.of(
                "message",
                result > 0
                        ? "Contact details saved successfully"
                        : "Applicant not found"
        );
    }

    // REGISTER APPLICANT
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest req) {

        try {
            UUID id = applicantService.registerApplicant(req);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "applicationId", id.toString(),
                            "message", "Registration successful"
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of("message", "Registration failed.")
            );
        }
    }

    // LIST APPLICANTS
    @GetMapping("/list")
    public List<Map<String, Object>> listApplicants() {
        return applicantService.listApplicants();
    }

    // REVIEW APPLICATION
    @PatchMapping("/{id}/review")
    public ResponseEntity<?> review(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequest req) {

        String status = req.getStatus().trim().toUpperCase();

        String reason = req.getReason() == null
                ? ""
                : req.getReason().trim();

        if (!List.of(
                "APPROVED",
                "REJECTED",
                "PENDING_REVIEW"
        ).contains(status)) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "Invalid status: " + req.getStatus())
            );
        }

        if ("REJECTED".equals(status) && reason.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "A rejection reason is required.")
            );
        }

        int updated = applicantService.reviewApplicant(
                id,
                status,
                reason
        );

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Applicant not found")
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Application status updated successfully",
                        "status", status
                )
        );
    }

    // UPLOAD APPLICANT DOCUMENTS
    @PostMapping(
            value = "/{id}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadDocuments(
            @PathVariable UUID id,

            @RequestParam(value = "nationalId", required = false)
            MultipartFile nationalId,

            @RequestParam(value = "kraCertificate", required = false)
            MultipartFile kraCertificate,

            @RequestParam(value = "passportPhoto", required = false)
            MultipartFile passportPhoto,

            @RequestParam(value = "proofOfAddress", required = false)
            MultipartFile proofOfAddress,

            @RequestParam(value = "proofOfIncome", required = false)
            MultipartFile proofOfIncome,

            @RequestParam(value = "employmentLetter", required = false)
            MultipartFile employmentLetter) {

        if (applicantService.getApplicantById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Applicant not found")
            );
        }

        try {
            String base = "uploads/" + id;

            Files.createDirectories(Paths.get(base));

            String nationalIdLink = save(nationalId, base, "national_id");
            String kraLink = save(kraCertificate, base, "kra");
            String photoLink = save(passportPhoto, base, "photo");
            String addressLink = save(proofOfAddress, base, "address_proof");
            String incomeLink = save(proofOfIncome, base, "income");
            String employmentLink = save(
                    employmentLetter, base, "employment_letter"
            );

            applicantService.updateDocuments(
                    id,
                    nationalIdLink,
                    kraLink,
                    photoLink,
                    addressLink,
                    incomeLink,
                    employmentLink
            );

            Map<String, Object> body = new HashMap<>();

            body.put("message", "Documents uploaded");
            body.put("nationalId", nationalIdLink);
            body.put("kra", kraLink);
            body.put("photo", photoLink);
            body.put("addressProof", addressLink);
            body.put("income", incomeLink);
            body.put("employmentLetter", employmentLink);

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of("message", "Upload failed.")
            );
        }
    }

    // SAVE AN UPLOADED FILE
    private String save(
            MultipartFile file,
            String base,
            String name) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String original = file.getOriginalFilename();
        String ext = "";

        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'))
                    .toLowerCase();
        }

        if (!List.of(
                ".pdf", ".png", ".jpg", ".jpeg"
        ).contains(ext)) {
            throw new IOException("Unsupported document file type");
        }

        Path target = Paths.get(base, name + ext);

        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );

        return target.toString();
    }

    // GET APPLICANT DETAILS
    // UUID pattern prevents check-duplicates from matching this route.
    @GetMapping("/{applicantId:^[0-9a-fA-F-]{36}$}")
    public ResponseEntity<Map<String, Object>> getApplicantById(
            @PathVariable UUID applicantId) {

        Map<String, Object> applicant =
                applicantService.getApplicantById(applicantId);

        if (applicant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "Applicant not found")
            );
        }

        return ResponseEntity.ok(applicant);
    }

    // VIEW OR DOWNLOAD AN APPLICANT DOCUMENT
    @GetMapping("/{applicantId:^[0-9a-fA-F-]{36}$}/documents/{documentType}")
    public ResponseEntity<Resource> viewDocument(
            @PathVariable UUID applicantId,
            @PathVariable String documentType,
            @RequestParam(defaultValue = "false") boolean download) {

        Map<String, Object> applicant =
                applicantService.getApplicantById(applicantId);

        if (applicant == null) {
            return ResponseEntity.notFound().build();
        }

        String columnName = switch (documentType) {
            case "national_id" -> "national_id_link";
            case "kra" -> "kra_link";
            case "photo" -> "photo_link";
            case "address_proof" -> "address_proof_link";
            case "income" -> "income_link";
            case "employment_letter" -> "employment_letter_link";
            default -> null;
        };

        if (columnName == null) {
            return ResponseEntity.badRequest().build();
        }

        Object storedPath = applicant.get(columnName);

        if (storedPath == null || storedPath.toString().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path uploadsRoot = Paths.get("uploads")
                    .toAbsolutePath()
                    .normalize();

            Path applicantFolder = uploadsRoot
                    .resolve(applicantId.toString())
                    .normalize();

            Path filePath = Paths.get(storedPath.toString())
                    .toAbsolutePath()
                    .normalize();

            if (!filePath.startsWith(applicantFolder)
                    || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            MediaType contentType = MediaTypeFactory
                    .getMediaType(resource)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            String disposition = download
                    ? "attachment"
                    : "inline";

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition + "; filename=\"" +
                                    resource.getFilename() + "\""
                    )
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
