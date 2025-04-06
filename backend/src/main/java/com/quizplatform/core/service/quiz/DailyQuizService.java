package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.exception.BusinessException;
import com.quizplatform.core.exception.ErrorCode;
import com.quizplatform.core.repository.quiz.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

/**
 * 데일리 퀴즈 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyQuizService {

    private final QuizRepository quizRepository;
    private final Random random = new Random();

    /**
     * 매일 자정에 새로운 데일리 퀴즈 선택
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 00:00에 실행
    @Transactional
    public void selectDailyQuiz() {
        log.info("Selecting new daily quiz");
        
        // 기존 데일리 퀴즈 만료 처리
        expireCurrentDailyQuizzes();
        
        // 새 데일리 퀴즈 선택
        Quiz dailyQuiz = selectRandomQuizForDaily();
        
        // 데일리 퀴즈 설정
        if (dailyQuiz != null) {
            createDailyQuizFromTemplate(dailyQuiz);
            log.info("Selected new daily quiz: {}", dailyQuiz.getTitle());
        } else {
            log.warn("No suitable quiz found for daily quiz selection");
        }
    }
    
    /**
     * 현재 데일리 퀴즈 조회
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
     */
    private void expireCurrentDailyQuizzes() {
        List<Quiz> activeDailyQuizzes = quizRepository.findByQuizTypeAndValidUntilAfter(
                QuizType.DAILY, LocalDateTime.now());
        
        for (Quiz quiz : activeDailyQuizzes) {
            quiz.setValidUntil(LocalDateTime.now());
            log.debug("Expired daily quiz: {}", quiz.getTitle());
        }
    }
    
    /**
     * 데일리 퀴즈로 사용할 랜덤 퀴즈 선택
     */
    private Quiz selectRandomQuizForDaily() {
        // 최근 7일간 데일리 퀴즈로 사용되지 않은 퀴즈 중에서 선택
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
}