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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Lazy;

/**
 * 배틀 모드 게임 관련 비즈니스 로직을 처리하는 서비스
 * * <p>사용자들이 실시간으로 경쟁하는 배틀 모드의 생성, 참가, 진행, 종료 등
 * 전체 생명주기를 관리합니다. WebSocket을 통한 실시간 통신과 Redis를 활용한
 * 세션 관리를 포함합니다.</p>
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Service
// @RequiredArgsConstructor
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
     * BattleService의 생성자입니다. 필요한 Repository와 Service를 주입받습니다.
     * SimpMessagingTemplate은 순환 참조 문제를 피하기 위해 @Lazy 로딩을 사용합니다.
     *
     * @param battleRoomRepository        배틀룸 Repository
     * @param participantRepository      참가자 Repository
     * @param userRepository             사용자 Repository
     * @param quizRepository             퀴즈 Repository
     * @param userBattleStatsRepository 사용자 배틀 통계 Repository
     * @param redisTemplate              Redis 작업을 위한 Template
     * @param levelingService            레벨 및 경험치 관련 Service
     * @param entityMapperService        엔티티-DTO 변환 Service
     * @param messagingTemplate          WebSocket 메시징을 위한 Template (@Lazy 로딩)
     */
    @Autowired
    public BattleService(BattleRoomRepository battleRoomRepository, BattleParticipantRepository participantRepository,
                         UserRepository userRepository, QuizRepository quizRepository, UserBattleStatsRepository userBattleStatsRepository,
                         RedisTemplate<String, String> redisTemplate, LevelingService levelingService,
                         EntityMapperService entityMapperService, @Lazy SimpMessagingTemplate messagingTemplate) {
        this.battleRoomRepository = battleRoomRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.userBattleStatsRepository = userBattleStatsRepository;
        this.redisTemplate = redisTemplate;
        this.levelingService = levelingService;
        this.entityMapperService = entityMapperService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 새로운 대결방을 생성합니다.
     * 방 생성자를 첫 번째 참가자로 자동 추가합니다.
     *
     * @param creator         대결방을 생성하는 사용자
     * @param quizId          대결에서 사용할 퀴즈의 ID
     * @param maxParticipants 최대 참가자 수 (null일 경우 기본값 4)
     * @return 생성된 대결방 정보를 담은 {@link BattleRoomResponse}
     * @throws BusinessException 퀴즈를 찾을 수 없을 때 (ErrorCode.QUIZ_NOT_FOUND)
     */
    public BattleRoomResponse createBattleRoom(User creator, Long quizId, Integer maxParticipants) {
        // 퀴즈 조회 (with FETCH JOIN)
        Quiz quiz = quizRepository.findByIdWithDetails(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다."));

        // 대결방 생성
        BattleRoom battleRoom = BattleRoom.builder()
                .quiz(quiz)
                .maxParticipants(maxParticipants != null ? maxParticipants : 4)
                .creatorId(creator.getId()) // 방 생성자 ID 설정
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
     * 특정 ID의 대결방 상세 정보를 조회합니다.
     * 참가자 및 퀴즈 정보를 포함하여 조회합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 조회된 대결방 정보를 담은 {@link BattleRoomResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    public BattleRoomResponse getBattleRoom(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 특정 상태(대기중, 진행중 등)의 대결방 목록을 조회합니다.
     * N+1 문제를 방지하기 위해 상세 정보를 별도로 조회합니다.
     *
     * @param status 조회할 대결방의 상태 ({@link BattleRoomStatus})
     * @return 해당 상태의 대결방 목록 ({@link BattleRoomResponse} 리스트)
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
     * 특정 사용자가 현재 참여하고 있는 '진행중' 상태의 대결방을 조회합니다.
     *
     * @param user 조회할 사용자
     * @return 사용자가 참여중인 활성 대결방 정보 ({@link BattleRoomResponse}), 없으면 null
     */
    public BattleRoomResponse getActiveBattleRoomByUser(User user) {
        try {
            // IN_PROGRESS 상태인 방 찾기 시도
            Optional<BattleRoom> roomOpt = battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.IN_PROGRESS);
            
            // IN_PROGRESS 상태인 방이 없으면 WAITING 상태인 방도 찾아봄
            if (roomOpt.isEmpty()) {
                roomOpt = battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.WAITING);
            }
            
            // 활성 대결방이 없으면 null 반환
            if (roomOpt.isEmpty()) {
                return null;
            }
            
            // 상세 정보 로드
            BattleRoom room = battleRoomRepository.findByIdWithDetails(roomOpt.get().getId())
                    .orElse(null);
                    
            if (room == null) {
                return null;
            }
    
            return entityMapperService.mapToBattleRoomResponse(room);
        } catch (Exception e) {
            // 예외 발생 시에도 null 반환 (컨트롤러에서 빈 배열로 변환)
            return null;
        }
    }

    /**
     * 사용자가 특정 대결방에 참가합니다.
     * 대기 중인 방에만 참가 가능하며, 정원 초과 및 중복 참가를 확인합니다.
     * 참가 시 WebSocket으로 참가자 목록 업데이트 메시지를 전송합니다.
     *
     * @param roomId 참가할 대결방의 ID
     * @param user   참가하려는 사용자
     * @return 참가 후 업데이트된 대결방 정보 ({@link BattleRoomResponse})
     * @throws BusinessException 대결방을 찾을 수 없거나 (BATTLE_ROOM_NOT_FOUND),
     * 이미 시작되었거나 (BATTLE_ALREADY_STARTED),
     * 정원이 찼거나 (BATTLE_ROOM_FULL),
     * 이미 참가 중일 때 (ALREADY_PARTICIPATING)
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
     * 참가자의 준비 상태를 토글합니다 (준비/준비 해제).
     * 모든 참가자가 준비 완료되면 자동으로 대결을 시작합니다.
     *
     * @param roomId 준비 상태를 변경할 대결방의 ID
     * @param user   준비 상태를 변경할 사용자
     * @return 상태 변경 후의 대결방 정보 ({@link BattleRoomResponse})
     * @throws BusinessException 대결방 또는 참가자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, PARTICIPANT_NOT_FOUND)
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
     * 사용자가 대기 중인 대결방에서 나갑니다.
     * 진행 중인 대결에서는 나갈 수 없습니다.
     * 마지막 참가자가 나가면 대결방은 삭제됩니다.
     *
     * @param roomId 나갈 대결방의 ID
     * @param user   나가려는 사용자
     * @return 업데이트된 대결방 정보 ({@link BattleRoomResponse}), 방이 삭제되면 null 반환
     * @throws BusinessException 대결방을 찾을 수 없거나 (BATTLE_ROOM_NOT_FOUND),
     * 진행 중인 대결이거나 (BATTLE_ALREADY_STARTED),
     * 참가자를 찾을 수 없을 때 (PARTICIPANT_NOT_FOUND)
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
     * WebSocket 연결 시 대결방 입장 처리를 합니다.
     * 사용자를 참가자로 확인/추가하고, WebSocket 세션 ID와 참가자 ID를 Redis에 저장합니다.
     *
     * @param request   입장 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 입장 처리 결과 및 현재 참가자 정보를 담은 {@link BattleJoinResponse}
     * @throws BusinessException 대결방 또는 사용자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, USER_NOT_FOUND)
     */
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        // 대결방 조회
        BattleRoom room = battleRoomRepository.findByIdWithDetails(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findByIdWithStats(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 참가자 생성 및 저장 (이미 존재하면 기존 참가자 반환)
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
     * 사용자가 제출한 답변을 처리합니다.
     * Redis에서 세션 ID로 참가자를 조회하고, 답변의 유효성(진행중인 문제, 중복 답변 등)을 검증합니다.
     * 점수를 계산하고 답변 정보를 저장한 후, 업데이트된 참가자 정보를 Redis에 반영합니다.
     *
     * @param request   답변 요청 정보 (roomId, questionId, answer, timeSpentSeconds)
     * @param sessionId WebSocket 세션 ID
     * @return 답변 처리 결과 (정답 여부, 획득 점수, 현재 총점 등)를 담은 {@link BattleAnswerResponse}
     * @throws BusinessException 참가자/방/질문 정보 오류, 배틀 상태 오류, 유효하지 않은 입력/순서, 중복 답변 등 다양한 오류 발생 가능
     */
    @Transactional
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND);
        }

        // 배틀룸 상세 정보 로드 (질문 포함)
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

        // 시간 검증 (최대 시간 제한 적용)
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
     * 대결방의 모든 참가자가 준비를 완료했는지 확인합니다. (대결 시작 가능 여부 확인)
     * 대기 상태(WAITING)인 방만 확인합니다. 동시성 문제를 방지하기 위해 synchronized 처리됩니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 모든 참가자가 준비 완료 상태면 true, 아니면 false
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    @Transactional
    public synchronized boolean isReadyToStart(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 이미 시작된 방이면 false 반환
        if (room.getStatus() != BattleRoomStatus.WAITING) {
            return false;
        }

        return room.isReadyToStart();
    }

    /**
     * 대결을 시작합니다.
     * 방 상태를 IN_PROGRESS로 변경하고 시작 시간을 기록합니다.
     * 동시성 문제를 방지하기 위해 synchronized 처리됩니다.
     *
     * @param roomId 시작할 대결방의 ID
     * @return 대결 시작 정보(참가자, 총 문제 수, 첫 문제 정보 등)를 담은 {@link BattleStartResponse}
     * @throws BusinessException 대결방을 찾을 수 없거나 (BATTLE_ROOM_NOT_FOUND),
     * 이미 시작되었거나 대기 상태가 아닐 때 (BATTLE_ALREADY_STARTED)
     */
    @Transactional
    public synchronized BattleStartResponse startBattle(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 추가 안전 장치: 방 상태 다시 확인
        if (room.getStatus() != BattleRoomStatus.WAITING && room.getStatus() != BattleRoomStatus.READY) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작된 배틀입니다.");
        }

        // 대결 시작 상태로 변경
        room.startBattle();
        battleRoomRepository.save(room);

        return createBattleStartResponse(room);
    }

    /**
     * 다음 문제를 준비하고 해당 문제 정보를 반환합니다.
     * 대결방의 현재 문제 인덱스를 증가시키고 다음 문제를 로드합니다.
     * 더 이상 문제가 없으면 게임 종료 상태를 포함한 응답을 반환하고 방 상태를 FINISHED로 변경합니다.
     *
     * @param roomId 진행 중인 대결방의 ID
     * @return 다음 문제 정보 또는 게임 종료 상태를 담은 {@link BattleNextQuestionResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
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

    /**
     * 현재 진행 중인 문제에 대해 모든 활성 참가자가 답변을 완료했는지 확인합니다.
     * 지연 로딩 문제를 피하기 위해 참가자의 답변 목록을 명시적으로 로드하여 확인합니다.
     * 동시성 문제를 방지하기 위해 synchronized 처리됩니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 모든 활성 참가자가 현재 문제에 대한 답변을 완료했으면 true, 아니면 false
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    @Transactional
    public synchronized boolean allParticipantsAnswered(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        log.info("모든 참가자 답변 확인 시작: roomId={}, 현재문제인덱스={}",
                roomId, room.getCurrentQuestionIndex());

        // 1. 현재 필요한 답변 수 계산 (현재 진행 중인 문제 번호 + 1)
        int requiredAnswers = room.getCurrentQuestionIndex() + 1;

        // 2. 활성 참가자별 답변 상태 확인
        for (BattleParticipant p : room.getParticipants()) {
            if (p.isActive()) {
                // 지연 로딩으로 인한 예외 방지를 위해 명시적으로 참가자 답변 로드
                int answersCount = participantRepository.findByIdWithAnswers(p.getId())
                        .map(loaded -> loaded.getAnswers().size())
                        .orElse(0);

                boolean answered = answersCount >= requiredAnswers;
                log.info("참가자 답변 상태: userId={}, 활성상태={}, 답변여부={}, 답변수={}, 필요답변수={}",
                        p.getUser().getId(), p.isActive(), answered, answersCount, requiredAnswers);

                // 한 명이라도 아직 답변하지 않았으면 false 반환
                if (!answered) {
                    log.info("참가자 미답변 발견: userId={}", p.getUser().getId());
                    return false;
                }
            }
        }

        // 모든 활성 참가자가 답변한 경우
        log.info("모든 참가자 답변 완료 확인: roomId={}", roomId);
        return true;
    }

    /**
     * 대결 진행 상황 (점수판)을 조회합니다.
     * 현재 문제 인덱스, 남은 시간, 각 참가자의 점수 및 답변 상태 등을 포함합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 대결 진행 상황 정보를 담은 {@link BattleProgressResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        BattleProgress battleProgress = room.getProgress();
        return createBattleProgressResponse(battleProgress);
    }

    /**
     * 대결을 종료 처리하고 최종 결과를 계산합니다.
     * 방 상태를 FINISHED로 변경하고, 최종 점수 및 순위를 계산합니다.
     * 결과에 따라 참가자들에게 경험치를 부여하고, 사용자 및 퀴즈 통계를 업데이트합니다.
     *
     * @param roomId 종료할 대결방의 ID
     * @return 최종 대결 결과(승자, 참가자 순위, 점수 등)를 담은 {@link BattleEndResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    public BattleEndResponse endBattle(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대결 종료 및 결과 계산 (이미 종료된 경우 상태 변경 건너뜀)
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

    // --- 내부 도우미 메서드들 ---

    /**
     * 사용자를 대결방의 참가자로 추가합니다. (내부 헬퍼 메서드)
     * 사용자의 배틀 통계(UserBattleStats)가 없으면 새로 생성하고 저장합니다.
     *
     * @param battleRoom 참가자를 추가할 대결방
     * @param user       추가될 사용자
     * @return 생성된 {@link BattleParticipant} 엔티티
     * @throws BusinessException 사용자 조회 실패 시 (ErrorCode.USER_NOT_FOUND)
     */
    private BattleParticipant addParticipant(BattleRoom battleRoom, User user) {
        // 사용자에게 UserBattleStats가 없으면 생성
        if (user.getBattleStats() == null) {
            UserBattleStats stats = new UserBattleStats(user);
            userBattleStatsRepository.save(stats);

            // 사용자 다시 로드하여 양방향 관계 초기화 (stats 포함)
            user = userRepository.findByIdWithStats(user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }

        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(battleRoom)
                .user(user)
                .build();

        return participantRepository.save(participant);
    }

    /**
     * WebSocket 세션 ID와 참가자 ID를 Redis에 저장합니다. (내부 헬퍼 메서드)
     * 세션 ID를 키로 사용하여 참가자 ID를 저장하며, 일정 시간 후 만료되도록 설정합니다.
     *
     * @param participant 저장할 참가자 정보
     * @param sessionId   연결된 WebSocket 세션 ID
     */
    private void saveParticipantToRedis(BattleParticipant participant, String sessionId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(
                participantKey,
                participant.getId().toString(),
                ROOM_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );
    }

    /**
     * Redis에서 WebSocket 세션 ID를 이용해 참가자 정보를 조회합니다. (내부 헬퍼 메서드)
     * Redis에서 참가자 ID를 찾고, 해당 ID로 데이터베이스에서 참가자 엔티티를 조회합니다.
     *
     * @param sessionId 조회할 WebSocket 세션 ID
     * @return 조회된 {@link BattleParticipant} 엔티티, 찾지 못하면 null 반환
     */
    private BattleParticipant getParticipantFromRedis(String sessionId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + sessionId;
        String participantId = redisTemplate.opsForValue().get(participantKey);

        if (participantId == null) {
            return null;
        }

        // Redis에서 얻은 ID로 DB에서 참가자 조회
        return participantRepository.findById(Long.parseLong(participantId))
                .orElse(null); // DB에 해당 ID가 없을 수도 있음
    }

    /**
     * 대결방 입장(join) 시 WebSocket으로 전송할 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     *
     * @param room           대상 대결방
     * @param newParticipant 새로 참가한 (또는 정보를 조회하는) 참가자
     * @return 생성된 {@link BattleJoinResponse} DTO
     */
    private BattleJoinResponse createBattleJoinResponse(BattleRoom room, BattleParticipant newParticipant) {
        // 방의 최신 참가자 목록을 직접 DB에서 다시 조회
        List<BattleParticipant> latestParticipants = participantRepository.findByBattleRoom(room);
        
        log.info("대결방 참가자 목록 조회: roomId={}, 전체참가자수={}, 신규참가자ID={}",
                room.getId(), latestParticipants.size(), newParticipant.getUser().getId());
        
        // 최신 참가자 목록에서 ParticipantInfo로 변환
        List<BattleJoinResponse.ParticipantInfo> participants = latestParticipants.stream()
                .map(participant -> {
                    User user = participant.getUser();
                    return BattleJoinResponse.ParticipantInfo.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .profileImage(user.getProfileImage())
                            .level(user.getLevel())
                            .isReady(participant.isReady())
                            .build();
                })
                .collect(Collectors.toList());

        // 참가자 ID 목록 로깅 (디버깅용)
        String participantIds = participants.stream()
                .map(p -> p.getUserId().toString())
                .collect(Collectors.joining(", "));
        log.info("참가자 ID 목록: {}", participantIds);

        return BattleJoinResponse.builder()
                .roomId(room.getId())
                .userId(newParticipant.getUser().getId()) // 응답의 주체 사용자 ID
                .username(newParticipant.getUser().getUsername())
                .currentParticipants(latestParticipants.size()) // 최신 참가자 수 기준
                .maxParticipants(room.getMaxParticipants())
                .participants(participants) // 최신 참가자 목록
                .joinedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 답변 처리 후 WebSocket으로 전송할 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     * 이 메서드는 현재 {@link #processAnswer} 내에서 직접 빌더를 사용하므로 호출되지 않습니다.
     *
     * @param answer 처리된 답변 정보
     * @return 생성된 {@link BattleAnswerResponse} DTO
     */
    private BattleAnswerResponse createBattleAnswerResponse(BattleAnswer answer) {
        Question question = answer.getQuestion();

        return BattleAnswerResponse.builder()
                .questionId(question.getId())
                .isCorrect(answer.isCorrect())
                .earnedPoints(answer.getEarnedPoints())
                .timeBonus(answer.getTimeBonus())
                .currentScore(answer.getParticipant().getCurrentScore()) // 참가자의 현재 총점
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    /**
     * 대결 시작 시 WebSocket으로 전송할 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     * 첫 번째 문제 정보도 함께 포함합니다.
     *
     * @param room 시작하는 대결방
     * @return 생성된 {@link BattleStartResponse} DTO
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

        Question firstQuestion = room.getCurrentQuestion(); // 시작 시 첫 번째 문제
        BattleNextQuestionResponse firstQuestionResponse = createNextQuestionResponse(
                firstQuestion,
                room.getQuestions().size() == 1 // 문제가 하나뿐이면 첫 문제가 마지막 문제
        );

        return BattleStartResponse.builder()
                .roomId(room.getId())
                .participants(participants)
                .totalQuestions(room.getQuestions().size())
                .timeLimit(room.getCurrentQuestionTimeLimit()) // 첫 문제의 시간 제한
                .startTime(room.getStartTime())
                .firstQuestion(firstQuestionResponse) // 첫 문제 정보 포함
                .build();
    }

    /**
     * 다음 문제 정보를 담은 WebSocket 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     * 마지막 문제인지 여부를 포함합니다.
     *
     * @param question 다음 문제 객체
     * @param isLast   이 문제가 마지막 문제인지 여부
     * @return 생성된 {@link BattleNextQuestionResponse} DTO
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
                .isGameOver(false) // 다음 문제가 있다는 것은 게임오버가 아님
                .build();

        log.info("생성된 응답: questionId={}, 마지막문제={}", response.getQuestionId(), response.isLastQuestion());
        return response;
    }

    /**
     * 대결 진행 상황(점수판) 정보를 담은 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     *
     * @param progress 대결 진행 상황 데이터 객체
     * @return 생성된 {@link BattleProgressResponse} DTO
     */
    private BattleProgressResponse createBattleProgressResponse(BattleProgress progress) {
        Map<Long, BattleProgressResponse.ParticipantProgress> participantProgress = new HashMap<>();

        // 각 참가자의 진행 상황 매핑
        progress.getParticipantProgresses().forEach((userId, p) -> {
            participantProgress.put(
                    userId,
                    BattleProgressResponse.ParticipantProgress.builder()
                            .userId(userId)
                            .username(p.getUsername())
                            .currentScore(p.getCurrentScore())
                            .correctAnswers(p.getCorrectAnswers())
                            .hasAnsweredCurrent(p.isHasAnsweredCurrent()) // 현재 문제 답변 여부
                            .currentStreak(p.getCurrentStreak()) // 현재 연속 정답 횟수
                            .build()
            );
        });

        return BattleProgressResponse.builder()
                .roomId(progress.getBattleRoomId())
                .currentQuestionIndex(progress.getCurrentQuestionIndex())
                .totalQuestions(progress.getTotalQuestions())
                .remainingTimeSeconds((int) progress.getRemainingTime().getSeconds()) // 남은 시간 (초)
                .participantProgress(participantProgress) // 참가자별 진행 상황 맵
                .status(BattleRoomStatus.valueOf(progress.getStatus().name())) // 현재 배틀방 상태
                .build();
    }

    /**
     * 대결 종료 후 최종 결과를 계산합니다. (내부 헬퍼 메서드)
     * 참가자들을 점수 기준으로 내림차순 정렬하고, 승자를 결정합니다.
     *
     * @param room 종료된 대결방
     * @return 계산된 {@link BattleResult} 객체
     */
    private BattleResult calculateBattleResult(BattleRoom room) {
        // 참가자 목록 복사 후 점수 내림차순 정렬
        List<BattleParticipant> sortedParticipants = new ArrayList<>(room.getParticipants());
        sortedParticipants.sort(Comparator.comparingInt(BattleParticipant::getCurrentScore).reversed());

        // 승자 결정 (점수가 가장 높은 참가자, 동점일 경우 먼저 정렬된 참가자)
        BattleParticipant winner = sortedParticipants.isEmpty() ? null : sortedParticipants.get(0);
        int highestScore = winner != null ? winner.getCurrentScore() : 0;

        return BattleResult.builder()
                .roomId(room.getId())
                .winner(winner) // 승자 참가자 객체
                .participants(sortedParticipants) // 점수 순 정렬된 참가자 리스트
                .highestScore(highestScore)
                .startTime(room.getStartTime())
                .endTime(LocalDateTime.now()) // 종료 시점의 시간
                .totalTimeSeconds(room.getTotalTimeSeconds()) // 총 소요 시간
                .totalQuestions(room.getQuestions().size())
                .battleRoom(room) // 원본 배틀룸 참조
                .build();
    }

    /**
     * 대결 결과에 따라 참가자들에게 경험치를 부여합니다. (내부 헬퍼 메서드)
     * {@link LevelingService}를 호출하여 경험치 계산 및 적용을 위임합니다.
     *
     * @param result 계산된 대결 결과
     */
    private void awardExperiencePoints(BattleResult result) {
        if (result.getWinner() == null) return; // 참가자가 없으면 종료

        // 승자에게 경험치 부여
        levelingService.calculateBattleExp(result, result.getWinner().getUser());

        // 다른 참가자들에게도 경험치 부여 (승자 제외)
        result.getParticipants().stream()
                .filter(p -> !p.equals(result.getWinner()))
                .forEach(participant -> {
                    levelingService.calculateBattleExp(result, participant.getUser());
                });
    }

    /**
     * 대결 종료 후 사용자 및 퀴즈 관련 통계를 업데이트합니다. (내부 헬퍼 메서드)
     * 각 참가자의 배틀 통계(승/패, 점수 등)를 업데이트하고,
     * 대결에 사용된 퀴즈의 플레이 횟수 등 통계를 업데이트합니다.
     *
     * @param result 계산된 대결 결과
     */
    private void updateStatistics(BattleResult result) {
        // 사용자 통계 업데이트
        result.getParticipants().forEach(participant -> {
            UserBattleStats stats = participant.getUser().getBattleStats();
            if (stats != null) {
                stats.updateStats(participant); // 참가자 정보 기반으로 통계 업데이트
                userBattleStatsRepository.save(stats);
            }
        });

        // 퀴즈 통계 업데이트
        Quiz quiz = result.getBattleRoom().getQuiz();
        quiz.updateBattleStats(result); // 배틀 결과 기반으로 퀴즈 통계 업데이트
        quizRepository.save(quiz);
    }

    /**
     * 사용자가 대결(주로 대기 중)에서 나가는 요청을 처리합니다 (WebSocket 메시지 처리).
     * 참가자를 비활성(active=false) 상태로 변경합니다.
     * 대기 중 상태에서 활성 참가자가 1명 미만이 되면 방 상태를 FINISHED로 변경합니다.
     *
     * @param request   나가기 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID (현재 로직에서는 사용되지 않음)
     * @return 나가기 처리 결과(사용자 ID, 방 ID, 변경된 방 상태)를 담은 {@link BattleLeaveResponse}
     * @throws BusinessException 방/사용자/참가자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, USER_NOT_FOUND, PARTICIPANT_NOT_FOUND)
     */
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

        // 배틀 상태가 대기 중일 때만 참가자 수 확인 및 방 상태 변경
        if (battleRoom.getStatus() == BattleRoomStatus.WAITING) {
            // 활성 참가자 수 계산
            long activeParticipantsCount = battleRoom.getParticipants().stream()
                    .filter(BattleParticipant::isActive) // 활성 상태인 참가자만 필터링
                    .count();

            // 활성 참가자가 1명 미만이면 배틀 상태를 종료로 변경
            if (activeParticipantsCount < 1) {
                battleRoom.setStatus(BattleRoomStatus.FINISHED);
                battleRoomRepository.save(battleRoom);
                log.info("마지막 활성 참가자가 나가서 배틀룸 상태 FINISHED로 변경: roomId={}", battleRoom.getId());
            }
        }

        return new BattleLeaveResponse(
                user.getId(),
                battleRoom.getId(),
                battleRoom.getStatus() // 변경되었을 수 있는 방 상태 반환
        );
    }


    /**
     * 대결방이 유효한 상태인지 확인합니다.
     * 주로 대기(WAITING) 상태일 때, 활성 참가자가 1명 이상 있는지 확인하는 데 사용됩니다.
     * 다른 상태(진행중, 종료 등)는 항상 유효하다고 간주합니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 대결방이 유효하면 true, 아니면 false
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND)
     */
    public boolean isValidBattleRoom(Long roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 대기 상태에서만 활성 참가자 수 체크
        if (room.getStatus() == BattleRoomStatus.WAITING) {
            long activeParticipantsCount = room.getParticipants().stream()
                    .filter(BattleParticipant::isActive) // 활성 참가자만 필터링
                    .count();

            // 활성 참가자가 1명 이상이어야 유효
            return activeParticipantsCount >= 1;
        }

        // 대기 상태가 아니면 유효하다고 판단
        return true;
    }


    /**
     * WebSocket을 통해 참가자의 준비 상태 토글 요청을 처리합니다.
     * 요청한 사용자를 찾아 준비 상태를 변경하고, 변경된 참가자 목록 정보를 포함한 응답을 반환합니다.
     * 세션 ID와 참가자를 Redis에 연결합니다. 동시성 제어를 위해 synchronized 처리됩니다.
     *
     * @param request   준비 상태 변경 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 변경된 준비 상태를 포함한 참가자 목록 정보를 담은 {@link BattleReadyResponse}
     * @throws BusinessException 방/사용자/참가자 오류, 상태 오류, 작업 오류 등 발생 가능
     */
    @Transactional
    public synchronized BattleReadyResponse toggleReadyState(BattleReadyRequest request, String sessionId) {
        log.info("준비 상태 토글 요청: roomId={}, userId={}", request.getRoomId(), request.getUserId());

        try {
            // 1. 대결방 기본 정보 로드 (참가자 정보는 별도 조회)
            BattleRoom room = battleRoomRepository.findByIdWithBasicDetails(request.getRoomId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

            // 2. 사용자 조회
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 3. 참가자 조회
            BattleParticipant participant;
            Optional<BattleParticipant> existingParticipant = participantRepository.findByBattleRoomAndUser(room, user);

            if (existingParticipant.isPresent()) {
                participant = existingParticipant.get();
                log.info("기존 참가자 준비 상태 토글: roomId={}, userId={}", request.getRoomId(), request.getUserId());
            } else {
                // 방장은 방 생성 시 자동으로 추가되므로, 이 경우는 일반 참가자가 아직 DB에 반영되기 전이거나 오류 상황일 수 있음.
                log.warn("참가자가 등록되지 않음: roomId={}, userId={}. 방에 재입장 필요할 수 있음.",
                        request.getRoomId(), request.getUserId());
                throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND,
                        "참가자가 등록되지 않았습니다. 방에 다시 입장해주세요.");
            }

            // 4. 준비 상태 토글 전 유효성 검사 (방 상태, 참가자 활성 상태 등)
            validateReadyToggle(participant);

            // 5. 준비 상태 토글
            participant.toggleReady();
            participantRepository.save(participant);

            // 6. Redis에 참가자 정보 저장 (세션 정보 연결)
            saveParticipantToRedis(participant, sessionId);

            log.info("준비 상태 토글 완료: roomId={}, userId={}, isReady={}",
                    request.getRoomId(), request.getUserId(), participant.isReady());

            // 7. 응답 생성 - 최신 참가자 목록 조회하여 반영
            List<BattleParticipant> updatedParticipants = participantRepository.findByBattleRoom(room);
            return createBattleReadyResponse(room, updatedParticipants);
        } catch (Exception e) {
            log.error("준비 상태 토글 중 오류 발생: roomId={}, userId={}",
                    request.getRoomId(), request.getUserId(), e);
            // 발생한 예외를 그대로 다시 던져서 ControllerAdvice 등에서 처리하도록 함
            throw e;
        }
    }

    /**
     * 준비 상태 토글 가능 여부를 검증합니다. (내부 헬퍼 메서드)
     * 대기 중인 방에서만, 그리고 활성 상태의 참가자만 준비 상태를 변경할 수 있습니다.
     *
     * @param participant 검증할 참가자
     * @throws BusinessException 검증 실패 시 (BATTLE_ALREADY_STARTED, INVALID_OPERATION)
     */
    private void validateReadyToggle(BattleParticipant participant) {
        // 대기방 상태 확인
        if (participant.getBattleRoom().getStatus() != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED,
                    "대기 중인 방에서만 준비 상태를 변경할 수 있습니다.");
        }

        // 활성 상태 참가자만 준비 가능
        if (!participant.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "활성 상태의 참가자만 준비 상태를 변경할 수 있습니다.");
        }
    }

    /**
     * 특정 대결방의 현재 참가자 목록 정보를 조회합니다.
     * 주로 방장이 처음 연결되었을 때 현재 방 상태를 가져오기 위해 사용될 수 있습니다.
     * 응답 형식은 {@link #joinBattle}과 동일한 {@link BattleJoinResponse}를 사용합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 현재 참가자 목록 정보를 담은 {@link BattleJoinResponse}
     * @throws BusinessException 대결방 또는 방장(첫 참가자)을 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, PARTICIPANT_NOT_FOUND)
     */
    public BattleJoinResponse getCurrentBattleParticipants(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 방장 찾기 (일반적으로 첫 번째 참가자)
        BattleParticipant creator = room.getParticipants().stream()
                .findFirst() // 첫 번째 참가자를 방장으로 간주
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "방장을 찾을 수 없습니다. (참가자 없음)"));

        // 방장 정보를 기준으로 응답 생성
        return createBattleJoinResponse(room, creator);
    }

    /**
     * 특정 WebSocket 세션 ID를 특정 사용자의 참가자 정보와 연결합니다.
     * 주로 방장이 WebSocket에 연결되었을 때 호출되어 세션과 참가자를 매핑합니다.
     *
     * @param roomId    대상 대결방 ID
     * @param userId    연결할 사용자 ID
     * @param sessionId 연결할 WebSocket 세션 ID
     * @throws BusinessException 방/사용자/참가자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, USER_NOT_FOUND, PARTICIPANT_NOT_FOUND)
     */
    public void linkSessionToParticipant(Long roomId, Long userId, String sessionId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 해당 방에서 해당 사용자의 참가자 정보 조회
        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(room, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // Redis에 세션 ID와 참가자 ID 저장
        saveParticipantToRedis(participant, sessionId);
        log.info("세션과 참가자 연결 완료: roomId={}, userId={}, sessionId={}", roomId, userId, sessionId);
    }

    /**
     * 준비 상태 변경(토글) 시 WebSocket으로 전송할 응답 객체를 생성합니다. (내부 헬퍼 메서드)
     *
     * @param room         대상 대결방
     * @param participants 최신 상태의 참가자 목록
     * @return 생성된 {@link BattleReadyResponse} DTO
     */
    private BattleReadyResponse createBattleReadyResponse(BattleRoom room, List<BattleParticipant> participants) {
        List<BattleReadyResponse.ParticipantInfo> participantInfos = participants.stream()
                .map(p -> BattleReadyResponse.ParticipantInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername())
                        .profileImage(p.getUser().getProfileImage())
                        .level(p.getUser().getLevel())
                        .isReady(p.isReady()) // 각 참가자의 준비 상태 포함
                        .build())
                .collect(Collectors.toList());

        return BattleReadyResponse.builder()
                .roomId(room.getId())
                .participants(participantInfos) // 업데이트된 참가자 목록
                .build();
    }
}