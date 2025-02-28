package com.quizplatform.core.service.battle;

import com.quizplatform.core.domain.battle.BattleAnswer;
import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.UserBattleStats;
import com.quizplatform.core.dto.battle.*;
import com.quizplatform.core.dto.progess.BattleProgress;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.battle.BattleParticipantRepository;
import com.quizplatform.core.repository.battle.BattleRoomRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.user.UserBattleStatsRepository;
import com.quizplatform.core.service.level.LevelingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BattleService {
    private final BattleRoomRepository battleRoomRepository;
    private final BattleParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserBattleStatsRepository userBattleStatsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final LevelingService levelingService;

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * 새로운 대결방 생성
     */
    public BattleRoom createBattleRoom(User creator, Long quizId, Integer maxParticipants) {
        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다."));

        // 대결방 생성
        BattleRoom battleRoom = BattleRoom.builder()
                .quiz(quiz)
                .maxParticipants(maxParticipants != null ? maxParticipants : 4)
                .build();

        // 대결방 설정 유효성 검사
        battleRoom.validateBattleSettings();

        // 대결방 저장
        BattleRoom savedRoom = battleRoomRepository.save(battleRoom);

        // 방장을 첫 참가자로 추가
        addParticipant(savedRoom, creator);

        return savedRoom;
    }

    /**
     * 대결방 조회
     */
    public BattleRoom getBattleRoom(Long roomId) {
        return battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));
    }

    /**
     * 상태별 대결방 조회
     */
    public List<BattleRoom> getBattleRoomsByStatus(BattleRoomStatus status) {
        return battleRoomRepository.findByStatus(status);
    }

    /**
     * 사용자별 활성 대결방 조회
     */
    public BattleRoom getActiveBattleRoomByUser(User user) {
        return battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "활성 대결방이 없습니다."));
    }

    /**
     * 대결방 참가
     */
    public BattleRoom joinBattleRoom(Long roomId, User user) {
        BattleRoom battleRoom = getBattleRoom(roomId);

        // 이미 시작된 대결인지 확인
        if (battleRoom.getStatus() != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작된 대결방입니다.");
        }

        // 정원 초과 확인
        if (battleRoom.isParticipantLimitReached()) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL, "대결방이 가득 찼습니다.");
        }

        // 이미 참가 중인지 확인
        if (participantRepository.existsByBattleRoomAndUser(battleRoom, user)) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING, "이미 참가 중인 사용자입니다.");
        }

        // 참가자 추가
        addParticipant(battleRoom, user);

        return battleRoom;
    }

    /**
     * 준비 상태 토글
     */
    public BattleRoom toggleReady(Long roomId, User user) {
        BattleRoom battleRoom = getBattleRoom(roomId);

        // 참가자 조회
        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다."));

        // 준비 상태 토글
        participant.toggleReady();
        participantRepository.save(participant);

        // 모든 참가자가 준비 완료되었는지 확인하고 자동 시작
        if (isReadyToStart(roomId)) {
            startBattle(roomId);
        }

        return battleRoom;
    }

    /**
     * 대결방 나가기
     */
    public BattleRoom leaveBattleRoom(Long roomId, User user) {
        BattleRoom battleRoom = getBattleRoom(roomId);

        // 이미 시작된 대결인지 확인
        if (battleRoom.getStatus() == BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "진행 중인 대결에서는 나갈 수 없습니다.");
        }

        // 참가자 조회
        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다."));

        // 참가자 제거
        battleRoom.getParticipants().remove(participant);
        participantRepository.delete(participant);

        // 참가자가 없으면 대결방 삭제
        if (battleRoom.getParticipants().isEmpty()) {
            battleRoomRepository.delete(battleRoom);
            return null;
        }

        return battleRoom;
    }

    /**
     * 대결방 입장 처리 (WebSocket)
     */
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        // 대결방 조회
        BattleRoom room = battleRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 참가자 생성 및 저장
        BattleParticipant participant = addParticipant(room, user);

        // Redis에 참가자 정보 저장
        saveParticipantToRedis(participant, sessionId);

        // 응답 생성
        return createBattleJoinResponse(room, participant);
    }

    /**
     * 답변 처리 (WebSocket)
     */
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        // 현재 배틀룸의 현재 문제를 조회
        Question currentQuestion = participant.getBattleRoom().getCurrentQuestion();

        // 요청으로 전달된 질문 ID와 현재 문제의 ID가 일치하는지 검증
        if (currentQuestion == null || !currentQuestion.getId().equals(request.getQuestionId())) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        // 답변 제출 및 점수 계산
        BattleAnswer answer = participant.submitAnswer(
                currentQuestion,
                request.getAnswer(),
                request.getTimeSpentSeconds()
        );

        // 결과 저장
        participantRepository.save(participant);

        return createBattleAnswerResponse(answer);
    }

    /**
     * 대결 시작 준비 상태 확인
     */
    public boolean isReadyToStart(Long roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.isReadyToStart();
    }

    /**
     * 대결 시작 처리
     */
    public BattleStartResponse startBattle(Long roomId) {
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
    public BattleNextQuestionResponse prepareNextQuestion(Long roomId) {
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
    public boolean allParticipantsAnswered(Long roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.allParticipantsAnswered();
    }

    /**
     * 대결 진행 상황 조회
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return createBattleProgressResponse(room);
    }

    /**
     * 대결 종료 처리
     */
    public BattleEndResponse endBattle(Long roomId) {
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

    // 내부 도우미 메서드들

    /**
     * 참가자 추가
     */
    private BattleParticipant addParticipant(BattleRoom battleRoom, User user) {
        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(battleRoom)
                .user(user)
                .build();

        // 참가자에게 UserBattleStats가 없으면 생성
        if (user.getBattleStats() == null) {
            UserBattleStats stats = new UserBattleStats(user);
            userBattleStatsRepository.save(stats);
        }

        return participantRepository.save(participant);
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

        return participantRepository.findById(Long.parseLong(participantId))
                .orElse(null);
    }

    /**
     * 대결방 입장 응답 생성
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
     */
    private BattleProgressResponse createBattleProgressResponse(BattleRoom room) {
        Map<Long, BattleProgressResponse.ParticipantProgress> participantProgress = new HashMap<>();

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
     */
    private BattleResult calculateBattleResult(BattleRoom room) {
        List<BattleParticipant> sortedParticipants = room.getParticipants().stream()
                .sorted(Comparator.comparingInt(BattleParticipant::getCurrentScore).reversed())
                .collect(Collectors.toList());

        BattleParticipant winner = sortedParticipants.isEmpty() ? null : sortedParticipants.get(0);
        int highestScore = winner != null ? winner.getCurrentScore() : 0;

        return BattleResult.builder()
                .roomId(room.getId())
                .winner(winner)
                .participants(sortedParticipants)
                .highestScore(highestScore)
                .startTime(room.getStartTime())
                .endTime(LocalDateTime.now())
                .totalTimeSeconds(room.getTotalTimeSeconds())
                .battleRoom(room)
                .build();
    }

    /**
     * 경험치 보상 지급
     */
    private void awardExperiencePoints(BattleResult result) {
        if (result.getWinner() == null) return;

        // 승자에게 경험치 부여
        levelingService.calculateBattleExp(result, result.getWinner().getUser());

        // 다른 참가자들에게 경험치 부여
        result.getParticipants().stream()
                .filter(p -> !p.equals(result.getWinner()))
                .forEach(participant -> {
                    levelingService.calculateBattleExp(result, participant.getUser());
                });
    }

    /**
     * 통계 업데이트
     */
    private void updateStatistics(BattleResult result) {
        // 사용자 통계 업데이트
        result.getParticipants().forEach(participant -> {
            UserBattleStats stats = participant.getUser().getBattleStats();
            if (stats != null) {
                stats.updateStats(participant);
                userBattleStatsRepository.save(stats);
            }
        });

        // 퀴즈 통계 업데이트
        Quiz quiz = result.getBattleRoom().getQuiz();
        quiz.updateBattleStats(result);
        quizRepository.save(quiz);
    }

    /**
     * 대결 종료 응답을 생성
     */
    private BattleEndResponse createBattleEndResponse(BattleResult result) {
        List<BattleEndResponse.ParticipantResult> participantResults = result.getParticipants().stream()
                .map(participant -> {
                    Map<Long, Boolean> questionResults = participant.getAnswers().stream()
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
     * 참가자의 평균 답변 시간을 계산
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
     * 획득한 경험치를 계산
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