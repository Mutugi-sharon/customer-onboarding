package com.example.onboarding.auth;

import lombok.Data;

@Data
public class StaffLoginRequest {
    private String staffId;
    private String password;
}