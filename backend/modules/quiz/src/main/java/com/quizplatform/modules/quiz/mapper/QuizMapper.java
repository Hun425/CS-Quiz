package com.quizplatform.modules.quiz.mapper;

import com.quizplatform.core.mapper.GenericIdMapper;
import com.quizplatform.modules.quiz.domain.Quiz;
import com.quizplatform.modules.quiz.dto.QuizDetailResponse;
import com.quizplatform.modules.quiz.dto.QuizRequest;
import com.quizplatform.modules.quiz.dto.QuizResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Quiz 관련 매퍼 인터페이스
 * <p>
 * Quiz 엔티티와 Quiz 관련 DTO 간의 변환을 정의합니다.
 * </p>
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {QuestionMapper.class})
public interface QuizMapper extends GenericIdMapper<QuizResponse, Quiz, Long> {

    /**
     * Quiz 엔티티를 QuizResponse DTO로 변환합니다.
     * 
     * @param quiz Quiz 엔티티
     * @return QuizResponse DTO
     */
    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "timeLimit", source = "timeLimit")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "questionCount", expression = "java(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0)")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "attemptCount", source = "attemptCount")
    @Mapping(target = "averageScore", source = "averageScore")
    QuizResponse toDto(Quiz quiz);

    /**
     * Quiz 엔티티를 QuizDetailResponse DTO로 변환합니다.
     * 
     * @param quiz Quiz 엔티티
     * @return QuizDetailResponse DTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "timeLimit", source = "timeLimit")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "attemptCount", source = "attemptCount")
    @Mapping(target = "averageScore", source = "averageScore")
    @Mapping(target = "questions", source = "questions")
    QuizDetailResponse toDetailDto(Quiz quiz);

    /**
     * QuizRequest DTO를 Quiz 엔티티로 변환합니다.
     * 
     * @param quizRequest QuizRequest DTO
     * @return Quiz 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "timeLimit", source = "timeLimit")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "attemptCount", ignore = true)
    @Mapping(target = "averageScore", ignore = true)
    Quiz toEntity(QuizRequest quizRequest);

    /**
     * QuizRequest DTO로 기존 Quiz 엔티티를 업데이트합니다.
     * 
     * @param quizRequest 업데이트할 정보가 담긴 QuizRequest DTO
     * @param quiz 업데이트될 Quiz 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "difficulty", source = "difficulty")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "timeLimit", source = "timeLimit")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "questions", source = "questions")
    @Mapping(target = "attemptCount", ignore = true)
    @Mapping(target = "averageScore", ignore = true)
    void updateFromDto(QuizRequest quizRequest, @MappingTarget Quiz quiz);
}