package com.quizplatform.modules.quiz.presentation.dto;

import com.quizplatform.modules.quiz.domain.entity.Question;
import com.quizplatform.modules.quiz.domain.entity.QuestionType;
import com.quizplatform.modules.quiz.domain.entity.DifficultyLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class QuestionResponse {
    private Long id;
    private QuestionType questionType;
    private String questionText;
    private String codeSnippet;
    private String diagramData;
    private List<OptionDto> options; // String에서 OptionDto로 변경
    private String explanation;
    private int points;
    private DifficultyLevel difficultyLevel;
    private int timeLimitSeconds;

    public static QuestionResponse from(Question question) {
        List<OptionDto> optionDtos = convertToOptionDtos(question);

        return QuestionResponse.builder()
                .id(question.getId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .codeSnippet(question.getCodeSnippet())
                .options(question.getOptionDtoList())
                .explanation(question.getExplanation())
                .points(question.getPoints())
                .difficultyLevel(question.getDifficultyLevel())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .build();
    }

    private static List<OptionDto> convertToOptionDtos(Question question) {
        // JSON 문자열에서 직접 OptionDto 객체의 리스트로 파싱 시도
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(question.getOptions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OptionDto.class));
        } catch (Exception e) {
            // 파싱 실패 시 기존 방식으로 처리 시도
            List<String> stringOptions = question.getOptionList();
            List<OptionDto> result = new ArrayList<>();

            for (int i = 0; i < stringOptions.size(); i++) {
                // a, b, c, d... 형태로 키를 생성
                String key = String.valueOf((char)('a' + i));
                result.add(new OptionDto(key, stringOptions.get(i)));
            }

            return result;
        }
    }
}