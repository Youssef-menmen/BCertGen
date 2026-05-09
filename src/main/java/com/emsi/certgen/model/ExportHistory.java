package com.emsi.certgen.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Historique des exports de certificats PDF
 * Chaque ligne = un lot d'export (batch)
 */
@Entity
@Table(name = "export_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", unique = true, nullable = false, length = 50)
    private String batchId;

    @Column(name = "admin_username", nullable = false, length = 100)
    private String adminUsername;

    @Column(name = "admin_full_name", length = 200)
    private String adminFullName;

    @Column(name = "exported_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime exportedAt;

    @Column(name = "certificate_count", nullable = false)
    private Integer certificateCount;

    @Column(name = "template_name", length = 255)
    private String templateName;

    @Column(name = "template_type", length = 10)
    private String templateType;

    @Column(name = "export_type", length = 20)
    private String exportType; /* ZIP ou INDIVIDUAL */

    @Column(name = "student_names", columnDefinition = "TEXT")
    private String studentNames; /* JSON array des noms */

    @PrePersist
    protected void onCreate() {
        if (exportedAt == null) exportedAt = LocalDateTime.now();
    }
}
