package com.emsi.certgen.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecordExportRequest {
    private String      batchId;
    private Integer     certificateCount;
    private String      templateName;
    private String      templateType;
    private String      exportType;
    private List<String> studentNames;  /* gardé pour compatibilité ExportHistory */

    /* Données détaillées des étudiants pour GeneratedCertificate */
    private List<StudentData> students;

    @Data
    public static class StudentData {
        private String nom;
        private String prenom;
        private String email;
    }
}
