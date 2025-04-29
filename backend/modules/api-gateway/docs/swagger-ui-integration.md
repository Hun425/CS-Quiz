# Swagger UI 통합 문제 해결

## 발생한 문제
- API Gateway에서 Swagger UI 리소스에 접근할 때 403 Forbidden 오류 발생
- SecurityContext를 찾지 못해 `/webjars/swagger-ui/index.html` 등의 경로가 차단됨

## 해결 방법

### 1. SecurityConfig 변경
- Spring Security 설정에서 Swagger UI 관련 리소스를 명시적으로 허용
- NoOpServerSecurityContextRepository 사용하여 무상태 보안 컨텍스트 처리
- 경로 패턴을 단순화하여 일관된 접근 허용

### 2. JwtAuthenticationFilter 개선
- 필터에 높은 우선순위(Order = -100) 부여
- Swagger 리소스에 대한 확인 로직 개선
- 코드 가독성과 유지보수성 향상을 위한 메서드 분리

### 3. 회복성 추가
- Redis 장애 발생 시에도 정상 작동하도록 설정 추가
- Resilience4j Circuit Breaker 활성화

## 설정 파일 변경
- application.yml 및 application-docker.yml에 회복성 관련 설정 추가
- Bean 정의 충돌 방지를 위한 설정 추가

## 테스트 방법
- Docker Compose로 빌드 및 실행: `docker-compose -f dev.yml build --no-cache && docker-compose -f dev.yml up`
- 접근 URL: http://localhost:8080/swagger-ui.html 또는 http://localhost:8080/webjars/swagger-ui/index.html
