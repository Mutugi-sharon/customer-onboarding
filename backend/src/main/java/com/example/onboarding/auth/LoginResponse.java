
package com.example.onboarding.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String subjectId;
    private String role;
    private String name;
}
