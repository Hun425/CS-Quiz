package com.quizplatform.modules.battle.application;

// Domain imports (TODO: Move relevant entities to battle module)
import com.quizplatform.core.domain.battle.BattleAnswer;
import com.quizplatform.modules.battle.domain.entity.BattleParticipant; // Updated import
import com.quizplatform.modules.battle.domain.entity.BattleRoom; // Updated import
import com.quizplatform.core.domain.battle.BattleRoomStatus; // TODO: Move enum to battle module
import com.quizplatform.modules.quiz.domain.entity.Question; // Keep dependency on quiz module
import com.quizplatform.modules.quiz.domain.entity.Quiz; // Keep dependency on quiz module
import com.quizplatform.modules.user.domain.entity.User; // Keep dependency on user module
import com.quizplatform.modules.user.domain.entity.UserBattleStats; // Keep dependency on user module

// DTO imports (TODO: Move relevant DTOs to battle module)
import com.quizplatform.core.dto.battle.*;
import com.quizplatform.core.dto.progess.BattleProgress; // TODO: Check if this belongs here or shared_kernel
import com.quizplatform.modules.battle.presentation.dto.BattleRoomResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleStartResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleNextQuestionResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleProgressResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleEndResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleAnswerResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleJoinResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleLeaveResponse; // Updated import
import com.quizplatform.modules.battle.presentation.dto.BattleReadyResponse; // Updated import

// Exception imports (TODO: Move to shared_kernel)
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;

// Ports (Output)
import com.quizplatform.modules.battle.application.port.out.BattleRoomPersistencePort;
import com.quizplatform.modules.battle.application.port.out.BattleParticipantPersistencePort;
import com.quizplatform.modules.battle.application.port.out.BattleEventPublisherPort;
import com.quizplatform.modules.battle.application.port.out.QuizInfoPort;
import com.quizplatform.modules.battle.application.port.out.UserInfoPort;

// Repository imports - REMOVED (replaced by Ports)
// import com.quizplatform.modules.battle.infrastructure.repository.BattleParticipantRepository; // Updated import
// import com.quizplatform.modules.battle.infrastructure.repository.BattleRoomRepository; // Updated import

// Service imports - REMOVED
// import com.quizplatform.core.service.common.EntityMapperService; // TODO: Decide if this should be in shared_kernel or battle
// import com.quizplatform.core.service.level.LevelingService; // TODO: Decide if this should be in user or shared_kernel

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate; // Keep Redis dependency for now
import org.springframework.messaging.simp.SimpMessagingTemplate; // Keep WebSocket dependency for now
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
    // Output Ports
    private final BattleRoomPersistencePort battleRoomPersistencePort;
    private final BattleParticipantPersistencePort participantPersistencePort;
    private final BattleEventPublisherPort battleEventPublisherPort; // For Kafka events
    private final QuizInfoPort quizInfoPort; // To get Quiz info
    private final UserInfoPort userInfoPort; // To get User info

    // Infrastructure Dependencies (Keep for now, or abstract further if needed)
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // REMOVED Fields:
    // private final BattleRoomRepository battleRoomRepository;
    // private final BattleParticipantRepository participantRepository;
    // private final UserRepository userRepository;
    // private final QuizRepository quizRepository;
    // private final UserBattleStatsRepository userBattleStatsRepository;
    // private final LevelingService levelingService;
    // private final EntityMapperService entityMapperService;

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * BattleService의 생성자입니다. 필요한 Repository와 Service를 주입받습니다.
     * SimpMessagingTemplate은 순환 참조 문제를 피하기 위해 @Lazy 로딩을 사용합니다.
     *
     * @param battleRoomPersistencePort        배틀룸 Repository
     * @param participantPersistencePort      참가자 Repository
     * @param battleEventPublisherPort        대결 이벤트 발행을 위한 Port
     * @param quizInfoPort                    퀴즈 정보 포트
     * @param userInfoPort                    사용자 정보 포트
     * @param redisTemplate              Redis 작업을 위한 Template
     * @param messagingTemplate          WebSocket 메시징을 위한 Template (@Lazy 로딩, Infrastructure)
     */
    @Autowired
    public BattleService(BattleRoomPersistencePort battleRoomPersistencePort, // Changed from Repository
                         BattleParticipantPersistencePort participantPersistencePort, // Changed from Repository
                         BattleEventPublisherPort battleEventPublisherPort, // Added Port
                         QuizInfoPort quizInfoPort, // Added Port
                         UserInfoPort userInfoPort, // Added Port
                         RedisTemplate<String, String> redisTemplate,
                         @Lazy SimpMessagingTemplate messagingTemplate) {
        this.battleRoomPersistencePort = battleRoomPersistencePort; // Changed assignment
        this.participantPersistencePort = participantPersistencePort; // Changed assignment
        this.battleEventPublisherPort = battleEventPublisherPort; // Added assignment
        this.quizInfoPort = quizInfoPort; // Added assignment
        this.userInfoPort = userInfoPort; // Added assignment
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        // Removed assignments for old Repositories and Services
    }

    /**
     * 새로운 대결방을 생성합니다.
     * 방 생성자를 첫 번째 참가자로 자동 추가합니다.
     *
     * @param creator         대결방을 생성하는 사용자 (User Module Entity) // TODO: [MODULAR] userId (Long)로 변경 필요
     * @param quizId          대결에서 사용할 퀴즈의 ID (Quiz Module)
     * @param maxParticipants 최대 참가자 수 (null일 경우 기본값 4)
     * @return 생성된 대결방 정보를 담은 {@link BattleRoomResponse} (Battle Module DTO)
     * @throws BusinessException 퀴즈 정보를 로컬 읽기 모델에서 찾을 수 없을 때 (TODO: [MODULAR] ErrorCode 추가 필요)
     */
    public BattleRoomResponse createBattleRoom(User creator, Long quizId, Integer maxParticipants) {
        // TODO: [MODULAR] User creator 의존성 제거하고 creatorId (Long) 사용하도록 변경
        Long creatorId = creator.getId();

        // TODO: [MODULAR] Quiz 모듈의 Kafka 이벤트 기반 로컬 읽기 모델(QuizInfo) 조회 로직 추가
        // Quiz quiz = quizRepository.findByIdWithDetails(quizId)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다.")); // REMOVED Direct Repository Call
        // 임시 유효성 검사 (추후 읽기 모델 조회로 대체)
        if (quizId == null || quizId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 퀴즈 ID입니다.");
        }
        log.info("Creating battle room with quizId: {}, creatorId: {}", quizId, creatorId);
        // BattleRoom 엔티티는 quizId 필드를 가져야 함 (엔티티 수정 필요)
        BattleRoom battleRoom = BattleRoom.builder()
                // .quiz(quiz) // REMOVED Quiz Object Dependency
                .quizId(quizId) // TODO: [MODULAR] BattleRoom 엔티티에 quizId 필드 추가 필요
                .maxParticipants(maxParticipants != null ? maxParticipants : 4)
                .creatorId(creatorId) // 방 생성자 ID 설정
                .build();

        // TODO: [REFACTOR] battleRoom.validateBattleSettings()가 Quiz 객체에 의존했다면 수정 필요
        battleRoom.validateBattleSettings();

        // 대결방 저장 (Persistence Port 사용)
        BattleRoom savedRoom = battleRoomPersistencePort.save(battleRoom); // Use Port

        // TODO: [MODULAR] addParticipant 메서드 시그니처 및 내부 로직 수정 필요 (User 객체 대신 userId 사용)
        // 방장을 첫 참가자로 추가 (Internal method)
        addParticipant(savedRoom, creatorId); // Pass creatorId instead of creator object

        // TODO: [REFACTOR] EntityMapperService 의존성 제거하고 내부 매핑 로직 사용
        // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
        return entityMapperService.mapToBattleRoomResponse(savedRoom);
    }

    /**
     * 특정 ID의 대결방 상세 정보를 조회합니다.
     * 참가자 및 퀴즈 정보를 포함하여 조회합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 조회된 대결방 정보를 담은 {@link BattleRoomResponse} (Battle Module DTO)
     * @throws BusinessException 대결방을 찾을 수 없을 때 (ErrorCode.BATTLE_ROOM_NOT_FOUND - Shared Kernel Exception)
     */
    public BattleRoomResponse getBattleRoom(Long roomId) {
        // Persistence Port 사용
        BattleRoom battleRoom = battleRoomPersistencePort.findByIdWithDetails(roomId) // Use Port
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // TODO: [REFACTOR] Replace EntityMapperService call with internal mapping logic
        // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 특정 상태(대기중, 진행중 등)의 대결방 목록을 조회합니다.
     * N+1 문제를 방지하기 위해 상세 정보를 별도로 조회합니다.
     *
     * @param status 조회할 대결방의 상태 ({@link BattleRoomStatus} - TODO: Move to Battle Module)
     * @return 해당 상태의 대결방 목록 ({@link BattleRoomResponse} 리스트 - Battle Module DTO)
     */
    public List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status) {
        // Persistence Port 사용
        List<BattleRoom> rooms = battleRoomPersistencePort.findByStatus(status); // Use Port

        // N+1 문제를 방지하기 위해 ID 목록으로 한 번에 상세 조회
        List<Long> roomIds = rooms.stream().map(BattleRoom::getId).collect(Collectors.toList());
        List<BattleRoom> detailedRooms = new ArrayList<>();

        for (Long id : roomIds) {
            // Persistence Port 사용
            battleRoomPersistencePort.findByIdWithDetails(id).ifPresent(detailedRooms::add); // Use Port
        }

        // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
        return entityMapperService.mapToBattleRoomResponseList(detailedRooms);
    }

    /**
     * 특정 사용자가 현재 참여하고 있는 활성 대결방(진행중 또는 대기중)을 조회합니다.
     *
     * @param user 조회할 사용자 (User Module Entity)
     * @return 사용자가 참여중인 활성 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO), 없으면 null
     */
    public BattleRoomResponse getActiveBattleRoomByUser(User user) {
        try {
            // IN_PROGRESS 상태인 방 찾기 시도 (Persistence Port 사용)
            // TODO: [MODULAR] userId 사용하도록 수정 필요 (Controller에서 userId 전달받기)
            Long userId = user.getId(); // Temporarily get userId from User object
            Optional<BattleRoom> roomOpt = battleRoomPersistencePort.findActiveRoomByUserId(userId, BattleRoomStatus.IN_PROGRESS); // Use Port and userId

            // IN_PROGRESS 상태인 방이 없으면 WAITING 상태인 방도 찾아봄
            if (roomOpt.isEmpty()) {
                // Persistence Port 사용
                roomOpt = battleRoomPersistencePort.findActiveRoomByUserId(userId, BattleRoomStatus.WAITING); // Use Port and userId
            }

            // 활성 대결방이 없으면 null 반환
            if (roomOpt.isEmpty()) {
                return null;
            }

            // 상세 정보 로드 (Persistence Port 사용)
            BattleRoom room = battleRoomPersistencePort.findByIdWithDetails(roomOpt.get().getId()) // Use Port
                    .orElse(null); // Return null if not found after initial check

            if (room == null) {
                return null;
            }

            // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
            return entityMapperService.mapToBattleRoomResponse(room);
        } catch (Exception e) {
            // Log the exception
            log.error("Error finding active battle room for user {}: {}", user.getId(), e.getMessage(), e);
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
     * @param user   참가하려는 사용자 (User Module Entity)
     * @return 참가 후 업데이트된 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO)
     * @throws BusinessException 대결방을 찾을 수 없거나 (BATTLE_ROOM_NOT_FOUND),
     * 이미 시작되었거나 (BATTLE_ALREADY_STARTED),
     * 정원이 찼거나 (BATTLE_ROOM_FULL),
     * 이미 참가 중일 때 (ALREADY_PARTICIPATING) (Shared Kernel Exceptions)
     */
    // TODO: [MODULAR] User 객체 대신 userId 사용하도록 시그니처 변경
    // public BattleRoomResponse joinBattleRoom(Long roomId, User user) {
    public BattleRoomResponse joinBattleRoom(Long roomId, Long userId) {
        // Persistence Port 사용
        BattleRoom battleRoom = battleRoomPersistencePort.findByIdWithDetails(roomId) // Use Port
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // 이미 시작된 대결인지 확인
        if (battleRoom.getStatus() != BattleRoomStatus.WAITING) { // TODO: Use Battle Module Enum
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작된 대결방입니다."); // TODO: Use shared_kernel exception
        }

        // 정원 초과 확인 (Battle Module Entity Logic)
        if (battleRoom.isParticipantLimitReached()) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_FULL, "대결방이 가득 찼습니다."); // TODO: Use shared_kernel exception
        }

        // 이미 참가 중인지 확인 (Persistence Port 사용)
        // TODO: [MODULAR] participantRepository.existsByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
        // Persistence Port 사용
        if (participantPersistencePort.existsByBattleRoomIdAndUserId(roomId, userId)) { // Use Port
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING, "이미 참가 중인 사용자입니다."); // TODO: Use shared_kernel exception
        }

        // 참가자 추가 및 반환값 저장 (Internal method)
        // TODO: [MODULAR] addParticipant 호출 시 userId 사용
        BattleParticipant participant = addParticipant(battleRoom, userId);

        // WebSocket 메시지 발송 (Infrastructure - SimpMessagingTemplate)
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/participants",
                createBattleJoinResponse(battleRoom, participant) // Internal DTO creation helper
        );

        // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * 참가자의 준비 상태를 토글합니다 (준비/준비 해제).
     * 모든 참가자가 준비 완료되면 자동으로 대결을 시작합니다.
     *
     * @param roomId 준비 상태를 변경할 대결방의 ID
     * @param user   준비 상태를 변경할 사용자 (User Module Entity)
     * @return 상태 변경 후의 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO)
     * @throws BusinessException 대결방 또는 참가자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, PARTICIPANT_NOT_FOUND) (Shared Kernel Exceptions)
     */
    // TODO: [MODULAR] User 객체 대신 userId 사용하도록 시그니처 변경
    // public BattleRoomResponse toggleReady(Long roomId, User user) {
    public BattleRoomResponse toggleReady(Long roomId, Long userId) {
        // Persistence Port 사용
        BattleRoom battleRoom = battleRoomPersistencePort.findByIdWithDetails(roomId) // Use Port
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
        // Persistence Port 사용
        BattleParticipant participant = participantPersistencePort.findByBattleRoomIdAndUserId(roomId, userId) // Use Port
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // 상태 토글 유효성 검사 (Internal helper method)
        validateReadyToggle(participant);

        // 준비 상태 토글
        participant.toggleReady();
        // Persistence Port 사용
        participantPersistencePort.save(participant); // Use Port
        log.info("Participant ready state toggled: roomId={}, userId={}, isReady={}", roomId, userId, participant.isReady());

        // 모든 참가자가 준비되었는지 확인하고 게임 시작
        if (isReadyToStart(roomId)) {
            startBattle(roomId);
            // 게임 시작 후 업데이트된 방 상태를 다시 로드
            BattleRoom updatedRoom = battleRoomPersistencePort.findByIdWithDetails(roomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));
            return entityMapperService.mapToBattleRoomResponse(updatedRoom);
        } else {
            // 상태 변경 알림 (WebSocket)
            List<BattleParticipant> participants = participantPersistencePort.findByBattleRoom(battleRoom);
            messagingTemplate.convertAndSend(
                    "/topic/battle/" + roomId + "/ready",
                    createBattleReadyResponse(battleRoom, participants) // Internal DTO creation helper
            );
            return entityMapperService.mapToBattleRoomResponse(battleRoom);
        }
    }

    /**
     * 사용자가 현재 참가 중인 대결방에서 나갑니다.
     * 대기 중 상태에서 나가면 단순히 참가자 목록에서 제거됩니다.
     * 진행 중 상태에서 나가면 해당 참가자는 비활성화(active=false) 처리되고, 패배로 간주될 수 있습니다.
     * 마지막 참가자가 나가면 방 상태를 FINISHED로 변경합니다.
     *
     * @param roomId 나갈 대결방의 ID
     * @param user   나가려는 사용자 (User Module Entity)
     * @return 퇴장 후 업데이트된 대결방 정보 ({@link BattleRoomResponse} - Battle Module DTO)
     * @throws BusinessException 대결방 또는 참가자를 찾을 수 없을 때 (BATTLE_ROOM_NOT_FOUND, PARTICIPANT_NOT_FOUND) (Shared Kernel Exceptions)
     */
    // TODO: [MODULAR] User 객체 대신 userId 사용하도록 시그니처 변경
    // public BattleRoomResponse leaveBattleRoom(Long roomId, User user) {
    public BattleRoomResponse leaveBattleRoom(Long roomId, Long userId) {
        BattleRoom battleRoom = battleRoomPersistencePort.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

        // 진행 중인 배틀에서 나가는 경우 참가자 비활성화 처리
        if (battleRoom.getStatus() == BattleRoomStatus.IN_PROGRESS) { // TODO: Use Battle Module Enum
            // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
            // BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
            BattleParticipant participant = participantPersistencePort.findByBattleRoomIdAndUserId(roomId, userId) // Repository 메서드 변경 가정
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception

            participant.setActive(false); // 비활성화
            participantPersistencePort.save(participant);
            log.info("Participant left during battle (inactive): roomId={}, userId={}", roomId, userId);

            // 남은 활성 참가자 수 확인
            long activeParticipantsCount = battleRoom.getParticipants().stream()
                    .filter(BattleParticipant::isActive)
                    .count();

            // 활성 참가자가 없으면 배틀 종료 처리
            if (activeParticipantsCount == 0) {
                endBattle(roomId);
                log.info("Last active participant left, ending battle: roomId={}", roomId);
                // 종료 후 업데이트된 방 상태 로드
                BattleRoom updatedRoom = battleRoomPersistencePort.findByIdWithDetails(roomId).orElse(battleRoom); // Fallback to previous state if somehow not found
                return entityMapperService.mapToBattleRoomResponse(updatedRoom);
            }
        } else if (battleRoom.getStatus() == BattleRoomStatus.WAITING) { // TODO: Use Battle Module Enum
            // 대기 중 상태에서 나가면 참가자 정보 삭제
            // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
            // BattleParticipant participant = participantRepository.findByBattleRoomAndUser(battleRoom, user)
            BattleParticipant participant = participantPersistencePort.findByBattleRoomIdAndUserId(roomId, userId) // Repository 메서드 변경 가정
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "참가자를 찾을 수 없습니다.")); // TODO: Use shared_kernel exception
            battleRoom.getParticipants().remove(participant); // 컬렉션에서 제거
            participantPersistencePort.delete(participant); // DB에서 삭제
            log.info("Participant left waiting room: roomId={}, userId={}", roomId, userId);

            // 방장이 나갔고 남은 참가자가 있으면 새 방장 위임 또는 방 삭제 등 정책 필요 (현재는 방장 개념 없음)

            // 마지막 참가자가 나가면 방 삭제
            if (battleRoom.getParticipants().isEmpty()) {
                battleRoomPersistencePort.delete(battleRoom);
                log.info("Last participant left waiting room, deleting room: roomId={}", roomId);
                // 방이 삭제되었으므로 null 또는 특정 상태 DTO 반환 필요 (컨트롤러에서 처리)
                // 여기서는 일단 삭제된 방의 정보를 매핑 시도 (예외 발생 가능성 있음)
                return entityMapperService.mapToBattleRoomResponse(battleRoom); // Or return null / specific DTO
            }
        }

        // 참가자 목록 업데이트 알림 (WebSocket)
        List<BattleParticipant> remainingParticipants = participantPersistencePort.findByBattleRoom(battleRoom);
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/participants",
                 createBattleJoinResponse(battleRoom, null) // Pass null for participant when someone leaves
        );

        // DTO 변환 (External Service - TODO: consider moving mapping logic inside Battle module)
        return entityMapperService.mapToBattleRoomResponse(battleRoom);
    }

    /**
     * WebSocket 연결 시 클라이언트 세션과 대결방 정보를 연결합니다.
     * (현재는 사용되지 않음, Redis 기반 로직으로 대체됨)
     *
     * @param request 참가 요청 정보 (roomId, userId 포함)
     * @param sessionId WebSocket 세션 ID
     * @return 참가 성공 시 {@link BattleJoinResponse}
     * @throws BusinessException 유효하지 않은 요청 또는 참가 불가 시
     */
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
         BattleRoom room = battleRoomPersistencePort.findByIdWithDetails(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));
        // TODO: [MODULAR] UserRepository 직접 호출 제거
        // User user = userRepository.findById(request.getUserId())
        //         .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long userId = request.getUserId(); // Get userId from request

        // 참가 로직 (addParticipant 내부에서 Redis 저장)
        // TODO: [MODULAR] addParticipant 호출 시 userId 사용
        BattleParticipant participant = addParticipant(room, userId);
        // saveParticipantToRedis(participant, sessionId); // addParticipant에서 처리

        // WebSocket 메시지 발송 (기존 joinBattleRoom과 유사)
        messagingTemplate.convertAndSend(
                "/topic/battle/" + request.getRoomId() + "/participants",
                createBattleJoinResponse(room, participant)
        );

        return createBattleJoinResponse(room, participant);
    }


    /**
     * 참가자가 제출한 문제 답변을 처리합니다.
     * 답변의 정답 여부를 확인하고, 점수를 계산하며, 다음 문제로 넘어갈지 결정합니다.
     * Redis를 사용하여 참가자 상태를 관리합니다.
     *
     * @param request 답변 요청 정보 (roomId, userId, answer 등)
     * @param sessionId WebSocket 세션 ID
     * @return 답변 처리 결과 (정답 여부, 점수 등) {@link BattleAnswerResponse}
     * @throws BusinessException 참가자 또는 대결방을 찾을 수 없거나, 잘못된 요청일 경우
     */
    @Transactional
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        log.debug("Processing answer: sessionId={}, request={}", sessionId, request);

        // Redis에서 참가자 정보 조회
        BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null) {
            log.warn("Participant not found in Redis for session: {}", sessionId);
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "세션에 해당하는 참가자를 찾을 수 없습니다.");
        }

        // DB에서 최신 대결방 정보 조회 (퀴즈 질문 포함)
        BattleRoom battleRoom = battleRoomPersistencePort.findByIdWithQuizQuestions(participant.getBattleRoom().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방 정보를 찾을 수 없습니다."));

        log.debug("Found participant {} in room {}", participant.getUser().getId(), battleRoom.getId());

        // 요청의 room ID와 참가자의 실제 room ID 일치 확인
        if (!battleRoom.getId().equals(request.getRoomId())) {
            log.warn("Room ID mismatch: request={}, participantRoom={}", request.getRoomId(), battleRoom.getId());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "요청된 방 ID와 참가자의 방 ID가 일치하지 않습니다.");
        }

        // 대결이 진행 중인지 확인
        if (battleRoom.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            log.warn("Battle not in progress: roomId={}, status={}", battleRoom.getId(), battleRoom.getStatus());
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_PROGRESS, "대결이 진행 중이 아닙니다.");
        }

        // 요청된 질문 인덱스 유효성 검사
        List<Question> questions = battleRoom.getQuestions();
        int questionIndex = request.getQuestionIndex();
        if (questionIndex < 0 || questionIndex >= questions.size()) {
            log.warn("Invalid question index: requestIndex={}, totalQuestions={}", questionIndex, questions.size());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 질문 번호입니다.");
        }

        // 현재 대결방의 진행 상태와 요청된 질문 인덱스가 일치하는지 확인
        // (클라이언트가 이전 문제 답을 늦게 보내는 경우 등 방지)
        if (questionIndex != battleRoom.getCurrentQuestionIndex()) {
             log.warn("Question index mismatch: requestIndex={}, currentRoomIndex={}, participant={}, room={}",
                 questionIndex, battleRoom.getCurrentQuestionIndex(), participant.getUser().getId(), battleRoom.getId());
             // 이미 다음 문제로 넘어갔으므로, 이 답변은 무시하고 현재 상태에 대한 응답을 보낼 수 있음
             // 또는 에러를 반환하여 클라이언트가 재시도하도록 유도 (현재는 에러 반환)
             throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 순서의 답변입니다.");
        }

        // 현재 문제 가져오기
        Question currentQuestion = battleRoom.getCurrentQuestion();
        if (currentQuestion == null || !currentQuestion.getId().equals(request.getQuestionId())) {
            log.warn("Question ID mismatch: requestQid={}, currentQid={}", request.getQuestionId(), currentQuestion != null ? currentQuestion.getId() : "null");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 질문 ID 입니다.");
        }

        // 답변 처리 및 점수 계산 (BattleParticipant Entity에게 위임)
        BattleAnswer answer = participant.submitAnswer(
                currentQuestion,
                request.getAnswer(),
                request.getTimeTaken(),
                battleRoom.getTimeLimitPerQuestion() // 문제당 시간 제한 전달
        );

        // Redis에 업데이트된 참가자 상태 저장
        saveParticipantToRedis(participant, sessionId);
        log.debug("Participant score updated: userId={}, score={}, answerCorrect={}", participant.getUser().getId(), participant.getCurrentScore(), answer.isCorrect());

        // 답변 처리 결과 DTO 생성 (Internal helper)
        BattleAnswerResponse response = createBattleAnswerResponse(answer);

        // 모든 참가자가 현재 문제에 답했는지 확인 (동기화 필요)
        if (allParticipantsAnswered(battleRoom.getId())) {
            log.info("All participants answered question {}. Preparing next question or ending battle for room {}.", questionIndex, battleRoom.getId());
            // 모든 참가자가 답변 완료 시, 즉시 다음 문제 준비 또는 결과 집계 알림
            // (별도 스케줄러 대신 답변 처리 시 확인)
            prepareNextQuestionOrEnd(battleRoom.getId());
        }

        return response;
    }

    /**
     * 대결방의 모든 참가자가 준비 완료 상태인지 확인합니다.
     * 동기화 처리를 통해 동시성 문제를 방지합니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 모든 참가자가 준비 완료 상태이면 true, 아니면 false
     * @throws BusinessException 대결방을 찾을 수 없을 때
     */
    @Transactional // Ensure data consistency
    public synchronized boolean isReadyToStart(Long roomId) {
        BattleRoom room = battleRoomPersistencePort.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND, "대결방을 찾을 수 없습니다."));

        // 참가자가 2명 이상이고, 모든 활성 참가자가 준비 상태인지 확인
        long activeParticipantCount = room.getParticipants().stream().filter(BattleParticipant::isActive).count();
        boolean allReady = room.getParticipants().stream()
                             .filter(BattleParticipant::isActive)
                             .allMatch(BattleParticipant::isReady);

        log.debug("Checking ready status for room {}: activeParticipants={}, allReady={}", roomId, activeParticipantCount, allReady);
        return activeParticipantCount >= 2 && allReady;
    }

    /**
     * 대결을 시작합니다. 대결방 상태를 IN_PROGRESS로 변경하고,
     * 첫 번째 문제를 참가자들에게 전송합니다.
     * 동기화 처리를 통해 동시성 문제를 방지합니다.
     *
     * @param roomId 시작할 대결방의 ID
     * @return 대결 시작 정보 (첫 문제, 시간 제한 등) {@link BattleStartResponse}
     * @throws BusinessException 대결방을 찾을 수 없거나, 이미 시작되었거나, 시작 조건 미충족 시
     */
    @Transactional
    public synchronized BattleStartResponse startBattle(Long roomId) {
        log.info("Attempting to start battle for room: {}", roomId);
        BattleRoom room = battleRoomPersistencePort.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (room.getStatus() != BattleRoomStatus.WAITING) {
            log.warn("Battle cannot start, room not in WAITING state: roomId={}, status={}", roomId, room.getStatus());
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_STARTED, "이미 시작되었거나 종료된 대결입니다.");
        }

        if (!isReadyToStart(roomId)) {
             log.warn("Battle cannot start, not all participants are ready or not enough participants: roomId={}", roomId);
            throw new BusinessException(ErrorCode.BATTLE_CANNOT_START, "모든 참가자가 준비되지 않았거나 참가자 수가 부족합니다.");
        }

        // 대결 시작 처리 (BattleRoom Entity에게 위임)
        room.startBattle();
        battleRoomPersistencePort.save(room);
        log.info("Battle started successfully: roomId={}", roomId);

        // 대결 시작 정보 생성 (Internal helper)
        BattleStartResponse response = createBattleStartResponse(room);

        // WebSocket으로 대결 시작 알림 및 첫 문제 전송
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/start", response);

        // 첫 번째 문제 타이머 시작 (별도 스케줄러 또는 로직 필요)
        // scheduleNextQuestion(roomId, 0); // 예시

        return response;
    }

    /**
     * 다음 문제를 준비하거나, 모든 문제가 출제되었으면 대결을 종료합니다.
     * 현재는 다음 문제 정보만 반환하고, 실제 상태 변경 및 알림은 호출하는 쪽에서 처리합니다.
     * (allParticipantsAnswered 확인 후 호출됨)
     *
     * @param roomId 다음 문제를 준비할 대결방 ID
     * @return 다음 문제 정보 또는 null (대결 종료 시)
     */
    @Transactional // Ensure consistency when updating room state
    public BattleNextQuestionResponse prepareNextQuestion(Long roomId) {
        BattleRoom room = battleRoomPersistencePort.findByIdWithQuizQuestions(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (room.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            log.warn("Cannot prepare next question, battle not in progress: roomId={}, status={}", roomId, room.getStatus());
            return null; // Or throw exception
        }

        // 다음 문제로 이동 (BattleRoom Entity 로직)
        boolean hasNext = room.moveToNextQuestion();
        battleRoomPersistencePort.save(room);

        if (hasNext) {
            Question nextQuestion = room.getCurrentQuestion();
            boolean isLast = room.getCurrentQuestionIndex() == room.getQuestions().size() - 1;
            log.info("Preparing next question {} for room {}. Is last: {}", room.getCurrentQuestionIndex(), roomId, isLast);
            // 다음 문제 정보 DTO 생성
            return createNextQuestionResponse(nextQuestion, isLast);
        } else {
            // 모든 문제가 출제됨 (종료 로직은 별도 호출)
            log.info("No more questions for room {}. Battle should end.", roomId);
            return null;
        }
    }

    /**
     * 특정 대결방의 모든 활성 참가자가 현재 문제에 대한 답변을 제출했는지 확인합니다.
     * Redis에 저장된 참가자별 마지막 답변 인덱스를 확인합니다.
     * 동기화 처리가 필요할 수 있습니다.
     *
     * @param roomId 확인할 대결방 ID
     * @return 모든 활성 참가자가 답변했으면 true, 아니면 false
     */
    @Transactional // Read consistency
    public synchronized boolean allParticipantsAnswered(Long roomId) {
        BattleRoom room = battleRoomPersistencePort.findByIdWithDetails(roomId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (room.getStatus() != BattleRoomStatus.IN_PROGRESS) {
            return false; // 진행 중이 아니면 확인할 필요 없음
        }

        int currentQuestionIndex = room.getCurrentQuestionIndex();
        if (currentQuestionIndex < 0) {
            return false; // 아직 문제가 시작되지 않음
        }

        // Redis에서 활성 참가자들의 세션 ID 목록을 가져와야 함 (현재 구조에서는 어려움)
        // 또는 DB에서 활성 참가자 목록 조회
        List<BattleParticipant> activeParticipants = room.getParticipants().stream()
                .filter(BattleParticipant::isActive)
                .collect(Collectors.toList());

        if (activeParticipants.isEmpty()) {
            log.warn("No active participants found in room {} during answer check.", roomId);
            // 활성 참가자가 없으면 더 이상 진행할 수 없으므로, true를 반환하여 종료 로직 유도?
            // 또는 false를 반환하여 타임아웃 처리 등 다른 로직에 맡김 (현재는 false)
            return false;
        }

        // 각 활성 참가자의 마지막 답변 인덱스 확인 (Redis 또는 DB)
        for (BattleParticipant p : activeParticipants) {
            // Redis에서 참가자 상태 조회 (getParticipantFromRedis는 sessionId 필요 -> 구조 변경 필요)
            // 임시로 DB에서 참가자별 마지막 답변 인덱스 확인 (BattleAnswer 엔티티 필요)
            // 여기서는 BattleParticipant에 lastAnsweredIndex 필드가 있다고 가정 (실제로는 없음)
            // int lastAnsweredIndex = p.getLastAnsweredIndex(); // 가상의 필드

            // 실제 구현: BattleAnswer 엔티티를 조회하여 해당 참가자의 현재 문제 인덱스 답변이 있는지 확인
            boolean answered = p.getAnswers().stream()
                               .anyMatch(ans -> ans.getQuestion().getOrderIndex() == currentQuestionIndex);

            if (!answered) {
                log.debug("Participant {} hasn't answered question {} yet for room {}.", p.getUser().getId(), currentQuestionIndex, roomId);
                return false; // 한 명이라도 답변 안 했으면 false
            }
        }

        log.debug("All active participants have answered question {} for room {}.", currentQuestionIndex, roomId);
        return true; // 모두 답변했으면 true
    }

    /**
     * 현재 대결 진행 상황 정보를 조회합니다.
     * 각 참가자의 현재 점수, 순위 등의 정보를 포함합니다.
     *
     * @param roomId 조회할 대결방 ID
     * @return 대결 진행 상황 정보 {@link BattleProgressResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때
     */
    public BattleProgressResponse getBattleProgress(Long roomId) {
        BattleRoom room = battleRoomPersistencePort.findByIdWithDetails(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 진행 상황 계산 로직 (별도 클래스 또는 메서드로 분리 가능)
        BattleProgress progress = room.calculateProgress(); // BattleRoom 엔티티 내부에 로직 구현 가정

        // DTO 변환 (Internal helper)
        return createBattleProgressResponse(progress);
    }

    /**
     * 대결을 종료합니다.
     * 최종 결과를 계산하고, 참가자들에게 경험치를 부여하며, 통계를 업데이트합니다.
     * 대결방 상태를 FINISHED로 변경합니다.
     *
     * @param roomId 종료할 대결방 ID
     * @return 대결 결과 정보 {@link BattleEndResponse}
     * @throws BusinessException 대결방을 찾을 수 없거나 이미 종료된 경우
     */
    public BattleEndResponse endBattle(Long roomId) {
        log.info("Ending battle for room: {}", roomId);
        BattleRoom room = battleRoomPersistencePort.findByIdWithQuizQuestions(roomId) // 결과 계산 위해 질문 필요
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (room.getStatus() == BattleRoomStatus.FINISHED) {
            log.warn("Battle already finished: roomId={}", roomId);
            // 이미 종료된 경우 기존 결과 반환 또는 에러 처리
            // 여기서는 간단히 에러 발생
            throw new BusinessException(ErrorCode.BATTLE_ALREADY_FINISHED, "이미 종료된 대결입니다.");
        }

        // 최종 결과 계산 (Internal helper)
        BattleResult result = calculateBattleResult(room);

        // 경험치 부여 (External Service - LevelingService)
        awardExperiencePoints(result);

        // 통계 업데이트 (Internal helper -> UserBattleStatsRepository)
        updateStatistics(result);

        // 대결방 상태 변경 및 저장
        room.endBattle(result.getWinner()); // BattleRoom 엔티티에 종료 로직 위임 (상태 변경, 종료 시간 설정 등)
        battleRoomPersistencePort.save(room);
        log.info("Battle finished successfully: roomId={}, winnerId={}", roomId, result.getWinner() != null ? result.getWinner().getId() : "None");

        // 결과 DTO 생성 (BattleResult 기반)
        BattleEndResponse response = BattleEndResponse.from(result);

        // WebSocket으로 최종 결과 전송
        messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/end", response);

        // Redis에 저장된 관련 정보 삭제 (선택적)
        // clearBattleRedisData(roomId);

        return response;
    }

    // --- Private Helper Methods --- //

    /**
     * 대결방에 새로운 참가자를 추가하고, 관련 통계를 초기화하며, Redis에 세션 정보를 저장합니다.
     *
     * @param battleRoom 참가자를 추가할 대결방
     * @param userId     추가될 사용자의 ID
     * @return 생성된 {@link BattleParticipant} 엔티티
     */
    // TODO: [MODULAR] User 객체 대신 userId 사용하도록 시그니처 변경
    // private BattleParticipant addParticipant(BattleRoom battleRoom, User user) {
    private BattleParticipant addParticipant(BattleRoom battleRoom, Long userId) {
        log.debug("Adding participant {} to room {}", userId, battleRoom.getId());

        // TODO: [MODULAR] UserBattleStatsRepository 직접 접근 제거. User 모듈에서 ParticipantJoinedEvent 처리하도록 변경.
        // 사용자 배틀 통계 조회 또는 생성
        // UserBattleStats stats = userBattleStatsRepository.findByUser(user)
        //         .orElseGet(() -> {
        //             UserBattleStats newStats = new UserBattleStats(user);
        //             return userBattleStatsRepository.save(newStats);
        //         });

        // TODO: [MODULAR] BattleParticipant가 User 객체 대신 userId를 참조하도록 엔티티 수정 필요
        // 참가자 엔티티 생성
        BattleParticipant participant = BattleParticipant.builder()
                .battleRoom(battleRoom)
                // .user(user) // REMOVED User Object Dependency
                .userId(userId) // TODO: [MODULAR] BattleParticipant 엔티티에 userId 필드 추가 필요
                // .userBattleStats(stats) // REMOVED Stats Dependency
                .build();

        // 참가자 저장 (Cascade 설정에 따라 battleRoom 저장 시 함께 저장될 수도 있음)
        participant = participantPersistencePort.save(participant);
        battleRoom.getParticipants().add(participant); // 컬렉션에도 추가
        log.info("Participant added successfully: roomId={}, userId={}", battleRoom.getId(), userId);

        // TODO: [MODULAR] 참가자 추가 이벤트 발행 (Kafka)
        // publishParticipantJoinedEvent(battleRoom.getId(), userId, participant.getId());

        // TODO: [REFACTOR] Redis 저장 로직 검토 및 sessionId 매핑 방식 확인 필요
        // Redis에 참가자 정보 임시 저장 (세션 ID는 WebSocket 연결 시점에 매핑)
        // 현재는 세션 ID 없이 기본적인 정보만 저장 (추후 개선 필요)
        // saveParticipantToRedis(participant, "temp-session-" + userId); // 임시 키

        return participant;
    }

    /**
     * 참가자 정보를 Redis에 저장합니다. WebSocket 세션 ID를 키로 사용합니다.
     *
     * @param participant 저장할 참가자 엔티티
     * @param sessionId   WebSocket 세션 ID
     */
    private void saveParticipantToRedis(BattleParticipant participant, String sessionId) {
        String key = PARTICIPANT_KEY_PREFIX + sessionId;
        // 참가자 ID, 방 ID 등 필요한 최소 정보만 저장하거나, 직렬화하여 저장
        Map<String, String> participantData = new HashMap<>();
        participantData.put("participantId", participant.getId().toString());
        participantData.put("userId", participant.getUser().getId().toString());
        participantData.put("roomId", participant.getBattleRoom().getId().toString());
        participantData.put("currentScore", String.valueOf(participant.getCurrentScore()));
        participantData.put("lastAnsweredIndex", String.valueOf(participant.getLastAnsweredIndex()));

        try {
            redisTemplate.opsForHash().putAll(key, participantData);
            redisTemplate.expire(key, ROOM_EXPIRE_SECONDS, TimeUnit.SECONDS); // 만료 시간 설정
            log.debug("Participant data saved to Redis: key={}, data={}", key, participantData);
        } catch (Exception e) {
            log.error("Failed to save participant data to Redis: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * Redis에서 WebSocket 세션 ID를 이용해 참가자 정보를 조회합니다.
     *
     * @param sessionId WebSocket 세션 ID
     * @return 조회된 {@link BattleParticipant} 엔티티, 찾지 못하면 null 반환
     */
    private BattleParticipant getParticipantFromRedis(String sessionId) {
        String key = PARTICIPANT_KEY_PREFIX + sessionId;
        try {
            Map<Object, Object> participantData = redisTemplate.opsForHash().entries(key);
            if (participantData.isEmpty()) {
                log.warn("No participant data found in Redis for key: {}", key);
                return null;
            }

            Long participantId = Long.parseLong((String) participantData.get("participantId"));
            // DB에서 참가자 정보 다시 로드 (최신 상태 반영 및 연관 엔티티 로딩)
            // Fetch join 등을 사용하여 필요한 정보 로드
            BattleParticipant participant = participantRepository.findById(participantId)
                    .orElse(null);
            log.debug("Participant data retrieved from Redis and DB: key={}, participantId={}", key, participantId);
            return participant;

        } catch (Exception e) {
            log.error("Failed to get participant data from Redis: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 새로운 참가자가 입장했을 때 WebSocket으로 전송할 참가자 목록 정보를 생성합니다.
     *
     * @param room          대결방 정보
     * @param newParticipant 새로 참가한 사용자 (null일 경우 단순 목록 조회)
     * @return 참가자 목록 정보 {@link BattleJoinResponse}
     */
    private BattleJoinResponse createBattleJoinResponse(BattleRoom room, BattleParticipant newParticipant) {
        // DB에서 최신 참가자 목록 조회
        List<BattleParticipant> latestParticipants = participantRepository.findByBattleRoom(room);

        List<BattleJoinResponse.ParticipantInfo> participantInfos = latestParticipants.stream()
                .map(p -> BattleJoinResponse.ParticipantInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername()) // 사용자 이름 추가
                        .isReady(p.isReady())
                        .isActive(p.isActive())
                        // .profileImageUrl(p.getUser().getProfileImageUrl()) // 프로필 이미지 URL 추가 (필요시)
                        .isCreator(room.getCreatorId().equals(p.getUser().getId())) // 방장 여부 추가
                        .build())
                .collect(Collectors.toList());

        return BattleJoinResponse.builder()
                .roomId(room.getId())
                .participants(participantInfos)
                .currentParticipants(latestParticipants.size())
                .maxParticipants(room.getMaxParticipants())
                .newParticipantUserId(newParticipant != null ? newParticipant.getUser().getId() : null)
                .build();
    }

    /**
     * 답변 처리 결과를 담는 DTO를 생성합니다.
     *
     * @param answer 처리된 답변 엔티티 ({@link BattleAnswer})
     * @return 답변 결과 DTO {@link BattleAnswerResponse}
     */
    private BattleAnswerResponse createBattleAnswerResponse(BattleAnswer answer) {
        return BattleAnswerResponse.builder()
                .participantId(answer.getParticipant().getId())
                .questionId(answer.getQuestion().getId())
                .isCorrect(answer.isCorrect())
                .scoreEarned(answer.getScoreEarned())
                .currentTotalScore(answer.getParticipant().getCurrentScore())
                .build();
    }

    /**
     * 대결 시작 시 첫 번째 문제 정보를 담는 DTO를 생성합니다.
     *
     * @param room 시작하는 대결방 엔티티
     * @return 대결 시작 정보 DTO {@link BattleStartResponse}
     */
    private BattleStartResponse createBattleStartResponse(BattleRoom room) {
        Question firstQuestion = room.getCurrentQuestion();
        if (firstQuestion == null) {
            // 이론적으로 startBattle 전에 질문이 로드되므로 null이면 안됨
            log.error("First question is null during battle start for room {}", room.getId());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "첫 번째 질문을 찾을 수 없습니다.");
        }

        BattleStartResponse.QuestionInfo questionInfo = BattleStartResponse.QuestionInfo.builder()
                .questionId(firstQuestion.getId())
                .questionText(firstQuestion.getQuestionText()) // 질문 텍스트
                .options(firstQuestion.getOptions())     // 보기 목록
                .orderIndex(firstQuestion.getOrderIndex()) // 질문 순서 (0부터 시작)
                .questionType(firstQuestion.getQuestionType()) // 질문 유형 (예: MULTIPLE_CHOICE)
                .imageUrl(firstQuestion.getImageUrl()) // 이미지 URL (있을 경우)
                .build();

        return BattleStartResponse.builder()
                .roomId(room.getId())
                .startTime(LocalDateTime.now()) // 실제 시작 시간
                .timeLimitPerQuestion(room.getTimeLimitPerQuestion()) // 문제당 시간 제한
                .totalQuestions(room.getQuestions().size()) // 전체 문제 수
                .firstQuestion(questionInfo) // 첫 번째 문제 정보
                .build();
    }

    /**
     * 다음 문제 정보를 담는 DTO를 생성합니다.
     *
     * @param question 다음 문제 엔티티
     * @param isLast   마지막 문제인지 여부
     * @return 다음 문제 정보 DTO {@link BattleNextQuestionResponse}
     */
    private BattleNextQuestionResponse createNextQuestionResponse(Question question, boolean isLast) {
        BattleNextQuestionResponse.QuestionInfo nextQuestionInfo = BattleNextQuestionResponse.QuestionInfo.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .options(question.getOptions())
                .orderIndex(question.getOrderIndex())
                .questionType(question.getQuestionType())
                .imageUrl(question.getImageUrl())
                .build();

        return BattleNextQuestionResponse.builder()
                .nextQuestion(nextQuestionInfo)
                .isLastQuestion(isLast)
                .build();
    }

    /**
     * 대결 진행 상황 정보를 담는 DTO를 생성합니다.
     *
     * @param progress 계산된 대결 진행 상황 객체 ({@link BattleProgress})
     * @return 진행 상황 DTO {@link BattleProgressResponse}
     */
    private BattleProgressResponse createBattleProgressResponse(BattleProgress progress) {
        List<BattleProgressResponse.ParticipantProgressInfo> progressInfos = progress.getParticipantProgressList().stream()
                .map(p -> BattleProgressResponse.ParticipantProgressInfo.builder()
                        .participantId(p.getParticipantId())
                        .userId(p.getUserId())
                        .username(p.getUsername())
                        .currentScore(p.getCurrentScore())
                        .rank(p.getRank())
                        .isReady(p.isReady()) // 참가자 준비 상태도 포함 (대기 중 상태)
                        .isActive(p.isActive()) // 참가자 활성 상태 포함
                        .correctAnswers(p.getCorrectAnswers())
                        .averageAnswerTime(p.getAverageAnswerTime())
                        .lastAnswerTime(p.getLastAnswerTime())
                        .build())
                .collect(Collectors.toList());

        return BattleProgressResponse.builder()
                .roomId(progress.getRoomId())
                .currentQuestionIndex(progress.getCurrentQuestionIndex())
                .remainingTime(progress.getRemainingTime()) // 남은 시간 (현재 문제 또는 전체 시간)
                .totalQuestions(progress.getTotalQuestions())
                .status(progress.getStatus()) // 현재 방 상태
                .participantProgressList(progressInfos)
                .build();
    }

    /**
     * 대결 종료 시 최종 결과를 계산합니다.
     * 점수 기준으로 참가자 순위를 매기고 승자를 결정합니다.
     *
     * @param room 종료된 대결방 정보
     * @return 계산된 대결 결과 {@link BattleResult}
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
        if (result == null || result.getParticipants() == null) return;

        // TODO: [MODULAR] LevelingService 직접 호출 제거. BattleEndedEvent 발행 후 User 모듈에서 처리.
        // for (BattleParticipant participant : result.getParticipants()) {
        //     try {
        //         levelingService.addExperience(participant.getUser(), result, participant);
        //         log.debug("Awarded experience points to participant: userId={}", participant.getUser().getId());
        //     } catch (Exception e) {
        //         log.error("Failed to award experience points to participant: userId={}, error={}",
        //                   participant.getUser().getId(), e.getMessage(), e);
        //     }
        // }

        // TODO: [MODULAR] Kafka 이벤트 발행 로직 추가 (BattleEndedEvent)
        // Event payload should include necessary info for User module (e.g., roomId, participants' userIds, scores, winnerId, etc.)
        // Example:
        // BattleEndedEvent event = BattleEndedEvent.builder()
        //         .roomId(result.getRoomId())
        //         .winnerUserId(result.getWinner() != null ? result.getWinner().getUserId() : null)
        //         .participants(result.getParticipants().stream()
        //                 .map(p -> new BattleEndedEvent.ParticipantResult(p.getUserId(), p.getCurrentScore()))
        //                 .collect(Collectors.toList()))
        //         .endTime(result.getEndTime())
        //         .build();
        // kafkaProducerService.publish("battle.ended", event);
        log.info("Skipping direct experience point awarding for room {}. Should be handled by User module via BattleEndedEvent.", result.getRoomId());

    }

    /**
     * 대결 결과에 따라 사용자별 배틀 통계를 업데이트합니다. (내부 헬퍼 메서드)
     *
     * @param result 계산된 대결 결과
     */
    private void updateStatistics(BattleResult result) {
        if (result == null || result.getParticipants() == null) return;

        for (BattleParticipant participant : result.getParticipants()) {
            try {
                UserBattleStats stats = participant.getUserBattleStats();
                if (stats == null) {
                    // 통계 정보가 없는 경우 (이론상 발생하면 안됨)
                    log.warn("UserBattleStats not found for participant: userId={}", participant.getUser().getId());
                    // 필요하다면 여기서 다시 조회 또는 생성
                    stats = userBattleStatsRepository.findByUser(participant.getUser())
                            .orElseGet(() -> userBattleStatsRepository.save(new UserBattleStats(participant.getUser())));
                }
                stats.updateStats(participant); // 참가자 정보 기반으로 통계 업데이트
                userBattleStatsRepository.save(stats);
                log.debug("Updated battle statistics for participant: userId={}", participant.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to update statistics for participant: userId={}, error={}",
                          participant.getUser().getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 사용자가 대결방에서 나갈 때 처리합니다. (WebSocket 요청 처리용)
     * Redis에서 참가자 정보를 제거하고, 다른 참가자들에게 알림을 보냅니다.
     *
     * @param request 나가기 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 나가기 처리 결과 {@link BattleLeaveResponse}
     */
    public BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId) {
         log.info("Processing leave request: sessionId={}, request={}", sessionId, request);
         BattleRoom battleRoom = battleRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));
        // TODO: [MODULAR] UserRepository 직접 호출 제거
        // User user = userRepository.findById(request.getUserId())
        //         .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Long userId = request.getUserId(); // Get userId from request

         // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
         // BattleParticipant participant = participantRepository
         //        .findByBattleRoomAndUser(battleRoom, user)
         BattleParticipant participant = participantRepository
                .findByBattleRoomIdAndUserId(request.getRoomId(), userId) // Repository 메서드 변경 가정
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

         // Redis에서 정보 제거
         String key = PARTICIPANT_KEY_PREFIX + sessionId;
         redisTemplate.delete(key);
         log.debug("Removed participant data from Redis: key={}", key);

         // DB 상태 업데이트 (leaveBattleRoom 로직과 유사하게 처리)
         participant.setActive(false); // 비활성화 또는 삭제 (상태에 따라)
         if (battleRoom.getStatus() == BattleRoomStatus.WAITING) {
             participantRepository.delete(participant);
             battleRoom.getParticipants().removeIf(p -> p.getId().equals(participant.getId())); // 컬렉션에서도 제거
              log.info("Participant removed from waiting room: roomId={}, userId={}", request.getRoomId(), request.getUserId());
             // 마지막 참가자 방 삭제 로직 (leaveBattleRoom 참고)
             if (battleRoom.getParticipants().isEmpty()) {
                 battleRoomRepository.delete(battleRoom);
                 log.info("Last participant left waiting room, deleting room: roomId={}", request.getRoomId());
             }
         } else {
             participantRepository.save(participant);
             log.info("Participant marked as inactive in ongoing battle: roomId={}, userId={}", request.getRoomId(), userId);
             // 마지막 활성 참가자 방 종료 로직 (leaveBattleRoom 참고)
              long activeCount = battleRoom.getParticipants().stream().filter(BattleParticipant::isActive).count();
             if (activeCount == 0 && battleRoom.getStatus() == BattleRoomStatus.IN_PROGRESS) {
                 endBattle(request.getRoomId());
             }
         }

        // 알림 발송
        List<BattleParticipant> remainingParticipants = participantRepository.findByBattleRoom(battleRoom);
        messagingTemplate.convertAndSend(
                "/topic/battle/" + request.getRoomId() + "/participants",
                createBattleJoinResponse(battleRoom, null) // 참가자 떠남 알림
        );

        return BattleLeaveResponse.builder()
                .roomId(request.getRoomId())
                .userId(request.getUserId())
                .message("성공적으로 대결방을 나갔습니다.")
                .roomStatus(battleRoom.getStatus()) // 변경되었을 수 있는 방 상태 반환
                .build();
    }

    /**
     * 주어진 ID의 대결방이 유효한지 (존재하는지) 확인합니다.
     *
     * @param roomId 확인할 대결방 ID
     * @return 유효하면 true, 아니면 false
     */
    public boolean isValidBattleRoom(Long roomId) {
        if (roomId == null) return false;
        try {
            return battleRoomRepository.existsById(roomId);
        } catch (Exception e) {
            log.error("Error checking if battle room exists: roomId={}, error={}", roomId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 참가자의 준비 상태를 토글합니다. (WebSocket 요청 처리용)
     * Redis에서 참가자 상태를 업데이트하고, 다른 참가자들에게 알림을 보냅니다.
     * 모든 참가자가 준비되면 대결을 시작합니다.
     *
     * @param request 준비 토글 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 준비 상태 변경 결과 {@link BattleReadyResponse}
     */
    @Transactional
    public synchronized BattleReadyResponse toggleReadyState(BattleReadyRequest request, String sessionId) {
         log.info("Processing ready toggle request: sessionId={}, request={}", sessionId, request);
         BattleRoom room = battleRoomRepository.findByIdWithBasicDetails(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

         BattleParticipant participant = getParticipantFromRedis(sessionId);
        if (participant == null || !participant.getUser().getId().equals(request.getUserId())) {
             log.warn("Participant not found in Redis or user ID mismatch for session: {}, requestUserId={}", sessionId, request.getUserId());
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND, "유효하지 않은 참가자 정보입니다.");
        }

        // 유효성 검사
        validateReadyToggle(participant);

        // 상태 토글 및 Redis 업데이트
        participant.toggleReady();
        saveParticipantToRedis(participant, sessionId); // Redis 업데이트
        participantRepository.save(participant); // DB에도 반영 (선택적이지만 상태 일관성 위해 추천)
         log.info("Participant ready state toggled via WebSocket: roomId={}, userId={}, isReady={}", room.getId(), participant.getUser().getId(), participant.isReady());

        // 응답 생성
        List<BattleParticipant> updatedParticipants = participantRepository.findByBattleRoom(room);
        BattleReadyResponse response = createBattleReadyResponse(room, updatedParticipants);

        // 알림 발송
        messagingTemplate.convertAndSend("/topic/battle/" + request.getRoomId() + "/ready", response);

        // 모든 참가자 준비 시 게임 시작
        if (isReadyToStart(request.getRoomId())) {
            log.info("All participants ready, starting battle automatically: roomId={}", request.getRoomId());
            startBattle(request.getRoomId());
            // 시작 알림은 startBattle 내부에서 전송됨
        }

        return response;
    }

    /**
     * 준비 상태 토글 요청의 유효성을 검사합니다.
     * 대결방 상태가 WAITING 이어야 하고, 참가자가 활성 상태여야 합니다.
     *
     * @param participant 준비 상태를 변경하려는 참가자
     * @throws BusinessException 유효하지 않은 상태일 때
     */
    private void validateReadyToggle(BattleParticipant participant) {
        BattleRoom room = participant.getBattleRoom();
        if (room.getStatus() != BattleRoomStatus.WAITING) {
            throw new BusinessException(ErrorCode.BATTLE_NOT_IN_WAITING_STATE, "대기 중인 대결방에서만 준비 상태를 변경할 수 있습니다.");
        }
        if (!participant.isActive()) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NOT_ACTIVE, "활성 상태인 참가자만 준비 상태를 변경할 수 있습니다.");
        }
        // 방장만 시작 가능 등의 추가 규칙이 있다면 여기에 추가
    }

    /**
     * 특정 대결방의 현재 참가자 목록 정보를 조회합니다. (REST API용)
     *
     * @param roomId 조회할 대결방 ID
     * @return 참가자 목록 정보 {@link BattleJoinResponse}
     * @throws BusinessException 대결방을 찾을 수 없을 때
     */
    public BattleJoinResponse getCurrentBattleParticipants(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdWithDetails(roomId)
                 .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        // 참가자 정보 DTO 생성 (Internal helper)
        return createBattleJoinResponse(room, null);
    }

    /**
     * WebSocket 세션 ID와 특정 대결방의 참가자 정보를 연결합니다.
     * WebSocket 연결이 설정될 때 호출됩니다.
     *
     * @param roomId 대결방 ID
     * @param userId 사용자 ID
     * @param sessionId WebSocket 세션 ID
     * @throws BusinessException 대결방 또는 참가자를 찾을 수 없을 때
     */
     @Transactional
    public void linkSessionToParticipant(Long roomId, Long userId, String sessionId) {
         log.info("Linking WebSocket session to participant: roomId={}, userId={}, sessionId={}", roomId, userId, sessionId);
         BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));
        // TODO: [MODULAR] UserRepository 직접 호출 제거
        // User user = userRepository.findById(userId)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
        // BattleParticipant participant = participantRepository.findByBattleRoomAndUser(room, user)
        BattleParticipant participant = participantRepository.findByBattleRoomIdAndUserId(roomId, userId) // Repository 메서드 변경 가정
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));

        // Redis에 세션 정보 저장
        saveParticipantToRedis(participant, sessionId);
         log.info("Successfully linked session: roomId={}, userId={}, sessionId={}", roomId, userId, sessionId);
    }

    /**
     * 준비 상태 변경 시 WebSocket으로 전송할 응답 DTO를 생성합니다.
     *
     * @param room 대결방 정보
     * @param participants 현재 참가자 목록
     * @return 준비 상태 응답 DTO {@link BattleReadyResponse}
     */
    private BattleReadyResponse createBattleReadyResponse(BattleRoom room, List<BattleParticipant> participants) {
        List<BattleReadyResponse.ParticipantReadyStatus> statuses = participants.stream()
                .map(p -> BattleReadyResponse.ParticipantReadyStatus.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername())
                        .isReady(p.isReady())
                        .isActive(p.isActive())
                        .build())
                .collect(Collectors.toList());

        return BattleReadyResponse.builder()
                .roomId(room.getId())
                .participantStatuses(statuses)
                .build();
    }

    /**
     * 지정된 시간 동안 답변을 제출하지 않은 참가자를 타임아웃 처리합니다.
     * (스케줄러 등에 의해 주기적으로 호출될 수 있음)
     *
     * @param roomId 타임아웃 처리할 대결방 ID
     * @return 타임아웃 처리된 참가자 수
     */
     @Transactional
    public int handleTimeoutParticipants(Long roomId) {
         log.debug("Handling timeout participants for room: {}", roomId);
         BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId)
                 .orElse(null);

        if (room == null || room.getStatus() != BattleRoomStatus.IN_PROGRESS) {
             log.debug("Room not found or not in progress, skipping timeout check: roomId={}", roomId);
            return 0;
        }

        int currentQuestionIndex = room.getCurrentQuestionIndex();
        LocalDateTime questionStartTime = room.getCurrentQuestionStartTime(); // 문제 시작 시간 필드 필요
        int timeLimit = room.getTimeLimitPerQuestion();

        if (questionStartTime == null) {
            log.warn("Question start time is null for room {}, cannot check timeout.", roomId);
            return 0;
        }

        LocalDateTime deadline = questionStartTime.plusSeconds(timeLimit);
        if (LocalDateTime.now().isBefore(deadline)) {
            log.debug("Timeout deadline not reached yet for room {}: deadline={}", roomId, deadline);
            return 0; // 아직 타임아웃 시간 아님
        }

        int timeoutCount = 0;
        List<BattleParticipant> activeParticipants = room.getParticipants().stream()
                .filter(BattleParticipant::isActive)
                .collect(Collectors.toList());

        for (BattleParticipant participant : activeParticipants) {
            // 현재 문제에 대한 답변이 있는지 확인 (BattleAnswer 조회)
            boolean answered = participant.getAnswers().stream()
                    .anyMatch(ans -> ans.getQuestion().getOrderIndex() == currentQuestionIndex);

            if (!answered) {
                log.info("Participant timeout: roomId={}, userId={}, questionIndex={}", roomId, participant.getUser().getId(), currentQuestionIndex);
                // 타임아웃 처리: 점수 0점, 오답 처리, 또는 비활성화 등 정책 적용
                participant.submitAnswer(room.getCurrentQuestion(), "", timeLimit, timeLimit); // 빈 답 제출로 처리
                participantRepository.save(participant);
                saveParticipantToRedis(participant, null); // TODO: Need session ID mapping
                timeoutCount++;
            }
        }

        if (timeoutCount > 0) {
            log.info("{} participants timed out for question {} in room {}.", timeoutCount, currentQuestionIndex, roomId);
            // 모든 참가자가 타임아웃 되었거나 답변했는지 다시 확인 후 다음 문제 진행
            if (allParticipantsAnswered(roomId)) {
                prepareNextQuestionOrEnd(roomId);
            }
        }
        return timeoutCount;
    }

    /**
     * 참가자의 WebSocket 연결이 끊어졌을 때 처리합니다.
     * 참가자를 비활성화하고, 필요한 경우 대결을 종료하며, 다른 참가자들에게 알림을 보냅니다.
     *
     * @param roomId 연결이 끊어진 참가자가 속한 대결방 ID
     * @param userId 연결이 끊어진 사용자 ID
     * @return 처리 결과 {@link BattleLeaveResponse}
     */
     @Transactional
    public BattleLeaveResponse handleParticipantDisconnection(Long roomId, Long userId) {
         log.warn("Handling disconnection for participant: roomId={}, userId={}", roomId, userId);
        BattleRoom room = battleRoomRepository.findByIdWithQuizQuestions(roomId) // Load questions if needed for ending logic
                .orElse(null);
        // TODO: [MODULAR] UserRepository 직접 호출 제거
        // User user = userRepository.findById(userId).orElse(null);

        if (room == null /* || user == null */) { // User null check removed
            log.error("Room or User not found during disconnection handling: roomId={}, userId={}", roomId, userId);
            // 적절한 예외 또는 응답 반환
            return BattleLeaveResponse.builder().message("Room not found").build(); // Adjusted message
        }

        // TODO: [MODULAR] participantRepository.findByBattleRoomAndUser 메서드를 userId 기준으로 동작하도록 수정 필요
        // BattleParticipant participant = participantRepository.findByBattleRoomAndUser(room, user)
        BattleParticipant participant = participantRepository.findByBattleRoomIdAndUserId(roomId, userId) // Repository 메서드 변경 가정
                .orElse(null);

        if (participant == null) {
            log.warn("Participant not found during disconnection handling: roomId={}, userId={}", roomId, userId);
            return BattleLeaveResponse.builder().message("Participant not found").build();
        }

        // Redis에서 정보 제거 (세션 ID 필요 -> 현재 구조로는 어려움)
        // String sessionId = findSessionIdForParticipant(participant); // 세션 ID 찾는 로직 필요
        // if (sessionId != null) {
        //     redisTemplate.delete(PARTICIPANT_KEY_PREFIX + sessionId);
        //     log.debug("Removed disconnected participant data from Redis: sessionId={}", sessionId);
        // }

        // 참가자 비활성화
        participant.setActive(false);
        participantRepository.save(participant);
         log.info("Participant marked as inactive due to disconnection: roomId={}, userId={}", roomId, userId);

        // 대결 상태에 따른 추가 처리
        if (room.getStatus() == BattleRoomStatus.IN_PROGRESS) {
            long activeCount = room.getParticipants().stream().filter(BattleParticipant::isActive).count();
            log.debug("Active participants remaining after disconnection: count={}, roomId={}", activeCount, roomId);
            if (activeCount == 0) {
                 log.info("Last active participant disconnected, ending battle: roomId={}", roomId);
                endBattle(roomId); // 마지막 활성 참가자였으면 배틀 종료
            } else {
                // 현재 문제에 대한 모든 활성 참가자의 답변 여부 확인
                if (allParticipantsAnswered(roomId)) {
                    prepareNextQuestionOrEnd(roomId);
                }
            }
        } else if (room.getStatus() == BattleRoomStatus.WAITING) {
            // 대기 중 상태였다면 목록에서 완전히 제거 가능
             participantRepository.delete(participant);
             room.getParticipants().removeIf(p -> p.getId().equals(participant.getId()));
             log.info("Disconnected participant removed from waiting room: roomId={}, userId={}", roomId, userId);
             if (room.getParticipants().isEmpty()) {
                 battleRoomRepository.delete(room);
                 log.info("Last participant disconnected from waiting room, deleting room: roomId={}", roomId);
             }
        }
        battleRoomRepository.save(room); // 참가자 목록 변경 저장

        // 다른 참가자에게 알림
        List<BattleParticipant> remainingParticipants = participantRepository.findByBattleRoom(room);
        messagingTemplate.convertAndSend(
                "/topic/battle/" + roomId + "/participants",
                createBattleJoinResponse(room, null) // 참가자 떠남 알림
        );

        return BattleLeaveResponse.builder()
                .roomId(roomId)
                .userId(userId)
                .message("참가자 연결 끊김 처리 완료")
                .roomStatus(room.getStatus())
                .build();
    }

    /**
     * 모든 참가자가 답변했는지 확인 후 다음 문제를 준비하거나 대결을 종료하는 통합 메서드.
     * (handleTimeoutParticipants, processAnswer 등에서 호출)
     *
     * @param roomId 처리할 대결방 ID
     */
    private void prepareNextQuestionOrEnd(Long roomId) {
        BattleNextQuestionResponse nextQuestionResponse = prepareNextQuestion(roomId);
        if (nextQuestionResponse != null) {
            // 다음 문제 알림
            messagingTemplate.convertAndSend("/topic/battle/" + roomId + "/next", nextQuestionResponse);
            log.info("Sent next question notification for room: {}", roomId);
            // 다음 문제 타이머 시작 로직 추가 필요
        } else {
            // 대결 종료
            log.info("All questions answered or timeout, ending battle for room: {}", roomId);
            endBattle(roomId);
        }
    }

} 