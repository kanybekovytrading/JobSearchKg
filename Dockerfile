# Многоступенчатая сборка

# Стадия 1: Сборка
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Стадия 2: Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем JAR
COPY --from=build /app/target/*.jar app.jar

# Expose порт
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]