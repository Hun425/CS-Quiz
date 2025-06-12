# CS-Quiz Legacy to MSA Migration Guide

## 📊 Current Migration Status: 56% Complete

이 문서는 Legacy 모노리식 코드를 MSA 이벤트 기반 아키텍처로 전환하는 마이그레이션 가이드입니다.

## ⚠️ 필수 개발 가이드라인

### 📋 개발 진행 규칙
1. **MSA_DEVELOPMENT_GUIDE.md 참조 필수** - 모든 개발은 이 가이드를 따라야 함 (지키지 않으면 삭제)
2. **빌드/테스트 금지** - `./gradlew build` 등 빌드 테스트 명령어 실행 금지
3. **CLAUDE.md 업데이트 필수** - 모든 작업 완료 시 진행 상황을 이 문서에 업데이트
4. **Todo 관리** - TodoWrite/TodoRead 도구를 사용하여 작업 진행 상황 추적

### 🔧 코드 작성 규칙
1. **JavaDoc 주석 필수**
   ```java
   /**
    * [클래스/메서드 설명]
    * 
    * <p>[상세 설명]</p>
    *
    * @author 채기훈
    * @since JDK 21.0.6 Eclipse Temurin
    */
   ```
2. **헥사고날 아키텍처 패턴** - adapter/application/domain/infrastructure 구조
3. **이벤트 기반 통신** - 모듈 간 Kafka 이벤트 사용
4. **Spring Boot 표준** - @Service, @RestController, @Repository 등 표준 어노테이션
5. **람다식 적극 활용** ⭐
   - Stream API, Optional 등을 적극 활용하여 함수형 프로그래밍 지향
   - 람다에서 지역변수 수정이 필요한 경우 AtomicInteger, 배열, 컬렉션 등 활용
   ```java
   // ✅ 권장: 람다와 Stream API 활용
   AtomicInteger earnedPoints = new AtomicInteger(0);
   questionAttempts.stream()
       .filter(attempt -> attempt.getQuestionId().equals(question.getId()))
       .filter(QuestionAttempt::isCorrect)
       .findFirst()
       .ifPresent(attempt -> earnedPoints.addAndGet(questionPoints));
   
   // ❌ 최후 수단: 전통적 for 루프 (람다 사용 불가능한 경우만)
   for (QuestionAttempt attempt : questionAttempts) { ... }
   ```
---

## 🎯 Overall Migration Progress

### Legend
- ✅ **완료**: 기능이 완전히 구현됨
- ⚠️ **부분 완료**: 기본 구조는 있으나 일부 기능 누락
- ❌ **미구현**: 아직 구현되지 않음
- 🔄 **진행중**: 현재 작업 중

---

## 🏗️ MSA Modules Status

### 1. User Module (60% Complete)
**위치**: `modules/user/`

#### ✅ 구현 완료
- 기본 사용자 CRUD 작업
- 이벤트 기반 아키텍처 (Kafka)
- 사용자 생성/업데이트 이벤트
- 기본 도메인 모델 (User, UserLevel, UserBattleStats)
- RESTful API 엔드포인트
- Swagger 문서화

#### ⚠️ 부분 구현
- 사용자 레벨 시스템 (도메인 모델만 존재, 로직 부족)
- 배틀 통계 (모델만 존재)

#### ✅ 구현 완료 (추가)
- **LOCAL 인증 지원** (이메일/비밀번호 로그인)
- **회원가입 API** (/auth/register)
- **로그인 인증 API** (/auth/login)
- **사용자 정보 조회 API** (/auth/user/{userId})
- **비밀번호 암호화** (BCrypt)

#### ✅ 구현 완료 (추가)
- **OAuth2 소셜 로그인** (Google, GitHub, Kakao)
  - OAuth2ClientService: Google, GitHub, Kakao 사용자 정보 조회
  - OAuth2 콜백 엔드포인트: /api/auth/oauth2/callback
  - User Module OAuth2 사용자 처리 (조회/생성)
  - OAuth2 설정 파일 업데이트

#### ❌ 미구현
- **성취 시스템** (Achievement, UserAchievementHistory)
- **사용자 통계 및 분석**
- **사용자 레벨링 로직 및 경험치 시스템**
- **사용자 프로필 상세 정보**
- **최근 활동 추적**
- **주제별 성과 분석**

### 2. Quiz Module (65% Complete)
**위치**: `modules/quiz/`

#### ✅ 구현 완료
- 기본 퀴즈 CRUD 작업
- 기본 도메인 모델 (Quiz, Question, QuestionOption)
- 이벤트 기반 아키텍처
- RESTful API 엔드포인트
- Swagger 문서화

#### ⚠️ 부분 구현
- 퀴즈 생성/업데이트 기능 (기본 구조만)
- 사용자 캐싱 서비스 (LocalUserCacheService)

#### ✅ 구현 완료 (추가)
- **퀴즈 시도 및 채점 시스템** (QuizAttempt, 점수 계산)
  - QuizAttempt 엔티티 및 도메인 모델 (점수 계산, 통과 기준 로직)
  - QuizAttemptService: 퀴즈 시작/제출/완료 처리
  - QuizAttemptController: REST API 엔드포인트 제공
  - 점수 계산 알고리즘: 배점 기반 백분율 계산
  - QuizResultProcessor: 퀴즈 완료 후 통계/이벤트 처리
  - 이벤트 기반 통신: QuizCompletedEvent 발행으로 User Service 연동

#### ❌ 미구현
- **일일 퀴즈 시스템**
- **퀴즈 검색 및 필터링**
- **퀴즈 리뷰 시스템** (QuizReview, QuizReviewComment)
- **퀴즈 통계** (QuizStatistics)
- **난이도 기반 퀴즈 분류**
- **태그 시스템** (Tag CRUD, 계층구조, 퀴즈-태그 매핑)
- **기본 추천 기능** (고급 추천은 별도 서비스에서)

### 3. Battle Module (80% Complete) ✨
**위치**: `modules/battle/`

#### ✅ 구현 완료
- 실시간 배틀 시스템 (WebSocket)
- 배틀룸 생성/참가/나가기
- 실시간 답변 처리
- 배틀 진행 상태 관리
- 이벤트 기반 아키텍처
- 완전한 도메인 모델
- 포괄적인 API 엔드포인트

#### ⚠️ 부분 구현
- 배틀 결과 영속성 (이벤트는 발행하지만 저장 로직 부족)

#### ❌ 미구현
- **배틀 통계 집계**
- **배틀 히스토리 조회**

### 4. API Gateway (30% Complete)
**위치**: `modules/api-gateway/`

#### ✅ 구현 완료
- 기본 라우팅 설정
- Swagger UI 통합
- 서비스 디스커버리 (Eureka)
- 기본 웹 클라이언트 설정

#### ⚠️ 부분 구현
- JWT 토큰 검증 (기본 구조만)

#### ✅ 구현 완료 (추가)
- **JWT 토큰 생성/발급 기능**
- **기본 로그인 API** (/api/auth/login)
- **토큰 갱신 API** (/api/auth/refresh)
- **액세스/리프레시 토큰 분리**
- **User Service 연동**

#### ⚠️ 부분 구현
- JWT 토큰 검증 (액세스 토큰만 검증하도록 개선)

#### ✅ 구현 완료 (추가)
- **OAuth2 소셜 로그인 통합**
  - OAuth2 콜백 엔드포인트 추가 (/api/auth/oauth2/callback)
  - AuthService에 oauth2Login 메서드 추가
  - UserServiceClient에 processOAuth2User 메서드 추가

#### ❌ 미구현
- **사용자 세션 관리**
- **보안 필터 체인 완성**
- **요청 검증 및 변환**
- **로깅 및 모니터링**

### 5. Common Module (70% Complete)
**위치**: `modules/common/`

#### ✅ 구현 완료
- 이벤트 발행 인프라 (Kafka)
- 도메인 이벤트 정의
- 공통 예외 처리
- 사용자 인증 관련 공통 컴포넌트
- 기본 웹 설정

#### ⚠️ 부분 구현
- 사용자 컨텍스트 리졸버

#### ❌ 미구현
- **캐싱 인프라** (Redis/Caffeine)
- **성능 모니터링**
- **보안 유틸리티**

---

## 🚀 새로 생성해야 할 마이크로서비스

### 1. Analytics & Recommendation Service ❌
**우선순위**: 중간 (Quiz Module 핵심 기능 완성 후)

#### 구현해야 할 기능
**Analytics 기능:**
- 사용자 활동 추적
- 퀴즈 통계 집계
- 성과 분석
- 대시보드 데이터 제공

**Recommendation 기능:**
- 사용자 기반 퀴즈 추천
- 난이도 기반 추천
- 태그 기반 추천
- 머신러닝 알고리즘 통합

#### 통합 이유
- 추천 시스템은 분석 데이터에 강하게 의존
- 초기 단계에서는 하나의 서비스로 시작하여 복잡성 감소
- 향후 규모 확장시 필요에 따라 분리 가능

## 🔄 기존 모듈에 통합할 기능

### Quiz Module에 추가할 기능 ⚠️
**Tag 시스템 (기존 Tag Service 계획을 Quiz Module로 통합):**
- 태그 CRUD 작업
- 태그 계층 구조 관리
- 퀴즈-태그 매핑
- 태그 기반 검색
- 태그 통계

#### 통합 이유
- Tag는 Quiz와 강하게 결합된 도메인
- 퀴즈 조회/생성 시 태그 정보가 항상 필요
- 네트워크 호출 오버헤드 제거
- 데이터 일관성 보장 용이

---

## 📋 Migration Roadmap

### Phase 1: 인증 및 보안 강화 (2주)
1. **API Gateway 보안 강화**
   - JWT 인증/인가 완전 구현
   - OAuth2 소셜 로그인 통합
   - 보안 필터 체인 구축

2. **User Module OAuth2 통합**
   - 소셜 로그인 프로바이더 구현
   - 사용자 프로필 동기화

### Phase 2: 핵심 비즈니스 로직 (4주)
1. **Quiz Module 핵심 기능**
   - 퀴즈 시도 및 채점 시스템
   - 일일 퀴즈 시스템
   - **태그 시스템 통합** (기존 Tag Service 계획 포함)
   - 기본 검색 기능

### Phase 3: 고급 기능 (4주)
1. **User Module 고급 기능**
   - 성취 시스템
   - 레벨링 시스템
   - 상세 통계

2. **Quiz Module 고급 기능**
   - 리뷰 시스템
   - 태그 기반 고급 검색 및 필터링
   - 퀴즈 통계

### Phase 4: 분석 및 추천 통합 서비스 (3주)
1. **Analytics & Recommendation Service 구축**
   - 사용자 활동 분석
   - 퀴즈 추천 엔진
   - 통합 대시보드

### Phase 5: 최적화 및 프로덕션 준비 (1주)
1. **성능 최적화**
2. **모니터링 및 로깅**
3. **최종 테스트**

---

## 🔧 Technical Guidelines

### 마이크로서비스 설계 원칙
- **"Monolith First" 접근법**: 강하게 결합된 기능은 하나의 서비스에서 시작
- **도메인 중심 분리**: 기술적 분리보다는 비즈니스 도메인 기준으로 분리
- **향후 분리 고려**: 필요시 언제든 분리할 수 있도록 모듈화된 구조 유지

### 서비스 통합/분리 기준
✅ **통합해야 하는 경우:**
- 강한 데이터 결합 (Tag ↔ Quiz)
- 빈번한 동기 통신 필요
- 트랜잭션 경계가 같음
- 팀 규모가 작음

❌ **분리해야 하는 경우:**
- 다른 스케일링 요구사항
- 다른 기술 스택 필요  
- 독립적인 배포 주기
- 팀이 충분히 큼

### 이벤트 기반 아키텍처
- Kafka를 통한 비동기 통신
- 각 모듈은 독립적으로 배포 가능
- 이벤트 소싱 패턴 적용

### 코딩 컨벤션
- 헥사고날 아키텍처 패턴
- Domain-Driven Design (DDD)
- CQRS 패턴 (필요시)

### 테스팅 전략
- 단위 테스트: 각 도메인 로직
- 통합 테스트: 이벤트 플로우
- E2E 테스트: API 엔드포인트

---

## 📝 Work Log

### 2025-01-06
- 초기 분석 완료
- Legacy vs MSA 기능 비교 분석
- 마이그레이션 로드맵 수립
- CLAUDE.md 가이드 생성
- **아키텍처 재검토**: Tag Service → Quiz Module 통합, Analytics + Recommendation 통합
- 과도한 마이크로서비스 분리 방지를 위한 설계 개선
- **현재 모듈 구조 상세 분석 완료**
- **MSA_DEVELOPMENT_GUIDE.md 생성**: 포괄적인 개발 가이드라인 문서 작성
  - 헥사고날 아키텍처 패턴 정리
  - 이벤트 기반 통신 가이드
  - 새 모듈 생성 체크리스트
  - 코딩 컨벤션 및 테스팅 가이드
  - **코드 주석 가이드라인 추가**: JavaDoc 표준 템플릿 및 작성 규칙 정의

### 2025-01-06 (OAuth2 소셜 로그인 구현)
- **OAuth2 소셜 로그인 완전 구현**
  - OAuth2ClientService: Google, GitHub, Kakao 사용자 정보 조회 로직
  - API Gateway OAuth2 콜백 엔드포인트 추가 (/api/auth/oauth2/callback)
  - AuthService oauth2Login 메서드 구현
  - UserServiceClient processOAuth2User 메서드 추가
  - User Module OAuth2 사용자 처리 로직 (조회/생성)
  - User 엔티티 OAuth2 관련 필드 추가 (displayName, profileImageUrl)
  - OAuth2 설정 파일 업데이트 (Google, GitHub, Kakao)

### 2025-01-06 (퀴즈 시도 및 채점 시스템 구현)
- **퀴즈 시도 및 채점 시스템 완전 구현**
  - QuizAttempt/QuestionAttempt 엔티티 도메인 로직 개선
  - 점수 계산 알고리즘: 배점 기반 백분율 계산으로 개선
  - QuizAttemptService/Impl: 퀴즈 시작, 답변 제출, 완료 처리
  - QuizAttemptController: REST API 엔드포인트 (시작/제출/조회)
  - DTO 구현: QuizAttemptRequest/Response, QuestionAttemptRequest/Response, QuizSubmitRequest
  - QuizResultProcessor: 결과 처리, 통계 업데이트, 업적 확인
  - 이벤트 기반 통신: QuizCompletedEvent/UserAchievementEvent 발행
  - ErrorCode 확장: 퀴즈 시도 관련 에러 코드 추가

### 2025-01-06 (IntelliJ 로컬 개발환경 구축)
- **Docker 없이 IntelliJ 로컬 실행 환경 구축**
  - 각 모듈별 application-local.yml 설정 파일 생성
  - PostgreSQL 로컬 설정: user_schema, quiz_schema, battle_schema 분리
  - Redis/Kafka 로컬 연동 설정
  - IntelliJ Run Configuration 가이드 작성
  - INTELLIJ_LOCAL_SETUP.md: 상세한 로컬 개발환경 설치 가이드

### 2025-01-06 (하이브리드 개발환경 구축)
- **Docker 인프라 + IntelliJ 애플리케이션 하이브리드 환경**
  - docker-compose.yml: 인프라 서비스만 Docker로 관리 (PostgreSQL, Redis, Kafka, Elasticsearch)
  - 애플리케이션 서비스: IntelliJ에서 개별 실행 (-Dspring.profiles.active=local)
  - 데이터베이스 이름 일관성 수정: `quiz_platform` 표준화
  - PostgreSQL 헬스체크 및 Battle Service URL 수정
  - Config Server 설정 오류 수정: application-local.yml에서 profiles.active 제거

### 2025-01-06 (컴파일 오류 수정)
- **MSA 의존성 문제 해결**
  - QuizAttemptController: `currentUser.getUserId()` → `currentUser.id()` 수정
  - UserServiceImpl: 공통 EventPublisher 사용, UserEventPublisher 제거
  - API Gateway: 독립적인 OAuth2UserRequest DTO 생성 (User Service 의존성 제거)
  - QuizAttempt: 람다 표현식 final 변수 오류 수정 (for-each 루프로 임시 해결)
  - Topics.java: USER_CREATED 상수 추가

### 2025-01-06 (개발 가이드라인 강화)
- **람다식 적극 활용 가이드라인 추가**
  - Stream API, Optional 등 함수형 프로그래밍 지향
  - 람다에서 지역변수 수정: AtomicInteger, 배열, 컬렉션 활용 권장
  - for 루프는 최후 수단으로만 사용

---

## 🎯 Next Actions

1. **API Gateway 보안 강화** - JWT/OAuth2 인증/인가 시스템 완성
2. **Quiz Module 핵심 기능 강화** - 시도/채점 시스템 + 태그 통합
3. **User Module OAuth2 통합** - 소셜 로그인 기능 구현

---

*이 문서는 마이그레이션 진행에 따라 지속적으로 업데이트됩니다.*