package com.quizplatform.modules.quiz.infrastructure.repository;

import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import com.quizplatform.modules.quiz.domain.entity.Quiz;
import com.quizplatform.modules.quiz.domain.entity.Tag;
// QuizSubmitRequest 내부의 QuizSearchCondition 사용 가정이지만, DTO 위치 확인 필요
import com.quizplatform.modules.quiz.presentation.dto.QuizSubmitRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Quiz 엔티티에 대한 커스텀 데이터 접근 로직을 정의하는 리포지토리 인터페이스입니다.
 * 복잡한 검색 조건이나 특정 추천 로직 등 JPA 메서드 이름만으로는 정의하기 어려운
 * 쿼리 메서드를 선언합니다. 실제 구현은 별도의 Impl 클래스에서 이루어집니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface CustomQuizRepository {

    /**
     * 다양한 검색 조건을 이용하여 퀴즈를 검색하고 페이징 처리된 결과를 반환합니다.
     * 검색 조건은 QuizSearchCondition 객체에 정의된 필드(예: 키워드, 태그, 난이도 등)를 사용합니다.
     *
     * @param condition 검색 조건을 담고 있는 객체 (QuizSubmitRequest.QuizSearchCondition 또는 별도 DTO)
     * @param pageable  페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 검색 조건에 맞는 Quiz 엔티티 페이지 객체
     */
    Page<Quiz> search(QuizSubmitRequest.QuizSearchCondition condition, Pageable pageable);
    // 참고: QuizSearchCondition 클래스가 QuizSubmitRequest 내부에 정의되어 있지 않다면,
    // 해당 클래스의 정확한 경로로 수정해야 합니다. (예: com.quizplatform.core.dto.quiz.QuizSearchCondition)

    /**
     * 주어진 태그 목록 및 난이도를 기반으로 추천 퀴즈 목록을 조회합니다.
     * 추천 로직은 구현 클래스에서 정의되며, 일반적으로 관련성 높은 퀴즈를 찾아 반환합니다.
     *
     * @param tags       추천 기준이 되는 태그(Tag) 객체 Set
     * @param difficulty 추천 기준이 되는 난이도(DifficultyLevel)
     * @param limit      조회할 최대 퀴즈 개수
     * @return 추천된 Quiz 엔티티 리스트
     */
    List<Quiz> findRecommendedQuizzes(Set<Tag> tags, DifficultyLevel difficulty, int limit);
}