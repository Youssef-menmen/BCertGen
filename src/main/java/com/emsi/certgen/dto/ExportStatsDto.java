package com.emsi.certgen.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportStatsDto {
    private Long totalBatches;
    private Long totalCertificates;
    private Long totalAdmins;
    private Long totalStudents;
}
