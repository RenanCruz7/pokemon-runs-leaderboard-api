# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o arquivo pom.xml e o código fonte
COPY pom.xml .
COPY src src

# Executa o build da aplicação usando o Maven instalado na imagem
RUN mvn -B clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR gerado da stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponhe a porta da aplicação
EXPOSE 8080

# Define a variável de ambiente para conectar ao banco postgres
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/leaderboard_db

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]