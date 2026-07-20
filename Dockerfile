# Etapa 1: Construir frontend (React + Vite)
FROM node:18-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Etapa 2: Construir backend (Spring Boot + Maven)
FROM maven:3.9-eclipse-temurin-17-alpine AS backend
WORKDIR /app/backend
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src/ ./src/
COPY --from=frontend /app/frontend/dist/ ./src/main/resources/static/
RUN mvn clean package -DskipTests -B

# Etapa 3: Imagen de producción
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /app/data /app/uploads /app/respaldos
RUN chown -R appuser:appgroup /app
WORKDIR /app
COPY --from=backend /app/backend/target/restaurante-backend-1.0.0.jar /app/app.jar
EXPOSE 8080
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
