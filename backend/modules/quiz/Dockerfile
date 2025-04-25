# 빌드 스테이지
FROM gradle:7.6-jdk17 AS build

WORKDIR /workspace/app

# 프로젝트 파일 복사
COPY . .

# Gradle 명령으로 직접 빌드 (래퍼 없이)
RUN gradle :modules:quiz:clean :modules:quiz:build -x test

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /workspace/app/modules/quiz/build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8082

# 포트 노출
EXPOSE 8082

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]