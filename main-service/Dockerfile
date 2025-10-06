FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/main-service-*.jar
COPY ${JAR_FILE} app.jar
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-jar", "app.jar"]