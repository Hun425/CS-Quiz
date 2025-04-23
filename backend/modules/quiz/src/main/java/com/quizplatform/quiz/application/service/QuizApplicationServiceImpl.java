package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.dto.QuizCreateRequest;
import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.application.dto.QuizUpdateRequest;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.User;
import com.quizplatform.quiz.domain.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 퀴즈 애플리케이션 서비스 구현체
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizApplicationServiceImpl implements QuizApplicationService {

    private final QuizService quizService;
    
    @Override
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request) {
        // 도메인 서비스에 요청을 위임하고 결과를 반환
        Quiz quiz = convertToQuizDomain(request);
        Quiz createdQuiz = quizService.createQuiz(quiz);
        return convertToQuizResponse(createdQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("퀴즈를 찾을 수 없습니다. ID: " + id));
        return convertToQuizResponse(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getAllQuizzes(Pageable pageable) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByCategory(String category, Pageable pageable) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByDifficulty(int difficulty, Pageable pageable) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByCreator(Long creatorId, Pageable pageable) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(Long id, QuizUpdateRequest request) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional
    public void deleteQuiz(Long id) {
        // 구현 필요
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional
    public QuizResponse setQuizPublishStatus(Long id, boolean publish) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional
    public QuizResponse setQuizActiveStatus(Long id, boolean active) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> searchQuizzes(String keyword, Pageable pageable) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getRecommendedQuizzes(User user, int limit) {
        // 구현 필요 - 현재는 가짜 구현
        throw new UnsupportedOperationException("아직 구현되지 않았습니다");
    }
    
    /**
     * 요청 DTO를 도메인 모델로 변환
     */
    private Quiz convertToQuizDomain(QuizCreateRequest request) {
        // 구현 필요 - 현재는 가짜 구현
        return new Quiz();
    }
    
    /**
     * 도메인 모델을 응답 DTO로 변환
     */
    private QuizResponse convertToQuizResponse(Quiz quiz) {
        // 구현 필요 - 현재는 가짜 구현
        return new QuizResponse();
    }
} 