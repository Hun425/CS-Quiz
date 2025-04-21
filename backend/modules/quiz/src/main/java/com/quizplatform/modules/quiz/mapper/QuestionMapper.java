package com.quizplatform.modules.quiz.mapper;

import com.quizplatform.core.mapper.GenericIdMapper;
import com.quizplatform.modules.quiz.domain.Question;
import com.quizplatform.modules.quiz.dto.QuestionRequest;
import com.quizplatform.modules.quiz.dto.QuestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Question 관련 매퍼 인터페이스
 * <p>
 * Question 엔티티와 Question 관련 DTO 간의 변환을 정의합니다.
 * </p>
 */
@Mapper(componentModel = "spring", 
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AnswerMapper.class})
public interface QuestionMapper extends GenericIdMapper<QuestionResponse, Question, Long> {

    /**
     * Question 엔티티를 QuestionResponse DTO로 변환합니다.
     * 
     * @param question Question 엔티티
     * @return QuestionResponse DTO
     */
    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "points", source = "points")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "explanation", source = "explanation")
    QuestionResponse toDto(Question question);

    /**
     * QuestionRequest DTO를 Question 엔티티로 변환합니다.
     * 
     * @param questionRequest QuestionRequest DTO
     * @return Question 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "points", source = "points")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "explanation", source = "explanation")
    @Mapping(target = "quiz", ignore = true)
    Question toEntity(QuestionRequest questionRequest);

    /**
     * QuestionRequest DTO로 기존 Question 엔티티를 업데이트합니다.
     * 
     * @param questionRequest 업데이트할 정보가 담긴 QuestionRequest DTO
     * @param question 업데이트될 Question 엔티티
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "points", source = "points")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "explanation", source = "explanation")
    @Mapping(target = "quiz", ignore = true)
    void updateFromDto(QuestionRequest questionRequest, @MappingTarget Question question);
}