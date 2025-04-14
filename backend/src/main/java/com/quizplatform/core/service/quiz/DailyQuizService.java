package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.quiz.QuizAttemptRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 데일리 퀴즈 관리 서비스
 * 
 * <p>매일 자정에 새로운 데일리 퀴즈를 선택하고, 관리하는 기능을 담당합니다.
 * 요일별 난이도 로테이션, 최근 출제된 퀴즈 제외, 퀴즈 적합도 계산 등의 로직을 구현합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyQuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final Random random = new Random();
    
    // 난이도 로테이션 관련 상수
    private static final int BEGINNER_DAYS = 2;     // 주에 2일은 초급
    private static final int INTERMEDIATE_DAYS = 3; // 주에 3일은 중급
    private static final int ADVANCED_DAYS = 2;     // 주에 2일은 고급
    private static final int HISTORY_DAYS = 14;     // 최근 14일간 선택된 퀴즈는 제외

    /**
     * 매일 자정에 새로운 데일리 퀴즈 선택
     * 
     * <p>스케줄링된 작업으로 매일 00:00에 실행되며, 새로운 데일리 퀴즈를 선택합니다.</p>
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00에 실행
    @Transactional
    public void selectDailyQuiz() {
        log.info("새로운 데일리 퀴즈 선택 시작 - {}", LocalDateTime.now());
        
        try {
            // 기존 데일리 퀴즈 만료 처리
            expireCurrentDailyQuizzes();
            
            // 오늘의 난이도 결정
            DifficultyLevel todayDifficulty = determineTodaysDifficulty();
            log.info("오늘의 난이도: {}", todayDifficulty);
            
            // 새 데일리 퀴즈 선택
            Quiz dailyQuiz = selectBestQuizForDaily(todayDifficulty);
            
            // 데일리 퀴즈 설정
            if (dailyQuiz != null) {
                createDailyQuizFromTemplate(dailyQuiz);
                log.info("새 데일리 퀴즈 선택 완료: [{}] {}", dailyQuiz.getDifficultyLevel(), dailyQuiz.getTitle());
            } else {
                log.warn("적합한 데일리 퀴즈를 찾을 수 없습니다");
                
                // 백업 플랜: 난이도 제한 없이 선택
                Quiz backupQuiz = selectRandomQuizForDaily();
                if (backupQuiz != null) {
                    createDailyQuizFromTemplate(backupQuiz);
                    log.info("백업 플랜으로 데일리 퀴즈 선택: [{}] {}", backupQuiz.getDifficultyLevel(), backupQuiz.getTitle());
                } else {
                    log.error("데일리 퀴즈 선택 실패: 적합한 퀴즈가 없습니다");
                }
            }
        } catch (Exception e) {
            log.error("데일리 퀴즈 선택 중 오류 발생", e);
        }
    }
    
    /**
     * 현재 데일리 퀴즈 조회 (오늘의 퀴즈)
     * 
     * <p>현재 활성화된 데일리 퀴즈를 반환합니다.</p>
     * 
     * @return 오늘의 데일리 퀴즈
     * @throws BusinessException 데일리 퀴즈가 없는 경우
     */
    @Transactional(readOnly = true)
    public Quiz getCurrentDailyQuiz() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        
        return quizRepository.findByQuizTypeAndValidUntilBetween(
                QuizType.DAILY, today, tomorrow)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "오늘의 데일리 퀴즈가 없습니다"));
    }
    
    /**
     * 기존 데일리 퀴즈 만료 처리
     * 
     * <p>현재 활성화된 모든 데일리 퀴즈의 유효기간을 현재 시간으로 설정하여 만료시킵니다.</p>
     */
    private void expireCurrentDailyQuizzes() {
        List<Quiz> activeDailyQuizzes = quizRepository.findByQuizTypeAndValidUntilAfter(
                QuizType.DAILY, LocalDateTime.now());
        
        for (Quiz quiz : activeDailyQuizzes) {
            quiz.setValidUntil(LocalDateTime.now());
            log.debug("데일리 퀴즈 만료 처리: {}", quiz.getTitle());
            quizRepository.save(quiz);
        }
    }
    
    /**
     * 요일별 난이도 결정 로직
     * 
     * <p>주간 난이도 밸런스를 맞추기 위해 요일에 따라 난이도를 결정합니다.
     * 월-화는 초급, 수-목-금은 중급, 토-일은 고급으로 설정됩니다.</p>
     * 
     * @return 오늘의 난이도
     */
    private DifficultyLevel determineTodaysDifficulty() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1 (월요일) ~ 7 (일요일)
        
        // 주간 패턴: 초급(월,화) -> 중급(수,목,금) -> 고급(토,일)
        if (dayOfWeek <= BEGINNER_DAYS) {
            return DifficultyLevel.BEGINNER;
        } else if (dayOfWeek <= BEGINNER_DAYS + INTERMEDIATE_DAYS) {
            return DifficultyLevel.INTERMEDIATE;
        } else {
            return DifficultyLevel.ADVANCED;
        }
    }
    
    /**
     * 참여도, 평가, 난이도를 고려해 최적의 데일리 퀴즈 선택
     * 
     * <p>지정된 난이도에 맞는 퀴즈 중에서 조회수, 평균 점수, 시도 횟수 등을 고려하여
     * 데일리 퀴즈로 가장 적합한 퀴즈를 선택합니다.</p>
     * 
     * @param targetDifficulty 목표 난이도
     * @return 선택된 퀴즈, 적합한 퀴즈가 없는 경우 null
     */
    private Quiz selectBestQuizForDaily(DifficultyLevel targetDifficulty) {
        // 최근 14일간 사용된 퀴즈 ID 목록 조회
        LocalDateTime since = LocalDateTime.now().minusDays(HISTORY_DAYS);
        List<Quiz> recentDailyQuizzes = quizRepository.findRecentDailyQuizzes(since);
        List<Long> recentQuizIds = recentDailyQuizzes.stream()
                .map(Quiz::getId)
                .collect(Collectors.toList());
        
        // 적합한 퀴즈 후보 목록 조회
        List<Quiz> eligibleQuizzes = quizRepository.findByQuizTypeAndIsPublicTrue(QuizType.REGULAR).stream()
                .filter(quiz -> quiz.getDifficultyLevel() == targetDifficulty)
                .filter(quiz -> !recentQuizIds.contains(quiz.getId()))
                .filter(quiz -> quiz.getQuestionCount() >= 5) // 최소 5문제 이상
                .collect(Collectors.toList());
        
        if (eligibleQuizzes.isEmpty()) {
            log.warn("지정된 난이도 {}에 맞는 퀴즈가 없습니다", targetDifficulty);
            return null;
        }
        
        // 퀴즈 우선순위 계산 및 정렬
        List<QuizCandidate> candidates = new ArrayList<>();
        for (Quiz quiz : eligibleQuizzes) {
            int attemptCount = quiz.getAttemptCount();
            double avgScore = quiz.getAvgScore();
            int viewCount = quiz.getViewCount();
            
            // 점수 계산 (조회수, 평균 점수, 시도 횟수를 고려)
            double score = calculateQuizScore(viewCount, avgScore, attemptCount);
            candidates.add(new QuizCandidate(quiz, score));
        }
        
        // 점수 내림차순 정렬
        candidates.sort((a, b) -> Double.compare(b.score, a.score));
        
        // 상위 30%에서 랜덤 선택 (다양성 확보)
        int topCandidatesCount = Math.max(1, (int)(candidates.size() * 0.3));
        int randomIndex = random.nextInt(topCandidatesCount);
        
        return candidates.get(randomIndex).quiz;
    }
    
    /**
     * 퀴즈 점수 계산 (데일리 퀴즈로서의 적합도)
     * 
     * <p>조회수, 평균 점수, 시도 횟수를 기반으로 데일리 퀴즈로서의 적합도를 계산합니다.</p>
     * 
     * @param viewCount 조회수
     * @param avgScore 평균 점수
     * @param attemptCount 시도 횟수
     * @return 계산된 적합도 점수 (0.0 ~ 1.0)
     */
    private double calculateQuizScore(int viewCount, double avgScore, int attemptCount) {
        // 조회수가 많을수록 좋음 (인기도), 0.4의 가중치
        double viewScore = Math.min(1.0, viewCount / 100.0) * 0.4;
        
        // 평균 점수가 중간 정도일수록 좋음 (너무 쉽거나 어렵지 않게), 0.3의 가중치
        double difficultyScore = (1.0 - Math.abs(avgScore - 70.0) / 30.0) * 0.3;
        
        // 시도 횟수가 적당히 많을수록 좋음 (검증된 퀴즈), 0.3의 가중치
        double attemptScore = Math.min(1.0, attemptCount / 50.0) * 0.3;
        
        return viewScore + difficultyScore + attemptScore;
    }
    
    /**
     * 백업용: 적합한 퀴즈가 없을 경우 랜덤 선택
     * 
     * <p>지정된 난이도에 맞는 퀴즈가 없는 경우 사용되는 백업 메서드로,
     * 최근 7일간 사용되지 않은 퀴즈 중에서 랜덤하게 선택합니다.</p>
     * 
     * @return 선택된 퀴즈, 적합한 퀴즈가 없는 경우 null
     */
    private Quiz selectRandomQuizForDaily() {
        // 최근 7일간 사용된 퀴즈 제외
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Quiz> eligibleQuizzes = quizRepository.findEligibleQuizzesForDaily(sevenDaysAgo);
        
        if (eligibleQuizzes.isEmpty()) {
            // 적합한 퀴즈가 없으면 모든 일반 퀴즈 중에서 선택
            eligibleQuizzes = quizRepository.findByQuizTypeAndIsPublicTrue(QuizType.REGULAR);
        }
        
        if (eligibleQuizzes.isEmpty()) {
            return null;
        }
        
        // 랜덤 선택
        return eligibleQuizzes.get(random.nextInt(eligibleQuizzes.size()));
    }
    
    /**
     * 기존 퀴즈를 바탕으로 데일리 퀴즈 생성
     * 
     * <p>선택된 템플릿 퀴즈를 기반으로 새로운 데일리 퀴즈를 생성하고,
     * 유효기간을 다음날 자정까지로 설정합니다.</p>
     * 
     * @param template 템플릿으로 사용할 퀴즈
     * @return 생성된 데일리 퀴즈
     */
    private Quiz createDailyQuizFromTemplate(Quiz template) {
        // 데일리 퀴즈 복사본 생성
        Quiz dailyQuiz = template.createDailyCopy();
        
        // 유효기간 설정 (다음날 자정까지)
        LocalDateTime midnight = LocalDate.now().plusDays(1).atStartOfDay();
        dailyQuiz.setValidUntil(midnight);
        
        // 저장
        return quizRepository.save(dailyQuiz);
    }
    
    /**
     * 퀴즈 후보 클래스 (점수 계산용)
     * 
     * <p>퀴즈와 그 적합도 점수를 함께 저장하기 위한 내부 클래스입니다.</p>
     */
    private static class QuizCandidate {
        private final Quiz quiz;
        private final double score;
        
        /**
         * 퀴즈 후보 생성자
         * 
         * @param quiz 퀴즈 객체
         * @param score 계산된 적합도 점수
         */
        public QuizCandidate(Quiz quiz, double score) {
            this.quiz = quiz;
            this.score = score;
        }
    }
}