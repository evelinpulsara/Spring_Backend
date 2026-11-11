# Backend Spring Boot para Render (o cualquier servicio con Docker).
# 1) Compila el proyecto
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

# 2) Imagen liviana solo para ejecutar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/spring-backend-*.jar app.jar
# Usa como máximo el 75% de la memoria (el plan gratis de Render tiene 512 MB).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
