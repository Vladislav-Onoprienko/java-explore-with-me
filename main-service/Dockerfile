FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY main-service/target/main-service-0.0.1-SNAPSHOT.jar app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-jar", "app.jar"]