package com.quizplatform.quiz.domain.service;

import com.quizplatform.quiz.domain.model.DifficultyLevel;
import com.quizplatform.quiz.domain.model.Question;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.QuizType;
import com.quizplatform.quiz.domain.repository.QuestionRepository;
import com.quizplatform.quiz.domain.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 데일리 퀴즈 서비스 구현
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyQuizServiceImpl implements DailyQuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizEventPublisher eventPublisher;
    
    // 데일리 퀴즈 기본 설정
    private static final int DAILY_QUIZ_QUESTIONS = 5;
    private static final String DAILY_QUIZ_CATEGORY = "daily";
    private static final Long SYSTEM_CREATOR_ID = 1L; // 시스템이 생성한 퀴즈의 creator ID
    
    @Override
    @Transactional
    public Quiz generateDailyQuiz(LocalDate date) {
        log.info("Generating daily quiz for date: {}", date);
        
        // 이미 해당 날짜에 데일리 퀴즈가 있는지 확인
        Optional<Quiz> existingQuiz = getDailyQuizByDate(date);
        if (existingQuiz.isPresent()) {
            log.info("Daily quiz for {} already exists with ID: {}", date, existingQuiz.get().getId());
            return existingQuiz.get();
        }
        
        // 퀴즈 유효 기간 설정 (하루)
        LocalDateTime validUntil = date.plusDays(1).atStartOfDay();
        
        // 데일리 퀴즈 생성
        Quiz dailyQuiz = Quiz.builder()
                .creatorId(SYSTEM_CREATOR_ID)
                .title(date + " 데일리 퀴즈")
                .description("오늘의 데일리 퀴즈입니다. 다양한 주제의 문제가 출제됩니다.")
                .category(DAILY_QUIZ_CATEGORY)
                .difficulty(3) // 중간 난이도
                .timeLimit(10) // 10분 제한 시간
                .passingScore(60) // 60점 합격 기준
                .quizType(QuizType.DAILY)
                .difficultyLevel(DifficultyLevel.INTERMEDIATE)
                .validUntil(validUntil)
                .build();
        
        // 태그 추가
        dailyQuiz.addTag("daily");
        dailyQuiz.addTag(date.toString());
        
        // 저장
        Quiz savedQuiz = quizRepository.save(dailyQuiz);
        
        // 문제 선택 및 추가
        selectAndAddQuestions(savedQuiz);
        
        // 퀴즈 활성화 및 공개
        savedQuiz.activate();
        savedQuiz.publish();
        
        // 이벤트 발행
        eventPublisher.publishDailyQuizCreated(savedQuiz.getId().toString(), date.toString());
        
        log.info("Daily quiz generated successfully for {} with ID: {}", date, savedQuiz.getId());
        return savedQuiz;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> getDailyQuizByDate(LocalDate date) {
        // 해당 날짜의 시작과 끝 시간 계산
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // 데일리 퀴즈 검색 (날짜 기반)
        List<Quiz> dailyQuizzes = quizRepository.findByQuizTypeAndActive(QuizType.DAILY, true).stream()
                .filter(quiz -> {
                    // 퀴즈 생성 시간이 해당 날짜 범위 내에 있는지 확인
                    LocalDateTime createdAt = quiz.getCreatedAt();
                    return createdAt != null && 
                           createdAt.isAfter(startOfDay) && 
                           createdAt.isBefore(endOfDay);
                })
                .collect(Collectors.toList());
        
        return dailyQuizzes.isEmpty() ? Optional.empty() : Optional.of(dailyQuizzes.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Quiz> getTodayDailyQuiz() {
        return getDailyQuizByDate(LocalDate.now());
    }

    @Override
    @Transactional
    public Quiz prepareNextDayQuiz() {
        // 내일 날짜 계산
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        // 내일의 퀴즈 미리 생성
        return generateDailyQuiz(tomorrow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Quiz> getDailyQuizzesBetween(LocalDate startDate, LocalDate endDate) {
        // 기간 내 모든 데일리 퀴즈 조회
        List<Quiz> dailyQuizzes = quizRepository.findByQuizTypeAndActive(QuizType.DAILY, true);
        
        // 시작 날짜와 종료 날짜 사이에 있는 퀴즈만 필터링
        return dailyQuizzes.stream()
                .filter(quiz -> {
                    LocalDate quizDate = quiz.getCreatedAt().toLocalDate();
                    return !quizDate.isBefore(startDate) && !quizDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 데일리 퀴즈에 문제 선택 및 추가
     * 
     * @param quiz 데일리 퀴즈
     */
    private void selectAndAddQuestions(Quiz quiz) {
        // 모든 퀴즈에서 랜덤하게 문제 선택
        List<Quiz> allQuizzes = quizRepository.findByPublishedAndActiveOrderByCreatedAtDesc(true, true, null)
                .stream()
                .filter(q -> q.getQuizType() != QuizType.DAILY) // 데일리 퀴즈는 제외
                .limit(20) // 최근 20개 퀴즈만 대상으로
                .collect(Collectors.toList());
        
        // 모든 퀴즈에서 문제 추출
        List<Question> candidateQuestions = new ArrayList<>();
        for (Quiz q : allQuizzes) {
            candidateQuestions.addAll(q.getQuestions());
        }
        
        // 문제가 충분하지 않은 경우 기본 문제 생성 (실제 구현에서는 문제 풀에서 가져옴)
        if (candidateQuestions.size() < DAILY_QUIZ_QUESTIONS) {
            log.warn("Not enough questions for daily quiz, need to implement fallback");
            // 여기에 기본 문제 생성 로직 구현 (실제 서비스에서 필요)
        }
        
        // 랜덤하게 문제 선택
        Collections.shuffle(candidateQuestions);
        List<Question> selectedQuestions = candidateQuestions.stream()
                .limit(DAILY_QUIZ_QUESTIONS)
                .collect(Collectors.toList());
        
        // 선택된 문제들을 데일리 퀴즈에 추가
        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question originalQuestion = selectedQuestions.get(i);
            
            // 문제 복사 (ID는 제외)
            Question newQuestion = Question.builder()
                    .content(originalQuestion.getContent())
                    .type(originalQuestion.getQuestionType())
                    .explanation(originalQuestion.getExplanation())
                    .points(originalQuestion.getPoints())
                    .order(i + 1) // 순서 지정
                    .answer(originalQuestion.getCorrectAnswer())
                    .codeSnippet(originalQuestion.getCodeSnippet())
                    .build();
            
            // 복사한 문제를 퀴즈에 추가
            quiz.addQuestion(newQuestion);
            
            // 선택지도 복사 (객관식인 경우)
            originalQuestion.getOptions().forEach(option -> {
                newQuestion.addOption(option);
            });
        }
        
        log.info("Added {} questions to daily quiz", selectedQuestions.size());
    }
} 