package com.emsi.certgen.repository;

import com.emsi.certgen.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByResetToken(String resetToken);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    /* Connexion par username OU email */
    @Query("SELECT a FROM Admin a WHERE a.username = :identifier OR a.email = :identifier")
    Optional<Admin> findByUsernameOrEmail(@Param("identifier") String identifier);
}
