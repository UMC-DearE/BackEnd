# ===================================
# DearE Backend - Dockerfile (Multi-stage)
# - 컨테이너 내부에서 Gradle 빌드(bootJar) 수행
# - 로컬/CI 환경 불일치 방지
# - .dockerignore에 build/가 있어도 문제 없음

# 1) Build stage
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Gradle wrapper / 설정 파일 먼저 복사 (캐시 효율)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여 (Linux 컨테이너)
RUN chmod +x gradlew

# 소스 복사
COPY src src

# bootJar 생성 (테스트는 CI에서 별도 수행 권장)
RUN ./gradlew clean bootJar -x test


# 2) Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# builder 스테이지에서 만들어진 jar만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 내부 포트는 8080 고정
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
