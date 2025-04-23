package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.Quiz;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 데일리 퀴즈 서비스 인터페이스
 * 
 * <p>매일 새로운 퀴즈를 생성하고 관리하는 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface DailyQuizService {
    
    /**
     * 데일리 퀴즈 생성
     * 
     * @param date 날짜
     * @return 생성된 데일리 퀴즈
     */
    Quiz generateDailyQuiz(LocalDate date);
    
    /**
     * 특정 날짜의 데일리 퀴즈 조회
     * 
     * @param date 날짜
     * @return 데일리 퀴즈
     */
    Optional<Quiz> getDailyQuizByDate(LocalDate date);
    
    /**
     * 오늘의 데일리 퀴즈 조회
     * 
     * @return 오늘의 데일리 퀴즈
     */
    Optional<Quiz> getTodayDailyQuiz();
    
    /**
     * 내일의 데일리 퀴즈 미리 생성
     * 
     * @return 생성된 내일의 데일리 퀴즈
     */
    Quiz prepareNextDayQuiz();
    
    /**
     * 특정 기간의 데일리 퀴즈 목록 조회
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 기간 내 데일리 퀴즈 목록
     */
    Iterable<Quiz> getDailyQuizzesBetween(LocalDate startDate, LocalDate endDate);
} 