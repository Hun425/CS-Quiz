# MSA Development Guidelines

CS-Quiz 프로젝트의 마이크로서비스 아키텍처 개발 가이드입니다. 새로운 모듈 생성이나 기존 모듈 확장 시 반드시 이 가이드를 따라주세요.

---

## 📋 Table of Contents

1. [아키텍처 개요](#1-아키텍처-개요)
2. [모듈 구조 표준](#2-모듈-구조-표준)
3. [이벤트 기반 통신](#3-이벤트-기반-통신)
4. [개발 컨벤션](#4-개발-컨벤션)
5. [새 모듈 생성 가이드](#5-새-모듈-생성-가이드)
6. [테스팅 가이드](#6-테스팅-가이드)
7. [배포 및 설정](#7-배포-및-설정)
8. [문제해결 가이드](#8-문제해결-가이드)

---

## ⚠️ 중요 개발 환경 유의사항

### 🔧 개발 환경 설정

**모든 개발과 테스트는 `docker-compose.dev.yml`을 기준으로 진행합니다.**

```bash
# 개발 환경 시작
docker-compose -f docker-compose.dev.yml up -d

# 특정 서비스만 재시작
docker-compose -f docker-compose.dev.yml restart user-service

# 로그 확인
docker-compose -f docker-compose.dev.yml logs -f user-service
```

### 📋 개발 환경 주요 설정

| 설정 | 값 | 설명 |
|------|----|----- |
| **Spring Profile** | `docker` | 모든 서비스는 `application-docker.yml` 설정 사용 |
| **Database** | PostgreSQL (Schema 분리) | `quiz_platform` DB 내 각 모듈별 스키마 |
| **서비스 포트** | 808x (디버깅: 508x) | API Gateway(8080), User(8081), Quiz(8082), Battle(8083) |
| **Kafka** | `kafka:29092` | 내부 컨테이너 통신용 |
| **Eureka** | `eureka-server:8761` | 서비스 디스커버리 |
| **Redis** | `redis:6379` | 캐시 및 세션 (Battle 서비스) |
| **Elasticsearch** | `elasticsearch:9200` | 검색 엔진 (Quiz 서비스) |

### 🐛 디버깅 포트

각 서비스마다 디버깅 포트가 설정되어 있습니다:

| 서비스 | 애플리케이션 포트 | 디버깅 포트 |
|--------|------------------|-------------|
| API Gateway | 8080 | 5080 |
| User Service | 8081 | 5081 |
| Quiz Service | 8082 | 5082 |
| Battle Service | 8083 | 5083 |
| Eureka Server | 8761 | 5761 |
| Config Server | 8888 | 5888 |

### 📂 볼륨 마운팅

개발 중 코드 변경사항이 즉시 반영되도록 각 모듈 폴더가 마운팅됩니다:

```yaml
volumes:
  - ./modules/[module-name]:/workspace/app
  - maven-repo:/root/.m2
```

---

## 1. 아키텍처 개요

### 1.1 전체 시스템 구조

``` mermaid
graph TB
    Client[Frontend Client] --> Gateway[API Gateway]
    Gateway --> User[User Service]
    Gateway --> Quiz[Quiz Service]
    Gateway --> Battle[Battle Service]
    
    User -.->|Events| Kafka[Apache Kafka]
    Quiz -.->|Events| Kafka
    Battle -.->|Events| Kafka
    
    Kafka -.->|Events| User
    Kafka -.->|Events| Quiz
    Kafka -.->|Events| Battle
    
    User --> DB1[(PostgreSQL)]
    Quiz --> DB2[(PostgreSQL)]
    Battle --> DB3[(PostgreSQL)]
    Battle --> Cache[(Redis)]
    
    Eureka[Service Registry] -.-> Gateway
    Config[Config Server] -.-> User
    Config -.-> Quiz
    Config -.-> Battle
```

### 1.2 핵심 원칙

- **헥사고날 아키텍처**: 각 모듈은 포트와 어댑터 패턴을 따름
- **도메인 주도 설계 (DDD)**: 비즈니스 도메인 중심의 모듈 분리
- **이벤트 기반 통신**: 모듈 간 느슨한 결합을 위한 비동기 통신
- **단일 책임 원칙**: 각 모듈은 하나의 비즈니스 도메인에 집중

### 1.3 현재 모듈 현황

| 모듈명 | 책임 | 상태 | 핵심 기술 |
|--------|------|------|-----------|
| `api-gateway` | 라우팅, 인증, API 게이트웨이 | 🔄 개발중 | Spring Cloud Gateway, JWT |
| `user` | 사용자 관리, 인증 | 🔄 개발중 | Spring Boot, JPA, OAuth2 |
| `quiz` | 퀴즈 관리, 태그 시스템 | 🔄 개발중 | Spring Boot, JPA, Elasticsearch |
| `battle` | 실시간 배틀 시스템 | ✅ 완료 | Spring Boot, WebSocket, Redis |
| `common` | 공통 유틸리티, 이벤트 | ✅ 완료 | Spring Boot, Kafka |
| `config-server` | 설정 관리 | ✅ 완료 | Spring Cloud Config |
| `eureka-server` | 서비스 디스커버리 | ✅ 완료 | Spring Cloud Eureka |

---

## 2. 모듈 구조 표준

### 2.1 필수 폴더 구조

```
modules/[module-name]/
├── build.gradle                         # 모듈별 빌드 설정
├── Dockerfile                          # Docker 이미지 빌드
└── src/
    ├── main/
    │   ├── java/com/quizplatform/[module]/
    │   │   ├── [Module]Application.java        # 🔸 Spring Boot 메인 클래스
    │   │   ├── adapter/                        # 🔸 어댑터 계층 (외부 통신)
    │   │   │   ├── in/                        # 인바운드 어댑터 (요청 처리)
    │   │   │   │   ├── web/                   # REST API 컨트롤러
    │   │   │   │   │   ├── [Entity]Controller.java
    │   │   │   │   │   └── dto/               # 웹 계층 DTO
    │   │   │   │   │       ├── [Entity]Request.java
    │   │   │   │   │       └── [Entity]Response.java
    │   │   │   │   └── event/                 # 이벤트 리스너
    │   │   │   │       └── [Module]EventListener.java
    │   │   │   └── out/                       # 아웃바운드 어댑터 (외부 호출)
    │   │   │       ├── persistence/           # 데이터베이스 어댑터
    │   │   │       │   ├── repository/        # JPA Repository
    │   │   │       │   └── cache/             # 캐시 구현
    │   │   │       └── event/                 # 이벤트 발행
    │   │   ├── application/                   # 🔸 애플리케이션 계층 (유스케이스)
    │   │   │   ├── dto/                       # 애플리케이션 DTO
    │   │   │   └── service/                   # 애플리케이션 서비스
    │   │   │       ├── [Module]Service.java
    │   │   │       └── [Module]ServiceImpl.java
    │   │   ├── domain/                        # 🔸 도메인 계층 (핵심 비즈니스)
    │   │   │   ├── model/                     # 도메인 엔티티
    │   │   │   │   └── [Entity].java
    │   │   │   ├── event/                     # 도메인 이벤트
    │   │   │   │   └── [Entity][Action]Event.java
    │   │   │   └── service/                   # 도메인 서비스
    │   │   │       └── [Domain]Service.java
    │   │   └── infrastructure/                # 🔸 인프라 계층 (설정, 기술적 관심사)
    │   │       └── config/                    # 설정 클래스
    │   │           ├── SwaggerConfig.java
    │   │           └── WebConfig.java
    │   └── resources/
    │       ├── application.yml                # 기본 설정
    │       ├── application-docker.yml         # Docker 환경 설정
    │       └── bootstrap.yml                  # Spring Cloud 부트스트랩
    └── test/
        └── java/com/quizplatform/[module]/
            ├── domain/                        # 도메인 테스트
            ├── application/service/           # 서비스 테스트
            └── adapter/in/web/               # 컨트롤러 테스트
```

### 2.2 핵심 클래스 템플릿

#### 2.2.1 Spring Boot 메인 클래스

``` java
package com.quizplatform.[module];

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(scanBasePackages = {
    "com.quizplatform.[module]",
    "com.quizplatform.common"
})
@EnableEurekaClient
public class [Module]Application {
    public static void main(String[] args) {
        SpringApplication.run([Module]Application.class, args);
    }
}
```

#### 2.2.2 도메인 엔티티

``` java
package com.quizplatform.[module].domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "[table_name]")
@EntityListeners(AuditingEntityListener.class)
public class [Entity] {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 기본 생성자 (JPA 필수)
    protected [Entity]() {}
    
    // 빌더 패턴 생성자
    private [Entity](Builder builder) {
        // 필드 초기화
    }
    
    // 비즈니스 메서드들
    public void businessMethod() {
        // 도메인 로직
    }
    
    // Builder 패턴
    public static class Builder {
        // 빌더 구현
        
        public [Entity] build() {
            return new [Entity](this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getter, equals, hashCode, toString
}
```

#### 2.2.3 REST 컨트롤러

``` java
package com.quizplatform.[module].adapter.in.web;

import com.quizplatform.common.auth.CurrentUser;
import com.quizplatform.common.auth.CurrentUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/[module]")
@RequiredArgsConstructor
@Tag(name = "[Module] API", description = "[Module] 관련 API")
public class [Entity]Controller {
    
    private final [Module]Service [module]Service;
    
    @PostMapping
    @Operation(summary = "[Entity] 생성", description = "새로운 [Entity]를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "[Entity] 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    public ResponseEntity<[Entity]Response> create[Entity](
            @RequestBody @Valid [Entity]CreateRequest request,
            @CurrentUser CurrentUserInfo currentUser) {
        
        [Entity]Response response = [module]Service.create[Entity](request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "[Entity] 조회", description = "ID로 [Entity]를 조회합니다.")
    public ResponseEntity<[Entity]Response> get[Entity](@PathVariable Long id) {
        [Entity]Response response = [module]Service.get[Entity](id);
        return ResponseEntity.ok(response);
    }
}
```

#### 2.2.4 애플리케이션 서비스

``` java
package com.quizplatform.[module].application.service;

import com.quizplatform.common.auth.CurrentUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class [Module]ServiceImpl implements [Module]Service {
    
    private final [Entity]Repository [entity]Repository;
    private final EventPublisher eventPublisher;
    
    @Override
    @Transactional
    public [Entity]Response create[Entity]([Entity]CreateRequest request, CurrentUserInfo currentUser) {
        // 1. 도메인 객체 생성
        [Entity] [entity] = [Entity].builder()
            .name(request.getName())
            .userId(currentUser.getUserId())
            .build();
        
        // 2. 저장
        [Entity] saved[Entity] = [entity]Repository.save([entity]);
        
        // 3. 이벤트 발행
        [Entity]CreatedEvent event = new [Entity]CreatedEvent(saved[Entity].getId(), currentUser.getUserId());
        eventPublisher.publish(event);
        
        // 4. 응답 변환
        return [Entity]Response.from(saved[Entity]);
    }
    
    @Override
    public [Entity]Response get[Entity](Long id) {
        [Entity] [entity] = [entity]Repository.findById(id)
            .orElseThrow(() -> new [Entity]NotFoundException("ID: " + id));
        
        return [Entity]Response.from([entity]);
    }
}
```

### 2.3 필수 설정 파일

#### 2.3.1 build.gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

group = 'com.quizplatform'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

ext {
    set('springCloudVersion', "2022.0.3")
}

dependencies {
    // 🔸 필수 의존성
    implementation project(':modules:common')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    
    // 🔸 문서화
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'
    
    // 🔸 유틸리티
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // 🔸 데이터베이스
    runtimeOnly 'org.postgresql:postgresql'
    
    // 🔸 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

#### 2.3.2 application.yml

```yaml
# 🔸 서버 설정
server:
  port: 0  # 동적 포트 할당 (Eureka 사용 시)

# 🔸 Spring 설정
spring:
  application:
    name: [module]-service
  
  # 🔸 데이터베이스 설정
  datasource:
    url: jdbc:postgresql://localhost:5432/[module]_db
    username: ${DB_USERNAME:quizplatform}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  # 🔸 JPA 설정
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  
  # 🔸 Kafka 설정
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: [module]-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.quizplatform.common.event"

# 🔸 Eureka 클라이언트 설정
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true

# 🔸 로깅 설정
logging:
  level:
    com.quizplatform: DEBUG
    org.springframework.kafka: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# 🔸 관리 엔드포인트
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

## 3. 이벤트 기반 통신

### 3.1 이벤트 정의 규칙

#### 3.1.1 이벤트 명명 규칙

```
토픽명: [module].[action]
클래스명: [Entity][Action]Event
예시: quiz.completed → QuizCompletedEvent
```

#### 3.1.2 이벤트 클래스 구조

```java
package com.quizplatform.common.event.[module];

import com.quizplatform.common.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class [Entity][Action]Event implements DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final Long [entity]Id;
    private final Long userId;
    
    public [Entity][Action]Event(Long [entity]Id, Long userId) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.[entity]Id = [entity]Id;
        this.userId = userId;
    }
    
    @Override
    public String getEventType() {
        return "[module].[action]";
    }
}
```

### 3.2 이벤트 발행

```java
@Service
@RequiredArgsConstructor
public class [Module]ServiceImpl {
    
    private final EventPublisher eventPublisher;
    
    @Transactional
    public void businessMethod() {
        // 비즈니스 로직 수행
        
        // 이벤트 발행
        [Entity][Action]Event event = new [Entity][Action]Event(entityId, userId);
        eventPublisher.publish(event);
    }
}
```

### 3.3 이벤트 구독

```java
package com.quizplatform.[module].adapter.in.event;

import com.quizplatform.common.event.[other-module].[Entity][Action]Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class [Module]EventListener {
    
    private final [Module]Service [module]Service;
    
    @KafkaListener(topics = "[other-module].[action]", groupId = "[module]-service")
    public void handle[Entity][Action]Event([Entity][Action]Event event) {
        log.info("Received [Entity][Action]Event: {}", event.getEventId());
        
        try {
            [module]Service.handle[Entity][Action](event);
        } catch (Exception e) {
            log.error("Error handling [Entity][Action]Event: {}", event.getEventId(), e);
            // 오류 처리 로직 (DLQ 등)
        }
    }
}
```

### 3.4 현재 정의된 이벤트 목록

| 토픽 | 이벤트 | 설명 | 발행자 | 구독자 |
|------|--------|------|--------|--------|
| `user.registered` | UserRegisteredEvent | 사용자 가입 | User Service | Quiz, Battle |
| `user.levelup` | UserLevelUpEvent | 사용자 레벨업 | User Service | Quiz |
| `quiz.completed` | QuizCompletedEvent | 퀴즈 완료 | Quiz Service | User |
| `quiz.score.calculated` | QuizScoreCalculationResultEvent | 점수 계산 완료 | Quiz Service | User |
| `battle.completed` | BattleCompletedEvent | 배틀 완료 | Battle Service | User |
| `battle.started` | BattleStartedEvent | 배틀 시작 | Battle Service | Quiz |

---

## 4. 개발 컨벤션

### 4.1 명명 규칙

#### 4.1.1 패키지명
- 소문자, 단수형 사용
- `com.quizplatform.[module].[layer].[sublayer]`

#### 4.1.2 클래스명
- PascalCase 사용
- 역할에 따른 접미사 사용

| 역할 | 접미사 | 예시 |
|------|--------|------|
| 컨트롤러 | Controller | UserController |
| 서비스 | Service, ServiceImpl | UserService, UserServiceImpl |
| 리포지토리 | Repository | UserRepository |
| DTO | Request, Response | UserCreateRequest, UserResponse |
| 이벤트 | Event | UserCreatedEvent |
| 예외 | Exception | UserNotFoundException |
| 설정 | Config | SwaggerConfig |

#### 4.1.3 메서드명
- camelCase 사용
- 동사로 시작

```java
// ✅ Good
public UserResponse createUser(UserCreateRequest request)
public Optional<User> findByEmail(String email)
public void validateUserData(User user)

// ❌ Bad
public UserResponse user(UserCreateRequest request)
public Optional<User> getByEmail(String email)
public void checkUser(User user)
```

### 4.2 코딩 스타일

#### 4.2.1 필수 어노테이션

```java
// Lombok 사용 권장
@RequiredArgsConstructor  // 생성자 주입
@Slf4j                   // 로깅
@Getter                  // Getter 메서드

// Spring 어노테이션
@Service                 // 서비스 계층
@RestController          // 컨트롤러 계층
@Repository              // 리포지토리 계층
@Component               // 일반 빈

// Validation
@Valid                   // 요청 검증
@NotNull, @NotBlank     // 필드 검증

// JPA
@Entity                  // 엔티티
@Table(name = "table_name") // 테이블명 명시
@Column(name = "column_name") // 컬럼명 명시
```

#### 4.2.2 예외 처리

```java
// 🔸 비즈니스 예외 클래스
public class [Entity]NotFoundException extends BusinessException {
    public [Entity]NotFoundException(String message) {
        super(ErrorCode.[ENTITY]_NOT_FOUND, message);
    }
}

// 🔸 ErrorCode 정의 (common 모듈)
public enum ErrorCode {
    [ENTITY]_NOT_FOUND("E001", "[Entity] not found"),
    [ENTITY]_INVALID("E002", "Invalid [Entity] data");
    
    private final String code;
    private final String message;
}

// 🔸 서비스에서 예외 사용
@Override
public UserResponse getUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User ID: " + id));
    return UserResponse.from(user);
}
```

#### 4.2.3 로깅 가이드

```java
@Slf4j
@Service
public class UserServiceImpl {
    
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());
        
        try {
            // 비즈니스 로직
            User user = userRepository.save(newUser);
            log.info("User created successfully with ID: {}", user.getId());
            return UserResponse.from(user);
            
        } catch (Exception e) {
            log.error("Failed to create user with email: {}", request.getEmail(), e);
            throw new UserCreationException("Failed to create user", e);
        }
    }
}
```

### 4.3 데이터베이스 컨벤션

#### 4.3.1 테이블 명명 규칙
- 소문자, snake_case 사용
- 복수형 사용 (users, quizzes, battle_rooms)

#### 4.3.2 컬럼 명명 규칙
- 소문자, snake_case 사용
- ID 컬럼: `id` (Long, BIGSERIAL)
- 외래키: `[table_name]_id` (user_id, quiz_id)
- 타임스탬프: `created_at`, `updated_at`

#### 4.3.3 JPA 엔티티 매핑

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Column(name = "display_name", nullable = false)
    private String displayName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

## 5. 새 모듈 생성 가이드

### 5.1 체크리스트

새로운 모듈을 생성할 때 다음 단계를 따라주세요:

- [ ] **1단계**: 모듈 폴더 구조 생성
- [ ] **2단계**: build.gradle 설정
- [ ] **3단계**: 메인 애플리케이션 클래스 생성
- [ ] **4단계**: 도메인 모델 정의
- [ ] **5단계**: 리포지토리 인터페이스 생성
- [ ] **6단계**: 서비스 계층 구현
- [ ] **7단계**: 컨트롤러 생성
- [ ] **8단계**: 설정 클래스 추가
- [ ] **9단계**: 이벤트 통합
- [ ] **10단계**: 테스트 작성
- [ ] **11단계**: 문서화

### 5.2 단계별 상세 가이드

#### 5.2.1 1단계: 모듈 폴더 구조 생성

```bash
# modules 디렉터리 하위에 새 모듈 생성
mkdir -p modules/[module-name]/src/main/java/com/quizplatform/[module]
mkdir -p modules/[module-name]/src/main/resources
mkdir -p modules/[module-name]/src/test/java/com/quizplatform/[module]

# 필수 패키지 구조 생성
cd modules/[module-name]/src/main/java/com/quizplatform/[module]
mkdir -p adapter/in/web/dto
mkdir -p adapter/in/event
mkdir -p adapter/out/persistence/repository
mkdir -p adapter/out/event
mkdir -p application/dto
mkdir -p application/service
mkdir -p domain/model
mkdir -p domain/event
mkdir -p domain/service
mkdir -p infrastructure/config
```

#### 5.2.2 2단계: settings.gradle 수정

```gradle
// backend/settings.gradle에 새 모듈 추가
include ':modules:[module-name]'
```

#### 5.2.3 3단계: 도메인 이벤트 정의

Common 모듈에 새 모듈의 이벤트 추가:

```java
// common/src/main/java/com/quizplatform/common/event/[module]/
// [Entity][Action]Event.java 생성
```

#### 5.2.4 4단계: Dockerfile 생성

```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

COPY build/libs/[module-name]-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 5.2.5 5단계: Docker Compose 설정 추가

**중요**: `docker-compose.dev.yml`에 새 서비스 추가:

```yaml
services:
  # 새 모듈 서비스 (예: analytics-service)
  [module-name]-service:
    build: 
      context: .
      dockerfile: ./modules/[module-name]/Dockerfile
    container_name: quiz-[module-name]-service-dev
    ports:
      - "808x:808x"      # 애플리케이션 포트 (8084, 8085...)
      - "508x:508x"      # 디버깅 포트 (5084, 5085...)
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE-URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/quiz_platform?currentSchema=[module]_schema
      - SPRING_DATASOURCE_USERNAME=quizuser
      - SPRING_DATASOURCE_PASSWORD=quizpass
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:508x
    volumes:
      - ./modules/[module-name]:/workspace/app
      - maven-repo:/root/.m2
    depends_on:
      - postgres
      - config-server
      - eureka-server
      - kafka
    networks:
      - quiz-network
    restart: unless-stopped
```

**포트 번호 규칙**:
- User: 8081 (디버깅: 5081)
- Quiz: 8082 (디버깅: 5082)  
- Battle: 8083 (디버깅: 5083)
- 새 모듈: 8084+ (디버깅: 5084+)

### 5.3 새 모듈 테스트

```bash
# 1. 빌드 테스트
./gradlew :modules:[module-name]:build

# 2. 단위 테스트 실행
./gradlew :modules:[module-name]:test

# 3. 개발환경 Docker 빌드 테스트
docker-compose -f docker-compose.dev.yml build [module-name]-service

# 4. 개발환경 서비스 실행 테스트
docker-compose -f docker-compose.dev.yml up -d [module-name]-service

# 5. 서비스 로그 확인
docker-compose -f docker-compose.dev.yml logs -f [module-name]-service

# 6. Eureka 등록 확인
curl http://localhost:8761/eureka/apps

# 7. 헬스 체크
curl http://localhost:808x/actuator/health
```

---

## 6. 테스팅 가이드

### 6.1 테스트 전략

| 테스트 유형 | 범위 | 도구 | 위치 |
|-------------|------|------|------|
| **단위 테스트** | 클래스/메서드 | JUnit 5, Mockito | domain, service 패키지 |
| **통합 테스트** | 여러 레이어 | Spring Boot Test | controller, repository |
| **계약 테스트** | API 명세 | Spring Cloud Contract | adapter/in/web |
| **이벤트 테스트** | 이벤트 플로우 | TestContainers (Kafka) | event 패키지 |

### 6.2 테스트 구조

```
src/test/java/com/quizplatform/[module]/
├── domain/                           # 도메인 로직 테스트
│   ├── [Entity]Test.java            # 엔티티 테스트
│   └── [Domain]ServiceTest.java     # 도메인 서비스 테스트
├── application/service/              # 애플리케이션 서비스 테스트
│   └── [Module]ServiceTest.java
├── adapter/
│   ├── in/web/                      # 컨트롤러 테스트
│   │   └── [Entity]ControllerTest.java
│   ├── in/event/                    # 이벤트 리스너 테스트
│   │   └── [Module]EventListenerTest.java
│   └── out/persistence/             # 리포지토리 테스트
│       └── [Entity]RepositoryTest.java
└── integration/                     # 통합 테스트
    └── [Module]IntegrationTest.java
```

### 6.3 테스트 작성 가이드

#### 6.3.1 도메인 테스트

```java
@DisplayName("User 도메인 테스트")
class UserTest {

    @Test
    @DisplayName("사용자 생성 시 모든 필드가 올바르게 설정된다")
    void createUser_ShouldSetAllFieldsCorrectly() {
        // given
        String email = "test@example.com";
        String displayName = "Test User";
        UserRole role = UserRole.USER;

        // when
        User user = User.builder()
            .email(email)
            .displayName(displayName)
            .role(role)
            .build();

        // then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("이메일이 null인 경우 예외가 발생한다")
    void createUser_WithNullEmail_ShouldThrowException() {
        // given & when & then
        assertThatThrownBy(() -> User.builder()
            .email(null)
            .displayName("Test User")
            .role(UserRole.USER)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email cannot be null");
    }
}
```

#### 6.3.2 서비스 테스트

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("사용자 생성 성공")
    void createUser_Success() {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
            .email("test@example.com")
            .displayName("Test User")
            .build();
        
        User savedUser = User.builder()
            .id(1L)
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .role(UserRole.USER)
            .build();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserResponse response = userService.createUser(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getDisplayName()).isEqualTo(request.getDisplayName());
        
        verify(eventPublisher).publish(any(UserCreatedEvent.class));
    }
}
```

#### 6.3.3 컨트롤러 테스트

```java
@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("사용자 생성 API 성공")
    void createUser_Success() throws Exception {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
            .email("test@example.com")
            .displayName("Test User")
            .build();
        
        UserResponse response = UserResponse.builder()
            .id(1L)
            .email(request.getEmail())
            .displayName(request.getDisplayName())
            .role(UserRole.USER)
            .build();
        
        when(userService.createUser(any(UserCreateRequest.class), any(CurrentUserInfo.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@example.com",
                        "displayName": "Test User"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.displayName").value("Test User"));
    }
}
```

#### 6.3.4 이벤트 테스트

```java
@SpringBootTest
@Testcontainers
@DisplayName("User 이벤트 통합 테스트")
class UserEventIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @Autowired
    private UserService userService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("사용자 생성 시 UserCreatedEvent가 발행된다")
    void createUser_ShouldPublishEvent() {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
            .email("test@example.com")
            .displayName("Test User")
            .build();

        // when
        userService.createUser(request, createMockCurrentUser());

        // then
        // Kafka 토픽에서 이벤트 확인
        // 구체적인 검증 로직 구현
    }
}
```

### 6.4 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트 실행
./gradlew :modules:[module-name]:test

# 특정 테스트 클래스 실행
./gradlew :modules:[module-name]:test --tests="*UserServiceTest*"

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

---

## 7. 배포 및 설정

### 7.1 환경별 설정

#### 7.1.1 개발 환경 (Local)

```yaml
# application.yml
spring:
  profiles:
    active: local
  datasource:
    url: jdbc:postgresql://localhost:5432/[module]_db
    username: quizplatform
    password: password
  kafka:
    bootstrap-servers: localhost:9092

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### 7.1.2 Docker 환경 (개발환경 기준)

**중요**: `docker-compose.dev.yml` 환경변수와 일치시켜야 합니다.

```yaml
# application-docker.yml
server:
  port: 808x  # 각 서비스별로 8081, 8082, 8083...

spring:
  datasource:
    # Schema 분리 방식 사용
    url: jdbc:postgresql://postgres:5432/quiz_platform?currentSchema=[module]_schema
    username: quizuser
    password: quizpass
    driver-class-name: org.postgresql.Driver
  
  kafka:
    bootstrap-servers: kafka:29092  # 내부 컨테이너 통신용 포트
    
  # Redis 설정 (Battle, API Gateway에서 사용)
  redis:
    host: redis
    port: 6379
    
  # Elasticsearch 설정 (Quiz 서비스)
  elasticsearch:
    rest:
      uris: http://elasticsearch:9200

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true
```

#### 7.1.3 데이터베이스 스키마 구조

각 모듈은 독립적인 스키마를 사용합니다:

| 모듈 | 스키마명 | 연결 URL |
|------|----------|----------|
| User | `user_schema` | `?currentSchema=user_schema` |
| Quiz | `quiz_schema` | `?currentSchema=quiz_schema` |
| Battle | `battle_schema` | `?currentSchema=battle_schema` |

### 7.2 개발환경 Docker 배포

**개발환경에서는 반드시 `docker-compose.dev.yml`을 사용합니다.**

```bash
# 1. 전체 시스템 빌드
./gradlew build

# 2. 개발환경 Docker 이미지 빌드
docker-compose -f docker-compose.dev.yml build

# 3. 개발환경 서비스 시작
docker-compose -f docker-compose.dev.yml up -d

# 4. 특정 서비스 재시작 (코드 변경 후)
docker-compose -f docker-compose.dev.yml restart user-service

# 5. 로그 확인
docker-compose -f docker-compose.dev.yml logs -f user-service

# 6. 헬스 체크
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Quiz Service
curl http://localhost:8083/actuator/health  # Battle Service

# 7. 서비스 상태 확인
docker-compose -f docker-compose.dev.yml ps
```

#### 7.2.1 개발환경 서비스 URL

| 서비스 | URL | 비고 |
|--------|-----|------|
| API Gateway | http://localhost:8080 | 메인 진입점 |
| User Service | http://localhost:8081 | 직접 접근 (개발용) |
| Quiz Service | http://localhost:8082 | 직접 접근 (개발용) |
| Battle Service | http://localhost:8083 | 직접 접근 (개발용) |
| Eureka Dashboard | http://localhost:8761 | 서비스 등록 상태 |
| Swagger UI | http://localhost:808x/swagger-ui.html | API 문서 |

### 7.3 모니터링

#### 7.3.1 Health Check 엔드포인트

모든 서비스는 다음 관리 엔드포인트를 제공해야 합니다:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
  health:
    kafka:
      enabled: true
    db:
      enabled: true
```

#### 7.3.2 로그 설정

```yaml
logging:
  level:
    com.quizplatform: DEBUG
    org.springframework.kafka: INFO
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  file:
    name: logs/[module]-service.log
```

---

## 8. 문제해결 가이드

### 8.1 일반적인 문제들

#### 8.1.1 서비스 간 통신 문제

**증상**: 다른 서비스 호출 시 연결 실패

**해결책**:
1. Eureka 서버 상태 확인: `http://localhost:8761`
2. 네트워크 연결 확인: `docker network ls`
3. 서비스 등록 상태 확인: `curl http://localhost:8761/eureka/apps`

#### 8.1.2 Kafka 이벤트 전송 실패

**증상**: 이벤트 발행 후 다른 서비스에서 수신하지 못함

**해결책**:
1. Kafka 브로커 상태 확인
2. 토픽 존재 여부 확인
3. 컨슈머 그룹 상태 확인
4. 직렬화/역직렬화 설정 확인

```bash
# Kafka 토픽 목록 확인
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# 컨슈머 그룹 상태 확인
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups
```

#### 8.1.3 데이터베이스 연결 실패

**증상**: 애플리케이션 시작 시 데이터베이스 연결 오류

**해결책**:
1. PostgreSQL 컨테이너 상태 확인
```bash
docker-compose -f docker-compose.dev.yml ps postgres
```

2. 데이터베이스 자격 증명 확인 (개발환경 기준)
```yaml
# application-docker.yml에서 확인
username: quizuser
password: quizpass
database: quiz_platform
```

3. 스키마 존재 확인
```bash
# PostgreSQL 컨테이너 접속
docker exec -it quiz-postgres-dev psql -U quizuser -d quiz_platform

# 스키마 목록 확인
\dn

# 테이블 확인 (특정 스키마)
\dt user_schema.*
```

4. `docker-compose.dev.yml` 환경변수와 `application-docker.yml` 일치 확인

### 8.2 성능 최적화

#### 8.2.1 데이터베이스 쿼리 최적화

```java
// ❌ N+1 문제 발생
public List<QuizResponse> getQuizzes() {
    return quizRepository.findAll().stream()
        .map(quiz -> {
            List<Tag> tags = tagRepository.findByQuizId(quiz.getId()); // N+1!
            return QuizResponse.from(quiz, tags);
        })
        .toList();
}

// ✅ Fetch Join 사용
@Query("SELECT q FROM Quiz q LEFT JOIN FETCH q.tags")
List<Quiz> findAllWithTags();
```

#### 8.2.2 캐시 사용

```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
            .map(UserResponse::from)
            .orElseThrow(() -> new UserNotFoundException("ID: " + id));
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void updateUser(Long id, UserUpdateRequest request) {
        // 업데이트 로직
    }
}
```

### 8.3 보안 고려사항

#### 8.3.1 민감 정보 보호

```yaml
# 환경 변수 사용
spring:
  datasource:
    password: ${DB_PASSWORD:default_password}
  
# 프로파일별 설정 분리
---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    password: ${DB_PASSWORD}  # 환경 변수 필수
```

#### 8.3.2 API 보안

```java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @RequestBody @Valid UserCreateRequest request,  // 입력 검증
            @CurrentUser CurrentUserInfo currentUser) {     // 인증된 사용자
        
        // 인가 검증
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("권한이 없습니다");
        }
        
        return ResponseEntity.ok(userService.createUser(request, currentUser));
    }
}
```

---

## 9. 참고 자료

### 9.1 공식 문서

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Cloud 공식 문서](https://spring.io/projects/spring-cloud)
- [Apache Kafka 문서](https://kafka.apache.org/documentation/)
- [PostgreSQL 문서](https://www.postgresql.org/docs/)

### 9.2 아키텍처 패턴

- [헥사고날 아키텍처](https://alistair.cockburn.us/hexagonal-architecture/)
- [마이크로서비스 패턴](https://microservices.io/)
- [도메인 주도 설계](https://www.domainlanguage.com/ddd/)
- [이벤트 소싱](https://martinfowler.com/eaaDev/EventSourcing.html)

### 9.3 도구 및 라이브러리

- [TestContainers](https://www.testcontainers.org/)
- [Swagger/OpenAPI](https://swagger.io/)
- [Lombok](https://projectlombok.org/)
- [Docker](https://docs.docker.com/)

---

## 10. 변경 이력

| 날짜 | 버전 | 변경 내용 | 작성자 |
|------|------|-----------|--------|
| 2025-01-06 | 1.0 | MSA Development Guide 최초 작성 | Claude |

---

*이 문서는 프로젝트 진행에 따라 지속적으로 업데이트됩니다. 문의사항이나 개선 제안이 있으면 언제든 알려주세요.*