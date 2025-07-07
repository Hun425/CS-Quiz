package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 배틀 문제 관리 클래스
 * 
 * <p>배틀 진행 중 문제와 관련된 모든 로직을 담당합니다.
 * 문제 순서 관리, 시간 제한 처리, 문제 전환 등을 처리합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
public class BattleQuestionManager {

    /**
     * 다음 문제로 진행
     * 
     * @param quiz 배틀 퀴즈
     * @param currentQuestionIndex 현재 문제 인덱스
     * @param roomId 배틀 방 ID (로깅용)
     * @return 새로운 문제 인덱스와 다음 문제 정보를 담은 결과 객체
     */
    public QuestionTransitionResult startNextQuestion(Quiz quiz, int currentQuestionIndex, Long roomId) {
        List<Question> questions = getQuestions(quiz);
        
        log.info("startNextQuestion 호출 - 시작: roomId={}, 현재 인덱스={}, 문제 목록 크기={}",
                roomId, currentQuestionIndex, questions.size());
        
        if (currentQuestionIndex + 1 >= questions.size()) {
            log.info("더 이상 문제가 없습니다. 게임 종료: roomId={}", roomId);
            return new QuestionTransitionResult(currentQuestionIndex, null, true);
        }
        
        int newQuestionIndex = currentQuestionIndex + 1;
        Question nextQuestion = questions.get(newQuestionIndex);
        
        log.info("다음 문제 선택: roomId={}, 인덱스={} -> {}, 문제ID={}",
                roomId, currentQuestionIndex, newQuestionIndex, nextQuestion.getId());
        
        return new QuestionTransitionResult(newQuestionIndex, nextQuestion, false);
    }

    /**
     * 현재 진행 중인 문제를 반환
     * 
     * @param quiz 배틀 퀴즈
     * @param status 배틀 방 상태
     * @param currentQuestionIndex 현재 문제 인덱스
     * @return 현재 문제, 없으면 null
     * @throws BusinessException 퀴즈가 설정되지 않았거나 인덱스가 유효하지 않을 경우
     */
    public Question getCurrentQuestion(Quiz quiz, BattleRoomStatus status, int currentQuestionIndex) {
        if (status != BattleRoomStatus.IN_PROGRESS) {
            return null;
        }
        
        validateQuizSettings(quiz);
        
        List<Question> questions = getQuestions(quiz);
        
        if (currentQuestionIndex < 0) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, 
                    "잘못된 문제 순서입니다: " + currentQuestionIndex);
        }
        
        if (currentQuestionIndex >= questions.size()) {
            return null;
        }
        
        return questions.get(currentQuestionIndex);
    }

    /**
     * 특정 인덱스의 문제를 반환
     * 
     * @param quiz 배틀 퀴즈
     * @param index 문제 인덱스
     * @return 해당 인덱스의 문제
     * @throws BusinessException 인덱스가 범위를 벗어날 경우
     */
    public Question getQuestionByIndex(Quiz quiz, int index) {
        validateQuizSettings(quiz);
        
        List<Question> questions = getQuestions(quiz);
        
        if (index < 0 || index >= questions.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                    String.format("잘못된 문제 인덱스입니다: %d, 전체 문제 수: %d", index, questions.size()));
        }
        
        return questions.get(index);
    }

    /**
     * 현재 문제의 남은 시간 계산
     * 
     * @param quiz 배틀 퀴즈
     * @param status 배틀 방 상태
     * @param currentQuestionIndex 현재 문제 인덱스
     * @param currentQuestionStartTime 현재 문제 시작 시간
     * @return 남은 시간
     */
    public Duration getRemainingTimeForCurrentQuestion(Quiz quiz, BattleRoomStatus status, 
                                                     int currentQuestionIndex, LocalDateTime currentQuestionStartTime) {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return Duration.ZERO;
        }
        
        Question currentQuestion = getCurrentQuestion(quiz, status, currentQuestionIndex);
        if (currentQuestion == null) {
            return Duration.ZERO;
        }
        
        LocalDateTime deadline = currentQuestionStartTime.plusSeconds(currentQuestion.getTimeLimitSeconds());
        return Duration.between(LocalDateTime.now(), deadline);
    }

    /**
     * 현재 문제의 제한 시간 초과 여부 확인
     * 
     * @param quiz 배틀 퀴즈
     * @param status 배틀 방 상태
     * @param currentQuestionIndex 현재 문제 인덱스
     * @param currentQuestionStartTime 현재 문제 시작 시간
     * @return 시간 초과면 true, 아니면 false
     */
    public boolean isCurrentQuestionTimeExpired(Quiz quiz, BattleRoomStatus status, 
                                              int currentQuestionIndex, LocalDateTime currentQuestionStartTime) {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return true;
        }
        
        Question currentQuestion = getCurrentQuestion(quiz, status, currentQuestionIndex);
        return currentQuestion != null && currentQuestion.isTimeExpired(currentQuestionStartTime);
    }

    /**
     * 현재 문제가 마지막 문제인지 확인
     * 
     * @param quiz 배틀 퀴즈
     * @param currentQuestionIndex 현재 문제 인덱스
     * @return 마지막 문제면 true, 아니면 false
     */
    public boolean isLastQuestion(Quiz quiz, int currentQuestionIndex) {
        return currentQuestionIndex == getQuestions(quiz).size() - 1;
    }

    /**
     * 남은 시간(초)을 계산
     * 
     * @param quiz 배틀 퀴즈
     * @param status 배틀 방 상태
     * @param currentQuestionIndex 현재 문제 인덱스
     * @param currentQuestionStartTime 현재 문제 시작 시간
     * @return 남은 시간(초)
     */
    public int getRemainingTimeSeconds(Quiz quiz, BattleRoomStatus status, 
                                     int currentQuestionIndex, LocalDateTime currentQuestionStartTime) {
        if (status != BattleRoomStatus.IN_PROGRESS || currentQuestionStartTime == null) {
            return 0;
        }
        
        Question currentQuestion = getCurrentQuestion(quiz, status, currentQuestionIndex);
        if (currentQuestion == null) {
            return 0;
        }
        
        long remainingSeconds = Duration.between(
                LocalDateTime.now(),
                currentQuestionStartTime.plusSeconds(currentQuestion.getTimeLimitSeconds())
        ).getSeconds();
        
        return (int) Math.max(0, remainingSeconds);
    }

    /**
     * 현재 문제의 제한 시간 조회
     * 
     * @param quiz 배틀 퀴즈
     * @param status 배틀 방 상태
     * @param currentQuestionIndex 현재 문제 인덱스
     * @return 제한 시간(초)
     * @throws BusinessException 현재 진행 중인 문제가 없을 경우
     */
    public int getCurrentQuestionTimeLimit(Quiz quiz, BattleRoomStatus status, int currentQuestionIndex) {
        Question currentQuestion = getCurrentQuestion(quiz, status, currentQuestionIndex);
        if (currentQuestion == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "현재 진행 중인 문제가 없습니다.");
        }
        return currentQuestion.getTimeLimitSeconds();
    }

    /**
     * 특정 인덱스의 문제 시작 시간 계산
     * 
     * @param quiz 배틀 퀴즈
     * @param questionIndex 문제 인덱스
     * @param battleStartTime 배틀 시작 시간
     * @return 시작 시간
     * @throws BusinessException 유효하지 않은 인덱스일 경우
     */
    public LocalDateTime getQuestionStartTimeForIndex(Quiz quiz, int questionIndex, LocalDateTime battleStartTime) {
        List<Question> questionsList = getQuestions(quiz);
        
        if (questionIndex < 0 || questionIndex >= questionsList.size()) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE);
        }
        
        LocalDateTime baseTime = battleStartTime;
        for (int i = 0; i < questionIndex; i++) {
            Question question = questionsList.get(i);
            baseTime = baseTime.plusSeconds(question.getTimeLimitSeconds());
        }
        
        return baseTime;
    }

    /**
     * 모든 문제 목록을 반환
     * 
     * @param quiz 배틀 퀴즈
     * @return 문제 리스트
     */
    public List<Question> getQuestions(Quiz quiz) {
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
        return new ArrayList<>(quiz.getQuestions());
    }

    /**
     * 퀴즈 설정 유효성 검사
     * 
     * @param quiz 검사할 퀴즈
     * @throws BusinessException 퀴즈가 유효하지 않을 경우
     */
    private void validateQuizSettings(Quiz quiz) {
        if (quiz == null || quiz.getQuestions() == null) {
            throw new BusinessException(ErrorCode.INVALID_BATTLE_SETTINGS, "퀴즈가 설정되지 않았습니다.");
        }
    }

    /**
     * 문제 전환 결과를 담는 내부 클래스
     */
    public static class QuestionTransitionResult {
        private final int newQuestionIndex;
        private final Question nextQuestion;
        private final boolean isFinished;

        public QuestionTransitionResult(int newQuestionIndex, Question nextQuestion, boolean isFinished) {
            this.newQuestionIndex = newQuestionIndex;
            this.nextQuestion = nextQuestion;
            this.isFinished = isFinished;
        }

        public int getNewQuestionIndex() {
            return newQuestionIndex;
        }

        public Question getNextQuestion() {
            return nextQuestion;
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}