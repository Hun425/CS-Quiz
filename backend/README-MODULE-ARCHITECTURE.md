# 퀴즈 플랫폼 모듈형 아키텍처

## 개요

이 프로젝트는 모듈형 아키텍처(Modular Architecture)를 기반으로 구축된 퀴즈 플랫폼입니다. 향후 마이크로서비스 아키텍처(MSA)로의 쉬운 전환을 위해 설계되었습니다.

## 아키텍처 구조

프로젝트는 다음과 같은 모듈로 구성되어 있습니다:

1. **코어(Core) 모듈**: 공통 컴포넌트, 유틸리티, 기본 엔티티 등을 포함합니다.
2. **사용자(User) 모듈**: 사용자 인증, 권한 관리, 프로필 관리 기능을 제공합니다.
3. **퀴즈(Quiz) 모듈**: 퀴즈 생성, 조회, 수정, 삭제 및 퀴즈 풀이 기능을 제공합니다.
4. **태그(Tag) 모듈**: 태그 관리 및 컨텐츠 연결 기능을 제공합니다.
5. **배틀(Battle) 모듈**: 실시간 퀴즈 대결, 배틀 관리 기능을 제공합니다.
6. **검색(Search) 모듈**: ElasticSearch를 활용한 컨텐츠 검색 기능을 제공합니다.
7. **API 게이트웨이(API Gateway)**: 외부 API 요청을 적절한 모듈로 라우팅하는 게이트웨이 역할을 합니다.

## 모듈 간 의존성

각 모듈 간의 의존성은 다음과 같습니다:

```
API Gateway
    ├── Core
    ├── User Module
    ├── Quiz Module
    ├── Tag Module
    ├── Battle Module
    └── Search Module

User Module
    └── Core

Quiz Module
    ├── Core
    ├── User Module
    └── Tag Module

Tag Module
    └── Core

Battle Module
    ├── Core
    ├── User Module
    └── Quiz Module

Search Module
    ├── Core
    ├── Quiz Module
    └── Tag Module
```

## 모듈 간 통신

모듈 간 통신은 다음과 같은 방식으로 이루어집니다:

1. **모노리스 모드**: 단일 애플리케이션으로 실행할 때는 직접 메서드 호출을 통해 통신
2. **MSA 모드**: 각 모듈이 독립적인 서비스로 실행될 때는 HTTP/REST API를 통해 통신

## 모듈별 API 인터페이스

각 모듈은 명확한 API 인터페이스를 제공하여 다른 모듈과의 통신을 정의합니다:

- `UserModuleApi`: 사용자 관련 API
- `QuizModuleApi`: 퀴즈 관련 API
- `TagModuleApi`: 태그 관련 API
- `BattleModuleApi`: 배틀 관련 API
- `SearchModuleApi`: 검색 관련 API

## DTO 변환

모듈 간 데이터 교환은 DTO(Data Transfer Object)를 통해 이루어집니다. 각 모듈은 자체 도메인 모델을 가지고 있으며, `DtoMapper` 인터페이스를 구현하여 도메인 모델과 DTO 간의 변환을 담당합니다.

## 설정

각 모듈은 독립적인 설정 파일(`application.yml`)을 가지고 있으며, 다음과 같은 정보를 포함합니다:

- 데이터베이스 연결 정보
- 서버 포트
- 로깅 설정
- 모듈 특화 설정

## 빌드 및 실행

### 빌드

각 모듈은 독립적으로 빌드할 수 있습니다:

```bash
./gradlew :core:build
./gradlew :modules:user:build
./gradlew :modules:quiz:build
./gradlew :modules:tag:build
./gradlew :modules:battle:build
./gradlew :modules:search:build
./gradlew :api-gateway:build
```

### 실행

#### 모노리스 모드 (단일 애플리케이션)

```bash
./gradlew :api-gateway:bootRun
```

#### MSA 모드 (독립 서비스)

각 모듈을 독립적으로 실행:

```bash
./gradlew :modules:user:bootRun
./gradlew :modules:quiz:bootRun
./gradlew :modules:tag:bootRun
./gradlew :modules:battle:bootRun
./gradlew :modules:search:bootRun
./gradlew :api-gateway:bootRun
```

#### Docker Compose 사용 (권장)

```bash
docker-compose up -d
```

## 모듈 실행 포트

- API Gateway: 8080
- User Module: 8081
- Quiz Module: 8082
- Tag Module: 8083
- Battle Module: 8084
- Search Module: 8085

## 기술 스택

- Java 17
- Spring Boot 3.2.2
- Spring Data JPA
- Spring Security
- Spring WebSocket
- Spring Data Redis
- Spring Data Elasticsearch
- PostgreSQL
- Redis
- ElasticSearch
- Docker & Docker Compose
- Gradle

## MSA 전환 전략

모듈형 아키텍처에서 MSA로의 전환은 다음과 같은 단계로 이루어집니다:

1. 모듈 API 인터페이스 정의 및 구현 (완료)
2. DTO 변환 구현 (완료)
3. 모듈별 독립 실행 환경 구성 (완료)
4. 모듈 간 HTTP 통신 구현 (완료)
5. 컨테이너화 및 Docker Compose 설정 (완료)
6. 서비스 디스커버리 및 로드 밸런싱 도입 (향후 작업)
7. 분산 트랜잭션 관리 (향후 작업)
8. 서킷 브레이커 패턴 적용 (향후 작업)
9. API 게이트웨이 고도화 (향후 작업)
10. 로깅 및 모니터링 시스템 통합 (향후 작업)

## 라이선스

MIT