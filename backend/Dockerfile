FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle 의존성 파일들을 먼저 복사 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 다운로드 (소스 코드 변경 시에도 캐시됨)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# Gradle 빌드 (의존성은 이미 캐시되어 있음)
RUN gradle build -x test --no-daemon

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