# CS-Quiz 프로젝트 기술 스택 상세 문서

## 1. 아키텍처 개요

### 모듈형 마이크로서비스 아키텍처 (MMA)
- **구조**: 독립적인 모듈(user, quiz, battle, common 등)로 구성
- **장점**: 기능별 모듈화로 개발 및 유지보수 용이
- **구현 방식**: 각 모듈은 자체 도메인 로직과 데이터베이스 스키마 보유

### 레이어드 아키텍처
- **계층 구조**:
  - Presentation Layer: API 엔드포인트, 컨트롤러
  - Application Layer: 서비스 조정, 유스케이스 구현
  - Domain Layer: 핵심 비즈니스 로직, 모델, 서비스
  - Infrastructure Layer: 외부 시스템 연동, 기술적 구현

## 2. 핵심 기술 스택

### Spring Boot 3.2.2
- **핵심 기능**: 자동 구성, 내장 서버, 의존성 관리
- **활용 방식**: 각 모듈별 독립 애플리케이션으로 구성
- **설정 관리**: application.yml을 통한 프로퍼티 관리
- **심화 학습 자료**: [공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)

### Spring Cloud
- **Eureka Server (서비스 디스커버리)**
  - **역할**: 서비스 등록 및 발견 기능 제공
  - **구현 방식**: `@EnableDiscoveryClient` 어노테이션으로 클라이언트 등록
  - **작동 원리**: 서비스 인스턴스가 시작할 때 Eureka 서버에 자신을 등록, 다른 서비스에서 호출 가능
  - **심화 학습**: [Netflix Eureka 아키텍처](https://cloud.spring.io/spring-cloud-netflix/reference/html/)

### Apache Kafka
- **용도**: 모듈 간 비동기 이벤트 통신
- **구성 요소**:
  - **Producer**: 각 모듈의 EventPublisher 구현체
  - **Consumer**: 이벤트 리스너를 통한 메시지 처리
  - **Topics**: `user.created`, `user.level-up` 등 도메인 이벤트 토픽
- **설정**: KafkaCommonConfig, UserKafkaConfig 등에서 관리
- **심화 학습**: [Kafka 공식 문서](https://kafka.apache.org/documentation/)

### Spring Data JPA
- **기능**: 객체-관계 매핑(ORM), 리포지토리 추상화
- **주요 컴포넌트**:
  - **엔티티**: `@Entity` 어노테이션으로 정의된 도메인 모델
  - **리포지토리**: `JpaRepository` 인터페이스 확장
  - **쿼리 메소드**: 메소드 이름으로 쿼리 자동 생성
- **심화 학습**: [Spring Data JPA 레퍼런스](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### Lombok
- **용도**: 반복 코드 감소, 가독성 향상
- **주요 어노테이션**:
  - `@Getter`, `@Setter`: 접근자/변경자 자동 생성
  - `@Builder`: 빌더 패턴 구현
  - `@RequiredArgsConstructor`: 생성자 자동 생성
- **심화 학습**: [Lombok 프로젝트](https://projectlombok.org/features/)

## 3. 디자인 패턴 및 원칙

### 도메인 주도 설계 (DDD)
- **핵심 개념**:
  - **엔티티**: ID로 식별되는 도메인 객체 (User, Quiz 등)
  - **값 객체**: 속성으로만 식별되는 객체
  - **집합체(Aggregate)**: 일관성 경계를 형성하는 객체 그룹
  - **리포지토리**: 도메인 객체의 저장 및 검색 담당
- **적용 사례**: 각 모듈의 도메인 모델 설계, 리포지토리 구현
- **심화 학습**: [DDD 참고서](https://domainlanguage.com/ddd/)

### 이벤트 주도 아키텍처 (EDA)
- **구현 방식**:
  - **이벤트 발행**: EventPublisher를 통한 도메인 이벤트 발행
  - **이벤트 소비**: 리스너를 통한 비동기 처리
- **장점**: 느슨한 결합, 확장성, 비동기 처리
- **적용 사례**: UserCreatedEvent, UserLevelUpEvent 등
- **심화 학습**: [이벤트 주도 마이크로서비스](https://www.confluent.io/blog/building-a-microservices-architecture-with-kafka-streams-and-ksql/)

### SOLID 원칙
- **단일 책임 원칙 (SRP)**: 각 클래스는 하나의 책임만 가짐
- **개방-폐쇄 원칙 (OCP)**: 확장에 열려있고, 수정에 닫혀있음
- **리스코프 치환 원칙 (LSP)**: 상속 관계에서 하위 타입은 상위 타입을 대체할 수 있어야 함
- **인터페이스 분리 원칙 (ISP)**: 클라이언트는 사용하지 않는 인터페이스에 의존해서는 안 됨
- **의존성 역전 원칙 (DIP)**: 추상화에 의존하고, 구체적인 것에 의존하지 않음
- **적용 사례**: 인터페이스 기반 설계 (EventPublisher 등)

### 기타 디자인 패턴
- **빌더 패턴**: 복잡한 객체 생성 (Lombok @Builder)
- **팩토리 패턴**: 객체 생성 로직 캡슐화
- **전략 패턴**: 알고리즘을 인터페이스로 추상화
- **옵저버 패턴**: 이벤트 기반 통신에서 활용

## 4. 모듈별 분석

### Common 모듈
- **역할**: 공통 기능 제공, 다른 모듈에서 재사용
- **주요 컴포넌트**: 
  - EventPublisher 인터페이스
  - Kafka 설정
  - 공통 유틸리티 클래스

### User 모듈
- **기능**: 사용자 관리, 인증, 권한 처리
- **주요 컴포넌트**:
  - User 엔티티
  - UserService, UserRepository
  - UserEventPublisher

### Quiz 모듈
- **기능**: 퀴즈 관리, 문제 출제, 결과 처리
- **주요 컴포넌트**:
  - Quiz, Question, QuizAttempt 엔티티
  - QuizService, QuestionService
  - QuizRepository, QuestionRepository

### Battle 모듈
- **기능**: 실시간 퀴즈 대결 기능
- **주요 컴포넌트**:
  - Battle, BattleParticipant 엔티티
  - 실시간 처리 로직

## 5. 심화 학습 자료

### 마이크로서비스 아키텍처
- [Building Microservices](https://samnewman.io/books/building_microservices/)
- [Microservices Patterns](https://microservices.io/patterns/index.html)

### 스프링 에코시스템
- [Spring Framework Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Baeldung Spring 튜토리얼](https://www.baeldung.com/spring-tutorial)

### 이벤트 기반 아키텍처
- [Designing Event-Driven Systems](https://www.confluent.io/designing-event-driven-systems/)
- [Event Sourcing Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/event-sourcing)

### 도메인 주도 설계
- [Domain-Driven Design Reference](https://domainlanguage.com/ddd/reference/)
- [Implementing Domain-Driven Design](https://vaughnvernon.co/?page_id=168)

### 클라우드 네이티브 아키텍처
- [12-Factor App](https://12factor.net/ko/)
- [Cloud Native Computing Foundation](https://www.cncf.io/projects/)

이 문서는 계속 확장하고 업데이트할 수 있으며, 각 기술에 대한 더 깊은 이해를 위해 제공된 자료를 참고하세요. 