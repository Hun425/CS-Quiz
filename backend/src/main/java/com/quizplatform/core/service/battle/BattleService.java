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
import com.quizplatform.core.service.common.EntityMapperService;
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
    private final EntityMapperService entityMapperService;
    private final SimpMessagingTemplate messagingTemplate;

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * 새로운 대결방 생성
     */
    public BattleRoomResponse createBattleRoom(User creator, Long quizId, Integer maxParticipants) {
        // 퀴즈 조회 (with FETCH JOIN)
        Quiz quiz = quizRepository.findByIdWithDetails(quizId)
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

        // 참가자 추가 및 반환값 저장
        BattleParticipant participant = addParticipant(battleRoom, user);

        // WebSocket 메시지 발송
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/participants",
                createBattleJoinResponse(battleRoom, participant)
        );

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
     * 답변 처리
     * 답변 검증 및 처리 로직 개선
     */
    @Transactional
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        // 배틀룸 상세 정보 로드
        BattleRoom battleRoom = battleRoomRepository.findByIdWithQuizQuestions(participant.getBattleRoom().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        log.info("답변 처리: roomId={}, questionId={}, userId={}, 현재문제인덱스={}",
                request.getRoomId(), request.getQuestionId(), participant.getUser().getId(),
                battleRoom.getCurrentQuestionIndex());

        // 요청으로 전달된 방 ID와 참가자의 방 ID가 일치하는지 확인
        if (!battleRoom.getId().equals(request.getRoomId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 배틀 상태 확인
        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS);
        }

        // 문제를 찾기
        List<Question> questions = battleRoom.getQuestions();
        Question targetQuestion = null;
        int questionIndex = -1;

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            if (q.getId().equals(request.getQuestionId())) {
                targetQuestion = q;
                questionIndex = i;
                break;
            }
        }

        if (targetQuestion == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION, "요청한 문제를 찾을 수 없습니다.");
        }

        log.info("답변 처리 상세: roomId={}, 문제번호={}/{}, 인덱스={}, 현재인덱스={}, userId={}",
                request.getRoomId(), questionIndex + 1, battleRoom.getQuestions().size(),
                questionIndex, battleRoom.getCurrentQuestionIndex(), participant.getUser().getId());

        // 현재 진행중인 문제와 요청한 문제 ID가 일치하는지 확인
        // 이 부분을 수정하여 인덱스가 아닌 문제 ID로 검증
        boolean isCurrentQuestion = false;
        Question currentQuestion = battleRoom.getCurrentQuestion();

        if (currentQuestion != null && currentQuestion.getId().equals(request.getQuestionId())) {
            isCurrentQuestion = true;
        }

        // 현재 진행 중인 문제가 아니라면 답변할 수 없음
        if (!isCurrentQuestion) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE,
                    String.format("현재 진행 중인 문제가 아닙니다. 요청ID: %d, 현재ID: %d",
                            request.getQuestionId(),
                            currentQuestion != null ? currentQuestion.getId() : -1)
            );
        }

        // 이미 답변한 문제인지 확인 - ID 기반으로 검사
        boolean alreadyAnswered = participant.getAnswers().stream()
                .anyMatch(a -> a.getQuestion().getId().equals(request.getQuestionId()));

        if (alreadyAnswered) {
            log.warn("이미 답변한 문제: roomId={}, questionId={}, userId={}, 인덱스={}",
                    request.getRoomId(), request.getQuestionId(), participant.getUser().getId(), questionIndex);
            throw new BusinessException(ErrorCode.ANSWER_ALREADY_SUBMITTED, "이미 답변을 제출했습니다.");
        }

        // 참가자 엔티티 다시 로드하여 최신 상태 확보
        participant = participantRepository.findById(participant.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // 시간 검증
        int timeSpentSeconds = Math.min(request.getTimeSpentSeconds(), targetQuestion.getTimeLimitSeconds());

        // 답변 제출 및 점수 계산
        BattleAnswer answer = participant.submitAnswer(
                targetQuestion,
                request.getAnswer(),
                timeSpentSeconds
        );

        // 결과 저장
        participant = participantRepository.save(participant);

        // Redis에 업데이트된 참가자 정보 저장
        saveParticipantToRedis(participant, sessionId);

        // 응답 생성
        return BattleAnswerResponse.builder()
                .questionId(answer.getQuestion().getId())
                .isCorrect(answer.isCorrect())
                .earnedPoints(answer.getEarnedPoints())
                .timeBonus(answer.getTimeBonus())
                .currentScore(participant.getCurrentScore())
                .correctAnswer(answer.getQuestion().getCorrectAnswer())
                .explanation(answer.getQuestion().getExplanation())
                .build();
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
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
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
    public BattleNextQuestionResponse prepareNextQuestion(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 문제 목록 확인 로그
        List<Question> questions = room.getQuestions();
        log.info("prepareNextQuestion - 호출 시작: roomId={}, 현재 인덱스={}, 문제 목록 크기={}",
                roomId, room.getCurrentQuestionIndex(), questions.size());

        // 현재 인덱스의 문제 정보 로깅 (startNextQuestion 호출 전)
        if (room.getCurrentQuestionIndex() < questions.size()) {
            Question currentQuestion = questions.get(room.getCurrentQuestionIndex());
            log.info("현재 인덱스의 문제: ID={}, 내용={}",
                    currentQuestion.getId(),
                    currentQuestion.getQuestionText().substring(0, Math.min(30, currentQuestion.getQuestionText().length())));
        }

        // 다음 문제 가져오기 - 이 호출로 currentQuestionIndex가 증가함
        Question nextQuestion = room.startNextQuestion();

        // 변경사항 저장
        battleRoomRepository.save(room);

        // 다음 문제 ID 및 현재 상태 로깅
        if (nextQuestion != null) {
            log.info("선택된 다음 문제 결과: ID={}, 새 인덱스={}",
                    nextQuestion.getId(), room.getCurrentQuestionIndex());

            boolean isLastQuestion = room.getCurrentQuestionIndex() >= questions.size() - 1;
            return createNextQuestionResponse(nextQuestion, isLastQuestion);
        } else {
            // 더 이상 문제가 없는 경우 (게임 종료)
            log.info("더 이상 문제가 없음. 게임 종료: roomId={}", roomId);

            // 명시적으로 게임 종료 처리 수행
            room.finishBattle();
            battleRoomRepository.save(room);

            // 게임 종료 응답 생성
            BattleNextQuestionResponse gameOverResponse = BattleNextQuestionResponse.builder()
                    .isGameOver(true)
                    .build();


            return gameOverResponse;
        }
    }



    @Transactional  // 트랜잭션 추가하여 세션이 활성화된 상태에서 지연 로딩 처리
    public boolean allParticipantsAnswered(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        log.info("모든 참가자 답변 확인 시작: roomId={}, 현재문제인덱스={}",
                roomId, room.getCurrentQuestionIndex());

        // 1. 현재 필요한 답변 수 계산
        int requiredAnswers = room.getCurrentQuestionIndex() + 1;

        // 2. 참가자별 답변 상태 로깅 (안전하게 참가자 ID와 필요 값만 미리 가져오기)
        for (BattleParticipant p : room.getParticipants()) {
            if (p.isActive()) {
                // 지연 로딩으로 인한 예외 방지를 위해 명시적으로 초기화
                int answersCount = participantRepository.findByIdWithAnswers(p.getId())
                        .map(loaded -> loaded.getAnswers().size())
                        .orElse(0);

                boolean answered = answersCount >= requiredAnswers;
                log.info("참가자 답변 상태: userId={}, 활성상태={}, 답변여부={}, 답변수={}, 필요답변수={}",
                        p.getUser().getId(), p.isActive(), answered, answersCount, requiredAnswers);

                // 한 명이라도 아직 답변하지 않았으면 빠르게 결과 반환
                if (!answered) {
                    log.info("참가자 미답변 발견: userId={}", p.getUser().getId());
                    return false;
                }
            }
        }

        // 모든 참가자가 답변한 경우
        log.info("모든 참가자 답변 완료 확인: roomId={}", roomId);
        return true;
    }

    /**
     * 대결 진행 상황 조회
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        BattleProgress battleProgress = room.getProgress();
        return createBattleProgressResponse(battleProgress);
    }

    /**
     * 대결 종료 처리
     */
    public BattleEndResponse endBattle(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대결 종료 및 결과 계산
        if (room.getStatus() != BattleRoomStatus.FINISHED) {
            log.info("배틀룸 종료 처리 수행: roomId={}", roomId);
            room.finishBattle();
            battleRoomRepository.save(room);
        } else {
            log.info("배틀룸 이미 종료됨: roomId={}", roomId);
        }

        BattleResult result = calculateBattleResult(room);

        // 경험치 부여 및 통계 업데이트
        awardExperiencePoints(result);
        updateStatistics(result);

        // 결과 응답 생성
        BattleEndResponse response = entityMapperService.mapToBattleEndResponse(result);

        return response;
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
        BattleNextQuestionResponse response = BattleNextQuestionResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .options(question.getOptionList())
                .timeLimit(question.getTimeLimitSeconds())
                .points(question.getPoints())
                .isLastQuestion(isLast)
                .isGameOver(false)
                .build();

        log.info("생성된 응답: questionId={}, 마지막문제={}", response.getQuestionId(), response.isLastQuestion());
        return response;
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
                .status(BattleRoomStatus.valueOf(progress.getStatus().name()))
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

    public BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId) {
        BattleRoom battleRoom = battleRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        BattleParticipant participant = participantRepository
                .findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // 참가자 상태 비활성화
        participant.setActive(false);
        participantRepository.save(participant);

        // 배틀 상태가 대기 중일 때만 참가자 수 확인
        if (battleRoom.getStatus() == BattleRoomStatus.WAITING) {
            long activeParticipantsCount = battleRoom.getParticipants().stream()
                    .filter(BattleParticipant::isActive)
                    .count();

            // 활성 참가자가 1명 미만이면 배틀 상태를 종료로 변경
            if (activeParticipantsCount < 1) {
                battleRoom.setStatus(BattleRoomStatus.FINISHED);
                battleRoomRepository.save(battleRoom);
            }
        }

        return new BattleLeaveResponse(
                user.getId(),
                battleRoom.getId(),
                battleRoom.getStatus()
        );
    }


    public boolean isValidBattleRoom(Long roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대기 상태에서만 방 유효성 체크
        if (room.getStatus() == BattleRoomStatus.WAITING) {
            long activeParticipantsCount = room.getParticipants().stream()
                    .filter(BattleParticipant::isActive)
                    .count();

            return activeParticipantsCount >= 1;
        }

        return true;
    }


    /**
     * WebSocket을 통한 준비 상태 토글
     */
    @Transactional
    public BattleReadyResponse toggleReadyState(BattleReadyRequest request, String sessionId) {
        log.info("준비 상태 토글 요청: roomId={}, userId={}", request.getRoomId(), request.getUserId());

        try {
            // 1. 대결방 기본 정보 로드
            BattleRoom room = battleRoomRepository.findByIdWithBasicDetails(request.getRoomId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

            // 2. 사용자 조회
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 3. 참가자 조회
            BattleParticipant participant = participantRepository.findByBattleRoomAndUser(room, user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

            // 4. 준비 상태 토글
            participant.toggleReady();
            participantRepository.save(participant);

            // 5. Redis에 참가자 정보 저장 (세션 정보 연결)
            saveParticipantToRedis(participant, sessionId);

            log.info("준비 상태 토글 완료: roomId={}, userId={}, isReady={}",
                    request.getRoomId(), request.getUserId(), participant.isReady());

            // 6. 응답 생성 - 참가자 정보 새로 조회하여 최신 상태 반영
            List<BattleParticipant> updatedParticipants = participantRepository.findByBattleRoom(room);
            return createBattleReadyResponse(room, updatedParticipants);
        } catch (Exception e) {
            log.error("준비 상태 토글 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
            throw e;
        }
    }

    /**
     * 준비 상태 변경 응답 생성
     */
    private BattleReadyResponse createBattleReadyResponse(BattleRoom room, List<BattleParticipant> participants) {
        List<BattleReadyResponse.ParticipantInfo> participantInfos = participants.stream()
                .map(p -> BattleReadyResponse.ParticipantInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername())
                        .profileImage(p.getUser().getProfileImage())
                        .level(p.getUser().getLevel())
                        .isReady(p.isReady())
                        .build())
                .collect(Collectors.toList());

        return BattleReadyResponse.builder()
                .roomId(room.getId())
                .participants(participantInfos)
                .build();
    }
}