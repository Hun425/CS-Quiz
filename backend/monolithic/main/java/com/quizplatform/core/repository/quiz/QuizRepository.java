package com.quizplatform.core.repository.quiz;

/**
 * Quiz 엔티티에 대한 통합 데이터 접근 인터페이스
 * 
 * 리팩토링된 구조:
 * - QuizBasicRepository: 기본 CRUD 및 조회 (JpaRepository 포함)
 * - QuizSearchRepository: 검색 및 필터링  
 * - QuizStatisticsRepository: 통계 및 원자적 업데이트
 * - DailyQuizRepository: 데일리 퀴즈 전용
 * - CustomQuizRepository: 복잡한 동적 쿼리 (QueryDSL)
 * 
 * @author 채기훈
 * @deprecated 새로운 코드에서는 분리된 인터페이스를 직접 사용하세요.
 *             기존 호환성을 위해 모든 기능을 통합한 인터페이스입니다.
 */
@Deprecated
public interface QuizRepository extends 
        QuizBasicRepository, 
        QuizSearchRepository, 
        QuizStatisticsRepository, 
        DailyQuizRepository, 
        CustomQuizRepository {
    
    // 모든 메서드는 상위 인터페이스들에서 상속받습니다.
    // 새로운 메서드 추가 시에는 적절한 분리된 인터페이스에 추가하세요.
}