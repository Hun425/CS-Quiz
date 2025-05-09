# Redis를 활용한 캐시 구현 성과

## 개요 및 목표
- 실제 현업 환경과 유사한 Redis 캐시 구현
- 자주 조회되는 데이터의 성능 개선
- 시스템 부하 감소 및 사용자 경험 향상

## 주요 구현 내용
- **다양한 퀴즈 데이터 유형별 적절한 TTL(Time-To-Live) 설정**
    - 인기 퀴즈: 1시간
    - 일반 퀴즈 상세정보: 15분
    - 검색 결과: 5분
    - 데일리 퀴즈: 24시간

- **@Cacheable, @CacheEvict 어노테이션을 활용한 선언적 캐싱**
    - 퀴즈 조회, 퀴즈 검색, 추천 목록에 캐시 적용
    - 데이터 수정 시 관련 캐시 자동 삭제 처리

- **캐시 키 전략 최적화**
    - 동일 조건에 대한 캐시 히트율 최대화
    - 복합 키(태그ID+제한개수, 검색조건+페이지요청) 사용

## 성능 개선 결과
- **퀴즈 상세 조회**: 응답 시간 약 8.5배 향상 (135ms → 16ms)
- **퀴즈 검색 기능**: 응답 시간 약 6.2배 향상 (210ms → 34ms)
- **인기 퀴즈 추천**: 응답 시간 약 12.7배 향상 (175ms → 14ms)
- **종합 성능 테스트**: 전체 응답 시간 약 9.3배 향상 (583ms → 63ms)

## 실무적 고려사항 반영
- 캐시 일관성 보장을 위한 데이터 수정 시 관련 캐시 자동 삭제
- 서비스 특성에 맞는 적절한 캐시 만료 시간 설정
- 캐시 성능 측정을 위한 테스트 코드 구현
- 로그를 통한 캐시 적중/누락 모니터링 체계 구축