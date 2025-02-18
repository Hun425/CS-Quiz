package com.quizplatform.core.service.battle;

import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.battle.BattleRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

// Redis를 활용한 실시간 매칭 서비스
@Service
@RequiredArgsConstructor
public class BattleMatchingService {
    private final RedisTemplate<String, String> redisTemplate;
    private final BattleRoomRepository battleRoomRepository;

    // 매칭 대기열에 사용자 추가
    public void addToMatchingQueue(User user, String difficultyLevel) {
        String queueKey = "matching:queue:" + difficultyLevel;
        redisTemplate.opsForList().rightPush(queueKey, user.getId().toString());

        // 대기열 크기 확인
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        if (queueSize >= 2) { // 2명 이상이면 매칭 시도
            tryMatching(difficultyLevel);
        }
    }

    // 매칭 시도
    @Transactional
    public void tryMatching(String difficultyLevel) {
        String queueKey = "matching:queue:" + difficultyLevel;

        // 락 획득 시도
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent("matching:lock:" + difficultyLevel, "locked", Duration.ofSeconds(10));

        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 대기열에서 2명의 사용자 추출
                List<String> userIds = redisTemplate.opsForList()
                        .range(queueKey, 0, 1);

                if (userIds != null && userIds.size() == 2) {
                    // 적절한 퀴즈 선택
                    Quiz quiz = selectAppropriateQuiz(difficultyLevel);

                    // 대결방 생성
                    BattleRoom room = BattleRoom.create(quiz, 2, 10); // 10분 제한

                    // 참가자 추가
                    userIds.forEach(userId ->
                            room.addParticipant(userRepository.findById(UUID.fromString(userId))
                                    .orElseThrow()));

                    // 대결방 저장
                    battleRoomRepository.save(room);

                    // 매칭된 사용자 대기열에서 제거
                    redisTemplate.opsForList().trim(queueKey, 2, -1);
                }
            } finally {
                // 락 해제
                redisTemplate.delete("matching:lock:" + difficultyLevel);
            }
        }
    }
}