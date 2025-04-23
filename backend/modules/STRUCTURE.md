# 모듈형 아키텍처 통일 가이드라인

이 문서는 모든 모듈이 일관성 있는 구조를 유지하기 위한 가이드라인을 제공합니다.

## 폴더 구조

모든 모듈은 다음과 같은 구조를 가져야 합니다:

```
modules/
  ├── [module-name]/
      ├── domain/                  # 도메인 모델과 비즈니스 로직
      │   ├── model/               # 도메인 엔티티 및 값 객체
      │   ├── service/             # 도메인 서비스 (옵션)
      │   └── exception/           # 도메인 관련 예외
      ├── application/             # 애플리케이션 계층
      │   ├── port/                # 포트 인터페이스
      │   │   ├── in/              # 인바운드 포트 (유스케이스)
      │   │   └── out/             # 아웃바운드 포트 (리포지토리 인터페이스)
      │   └── service/             # 유스케이스 구현체
      └── adapter/                 # 어댑터 계층
          ├── in/                  # 인바운드 어댑터
          │   ├── web/             # 웹 컨트롤러
          │   └── event/           # 이벤트 리스너
          └── out/                 # 아웃바운드 어댑터
              ├── persistence/     # 영속성 어댑터
              │   ├── entity/      # JPA 엔티티
              │   ├── repository/  # JPA 리포지토리
              │   └── mapper/      # 엔티티-도메인 매퍼
              └── event/           # 이벤트 발행자
```

## 팩키지 명명 규칙

각 모듈의 패키지 구조는 다음과 같이 구성합니다:

```
[module-name].domain.model
[module-name].domain.service
[module-name].domain.exception
[module-name].application.port.in
[module-name].application.port.out
[module-name].application.service
[module-name].adapter.in.web
[module-name].adapter.in.event
[module-name].adapter.out.persistence.entity
[module-name].adapter.out.persistence.repository
[module-name].adapter.out.persistence.mapper
[module-name].adapter.out.event
```

## 의존성 규칙

1. 도메인 계층은 어떤 외부 계층에도 의존하지 않습니다.
2. 애플리케이션 계층은 도메인 계층에만 의존합니다.
3. 어댑터 계층은 애플리케이션 계층의 포트와 도메인 계층에 의존할 수 있습니다.
4. 외부 계층에서 내부 계층으로의 의존성만 허용됩니다 (adapter → application → domain).

## 모듈 간 통신

모듈 간 통신은 다음 방법 중 하나를 사용합니다:

1. REST API를 통한 동기 통신
2. 이벤트 메시징을 통한 비동기 통신

각 모듈은 자신의 도메인 경계 내에서 완전한 기능을 갖추어야 하며, 다른 모듈과의 의존성은 최소화해야 합니다. 