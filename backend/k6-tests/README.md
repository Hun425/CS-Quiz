# Redis 캐시 성능 테스트 가이드

Redis 캐시 적용에 따른 성능 개선 효과를 측정하기 위한 k6 테스트 스크립트와 가이드입니다.

## 사전 준비 사항

### k6 설치 (옵션 1: 로컬 설치)

#### Windows
```
winget install k6
```

#### macOS
```
brew install k6
```

#### Linux
```
curl -L https://github.com/grafana/k6/releases/download/v0.42.0/k6-v0.42.0-linux-amd64.tar.gz | tar xz
sudo mv k6-v0.42.0-linux-amd64/k6 /usr/local/bin/
```

### Docker를 통한 k6 실행 (옵션 2: 도커 사용)

로컬에 k6를 설치하지 않고 Docker를 통해 실행할 수도 있습니다. 이 방법을 사용하면 환경 설정이 더 간단해집니다.

#### 1. Docker와 Docker Compose 설치

시스템에 Docker와 Docker Compose가 설치되어 있어야 합니다.

#### 2. Docker Compose로 모든 서비스 시작

```bash
cd k6-tests
docker-compose up -d
```

이 명령은 InfluxDB, Grafana, k6 컨테이너를 모두 시작합니다.

#### 3. Docker를 통한 k6 테스트 실행

```bash
# 일반 부하 테스트
docker-compose exec k6 k6 run /scripts/redis-cache-performance.js

# 캐시 사용/미사용 비교 테스트 
docker-compose exec k6 k6 run /scripts/redis-cache-comparison.js
```

결과는 자동으로 InfluxDB에 저장되며 Grafana에서 볼 수 있습니다.

### 백엔드 구성 요구사항

1. Redis 캐시가 구성되어 있어야 합니다.
2. 캐시 테스트를 위해 `X-Skip-Cache: true` 헤더를 처리할 수 있도록 백엔드 수정이 필요합니다.

백엔드의 요청 처리 로직에 다음과 같은 코드를 추가해야 합니다:

```java
@Override
public QuizResponse getQuizWithQuestions(Long quizId, HttpServletRequest request) {
    // X-Skip-Cache 헤더가 존재하고 true일 경우 캐시를 건너뛰도록 처리
    String skipCache = request.getHeader("X-Skip-Cache");
    if (skipCache != null && skipCache.equalsIgnoreCase("true")) {
        // 캐시를 사용하지 않고 직접 DB에서 조회
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        return entityMapperService.mapToQuizResponse(quiz);
    }
    
    // 기존 캐시 로직 실행
    return getCachedQuizWithQuestions(quizId);
}
```

위와 같은 패턴으로 캐시를 사용하는 모든 메소드에 적용해야 합니다.

## 테스트 실행 방법

### 1. 일반 부하 테스트

```bash
# 로컬 설치 버전 사용 시
k6 run k6-tests/redis-cache-performance.js

# Docker 사용 시
docker-compose exec k6 k6 run /scripts/redis-cache-performance.js
```

이 테스트는 점진적으로 증가하는 부하를 시뮬레이션하며 캐시가 시스템 부하 상황에서 얼마나 잘 작동하는지 측정합니다.

### 2. 캐시 사용/미사용 비교 테스트

```bash
# 로컬 설치 버전 사용 시
k6 run k6-tests/redis-cache-comparison.js

# Docker 사용 시
docker-compose exec k6 k6 run /scripts/redis-cache-comparison.js
```

이 테스트는 동일한 요청에 대해 캐시 사용 시와 미사용 시의 응답 시간을 직접 비교합니다.

### 3. 단일 명령으로 Docker Compose 실행 및 테스트 수행

모든 것을 한 번에 실행하려면 다음 명령을 사용할 수 있습니다:

```bash
# 모든 서비스 시작 및 테스트 실행
docker-compose up -d && \
docker-compose exec k6 k6 run /scripts/redis-cache-comparison.js
```

## Grafana로 시각화하기

k6 결과를 Grafana에서 시각화하면 보다 직관적인 분석이 가능합니다.

### 1. Docker Compose 설정

`docker-compose.yml` 파일은 이미 구성되어 있습니다.

### 2. 시작하기

```bash
docker-compose up -d
```

### 3. k6 테스트 결과를 InfluxDB로 전송하기

Docker 버전 사용 시 자동으로 InfluxDB로 전송됩니다.
로컬 k6 사용 시:

```bash
k6 run --out influxdb=http://localhost:8086/k6 k6-tests/redis-cache-performance.js
```

```bash
k6 run --out influxdb=http://localhost:8086/k6 k6-tests/redis-cache-comparison.js
```

### 4. Grafana 대시보드 설정

1. 브라우저에서 `http://localhost:3000` 접속
2. Configuration > Data Sources > Add data source 선택
3. InfluxDB 선택
   - URL: `http://influxdb:8086`
   - Database: `k6`
   - User: `admin`
   - Password: `admin`
   - HTTP Method: `GET`
4. "Save & Test" 클릭하여 연결 확인
5. 대시보드 생성 (+ 버튼 > Import)
   - Grafana의 k6 대시보드 ID를 이용해 import (예: 2587)

## 성능 테스트 보고서 작성 요령

k6 테스트 결과를 바탕으로 다음 항목을 포함하는 성능 보고서를 작성하는 것이 좋습니다:

1. 테스트 환경 및 설정
   - 서버 스펙, 네트워크 환경, 데이터베이스 크기
   - 테스트 데이터 개수 (퀴즈, 태그, 사용자 수 등)
   - 테스트 시나리오 및 지속 시간

2. 성능 지표
   - 응답 시간 (평균, 중앙값, 95%, 99% 백분위)
   - 초당 요청 처리량 (RPS)
   - 캐시 적중률
   - 서버 리소스 사용률 (CPU, 메모리, 네트워크)

3. 캐시 적용 전/후 비교
   - 각 엔드포인트별 성능 향상 배수
   - 전체 시스템 부하 감소량
   - 사용자 경험 개선 효과 (예: 페이지 로드 시간)

4. 결론 및 권장사항
   - 캐시 적용 효과 요약
   - 추가 최적화 가능성
   - 실제 서비스 적용 시 고려사항 

# Redis 캐시 성능 테스트

이 디렉토리에는 Redis 캐시 성능 테스트를 위한 k6 스크립트와 Docker 설정이 포함되어 있습니다.

## 테스트 환경 구성

1. Docker와 Docker Compose가 설치되어 있어야 합니다.
2. 다음 명령으로 테스트 환경을 시작합니다:

```bash
docker-compose up -d
```

3. Grafana 대시보드는 http://localhost:3000 에서 접근할 수 있습니다.
   - 기본 데이터 소스로 InfluxDB가 자동으로 구성됩니다.
   - 대시보드는 k6 결과를 시각화하도록 설정되어 있습니다.
   
## 테스트 스크립트

이 프로젝트에는 다음과 같은 테스트 스크립트가 포함되어 있습니다:

1. **redis-cache-performance.js** - 기본 부하 테스트
   - Redis 캐시를 사용하는 API에 대한 부하 테스트
   - 다양한 엔드포인트에 대한 기본적인 성능 측정

2. **redis-cache-comparison.js** - 캐시 사용/미사용 비교 테스트
   - 각 API 엔드포인트에 대해 캐시 사용/미사용시의 성능 비교
   - 'X-Skip-Cache' 헤더를 사용하여 캐시 사용 여부 제어

3. **cache-scale-test.js** - 데이터 크기별 캐시 효과 측정 테스트
   - 다양한 데이터 크기(10, 100, 500, 1000개)에 따른 캐시 효과 측정
   - 캐시의 스케일링 효과를 분석하기 위한 자동화된 테스트

## 테스트 실행 방법

### 기본 부하 테스트 실행

```bash
docker-compose exec k6 k6 run /scripts/redis-cache-performance.js
```

### 캐시 사용/미사용 비교 테스트 실행

```bash
docker-compose exec k6 k6 run /scripts/redis-cache-comparison.js
```

### 데이터 크기별 캐시 효과 측정 테스트 실행

#### Linux/Mac 환경

```bash
# 실행 권한 부여
chmod +x run-scale-test.sh

# 테스트 실행
./run-scale-test.sh
```

#### Windows 환경 (PowerShell)

```powershell
# PowerShell에서 실행
.\run-scale-test.ps1
```

이 테스트는 다음 과정을 자동으로 수행합니다:
1. 테스트 데이터 삭제
2. 지정된 크기(10, 100, 500, 1000)의 더미 퀴즈 데이터 생성
3. 캐시 워밍업
4. 각 데이터 크기별로 캐시 사용/미사용 성능 비교
5. 결과를 `scale_test_results` 디렉토리에 저장
6. 결과 요약을 Markdown 형식으로 생성

## 테스트 결과 분석

테스트 결과는 다음과 같은 방법으로 분석할 수 있습니다:

1. **Grafana 대시보드** - http://localhost:3000 에서 실시간 테스트 결과 확인
2. **콘솔 출력** - 테스트 실행 중 콘솔에 출력되는 결과 확인
3. **결과 요약 파일** - 스케일 테스트의 경우 `scale_test_results/summary.md` 파일에서 결과 요약 확인

## 테스트 관리 API

스케일 테스트를 위해 다음 관리자 API가 제공됩니다:

1. **더미 데이터 생성** - `POST /api/admin/generate-quizzes?count={개수}`
2. **캐시 워밍업** - `POST /api/admin/warmup-cache`
3. **더미 데이터 삭제** - `DELETE /api/admin/clear-data`

## 스케일 테스트 결과 설명

스케일 테스트 결과는 다음 정보를 보여줍니다:

- **데이터 크기**: 테스트에 사용된 퀴즈 데이터 수
- **퀴즈 상세 조회 (개선율)**: 퀴즈 상세 조회 API의 캐시 사용 시 성능 개선 비율
- **퀴즈 검색 (개선율)**: 퀴즈 검색 API의 캐시 사용 시 성능 개선 비율
- **인기 퀴즈 추천 (개선율)**: 인기 퀴즈 추천 API의 캐시 사용 시 성능 개선 비율
- **평균 개선율**: 전체 API의 평균 성능 개선 비율

일반적으로 데이터 크기가 클수록 캐시의 효과가 더 크게 나타납니다. 이는 데이터베이스 쿼리 부하가 데이터 크기에 비례하여 증가하는 반면, 캐시 조회 시간은 데이터 크기와 거의 무관하기 때문입니다. 

const BASE_URL = 'http://localhost:8080/api';  // 또는 실제 IP 주소 사용 