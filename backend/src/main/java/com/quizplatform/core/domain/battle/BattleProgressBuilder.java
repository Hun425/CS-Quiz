package com.quizplatform.core.domain.battle;


import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.dto.progess.BattleProgress;
import com.quizplatform.core.dto.progess.ParticipantProgress;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// BattleProgress를 생성하는 빌더 클래스
@RequiredArgsConstructor
public class BattleProgressBuilder {
    private final BattleRoom battleRoom;

    public BattleProgress build() {
        // 참가자별 진행 상황 계산
        Map<UUID, ParticipantProgress> participantProgresses = new HashMap<>();
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

    private int getCurrentQuestionPoints(BattleRoom battleRoom) {
        Question currentQuestion = battleRoom.getCurrentQuestion();
        return currentQuestion != null ? currentQuestion.getPoints() : 0;
    }

    private Duration calculateAverageAnswerTime(BattleRoom battleRoom) {
        return battleRoom.getParticipants().stream()
                .map(BattleParticipant::getAverageAnswerTime)
                .reduce(Duration.ZERO, Duration::plus)
                .dividedBy(battleRoom.getParticipants().size());
    }
}