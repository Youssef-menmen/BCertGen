package com.emsi.certgen.repository;

import com.emsi.certgen.model.ExportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExportHistoryRepository extends JpaRepository<ExportHistory, Long> {
    List<ExportHistory> findAllByOrderByExportedAtDesc();
    List<ExportHistory> findByAdminUsernameOrderByExportedAtDesc(String username);

    @Query("SELECT SUM(e.certificateCount) FROM ExportHistory e")
    Long sumTotalCertificates();

    @Query("SELECT COUNT(e) FROM ExportHistory e")
    Long countTotalBatches();
}
