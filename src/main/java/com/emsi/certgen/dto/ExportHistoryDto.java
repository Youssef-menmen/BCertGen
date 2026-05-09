package com.emsi.certgen.dto;

import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportHistoryDto {
    private Long id;
    private String batchId;
    private String adminUsername;
    private String adminFullName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime exportedAt;
    private Integer certificateCount;
    private String templateName;
    private String templateType;
    private String exportType;
    private String studentNames;
}
