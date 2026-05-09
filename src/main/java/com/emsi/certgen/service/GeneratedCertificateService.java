package com.emsi.certgen.service;

import com.emsi.certgen.dto.GeneratedCertificateDto;
import com.emsi.certgen.dto.RecordExportRequest;
import com.emsi.certgen.model.GeneratedCertificate;
import com.emsi.certgen.repository.AdminRepository;
import com.emsi.certgen.repository.GeneratedCertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratedCertificateService {

    private final GeneratedCertificateRepository certRepository;
    private final AdminRepository                adminRepository;

    /* ══════════════════════════════════════
       Enregistrer tous les étudiants d'un export
    ══════════════════════════════════════ */
    @Transactional
    public void recordStudents(RecordExportRequest request, String adminUsername) {
        if (request.getStudents() == null || request.getStudents().isEmpty()) return;

        String adminFullName = adminRepository.findByUsername(adminUsername)
                .map(a -> a.getFullName())
                .orElse(adminUsername);

        LocalDateTime now = LocalDateTime.now();

        List<GeneratedCertificate> certs = request.getStudents().stream()
                .map(s -> GeneratedCertificate.builder()
                        .studentNom(s.getNom())
                        .studentPrenom(s.getPrenom())
                        .studentEmail(s.getEmail() != null ? s.getEmail() : "")
                        .adminUsername(adminUsername)
                        .adminFullName(adminFullName)
                        .batchId(request.getBatchId())
                        .exportType(request.getExportType())
                        .templateName(request.getTemplateName() != null
                                ? request.getTemplateName() : "Sans template")
                        .generatedAt(now)
                        .build()
                ).collect(Collectors.toList());

        certRepository.saveAll(certs);
        log.info("📋 {} certificats enregistrés (batch: {}) par {}",
                certs.size(), request.getBatchId(), adminUsername);
    }

    /* ── Liste complète (super admin) ── */
    public List<GeneratedCertificateDto> getAll() {
        return certRepository.findAllByOrderByGeneratedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /* ── Liste d'un admin ── */
    public List<GeneratedCertificateDto> getByAdmin(String username) {
        return certRepository.findByAdminUsernameOrderByGeneratedAtDesc(username)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /* ── Nombre total d'étudiants distincts ── */
    public Long countDistinctStudents() {
        return certRepository.countDistinctStudents();
    }

    private GeneratedCertificateDto toDto(GeneratedCertificate c) {
        return GeneratedCertificateDto.builder()
                .id(c.getId())
                .studentNom(c.getStudentNom())
                .studentPrenom(c.getStudentPrenom())
                .studentEmail(c.getStudentEmail())
                .adminUsername(c.getAdminUsername())
                .adminFullName(c.getAdminFullName())
                .batchId(c.getBatchId())
                .exportType(c.getExportType())
                .templateName(c.getTemplateName())
                .generatedAt(c.getGeneratedAt())
                .build();
    }
}
