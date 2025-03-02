package com.quizplatform.core.service.battle;

import com.quizplatform.core.domain.battle.*;
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
import com.quizplatform.core.service.common.EntityMapperService;
import com.quizplatform.core.service.level.LevelingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final EntityMapperService entityMapperService;

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * 새로운 대결방 생성
     */
    public BattleRoomResponse createBattleRoom(User creator, Long quizId, Integer maxParticipants) {
        // 퀴즈 조회 (with FETCH JOIN)
        Quiz quiz = quizRepository.findByIdWithAllDetails(quizId)
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

        return entityMapperService.mapToBattleRoomResponse(savedRoom);
    }

    /**
     * 대결방 조회
     */
    public BattleRoomResponse getBattleRoom(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 상태별 대결방 조회
     */
    public List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status) {
        List<BattleRoom> rooms = battleRoomRepository.findByStatus(status);

        // N+1 문제를 방지하기 위해 ID 목록으로 한 번에 상세 조회
        List<Long> roomIds = rooms.stream().map(BattleRoom::getId).collect(Collectors.toList());
        List<BattleRoom> detailedRooms = new ArrayList<>();

        for (Long id : roomIds) {
            battleRoomRepository.findByIdWithDetails(id).ifPresent(detailedRooms::add);
        }

        return entityMapperService.mapToBattleRoomResponseList(detailedRooms);
    }

    /**
     * 사용자별 활성 대결방 조회
     */
    public BattleRoomResponse getActiveBattleRoomByUser(User user) {
        BattleRoom room = battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "활성 대결방이 없습니다."));

        // 상세 정보 로드
        room = battleRoomRepository.findByIdWithDetails(room.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "활성 대결방이 없습니다."));

        return entityMapperService.mapToBattleRoomResponse(room);
    }

    /**
     * 대결방 참가
     */
    public BattleRoomResponse joinBattleRoom(Long roomId, User user) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

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

        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 준비 상태 토글
     */
    public BattleRoomResponse toggleReady(Long roomId, User user) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

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

        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 대결방 나가기
     */
    public BattleRoomResponse leaveBattleRoom(Long roomId, User user) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

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

        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 대결방 입장 처리 (WebSocket)
     */
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        // 대결방 조회
        BattleRoom room = battleRoomRepository.findByIdWithDetails(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findByIdWithStats(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 참가자 생성 및 저장
        BattleParticipant participant = room.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseGet(() -> addParticipant(room, user));

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

        // 배틀룸 상세 정보 로드
        BattleRoom battleRoom = battleRoomRepository.findByIdWithAllDetails(participant.getBattleRoom().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 현재 배틀룸의 현재 문제를 조회
        Question currentQuestion = battleRoom.getCurrentQuestion();

        // 요청으로 전달된 질문 ID와 현재 문제의 ID가 일치하는지 검증
        if (currentQuestion == null || !currentQuestion.getId().equals(request.getQuestionId())) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        // 참가자 엔티티 다시 로드하여 최신 상태 확보
        participant = participantRepository.findById(participant.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // 답변 제출 및 점수 계산
        BattleAnswer answer = participant.submitAnswer(
                currentQuestion,
                request.getAnswer(),
                request.getTimeSpentSeconds()
        );

        // 결과 저장
        participant = participantRepository.save(participant);

        // Redis에 업데이트된 참가자 정보 저장
        saveParticipantToRedis(participant, sessionId);

        return createBattleAnswerResponse(answer);
    }

    /**
     * 대결 시작 준비 상태 확인
     */
    public boolean isReadyToStart(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.isReadyToStart();
    }

    /**
     * 대결 시작 처리
     */
    public BattleStartResponse startBattle(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithAllDetails(roomId)
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
        BattleRoom room = battleRoomRepository.findByIdWithAllDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        Question nextQuestion = room.startNextQuestion();
        battleRoomRepository.save(room);

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
        BattleRoom room = battleRoomRepository.findByIdWithAllDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        return room.allParticipantsAnswered();
    }

    /**
     * 대결 진행 상황 조회
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithAllDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        BattleProgress battleProgress = room.getProgress();
        return createBattleProgressResponse(battleProgress);
    }

    /**
     * 대결 종료 처리
     */
    public BattleEndResponse endBattle(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithAllDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대결 종료 및 결과 계산
        room.finishBattle();
        BattleResult result = calculateBattleResult(room);

        // 경험치 부여 및 통계 업데이트
        awardExperiencePoints(result);
        updateStatistics(result);

        // 저장
        battleRoomRepository.save(room);

        return entityMapperService.mapToBattleEndResponse(result);
    }

    // 내부 도우미 메서드들

    /**
     * 참가자 추가
     */
    private BattleParticipant addParticipant(BattleRoom battleRoom, User user) {
        // 사용자에게 UserBattleStats가 없으면 생성
        if (user.getBattleStats() == null) {
            UserBattleStats stats = new UserBattleStats(user);
            userBattleStatsRepository.save(stats);

            // 사용자 다시 로드하여 양방향 관계 초기화
            user = userRepository.findByIdWithStats(user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(battleRoom)
                .user(user)
                .build();

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
    private BattleProgressResponse createBattleProgressResponse(BattleProgress progress) {
        Map<Long, BattleProgressResponse.ParticipantProgress> participantProgress = new HashMap<>();

        progress.getParticipantProgresses().forEach((id, p) -> {
            participantProgress.put(
                    id,
                    BattleProgressResponse.ParticipantProgress.builder()
                            .userId(id)
                            .username(p.getUsername())
                            .currentScore(p.getCurrentScore())
                            .correctAnswers(p.getCorrectAnswers())
                            .hasAnsweredCurrent(p.isHasAnsweredCurrent())
                            .currentStreak(p.getCurrentStreak())
                            .build()
            );
        });

        return BattleProgressResponse.builder()
                .roomId(progress.getBattleRoomId())
                .currentQuestionIndex(progress.getCurrentQuestionIndex())
                .totalQuestions(progress.getTotalQuestions())
                .remainingTimeSeconds((int) progress.getRemainingTime().getSeconds())
                .participantProgress(participantProgress)
                .status(BattleStatus.valueOf(progress.getStatus().name()))
                .build();
    }

    /**
     * 대결 결과 계산
     */
    private BattleResult calculateBattleResult(BattleRoom room) {
        List<BattleParticipant> sortedParticipants = new ArrayList<>(room.getParticipants());
        sortedParticipants.sort(Comparator.comparingInt(BattleParticipant::getCurrentScore).reversed());

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
                .totalQuestions(room.getQuestions().size())
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

        // 다른 참가자들에게도 경험치 부여
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
}