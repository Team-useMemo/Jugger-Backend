# backend/Dockerfile

FROM openjdk:17-jdk-slim

WORKDIR /app

# jar 복사 (GitHub Actions에서 미리 build된 jar)
COPY build/libs/*.jar app.jar

# 환경변수로 application.properties 안 쓰고도 커버 가능
CMD ["java", "-jar", "app.jar"]
