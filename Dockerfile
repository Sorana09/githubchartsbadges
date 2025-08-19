FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY backend/pom.xml backend/
COPY github-api/pom.xml github-api/
COPY badgeLibrary/pom.xml badgeLibrary/
COPY chartLibrary/pom.xml chartLibrary/
RUN mvn dependency:go-offline -B
COPY . .
RUN mvn clean package -pl backend -am -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/backend/target/backend-*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "app.jar"]
