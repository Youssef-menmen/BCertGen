# ═══════════════════════════════════════════════════════
#  Dockerfile — EMSI Certificate Generator Backend
#  Compatible Railway + Render + tout cloud Docker
# ═══════════════════════════════════════════════════════

# ── Étape 1 : Build Maven ──────────────────────────────
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Build
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ── Étape 2 : Image finale légère ─────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copier le JAR compilé
COPY --from=build /app/target/*.jar app.jar

# Railway injecte PORT automatiquement
EXPOSE 8080

# Démarrage avec profil prod
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-Dserver.port=${PORT:-8080}", \
  "-jar", "app.jar"]
