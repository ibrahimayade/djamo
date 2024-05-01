
# Utilise l'image JDK Alpine comme base
FROM openjdk:17-alpine

# Copie du fichier JAR de l'application Spring Boot dans le conteneur
COPY target/backend-0.0.1-SNAPSHOT.jar backend.jar

# Exécution de l'application Spring Boot
CMD ["java", "-jar", "backend.jar"]

