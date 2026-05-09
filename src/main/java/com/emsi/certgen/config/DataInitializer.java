package com.emsi.certgen.config;

import com.emsi.certgen.model.Admin;
import com.emsi.certgen.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer — Crée le premier super-admin au démarrage
 * si aucun admin n'existe dans la base de données
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin superAdmin = Admin.builder()
                    .username("youssef.assid")
                    .fullName("Youssef ASSID")
                    .email("youssef.assid@emsi.ma")
                    .passwordHash(passwordEncoder.encode("Admin@EMSI2024"))
                    .role("ADMIN")
                    .createdBy("SYSTEM")
                    .build();

            adminRepository.save(superAdmin);

            log.info("═══════════════════════════════════════════");
            log.info("  ✅ Super admin créé automatiquement !");
            log.info("  👤 Username : youssef.assid");
            log.info("  🔑 Password : Admin@EMSI2024");
            log.info("  ⚠️  Changez ce mot de passe après connexion !");
            log.info("═══════════════════════════════════════════");
        }
    }
}
