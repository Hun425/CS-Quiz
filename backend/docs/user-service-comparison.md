# 레거시 UserService와 모듈식 UserService 비교

## 개요
이 문서는 레거시 코드베이스의 `UserService`와 모듈화된 아키텍처의 `UserService` 간의 차이점을 비교 분석합니다.

## 구조적 차이점

### 레거시 UserService
- **위치**: `legacy/main/java/com/quizplatform/core/service/user/UserService.java`
- **타입**: 구현 클래스 (Implementation Class)
- **주요 특징**: 
  - 단일 클래스에 모든 사용자 관련 기능 구현
  - 구체적인 데이터 접근 계층과 직접 연결
  - 다양한 DTO를 사용하여 데이터 전송

### 모듈식 UserService
- **위치**: `modules/user/src/main/java/com/quizplatform/user/domain/service/UserService.java`
- **타입**: 인터페이스 (Interface)
- **주요 특징**: 
  - 도메인 중심 설계
  - 명확한 책임 분리
  - 확장성과 테스트 용이성 제공
  - 구현과 인터페이스의 분리

## 기능 비교

| 기능 | 레거시 UserService | 모듈식 UserService |
|-----|------------------|------------------|
| 사용자 정보 조회 | `getUserInfo()` | `findUserById()`, `findUserByUsername()`, `findUserByEmail()` |
| 통계 조회 | `getUserStatistics()` | 별도 서비스로 분리 예정 |
| 활동 이력 | `getRecentActivities()` | 별도 서비스로 분리 예정 |
| 대전 이력 | `getRecentMatches()` | 별도 서비스로 분리 예정 |
| 업적 관리 | `getUserAchievements()` | 별도 서비스로 분리 예정 |
| 주제별 성능 | `getTopicPerformance()` | 별도 서비스로 분리 예정 |
| 프로필 업데이트 | `updateProfile()` | `updateUserProfile()` |
| 경험치 관리 | 명시적 방법 없음 | `increaseExperience()` |
| 포인트 관리 | 명시적 방법 없음 | `increasePoints()` |
| 유저 상태 관리 | 명시적 방법 없음 | `toggleUserStatus()` |
| 역할 변경 | 명시적 방법 없음 | `changeUserRole()` |

## 주요 코드 차이점

### 레거시 UserService (일부 코드)
```java
public UserInfoDTO getUserInfo(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    
    return UserInfoDTO.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .profileUrl(user.getProfileUrl())
        .level(user.getLevel())
        .experience(user.getExperience())
        .points(user.getPoints())
        .role(user.getRole().name())
        .createdAt(user.getCreatedAt())
        .build();
}
```

### 모듈식 UserService (인터페이스 정의)
```java
/**
 * 사용자를 ID로 찾습니다.
 * @param userId 사용자 ID
 * @return 사용자 정보를 포함한 Optional 객체
 */
Optional<UserProfile> findUserById(UUID userId);
```

## 장단점 분석

### 레거시 UserService
- **장점**:
  - 모든 기능이 단일 클래스에 통합되어 있어 관련 기능을 찾기 쉬움
  - 구체적인 구현으로 인해 바로 사용 가능
  - 데이터 접근 계층과 직접 연결되어 있어 빠른 구현 가능

- **단점**:
  - 단일 책임 원칙 위반 (SRP)
  - 테스트하기 어려움
  - 비즈니스 로직과 데이터 접근 로직의 혼합
  - 확장성 제한

### 모듈식 UserService
- **장점**:
  - 명확한 책임 분리
  - 인터페이스 기반 설계로 테스트 용이성 향상
  - 느슨한 결합으로 모듈 간 의존성 감소
  - 도메인 중심 설계로 비즈니스 규칙 캡슐화

- **단점**:
  - 추가적인 추상화 계층으로 인한 복잡성 증가
  - 기능 구현을 위해 여러 클래스/인터페이스를 이해해야 함
  - 초기 설정에 더 많은 시간 소요

## 마이그레이션 전략

1. **점진적 접근**: 
   - 우선 핵심 사용자 관리 기능만 모듈식 구조로 이전
   - 통계, 활동, 성과 등의 기능은 점진적으로 별도 서비스로 분리

2. **어댑터 패턴 고려**: 
   - 필요한 경우 레거시 코드와 모듈식 코드 사이의 어댑터 구현
   - 새로운 기능은 모듈식 구조에 직접 구현

3. **테스트 우선 접근**: 
   - 각 기능 마이그레이션 전 테스트 케이스 작성
   - 동일한 동작 보장을 위한 통합 테스트 실행

## 결론

모듈식 `UserService`는 도메인 중심 설계와 명확한 책임 분리를 통해 장기적으로 더 유지보수가 용이하고 확장 가능한 구조를 제공합니다. 레거시 코드의 모든 기능을 점진적으로 마이그레이션하면서, 새로운 기능은 모듈식 아키텍처에 맞게 설계하는 전략이 권장됩니다. 