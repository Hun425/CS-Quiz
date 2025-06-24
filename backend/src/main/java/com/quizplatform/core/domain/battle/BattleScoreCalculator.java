package com.quizplatform.core.domain.battle;

import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 배틀 점수 계산 및 승부 결정 클래스
 * 
 * <p>배틀에서 점수 계산, 승자 결정, 보상 처리 등을 담당합니다.
 * 참가자별 점수 집계, 보너스 계산, 최종 순위 결정 등을 처리합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
public class BattleScoreCalculator {

    /**
     * 승자 결정 및 보상 처리
     * 
     * @param participants 참가자 목록
     * @return 승자 참가자
     * @throws BusinessException 참가자가 없을 경우
     */
    public BattleParticipant determineWinnerAndCalculateRewards(Set<BattleParticipant> participants) {
        BattleParticipant winner = participants.stream()
                .max(Comparator.comparingInt(BattleParticipant::getCurrentScore))
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_PARTICIPANTS));
        
        int bonusPoints = calculateWinnerBonus(winner);
        winner.addBonusPoints(bonusPoints);
        
        log.info("승자 결정 완료: userId={}, 최종점수={}, 보너스={}", 
                winner.getUser().getId(), winner.getCurrentScore(), bonusPoints);
        
        return winner;
    }

    /**
     * 승자 보너스 점수 계산
     * 
     * @param winner 배틀 승자
     * @return 보너스 점수
     */
    public int calculateWinnerBonus(BattleParticipant winner) {
        int bonus = 50; // 기본 승리 보너스
        
        // 완벽한 정답률 보너스
        if (winner.hasAllCorrectAnswers()) {
            bonus += 30;
            log.info("완벽한 정답률 보너스 추가: userId={}, 보너스=+30", winner.getUser().getId());
        }
        
        // 빠른 답변 보너스
        Duration avgAnswerTime = winner.getAverageAnswerTime();
        if (avgAnswerTime.getSeconds() < 30) {
            bonus += 20;
            log.info("빠른 답변 보너스 추가: userId={}, 평균시간={}초, 보너스=+20", 
                    winner.getUser().getId(), avgAnswerTime.getSeconds());
        }
        
        log.info("승자 보너스 계산 완료: userId={}, 총보너스={}", winner.getUser().getId(), bonus);
        return bonus;
    }

    /**
     * 참가자별 점수 조회
     * 
     * @param participants 참가자 목록
     * @return 사용자 ID를 키로, 점수를 값으로 하는 맵
     */
    public Map<Long, Integer> getParticipantScores(Set<BattleParticipant> participants) {
        return participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        BattleParticipant::getCurrentScore
                ));
    }

    /**
     * 참가자별 정답률 조회
     * 
     * @param participants 참가자 목록
     * @return 사용자 ID를 키로, 정답률을 값으로 하는 맵
     */
    public Map<Long, Double> getParticipantAccuracies(Set<BattleParticipant> participants) {
        return participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        BattleParticipant::getAccuracy
                ));
    }

    /**
     * 참가자별 평균 답변 시간 조회
     * 
     * @param participants 참가자 목록
     * @return 사용자 ID를 키로, 평균 답변 시간을 값으로 하는 맵
     */
    public Map<Long, Duration> getParticipantAverageAnswerTimes(Set<BattleParticipant> participants) {
        return participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getUser().getId(),
                        BattleParticipant::getAverageAnswerTime
                ));
    }

    /**
     * 배틀 결과 통계 계산
     * 
     * @param participants 참가자 목록
     * @return 배틀 결과 통계
     */
    public BattleResultStatistics calculateBattleStatistics(Set<BattleParticipant> participants) {
        if (participants.isEmpty()) {
            return new BattleResultStatistics(0, 0.0, Duration.ZERO, 0);
        }

        int totalParticipants = participants.size();
        
        // 전체 정답률 계산
        double overallAccuracy = participants.stream()
                .mapToDouble(BattleParticipant::getAccuracy)
                .average()
                .orElse(0.0);
        
        // 전체 평균 답변 시간 계산
        long totalSeconds = participants.stream()
                .mapToLong(p -> p.getAverageAnswerTime().getSeconds())
                .sum();
        Duration averageAnswerTime = Duration.ofSeconds(totalSeconds / totalParticipants);
        
        // 최고 점수 계산
        int highestScore = participants.stream()
                .mapToInt(BattleParticipant::getCurrentScore)
                .max()
                .orElse(0);
        
        return new BattleResultStatistics(totalParticipants, overallAccuracy, averageAnswerTime, highestScore);
    }

    /**
     * 순위별 참가자 목록 반환
     * 
     * @param participants 참가자 목록
     * @return 점수 순으로 정렬된 참가자 목록
     */
    public java.util.List<BattleParticipant> getRankedParticipants(Set<BattleParticipant> participants) {
        return participants.stream()
                .sorted(Comparator.comparingInt(BattleParticipant::getCurrentScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 배틀 결과 통계를 담는 내부 클래스
     */
    public static class BattleResultStatistics {
        private final int totalParticipants;
        private final double overallAccuracy;
        private final Duration averageAnswerTime;
        private final int highestScore;

        public BattleResultStatistics(int totalParticipants, double overallAccuracy, 
                                    Duration averageAnswerTime, int highestScore) {
            this.totalParticipants = totalParticipants;
            this.overallAccuracy = overallAccuracy;
            this.averageAnswerTime = averageAnswerTime;
            this.highestScore = highestScore;
        }

        public int getTotalParticipants() {
            return totalParticipants;
        }

        public double getOverallAccuracy() {
            return overallAccuracy;
        }

        public Duration getAverageAnswerTime() {
            return averageAnswerTime;
        }

        public int getHighestScore() {
            return highestScore;
        }
    }
}