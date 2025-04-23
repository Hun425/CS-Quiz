package com.quizplatform.modules.battle.application.service;

// Domain imports
import com.quizplatform.modules.battle.domain.entity.BattleAnswer; // TODO: Verify BattleAnswer location and potentially move related logic
import com.quizplatform.modules.battle.domain.entity.BattleParticipant;
import com.quizplatform.modules.battle.domain.entity.BattleRoom;
import com.quizplatform.modules.battle.domain.vo.BattleRoomStatus; // TODO: Move BattleRoomStatus enum to battle module's domain layer (e.g., domain/vo)
import com.quizplatform.modules.battle.presentation.dto.*; // Keep existing DTO imports within battle module
import com.quizplatform.modules.quiz.domain.entity.Question; // Cross-module dependency (Quiz)
import com.quizplatform.modules.quiz.domain.entity.Quiz; // Cross-module dependency (Quiz)
import com.quizplatform.modules.user.domain.entity.User; // Cross-module dependency (User)
import com.quizplatform.modules.user.domain.entity.UserBattleStats; // Cross-module dependency (User)

// DTO imports
import com.quizplatform.modules.battle.presentation.dto.BattleProgress; // TODO: Verify BattleProgress DTO location
import com.quizplatform.modules.battle.presentation.dto.BattleProgressResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleResult; // TODO: Verify BattleResult DTO location
import com.quizplatform.modules.battle.presentation.dto.BattleEndResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleStartResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleNextQuestionResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleAnswerRequest; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleAnswerResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleJoinRequest; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleReadyRequest; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleReadyResponse; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleLeaveRequest; // Already in battle DTOs
import com.quizplatform.modules.battle.presentation.dto.BattleLeaveResponse; // Already in battle DTOs

// Exception imports
import com.quizplatform.shared_kernel.exception.BusinessException; // TODO: Move BusinessException to shared_kernel
import com.quizplatform.shared_kernel.exception.ErrorCode; // TODO: Move ErrorCode to shared_kernel

// Repository imports
import com.quizplatform.modules.user.infrastructure.repository.UserRepository; // Cross-module dependency (User Repository). TODO: Consider replacing direct repo access with event/API call.
import com.quizplatform.modules.battle.infrastructure.repository.BattleParticipantRepository;
import com.quizplatform.modules.battle.infrastructure.repository.BattleRoomRepository;
import com.quizplatform.modules.quiz.infrastructure.repository.QuizRepository; // Cross-module dependency (Quiz Repository). TODO: Consider replacing direct repo access with event/API call.
import com.quizplatform.modules.user.infrastructure.repository.UserBattleStatsRepository; // Cross-module dependency (User Repository). TODO: Consider replacing direct repo access with event/API call.

// Service imports
import com.quizplatform.shared_kernel.service.EntityMapperService; // TODO: Move EntityMapperService to shared_kernel or implement mapping within battle module (e.g., MapStruct)
import com.quizplatform.modules.user.application.service.LevelingService; // Cross-module dependency (User Service). TODO: Replace direct call with event publishing (e.g., BattleEndedEvent)

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // Infrastructure dependency
import org.springframework.messaging.simp.SimpMessagingTemplate; // Infrastructure dependency
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
// @RequiredArgsConstructor // Keep Autowired constructor for now due to @Lazy
@Transactional
@Slf4j
public class BattleService {
    private final BattleRoomRepository battleRoomRepository;
    private final BattleParticipantRepository participantRepository;
    private final UserRepository userRepository; // TODO: [MODULAR] Replace direct User repo access with API/event or ensure required data is passed in
    private final QuizRepository quizRepository; // TODO: [MODULAR] Replace direct Quiz repo access with API/event or ensure required data is passed in
    private final UserBattleStatsRepository userBattleStatsRepository; // TODO: [MODULAR] Replace direct UserBattleStats repo access with event-based updates in User module
    private final RedisTemplate<String, Object> redisTemplate; // Infrastructure dependency - OK
    private final LevelingService levelingService; // TODO: [MODULAR] Remove direct LevelingService call, trigger via event (e.g., BattleEndedEvent)
    private final EntityMapperService entityMapperService; // TODO: [REFACTOR] Replace with battle module internal mapping (e.g., MapStruct)
    private final SimpMessagingTemplate messagingTemplate; // Infrastructure dependency - OK

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_SESSION_KEY_PREFIX = "battle:participant:session:"; // Key for session ID -> participant info
    private static final String PARTICIPANT_USER_KEY_PREFIX = "battle:participant:user:"; // Key for userId -> session ID
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간
    private static final int PARTICIPANT_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * BattleService의 생성자입니다. 필요한 Repository와 Service를 주입받습니다.
     * SimpMessagingTemplate은 순환 참조 문제를 피하기 위해 @Lazy 로딩을 사용합니다.
     *
     * @param battleRoomRepository        배틀룸 Repository
     * @param participantRepository      참가자 Repository
     * @param userRepository             사용자 Repository (User Module)
     * @param quizRepository             퀴즈 Repository (Quiz Module)
     * @param userBattleStatsRepository 사용자 배틀 통계 Repository (User Module)
     * @param redisTemplate              Redis 작업을 위한 Template (Object type)
     * @param levelingService            레벨 및 경험치 관련 Service (External)
     * @param entityMapperService        엔티티-DTO 변환 Service (External)
     * @param messagingTemplate          WebSocket 메시징을 위한 Template (@Lazy 로딩, Infrastructure)
     */
    @Autowired
    public BattleService(BattleRoomRepository battleRoomRepository, BattleParticipantRepository participantRepository,
                         UserRepository userRepository, QuizRepository quizRepository, UserBattleStatsRepository userBattleStatsRepository,
                         RedisTemplate<String, Object> redisTemplate, LevelingService levelingService, // Changed type argument
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
     * Controller에서 전달받은 request DTO를 사용하여 방 정보를 설정합니다.
     *
     * @param creatorId       대결방을 생성하는 사용자의 ID
     * @param request         배틀방 생성 요청 데이터 (퀴즈 ID, 최대 참가자 수 포함)
     * @return 생성된 대결방 정보를 담은 {@link BattleRoomResponse} (Battle Module DTO)
     * @throws BusinessException 사용자를 찾을 수 없거나(USER_NOT_FOUND), 퀴즈를 찾을 수 없을 때 (QUIZ_NOT_FOUND)
     */
    public BattleRoomResponse createBattleRoom(Long creatorId, BattleRoomCreateRequest request) {
        // 사용자 조회 (User Module Repository)
        // TODO: [MODULAR] Consider alternatives: Fetch needed User data (e.g., just ID validation) via dedicated API/query, or assume creatorId is valid if authenticated.
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // 퀴즈 조회 (Quiz Module Repository)
        // TODO: [MODULAR] Consider alternatives: Fetch needed Quiz data (title, question count, etc.) via dedicated API/query, or pass required Quiz info (ID, questionCount) in the request.
        Quiz quiz = quizRepository.findByIdWithDetails(request.getQuizId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // 대결방 생성 (Battle Module Entity)
        BattleRoom battleRoom = BattleRoom.builder()
                .quiz(quiz) // TODO: [MODULAR] Avoid passing the whole Quiz entity. Pass only quizId and potentially question count.
                .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 4) // 최대 참가자 설정 (기본값 4)
                .creatorId(creator.getId()) // 방 생성자 ID 설정
                .build();

        // 대결방 설정 유효성 검사 (Battle Module Entity Logic)
        battleRoom.validateBattleSettings();

        // 대결방 저장 (Battle Module Repository)
        BattleRoom savedRoom = battleRoomRepository.save(battleRoom);

        // 방장을 첫 참가자로 추가 (Internal method)
        addParticipant(savedRoom, creator); // TODO: [MODULAR] addParticipant might need user data; review its implementation.

        // DTO 변환 (External Service)
        // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic.
        return entityMapperService.mapToBattleRoomResponse(savedRoom);
    }

    /**
     * 특정 ID의 대결방 상세 정보를 조회합니다.
     * 참가자 및 퀴즈 정보를 포함하여 조회합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 조회된 대결방 정보를 담은 {@link BattleRoomResponse} (Battle Module DTO)
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND - TODO: Use shared_kernel exception)
     */
    public BattleRoomResponse getBattleRoom(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // DTO 변환 (External Service)
        // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic.
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 특정 상태(대기중, 진행중 등)의 대결방 목록을 조회합니다.
     * N+1 문제를 방지하기 위해 상세 정보를 별도로 조회합니다.
     *
     * @param status 조회할 대결방의 상태 ({@link BattleRoomStatus} - TODO: Move to Battle Module)
     * @return 해당 상태의 대결방 목록 ({@link BattleRoomResponse} 리스트 - Battle Module DTO)
     */
    public List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status) { // TODO: [REFACTOR] Use String for status input if DTO uses String
        List<BattleRoom> rooms = battleRoomRepository.findByStatus(status);

        // N+1 문제를 방지하기 위해 ID 목록으로 한 번에 상세 조회
        List<Long> roomIds = rooms.stream().map(BattleRoom::getId).collect(Collectors.toList());
        List<BattleRoom> detailedRooms = battleRoomRepository.findByIdWithDetailsIn(roomIds); // Use findByIdIn for batch fetching

        // DTO 변환 (External Service)
        // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic.
        return entityMapperService.mapToBattleRoomResponseList(detailedRooms);
    }

    /**
     * 특정 사용자가 현재 참여하고 있는 활성 대결방(진행중 또는 대기중)을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 사용자가 참여중인 활성 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO), 없으면 null
     */
    public BattleRoomResponse getActiveBattleRoomByUser(Long userId) {
        try {
            // 사용자 조회 (User Module Repository)
            // TODO: [MODULAR] Direct user check might be okay for finding the room, but review if user details are needed later.
             User user = userRepository.findById(userId)
                .orElse(null); // 사용자가 없으면 null 반환

            if (user == null) {
                 log.warn("User not found for ID: {}", userId);
                 return null; // 사용자가 없으면 활성 방도 없음
             }

            // IN_PROGRESS 또는 WAITING 상태인 방 찾기 (Battle Module Repository)
            Optional<BattleRoom> roomOpt = battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.IN_PROGRESS);

            if (roomOpt.isEmpty()) {
                roomOpt = battleRoomRepository.findActiveRoomByUser(user, BattleRoomStatus.WAITING);
            }

            if (roomOpt.isEmpty()) {
                return null; // 활성 대결방 없음
            }

            // 상세 정보 로드 (N+1 방지 위해 findByIdWithDetails 사용)
            BattleRoom room = battleRoomRepository.findByIdWithDetails(roomOpt.get().getId())
                    .orElse(null);

            if (room == null) {
                 log.warn("Active battle room {} not found after initial check for user {}", roomOpt.get().getId(), userId);
                return null;
            }

            // DTO 변환 (External Service)
            // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic.
            return entityMapperService.mapToBattleRoomResponse(room);
        } catch (Exception e) {
            log.error("Error finding active battle room for user {}: {}", userId, e.getMessage(), e);
            return null; // 예외 발생 시 null 반환
        }
    }

    /**
     * 사용자가 특정 대결방에 참가합니다.
     * 대기 중인 방에만 참가 가능하며, 정원 초과 및 중복 참가를 확인합니다.
     * 참가 시 WebSocket으로 참가자 목록 업데이트 메시지를 전송합니다.
     *
     * @param roomId 참가할 대결방의 ID
     * @param userId 참가하려는 사용자의 ID
     * @return 참가 후 업데이트된 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO)
     * @throws BusinessException 대결방/사용자를 찾을 수 없거나, 이미 시작/참가중/정원초과 상태일 때 (TODO: Use shared_kernel exception)
     */
    public BattleRoomResponse joinBattleRoom(Long roomId, Long userId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // TODO: [MODULAR] Consider alternatives: Fetch needed User data via API/query, or assume userId is valid.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // 이미 시작된 대결인지 확인
        if (battleRoom.getStatus() != BattleRoomStatus.WAITING) { // TODO: Use Battle Module Enum
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작된 대결방입니다."); // TODO: Use shared_kernel exception
        }

        // 정원 초과 확인 (Battle Module Entity Logic)
        if (battleRoom.isParticipantLimitReached()) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL, "대결방이 가득 찼습니다."); // TODO: Use shared_kernel exception
        }

        // 이미 참가 중인지 확인 (Battle Module Repository)
        if (participantRepository.existsByBattleRoomAndUser(battleRoom, user)) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING, "이미 참가 중인 사용자입니다."); // TODO: Use shared_kernel exception
        }

        // 참가자 추가 및 반환값 저장 (Internal method)
        BattleParticipant participant = addParticipant(battleRoom, user);

        // WebSocket 메시지 전송 (Infrastructure)
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/participants", createBattleJoinResponse(battleRoom, participant));

        // TODO: [MODULAR] Consider publishing a ParticipantJoinedEvent if other modules need to react.

        // 업데이트된 방 정보 반환 (DTO 변환 필요)
        // DTO 변환 (External Service - TODO: Implement mapping within battle module)
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }


    /**
     * 대결방에서 사용자의 준비 상태를 토글합니다.
     * 모든 참가자가 준비 완료되면 자동으로 대결을 시작합니다.
     *
     * @param roomId 토글할 대결방 ID
     * @param userId 상태를 토글할 사용자 ID
     * @return 상태 변경 후 업데이트된 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO)
     * @throws BusinessException 대결방/참가자/사용자를 찾을 수 없거나, 상태 변경 불가 조건일 때 (TODO: Use shared_kernel exception)
     */
    @Transactional // 참가자 상태 변경과 대결 시작 로직을 하나의 트랜잭션으로 묶음
    public BattleRoomResponse toggleReady(Long roomId, Long userId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

         User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "해당 대결방의 참가자가 아닙니다.")); // TODO: Use shared_kernel exception

        // 준비 상태 토글 유효성 검사 (대결 상태 등)
        validateReadyToggle(participant); // Internal validation method

        // 준비 상태 토글
        participant.toggleReady();
        participantRepository.save(participant); // 변경된 상태 저장
        log.info("User {} in room {} toggled ready state to: {}", userId, roomId, participant.isReady());

        // 업데이트된 참가자 목록 가져오기 (DB에서 최신 정보 조회)
        List<BattleParticipant> updatedParticipants = participantRepository.findByBattleRoomId(roomId);
        battleRoom.setParticipants(new HashSet<>(updatedParticipants)); // 최신 참가자 목록으로 업데이트

        // 모든 참가자 준비 완료 시 대결 시작 로직 호출
        if (isReadyToStart(battleRoom)) {
            startBattle(roomId); // 대결 시작 (startBattle 내부에서 WebSocket 메시지 전송)
            // startBattle 후 battleRoom 상태가 변경되었으므로 다시 로드
             battleRoom = battleRoomRepository.findByIdWithDetails(roomId).orElse(battleRoom); // 재조회, 실패 시 이전 상태 유지
        } else {
             // 준비 상태 변경 알림 (WebSocket)
             // TODO: Ensure BattleReadyResponse DTO exists in battle module
             BattleReadyResponse readyResponse = createBattleReadyResponse(battleRoom, updatedParticipants);
             messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/ready", readyResponse);
         }

        // 업데이트된 방 정보 반환 (DTO 변환 필요)
        // DTO 변환 (External Service - TODO: Implement mapping within battle module)
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 사용자가 대결방에서 나갑니다.
     * 대기 중인 방에서 나가면 참가자 목록에서 제거됩니다.
     * 진행 중인 방에서 나가면 패배 처리되고 방 상태에 따라 처리됩니다.
     *
     * @param roomId 나갈 대결방 ID
     * @param userId 나가는 사용자 ID
     * @return 퇴장 처리 후 업데이트된 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO), 방이 삭제되면 null
     * @throws BusinessException 대결방/참가자/사용자를 찾을 수 없을 때 (TODO: Use shared_kernel exception)
     */
    @Transactional
    public BattleRoomResponse leaveBattleRoom(Long roomId, Long userId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        // TODO: [MODULAR] Consider alternatives: Fetch needed User data via API/query, or assume userId is valid.
        User user = userRepository.findById(userId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.")); // TODO: [REFACTOR] Use shared_kernel exception

        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "해당 대결방의 참가자가 아닙니다.")); // TODO: Use shared_kernel exception

        log.info("User {} leaving battle room {}", userId, roomId);

        // 세션 정보 제거 (Redis)
        String sessionId = getSessionIdFromRedis(userId);
        if (sessionId != null) {
            removeParticipantFromRedis(sessionId, userId);
        }

        // 참가자 엔티티 제거
        participantRepository.delete(participant);
        battleRoom.getParticipants().remove(participant); // 컬렉션에서도 제거

        // 남은 참가자 수 확인
        int remainingParticipants = participantRepository.countByBattleRoomId(roomId);
        log.debug("Remaining participants in room {}: {}", roomId, remainingParticipants);

        BattleLeaveResponse leaveResponse = BattleLeaveResponse.builder()
                .roomId(roomId)
                .userId(userId)
                .remainingParticipants(remainingParticipants)
                .build();

        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/leave", leaveResponse);


        // 방 상태에 따른 후처리
        if (remainingParticipants == 0) {
            // 마지막 참가자가 나간 경우, 방 삭제
            log.info("Last participant left room {}. Deleting room.", roomId);
            battleRoomRepository.delete(battleRoom);
            // TODO: Publish RoomDeletedEvent?
            return null; // 방이 삭제되었으므로 null 반환
        } else {
            // 다른 참가자가 남은 경우
            if (participant.getId().equals(battleRoom.getCreatorId()) && battleRoom.getStatus() == BattleRoomStatus.WAITING) {
                 // 방장이 나갔고, 대기 중인 방이면 다음 참가자를 방장으로 위임
                 BattleParticipant newHost = battleRoom.getParticipants().stream().findFirst().orElse(null);
                 if (newHost != null) {
                     battleRoom.setCreatorId(newHost.getUser().getId());
                     log.info("Host left room {}. New host assigned: {}", roomId, newHost.getUser().getId());
                     // TODO: Notify participants about new host?
                 }
             }

            // 진행 중인 방이었고, 나간 사용자가 패배하지 않은 상태였다면 패배 처리
            if (battleRoom.getStatus() == BattleRoomStatus.IN_PROGRESS && !participant.isDefeated()) {
                participant.markAsDefeated(); // 패배 처리
                // TODO: Update participant score/stats if necessary
                log.info("User {} marked as defeated in room {} due to leaving.", userId, roomId);

                // 모든 참가자가 응답했는지(패배 포함) 확인하고 다음 문제 진행 또는 종료
                if (allParticipantsRespondedOrDefeated(roomId)) {
                     prepareNextQuestionOrEnd(roomId);
                 }
            }

            battleRoomRepository.save(battleRoom); // 변경사항 저장 (방장 변경 등)
            // DTO 변환 (External Service - TODO: Implement mapping within battle module)
            return entityMapperService.mapToBattleRoomResponse(battleRoom);
        }
    }

    /**
     * WebSocket 연결 시 사용자의 세션 ID와 참가자 정보를 Redis에 저장하고,
     * 참가자 목록을 반환하며 다른 참가자들에게 알립니다.
     *
     * @param request   참가 요청 데이터 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 참가자 목록 및 방 정보를 담은 {@link BattleJoinResponse}
     * @throws BusinessException 방/사용자를 찾을 수 없거나, Redis 저장 실패 시
     */
    @Transactional // DB 조회 및 Redis 저장을 원자적으로 처리
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();

        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // Redis에 세션 정보 저장
        saveParticipantToRedis(participant, sessionId);

        // 현재 참가자 목록 조회
        List<BattleParticipant> participants = participantRepository.findByBattleRoomId(roomId);
        battleRoom.setParticipants(new HashSet<>(participants)); // 엔티티에 최신 목록 반영

        // 응답 생성 및 반환
        BattleJoinResponse response = createBattleJoinResponse(battleRoom, participant);
        log.info("User {} (session {}) connected to battle room {}", userId, sessionId, roomId);

        // 다른 참가자들에게 새로운 참가자 알림 (본인 제외)
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/participants", response);

        return response;
    }

    /**
     * 사용자의 퀴즈 답변을 처리합니다.
     * 답변의 정답 여부를 확인하고, 점수를 업데이트하며, 결과를 저장합니다.
     * 모든 참가자가 답변하면 다음 문제로 넘어가거나 대결을 종료합니다.
     *
     * @param request   답변 요청 데이터 (roomId, userId, questionId, answer)
     * @param sessionId 답변을 제출한 사용자의 WebSocket 세션 ID
     * @return 답변 처리 결과 (정답 여부, 점수 등)를 담은 {@link BattleAnswerResponse}
     * @throws BusinessException 방/참가자/질문을 찾을 수 없거나, 잘못된 상태일 때
     */
    @Transactional
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        Long questionId = request.getQuestionId();
        String submittedAnswer = request.getAnswer();

        log.debug("Processing answer for room {}, user {}, question {}: {}", roomId, userId, questionId, submittedAnswer);

        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null || !participant.getUser().getId().equals(userId)) {
            // DB에서 다시 조회 시도 (Redis 정보가 유실되었을 경우)
            log.warn("Participant info not found in Redis for session {}. Querying DB.", sessionId);
            BattleRoom room = battleRoomRepository.findById(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
            participant = participantRepository.findByBattleRoomAndUser(room, user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다."));
        }

        BattleRoom battleRoom = participant.getBattleRoom(); // 참가자 통해 방 정보 접근

        // 대결 상태 및 현재 질문 유효성 검사
        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "대결이 진행 중이 아닙니다.");
        }
        if (battleRoom.getCurrentQuestionIndex() == null || battleRoom.getCurrentQuestionIndex() >= battleRoom.getQuiz().getQuestions().size()) {
             throw new BusinessException(ErrorCode.INVALID_BATTLE_STATE, "유효하지 않은 질문 상태입니다.");
         }
        Question currentQuestion = battleRoom.getQuiz().getQuestions().get(battleRoom.getCurrentQuestionIndex());
        if (!currentQuestion.getId().equals(questionId)) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION_SEQUENCE, "현재 진행 중인 질문이 아닙니다.");
        }

        // 이미 답변했는지 확인 (중복 답변 방지)
         if (participant.getAnswers().stream().anyMatch(ans -> ans.getQuestion().getId().equals(questionId))) {
             log.warn("User {} already answered question {} in room {}", userId, questionId, roomId);
             throw new BusinessException(ErrorCode.ANSWER_ALREADY_SUBMITTED, "이미 답변을 제출했습니다.");
         }

        // 정답 확인 및 점수 계산
        boolean isCorrect = currentQuestion.getAnswer().equalsIgnoreCase(submittedAnswer);
        int scoreGained = isCorrect ? calculateScore(battleRoom.getTimeLimitSeconds()) : 0; // TODO: Implement time-based scoring?

        // 답변 기록 생성 및 저장
        BattleAnswer answer = BattleAnswer.builder()
                .participant(participant)
                .question(currentQuestion)
                .submittedAnswer(submittedAnswer)
                .isCorrect(isCorrect)
                .scoreGained(scoreGained)
                .answeredAt(LocalDateTime.now())
                .build();
        participant.addAnswer(answer); // 연관관계 편의 메소드
        participant.addScore(scoreGained); // 참가자 점수 업데이트
        // battleAnswerRepository.save(answer); // CascadeType.PERSIST 또는 MERGE 설정 시 불필요

        participantRepository.save(participant); // 참가자 정보 업데이트 (점수, 답변 목록)

        log.info("User {} answered question {} in room {}. Correct: {}, Score gained: {}", userId, questionId, roomId, isCorrect, scoreGained);

        // 모든 참가자가 답변했는지 확인 후 다음 단계 진행
        if (allParticipantsRespondedOrDefeated(roomId)) {
            prepareNextQuestionOrEnd(roomId);
        }

        // 응답 생성 및 반환
        return createBattleAnswerResponse(answer);
    }


    /**
     * 대결방의 모든 참가자가 준비 완료 상태인지 확인합니다.
     * 최소 참가자 수 조건도 함께 확인합니다.
     *
     * @param roomId 확인할 대결방 ID
     * @return 모든 조건 만족 시 true, 아니면 false
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public synchronized boolean isReadyToStart(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));
        return isReadyToStart(battleRoom); // 오버로딩된 메소드 호출
    }

    /**
     * 대결방의 모든 참가자가 준비 완료 상태인지 확인합니다. (내부 사용)
     * 최소 참가자 수 조건도 함께 확인합니다.
     *
     * @param battleRoom 확인할 BattleRoom 엔티티 (참가자 정보 포함)
     * @return 모든 조건 만족 시 true, 아니면 false
     */
    private synchronized boolean isReadyToStart(BattleRoom battleRoom) {
        if (battleRoom.getStatus() != BattleRoomStatus.WAITING) {
            log.warn("Room {} is not in WAITING state, cannot start.", battleRoom.getId());
            return false;
        }
        // TODO: 최소 참가자 수 설정 가능하게 변경 (e.g., battleRoom.getMinParticipants())
        int minParticipants = 2;
        if (battleRoom.getParticipants().size() < minParticipants) {
            log.debug("Room {} has {} participants, less than minimum required {}. Cannot start.",
                      battleRoom.getId(), battleRoom.getParticipants().size(), minParticipants);
            return false;
        }
        boolean allReady = battleRoom.getParticipants().stream().allMatch(BattleParticipant::isReady);
        if (allReady) {
            log.info("All {} participants in room {} are ready.", battleRoom.getParticipants().size(), battleRoom.getId());
        } else {
             log.debug("Not all participants in room {} are ready.", battleRoom.getId());
         }
        return allReady;
    }

    /**
     * 대결을 시작합니다.
     * 방 상태를 IN_PROGRESS로 변경하고, 첫 번째 문제를 준비하여 참가자들에게 전송합니다.
     *
     * @param roomId 시작할 대결방 ID
     * @return 대결 시작 정보 (참가자, 첫 문제 등) 를 담은 {@link BattleStartResponse}
     * @throws BusinessException 방을 찾을 수 없거나, 시작할 수 없는 상태일 때
     */
    @Transactional
    public synchronized BattleStartResponse startBattle(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        if (battleRoom.getStatus() != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED_OR_ENDED, "대결을 시작할 수 없는 상태입니다."); // TODO: Use shared_kernel exception
        }
        if (!isReadyToStart(battleRoom)) {
             throw new BusinessException(ErrorCode.NOT_ALL_PARTICIPANTS_READY, "모든 참가자가 준비되지 않았습니다."); // TODO: Use shared_kernel exception
         }

        log.info("Starting battle in room {}", roomId);

        battleRoom.startBattle(); // 방 상태 변경, 시작 시간 기록 등
        battleRoomRepository.save(battleRoom);

        // 첫 번째 문제 준비 및 전송
        BattleNextQuestionResponse firstQuestionResponse = prepareNextQuestion(roomId); // 첫 문제 로드 및 상태 업데이트

        // 대결 시작 응답 생성
        BattleStartResponse startResponse = createBattleStartResponse(battleRoom);
        startResponse.setNextQuestion(firstQuestionResponse); // 시작 정보에 첫 문제 정보 포함

        // WebSocket으로 대결 시작 알림
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/start", startResponse);

        return startResponse;
    }


    /**
     * 다음 문제를 준비하거나, 모든 문제가 출제되었으면 대결을 종료합니다.
     * 현재 질문 인덱스를 증가시키고, 다음 문제 정보를 참가자들에게 전송합니다.
     *
     * @param roomId 진행 중인 대결방 ID
     * @return 다음 문제 정보 (다음 문제가 없을 경우 null?) - 수정: 종료 시 BattleEndResponse 전송 고려
     * @throws BusinessException 방을 찾을 수 없거나, 진행 중이 아닐 때
     */
    @Transactional // 방 상태 변경과 메시지 전송을 묶음
    public BattleNextQuestionResponse prepareNextQuestion(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            log.warn("Battle room {} is not IN_PROGRESS, cannot prepare next question.", roomId);
            // 이미 종료되었거나 다른 상태일 수 있으므로 에러 대신 null 또는 특정 응답 반환 고려
             return null;
            // throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "대결이 진행 중이 아닙니다.");
        }

        int nextQuestionIndex = battleRoom.getCurrentQuestionIndex() == null ? 0 : battleRoom.getCurrentQuestionIndex() + 1;
        List<Question> questions = battleRoom.getQuiz().getQuestions();

        if (nextQuestionIndex >= questions.size()) {
            // 모든 문제 출제 완료, 대결 종료 로직 호출
            log.info("All questions answered in room {}. Ending battle.", roomId);
            endBattle(roomId); // 종료 처리 및 결과 전송
            return null; // 다음 문제가 없으므로 null 반환
        } else {
            // 다음 문제 준비
            battleRoom.setCurrentQuestionIndex(nextQuestionIndex);
            battleRoom.setQuestionStartTime(LocalDateTime.now()); // 다음 문제 시작 시간 기록
            battleRoomRepository.save(battleRoom);

            Question nextQuestion = questions.get(nextQuestionIndex);
            boolean isLastQuestion = (nextQuestionIndex == questions.size() - 1);

            BattleNextQuestionResponse nextQuestionResponse = createNextQuestionResponse(nextQuestion, isLastQuestion);

            // WebSocket으로 다음 문제 정보 전송
            messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/next-question", nextQuestionResponse);
            log.info("Prepared next question (index {}) for room {}", nextQuestionIndex, roomId);

            return nextQuestionResponse;
        }
    }


    /**
     * 현재 문제에 대해 모든 참가자가 답변했는지 또는 패배했는지 확인합니다.
     *
     * @param roomId 확인할 대결방 ID
     * @return 모든 참가자가 응답했으면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public synchronized boolean allParticipantsRespondedOrDefeated(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
             log.warn("Room {} is not in progress, skipping check.", roomId);
             return false; // 진행 중이 아니면 false
         }

        if (battleRoom.getCurrentQuestionIndex() == null) {
            log.warn("Current question index is null for room {}, cannot check responses.", roomId);
            return false; // 현재 문제가 없으면 false
        }

        Question currentQuestion = battleRoom.getQuiz().getQuestions().get(battleRoom.getCurrentQuestionIndex());
        Long currentQuestionId = currentQuestion.getId();

        // 현재 활동 중인 (패배하지 않은) 참가자 수
        long activeParticipantsCount = battleRoom.getParticipants().stream()
                .filter(p -> !p.isDefeated())
                .count();

        if (activeParticipantsCount == 0) {
            log.info("No active participants remaining in room {}.", roomId);
            return true; // 활동 중인 참가자가 없으면 모두 응답한 것으로 간주 (종료 로직으로 이어짐)
        }

        // 현재 문제에 대해 답변한 활동 참가자 수 계산
        long answeredCount = battleRoom.getParticipants().stream()
                .filter(p -> !p.isDefeated()) // 패배하지 않은 참가자만 고려
                .filter(p -> p.getAnswers().stream()
                        .anyMatch(a -> a.getQuestion().getId().equals(currentQuestionId)))
                .count();

        log.debug("Room {}: Active participants = {}, Answered for question {} = {}",
                  roomId, activeParticipantsCount, currentQuestionId, answeredCount);

        return answeredCount >= activeParticipantsCount;
    }



    /**
     * 대결 진행 상황 (참가자별 점수 등) 정보를 조회합니다.
     *
     * @param roomId 조회할 대결방 ID
     * @return 대결 진행 상황 정보를 담은 {@link BattleProgressResponse}
     * @throws BusinessException 방을 찾을 수 없을 때
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // DTO 변환 로직 필요 (BattleProgress 객체 생성)
        // TODO: Move BattleProgress DTO and mapping logic
        BattleProgress progress = new BattleProgress(); // 임시 객체
        progress.setRoomId(roomId);
        // 참가자별 점수, 순위 등 정보 채우기
         List<BattleProgress.ParticipantProgress> participantProgressList = battleRoom.getParticipants().stream()
                 .map(p -> new BattleProgress.ParticipantProgress(
                         p.getUser().getId(),
                         p.getUser().getUsername(), // TODO: Consider fetching less user data
                         p.getScore(),
                         p.isReady(), // 상태 정보 포함
                         p.isDefeated()
                 ))
                 .sorted(Comparator.comparingInt(BattleProgress.ParticipantProgress::getScore).reversed()) // 점수 내림차순 정렬
                 .collect(Collectors.toList());
         progress.setParticipantsProgress(participantProgressList);
         progress.setStatus(battleRoom.getStatus()); // 현재 방 상태
         progress.setCurrentQuestionIndex(battleRoom.getCurrentQuestionIndex()); // 현재 문제 인덱스


        return createBattleProgressResponse(progress); // DTO 매핑
    }


    /**
     * 대결을 종료 처리합니다.
     * 방 상태를 ENDED로 변경하고, 최종 결과를 계산하여 저장 및 반환합니다.
     * 경험치 및 통계 업데이트 로직을 호출합니다.
     *
     * @param roomId 종료할 대결방 ID
     * @return 대결 결과 정보를 담은 {@link BattleEndResponse}
     * @throws BusinessException 방을 찾을 수 없거나, 종료할 수 없는 상태일 때
     */
    @Transactional
    public BattleEndResponse endBattle(Long roomId) {
        log.info("Ending battle for room ID: {}", roomId);
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "Cannot end battle. Room not found.")); // TODO: [REFACTOR] Use shared_kernel exception

        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "Battle is not in progress."); // TODO: [REFACTOR] Use shared_kernel exception
        }

        battleRoom.endBattle(); // 상태 변경 (Domain Logic)
        LocalDateTime endTime = battleRoom.getEndTime();

        // 결과 계산 (내부 메소드)
        BattleResult battleResult = calculateBattleResult(battleRoom); // TODO: Review calculateBattleResult for cross-module dependencies (e.g., User info for winner?)

        // --- Start of Event-Driven Replacement Area ---
        // TODO: [MODULAR] Remove the following direct calls (awardExperiencePoints, updateStatistics)
        // TODO: [MODULAR] Instead, publish a BattleEndedEvent containing relevant data from battleResult (e.g., roomId, List<ParticipantResultData>, quizId, startTime, endTime).

        // 경험치 지급 (External Service Call)
        awardExperiencePoints(battleResult);

        // 통계 업데이트 (User Module Repository Call)
        updateStatistics(battleResult);
        // --- End of Event-Driven Replacement Area ---

        // 대결방 정보 DB 업데이트 (Battle Module Repository)
        battleRoomRepository.save(battleRoom);

        // Redis 정보 정리 (Infrastructure)
        // TODO: [REFACTOR] Clear relevant Redis keys for the ended battle room and participants.

        // DTO 변환
        // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic.
        // TODO: [REFACTOR] Ensure BattleResult is mapped correctly to BattleEndResponse (or use BattleResultResponse directly if suitable).
        BattleEndResponse response = entityMapperService.mapToBattleEndResponse(battleResult);

        // WebSocket 메시지 전송 (Infrastructure)
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/end", response);

        log.info("Battle room {} ended successfully.", roomId);
        return response;
    }

    // --- Helper Methods ---

    /**
     * 대결방에 참가자를 추가하고 DB와 Redis에 저장합니다.
     *
     * @param battleRoom 참가자를 추가할 BattleRoom
     * @param user       추가될 User
     * @return 생성된 BattleParticipant 엔티티
     */
    private BattleParticipant addParticipant(BattleRoom battleRoom, User user) {
        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(battleRoom)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();
        battleRoom.addParticipant(participant); // 연관관계 설정
        BattleParticipant savedParticipant = participantRepository.save(participant); // DB 저장

        // Redis에 세션 정보 매핑 (세션 ID는 WebSocket 연결 시 설정)
        // saveParticipantToRedis(savedParticipant, null); // 초기에는 세션 ID 없음

        log.info("Added participant {} to room {}", user.getId(), battleRoom.getId());
        return savedParticipant;
    }


    /**
     * 참가자 정보와 세션 ID를 Redis에 저장합니다.
     * sessionId -> participant 객체, userId -> sessionId 두 가지 매핑을 저장합니다.
     *
     * @param participant 저장할 BattleParticipant 엔티티
     * @param sessionId   해당 참가자의 WebSocket 세션 ID
     */
    private void saveParticipantToRedis(BattleParticipant participant, String sessionId) {
         if (sessionId == null) {
             log.warn("Session ID is null for participant {}, cannot save to Redis session mapping.", participant.getId());
             return;
         }
         try {
             String sessionKey = PARTICIPANT_SESSION_KEY_PREFIX + sessionId;
             String userKey = PARTICIPANT_USER_KEY_PREFIX + participant.getUser().getId();

             // 직렬화 가능한 DTO 또는 객체로 변환하여 저장 고려
             // 여기서는 간단히 participant 객체를 저장 (Redis 설정에 따라 직렬화 필요)
             redisTemplate.opsForValue().set(sessionKey, participant, PARTICIPANT_EXPIRE_SECONDS, TimeUnit.SECONDS);
             redisTemplate.opsForValue().set(userKey, sessionId, PARTICIPANT_EXPIRE_SECONDS, TimeUnit.SECONDS); // userId로 세션 ID 찾기

             log.debug("Saved participant {} (session {}) to Redis.", participant.getId(), sessionId);
         } catch (Exception e) {
             log.error("Failed to save participant {} (session {}) to Redis: {}", participant.getId(), sessionId, e.getMessage(), e);
             // Redis 저장 실패 시 예외 처리 또는 로깅 강화
         }
    }

    /**
     * Redis에서 세션 ID를 기반으로 참가자 정보를 조회합니다.
     *
     * @param sessionId 조회할 WebSocket 세션 ID
     * @return 조회된 BattleParticipant 객체, 없거나 타입 오류 시 null
     */
    private BattleParticipant getParticipantFromRedis(String sessionId) {
        if (sessionId == null) return null;
        try {
            String sessionKey = PARTICIPANT_SESSION_KEY_PREFIX + sessionId;
            Object obj = redisTemplate.opsForValue().get(sessionKey);
            if (obj instanceof BattleParticipant) {
                 log.debug("Retrieved participant from Redis for session {}", sessionId);
                return (BattleParticipant) obj;
            } else if (obj != null) {
                 log.warn("Object found in Redis for session {} is not of type BattleParticipant: {}", sessionId, obj.getClass().getName());
             } else {
                 log.debug("No participant found in Redis for session {}", sessionId);
             }
        } catch (Exception e) {
            log.error("Failed to get participant from Redis for session {}: {}", sessionId, e.getMessage(), e);
        }
        return null;
    }

     /**
      * Redis에서 사용자 ID를 기반으로 세션 ID를 조회합니다.
      *
      * @param userId 조회할 사용자 ID
      * @return 조회된 WebSocket 세션 ID, 없으면 null
      */
     private String getSessionIdFromRedis(Long userId) {
         if (userId == null) return null;
         try {
             String userKey = PARTICIPANT_USER_KEY_PREFIX + userId;
             Object obj = redisTemplate.opsForValue().get(userKey);
             if (obj instanceof String) {
                 log.debug("Retrieved session ID from Redis for user {}", userId);
                 return (String) obj;
             } else {
                 log.debug("No session ID found in Redis for user {}", userId);
             }
         } catch (Exception e) {
             log.error("Failed to get session ID from Redis for user {}: {}", userId, e.getMessage(), e);
         }
         return null;
     }

     /**
      * Redis에서 참가자 및 세션 매핑 정보를 제거합니다.
      *
      * @param sessionId 제거할 WebSocket 세션 ID
      * @param userId    제거할 사용자 ID
      */
     private void removeParticipantFromRedis(String sessionId, Long userId) {
         try {
             if (sessionId != null) {
                 String sessionKey = PARTICIPANT_SESSION_KEY_PREFIX + sessionId;
                 redisTemplate.delete(sessionKey);
                 log.debug("Removed participant session data from Redis for session {}", sessionId);
             }
             if (userId != null) {
                 String userKey = PARTICIPANT_USER_KEY_PREFIX + userId;
                 redisTemplate.delete(userKey);
                 log.debug("Removed user-session mapping from Redis for user {}", userId);
             }
         } catch (Exception e) {
             log.error("Failed to remove participant data from Redis (session: {}, user: {}): {}", sessionId, userId, e.getMessage(), e);
         }
     }


    /**
     * 대결 참가 응답 DTO (BattleJoinResponse)를 생성합니다.
     * 현재 방 정보와 참가자 목록을 포함합니다.
     *
     * @param room           현재 BattleRoom 엔티티 (참가자 포함)
     * @param newParticipant 새로 참가한 BattleParticipant (선택적)
     * @return 생성된 BattleJoinResponse DTO
     */
    private BattleJoinResponse createBattleJoinResponse(BattleRoom room, BattleParticipant newParticipant) {
        // TODO: Use EntityMapperService or MapStruct for mapping
        List<BattleJoinResponse.ParticipantInfo> participantInfos = room.getParticipants().stream()
                .map(p -> BattleJoinResponse.ParticipantInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername()) // TODO: Pass only necessary info
                        .isReady(p.isReady())
                        .isHost(p.getId().equals(room.getCreatorId())) // Use participant ID for host check? Check BattleRoom creatorId type. Assuming creatorId is userId.
                        .isNewJoiner(newParticipant != null && p.getId().equals(newParticipant.getId())) // 새로 참가한 사용자인지 여부
                        .build())
                .collect(Collectors.toList());

        return BattleJoinResponse.builder()
                .roomId(room.getId())
                .quizTitle(room.getQuiz().getTitle())
                .maxParticipants(room.getMaxParticipants())
                .currentParticipants(participantInfos.size())
                .participants(participantInfos)
                .status(room.getStatus()) // 현재 방 상태 추가
                .build();
    }


    /**
     * 답변 처리 결과 응답 DTO (BattleAnswerResponse)를 생성합니다.
     *
     * @param answer 처리된 BattleAnswer 엔티티
     * @return 생성된 BattleAnswerResponse DTO
     */
    private BattleAnswerResponse createBattleAnswerResponse(BattleAnswer answer) {
         // TODO: Use EntityMapperService or MapStruct for mapping
         return BattleAnswerResponse.builder()
                 .userId(answer.getParticipant().getUser().getId())
                 .questionId(answer.getQuestion().getId())
                 .isCorrect(answer.isCorrect())
                 .scoreGained(answer.getScoreGained())
                 .totalScore(answer.getParticipant().getScore()) // 참가자의 누적 점수
                 .answeredAt(answer.getAnsweredAt())
                 .build();
    }


    /**
     * 대결 시작 응답 DTO (BattleStartResponse)를 생성합니다.
     * 참가자 목록과 첫 번째 문제 정보를 포함 (별도 설정 필요).
     *
     * @param room 시작된 BattleRoom 엔티티
     * @return 생성된 BattleStartResponse DTO
     */
    private BattleStartResponse createBattleStartResponse(BattleRoom room) {
        // TODO: Use EntityMapperService or MapStruct for mapping
        List<BattleStartResponse.ParticipantStartInfo> participantInfos = room.getParticipants().stream()
                .map(p -> BattleStartResponse.ParticipantStartInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername()) // TODO: Pass necessary info only
                        .initialScore(p.getScore()) // 시작 시점 점수 (보통 0)
                        .build())
                .collect(Collectors.toList());

        return BattleStartResponse.builder()
                .roomId(room.getId())
                .quizId(room.getQuiz().getId())
                .quizTitle(room.getQuiz().getTitle())
                .startTime(room.getStartTime())
                .timeLimitSeconds(room.getTimeLimitSeconds()) // 문제당 시간 제한 정보 추가
                .participants(participantInfos)
                .build();
        // Note: firstQuestion will be set after calling prepareNextQuestion
    }


    /**
     * 다음 문제 정보 응답 DTO (BattleNextQuestionResponse)를 생성합니다.
     *
     * @param question 다음 Question 엔티티
     * @param isLast   마지막 문제인지 여부
     * @return 생성된 BattleNextQuestionResponse DTO
     */
    private BattleNextQuestionResponse createNextQuestionResponse(Question question, boolean isLast) {
         // TODO: Use EntityMapperService or MapStruct for mapping
         // 답변은 제외하고 문제 내용만 전달
         return BattleNextQuestionResponse.builder()
                 .questionId(question.getId())
                 .questionText(question.getText())
                 .options(question.getOptions()) // TODO: Ensure options are correctly fetched/mapped
                 .questionIndex(question.getSequence()) // 문제 순서 정보 추가
                 .isLastQuestion(isLast)
                 .build();
    }


    /**
     * 대결 진행 상황 응답 DTO (BattleProgressResponse)를 생성합니다.
     *
     * @param progress BattleProgress 객체 (계산된 진행 정보)
     * @return 생성된 BattleProgressResponse DTO
     */
    private BattleProgressResponse createBattleProgressResponse(BattleProgress progress) {
        // TODO: Use EntityMapperService or MapStruct for mapping
        // BattleProgress 객체를 BattleProgressResponse DTO로 변환
         List<BattleProgressResponse.ParticipantProgressInfo> participantProgressInfos =
                 progress.getParticipantsProgress().stream()
                         .map(p -> BattleProgressResponse.ParticipantProgressInfo.builder()
                                 .userId(p.getUserId())
                                 .username(p.getUsername())
                                 .score(p.getScore())
                                 .isReady(p.isReady())
                                 .isDefeated(p.isDefeated())
                                 // TODO: 순위 정보 추가?
                                 .build())
                         .collect(Collectors.toList());

         return BattleProgressResponse.builder()
                 .roomId(progress.getRoomId())
                 .status(progress.getStatus())
                 .currentQuestionIndex(progress.getCurrentQuestionIndex())
                 .participantsProgress(participantProgressInfos)
                 .build();
    }

     /**
      * 대결 종료 응답 DTO (BattleEndResponse)를 생성합니다.
      *
      * @param result 계산된 BattleResult 객체
      * @return 생성된 BattleEndResponse DTO
      */
     private BattleEndResponse createBattleEndResponse(BattleResult result) {
         // TODO: Use EntityMapperService or MapStruct for mapping
         List<BattleEndResponse.ParticipantResultInfo> participantResultInfos =
                 result.getParticipants().stream()
                         .map(p -> BattleEndResponse.ParticipantResultInfo.builder()
                                 .userId(p.getUserId())
                                 .username(p.getUsername())
                                 .finalScore(p.getScore())
                                 .rank(p.getRank()) // 순위 정보
                                 .isWinner(p.getRank() == 1) // 1위가 우승자
                                 .experienceGained(p.getExperienceGained()) // 획득 경험치
                                 .build())
                         .collect(Collectors.toList());

         return BattleEndResponse.builder()
                 .roomId(result.getRoomId())
                 .quizId(result.getQuizId())
                 .endTime(result.getEndTime() != null ? result.getEndTime() : LocalDateTime.now()) // 종료 시간 설정
                 .results(participantResultInfos)
                 .build();
     }


    /**
     * 대결 최종 결과를 계산합니다. (참가자 순위 포함)
     *
     * @param room 종료된 BattleRoom 엔티티 (참가자 및 점수 포함)
     * @return 계산된 BattleResult 객체
     */
    private BattleResult calculateBattleResult(BattleRoom room) {
        // 참가자들을 점수 내림차순, 동점 시 먼저 참가한 순? 또는 이름순? 으로 정렬
        List<BattleParticipant> sortedParticipants = room.getParticipants().stream()
                .sorted(Comparator.comparingInt(BattleParticipant::getScore).reversed()
                        // .thenComparing(BattleParticipant::getJoinedAt) // 동점자 처리 기준 추가 가능
                )
                .collect(Collectors.toList());

        // 순위 매기기
        int rank = 0;
        int currentRank = 0;
        int previousScore = -1;
        List<BattleResult.ParticipantResult> participantResults = new ArrayList<>();

        for (BattleParticipant p : sortedParticipants) {
             rank++;
             if (p.getScore() != previousScore) {
                 currentRank = rank;
                 previousScore = p.getScore();
             }
             // TODO: Experience calculation logic needed
             int experienceGained = 0; // 임시값, LevelingService 호출 결과 받아와야 함

             participantResults.add(BattleResult.ParticipantResult.builder()
                     .userId(p.getUser().getId())
                     .username(p.getUser().getUsername()) // TODO: Pass necessary info only
                     .score(p.getScore())
                     .rank(currentRank)
                     .experienceGained(experienceGained) // 추후 설정
                     .isDefeated(p.isDefeated())
                     .build());
        }

        return BattleResult.builder()
                .roomId(room.getId())
                .quizId(room.getQuiz().getId())
                .endTime(room.getEndTime() != null ? room.getEndTime() : LocalDateTime.now()) // 종료 시간 설정
                .participants(participantResults)
                .build();
    }


    /**
     * 대결 결과에 따라 참가자들에게 경험치를 부여합니다. (LevelingService 호출)
     *
     * @param result 계산된 BattleResult 객체
     */
    private void awardExperiencePoints(BattleResult result) {
        log.info("Awarding experience points for battle room {}", result.getRoomId());
        for (BattleResult.ParticipantResult participantResult : result.getParticipants()) {
            try {
                // TODO: Replace direct service call with event publishing (e.g., BattleEndedEvent)
                 // LevelingService에 경험치 부여 요청
                 // 경험치 계산 로직은 LevelingService 내부에 있거나, 여기서 계산해서 전달
                 int calculatedExp = calculateExperience(participantResult.getScore(), participantResult.getRank(), result.getParticipants().size());
                 levelingService.addExperience(participantResult.getUserId(), calculatedExp, "Battle Participation"); // TODO: Check LevelingService method signature
                 // 결과 객체에 실제 부여된 경험치 업데이트 (LevelingService 응답이 있다면)
                 participantResult.setExperienceGained(calculatedExp); // TODO: Update based on actual exp awarded by LevelingService
                 log.debug("Awarded {} EXP to user {}", calculatedExp, participantResult.getUserId());
            } catch (Exception e) {
                // 개별 사용자 경험치 부여 실패 시 로깅만 하고 계속 진행
                log.error("Failed to award experience to user {}: {}", participantResult.getUserId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 대결 결과에 따라 참가자들의 통계를 업데이트합니다. (UserBattleStatsRepository 사용)
     *
     * @param result 계산된 BattleResult 객체
     */
    private void updateStatistics(BattleResult result) {
         log.info("Updating battle statistics for room {}", result.getRoomId());
         for (BattleResult.ParticipantResult participantResult : result.getParticipants()) {
             try {
                 // TODO: Consider replacing direct repository access with User module API call or event
                 User user = userRepository.findById(participantResult.getUserId())
                         .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자 통계 업데이트 실패: 사용자 없음")); // TODO: Use shared_kernel exception

                 UserBattleStats stats = userBattleStatsRepository.findByUser(user)
                         .orElseGet(() -> {
                             UserBattleStats newStats = new UserBattleStats(user);
                             user.setUserBattleStats(newStats); // 연관관계 설정
                             return newStats;
                         });

                 stats.incrementBattlesPlayed();
                 stats.addTotalScore(participantResult.getScore());
                 if (participantResult.getRank() == 1) {
                     stats.incrementWins();
                 }
                 // 최고 점수 업데이트 등 추가 통계 업데이트 가능

                 userBattleStatsRepository.save(stats); // 통계 저장
                 log.debug("Updated stats for user {}: battles={}, wins={}, totalScore={}",
                           user.getId(), stats.getBattlesPlayed(), stats.getWins(), stats.getTotalScore());

             } catch (Exception e) {
                 // 개별 사용자 통계 업데이트 실패 시 로깅만 하고 계속 진행
                 log.error("Failed to update statistics for user {}: {}", participantResult.getUserId(), e.getMessage(), e);
             }
         }
    }


    /**
     * WebSocket 연결 해제 등 비정상 종료 시 사용자의 퇴장 처리를 합니다.
     * 세션 ID를 기반으로 사용자를 찾아 퇴장 로직(leaveBattleRoom)을 호출합니다.
     *
     * @param sessionId 연결이 끊어진 WebSocket 세션 ID
     * @return 퇴장 처리 결과 ({@link BattleLeaveResponse}), 사용자를 찾지 못하면 null
     */
     @Transactional
     public BattleLeaveResponse handleDisconnection(String sessionId) {
         log.warn("Handling disconnection for session: {}", sessionId);
         BattleParticipant participant = getParticipantFromRedis(sessionId);

         if (participant == null) {
             log.error("Cannot find participant in Redis for disconnected session: {}", sessionId);
             // DB에서 세션 ID로 참가자를 찾는 로직 추가? (일반적으로 어려움)
             return null; // 처리할 참가자 없음
         }

         Long roomId = participant.getBattleRoom().getId();
         Long userId = participant.getUser().getId();
         log.info("User {} (room {}) disconnected (session {}). Processing leave.", userId, roomId, sessionId);

         // Redis에서 정보 먼저 제거
         removeParticipantFromRedis(sessionId, userId);

         try {
             // leaveBattleRoom 메소드 호출하여 DB 처리 및 알림 전송
             BattleRoomResponse roomResponse = leaveBattleRoom(roomId, userId); // Use existing leave logic
             // leaveBattleRoom이 null을 반환하면 방이 삭제된 것
             int remainingParticipants = (roomResponse != null) ?
                     participantRepository.countByBattleRoomId(roomId) : 0; // 방 삭제 시 0명

             return BattleLeaveResponse.builder()
                     .roomId(roomId)
                     .userId(userId)
                     .remainingParticipants(remainingParticipants)
                     .message("User disconnected and processed.")
                     .build();
         } catch (BusinessException e) {
              // leaveBattleRoom 내부에서 발생하는 예외 처리 (이미 나갔거나, 방/참가자 못 찾는 경우 등)
              log.error("Error processing disconnection leave for user {} in room {}: {}", userId, roomId, e.getMessage());
              // 예외 상황에 맞는 응답 반환 또는 로깅만 수행
              return BattleLeaveResponse.builder()
                      .roomId(roomId)
                      .userId(userId)
                      .remainingParticipants(participantRepository.countByBattleRoomId(roomId)) // 현재 DB 기준 참가자 수
                      .message("Error during disconnection processing: " + e.getMessage())
                      .build();
         } catch (Exception e) {
              log.error("Unexpected error processing disconnection for session {}: {}", sessionId, e.getMessage(), e);
              return null; // 예측 불가능한 오류
          }
     }

    /**
     * 사용자가 대결방에서 나갈 때 호출됩니다 (WebSocket 메시지 기반).
     * Redis에서 참가자 정보를 제거하고 leaveBattleRoom을 호출하여 후속 처리를 합니다.
     *
     * @param request   퇴장 요청 데이터 (roomId, userId)
     * @param sessionId 퇴장을 요청한 사용자의 WebSocket 세션 ID
     * @return 퇴장 처리 결과를 담은 {@link BattleLeaveResponse}
     * @throws BusinessException 방/사용자를 찾을 수 없거나 Redis 정보 불일치 시
     */
    public BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId) {
         Long roomId = request.getRoomId();
         Long userId = request.getUserId();
         log.info("Processing leave request for user {} from room {} (session {})", userId, roomId, sessionId);

         // Redis 정보 확인 및 제거
         BattleParticipant participantFromRedis = getParticipantFromRedis(sessionId);
         if (participantFromRedis == null || !participantFromRedis.getUser().getId().equals(userId)) {
             log.warn("Mismatch or missing Redis data for leave request. Session: {}, Request User: {}", sessionId, userId);
             // Redis 정보가 없거나 불일치해도 DB 기반으로 처리를 시도할 수 있음
             // throw new BusinessException(ErrorCode.INVALID_SESSION_DATA, "세션 정보가 유효하지 않습니다.");
         }
         // Redis 데이터 제거는 leaveBattleRoom 내부의 handleDisconnection 호출 시 처리되므로 여기서 중복 제거 불필요

         try {
             // leaveBattleRoom 호출 (DB 업데이트 및 다른 참가자 알림)
             BattleRoomResponse roomResponse = leaveBattleRoom(roomId, userId);
             int remainingParticipants = (roomResponse != null) ?
                     participantRepository.countByBattleRoomId(roomId) : 0;

             return BattleLeaveResponse.builder()
                     .roomId(roomId)
                     .userId(userId)
                     .remainingParticipants(remainingParticipants)
                     .message("User successfully left the battle room.")
                     .build();
         } catch (BusinessException e) {
             log.error("Error processing leave request for user {} in room {}: {}", userId, roomId, e.getMessage());
             throw e; // Controller에서 처리하도록 예외 다시 던지기
         } catch (Exception e) {
             log.error("Unexpected error processing leave request for session {}: {}", sessionId, e.getMessage(), e);
             throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "퇴장 처리 중 오류가 발생했습니다."); // TODO: Use shared_kernel exception
         }
    }

    /**
     * 특정 대결방이 유효한지 (존재하는지) 확인합니다.
     *
     * @param roomId 확인할 대결방 ID
     * @return 방이 존재하면 true, 아니면 false
     */
    public boolean isValidBattleRoom(Long roomId) {
        return battleRoomRepository.existsById(roomId);
    }


    /**
     * 사용자의 준비 상태를 토글합니다 (WebSocket 메시지 기반).
     * Redis에서 참가자 정보를 찾아 toggleReady 메소드를 호출합니다.
     *
     * @param request   준비 상태 토글 요청 데이터 (roomId, userId)
     * @param sessionId 요청한 사용자의 WebSocket 세션 ID
     * @return 토글 결과 및 현재 참가자들의 준비 상태를 담은 {@link BattleReadyResponse}
     * @throws BusinessException 방/사용자를 찾을 수 없거나 Redis 정보 불일치 시
     */
    @Transactional
    public synchronized BattleReadyResponse toggleReadyState(BattleReadyRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        log.info("Processing ready toggle request for user {} in room {} (session {})", userId, roomId, sessionId);

        // Redis 정보 확인
        BattleParticipant participantFromRedis = getParticipantFromRedis(sessionId);
        if (participantFromRedis == null || !participantFromRedis.getUser().getId().equals(userId)) {
            log.warn("Mismatch or missing Redis data for ready toggle request. Session: {}, Request User: {}", sessionId, userId);
             // DB 기반으로 처리를 시도하거나 에러 반환
             // 여기서는 DB 기반 처리를 위해 toggleReady(roomId, userId) 호출
             // throw new BusinessException(ErrorCode.INVALID_SESSION_DATA, "세션 정보가 유효하지 않습니다.");
         }

        try {
             // toggleReady 호출 (DB 업데이트 및 대결 시작 로직 포함)
             BattleRoomResponse roomResponse = toggleReady(roomId, userId);

             // toggleReady 내부에서 대결이 시작되지 않은 경우에만 ready 메시지 전송
             // (시작된 경우는 startBattle에서 메시지 전송됨)
             // toggleReady 내부에서 이미 메시지를 보내므로 여기서 중복 전송 X

             // 현재 참가자 목록 및 준비 상태 조회하여 응답 생성
             BattleRoom updatedRoom = battleRoomRepository.findByIdWithDetails(roomId)
                     .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "상태 토글 후 방 정보를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception
             List<BattleParticipant> participants = participantRepository.findByBattleRoomId(roomId);

             return createBattleReadyResponse(updatedRoom, participants);

         } catch (BusinessException e) {
             log.error("Error processing ready toggle request for user {} in room {}: {}", userId, roomId, e.getMessage());
             throw e; // Controller에서 처리하도록 예외 다시 던지기
         } catch (Exception e) {
             log.error("Unexpected error processing ready toggle for session {}: {}", sessionId, e.getMessage(), e);
             throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "준비 상태 변경 중 오류가 발생했습니다."); // TODO: Use shared_kernel exception
         }
    }


    /**
     * 준비 상태 토글 유효성 검사 (내부 헬퍼)
     * 대결방 상태 등을 확인합니다.
     * @param participant 상태를 변경하려는 참가자
     * @throws BusinessException 상태 변경 불가 시
     */
    private void validateReadyToggle(BattleParticipant participant) {
         BattleRoom room = participant.getBattleRoom();
         // 대기 중인 방에서만 가능
         if (room.getStatus() != BattleRoomStatus.WAITING) { // TODO: Use enum from battle module
             throw new BusinessException(ErrorCode.CANNOT_TOGGLE_READY_NOT_WAITING, "대기 중인 대결방에서만 준비 상태를 변경할 수 있습니다."); // TODO: Use shared_kernel exception
         }
         // 이미 패배한 경우 등 추가 조건 가능성
         if (participant.isDefeated()) {
              throw new BusinessException(ErrorCode.CANNOT_TOGGLE_READY_DEFEATED, "이미 패배 처리된 참가자는 준비 상태를 변경할 수 없습니다."); // TODO: Use shared_kernel exception
          }
    }


    /**
     * 특정 대결방의 현재 참가자 목록 정보를 조회합니다.
     * 주로 REST API 엔드포인트에서 사용됩니다.
     *
     * @param roomId 조회할 대결방 ID
     * @return 참가자 목록 정보를 담은 {@link BattleJoinResponse}
     * @throws BusinessException 방을 찾을 수 없을 때
     */
    public BattleJoinResponse getCurrentBattleParticipants(Long roomId) {
         BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

         // BattleJoinResponse 생성 로직 재사용
         return createBattleJoinResponse(battleRoom, null); // newParticipant는 null로 전달
    }


     /**
      * WebSocket 세션 ID와 참가자(roomId, userId) 정보를 연결하여 Redis에 저장합니다.
      * WebSocket 연결 설정 단계에서 호출될 수 있습니다.
      *
      * @param roomId    참가자가 속한 방 ID
      * @param userId    참가자 ID
      * @param sessionId 해당 참가자의 WebSocket 세션 ID
      * @throws BusinessException 방 또는 사용자를 찾을 수 없을 때
      */
     @Transactional
     public void linkSessionToParticipant(Long roomId, Long userId, String sessionId) {
         BattleRoom battleRoom = battleRoomRepository.findById(roomId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "세션 연결 실패: 대결방을 찾을 수 없습니다."));
         User user = userRepository.findById(userId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "세션 연결 실패: 사용자를 찾을 수 없습니다."));
         BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
                 .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "세션 연결 실패: 참가자를 찾을 수 없습니다."));

         saveParticipantToRedis(participant, sessionId);
         log.info("Linked session {} to participant {} in room {}", sessionId, userId, roomId);
     }

    /**
     * 준비 상태 변경 응답 DTO (BattleReadyResponse)를 생성합니다.
     *
     * @param room         현재 BattleRoom 엔티티
     * @param participants 현재 참가자 목록
     * @return 생성된 BattleReadyResponse DTO
     */
    private BattleReadyResponse createBattleReadyResponse(BattleRoom room, List<BattleParticipant> participants) {
        // TODO: Use EntityMapperService or MapStruct for mapping
         List<BattleReadyResponse.ParticipantReadyInfo> readyInfos = participants.stream()
                 .map(p -> BattleReadyResponse.ParticipantReadyInfo.builder()
                         .userId(p.getUser().getId())
                         .username(p.getUser().getUsername()) // TODO: Pass necessary info only
                         .isReady(p.isReady())
                         .build())
                 .collect(Collectors.toList());

         boolean allReady = participants.stream().allMatch(BattleParticipant::isReady);
         // TODO: 최소 참가자 수 조건 확인 로직 필요
         boolean canStart = allReady && participants.size() >= 2; // 임시 최소 참가자 수

         return BattleReadyResponse.builder()
                 .roomId(room.getId())
                 .participantsReadyStatus(readyInfos)
                 .allParticipantsReady(allReady)
                 .canStart(canStart) // 시작 가능 여부 정보 추가
                 .build();
    }


     /**
      * 제한 시간 내에 답변하지 않은 참가자를 찾아 패배 처리합니다. (스케줄러 등에서 호출될 수 있음)
      *
      * @param roomId 처리할 대결방 ID
      * @return 타임아웃 처리된 참가자 수
      */
     @Transactional
     public int handleTimeoutParticipants(Long roomId) {
         BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                 .orElse(null); // 방이 없으면 처리 중단

         if (battleRoom == null || battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
             log.debug("Skipping timeout check for room {} (not found or not in progress).", roomId);
             return 0;
         }

         if (battleRoom.getCurrentQuestionIndex() == null || battleRoom.getQuestionStartTime() == null) {
              log.warn("Skipping timeout check for room {} due to invalid question state.", roomId);
              return 0;
          }

         LocalDateTime deadline = battleRoom.getQuestionStartTime().plusSeconds(battleRoom.getTimeLimitSeconds());
         if (LocalDateTime.now().isBefore(deadline)) {
             // 아직 제한 시간이 지나지 않음
             return 0;
         }

         log.info("Checking for timed out participants in room {}", roomId);
         Question currentQuestion = battleRoom.getQuiz().getQuestions().get(battleRoom.getCurrentQuestionIndex());
         Long currentQuestionId = currentQuestion.getId();
         int timeoutCount = 0;

         List<BattleParticipant> participantsToCheck = participantRepository.findByBattleRoomId(roomId); // DB에서 최신 참가자 목록 조회

         for (BattleParticipant participant : participantsToCheck) {
             // 활동 중이고 아직 현재 문제에 답하지 않은 참가자 확인
             if (!participant.isDefeated() &&
                 participant.getAnswers().stream().noneMatch(a -> a.getQuestion().getId().equals(currentQuestionId)))
             {
                 log.warn("Participant {} in room {} timed out for question {}.", participant.getUser().getId(), roomId, currentQuestionId);
                 participant.markAsDefeated(); // 타임아웃으로 패배 처리
                 participantRepository.save(participant); // 상태 저장
                 timeoutCount++;

                 // TODO: 타임아웃 알림 메시지 전송? (개별 또는 전체)
                 // messagingTemplate.convertAndSendToUser(participant.getSessionId(), "/queue/errors", "You timed out!");
             }
         }

         if (timeoutCount > 0) {
             log.info("{} participants timed out in room {}.", timeoutCount, roomId);
             // 타임아웃 발생 후, 모든 참가자가 응답했는지 다시 확인하여 다음 단계 진행
             if (allParticipantsRespondedOrDefeated(roomId)) {
                 prepareNextQuestionOrEnd(roomId);
             }
         }

         return timeoutCount;
     }


      /**
       * 특정 사용자의 연결 끊김(퇴장)을 처리합니다. (컨트롤러에서 호출 가능)
       *
       * @param roomId 연결이 끊어진 사용자가 속한 방 ID
       * @param userId 연결이 끊어진 사용자 ID
       * @return 퇴장 처리 결과를 담은 {@link BattleLeaveResponse}
       */
      @Transactional
      public BattleLeaveResponse handleParticipantDisconnection(Long roomId, Long userId) {
          log.warn("Handling explicit disconnection for user {} in room {}", userId, roomId);

          // 세션 ID를 알 수 없으므로 Redis에서 직접 제거는 어려움 (userId -> sessionId 매핑이 있다면 가능)
          String sessionId = getSessionIdFromRedis(userId);
          if (sessionId != null) {
              removeParticipantFromRedis(sessionId, userId); // 세션 정보 제거 시도
          } else {
               log.warn("Could not find session ID in Redis for disconnected user {}", userId);
           }


          try {
              // leaveBattleRoom 메소드 호출하여 DB 처리 및 알림 전송
              BattleRoomResponse roomResponse = leaveBattleRoom(roomId, userId);
              int remainingParticipants = (roomResponse != null) ?
                      participantRepository.countByBattleRoomId(roomId) : 0;

              return BattleLeaveResponse.builder()
                      .roomId(roomId)
                      .userId(userId)
                      .remainingParticipants(remainingParticipants)
                      .message("User disconnection processed.")
                      .build();
          } catch (BusinessException e) {
               // leaveBattleRoom 내부에서 발생하는 예외 처리
               log.error("Error processing disconnection leave for user {} in room {}: {}", userId, roomId, e.getMessage());
               // 예외 상황에 맞는 응답 반환
               return BattleLeaveResponse.builder()
                       .roomId(roomId)
                       .userId(userId)
                       .remainingParticipants(participantRepository.countByBattleRoomId(roomId)) // 현재 DB 기준 참가자 수
                       .message("Error during disconnection processing: " + e.getMessage())
                       .build();
          } catch (Exception e) {
               log.error("Unexpected error processing disconnection for user {}: {}", userId, e.getMessage(), e);
               throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "연결 끊김 처리 중 오류 발생"); // TODO: Use shared_kernel exception
           }
      }

      /**
       * 경험치 계산 로직 (임시)
       * TODO: LevelingService 또는 별도 모듈로 이동 고려
       * @param score 점수
       * @param rank 순위
       * @param totalParticipants 총 참가자 수
       * @return 계산된 경험치
       */
      private int calculateExperience(int score, int rank, int totalParticipants) {
          // 기본 경험치
          int baseExp = 50;
          // 점수 기반 추가 경험치 (점수 10점당 1 EXP?)
          int scoreExp = score / 10;
          // 순위 기반 추가 경험치 (1등: 50, 2등: 30, 3등: 10)
          int rankExp = 0;
          if (rank == 1) rankExp = 50;
          else if (rank == 2) rankExp = 30;
          else if (rank == 3) rankExp = 10;
          // 참가자 수 기반 보너스?
          int participantBonus = (totalParticipants - 1) * 5;

          return baseExp + scoreExp + rankExp + participantBonus;
      }


    /**
     * 모든 참가자가 응답(또는 패배)했을 때 다음 문제를 준비하거나 대결을 종료하는 로직 호출 (내부 헬퍼)
     * @param roomId 대상 방 ID
     */
    private void prepareNextQuestionOrEnd(Long roomId) {
        log.debug("All participants responded or defeated in room {}. Preparing next step.", roomId);
        // 비동기 처리 또는 지연 처리가 필요할 수 있음 (예: 결과 보여주는 시간)
        // 여기서는 즉시 다음 문제 준비 또는 종료 로직 호출
        try {
            prepareNextQuestion(roomId); // 다음 문제 준비 시도 (내부에서 종료 로직 호출 가능)
        } catch (Exception e) {
             log.error("Error preparing next question or ending battle for room {}: {}", roomId, e.getMessage(), e);
             // 예외 발생 시 처리 (예: 강제 종료)
             // endBattle(roomId); // 강제 종료 시도?
         }
    }

} 