package com.quizplatform.modules.quiz.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.quizplatform.modules.quiz.dto.OptionDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 퀴즈 문제 엔티티 클래스
 * 
 * <p>퀴즈를 구성하는 개별 문제의 정보를 관리합니다. 다양한 유형의 문제(객관식, 주관식 등)를 지원하며,
 * 문제 내용, 정답, 배점, 난이도, 제한 시간 등의 정보를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Entity
@Table(name = "questions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {
    // 기본 시간 제한 상수들
    public static final int DEFAULT_TIME_LIMIT_SECONDS = 60;
    public static final int TIME_LIMIT_SECONDS_MULTIPLE_CHOICE = 60;
    public static final int TIME_LIMIT_SECONDS_TRUE_FALSE = 30;
    public static final int TIME_LIMIT_SECONDS_SHORT_ANSWER = 120;
    public static final int TIME_LIMIT_SECONDS_CODE_ANALYSIS = 300;
    public static final int TIME_LIMIT_SECONDS_DIAGRAM_BASED = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 문제가 속한 퀴즈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    /**
     * 문제 유형 (객관식, 주관식, 참/거짓 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    /**
     * 문제 내용
     */
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    /**
     * 코드 스니펫 (프로그래밍 문제용)
     */
    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    /**
     * 선택지 (객관식 문제용, JSON 형식)
     */
    @Column(columnDefinition = "jsonb")
    private String options;

    /**
     * 정답
     */
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    /**
     * 해설
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * 배점
     */
    private int points = 1;

    /**
     * 문제별 제한 시간 (초 단위)
     */
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    /**
     * 문제 난이도
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    /**
     * 생성 시간
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 문제 생성자
     * 
     * @param questionType 문제 유형
     * @param questionText 문제 내용
     * @param correctAnswer 정답
     * @param explanation 해설
     * @param difficultyLevel 난이도
     * @param points 배점
     */
    @Builder
    public Question(QuestionType questionType, String questionText, String correctAnswer,
                    String explanation, DifficultyLevel difficultyLevel, Integer points) {
        this.questionType = questionType;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.difficultyLevel = difficultyLevel;
        this.points = points != null ? points : 1;
        this.timeLimitSeconds = getDefaultTimeLimitForType(questionType);
    }

    /**
     * 퀴즈 설정 (양방향 관계 설정용)
     * 
     * @param quiz 문제가 속할 퀴즈
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    /**
     * 코드 스니펫 설정 (프로그래밍 문제용)
     * 
     * @param codeSnippet 코드 스니펫
     */
    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    /**
     * 객관식 옵션 설정
     * 
     * @param optionList 선택지 목록
     * @throws IllegalArgumentException JSON 변환 실패 시
     */
    public void setOptions(List<String> optionList) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            this.options = objectMapper.writeValueAsString(optionList);
        } catch (Exception e) {
            throw new IllegalArgumentException("옵션 목록을 JSON으로 변환하는데 실패했습니다.", e);
        }
    }

    /**
     * 선택지 목록 조회 (String 리스트 형태)
     * 
     * @return 선택지 목록
     */
    public List<String> getOptionList() {
        if (options == null) {
            return Collections.emptyList();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // 먼저 JSON 노드로 파싱
            JsonNode jsonNode = objectMapper.readTree(options);

            // 배열인 경우
            if (jsonNode.isArray()) {
                List<String> result = new ArrayList<>();

                // 객체 배열({key, value} 형태)인지 확인
                if (jsonNode.size() > 0 && jsonNode.get(0).isObject() &&
                        jsonNode.get(0).has("value")) {
                    // {key, value} 객체에서 value만 추출
                    for (JsonNode node : jsonNode) {
                        result.add(node.get("value").asText());
                    }
                } else {
                    // 단순 문자열 배열인 경우
                    return objectMapper.readValue(options, new TypeReference<List<String>>() {});
                }
                return result;
            }
            // 기본 처리
            return objectMapper.readValue(options, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 문제 복사 (데일리 퀴즈 생성 등에 사용)
     * 
     * @return 복사된 새 문제 객체
     */
    public Question copy() {
        Question copiedQuestion = Question.builder()
                .questionType(this.questionType)
                .questionText(this.questionText)
                .correctAnswer(this.correctAnswer)
                .explanation(this.explanation)
                .difficultyLevel(this.difficultyLevel)
                .points(this.points)
                .build();

        copiedQuestion.setCodeSnippet(this.codeSnippet);
        if (this.options != null) {
            copiedQuestion.setOptions(this.getOptionList());
        }
        copiedQuestion.timeLimitSeconds = this.timeLimitSeconds;

        return copiedQuestion;
    }

    /**
     * 선택지 목록을 DTO 형태로 변환
     * 
     * @return 선택지 DTO 목록
     */
    public List<OptionDto> getOptionDtoList() {
        if (options == null) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // 먼저 {key, value} 형태로 직접 파싱 시도
            try {
                return objectMapper.readValue(options,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OptionDto.class));
            } catch (Exception e) {
                // 실패하면 문자열 리스트로 파싱 후 변환
                List<String> stringOptions = getOptionList();
                List<OptionDto> result = new ArrayList<>();

                for (int i = 0; i < stringOptions.size(); i++) {
                    // a, b, c, d... 형태로 키를 생성
                    String key = String.valueOf((char)('a' + i));
                    result.add(new OptionDto(key, stringOptions.get(i)));
                }

                return result;
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 원본 options JSON 문자열을 직접 반환
     * 
     * @return options JSON 문자열
     */
    public String getOptions() {
        return this.options;
    }

    /**
     * 사용자 답변의 정답 여부 확인
     * 
     * <p>문제 유형에 따라 다른 판정 로직을 적용합니다.</p>
     * 
     * @param answer 사용자 답변
     * @return 정답 여부
     */
    public boolean isCorrectAnswer(String answer) {
        if (answer == null) {
            return false;
        }

        switch (questionType) {
            case MULTIPLE_CHOICE:
                return correctAnswer.equals(answer.trim());
            case TRUE_FALSE:
                return correctAnswer.equalsIgnoreCase(answer.trim());
            case SHORT_ANSWER:
                // 주관식의 경우 공백과 대소문자를 무시하고 비교
                return correctAnswer.trim().equalsIgnoreCase(answer.trim());
            case CODE_ANALYSIS:
                // 코드 분석 문제는 정확한 일치 필요
                return correctAnswer.equals(answer);
            case DIAGRAM_BASED:
                // 다이어그램 기반 문제도 정확한 일치 필요
                return correctAnswer.equals(answer);
            default:
                return false;
        }
    }

    /**
     * 시간 제한 설정
     * 
     * @param seconds 초 단위 시간 제한
     * @throws IllegalArgumentException 유효 범위(10~600초) 벗어날 경우
     */
    public void setTimeLimit(int seconds) {
        if (seconds < 10 || seconds > 600) {
            throw new IllegalArgumentException("시간 제한은 10초에서 600초 사이여야 합니다.");
        }
        this.timeLimitSeconds = seconds;
    }

    /**
     * 문제 유형별 기본 제한 시간 설정
     * 
     * @param type 문제 유형
     * @return 유형에 따른 기본 제한 시간 (초)
     */
    private int getDefaultTimeLimitForType(QuestionType type) {
        switch (type) {
            case MULTIPLE_CHOICE:
                return TIME_LIMIT_SECONDS_MULTIPLE_CHOICE;
            case TRUE_FALSE:
                return TIME_LIMIT_SECONDS_TRUE_FALSE;
            case SHORT_ANSWER:
                return TIME_LIMIT_SECONDS_SHORT_ANSWER;
            case CODE_ANALYSIS:
                return TIME_LIMIT_SECONDS_CODE_ANALYSIS;
            case DIAGRAM_BASED:
                return TIME_LIMIT_SECONDS_DIAGRAM_BASED;
            default:
                return DEFAULT_TIME_LIMIT_SECONDS;
        }
    }

    /**
     * 남은 시간에 따른 보너스 점수 계산
     * 
     * <p>빨리 정답을 맞추면 추가 점수를 부여합니다.</p>
     * 
     * @param secondsRemaining 남은 시간 (초)
     * @return 보너스 점수 (0~3)
     */
    public int calculateTimeBonus(int secondsRemaining) {
        if (secondsRemaining <= 0) {
            return 0;
        }

        double timeRatio = (double) secondsRemaining / this.timeLimitSeconds;
        if (timeRatio >= 0.7) return 3;      // 70% 이상 남았을 때 3점
        if (timeRatio >= 0.5) return 2;      // 50% 이상 남았을 때 2점
        if (timeRatio >= 0.3) return 1;      // 30% 이상 남았을 때 1점
        return 0;
    }

    /**
     * 시간 초과 여부 확인
     * 
     * @param startTime 시작 시간
     * @return 시간 초과면 true, 아니면 false
     */
    public boolean isTimeExpired(LocalDateTime startTime) {
        return LocalDateTime.now().isAfter(startTime.plusSeconds(this.timeLimitSeconds));
    }

    /**
     * 배점 설정
     * 
     * @param points 배점 (1점 이상)
     * @throws IllegalArgumentException 1점 미만일 경우
     */
    public void setPoints(int points) {
        if (points < 1) {
            throw new IllegalArgumentException("포인트는 1점 이상이어야 합니다.");
        }
        this.points = points;
    }
}