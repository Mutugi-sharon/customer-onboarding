package com.example.onboarding.auth;

import com.example.onboarding.security.JwtService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(JdbcTemplate jdbc, PasswordEncoder encoder, JwtService jwtService) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginResponse loginApplicant(LoginRequest req) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, full_name, password_hash FROM applicants WHERE email = ?",
                req.getEmail()
        );
        if (rows.isEmpty()) throw new RuntimeException("Invalid credentials");
        Map<String, Object> row = rows.get(0);
        String hash = (String) row.get("password_hash");
        if (hash == null || !encoder.matches(req.getPassword(), hash)) {
            throw new RuntimeException("Invalid credentials");
        }
        String id = row.get("id").toString();
        String name = (String) row.get("full_name");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "APPLICANT");
        String token = jwtService.generateToken(id, claims);
        return new LoginResponse(token, id, "APPLICANT", name);
    }

    public LoginResponse loginStaff(StaffLoginRequest req) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, full_name, password_hash, role, is_active FROM staff WHERE staff_id = ?",
                req.getStaffId()
        );
        if (rows.isEmpty()) throw new RuntimeException("Invalid credentials");
        Map<String, Object> row = rows.get(0);
        Boolean active = (Boolean) row.get("is_active");
        if (active == null || !active) throw new RuntimeException("Staff account is inactive");
        String hash = (String) row.get("password_hash");
        if (!encoder.matches(req.getPassword(), hash)) throw new RuntimeException("Invalid credentials");

        String id = row.get("id").toString();
        String role = (String) row.get("role");
        String name = (String) row.get("full_name");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        String token = jwtService.generateToken(id, claims);
        return new LoginResponse(token, id, role, name);
    }
}