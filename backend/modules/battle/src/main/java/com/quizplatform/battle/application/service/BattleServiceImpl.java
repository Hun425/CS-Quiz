package com.quizplatform.battle.application.service;

import com.quizplatform.battle.application.dto.*;
import com.quizplatform.battle.domain.model.BattleRoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BattleService 인터페이스 구현 클래스
 * 웹소켓 기반 실시간 배틀 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BattleServiceImpl implements BattleService {

    // 실제 Repository 주입 필요
    // private final BattleRoomRepository battleRoomRepository;
    
    private final SimpMessagingTemplate messagingTemplate;
    
    // 메모리 기반 임시 저장소 (실제 구현에서는 DB와 Redis 사용 권장)
    private final Map<Long, BattleJoinResponse> roomsMap = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, BattleParticipant>> participantsMap = new ConcurrentHashMap<>();
    private final Map<String, UserSession> sessionMap = new ConcurrentHashMap<>();
    private final Map<Long, BattleProgressResponse> progressMap = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> questionsMap = new ConcurrentHashMap<>();
    
    // 세션 관리용 내부 클래스
    private static class UserSession {
        Long userId;
        Long roomId;
        
        public UserSession(Long userId, Long roomId) {
            this.userId = userId;
            this.roomId = roomId;
        }
    }
    
    @Override
    public BattleJoinResponse joinBattle(BattleJoinRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        
        // 기존 방이 없으면 생성
        if (!roomsMap.containsKey(roomId)) {
            // 기본 배틀방 정보 생성
            BattleJoinResponse room = new BattleJoinResponse();
            room.setRoomId(roomId);
            room.setStatus(BattleRoomStatus.WAITING);
            room.setMaxParticipants(4); // 기본값
            room.setCreatorId(request.getCreatorUserId());
            room.setParticipants(new ArrayList<>());
            roomsMap.put(roomId, room);
            
            // 참가자 맵 초기화
            participantsMap.put(roomId, new HashMap<>());
            
            // 진행 상태 맵 초기화
            progressMap.put(roomId, new BattleProgressResponse(
                    roomId, 0, 5, new HashMap<>()
            ));
        }
        
        // 방 정보 가져오기
        BattleJoinResponse room = roomsMap.get(roomId);
        
        // 참가자 추가
        BattleParticipant participant = new BattleParticipant(
                userId, "User " + userId, // 실제로는 API 호출로 사용자명 가져옴
                false, 
                Objects.equals(userId, request.getCreatorUserId())
        );
        
        // 참가자 맵에 저장
        participantsMap.get(roomId).put(userId, participant);
        
        // 세션 저장
        sessionMap.put(sessionId, new UserSession(userId, roomId));
        
        // 참가자 목록 갱신
        room.setParticipants(new ArrayList<>(participantsMap.get(roomId).values()));
        
        return room;
    }

    @Override
    public BattleJoinResponse getCurrentBattleParticipants(Long roomId) {
        return roomsMap.getOrDefault(roomId, 
                new BattleJoinResponse(roomId, new ArrayList<>(), BattleRoomStatus.WAITING, 
                        "일반", 4, null));
    }

    @Override
    public void linkSessionToParticipant(Long roomId, Long userId, String sessionId) {
        sessionMap.put(sessionId, new UserSession(userId, roomId));
    }

    @Override
    public BattleReadyResponse toggleReady(BattleReadyRequest request) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        
        // 참가자 맵에서 조회
        Map<Long, BattleParticipant> participants = participantsMap.get(roomId);
        if (participants == null || !participants.containsKey(userId)) {
            throw new NoSuchElementException("참가자를 찾을 수 없습니다");
        }
        
        // 준비 상태 토글
        BattleParticipant participant = participants.get(userId);
        participant.setReady(request.isReady());
        
        // 모든 참가자가 준비됐는지 확인
        boolean allReady = participants.values().stream()
                .allMatch(BattleParticipant::isReady);
        
        // 응답 생성
        BattleReadyResponse response = new BattleReadyResponse(
                roomId, 
                new ArrayList<>(participants.values()),
                allReady
        );
        
        return response;
    }

    @Override
    public boolean isReadyToStart(Long roomId) {
        Map<Long, BattleParticipant> participants = participantsMap.get(roomId);
        if (participants == null || participants.size() < 2) {
            return false;
        }
        
        return participants.values().stream().allMatch(BattleParticipant::isReady);
    }

    @Override
    public void updateRoomStatus(Long roomId, BattleRoomStatus status) {
        BattleJoinResponse room = roomsMap.get(roomId);
        if (room != null) {
            room.setStatus(status);
        }
    }

    @Override
    public BattleNextQuestionResponse prepareNextQuestion(Long roomId) {
        // 방 정보 가져오기
        BattleProgressResponse progress = progressMap.get(roomId);
        
        // 인덱스 증가
        int newIndex = progress.getCurrentQuestionIndex() + 1;
        progress.setCurrentQuestionIndex(newIndex);
        
        // 모든 문제가 끝났는지 확인
        if (newIndex >= progress.getTotalQuestions()) {
            return new BattleNextQuestionResponse(
                    roomId, null, null, null, 
                    newIndex, progress.getTotalQuestions(), 
                    true, 0
            );
        }
        
        // 실제로는 DB 에서 조회할 문제 정보
        Long questionId = getQuestionIdForIndex(roomId, newIndex);
        String questionText = "테스트 문제 " + newIndex;
        List<String> options = List.of(
                "옵션 1", "옵션 2", "옵션 3", "옵션 4"
        );
        
        // 참가자 답변 상태 초기화
        Map<Long, BattleProgressResponse.ParticipantProgress> partProgress = progress.getParticipantProgress();
        for (BattleProgressResponse.ParticipantProgress pp : partProgress.values()) {
            pp.setHasAnsweredCurrent(false);
        }
        
        return new BattleNextQuestionResponse(
                roomId, questionId, questionText, options,
                newIndex, progress.getTotalQuestions(),
                false, 20 // 20초 제한시간
        );
    }

    private Long getQuestionIdForIndex(Long roomId, int index) {
        // 현재 방의 문제 ID 목록
        List<Long> questions = questionsMap.computeIfAbsent(roomId, k -> {
            // 실제로는 DB에서 조회
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ids.add(1000L + i);
            }
            return ids;
        });
        
        if (index < questions.size()) {
            return questions.get(index);
        }
        
        return 0L; // 기본값
    }

    @Override
    public BattleAnswerResponse processAnswer(BattleAnswerRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        Long questionId = request.getQuestionId();
        int answerIndex = request.getAnswerIndex();
        
        // 임의로 정답 여부 결정 (실제로는 DB에서 정답 확인)
        boolean isCorrect = answerIndex == 0; // 0번 선택지가 정답이라고 가정
        int correctAnswerIndex = 0;
        
        // 점수 계산 (빨리 맞출수록 높은 점수)
        int points = isCorrect ? 
                (int)(100 + (3000 - request.getResponseTime()) / 30) : 0;
        
        // 배틀 진행상황 업데이트
        BattleProgressResponse progress = progressMap.get(roomId);
        Map<Long, BattleProgressResponse.ParticipantProgress> partProgress = progress.getParticipantProgress();
        
        // 참가자 진행상황이 없으면 생성
        if (!partProgress.containsKey(userId)) {
            BattleParticipant participant = participantsMap.get(roomId).get(userId);
            
            partProgress.put(userId, new BattleProgressResponse.ParticipantProgress(
                    userId, 
                    participant != null ? participant.getUsername() : "User " + userId,
                    0, 0, false
            ));
        }
        
        // 참가자 진행상황 업데이트
        BattleProgressResponse.ParticipantProgress pp = partProgress.get(userId);
        pp.setHasAnsweredCurrent(true);
        
        if (isCorrect) {
            pp.setCorrectAnswers(pp.getCorrectAnswers() + 1);
            pp.setCurrentScore(pp.getCurrentScore() + points);
        }
        
        // 응답 생성
        return new BattleAnswerResponse(
                roomId, userId, questionId,
                isCorrect, correctAnswerIndex, points,
                isCorrect ? "정답입니다!" : "오답입니다. 정답은 " + correctAnswerIndex + "번 입니다."
        );
    }

    @Override
    public BattleProgressResponse getBattleProgress(Long roomId) {
        return progressMap.getOrDefault(roomId, 
                new BattleProgressResponse(roomId, 0, 5, new HashMap<>()));
    }

    @Override
    public boolean allParticipantsAnswered(Long roomId) {
        BattleProgressResponse progress = progressMap.get(roomId);
        if (progress == null) {
            return false;
        }
        
        // 모든 참가자가 현재 문제에 답변했는지 확인
        return progress.getParticipantProgress().values().stream()
                .allMatch(BattleProgressResponse.ParticipantProgress::isHasAnsweredCurrent);
    }

    @Override
    public BattleResultResponse endBattle(Long roomId) {
        // 방 상태 변경
        updateRoomStatus(roomId, BattleRoomStatus.FINISHED);
        
        // 최종 결과 계산
        BattleProgressResponse progress = progressMap.get(roomId);
        
        List<BattleResultResponse.ParticipantResult> results = new ArrayList<>();
        
        // 참가자별 결과 정리
        for (Map.Entry<Long, BattleProgressResponse.ParticipantProgress> entry : 
                progress.getParticipantProgress().entrySet()) {
            
            BattleProgressResponse.ParticipantProgress pp = entry.getValue();
            
            results.add(new BattleResultResponse.ParticipantResult(
                    pp.getUserId(),
                    pp.getUsername(),
                    pp.getCurrentScore(),
                    pp.getCorrectAnswers(),
                    0 // 순위는 나중에 계산
            ));
        }
        
        // 점수순 정렬
        results.sort((a, b) -> Integer.compare(b.getFinalScore(), a.getFinalScore()));
        
        // 순위 계산
        int rank = 1;
        int prevScore = -1;
        int sameRankCount = 0;
        
        for (BattleResultResponse.ParticipantResult result : results) {
            if (prevScore == result.getFinalScore()) {
                // 동점이면 같은 순위
                sameRankCount++;
            } else {
                // 다른 점수면 순위 업데이트
                rank += sameRankCount;
                sameRankCount = 0;
                prevScore = result.getFinalScore();
            }
            
            result.setRank(rank);
        }
        
        return new BattleResultResponse(roomId, results);
    }

    @Override
    public BattleLeaveResponse leaveBattle(BattleLeaveRequest request, String sessionId) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();
        
        // 방 정보 가져오기
        BattleJoinResponse room = roomsMap.get(roomId);
        if (room == null) {
            throw new NoSuchElementException("배틀방을 찾을 수 없습니다");
        }
        
        // 참가자 맵에서 제거
        Map<Long, BattleParticipant> participants = participantsMap.get(roomId);
        participants.remove(userId);
        
        // 세션 맵에서 제거
        if (sessionId != null) {
            sessionMap.remove(sessionId);
        }
        
        // 남은 참가자 수 계산
        int remainingParticipants = participants.size();
        
        // 참가자가 없으면 방 제거
        boolean roomClosed = false;
        if (remainingParticipants == 0) {
            roomsMap.remove(roomId);
            participantsMap.remove(roomId);
            progressMap.remove(roomId);
            questionsMap.remove(roomId);
            roomClosed = true;
        } else {
            // 참가자 목록 갱신
            room.setParticipants(new ArrayList<>(participants.values()));
        }
        
        return new BattleLeaveResponse(roomId, userId, roomClosed, remainingParticipants);
    }

    @Override
    public List<BattleRoomResponse> getBattleRoomsByStatus(BattleRoomStatus status) {
        List<BattleRoomResponse> result = new ArrayList<>();
        
        // 모든 방을 순회하면서 상태가 일치하는 방만 필터링
        for (Map.Entry<Long, BattleJoinResponse> entry : roomsMap.entrySet()) {
            Long roomId = entry.getKey();
            BattleJoinResponse room = entry.getValue();
            
            // 상태가 일치하는 경우만 추가
            if (room.getStatus() == status) {
                BattleProgressResponse progress = progressMap.get(roomId);
                Map<Long, BattleParticipant> participants = participantsMap.get(roomId);
                
                // BattleRoomResponse 생성
                BattleRoomResponse response = BattleRoomResponse.builder()
                        .roomId(roomId)
                        .status(status)
                        .maxParticipants(room.getMaxParticipants())
                        .currentParticipants(participants != null ? participants.size() : 0)
                        .creatorId(room.getCreatorId())
                        .createdAt(LocalDateTime.now()) // 실제 구현에서는 DB에서 생성 시간 가져와야 함
                        .totalQuestions(progress != null ? progress.getTotalQuestions() : 0)
                        .questionTimeLimitSeconds(30) // 기본값
                        .participants(room.getParticipants())
                        .build();
                
                result.add(response);
            }
        }
        
        return result;
    }
} 