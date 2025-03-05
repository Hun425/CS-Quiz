FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 소스 코드 복사
COPY . .

# Gradle 빌드
RUN gradle build -x test

# 실행 환경
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1