FROM eclipse-temurin:17-jdk as build

WORKDIR /workspace/app

# Gradle Wrapper와 build 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY modules/quiz/build.gradle modules/quiz/
COPY modules/user/build.gradle modules/user/
COPY modules/tag/build.gradle modules/tag/

# 권한 설정
RUN chmod +x gradlew

# 의존성 레이어 캐싱을 위한 초기 빌드
RUN ./gradlew :modules:quiz:dependencies

# 소스 코드 복사
COPY core core
COPY modules/quiz/src modules/quiz/src
COPY modules/user/src modules/user/src
COPY modules/tag/src modules/tag/src

# 애플리케이션 빌드
RUN ./gradlew :modules:quiz:bootJar -x test

# 실행 이미지 생성
FROM eclipse-temurin:17-jre

# 타임존 설정
ENV TZ=Asia/Seoul

WORKDIR /app

# 빌드된 jar 파일 복사
COPY --from=build /workspace/app/modules/quiz/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8082

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app/app.jar"]