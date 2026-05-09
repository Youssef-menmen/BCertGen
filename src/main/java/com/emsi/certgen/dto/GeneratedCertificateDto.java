package com.emsi.certgen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedCertificateDto {
    private Long   id;
    private String studentNom;
    private String studentPrenom;
    private String studentEmail;
    private String adminUsername;
    private String adminFullName;
    private String batchId;
    private String exportType;
    private String templateName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;
}
