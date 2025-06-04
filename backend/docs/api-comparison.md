# 배틀 모듈 API 비교 문서

이 문서는 퀴즈 플랫폼의 레거시 배틀 API와 새로운 모듈형 배틀 API 간의 차이점을 상세히 설명합니다. 프론트엔드 개발자가 새 API로 마이그레이션하는 데 도움이 되는 정보를 제공합니다.

## 목차

1. [개요](#개요)
2. [REST API 비교](#rest-api-비교)
3. [WebSocket API 비교](#websocket-api-비교)
4. [DTO 객체 비교](#dto-객체-비교)
5. [주요 변경점 요약](#주요-변경점-요약)
6. [마이그레이션 가이드](#마이그레이션-가이드)

## 개요

퀴즈 플랫폼 배틀 기능은 기존 모놀리식 아키텍처에서 모듈형 아키텍처로 마이그레이션되었습니다. 이 과정에서 일부 API 엔드포인트와 요청/응답 형식에 변경이 있었습니다.

- **레거시 API 위치**: `legacy/main/java/com/quizplatform/core/controller/battle/`
- **새 모듈형 API 위치**: `modules/battle/src/main/java/com/quizplatform/battle/adapter/in/web/`

## REST API 비교

### 공통점

- 두 API 모두 기본 경로는 `/api/battles`를 사용합니다.
- 두 API 모두 동일한 주요 기능(생성, 참가, 준비, 나가기 등)을 제공합니다.
- 두 API 모두 Swagger/OpenAPI로 문서화되어 있습니다.

### 차이점

| 기능 | 레거시 API | 모듈형 API | 주요 차이점 |
|------|------------|------------|------------|
| **배틀방 생성** | POST `/api/battles` | POST `/api/battles` | **입력값**: 레거시는 `BattleRoomCreateRequest` DTO를 사용하고, 모듈형은 `Map<String, Object>`를 사용<br>**인증**: 레거시는 Spring Security를 사용한 사용자 인증 필요, 모듈형은 요청 본문에 사용자 ID 직접 포함<br>**반환값**: 레거시는 `CommonApiResponse<BattleRoomResponse>`, 모듈형은 `BattleRoom` 엔티티 직접 반환 |
| **배틀방 조회** | GET `/api/battles/{roomId}` | GET `/api/battles/{roomId}` | **반환값**: 레거시는 `CommonApiResponse<BattleRoomResponse>`, 모듈형은 `BattleRoom` 엔티티 직접 반환 |
| **배틀방 목록 조회** | GET `/api/battles/active` | GET `/api/battles?status=WAITING` | **경로**: 레거시는 `/active` 경로 사용, 모듈형은 쿼리 파라미터 사용<br>**필터링**: 모듈형은 `status` 파라미터로 상태별 필터링 가능 |
| **배틀방 참가** | POST `/api/battles/{roomId}/join` | POST `/api/battles/{roomId}/join` | **입력값**: 레거시는 요청 본문 없이 인증된 사용자 정보 사용, 모듈형은 요청 본문에 사용자 정보 포함<br>**인증**: 레거시는 Spring Security 사용, 모듈형은 직접 사용자 정보 전달 |
| **준비 상태 토글** | POST `/api/battles/{roomId}/ready` | POST `/api/battles/{roomId}/ready/{userId}` | **경로**: 모듈형은 URL에 userId 포함<br>**인증**: 레거시는 Spring Security 사용, 모듈형은 URL로 사용자 ID 직접 전달 |
| **배틀방 나가기** | POST `/api/battles/{roomId}/leave` | POST `/api/battles/{roomId}/leave/{userId}` | **경로**: 모듈형은 URL에 userId 포함<br>**인증**: 레거시는 Spring Security 사용, 모듈형은 URL로 사용자 ID 직접 전달 |
| **내 활성 대결방 조회** | GET `/api/battles/my-active` | 없음 | **기능 차이**: 모듈형에는 해당 기능 없음 |
| **배틀 시작** | 없음 | POST `/api/battles/{roomId}/start` | **기능 차이**: 레거시는 WebSocket으로만 관리, 모듈형은 REST API로도 시작 가능 |
| **다음 문제 진행** | 없음 | POST `/api/battles/{roomId}/next-question` | **기능 차이**: 레거시는 WebSocket으로만 관리, 모듈형은 REST API로도 진행 가능 |
| **답변 처리** | 없음 | POST `/api/battles/{roomId}/answer` | **기능 차이**: 레거시는 WebSocket으로만 관리, 모듈형은 REST API로도 처리 가능 |
| **배틀 종료** | 없음 | POST `/api/battles/{roomId}/finish` | **기능 차이**: 레거시는 WebSocket으로만 관리, 모듈형은 REST API로도 종료 가능 |

## WebSocket API 비교

두 API 모두 WebSocket을 사용하여 실시간 배틀 기능을 제공합니다.

### 공통점

- 두 API 모두 STOMP 프로토콜을 사용합니다.
- 엔드포인트 경로가 동일합니다: `/app/battle/...`
- 구독 채널 경로가 동일합니다: `/topic/battle/{roomId}/...`

### 차이점

| 기능 | 레거시 API | 모듈형 API | 주요 차이점 |
|------|------------|------------|------------|
| **WebSocket 연결** | `/ws-endpoint` | `/battle/ws` | **경로**: 연결 엔드포인트 경로가 다름 |
| **배틀 참가** | `/app/battle/join` | `/app/battle/join` | **기능 동일**: 참가 요청 및 처리 로직 동일 |
| **준비 상태 변경** | `/app/battle/ready` | `/app/battle/ready` | **기능 동일**: 준비 상태 변경 로직 동일 |
| **답변 제출** | `/app/battle/answer` | `/app/battle/answer` | **기능 동일**: 답변 처리 로직 동일 |
| **배틀방 나가기** | `/app/battle/leave` | `/app/battle/leave` | **기능 동일**: 나가기 처리 로직 동일 |
| **참가자 목록 구독** | `/topic/battle/{roomId}/participants` | `/topic/battle/{roomId}/participants` | **기능 동일**: 참가자 목록 업데이트 구독 동일 |
| **방 상태 구독** | `/topic/battle/{roomId}/status` | `/topic/battle/{roomId}/status` | **기능 동일**: 방 상태 변경 구독 동일 |
| **문제 구독** | `/topic/battle/{roomId}/question` | `/topic/battle/{roomId}/question` | **기능 동일**: 문제 구독 동일 |
| **진행 상황 구독** | `/topic/battle/{roomId}/progress` | `/topic/battle/{roomId}/progress` | **기능 동일**: 진행 상황 구독 동일 |
| **결과 구독** | `/topic/battle/{roomId}/result` | `/topic/battle/{roomId}/result` | **기능 동일**: 최종 결과 구독 동일 |
| **개인 결과 수신** | `/user/queue/battle/result` | `/user/queue/battle/result` | **기능 동일**: 개인 답변 결과 수신 동일 |

## DTO 객체 비교

### 레거시 API와 모듈형 API의 DTO 구조 비교

| 레거시 DTO | 모듈형 DTO | 주요 차이점 |
|------------|------------|------------|
| `BattleRoomCreateRequest` | 없음 (Map 사용) | 모듈형은 구조화된 DTO 대신 Map 사용 |
| `BattleRoomResponse` | `BattleRoom` | 모듈형은 응답 래퍼 DTO 대신 도메인 모델 직접 사용 |
| `BattleJoinRequest` | `BattleJoinRequest` | 기능 유사하나 패키지 경로 다름 |
| `BattleJoinResponse` | `BattleJoinResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleReadyRequest` | `BattleReadyRequest` | 기능 유사하나 패키지 경로 다름 |
| `BattleReadyResponse` | `BattleReadyResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleLeaveRequest` | `BattleLeaveRequest` | 기능 유사하나 패키지 경로 다름 |
| `BattleLeaveResponse` | `BattleLeaveResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleAnswerRequest` | `BattleAnswerRequest` | 기능 유사하나 패키지 경로 다름 |
| `BattleAnswerResponse` | `BattleAnswerResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleNextQuestionResponse` | `BattleNextQuestionResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleProgressResponse` | `BattleProgressResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleResultResponse` | `BattleResultResponse` | 기능 유사하나 패키지 경로 다름 |
| `BattleRoomStatusChangeResponse` | `BattleRoomStatusChangeResponse` | 기능 유사하나 패키지 경로 다름 |
| `CommonApiResponse<T>` | 없음 | 모듈형은 공통 응답 래퍼 사용하지 않음 |

## 주요 변경점 요약

1. **인증 방식 변경**:
   - 레거시: Spring Security와 `@AuthenticationPrincipal`을 통한 사용자 인증
   - 모듈형: 요청 본문 또는 URL에 직접 사용자 ID 포함

2. **응답 형식 변경**:
   - 레거시: `CommonApiResponse<T>` 래퍼 클래스 사용
   - 모듈형: 도메인 모델이나 DTO 직접 반환

3. **REST API 확장**:
   - 모듈형에서는 WebSocket으로만 처리하던 일부 기능(배틀 시작, 답변 제출 등)을 REST API로도 제공

4. **배틀방 목록 조회 방식 변경**:
   - 레거시: 고정 경로(`/active`)로 활성 방만 조회
   - 모듈형: 쿼리 파라미터로 다양한 상태의 방 조회 가능

5. **DTO 패키지 경로 변경**:
   - 레거시: `com.quizplatform.core.dto.battle`
   - 모듈형: `com.quizplatform.battle.application.dto`

## 마이그레이션 가이드

### REST API 마이그레이션

1. **배틀방 생성 API**:
   ```javascript
   // 레거시
   axios.post('/api/battles', {
     quizId: 123,
     maxParticipants: 4
   });

   // 모듈형
   axios.post('/api/battles', {
     quizId: 123,
     maxParticipants: 4,
     creatorId: currentUser.id,
     creatorUsername: currentUser.username,
     profileImage: currentUser.profileImage,
     totalQuestions: 10,
     questionTimeLimit: 30
   });
   ```

2. **배틀방 참가 API**:
   ```javascript
   // 레거시
   axios.post(`/api/battles/${roomId}/join`);

   // 모듈형
   axios.post(`/api/battles/${roomId}/join`, {
     userId: currentUser.id,
     username: currentUser.username,
     profileImage: currentUser.profileImage
   });
   ```

3. **준비 상태 토글 API**:
   ```javascript
   // 레거시
   axios.post(`/api/battles/${roomId}/ready`);

   // 모듈형
   axios.post(`/api/battles/${roomId}/ready/${currentUser.id}`);
   ```

4. **배틀방 나가기 API**:
   ```javascript
   // 레거시
   axios.post(`/api/battles/${roomId}/leave`);

   // 모듈형
   axios.post(`/api/battles/${roomId}/leave/${currentUser.id}`);
   ```

### WebSocket API 마이그레이션

1. **WebSocket 연결**:
   ```javascript
   // 레거시
   const socket = new SockJS('/ws-endpoint');

   // 모듈형
   const socket = new SockJS('/battle/ws');
   ```

2. **기타 WebSocket 통신**:
   - 나머지 WebSocket 엔드포인트와 구독 패턴은 동일하게 유지됩니다.
   - 응답 처리 로직에서 `CommonApiResponse` 래퍼를 제거해야 합니다.

### 중요 고려사항

1. **인증 변경**:
   - 모듈형 API는 Spring Security 통합이 없으므로 사용자 ID를 직접 전달해야 합니다.
   - 향후 보안 강화를 위해 인증 토큰 시스템 구현이 필요할 수 있습니다.

2. **응답 형식 변경**:
   - 레거시 API의 `CommonApiResponse` 래퍼를 처리하는 코드 제거
   - 오류 처리 로직 수정 (HTTP 상태 코드 및 응답 본문 형식 변경)

3. **선택적 REST API 사용**:
   - 모듈형에서 새롭게 제공하는 REST API(`/start`, `/next-question`, `/answer`, `/finish`)는 필요에 따라 WebSocket 대신 사용 가능합니다.
   - 실시간성이 중요한 경우 WebSocket 사용을 권장합니다. 