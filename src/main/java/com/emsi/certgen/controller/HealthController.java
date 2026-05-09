package com.emsi.certgen.controller;

import com.emsi.certgen.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HealthController — Endpoint de vérification du serveur
 * Accessible sans authentification : GET /api/health
 * Utilisé pour :
 *   - Vérifier que Spring Boot est démarré
 *   - UptimeRobot (monitoring)
 *   - Render (health check Docker)
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(
            ApiResponse.ok("Backend opérationnel ✅",
                Map.of(
                    "status",    "ok",
                    "service",   "EMSI Certificate Generator",
                    "timestamp", LocalDateTime.now().toString()
                )
            )
        );
    }
}
