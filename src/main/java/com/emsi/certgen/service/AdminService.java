package com.emsi.certgen.service;

import com.emsi.certgen.dto.*;
import com.emsi.certgen.model.Admin;
import com.emsi.certgen.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String SUPER_ADMIN = "youssef.assid";

    /* ══════════════════════════════════════
       CRÉER UN ADMIN
    ══════════════════════════════════════ */
    @Transactional
    public AdminDto createAdmin(CreateAdminRequest request, String createdByUsername) {
        if (adminRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Ce nom d'utilisateur est déjà utilisé.");
        if (adminRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Cet email est déjà utilisé.");

        Admin admin = Admin.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN")
                .createdBy(createdByUsername)
                .build();

        admin = adminRepository.save(admin);
        log.info("✅ Nouvel admin créé: {} par {}", request.getUsername(), createdByUsername);

        /* Email de bienvenue avec lien vers le site */
        try {
            emailService.sendWelcomeEmail(
                admin.getEmail(), admin.getFullName(),
                admin.getUsername(), request.getPassword()
            );
        } catch (Exception e) {
            log.warn("⚠️ Email de bienvenue non envoyé: {}", e.getMessage());
        }
        return toDto(admin);
    }

    /* ══════════════════════════════════════
       MODIFIER SON PROPRE PROFIL
    ══════════════════════════════════════ */
    @Transactional
    public AdminDto updateOwnProfile(String username, UpdateProfileRequest request) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        /* Vérifier unicité email si changé */
        if (!admin.getEmail().equals(request.getEmail()) &&
            adminRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre compte.");
        }

        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        adminRepository.save(admin);
        log.info("✅ Profil mis à jour: {}", username);
        return toDto(admin);
    }

    /* ══════════════════════════════════════
       CHANGER SON MOT DE PASSE
    ══════════════════════════════════════ */
    @Transactional
    public void changeOwnPassword(String username, ChangePasswordRequest request) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash()))
            throw new RuntimeException("Mot de passe actuel incorrect.");

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);
        log.info("✅ Mot de passe changé: {}", username);
    }

    /* ══════════════════════════════════════
       SUPER ADMIN : modifier un autre admin
    ══════════════════════════════════════ */
    @Transactional
    public AdminDto updateAdminBySuperAdmin(Long id, UpdateProfileRequest request, String requestedBy) {
        if (!SUPER_ADMIN.equals(requestedBy))
            throw new RuntimeException("Permission refusée. Seul le super administrateur peut modifier les autres comptes.");

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        if (SUPER_ADMIN.equals(admin.getUsername()))
            throw new RuntimeException("Impossible de modifier le super administrateur via cette route.");

        /* Vérifier unicité email */
        if (!admin.getEmail().equals(request.getEmail()) &&
            adminRepository.existsByEmailAndIdNot(request.getEmail(), id))
            throw new RuntimeException("Cet email est déjà utilisé par un autre compte.");

        /* Vérifier et modifier l username si fourni par le super admin */
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!admin.getUsername().equals(newUsername) &&
                adminRepository.existsByUsernameAndIdNot(newUsername, id))
                throw new RuntimeException("Ce nom d utilisateur est déjà utilisé.");
            admin.setUsername(newUsername);
        }

        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        adminRepository.save(admin);
        log.info("✅ Admin modifié par super admin : {} (username: {})", admin.getFullName(), admin.getUsername());
        return toDto(admin);
    }

    /* ══════════════════════════════════════
       SUPER ADMIN : reset password d'un admin
    ══════════════════════════════════════ */
    @Transactional
    public void resetAdminPasswordBySuperAdmin(Long id, String newPassword, String requestedBy) {
        if (!SUPER_ADMIN.equals(requestedBy))
            throw new RuntimeException("Permission refusée.");

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        if (SUPER_ADMIN.equals(admin.getUsername()))
            throw new RuntimeException("Impossible de modifier le super administrateur.");

        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        log.info("✅ Mot de passe resetté pour {} par super admin", admin.getUsername());
    }

    /* ══════════════════════════════════════
       LISTE
    ══════════════════════════════════════ */
    public List<AdminDto> getAllAdmins() {
        return adminRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /* ══════════════════════════════════════
       SUPPRIMER (super admin seulement)
    ══════════════════════════════════════ */
    @Transactional
    public void deleteAdmin(Long id, String requestedBy) {
        if (!SUPER_ADMIN.equals(requestedBy))
            throw new RuntimeException("Permission refusée. Seul le super administrateur peut supprimer des comptes.");

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin introuvable."));

        if (SUPER_ADMIN.equals(admin.getUsername()))
            throw new RuntimeException("Impossible de supprimer le super administrateur.");

        adminRepository.delete(admin);
        log.info("🗑️ Admin {} supprimé par super admin", admin.getUsername());
    }

    /* ── toDto ── */
    public AdminDto toDto(Admin admin) {
        return AdminDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .createdBy(admin.getCreatedBy())
                .build();
    }

    public boolean isSuperAdmin(String username) {
        return SUPER_ADMIN.equals(username);
    }
}
