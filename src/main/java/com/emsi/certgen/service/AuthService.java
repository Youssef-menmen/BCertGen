package com.emsi.certgen.service;

import com.emsi.certgen.dto.*;
import com.emsi.certgen.model.Admin;
import com.emsi.certgen.repository.AdminRepository;
import com.emsi.certgen.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.reset-token-expiry-hours:2}")
    private int resetTokenExpiryHours;

    /* ══════════════════════════════════════
       LOGIN — username OU email + mot de passe
    ══════════════════════════════════════ */
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getUsername().trim();

        /* Cherche par username OU email */
        Admin admin = adminRepository.findByUsernameOrEmail(identifier)
                .orElse(null);

        /* Anti brute-force + message générique */
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            throw new RuntimeException("Identifiants incorrects.");
        }

        String token = jwtUtil.generateToken(admin.getUsername(), admin.getRole(), admin.getFullName());

        log.info("✅ Connexion : {} ({}) via '{}'",
                admin.getFullName(), admin.getUsername(),
                identifier.contains("@") ? "email" : "username");

        return LoginResponse.builder()
                .token(token)
                .username(admin.getUsername())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    /* ══════════════════════════════════════
       VERIFY TOKEN
    ══════════════════════════════════════ */
    public LoginResponse verifyToken(String token) {
        if (!jwtUtil.isTokenValid(token))
            throw new RuntimeException("Token invalide ou expiré.");

        String username = jwtUtil.extractUsername(token);
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        return LoginResponse.builder()
                .token(token)
                .username(admin.getUsername())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    /* ══════════════════════════════════════
       FORGOT PASSWORD
    ══════════════════════════════════════ */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail()).orElse(null);
        if (admin == null) {
            log.warn("Reset pour email inconnu: {}", request.getEmail());
            return;
        }
        String resetToken = UUID.randomUUID().toString();
        admin.setResetToken(resetToken);
        admin.setResetTokenExpiry(LocalDateTime.now().plusHours(resetTokenExpiryHours));
        adminRepository.save(admin);
        emailService.sendPasswordResetEmail(admin.getEmail(), admin.getFullName(), resetToken);
        log.info("📧 Email reset envoyé à: {}", admin.getEmail());
    }

    /* ══════════════════════════════════════
       RESET PASSWORD
    ══════════════════════════════════════ */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Admin admin = adminRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide ou déjà utilisé."));

        if (admin.getResetTokenExpiry() == null ||
            LocalDateTime.now().isAfter(admin.getResetTokenExpiry()))
            throw new RuntimeException("Ce lien a expiré. Veuillez en demander un nouveau.");

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        admin.setResetToken(null);
        admin.setResetTokenExpiry(null);
        adminRepository.save(admin);
        log.info("✅ Mot de passe réinitialisé: {}", admin.getUsername());
    }
}
