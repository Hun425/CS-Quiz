FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# 소스 코드 복사
COPY . .

# Gradle 빌드
RUN ./gradlew build -x test

# 실행 환경
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/target/*.jar app.jar

# 헬스체크를 위한 curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1