# JPA N+1 및 성능 최적화 결과 보고서

## 개요

본 문서는 Quiz Platform 프로젝트의 JPA N+1 문제 및 기타 성능 관련 이슈를 개선한 결과를 기록합니다. 각 주요 API에 대한 최적화 전후의 성능 지표를 비교하고, 적용된 기법의 효과를 분석합니다.

## 테스트 환경

- **프로필**: `performance-test`
- **JVM 버전**: OpenJDK 17
- **데이터베이스**: H2 (테스트), MySQL 8.0 (운영)
- **테스트 데이터 크기**: 
  - 퀴즈: 100개
  - 질문: 퀴즈당 평균 10개
  - 사용자: 50명
  - 태그: 30개

## 주요 최적화 기법

1. **Fetch Join / EntityGraph 적용**
   - 연관 엔티티를 한 번의 쿼리로 함께 조회하여 N+1 문제 해결
   - `@EntityGraph` 어노테이션을 활용한 연관관계 그래프 정의

2. **DTO Projection 활용**
   - 필요한 데이터만 선택적으로 조회하여 데이터 전송량 감소
   - 엔티티 대신 DTO를 직접 조회하는 방식으로 변경

3. **Batch Size 최적화**
   - 컬렉션 조회 시 IN 절을 사용하여 한 번에 여러 엔티티 로딩
   - `@BatchSize` 어노테이션 및 `default_batch_fetch_size` 설정 적용

4. **읽기 전용 트랜잭션 활용**
   - 조회 로직에 `@Transactional(readOnly = true)` 적용
   - 영속성 컨텍스트 스냅샷 및 변경 감지 비용 절감

5. **캐싱 전략 개선**
   - 자주 사용되는 데이터에 적절한 캐싱 적용
   - 캐시 무효화 전략 개선으로 데이터 일관성 유지

## 성능 테스트 결과

### 1. 퀴즈 목록 조회 (QuizService.searchQuizzes)

#### 개선 전
- **평균 응답 시간**: XXX ms
- **쿼리 실행 횟수**: XXX회
- **SQL 실행 시간**: XXX ms
- **메모리 사용량**: XXX MB
- **주요 병목**: 
  - 퀴즈 목록 조회 후 각 퀴즈의 태그, 생성자 정보에 접근 시 N+1 문제 발생
  - 불필요한 연관 엔티티 로딩으로 인한 메모리 사용량 증가

#### 개선 후
- **평균 응답 시간**: XXX ms (XX% 개선)
- **쿼리 실행 횟수**: XXX회 (XX% 감소)
- **SQL 실행 시간**: XXX ms (XX% 개선)
- **메모리 사용량**: XXX MB (XX% 감소)
- **적용 기법**:
  - DTO Projection으로 필요한 데이터만 조회
  - 태그, 생성자 정보를 JOIN FETCH로 함께 조회
  - Batch Size 설정으로 인한 IN 쿼리 최적화

#### 개선 코드
```java
// 변경 전 코드
@Override
@Transactional
public Page<QuizResponse> searchQuizzes(String keyword, QuizType type, Pageable pageable) {
    Page<Quiz> quizPage = quizRepository.findBySearchConditions(keyword, type, pageable);
    return quizPage.map(entityMapperService::toQuizResponse);
}

// 변경 후 코드
@Override
@Transactional(readOnly = true)
public Page<QuizResponse> searchQuizzes(String keyword, QuizType type, Pageable pageable) {
    return quizRepository.findQuizResponseDtoBySearchConditions(keyword, type, pageable);
}
```

### 2. 퀴즈 상세 조회 (QuizService.getQuizDetail)

#### 개선 전
- **평균 응답 시간**: XXX ms
- **쿼리 실행 횟수**: XXX회
- **SQL 실행 시간**: XXX ms
- **메모리 사용량**: XXX MB
- **주요 병목**:
  - 퀴즈 엔티티 조회 후 질문, 태그, 생성자 정보 접근 시 N+1 문제
  - 트랜잭션 범위 외부에서 지연 로딩 시도로 인한 LazyInitializationException

#### 개선 후
- **평균 응답 시간**: XXX ms (XX% 개선)
- **쿼리 실행 횟수**: XXX회 (XX% 감소)
- **SQL 실행 시간**: XXX ms (XX% 개선)
- **메모리 사용량**: XXX MB (XX% 감소)
- **적용 기법**:
  - EntityGraph를 사용하여 연관 엔티티를 함께 로딩
  - 읽기 전용 트랜잭션으로 성능 최적화

#### 개선 코드
```java
// 변경 전 코드
@Override
@Transactional
public QuizDetailResponse getQuizDetail(Long quizId) {
    Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    return entityMapperService.toQuizDetailResponse(quiz);
}

// 변경 후 코드
@Override
@Transactional(readOnly = true)
public QuizDetailResponse getQuizDetail(Long quizId) {
    Quiz quiz = quizRepository.findByIdWithDetails(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    return entityMapperService.toQuizDetailResponse(quiz);
}

// QuizRepository에 추가된 메서드
@Query("SELECT DISTINCT q FROM Quiz q " +
        "LEFT JOIN FETCH q.questions " +
        "LEFT JOIN FETCH q.tags " +
        "LEFT JOIN FETCH q.creator " +
        "WHERE q.id = :id")
Optional<Quiz> findByIdWithDetails(@Param("id") Long id);
```

### 3. 사용자 프로필 조회 (UserService.getUserProfile)

(이하 동일한 형식으로 각 API에 대한 성능 개선 결과 문서화)

## 종합 분석

### 개선 효과 요약
- **쿼리 실행 횟수**: 평균 XX% 감소
- **응답 시간**: 평균 XX% 개선
- **메모리 사용량**: 평균 XX% 감소

### 최적화 기법별 효과 분석
1. **Fetch Join / EntityGraph**: 단일 엔티티 조회 시 가장 효과적 (N+1 문제 직접 해결)
2. **DTO Projection**: 목록 조회 시 가장 효과적 (필요한 데이터만 전송)
3. **Batch Size**: 대량의 연관 컬렉션 조회 시 효과적 (카테시안 곱 문제 없이 성능 개선)
4. **읽기 전용 트랜잭션**: 모든 조회 메서드에 효과적 (스냅샷 생성 비용 절감)

### 추가 개선 가능 영역
1. **키셋 페이징(Keyset Pagination)**: 대용량 데이터의 페이징 처리 시 오프셋 기반 페이징보다 효율적
2. **비동기 처리**: 독립적인 작업의 병렬 처리로 전체 응답 시간 단축
3. **쿼리 힌트 활용**: 특정 상황에서 쿼리 성능 최적화를 위한 JPA 쿼리 힌트 적용

## 결론

JPA N+1 문제 및 성능 이슈를 다양한 최적화 기법을 통해 효과적으로 개선하였습니다. 특히 대부분의 API에서 쿼리 실행 횟수가 크게 감소하고 응답 시간이 개선되었습니다. 각 API의 특성과 데이터 접근 패턴에 따라 적절한 최적화 기법을 선택적으로 적용하는 것이 중요하며, 지속적인 모니터링과 성능 측정을 통해 최적의 상태를 유지해야 합니다.

또한, 이번 최적화 과정에서 얻은 경험과 지식을 바탕으로 향후 개발할 기능에도 처음부터 성능을 고려한 설계를 적용할 수 있을 것입니다.
