# 기술 결정 사항 및 근거 문서 (Technical Decision Record)

## 개요
이 문서는 CS-Quiz MSA 프로젝트에서 내린 모든 기술적 결정사항과 그 근거를 기록합니다.
각 결정사항은 요구사항, 고려사항, 선택한 방안, 그리고 선택 이유를 포함합니다.

---

## TDR-001: 계층구조 태그 DB 설계 방식 선택

### 📅 결정 날짜
2025-06-13

### 🎯 문제 상황
Quiz Module에 Tag 시스템 통합 시 계층구조 태그(최대 3단계)를 지원하는 최적의 DB 설계 방식 선택 필요

### 🔍 요구사항
- 최대 3단계 깊이의 계층구조
- 관리자만 태그 생성/수정 (업데이트 빈도 낮음)
- 퀴즈 검색/필터링 성능 중요
- Legacy 코드와의 호환성 고려

### ⚖️ 고려된 옵션들

#### 1. Adjacency List (인접 리스트)
**구조:**
```sql
CREATE TABLE tags (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),
    parent_id BIGINT REFERENCES tags(id),
    level INT
);
```
**장점:** 구현 간단, 직관적, Legacy 호환
**단점:** 재귀 쿼리 필요 (깊은 계층에서 성능 이슈)

#### 2. Nested Set Model (중첩 집합)
**구조:**
```sql
CREATE TABLE tags (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),
    lft INT, rgt INT, level INT
);
```
**장점:** 하위 트리 조회 빠름
**단점:** 구현 복잡, 노드 추가/삭제 시 대량 업데이트

#### 3. Path Enumeration (경로 열거)
**구조:**
```sql
CREATE TABLE tags (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50),
    path VARCHAR(255), level INT
);
```
**장점:** 조상/후손 검색 빠름
**단점:** 경로 파싱 필요, 길이 제한

### ✅ 최종 결정: Adjacency List + 성능 최적화

### 🎯 선택 근거
1. **Legacy 호환성**: 기존 코드 재사용 가능, 마이그레이션 리스크 최소화
2. **제한된 깊이**: 3단계 제한으로 재귀 쿼리 성능 문제 미미
3. **구현 단순성**: MSA 환경에서 복잡도 최소화 필요
4. **업데이트 빈도**: 관리자만 수정하므로 노드 수정 빈도 낮음
5. **팀 이해도**: 직관적인 구조로 팀원들의 이해와 유지보수 용이

### 🔧 최적화 방안
- `level` 필드 추가로 깊이 사전 계산
- 계층 조회용 전용 쿼리 메서드 구현
- Redis 캐싱 적용으로 성능 보완
- 재귀 CTE(Common Table Expression) 활용

### 📊 예상 성능 영향
- 단일 태그 조회: O(1)
- 전체 하위 태그 조회: O(depth) ≈ O(3) = 상수
- 태그 추가/삭제: O(1)
- 계층 구조 변경: O(children) (영향받는 자식 노드만)

---

## TDR-002: Quiz-Tag 매핑 관계 유지 결정

### 📅 결정 날짜
2025-06-13

### 🎯 문제 상황
현재 MSA 구조의 Quiz-Tag 다대다 관계와 매핑 테이블을 유지할지, 아니면 새로운 구조로 변경할지 결정 필요

### 🔍 현재 구조 분석
**매핑 테이블:** `quiz_tag_mapping` (quiz_schema)
- 컬럼: `tag_id`, `quiz_id`
- Tag 엔티티가 연관관계 주인 (`@JoinTable` 사용)
- Quiz는 `mappedBy`로 역방향 참조

**장점:**
- 표준적인 JPA 다대다 관계 구현
- 매핑 테이블 명시적 정의로 스키마 명확
- Legacy와 일관된 관계 구조

**부족한 부분:**
- 퀴즈당 태그 수 제한 없음 (요구사항: 최대 10개)
- 태그 계층구조 정보 부재

### ⚖️ 고려된 옵션들

#### 1. 현재 구조 유지 + 개선
- 기존 매핑 관계 유지
- Tag 엔티티에 계층구조 필드 추가
- 비즈니스 로직으로 태그 수 제한

#### 2. 매핑 엔티티로 변경
- 중간 엔티티 `QuizTagMapping` 생성
- 추가 메타데이터 저장 가능 (생성시간, 가중치 등)

### ✅ 최종 결정: 현재 구조 유지 + 개선

### 🎯 선택 근거
1. **단순성**: 요구사항이 복잡하지 않아 표준 다대다 관계로 충분
2. **마이그레이션 비용**: 기존 테이블 구조 재사용으로 위험도 최소화
3. **성능**: 단순한 조인 테이블로 조회 성능 우수
4. **Legacy 호환성**: 기존 매핑 방식과 일관성 유지

### 🔧 개선 방안
- Tag 엔티티에 계층구조 필드 추가 (parent_id, level)
- Quiz 도메인에서 태그 수 제한 검증 로직
- 태그 관리 권한 체크를 Service 계층에서 처리

---

## TDR-003: 계층구조 Tag 도메인 모델 설계

### 📅 결정 날짜
2025-06-13

### 🎯 문제 상황
기존 평면적 Tag 모델을 계층구조를 지원하는 모델로 개선하여 요구사항(최대 3단계, 관리자 권한, 퀴즈당 최대 10개) 충족 필요

### 🔍 요구사항
- 최대 3단계 계층구조 지원
- 태그 생성/수정은 관리자만 가능
- 퀴즈당 최대 10개 태그 제한
- 태그 사용량 추적 및 통계
- 성능 최적화를 위한 인덱싱

### ⚖️ 설계 결정사항

#### 1. 계층구조 필드 추가
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Tag parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Tag> children = new ArrayList<>();

@Column(nullable = false)
private int level = 0;
```

#### 2. 비즈니스 제약사항 상수화
```java
public static final int MAX_HIERARCHY_LEVEL = 3;
public static final int MAX_TAGS_PER_QUIZ = 10;
```

#### 3. 성능 최적화 인덱스
```java
@Index(name = "idx_tag_parent_id", columnList = "parent_id"),
@Index(name = "idx_tag_level", columnList = "level"),
@Index(name = "idx_tag_name", columnList = "name")
```

#### 4. 계층구조 탐색 메서드
- `getAncestors()`: Stream API를 활용한 조상 탐색
- `getDescendants()`: 재귀적 후손 탐색
- `getFullPath()`: 전체 경로 문자열 생성

### ✅ 최종 설계 특징

**1. Adjacency List 패턴 적용**
- parent_id를 통한 부모 참조
- level 필드로 깊이 사전 계산
- 양방향 관계로 탐색 최적화

**2. 도메인 규칙 강화**
- 생성자/수정자에서 유효성 검증
- 계층 깊이 제한 검사
- 태그 수 제한 검사 (Quiz 엔티티와 협력)

**3. 함수형 프로그래밍 활용**
- Stream API를 통한 계층 탐색
- Optional을 활용한 안전한 참조
- 람다 표현식으로 간결한 로직

**4. 사용량 추적**
- usageCount 필드로 인기도 측정
- 태그 추가/제거 시 자동 업데이트

### 🎯 선택 근거
1. **확장성**: 향후 태그 기반 추천/분석 기능 확장 용이
2. **성능**: 인덱스와 level 필드로 계층 조회 최적화
3. **유지보수성**: 도메인 로직 캡슐화로 변경 영향 최소화
4. **타입 안전성**: 컴파일 타임 검증으로 런타임 오류 방지

### 📊 예상 성능 영향
- 계층 조회: O(depth) ≈ O(3) = 상수 시간
- 태그 추가: O(1) + 유효성 검증
- 전체 경로 계산: O(depth) = 최대 3번 순회
- 후손 탐색: O(children) = 각 레벨별 자식 수

---

## TDR-004: TagRepository 계층구조 쿼리 설계

### 📅 결정 날짜
2025-06-13

### 🎯 문제 상황
계층구조 태그의 효율적인 데이터 액세스를 위한 Repository 메서드와 쿼리 전략 설계 필요

### 🔍 구현된 주요 기능

#### 1. 계층구조 탐색 쿼리
```sql
-- 재귀 CTE를 활용한 후손 태그 조회
WITH RECURSIVE tag_descendants AS (
    SELECT * FROM tags WHERE parent_id = :tagId
    UNION ALL
    SELECT t.* FROM tags t
    INNER JOIN tag_descendants td ON t.parent_id = td.id
)
```

#### 2. 성능 최적화 인덱스
- `idx_tag_parent_id`: 부모-자식 관계 탐색 최적화
- `idx_tag_level`: 레벨별 필터링 최적화  
- `idx_tag_name`: 이름 검색 최적화

#### 3. 비즈니스 로직 지원 메서드
- 중복 검사: `existsByNameAndParent`
- 삭제 가능성 검사: `existsByParentId`, `hasConnectedQuizzes`
- 통계 쿼리: `getTagQuizCounts`

### ✅ 최종 설계 특징

**1. 함수형 프로그래밍 적용**
- Optional 반환으로 안전한 조회
- Stream-friendly 메서드 명명

**2. 쿼리 최적화**
- 재귀 CTE 활용으로 단일 쿼리로 계층 탐색
- JPQL과 네이티브 SQL 혼용으로 성능/가독성 균형

**3. 확장성 고려**
- 통계 쿼리 분리로 성능 부담 최소화
- 페이징 지원을 위한 카운트 쿼리 별도 제공

---

## TDR-005: TagService 비즈니스 로직 설계

### 📅 결정 날짜  
2025-06-13

### 🎯 문제 상황
계층구조 태그의 복잡한 비즈니스 규칙과 관리자 권한 체크를 포함한 서비스 계층 설계 필요

### 🔍 핵심 비즈니스 규칙

#### 1. 계층구조 무결성 보장
- 최대 3단계 깊이 제한
- 순환 참조 방지 (자기 자신이나 후손을 부모로 설정 금지)
- 같은 부모 하위에서 이름 중복 방지

#### 2. 삭제 정책
- 하위 태그가 있으면 삭제 불가
- 연결된 퀴즈가 있으면 삭제 불가
- 카스케이드 삭제 대신 명시적 체크

#### 3. 권한 관리
- 모든 CUD 작업은 관리자 권한 필요
- 조회 작업은 권한 불필요 (활성 태그만)

### ✅ 설계 특징

**1. 함수형 프로그래밍 활용**
```java
// Stream API를 활용한 필터링과 정렬
return allTags.stream()
    .filter(tag -> /* 조건 */)
    .sorted((t1, t2) -> /* 정렬 로직 */)
    .collect(Collectors.toList());
```

**2. 원자적 연산 보장**
- `@Transactional` 적절한 적용
- 검증 로직과 비즈니스 로직 분리

**3. 확장 가능한 아키텍처**
- 인터페이스 기반 설계
- Record 클래스 활용으로 불변 데이터 전달

---

## 작성 가이드라인
각 기술 결정사항은 다음 형식을 따릅니다:
- **TDR-번호**: 연속된 일련번호
- **결정 날짜**: YYYY-MM-DD 형식
- **문제 상황**: 해결해야 할 기술적 문제
- **요구사항**: 만족해야 할 조건들
- **고려된 옵션들**: 검토한 모든 대안
- **최종 결정**: 선택한 방안
- **선택 근거**: 왜 이 방안을 선택했는지 상세 설명
- **최적화/구현 방안**: 구체적인 실행 계획
- **예상 영향**: 성능, 유지보수성 등에 미치는 영향