package com.quizplatform.core.service.battle;

import com.quizplatform.core.domain.battle.BattleAnswer;
import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserBattleStats;
import com.quizplatform.core.dto.battle.*;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.battle.BattleParticipantRepository;
import com.quizplatform.core.repository.battle.BattleRoomRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.user.UserBattleStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final BattleRoomRepository battleRoomRepository;
    private final BattleParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserBattleStatsRepository userBattleStatsRepository;
    private final RedisTemplate<String, String> redisTemplate;


    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * 대결방 입장 처리
     * Redis를 활용하여 실시간 참가자 상태를 관리합니다.
     */
    @Transactional
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        // 대결방 조회
        BattleRoom room = battleRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 참가자 생성 및 저장
        BattleParticipant participant = room.addParticipant(user);
        participant = participantRepository.save(participant);

        // Redis에 참가자 정보 저장
        saveParticipantToRedis(participant, sessionId);

        // 응답 생성
        return createBattleJoinResponse(room, participant);
    }

    /**
     * 답변 처리
     * 답변의 정확성을 검증하고 점수를 계산합니다.
     */
    @Transactional
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        // 현재 배틀룸의 현재 문제를 조회합니다.
        Question currentQuestion = participant.getBattleRoom().getCurrentQuestion();

        // 요청으로 전달된 질문 ID와 현재 문제의 ID가 일치하는지 검증 (원하는 검증 로직에 따라 수정 가능)
        if (currentQuestion == null || !currentQuestion.getId().equals(request.getQuestionId())) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        // 답변 제출 및 점수 계산: 이제 Question 객체를 전달합니다.
        BattleAnswer answer = participant.submitAnswer(
                currentQuestion,
                request.getAnswer(),
                request.getTimeSpentSeconds()
        );

        // 결과 저장
        participantRepository.save(participant);


        // 결과 저장
        participantRepository.save(participant);

        return createBattleAnswerResponse(answer);
    }

    /**
     * 대결 시작 준비 상태 확인
     */
    public boolean isReadyToStart(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.isReadyToStart();
    }

    /**
     * 대결 시작 처리
     */
    @Transactional
    public BattleStartResponse startBattle(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대결 시작 상태로 변경
        room.startBattle();
        battleRoomRepository.save(room);

        return createBattleStartResponse(room);
    }

    /**
     * 다음 문제 준비
     */
    @Transactional
    public BattleNextQuestionResponse prepareNextQuestion(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        Question nextQuestion = room.startNextQuestion();
        if (nextQuestion == null) {
            return BattleNextQuestionResponse.builder()
                    .isGameOver(true)
                    .build();
        }

        return createNextQuestionResponse(nextQuestion, room.isLastQuestion());
    }

    /**
     * 모든 참가자의 답변 여부 확인
     */
    public boolean allParticipantsAnswered(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.allParticipantsAnswered();
    }

    /**
     * 대결 진행 상황 조회
     */
    public BattleProgressResponse getBattleProgress(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return createBattleProgressResponse(room);
    }

    /**
     * 대결 종료 처리
     */
    @Transactional
    public BattleEndResponse endBattle(UUID roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대결 종료 및 결과 계산
        room.finishBattle();
        BattleResult result = calculateBattleResult(room);

        // 경험치 부여 및 통계 업데이트
        awardExperiencePoints(result);
        updateStatistics(result);

        return createBattleEndResponse(result);
    }

    // Redis 관련 헬퍼 메서드들
    private void saveParticipantToRedis(BattleParticipant participant, String sessionId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(
                participantKey,
                participant.getId().toString(),
                ROOM_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private BattleParticipant getParticipantFromRedis(String sessionId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + sessionId;
        String participantId = redisTemplate.opsForValue().get(participantKey);

        if (participantId == null) {
            return null;
        }

        return participantRepository.findById(UUID.fromString(participantId))
                .orElse(null);
    }

    /**
     * 대결방 입장 응답 생성
     * 현재 참가자 상태와 방 정보를 포함한 응답을 생성합니다.
     */
    private BattleJoinResponse createBattleJoinResponse(BattleRoom room, BattleParticipant newParticipant) {
        List<BattleJoinResponse.ParticipantInfo> participants = room.getParticipants().stream()
                .map(participant -> BattleJoinResponse.ParticipantInfo.builder()
                        .userId(participant.getUser().getId())
                        .username(participant.getUser().getUsername())
                        .profileImage(participant.getUser().getProfileImage())
                        .level(participant.getUser().getLevel())
                        .isReady(participant.isReady())
                        .build())
                .collect(Collectors.toList());

        return BattleJoinResponse.builder()
                .roomId(room.getId())
                .userId(newParticipant.getUser().getId())
                .username(newParticipant.getUser().getUsername())
                .currentParticipants(room.getParticipants().size())
                .maxParticipants(room.getMaxParticipants())
                .participants(participants)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 답변 결과 응답 생성
     * 답변의 정확성, 획득 점수, 보너스 점수 등을 포함합니다.
     */
    private BattleAnswerResponse createBattleAnswerResponse(BattleAnswer answer) {
        Question question = answer.getQuestion();

        return BattleAnswerResponse.builder()
                .questionId(question.getId())
                .isCorrect(answer.isCorrect())
                .earnedPoints(answer.getEarnedPoints())
                .timeBonus(answer.getTimeBonus())
                .currentScore(answer.getParticipant().getCurrentScore())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    /**
     * 대결 시작 응답 생성
     * 첫 번째 문제와 참가자 정보를 포함합니다.
     */
    private BattleStartResponse createBattleStartResponse(BattleRoom room) {
        List<BattleStartResponse.ParticipantInfo> participants = room.getParticipants().stream()
                .map(participant -> BattleStartResponse.ParticipantInfo.builder()
                        .userId(participant.getUser().getId())
                        .username(participant.getUser().getUsername())
                        .profileImage(participant.getUser().getProfileImage())
                        .level(participant.getUser().getLevel())
                        .build())
                .collect(Collectors.toList());

        Question firstQuestion = room.getCurrentQuestion();
        BattleNextQuestionResponse firstQuestionResponse = createNextQuestionResponse(
                firstQuestion,
                room.getQuestions().size() == 1
        );

        return BattleStartResponse.builder()
                .roomId(room.getId())
                .participants(participants)
                .totalQuestions(room.getQuestions().size())
                .timeLimit(room.getCurrentQuestionTimeLimit())
                .startTime(room.getStartTime())
                .firstQuestion(firstQuestionResponse)
                .build();
    }

    /**
     * 다음 문제 응답 생성
     * 문제 내용과 선택지, 제한시간 등을 포함합니다.
     */
    private BattleNextQuestionResponse createNextQuestionResponse(Question question, boolean isLast) {
        return BattleNextQuestionResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .options(question.getOptionList())
                .timeLimit(question.getTimeLimitSeconds())
                .points(question.getPoints())
                .isLastQuestion(isLast)
                .isGameOver(false)
                .build();
    }

    /**
     * 대결 진행 상황 응답 생성
     * 현재 진행 상태와 참가자들의 점수 현황을 포함합니다.
     */
    private BattleProgressResponse createBattleProgressResponse(BattleRoom room) {
        Map<UUID, BattleProgressResponse.ParticipantProgress> participantProgress = new HashMap<>();

        room.getParticipants().forEach(participant -> {
            participantProgress.put(
                    participant.getUser().getId(),
                    BattleProgressResponse.ParticipantProgress.builder()
                            .userId(participant.getUser().getId())
                            .username(participant.getUser().getUsername())
                            .currentScore(participant.getCurrentScore())
                            .correctAnswers(participant.getCorrectAnswersCount())
                            .hasAnsweredCurrent(participant.hasAnsweredCurrentQuestion())
                            .currentStreak(participant.getCurrentStreak())
                            .build()
            );
        });

        return BattleProgressResponse.builder()
                .roomId(room.getId())
                .currentQuestionIndex(room.getCurrentQuestionIndex())
                .totalQuestions(room.getQuestions().size())
                .remainingTimeSeconds(room.getRemainingTimeSeconds())
                .participantProgress(participantProgress)
                .status(BattleStatus.valueOf(room.getStatus().name()))
                .build();
    }

    /**
     * 대결 결과 계산
     * 최종 승자 결정과 통계를 계산합니다.
     */
    private BattleResult calculateBattleResult(BattleRoom room) {
        List<BattleParticipant> sortedParticipants = room.getParticipants().stream()
                .sorted(Comparator.comparingInt(BattleParticipant::getCurrentScore).reversed())
                .collect(Collectors.toList());

        BattleParticipant winner = sortedParticipants.get(0);
        int highestScore = winner.getCurrentScore();

        return BattleResult.builder()
                .roomId(room.getId())
                .winner(winner)
                .participants(sortedParticipants)
                .highestScore(highestScore)
                .startTime(room.getStartTime())
                .endTime(LocalDateTime.now())
                .totalTimeSeconds(room.getTotalTimeSeconds())
                .build();
    }

    /**
     * 경험치 보상 지급
     * 승자와 참가자들에게 경험치를 부여합니다.
     */
    private void awardExperiencePoints(BattleResult result) {
        // 승자에게 추가 경험치 부여
        User winner = result.getWinner().getUser();
        int winnerExp = calculateWinnerExperience(result);
        winner.gainExperience(winnerExp);
        userRepository.save(winner);

        // 다른 참가자들에게 기본 경험치 부여
        result.getParticipants().stream()
                .filter(p -> !p.equals(result.getWinner()))
                .forEach(participant -> {
                    User user = participant.getUser();
                    int exp = calculateParticipantExperience(participant);
                    user.gainExperience(exp);
                    userRepository.save(user);
                });
    }

    /**
     * 승자 경험치 계산
     * 점수, 정답률, 승리 보너스를 고려합니다.
     */
    private int calculateWinnerExperience(BattleResult result) {
        BattleParticipant winner = result.getWinner();

        // 기본 경험치 (점수 기반)
        int baseExp = winner.getCurrentScore();

        // 정답률 보너스
        double correctRate = winner.getCorrectAnswersCount() / (double) result.getTotalQuestions();
        int accuracyBonus = correctRate >= 0.8 ? 50 : 0;

        // 승리 보너스
        int winBonus = 100;

        return baseExp + accuracyBonus + winBonus;
    }

    /**
     * 참가자 경험치 계산
     * 점수와 정답률을 고려합니다.
     */
    private int calculateParticipantExperience(BattleParticipant participant) {
        // 기본 경험치 (점수의 80%)
        int baseExp = (int) (participant.getCurrentScore() * 0.8);

        // 정답률 보너스
        double correctRate = participant.getCorrectAnswersCount() /
                (double) participant.getBattleRoom().getQuestions().size();
        int accuracyBonus = correctRate >= 0.7 ? 30 : 0;

        return baseExp + accuracyBonus;
    }

    /**
     * 통계 업데이트
     * 사용자와 퀴즈의 통계 정보를 업데이트합니다.
     */
    private void updateStatistics(BattleResult result) {
        // 사용자 통계 업데이트
        result.getParticipants().forEach(participant -> {
            UserBattleStats stats = participant.getUser().getBattleStats();
            stats.updateStats(participant);
            userBattleStatsRepository.save(stats);
        });

        // 퀴즈 통계 업데이트
        Quiz quiz = result.getBattleRoom().getQuiz();
        quiz.updateBattleStats(result);
        quizRepository.save(quiz);
    }

    /**
     * 대결 종료 응답을 생성합니다.
     */
    private BattleEndResponse createBattleEndResponse(BattleResult result) {
        List<BattleEndResponse.ParticipantResult> participantResults = result.getParticipants().stream()
                .map(participant -> {
                    Map<UUID, Boolean> questionResults = participant.getAnswers().stream()
                            .collect(Collectors.toMap(
                                    answer -> answer.getQuestion().getId(),
                                    BattleAnswer::isCorrect
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

    /**
     * 참가자의 평균 답변 시간을 계산합니다.
     */
    private int calculateAverageTime(BattleParticipant participant) {
        List<BattleAnswer> answers = participant.getAnswers();
        if (answers.isEmpty()) {
            return 0;
        }
        int totalTime = answers.stream()
                .mapToInt(BattleAnswer::getTimeTaken)
                .sum();
        return totalTime / answers.size();
    }

    /**
     * 획득한 경험치를 계산합니다.
     */
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
}