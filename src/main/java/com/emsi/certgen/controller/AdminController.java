package com.emsi.certgen.controller;

import com.emsi.certgen.dto.*;
import com.emsi.certgen.service.AdminService;
import com.emsi.certgen.service.ExportHistoryService;
import com.emsi.certgen.service.GeneratedCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService                  adminService;
    private final ExportHistoryService          exportHistoryService;
    private final GeneratedCertificateService   certService;

    /* ── Créer un admin ── */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Administrateur créé.", adminService.createAdmin(request, auth.getName())));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ── Liste admins ── */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> listAdmins() {
        return ResponseEntity.ok(ApiResponse.ok("Liste", adminService.getAllAdmins()));
    }

    /* ── Mon profil ── */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(Authentication auth) {
        return adminService.getAllAdmins().stream()
                .filter(a -> a.getUsername().equals(auth.getName()))
                .findFirst()
                .map(a -> ResponseEntity.ok(ApiResponse.ok("Profil", a)))
                .orElse(ResponseEntity.notFound().build());
    }

    /* ── Modifier mon profil ── */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Profil mis à jour.", adminService.updateOwnProfile(auth.getName(), request)));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ── Changer mon mot de passe ── */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changeMyPassword(@Valid @RequestBody ChangePasswordRequest request, Authentication auth) {
        try {
            adminService.changeOwnPassword(auth.getName(), request);
            return ResponseEntity.ok(ApiResponse.ok("Mot de passe modifié."));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ── Super admin : modifier un admin ── */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateAdmin(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request, Authentication auth) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Admin mis à jour.", adminService.updateAdminBySuperAdmin(id, request, auth.getName())));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ── Super admin : reset password ── */
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse> resetAdminPassword(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        try {
            String pwd = body.get("newPassword");
            if (pwd == null || pwd.length() < 8) return ResponseEntity.badRequest().body(ApiResponse.error("Minimum 8 caractères."));
            adminService.resetAdminPasswordBySuperAdmin(id, pwd, auth.getName());
            return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé."));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ── Super admin : supprimer ── */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAdmin(@PathVariable Long id, Authentication auth) {
        try {
            adminService.deleteAdmin(id, auth.getName());
            return ResponseEntity.ok(ApiResponse.ok("Admin supprimé."));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    /* ════════════════════════════════════════
       EXPORTS HISTORY
    ════════════════════════════════════════ */
    @PostMapping("/exports/record")
    public ResponseEntity<ApiResponse> recordExport(@RequestBody RecordExportRequest request, Authentication auth) {
        try {
            /* Enregistrer dans ExportHistory (lots) */
            ExportHistoryDto dto = exportHistoryService.recordExport(request, auth.getName());
            /* Enregistrer dans GeneratedCertificates (étudiants individuels) */
            certService.recordStudents(request, auth.getName());
            return ResponseEntity.ok(ApiResponse.ok("Export enregistré.", dto));
        } catch (RuntimeException e) { return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage())); }
    }

    @GetMapping("/exports/my")
    public ResponseEntity<ApiResponse> getMyExports(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Mes exports", exportHistoryService.getMyHistory(auth.getName())));
    }

    @GetMapping("/exports/all")
    public ResponseEntity<ApiResponse> getAllExports(Authentication auth) {
        if (!adminService.isSuperAdmin(auth.getName()))
            return ResponseEntity.status(403).body(ApiResponse.error("Accès réservé au super administrateur."));
        return ResponseEntity.ok(ApiResponse.ok("Historique", exportHistoryService.getAllHistory()));
    }

    @GetMapping("/exports/stats")
    public ResponseEntity<ApiResponse> getStats(Authentication auth) {
        if (!adminService.isSuperAdmin(auth.getName()))
            return ResponseEntity.status(403).body(ApiResponse.error("Accès réservé au super administrateur."));
        return ResponseEntity.ok(ApiResponse.ok("Statistiques", exportHistoryService.getStats()));
    }

    /* ════════════════════════════════════════
       CERTIFICATS GÉNÉRÉS (traçabilité étudiants)
    ════════════════════════════════════════ */

    /* Super admin : tous les certificats */
    @GetMapping("/certificates/all")
    public ResponseEntity<ApiResponse> getAllCertificates(Authentication auth) {
        if (!adminService.isSuperAdmin(auth.getName()))
            return ResponseEntity.status(403).body(ApiResponse.error("Accès réservé au super administrateur."));
        return ResponseEntity.ok(ApiResponse.ok("Certificats générés", certService.getAll()));
    }

    /* Admin : ses propres certificats */
    @GetMapping("/certificates/my")
    public ResponseEntity<ApiResponse> getMyCertificates(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Mes certificats", certService.getByAdmin(auth.getName())));
    }
}
