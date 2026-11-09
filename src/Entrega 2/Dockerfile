# Build único para o Railway: compila o frontend (React) e o embute no backend (Spring Boot).
# O sistema inteiro fica em um só endereço: telas em /, API em /api, Swagger em /swagger-ui/index.html.

FROM node:24-alpine AS frontend
WORKDIR /front
COPY Frontend/package.json Frontend/pnpm-lock.yaml Frontend/pnpm-workspace.yaml ./
RUN npx -y pnpm@11.19.0 install --frozen-lockfile
COPY Frontend/ ./
ENV VITE_API_URL=/api
RUN npx -y pnpm@11.19.0 build

FROM eclipse-temurin:17-jdk AS backend
WORKDIR /app
COPY Backend/.mvn .mvn
COPY Backend/mvnw Backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY Backend/src src
COPY --from=frontend /front/dist src/main/resources/static
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend --chown=10001:10001 /app/target/kfka-1.0.0.jar /app/kfka.jar
USER 10001:10001
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/kfka.jar"]
