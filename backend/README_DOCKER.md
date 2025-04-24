# Quiz Platform 도커 환경 가이드

## 개요
이 문서는 Quiz Platform의 마이크로서비스 아키텍처를 도커 환경에서 실행하는 방법에 대한 가이드를 제공합니다.
모든 모듈은 개별 컨테이너로 실행되며, Docker Compose를 통해 관리됩니다.

## 시스템 요구사항
- Docker Engine 24.0 이상
- Docker Compose v2 이상
- 최소 8GB RAM
- 최소 20GB 디스크 공간

## 서비스 구성
Quiz Platform은 다음과 같은 서비스로 구성되어 있습니다:

1. **마이크로서비스**
   - API Gateway (포트: 8080)
   - Eureka Server (포트: 8761)
   - User Service (포트: 8081)
   - Quiz Service (포트: 8082)
   - Battle Service (포트: 8083)

2. **인프라 서비스**
   - PostgreSQL (포트: 5432)
   - Redis (포트: 6379)
   - Elasticsearch (포트: 9200)
   - Kafka (포트: 9092)
   - Zookeeper (포트: 2181)

## 시작하기

### 1. 초기 설정
처음 시작하기 전에 Gradle 래퍼를 모든 모듈에 복사합니다:

```bash
# Windows
copy_gradle_wrapper.bat

# Linux/Mac
chmod +x copy_gradle_wrapper.sh
./copy_gradle_wrapper.sh
```

### 2. 서비스 시작

제공된 스크립트를 사용하여 서비스를 쉽게 시작할 수 있습니다:

```bash
# Windows
start-services.bat

# Linux/Mac
chmod +x start-services.sh
./start-services.sh
```

스크립트를 통해 다음과 같은 모드를 선택할 수 있습니다:
- **개발 모드**: 소스 코드 변경이 자동 반영되고, 디버깅 포트가 열립니다.
- **프로덕션 모드**: 최적화된 설정으로 모든 서비스가 실행됩니다.
- **인프라 서비스만**: 데이터베이스, 캐시, 메시지 큐 등만 실행됩니다.

### 3. 서비스 중지

서비스를 중지하려면 다음 스크립트를 사용합니다:

```bash
# Windows
stop-services.bat

# Linux/Mac
chmod +x stop-services.sh
./stop-services.sh
```

중지 옵션:
- 컨테이너만 중지 (데이터 유지)
- 컨테이너 삭제 (데이터 유지)
- 컨테이너와 볼륨 모두 삭제 (모든 데이터 삭제)

### 4. 로그 확인

서비스 로그를 확인하려면:

```bash
# Windows
logs.bat

# Linux/Mac
chmod +x logs.sh
./logs.sh
```

## 개발 환경

### 디버깅
개발 모드에서는 각 서비스에 대해 다음과 같은 디버깅 포트가 열려 있습니다:
- API Gateway: 5080
- User Service: 5081
- Quiz Service: 5082
- Battle Service: 5083
- Eureka Server: 5761

### 코드 변경 자동 반영
개발 모드에서는 로컬 소스 코드 디렉토리가 컨테이너에 마운트되어 코드 변경 시 자동으로 반영됩니다.

### 서비스 상세 정보

#### API Gateway
- 역할: 모든 클라이언트 요청의 진입점, 라우팅 및 인증 담당
- 포트: 8080
- 엔드포인트: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

#### User Service
- 역할: 사용자 계정 관리, 인증, 권한 관리
- 포트: 8081
- 엔드포인트: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html

#### Quiz Service
- 역할: 퀴즈 관리, 카테고리 관리, 검색
- 포트: 8082
- 엔드포인트: http://localhost:8082
- Swagger UI: http://localhost:8082/swagger-ui.html

#### Battle Service
- 역할: 실시간 대전 관리, 점수 계산
- 포트: 8083
- 엔드포인트: http://localhost:8083
- Swagger UI: http://localhost:8083/swagger-ui.html

#### Eureka Server
- 역할: 서비스 디스커버리, 로드 밸런싱
- 포트: 8761
- 대시보드: http://localhost:8761

## 문제 해결

### 일반적인 문제

#### 포트 충돌
이미 사용 중인 포트가 있다면 docker-compose.yml 또는 docker-compose.dev.yml 파일에서 해당 포트를 변경하세요.

#### 서비스 시작 실패
로그를 확인하여 문제를 파악하세요:
```bash
docker-compose logs -f [서비스명]
```

#### 데이터베이스 초기화 문제
볼륨을 삭제하고 다시 시작해보세요:
```bash
docker-compose down -v
docker-compose up -d
```

## 참고 사항
- 프로덕션 환경에서는 적절한 메모리 및 CPU 리소스 제한을 설정하세요.
- 민감한 정보(비밀번호, 키 등)는 환경 변수나 Docker Secrets를 통해 관리하세요.
- 정기적으로 데이터 백업을 수행하세요. 