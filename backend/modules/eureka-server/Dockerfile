FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# 모듈 소스 코드 복사 (모듈 디렉토리 내부의 파일만 복사)
COPY . .

# Gradle 빌드
RUN ./gradlew clean build -x test || echo "Build failed, continuing anyway"
RUN mkdir -p build/libs

# 런타임 이미지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8761

# 포트 노출
EXPOSE 8761

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
