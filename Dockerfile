# ---- build stage: compila com Maven (nao precisa de Maven instalado) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa dependencias primeiro (cache de camada) e depois compila.
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

# ---- run stage: imagem enxuta so com o JRE ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/lista-smart-api-1.0.0.jar app.jar

# O Render injeta a porta via env PORT; o app le server.port=${PORT:8080}.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
