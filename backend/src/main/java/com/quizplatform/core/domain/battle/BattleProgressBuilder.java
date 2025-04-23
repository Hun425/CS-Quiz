package com.quizplatform.core.domain.battle;

import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.dto.progess.BattleProgress;
import com.quizplatform.core.dto.progess.ParticipantProgress;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 배틀 진행 상황 빌더 클래스
 * 
 * <p>배틀 방의 현재 상태를 기반으로 배틀 진행 상황(BattleProgress) 객체를 생성합니다.
 * 참가자별 진행 상황, 전체 통계 등을 수집하여 DTO로 변환합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@RequiredArgsConstructor
public class BattleProgressBuilder {
    /**
     * 진행 상황을 빌드할 배틀 방
     */
    private final BattleRoom battleRoom;

    /**
     * 배틀 진행 상황 객체 생성
     * 
     * @return 생성된 배틀 진행 상황 객체
     */
    public BattleProgress build() {
        // 참가자별 진행 상황 계산
        Map<Long, ParticipantProgress> participantProgresses = new HashMap<>();
        for (BattleParticipant participant : battleRoom.getParticipants()) {
            participantProgresses.put(
                    participant.getId(),
                    createParticipantProgress(participant)
            );
        }

        return BattleProgress.builder()
                .battleRoomId(battleRoom.getId())
                .currentQuestionIndex(battleRoom.getCurrentQuestionIndex())
                .totalQuestions(battleRoom.getQuiz().getQuestions().size())
                .remainingTime(battleRoom.getRemainingTimeForCurrentQuestion())
                .participantProgresses(participantProgresses)
                .status(battleRoom.getStatus())
                .currentQuestionPoints(getCurrentQuestionPoints(battleRoom))
                .allParticipantsAnswered(battleRoom.allParticipantsAnswered())
                .averageAnswerTime(calculateAverageAnswerTime(battleRoom))
                .build();
    }

    /**
     * 참가자별 진행 상황 객체 생성
     * 
     * @param participant 진행 상황을 생성할 참가자
     * @return 생성된 참가자 진행 상황 객체
     */
    private ParticipantProgress createParticipantProgress(BattleParticipant participant) {
        List<BattleAnswer> answers = participant.getAnswers();

        return ParticipantProgress.builder()
                .participantId(participant.getId())
                .username(participant.getUser().getUsername())
                .currentScore(participant.getCurrentScore())
                .correctAnswers((int) answers.stream().filter(BattleAnswer::isCorrect).count())
                .totalAnswered(answers.size())
                .hasAnsweredCurrent(participant.hasAnsweredCurrentQuestion(
                        battleRoom.getCurrentQuestionIndex()))
                .currentStreak(participant.getCurrentStreak())
                .correctRate(participant.getAccuracy())
                .averageAnswerTime(participant.getAverageAnswerTime())
                .build();
    }

    /**
     * 현재 문제의 배점 조회
     * 
     * @param battleRoom 배틀 방
     * @return 현재 문제의 배점, 없으면 0
     */
    private int getCurrentQuestionPoints(BattleRoom battleRoom) {
        Question currentQuestion = battleRoom.getCurrentQuestion();
        return currentQuestion != null ? currentQuestion.getPoints() : 0;
    }

    /**
     * 모든 참가자의 평균 답변 시간 계산
     * 
     * @param battleRoom 배틀 방
     * @return 전체 참가자의 평균 답변 시간
     */
    private Duration calculateAverageAnswerTime(BattleRoom battleRoom) {
        return battleRoom.getParticipants().stream()
                .map(BattleParticipant::getAverageAnswerTime)
                .reduce(Duration.ZERO, Duration::plus)
                .dividedBy(battleRoom.getParticipants().size());
    }
}