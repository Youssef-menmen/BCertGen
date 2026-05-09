package com.emsi.certgen.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Traçabilité : un enregistrement par étudiant par export
 * Permet de savoir quel admin a généré le certificat de quel étudiant et quand
 */
@Entity
@Table(name = "generated_certificates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Étudiant ── */
    @Column(name = "student_nom", nullable = false, length = 150)
    private String studentNom;

    @Column(name = "student_prenom", nullable = false, length = 150)
    private String studentPrenom;

    @Column(name = "student_email", length = 255)
    private String studentEmail;

    /* ── Administrateur ── */
    @Column(name = "admin_username", nullable = false, length = 100)
    private String adminUsername;

    @Column(name = "admin_full_name", length = 200)
    private String adminFullName;

    /* ── Export ── */
    @Column(name = "batch_id", length = 60)
    private String batchId;

    @Column(name = "export_type", length = 20)
    private String exportType; /* ZIP ou INDIVIDUAL */

    @Column(name = "template_name", length = 255)
    private String templateName;

    @Column(name = "generated_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }
}
