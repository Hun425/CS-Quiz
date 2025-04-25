# 빌드 스테이지
FROM gradle:7.6-jdk17 AS build

WORKDIR /workspace/app

# 프로젝트 파일 복사
COPY . .

# Gradle 명령으로 직접 빌드 (래퍼 없이)
RUN gradle :modules:eureka-server:clean :modules:eureka-server:build -x test

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=build /workspace/app/modules/eureka-server/build/libs/*.jar app.jar

EXPOSE 8761 5761

ENTRYPOINT ["java", "-jar", "app.jar"]