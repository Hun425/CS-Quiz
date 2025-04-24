FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# 루트 디렉토리의 gradlew 파일과 필요한 gradle 디렉토리 모두 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY modules modules

# Gradle 빌드
RUN chmod +x ./gradlew
RUN ./gradlew :modules:eureka-server:clean :modules:eureka-server:build -x test

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# JAR 파일 복사
COPY --from=build /workspace/app/modules/eureka-server/build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=docker
# ENV SERVER_PORT=8082 # application.yml 에 이미 있으므로 중복 설정 불필요

# 포트 노출
EXPOSE 8761

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]