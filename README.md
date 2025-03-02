## CS 퀴즈 플랫폼

대중교통 이용 시간을 효율적으로 활용할 수 있는 CS(Computer Science) 학습 퀴즈 플랫폼입니다. 짧은 시간에 CS 지식을 테스트하고 향상시킬 수 있는 기회를 제공합니다.
## 💡 프로젝트 소개
본 프로젝트는 컴퓨터 과학 관련 지식을 퀴즈 형태로 학습할 수 있는 웹 애플리케이션입니다. 사용자들은 다양한 주제와 난이도의 퀴즈를 풀면서 지식을 쌓고, 실시간으로 다른 사용자들과 경쟁할 수 있습니다.

## 팀원

| **채기훈** |
| :------: |
| [<img src="https://avatars.githubusercontent.com/Hun425?v=4" height=150 width=150><br/> @Hun425](https://github.com/Hun425) |


## 주요 기능

다양한 주제별 CS 퀴즈
실시간 대결 모드로 다른 사용자와 경쟁
개인 학습 성취도 및 통계 트래킹
맞춤형 퀴즈 추천 시스템
소셜 로그인(Google, GitHub, Kakao)
데일리 퀴즈 챌린지
업적 및 레벨 시스템

## 🛠 기술 스택
### 백엔드

Java 17
Spring Boot 3.x
Spring Security
Spring Data JPA & QueryDSL
PostgreSQL
Redis
Elasticsearch
WebSocket

### 프론트엔드

React
TypeScript
React Router
상태 관리: Zustand
CSS-in-JS

### 인프라

Docker
GitHub Actions
AWS (예정)

## 📋 프로젝트 구조
```
├── backend/
│   ├── src/main/java/com/quizplatform/
│   │   ├── core/                          # 핵심 도메인 모델 및 서비스
│   │   │   ├── config/                    # 애플리케이션 설정
│   │   │   ├── controller/                # API 엔드포인트
│   │   │   ├── domain/                    # 도메인 모델
│   │   │   ├── dto/                       # 데이터 전송 객체
│   │   │   ├── exception/                 # 예외 처리
│   │   │   ├── repository/                # 데이터 접근 계층
│   │   │   └── service/                   # 비즈니스 로직
│   │   └── modules/                       # 기능별 모듈
│   └── resources/                         # 설정 파일
└── frontend/
    ├── src/
    │   ├── api/                           # API 연동
    │   ├── components/                    # 재사용 컴포넌트
    │   ├── pages/                         # 페이지 컴포넌트
    │   ├── store/                         # 상태 관리
    │   └── types/                         # 타입 정의
    └── public/                            # 정적 파일
```
## 📱 주요 화면
### 홈 화면

- 오늘의 퀴즈
- 추천 퀴즈
- 플랫폼 소개

### 퀴즈 리스트

- 태그, 난이도, 제목 기반 검색 및 필터링
- 퀴즈 카드 UI

### 퀴즈 상세

- 퀴즈 정보
- 통계 및 평균 점수
- 시작하기 버튼

### 퀴즈 플레이

- 문제 진행 상태
- 타이머
- 문제 유형별 응답 UI
- 문제 인덱스 네비게이션

### 결과 화면

- 점수 및 정답률
- 문제별 상세 결과 및 해설
- 관련 퀴즈 추천

### 프로필

- 사용자 정보
- 학습 통계
- 획득한 업적
- 주제별 성과
- 최근 활동

### 배틀 모드

- 실시간 대결방
- 참가자 목록 및 준비 상태
- 문제 진행 및 점수 현황
- 최종 결과 및 순위


## 📊 데이터베이스 스키마

- User: 사용자 정보, 통계, 레벨
- Quiz: 퀴즈 정보, 문제 목록, 태그
- Question: 문제 내용, 정답, 설명
- BattleRoom: 대결방 정보, 참가자, 상태
- Tag: 태그 계층 구조, 연관 퀴즈

## 🚀 향후 계획

- 모바일 앱 개발 (React Native)
- 퀴즈 에디터 기능 개선
- 코드 스니펫 실행 환경
- 대규모 사용자 지원을 위한 인프라 확장
- 커뮤니티 기능 추가
- AI 기반 문제 생성 시스템



