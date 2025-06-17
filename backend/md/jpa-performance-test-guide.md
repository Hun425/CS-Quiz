# JPA 성능 테스트 및 측정 가이드

## 개요

이 문서는 JPA N+1 문제 및 성능 최적화를 위한 테스트 및 측정 환경에 대한 가이드입니다. 최적화 전후의 성능 차이를 체계적으로 비교 분석하고 문서화하는 방법을 설명합니다.

## 설정된 환경 구성 요소

1. **성능 테스트용 프로필**
   - `application-performance-test.yml`: 성능 측정에 최적화된 설정 (통계 수집, SQL 로깅 활성화)

2. **성능 측정 유틸리티**
   - `PerformanceTestUtil.java`: 다양한 성능 지표 측정 및 비교 분석 기능 제공

3. **SQL 모니터링 도구**
   - P6Spy: SQL 쿼리 로깅 및 실행 시간 측정
   - 설정 파일: `spy.properties`와 `CustomLineFormat.java`

4. **테스트 클래스**
   - `QuizServicePerformanceTest.java`: 퀴즈 관련 기능 성능 테스트
   - `UserServicePerformanceTest.java`: 사용자 관련 기능 성능 테스트

5. **결과 문서화 템플릿**
   - `jpa-performance-comparison.md`: 성능 비교 결과를 기록하는 마크다운 템플릿

## 성능 테스트 실행 방법

### 1. 테스트 환경 구성

- Gradle 의존성이 포함된 것을 확인합니다 (P6Spy).
- `application-performance-test.yml` 파일에서 필요에 따라 설정을 조정합니다.

### 2. 최적화 전 코드 테스트

1. JUnit 테스트를 실행합니다:
   ```
   ./gradlew test --tests "com.quizplatform.core.performance.*Before"
   ```
   
2. 또는 IDE에서 개별 테스트 메서드를 실행합니다:
   - `testSearchQuizzesBefore()`
   - `testGetQuizDetailBefore()`
   - `testGetUserProfileBefore()`

3. 로그에서 출력된 성능 지표를 확인합니다.

### 3. 코드 최적화 적용

문서에서 제안된 최적화 방법을 적용합니다:

1. **N+1 문제 해결**
   - Fetch Join / EntityGraph 적용
   - Batch Size 설정
   - DTO Projection 구현

2. **기타 성능 최적화**
   - 읽기 전용 트랜잭션 적용
   - 쿼리 최적화
   - 캐싱 적용 (필요 시)

### 4. 최적화 후 테스트 실행

1. JUnit 테스트를 실행합니다:
   ```
   ./gradlew test --tests "com.quizplatform.core.performance.*After"
   ```
   
2. 또는 IDE에서 개별 테스트 메서드를 실행합니다:
   - `testSearchQuizzesAfter()`
   - `testGetQuizDetailAfter()`
   - `testGetUserProfileAfter()`

3. 성능 비교 테스트를 실행합니다:
   ```
   ./gradlew test --tests "com.quizplatform.core.performance.*comparePerformance"
   ```

### 5. 결과 분석 및 문서화

1. `PerformanceTestUtil`이 출력한 로그에서 성능 지표를 수집합니다.
2. `jpa-performance-comparison.md` 템플릿을 사용하여 결과를 문서화합니다.
3. 각 API별로 최적화 전후의 성능 지표와 개선 비율을 기록합니다.
4. 효과적이었던 최적화 기법과 그 효과를 분석합니다.

## 주요 성능 지표

측정 및 비교 분석에 사용되는 주요 성능 지표:

1. **쿼리 실행 횟수**: N+1 문제 해결 여부 확인
2. **실행 시간**: API 응답 시간
3. **SQL 실행 시간**: 데이터베이스 쿼리 처리 시간
4. **메모리 사용량**: 메모리 효율성
5. **엔티티 로드 수**: 영속성 컨텍스트에 로드된 엔티티 수
6. **컬렉션 로드 수**: 지연 로딩된 컬렉션 수

## 팁과 주의사항

1. **반복 테스트의 중요성**
   - 단일 실행은 JVM 웜업, 캐싱 등의 영향으로 편차가 클 수 있으므로 여러 번 실행하여 평균값을 사용합니다.

2. **외부 요인 제거**
   - 테스트 실행 시 다른 애플리케이션의 영향을 최소화합니다.
   - 일관된 환경에서 테스트를 실행합니다.

3. **실제 데이터와 유사한 테스트 데이터**
   - 실제 운영 환경의 데이터 패턴과 볼륨을 반영한 테스트 데이터를 사용합니다.
   - `TestDataInitializer` 클래스를 활용하여 적절한 테스트 데이터를 생성합니다.

4. **점진적 최적화**
   - 한 번에 모든 최적화를 적용하지 말고, 단계적으로 적용하며 각 기법의 효과를 측정합니다.
   - 각 최적화 기법별로 별도의 비교 테스트를 구성하면 더 정확한 분석이 가능합니다.

## 참고 자료

- Spring Data JPA 공식 문서: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- Hibernate 성능 최적화 가이드: https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#best-practices
- P6Spy 공식 문서: https://p6spy.readthedocs.io/en/latest/
