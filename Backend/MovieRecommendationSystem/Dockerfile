# Dockerfile backend
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Copiar tudo (pom.xml, src, etc.)
COPY . .

# Construir o projeto sem correr testes
RUN mvn clean install -DskipTests

# Expor a porta do backend
EXPOSE 8080

# Comando para arrancar a app
CMD ["java", "-jar", "target/MovieRecommendationSystem-0.0.1-SNAPSHOT.jar"]