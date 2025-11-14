# ------------------------
# 1단계: 빌드 스테이지
# ------------------------
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY . .

# 캐시 최적화 (dependencies 먼저 다운)
RUN gradle dependencies --no-daemon || true

# 빌드
RUN gradle bootJar --no-daemon

# ------------------------
# 2단계: 런타임 스테이지
# ------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드 된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경변수로 profile 설정
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
