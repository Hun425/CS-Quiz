package com.quizplatform.core.service.common;

import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.battle.BattleEndResponse;
import com.quizplatform.core.dto.battle.BattleResult;
import com.quizplatform.core.dto.battle.BattleRoomResponse;
import com.quizplatform.core.dto.question.QuestionAttemptDto;
import com.quizplatform.core.dto.quiz.QuizDetailResponse;
import com.quizplatform.core.dto.quiz.QuizResponse;
import com.quizplatform.core.dto.quiz.QuizResultResponse;
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;
import com.quizplatform.core.dto.user.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EntityMapperService {


    @Transactional(readOnly = true)
    public QuizDetailResponse mapToQuizDetailResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        return QuizDetailResponse.from(quiz);
    }

    @Transactional(readOnly = true)
    public QuizSummaryResponse mapToQuizSummaryResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화 (요약 정보에만 필요한 것들)
        quiz.getTags().size();
        if (quiz.getCreator() != null) {
            quiz.getCreator().getUsername();
        }
        return QuizSummaryResponse.from(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> mapToQuizSummaryResponseList(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::mapToQuizSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BattleRoomResponse mapToBattleRoomResponse(BattleRoom battleRoom) {
        // 필요한 연관 관계 초기화
        battleRoom.getParticipants().forEach(participant -> {
            participant.getUser().getUsername(); // 사용자 정보 초기화
            participant.getUser().getProfileImage();
            participant.getUser().getLevel();
        });
        battleRoom.getQuiz().getTitle(); // 퀴즈 정보 초기화
        battleRoom.getQuiz().getTimeLimit();
        battleRoom.getQuiz().getQuestions().size();

        return BattleRoomResponse.from(battleRoom);
    }

    @Transactional(readOnly = true)
    public List<BattleRoomResponse> mapToBattleRoomResponseList(List<BattleRoom> battleRooms) {
        return battleRooms.stream()
                .map(this::mapToBattleRoomResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileDto mapToUserProfileDto(User user) {
        // 사용자 기본 정보 초기화
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getLevel(),
                user.getExperience(),
                user.getRequiredExperience(),
                user.getTotalPoints(),
                formatDateTime(user.getCreatedAt()),
                user.getLastLogin() != null ? formatDateTime(user.getLastLogin()) : null
        );
    }

    @Transactional(readOnly = true)
    public BattleEndResponse mapToBattleEndResponse(BattleResult result) {
        // BattleResult로부터 BattleEndResponse 생성
        List<BattleEndResponse.ParticipantResult> participantResults = result.getParticipants().stream()
                .map(participant -> {
                    // 각 참가자의 답변 로딩
                    participant.getAnswers().size();

                    Map<Long, Boolean> questionResults = participant.getAnswers().stream()
                            .collect(Collectors.toMap(
                                    answer -> answer.getQuestion().getId(),
                                    answer -> answer.isCorrect()
                            ));

                    return BattleEndResponse.ParticipantResult.builder()
                            .userId(participant.getUser().getId())
                            .username(participant.getUser().getUsername())
                            .finalScore(participant.getCurrentScore())
                            .correctAnswers(participant.getCorrectAnswersCount())
                            .averageTimeSeconds(calculateAverageTime(participant))
                            .experienceGained(calculateExperienceGained(participant, result))
                            .isWinner(participant.equals(result.getWinner()))
                            .questionResults(questionResults)
                            .build();
                })
                .collect(Collectors.toList());

        return BattleEndResponse.builder()
                .roomId(result.getRoomId())
                .results(participantResults)
                .totalQuestions(result.getTotalQuestions())
                .timeTakenSeconds(result.getTotalTimeSeconds())
                .endTime(result.getEndTime())
                .build();
    }

    @Transactional(readOnly = true)
    public List<QuestionAttemptDto> mapToQuestionAttemptDtoList(List<QuestionAttempt> questionAttempts) {
        return questionAttempts.stream()
                .map(this::mapToQuestionAttemptDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionAttemptDto mapToQuestionAttemptDto(QuestionAttempt attempt) {
        // 필요한 연관 관계 초기화
        attempt.getQuestion().getQuestionText();
        attempt.getQuestion().getExplanation();

        return QuestionAttemptDto.from(attempt);
    }

    @Transactional(readOnly = true)
    public QuizResultResponse mapToQuizResultResponse(QuizAttempt quizAttempt, int experienceGained) {
        // QuizResultResponse 생성에 필요한 초기화 작업
        Quiz quiz = quizAttempt.getQuiz();
        quiz.getQuestions().size();

        // 문제별 결과 생성
        List<QuizResultResponse.QuestionResultDto> questionResults = new ArrayList<>();
        quizAttempt.getQuestionAttempts().forEach(qa -> {
            // 각 질문과 답변 로딩
            qa.getQuestion().getQuestionText();
            qa.getQuestion().getCorrectAnswer();
            qa.getQuestion().getExplanation();

            questionResults.add(
                    QuizResultResponse.QuestionResultDto.builder()
                            .id(qa.getQuestion().getId())
                            .questionText(qa.getQuestion().getQuestionText())
                            .yourAnswer(qa.getUserAnswer())
                            .correctAnswer(qa.getQuestion().getCorrectAnswer())
                            .isCorrect(qa.isCorrect())
                            .explanation(qa.getQuestion().getExplanation())
                            .points(qa.getQuestion().getPoints())
                            .build()
            );
        });

        // 총 가능 점수 계산
        int totalPossibleScore = quiz.getQuestions().stream()
                .mapToInt(q -> q.getPoints())
                .sum();

        // 결과 응답 생성
        return QuizResultResponse.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .totalQuestions(quiz.getQuestions().size())
                .correctAnswers((int) quizAttempt.getQuestionAttempts().stream()
                        .filter(QuestionAttempt::isCorrect)
                        .count())
                .score(quizAttempt.getScore())
                .totalPossibleScore(totalPossibleScore)
                .timeTaken(quizAttempt.getTimeTaken())
                .completedAt(quizAttempt.getEndTime())
                .experienceGained(experienceGained)
                .newTotalExperience(quizAttempt.getUser().getExperience())
                .questions(questionResults)
                .build();
    }

    // 헬퍼 메서드
    private void initializeQuizAssociations(Quiz quiz) {
        quiz.getQuestions().size(); // 질문 컬렉션 초기화
        quiz.getTags().size(); // 태그 컬렉션 초기화
        if (quiz.getCreator() != null) {
            quiz.getCreator().getUsername(); // 생성자 정보 초기화
        }
    }

    private String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private int calculateAverageTime(BattleParticipant participant) {
        List<com.quizplatform.core.domain.battle.BattleAnswer> answers = participant.getAnswers();
        if (answers.isEmpty()) {
            return 0;
        }
        int totalTime = answers.stream()
                .mapToInt(answer -> answer.getTimeTaken())
                .sum();
        return totalTime / answers.size();
    }

    private int calculateExperienceGained(BattleParticipant participant, BattleResult result) {
        // 기본 경험치 (점수의 10%)
        int baseExp = participant.getCurrentScore() / 10;

        // 승리 보너스
        if (participant.equals(result.getWinner())) {
            baseExp *= 1.5;  // 승리 시 50% 추가 경험치
        }

        // 정답률 보너스
        double correctRate = (double) participant.getCorrectAnswersCount() / result.getTotalQuestions();
        if (correctRate >= 0.8) {
            baseExp += 50;  // 80% 이상 정답 시 추가 보너스
        }

        return baseExp;
    }

    @Transactional(readOnly = true)
    public QuizResponse mapToQuizResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        return QuizResponse.from(quiz);
    }

    /**
     * Quiz 엔티티로부터 QuizResponse를 생성하며, 퀴즈 시도 ID를 추가로 설정합니다.
     */
    @Transactional(readOnly = true)
    public QuizResponse mapToQuizResponseWithAttemptId(Quiz quiz, Long quizAttemptId) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        return QuizResponse.from(quiz).withQuizAttemptId(quizAttemptId);
    }
}