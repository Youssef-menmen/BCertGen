package com.emsi.certgen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@Slf4j
public class CertgenApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(CertgenApplication.class, args);
    }

    /**
     * Affiché UNE SEULE FOIS quand Spring Boot est 100% prêt
     * (après que Tomcat, JPA, Security sont tous initialisés)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port    = env.getProperty("server.port", "8080");
        String profile = env.getProperty("spring.profiles.active", "local");

        log.info("");
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║       EMSI Certificate Generator — Backend API           ║");
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  ✅ Serveur Tomcat démarré avec succès                   ║");
        log.info("║  🌍 URL locale    : http://localhost:{}                ║", port);
        log.info("║  📋 Profil actif  : {}                                  ║", profile);
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  🔍 Health check  : http://localhost:{}/api/health      ║", port);
        log.info("║  🔐 Login API     : http://localhost:{}/api/auth/login  ║", port);
        log.info("╠══════════════════════════════════════════════════════════╣");
        log.info("║  👤 Super admin   : youssef.assid                        ║");
        log.info("║  🔑 Mot de passe  : Admin@EMSI2024  (à changer !)        ║");
        log.info("╚══════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("💡 Pour tester le backend, ouvrez dans votre navigateur :");
        log.info("   → http://localhost:{}/api/health", port);
        log.info("   Réponse attendue : {{\"success\":true,\"message\":\"Backend opérationnel\"}}");
        log.info("");
    }
}
