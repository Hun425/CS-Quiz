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
 * 데일리 퀴즈 관리 서비스 인터페이스
 * 
 * <p>매일 자정에 새로운 데일리 퀴즈를 선택하고, 관리하는 기능을 담당합니다.
 * 요일별 난이도 로테이션, 최근 출제된 퀴즈 제외, 퀴즈 적합도 계산 등의 로직을 구현합니다.</p>
 * 
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface DailyQuizService {

    /**
     * 매일 자정에 새로운 데일리 퀴즈 선택
     * 
     * <p>스케줄링된 작업으로 매일 00:00에 실행되며, 새로운 데일리 퀴즈를 선택합니다.</p>
     */
    void selectDailyQuiz();
    
    /**
     * 현재 데일리 퀴즈 조회 (오늘의 퀴즈)
     * 
     * <p>현재 활성화된 데일리 퀴즈를 반환합니다.</p>
     * 
     * @return 오늘의 데일리 퀴즈
     * @throws com.quizplatform.core.exception.BusinessException 데일리 퀴즈가 없는 경우
     */
    Quiz getCurrentDailyQuiz();
}