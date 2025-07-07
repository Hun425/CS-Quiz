package com.quizplatform.core.service.battle;

import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.battle.BattleRoomStatus;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.battle.*;

/**
 * 배틀 모드 게임 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 * <p>사용자들이 실시간으로 경쟁하는 배틀 모드의 생성, 참가, 진행, 종료 등
 * 전체 생명주기를 관리합니다. WebSocket을 통한 실시간 통신과 Redis를 활용한
 * 세션 관리를 포함합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface BattleService {

    /**
     * 새로운 대결방을 생성합니다.
     * 방 생성자를 첫 번째 참가자로 자동 추가합니다.
     *
     * @param creator         대결방을 생성하는 사용자
     * @param quizId          대결에서 사용할 퀴즈의 ID
     * @param maxParticipants 최대 참가자 수 (null일 경우 기본값 4)
     * @return 생성된 대결방 정보를 담은 {@link BattleRoomResponse}
     */
    BattleRoomResponse createBattleRoom(User creator, Long quizId, Integer maxParticipants);

    /**
     * 특정 ID의 대결방 상세 정보를 조회합니다.
     * 참가자 및 퀴즈 정보를 포함하여 조회합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 조회된 대결방 정보를 담은 {@link BattleRoomResponse}
     */
    BattleRoomResponse getBattleRoom(Long roomId);

    /**
     * 특정 상태(대기중, 진행중 등)의 대결방 목록을 조회합니다.
     * N+1 문제를 방지하기 위해 상세 정보를 별도로 조회합니다.
     *
     * @param status 조회할 대결방의 상태 ({@link BattleRoomStatus})
     * @return 해당 상태의 대결방 목록 ({@link BattleRoomResponse} 리스트)
     */
    java.util.List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status);

    /**
     * 특정 사용자가 현재 참여하고 있는 '진행중' 상태의 대결방을 조회합니다.
     *
     * @param user 조회할 사용자
     * @return 사용자가 참여중인 활성 대결방 정보 ({@link BattleRoomResponse}), 없으면 null
     */
    BattleRoomResponse getActiveBattleRoomByUser(User user);

    /**
     * 사용자가 특정 대결방에 참가합니다.
     * 대기 중인 방에만 참가 가능하며, 정원 초과 및 중복 참가를 확인합니다.
     * 참가 시 WebSocket으로 참가자 목록 업데이트 메시지를 전송합니다.
     *
     * @param roomId 참가할 대결방의 ID
     * @param user   참가하려는 사용자
     * @return 참가 후 업데이트된 대결방 정보 ({@link BattleRoomResponse})
     */
    BattleRoomResponse joinBattleRoom(Long roomId, User user);

    /**
     * 참가자의 준비 상태를 토글합니다 (준비/준비 해제).
     * 모든 참가자가 준비 완료되면 자동으로 대결을 시작합니다.
     *
     * @param roomId 준비 상태를 변경할 대결방의 ID
     * @param user   준비 상태를 변경할 사용자
     * @return 상태 변경 후의 대결방 정보 ({@link BattleRoomResponse})
     */
    BattleRoomResponse toggleReady(Long roomId, User user);

    /**
     * 사용자가 대기 중인 대결방에서 나갑니다.
     * 진행 중인 대결에서는 나갈 수 없습니다.
     * 마지막 참가자가 나가면 대결방은 삭제됩니다.
     *
     * @param roomId 나갈 대결방의 ID
     * @param user   나가려는 사용자
     * @return 업데이트된 대결방 정보 ({@link BattleRoomResponse}), 방이 삭제되면 null 반환
     */
    BattleRoomResponse leaveBattleRoom(Long roomId, User user);

    /**
     * WebSocket 연결 시 대결방 입장 처리를 합니다.
     * 사용자를 참가자로 확인/추가하고, WebSocket 세션 ID와 참가자 ID를 Redis에 저장합니다.
     *
     * @param request   입장 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 입장 처리 결과 및 현재 참가자 정보를 담은 {@link BattleJoinResponse}
     */
    BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId);

    /**
     * 사용자가 제출한 답변을 처리합니다.
     * Redis에서 세션 ID로 참가자를 조회하고, 답변의 유효성(진행중인 문제, 중복 답변 등)을 검증합니다.
     * 점수를 계산하고 답변 정보를 저장한 후, 업데이트된 참가자 정보를 Redis에 반영합니다.
     *
     * @param request   답변 요청 정보 (roomId, questionId, answer, timeSpentSeconds)
     * @param sessionId WebSocket 세션 ID
     * @return 답변 처리 결과 (정답 여부, 획득 점수, 현재 총점 등)를 담은 {@link BattleAnswerResponse}
     */
    BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId);

    /**
     * 대결방의 모든 참가자가 준비를 완료했는지 확인합니다. (대결 시작 가능 여부 확인)
     * 대기 상태(WAITING)인 방만 확인합니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 모든 참가자가 준비 완료 상태면 true, 아니면 false
     */
    boolean isReadyToStart(Long roomId);

    /**
     * 대결을 시작합니다.
     * 방 상태를 IN_PROGRESS로 변경하고 시작 시간을 기록합니다.
     *
     * @param roomId 시작할 대결방의 ID
     * @return 대결 시작 정보(참가자, 총 문제 수, 첫 문제 정보 등)를 담은 {@link BattleStartResponse}
     */
    BattleStartResponse startBattle(Long roomId);

    /**
     * 다음 문제를 준비하고 해당 문제 정보를 반환합니다.
     * 대결방의 현재 문제 인덱스를 증가시키고 다음 문제를 로드합니다.
     * 더 이상 문제가 없으면 게임 종료 상태를 포함한 응답을 반환하고 방 상태를 FINISHED로 변경합니다.
     *
     * @param roomId 진행 중인 대결방의 ID
     * @return 다음 문제 정보 또는 게임 종료 상태를 담은 {@link BattleNextQuestionResponse}
     */
    BattleNextQuestionResponse prepareNextQuestion(Long roomId);

    /**
     * 현재 진행 중인 문제에 대해 모든 활성 참가자가 답변을 완료했는지 확인합니다.
     * 지연 로딩 문제를 피하기 위해 참가자의 답변 목록을 명시적으로 로드하여 확인합니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 모든 활성 참가자가 현재 문제에 대한 답변을 완료했으면 true, 아니면 false
     */
    boolean allParticipantsAnswered(Long roomId);

    /**
     * 대결 진행 상황 (점수판)을 조회합니다.
     * 현재 문제 인덱스, 남은 시간, 각 참가자의 점수 및 답변 상태 등을 포함합니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 대결 진행 상황 정보를 담은 {@link BattleProgressResponse}
     */
    BattleProgressResponse getBattleProgress(Long roomId);

    /**
     * 대결을 종료 처리하고 최종 결과를 계산합니다.
     * 방 상태를 FINISHED로 변경하고, 최종 점수 및 순위를 계산합니다.
     * 결과에 따라 참가자들에게 경험치를 부여하고, 사용자 및 퀴즈 통계를 업데이트합니다.
     *
     * @param roomId 종료할 대결방의 ID
     * @return 최종 대결 결과(승자, 참가자 순위, 점수 등)를 담은 {@link BattleEndResponse}
     */
    BattleEndResponse endBattle(Long roomId);

    /**
     * 사용자가 대결(주로 대기 중)에서 나가는 요청을 처리합니다 (WebSocket 메시지 처리).
     * 참가자를 비활성(active=false) 상태로 변경합니다.
     * 대기 중 상태에서 활성 참가자가 1명 미만이 되면 방 상태를 FINISHED로 변경합니다.
     *
     * @param request   나가기 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID (현재 로직에서는 사용되지 않음)
     * @return 나가기 처리 결과(사용자 ID, 방 ID, 변경된 방 상태)를 담은 {@link BattleLeaveResponse}
     */
    BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId);

    /**
     * 대결방이 유효한 상태인지 확인합니다.
     * 주로 대기(WAITING) 상태일 때, 활성 참가자가 1명 이상 있는지 확인하는 데 사용됩니다.
     * 다른 상태(진행중, 종료 등)는 항상 유효하다고 간주합니다.
     *
     * @param roomId 확인할 대결방의 ID
     * @return 대결방이 유효하면 true, 아니면 false
     */
    boolean isValidBattleRoom(Long roomId);

    /**
     * WebSocket을 통해 참가자의 준비 상태 토글 요청을 처리합니다.
     * 요청한 사용자를 찾아 준비 상태를 변경하고, 변경된 참가자 목록 정보를 포함한 응답을 반환합니다.
     * 세션 ID와 참가자를 Redis에 연결합니다.
     *
     * @param request   준비 상태 변경 요청 정보 (roomId, userId)
     * @param sessionId WebSocket 세션 ID
     * @return 변경된 준비 상태를 포함한 참가자 목록 정보를 담은 {@link BattleReadyResponse}
     */
    BattleReadyResponse toggleReadyState(BattleReadyRequest request, String sessionId);

    /**
     * 특정 대결방의 현재 참가자 목록 정보를 조회합니다.
     * 주로 방장이 처음 연결되었을 때 현재 방 상태를 가져오기 위해 사용될 수 있습니다.
     *
     * @param roomId 조회할 대결방의 ID
     * @return 현재 참가자 목록 정보를 담은 {@link BattleJoinResponse}
     */
    BattleJoinResponse getCurrentBattleParticipants(Long roomId);

    /**
     * 특정 WebSocket 세션 ID를 특정 사용자의 참가자 정보와 연결합니다.
     * 주로 방장이 WebSocket에 연결되었을 때 호출되어 세션과 참가자를 매핑합니다.
     *
     * @param roomId    대상 대결방 ID
     * @param userId    연결할 사용자 ID
     * @param sessionId 연결할 WebSocket 세션 ID
     */
    void linkSessionToParticipant(Long roomId, Long userId, String sessionId);

    /**
     * 시간 내에 문제를 풀지 못한 참가자를 처리합니다.
     * 현재 진행 중인 문제에 미응답 상태인 참가자들에게 자동으로 오답 처리합니다.
     *
     * @param roomId 배틀룸 ID
     * @return 처리된 참가자 수
     */
    int handleTimeoutParticipants(Long roomId);

    /**
     * 배틀 중간에 이탈한 참가자를 처리합니다.
     * WebSocket 연결이 끊긴 참가자를 자동으로 비활성화하고 필요시 오답 처리합니다.
     *
     * @param roomId 배틀룸 ID
     * @param userId 참가자 ID
     * @return 처리 결과 응답
     */
    BattleLeaveResponse handleParticipantDisconnection(Long roomId, Long userId);

    /**
     * 사용자가 배틀룸의 생성자(방장)인지 확인합니다.
     * 
     * <p>주어진 사용자 ID가 배틀룸의 생성자인지 검증합니다.
     * 강제 진행 등 방장 권한이 필요한 작업에서 사용됩니다.</p>
     * 
     * @param roomId 배틀룸 ID
     * @param userId 확인할 사용자 ID
     * @return 생성자인 경우 true, 아니면 false
     */
    boolean isRoomCreator(Long roomId, Long userId);
}