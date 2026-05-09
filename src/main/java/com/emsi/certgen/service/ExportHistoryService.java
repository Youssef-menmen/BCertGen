package com.emsi.certgen.service;

import com.emsi.certgen.dto.ExportHistoryDto;
import com.emsi.certgen.repository.GeneratedCertificateRepository;
import com.emsi.certgen.dto.ExportStatsDto;
import com.emsi.certgen.dto.RecordExportRequest;
import com.emsi.certgen.model.ExportHistory;
import com.emsi.certgen.repository.AdminRepository;
import com.emsi.certgen.repository.ExportHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportHistoryService {

    private final ExportHistoryRepository exportHistoryRepository;
    private final AdminRepository adminRepository;
    private final GeneratedCertificateRepository certRepository;
    private final ObjectMapper objectMapper;

    /* ══════════════════════════════════════
       ENREGISTRER UN EXPORT
    ══════════════════════════════════════ */
    @Transactional
    public ExportHistoryDto recordExport(RecordExportRequest request, String adminUsername) {
        String adminFullName = adminRepository.findByUsername(adminUsername)
                .map(a -> a.getFullName())
                .orElse(adminUsername);

        String studentNamesJson = "[]";
        try {
            if (request.getStudentNames() != null) {
                studentNamesJson = objectMapper.writeValueAsString(request.getStudentNames());
            }
        } catch (Exception e) {
            log.warn("Erreur sérialisation noms: {}", e.getMessage());
        }

        ExportHistory history = ExportHistory.builder()
                .batchId(request.getBatchId())
                .adminUsername(adminUsername)
                .adminFullName(adminFullName)
                .certificateCount(request.getCertificateCount())
                .templateName(request.getTemplateName() != null ? request.getTemplateName() : "Sans template")
                .templateType(request.getTemplateType() != null ? request.getTemplateType() : "none")
                .exportType(request.getExportType() != null ? request.getExportType() : "ZIP")
                .studentNames(studentNamesJson)
                .build();

        history = exportHistoryRepository.save(history);
        log.info("📊 Export enregistré: {} — {} certificats par {}",
                request.getBatchId(), request.getCertificateCount(), adminUsername);
        return toDto(history);
    }

    /* ══════════════════════════════════════
       LISTE COMPLÈTE (super admin)
    ══════════════════════════════════════ */
    public List<ExportHistoryDto> getAllHistory() {
        return exportHistoryRepository.findAllByOrderByExportedAtDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /* ══════════════════════════════════════
       HISTORIQUE D'UN ADMIN
    ══════════════════════════════════════ */
    public List<ExportHistoryDto> getMyHistory(String username) {
        return exportHistoryRepository.findByAdminUsernameOrderByExportedAtDesc(username)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /* ══════════════════════════════════════
       STATISTIQUES GLOBALES
    ══════════════════════════════════════ */
    public ExportStatsDto getStats() {
        Long totalBatches      = exportHistoryRepository.countTotalBatches();
        Long totalCertificates = exportHistoryRepository.sumTotalCertificates();
        Long totalAdmins       = adminRepository.count();
        Long totalStudents     = certRepository.countDistinctStudents();
        return ExportStatsDto.builder()
                .totalBatches(totalBatches != null ? totalBatches : 0L)
                .totalCertificates(totalCertificates != null ? totalCertificates : 0L)
                .totalAdmins(totalAdmins)
                .totalStudents(totalStudents != null ? totalStudents : 0L)
                .build();
    }

    private ExportHistoryDto toDto(ExportHistory h) {
        return ExportHistoryDto.builder()
                .id(h.getId())
                .batchId(h.getBatchId())
                .adminUsername(h.getAdminUsername())
                .adminFullName(h.getAdminFullName())
                .exportedAt(h.getExportedAt())
                .certificateCount(h.getCertificateCount())
                .templateName(h.getTemplateName())
                .templateType(h.getTemplateType())
                .exportType(h.getExportType())
                .studentNames(h.getStudentNames())
                .build();
    }
}
