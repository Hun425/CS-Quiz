package com.quizplatform.battle.application.service;

import com.quizplatform.battle.domain.event.BattleCompletedEvent;
import com.quizplatform.battle.domain.model.*;
import com.quizplatform.battle.infrastructure.kafka.BattleEventProducer;
import com.quizplatform.battle.infrastructure.repository.BattleAnswerRepository;
import com.quizplatform.battle.infrastructure.repository.BattleParticipantRepository;
import com.quizplatform.battle.infrastructure.repository.BattleRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 배틀 모드 게임 관련 비즈니스 로직을 처리하는 서비스
 * 
 * <p>배틀의 생성, 참가, 진행, 종료 등 전체 생명주기를 관리합니다.
 * 모듈화된 아키텍처에서 이벤트 기반으로 다른 모듈과 통신합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BattleService {
    private final BattleRoomRepository battleRoomRepository;
    private final BattleParticipantRepository participantRepository;
    private final BattleAnswerRepository answerRepository;
    private final BattleEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;

    // Redis 키 접두사
    private static final String BATTLE_ROOM_KEY_PREFIX = "battle:room:";
    private static final String PARTICIPANT_KEY_PREFIX = "battle:participant:";
    private static final int ROOM_EXPIRE_SECONDS = 3600; // 1시간

    /**
     * 새로운 배틀방을 생성합니다.
     * 
     * @param quizId 사용할 퀴즈 ID
     * @param maxParticipants 최대 참가자 수
     * @param creatorId 방 생성자 ID
     * @param creatorUsername 방 생성자 사용자명
     * @param creatorProfileImage 방 생성자 프로필 이미지
     * @param totalQuestions 문제 총 개수
     * @param questionTimeLimitSeconds 문제당 시간 제한(초)
     * @return 생성된 배틀방
     */
    public BattleRoom createBattleRoom(Long quizId, int maxParticipants, Long creatorId, 
                                       String creatorUsername, String creatorProfileImage,
                                       int totalQuestions, Integer questionTimeLimitSeconds) {
        // 배틀방 생성
        BattleRoom battleRoom = BattleRoom.builder()
                .quizId(quizId)
                .maxParticipants(maxParticipants)
                .creatorId(creatorId)
                .totalQuestions(totalQuestions)
                .questionTimeLimitSeconds(questionTimeLimitSeconds)
                .build();

        // 배틀방 저장
        BattleRoom savedRoom = battleRoomRepository.save(battleRoom);

        // 방장을 첫 참가자로 추가
        addParticipant(savedRoom, creatorId, creatorUsername, creatorProfileImage);

        return savedRoom;
    }

    /**
     * 특정 ID의 배틀방을 조회합니다.
     * 
     * @param roomId 조회할 배틀방 ID
     * @return 조회된 배틀방
     * @throws NoSuchElementException 배틀방을 찾을 수 없을 때
     */
    public BattleRoom getBattleRoom(Long roomId) {
        return battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new NoSuchElementException("배틀방을 찾을 수 없습니다. ID: " + roomId));
    }

    /**
     * 특정 상태의 배틀방 목록을 조회합니다.
     * 
     * @param status 조회할 배틀방 상태
     * @return 해당 상태의 배틀방 목록
     */
    public List<BattleRoom> getBattleRoomsByStatus(BattleRoomStatus status) {
        return battleRoomRepository.findByStatus(status);
    }

    /**
     * 사용자가 특정 배틀방에 참가합니다.
     * 
     * @param roomId 참가할 배틀방 ID
     * @param userId 참가자 ID
     * @param username 참가자 사용자명
     * @param profileImage 참가자 프로필 이미지
     * @return 참가 후 업데이트된 배틀방
     * @throws NoSuchElementException 배틀방을 찾을 수 없을 때
     * @throws IllegalStateException 참가할 수 없는 상태일 때
     */
    public BattleRoom joinBattleRoom(Long roomId, Long userId, String username, String profileImage) {
        BattleRoom battleRoom = battleRoomRepository.findByIdWithDetails(roomId)
                .orElseThrow(() -> new NoSuchElementException("배틀방을 찾을 수 없습니다. ID: " + roomId));

        // 참가자 추가
        addParticipant(battleRoom, userId, username, profileImage);

        return battleRoom;
    }

    /**
     * 참가자의 준비 상태를 토글합니다.
     * 모든 참가자가 준비되면 배틀을 시작합니다.
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방
     */
    public BattleRoom toggleReady(Long roomId, Long userId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 참가자 찾기
        BattleParticipant participant = battleRoom.getParticipant(userId)
                .orElseThrow(() -> new NoSuchElementException("참가자를 찾을 수 없습니다. 사용자 ID: " + userId));
        
        // 준비 상태 토글
        participant.toggleReady();
        
        // 모든 참가자가 준비되었고 최소 인원을 만족하면 배틀 시작
        if (battleRoom.isReadyToStart()) {
            startBattle(roomId);
        }
        
        return battleRoomRepository.save(battleRoom);
    }

    /**
     * 배틀방에서 참가자를 제거합니다.
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @return 업데이트된 배틀방
     */
    public BattleRoom leaveBattleRoom(Long roomId, Long userId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 참가자 찾기
        BattleParticipant participant = battleRoom.getParticipant(userId)
                .orElseThrow(() -> new NoSuchElementException("참가자를 찾을 수 없습니다. 사용자 ID: " + userId));
        
        // 진행 중인 배틀이라면 포기 처리
        if (battleRoom.getStatus() == BattleRoomStatus.IN_PROGRESS) {
            participant.forfeit();
            
            // 배틀 종료 조건 체크 (1명만 남았거나 모두 포기했을 때)
            long activePlayers = battleRoom.getParticipants().stream()
                    .filter(p -> !p.hasForfeited())
                    .count();
            
            if (activePlayers <= 1) {
                finishBattle(roomId);
            }
        } else {
            // 대기 중이라면 참가자 제거
            participantRepository.delete(participant);
            battleRoom.getParticipants().remove(participant);
        }
        
        return battleRoomRepository.save(battleRoom);
    }

    /**
     * 배틀을 시작합니다.
     * 
     * @param roomId 배틀방 ID
     * @return 시작된 배틀방
     */
    @Transactional
    public BattleRoom startBattle(Long roomId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 배틀 시작
        battleRoom.startBattle();
        
        return battleRoomRepository.save(battleRoom);
    }

    /**
     * 배틀에서 다음 문제로 진행합니다.
     * 
     * @param roomId 배틀방 ID
     * @return 업데이트된 배틀방
     */
    @Transactional
    public BattleRoom startNextQuestion(Long roomId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 다음 문제로 진행
        battleRoom.startNextQuestion();
        
        return battleRoomRepository.save(battleRoom);
    }

    /**
     * 배틀 참가자의 문제 답변을 처리합니다.
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @param questionIndex 문제 인덱스
     * @param answer 제출한 답변
     * @param isCorrect 정답 여부
     * @param answerTime 답변 소요 시간(ms)
     * @return 처리된 답변
     */
    @Transactional
    public BattleAnswer processAnswer(Long roomId, Long userId, int questionIndex, 
                                     String answer, boolean isCorrect, long answerTime) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 참가자 찾기
        BattleParticipant participant = battleRoom.getParticipant(userId)
                .orElseThrow(() -> new NoSuchElementException("참가자를 찾을 수 없습니다. 사용자 ID: " + userId));
        
        // 답변 생성
        double scoreForAnswer = isCorrect ? calculateScore(answerTime, battleRoom.getQuestionTimeLimitSeconds()) : 0;
        
        BattleAnswer battleAnswer = BattleAnswer.builder()
                .participant(participant)
                .questionIndex(questionIndex)
                .answer(answer)
                .isCorrect(isCorrect)
                .answerTime(answerTime)
                .score(scoreForAnswer)
                .submittedAt(LocalDateTime.now())
                .build();
        
        // 참가자에 답변 추가
        participant.addAnswer(battleAnswer);
        
        // 답변 저장
        answerRepository.save(battleAnswer);
        
        // 모든 참가자가 답변했는지 체크
        if (battleRoom.allParticipantsAnswered()) {
            startNextQuestion(roomId);
        }
        
        return battleAnswer;
    }

    /**
     * 답변 시간에 따른 점수를 계산합니다.
     * 
     * @param answerTimeMs 답변 소요 시간(ms)
     * @param timeLimitSeconds 제한 시간(초)
     * @return 계산된 점수
     */
    private double calculateScore(long answerTimeMs, Integer timeLimitSeconds) {
        if (timeLimitSeconds == null || timeLimitSeconds <= 0) {
            timeLimitSeconds = 30; // 기본값
        }
        
        // 제한 시간을 ms로 변환
        long timeLimitMs = timeLimitSeconds * 1000L;
        
        // 남은 시간 비율 계산 (0~1)
        double timeRatio = Math.max(0, 1.0 - (double)answerTimeMs / timeLimitMs);
        
        // 기본 점수 100점에 시간 비율에 따른 보너스 (최대 100점 추가)
        return 100.0 + (timeRatio * 100.0);
    }

    /**
     * 배틀을 종료하고 결과를 처리합니다.
     * 
     * @param roomId 배틀방 ID
     * @return 종료된 배틀방
     */
    @Transactional
    public BattleRoom finishBattle(Long roomId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        
        // 이미 종료된 배틀이면 바로 반환
        if (battleRoom.getStatus() == BattleRoomStatus.FINISHED) {
            return battleRoom;
        }
        
        // 배틀 종료 처리
        battleRoom.finishBattle();
        
        // 이벤트 발행
        BattleCompletedEvent event = new BattleCompletedEvent(battleRoom);
        eventProducer.publishBattleCompletedEvent(event);
        
        log.info("배틀 종료 - ID: {}, 우승자: {}", roomId, 
                battleRoom.getWinner() != null ? battleRoom.getWinner().getUsername() : "없음");
        
        return battleRoomRepository.save(battleRoom);
    }

    /**
     * 참가자 데이터를 Redis에 저장합니다.
     * 
     * @param participant 저장할 참가자
     * @param sessionId 웹소켓 세션 ID
     */
    private void saveParticipantToRedis(BattleParticipant participant, String sessionId) {
        String key = PARTICIPANT_KEY_PREFIX + sessionId;
        String value = participant.getId().toString();
        redisTemplate.opsForValue().set(key, value, ROOM_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Redis에서 세션 ID로 참가자를 찾습니다.
     * 
     * @param sessionId 웹소켓 세션 ID
     * @return 참가자 객체 (없으면 null)
     */
    private BattleParticipant getParticipantFromRedis(String sessionId) {
        String key = PARTICIPANT_KEY_PREFIX + sessionId;
        String participantId = redisTemplate.opsForValue().get(key);
        
        if (participantId == null) {
            return null;
        }
        
        return participantRepository.findById(Long.valueOf(participantId)).orElse(null);
    }

    /**
     * 참가자를 배틀방에 추가합니다.
     * 
     * @param battleRoom 배틀방
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param profileImage 프로필 이미지
     * @return 추가된 참가자
     */
    private BattleParticipant addParticipant(BattleRoom battleRoom, Long userId, 
                                            String username, String profileImage) {
        // 참가자 추가
        BattleParticipant participant = battleRoom.addParticipant(userId, username, profileImage);
        
        // 참가자 저장
        return participantRepository.save(participant);
    }

    /**
     * 특정 세션 ID를 참가자와 연결합니다.
     * 
     * @param roomId 배틀방 ID
     * @param userId 참가자 ID
     * @param sessionId 세션 ID
     */
    public void linkSessionToParticipant(Long roomId, Long userId, String sessionId) {
        BattleRoom battleRoom = getBattleRoom(roomId);
        BattleParticipant participant = battleRoom.getParticipant(userId)
                .orElseThrow(() -> new NoSuchElementException("참가자를 찾을 수 없습니다. 사용자 ID: " + userId));
        
        saveParticipantToRedis(participant, sessionId);
    }
} 