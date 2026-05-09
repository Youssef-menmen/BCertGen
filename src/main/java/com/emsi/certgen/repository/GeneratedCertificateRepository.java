package com.emsi.certgen.repository;

import com.emsi.certgen.model.GeneratedCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeneratedCertificateRepository extends JpaRepository<GeneratedCertificate, Long> {

    List<GeneratedCertificate> findAllByOrderByGeneratedAtDesc();

    List<GeneratedCertificate> findByAdminUsernameOrderByGeneratedAtDesc(String adminUsername);

    List<GeneratedCertificate> findByBatchIdOrderByGeneratedAtDesc(String batchId);

    @Query("SELECT COUNT(DISTINCT g.studentEmail) FROM GeneratedCertificate g WHERE g.studentEmail IS NOT NULL AND g.studentEmail != ''")
    Long countDistinctStudents();

    @Query("SELECT COUNT(g) FROM GeneratedCertificate g")
    Long countTotalGenerated();
}
