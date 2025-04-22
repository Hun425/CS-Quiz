package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.port.in.FinishQuizAttemptUseCase;
import com.quizplatform.quiz.application.port.in.GetQuizAttemptUseCase;
import com.quizplatform.quiz.application.port.in.StartQuizAttemptUseCase;
import com.quizplatform.quiz.application.port.in.SubmitQuizAnswerUseCase;
import com.quizplatform.quiz.application.port.in.command.FinishQuizAttemptCommand;
import com.quizplatform.quiz.application.port.in.command.StartQuizAttemptCommand;
import com.quizplatform.quiz.application.port.in.command.SubmitAnswerCommand;
import com.quizplatform.quiz.application.port.out.*;
import com.quizplatform.quiz.domain.event.HighScoreAchievedEvent;
import com.quizplatform.quiz.domain.event.QuizCompletedEvent;
import com.quizplatform.quiz.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 퀴즈 시도 서비스
 */
@Service
@RequiredArgsConstructor
public class QuizAttemptService implements 
        StartQuizAttemptUseCase, 
        SubmitQuizAnswerUseCase, 
        FinishQuizAttemptUseCase,
        GetQuizAttemptUseCase {

    private final LoadQuizPort loadQuizPort;
    private final SaveQuizPort saveQuizPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadQuizAttemptPort loadQuizAttemptPort;
    private final SaveQuizAttemptPort saveQuizAttemptPort;
    private final SaveQuestionAttemptPort saveQuestionAttemptPort;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public QuizAttempt startQuizAttempt(StartQuizAttemptCommand command) {
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findById(command.getQuizId());
        if (quizOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + command.getQuizId());
        }
        
        Quiz quiz = quizOptional.get();
        
        // 퀴즈 유효성 검사 (데일리 퀴즈인 경우 이미 완료했는지 확인)
        if (quiz.getQuizType() == QuizType.DAILY) {
            boolean alreadyCompleted = loadQuizAttemptPort.hasCompletedQuiz(command.getUserId(), command.getQuizId());
            if (alreadyCompleted) {
                throw new IllegalStateException("이미 완료한 데일리 퀴즈입니다.");
            }
        }
        
        // 퀴즈 시도 생성
        QuizAttempt quizAttempt = QuizAttempt.builder()
                .userId(command.getUserId())
                .quizId(command.getQuizId())
                .quizType(quiz.getQuizType())
                .build();
        
        // 퀴즈 시도 저장
        return saveQuizAttemptPort.save(quizAttempt);
    }

    @Override
    @Transactional
    public Quiz getPlayableQuiz(Long quizId, Long userId) {
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findByIdWithQuestions(quizId);
        if (quizOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + quizId);
        }
        
        Quiz quiz = quizOptional.get();
        
        // 공개 여부 확인
        if (!quiz.isPublic() && !quiz.getCreatorId().equals(userId)) {
            throw new IllegalStateException("접근할 수 없는 퀴즈입니다.");
        }
        
        // 데일리 퀴즈 만료 확인
        if (quiz.getQuizType() == QuizType.DAILY && quiz.isExpired()) {
            throw new IllegalStateException("만료된 데일리 퀴즈입니다.");
        }
        
        // 퀴즈 조회수 증가
        saveQuizPort.incrementViewCount(quizId);
        
        return quiz;
    }

    @Override
    @Transactional
    public QuestionAttempt submitAnswer(SubmitAnswerCommand command) {
        // 퀴즈 시도 조회
        Optional<QuizAttempt> quizAttemptOptional = loadQuizAttemptPort.findById(command.getQuizAttemptId());
        if (quizAttemptOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈 시도를 찾을 수 없습니다: " + command.getQuizAttemptId());
        }
        
        QuizAttempt quizAttempt = quizAttemptOptional.get();
        
        // 퀴즈 완료 여부 확인
        if (quizAttempt.isCompleted()) {
            throw new IllegalStateException("이미 완료된 퀴즈입니다.");
        }
        
        // 문제 조회
        Optional<Question> questionOptional = loadQuestionPort.findById(command.getQuestionId());
        if (questionOptional.isEmpty()) {
            throw new NoSuchElementException("문제를 찾을 수 없습니다: " + command.getQuestionId());
        }
        
        Question question = questionOptional.get();
        
        // 문제 시도 생성
        QuestionAttempt questionAttempt = QuestionAttempt.createAttempt(
                command.getQuizAttemptId(),
                question,
                command.getUserAnswer(),
                command.getTimeTaken()
        );
        
        // 문제 시도 저장
        return saveQuestionAttemptPort.save(questionAttempt);
    }

    @Override
    @Transactional
    public QuizAttempt finishQuizAttempt(FinishQuizAttemptCommand command) {
        // 퀴즈 시도 조회
        Optional<QuizAttempt> quizAttemptOptional = loadQuizAttemptPort.findByIdWithQuestionAttempts(command.getQuizAttemptId());
        if (quizAttemptOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈 시도를 찾을 수 없습니다: " + command.getQuizAttemptId());
        }
        
        QuizAttempt quizAttempt = quizAttemptOptional.get();
        
        // 권한 확인
        if (!quizAttempt.getUserId().equals(command.getUserId())) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }
        
        // 이미 완료된 퀴즈인지 확인
        if (quizAttempt.isCompleted()) {
            return quizAttempt;
        }
        
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findByIdWithQuestions(quizAttempt.getQuizId());
        if (quizOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + quizAttempt.getQuizId());
        }
        
        Quiz quiz = quizOptional.get();
        
        // 총 배점과 획득 점수 계산
        int totalPoints = calculateTotalPoints(quiz);
        int earnedPoints = calculateEarnedPoints(quizAttempt, quiz);
        
        // 퀴즈 시도 완료 처리
        QuizAttempt completedAttempt = saveQuizAttemptPort.complete(command.getQuizAttemptId(), totalPoints, earnedPoints);
        
        // 퀴즈 통계 업데이트
        saveQuizPort.updateStatistics(quizAttempt.getQuizId(), completedAttempt.getScore());
        
        // 최고 점수 확인 및 이벤트 발행
        checkAndPublishHighScore(completedAttempt);
        
        // 퀴즈 완료 이벤트 발행
        publishQuizCompletedEvent(completedAttempt, quiz.getDifficultyLevel());
        
        return completedAttempt;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuizAttempt> getQuizAttemptById(Long quizAttemptId) {
        return loadQuizAttemptPort.findByIdWithQuestionAttempts(quizAttemptId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getUserQuizAttempts(Long userId, Long quizId) {
        return loadQuizAttemptPort.findByUserIdAndQuizId(userId, quizId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getAllUserQuizAttempts(Long userId, int limit, int offset) {
        return loadQuizAttemptPort.findByUserId(userId, limit, offset);
    }

    /**
     * 퀴즈의 총 배점을 계산합니다.
     */
    private int calculateTotalPoints(Quiz quiz) {
        return quiz.getQuestions().stream()
                .mapToInt(Question::getPoints)
                .sum();
    }

    /**
     * 획득한 점수를 계산합니다.
     */
    private int calculateEarnedPoints(QuizAttempt quizAttempt, Quiz quiz) {
        // 맞은 문제의 배점 합계
        return quizAttempt.getQuestionAttempts().stream()
                .filter(QuestionAttempt::isCorrect)
                .mapToInt(questionAttempt -> 
                    quiz.getQuestions().stream()
                        .filter(q -> q.getId().equals(questionAttempt.getQuestionId()))
                        .findFirst()
                        .map(Question::getPoints)
                        .orElse(0)
                )
                .sum();
    }

    /**
     * 최고 점수인지 확인하고 해당하는 이벤트를 발행합니다.
     */
    private void checkAndPublishHighScore(QuizAttempt completedAttempt) {
        // 사용자의 이전 시도들 조회
        List<QuizAttempt> previousAttempts = loadQuizAttemptPort.findByUserIdAndQuizId(
                completedAttempt.getUserId(),
                completedAttempt.getQuizId()
        );
        
        // 현재 완료된 시도 제외
        previousAttempts.removeIf(attempt -> attempt.getId().equals(completedAttempt.getId()));
        
        // 이전 최고 점수 계산
        int previousBestScore = previousAttempts.stream()
                .filter(QuizAttempt::isCompleted)
                .map(QuizAttempt::getScore)
                .filter(score -> score != null)
                .max(Integer::compareTo)
                .orElse(0);
        
        // 새 점수가 이전 최고 점수보다 높은지 확인
        if (completedAttempt.getScore() > previousBestScore) {
            // 전체 퀴즈 상위 점수인지 확인 (상위 5% 내 점수일 경우 글로벌 최고 점수로 간주)
            boolean isGlobalTopScore = isGlobalTopScore(completedAttempt);
            
            // 이벤트 발행
            eventPublisher.publish(new HighScoreAchievedEvent(
                    completedAttempt.getQuizId(),
                    completedAttempt.getUserId(),
                    completedAttempt.getScore(),
                    previousBestScore,
                    isGlobalTopScore
            ));
        }
    }

    /**
     * 전체 퀴즈 시도 중 상위 점수인지 확인합니다.
     */
    private boolean isGlobalTopScore(QuizAttempt completedAttempt) {
        // 이 퀴즈의 모든 시도 조회
        List<QuizAttempt> allAttempts = loadQuizAttemptPort.findByQuizId(completedAttempt.getQuizId());
        
        // 완료된 시도의 점수만 추출
        List<Integer> scores = allAttempts.stream()
                .filter(QuizAttempt::isCompleted)
                .map(QuizAttempt::getScore)
                .filter(score -> score != null)
                .sorted((a, b) -> Integer.compare(b, a)) // 내림차순 정렬
                .toList();
        
        // 상위 5% 임계점 계산
        int threshold = (int) Math.ceil(scores.size() * 0.05);
        threshold = Math.max(threshold, 1); // 최소 1개 이상
        
        // 상위 5% 점수 목록
        List<Integer> topScores = scores.stream().limit(threshold).toList();
        
        // 현재 점수가 상위 5% 내에 있는지 확인
        return !topScores.isEmpty() && completedAttempt.getScore() >= topScores.get(topScores.size() - 1);
    }

    /**
     * 퀴즈 완료 이벤트를 발행합니다.
     */
    private void publishQuizCompletedEvent(QuizAttempt completedAttempt, DifficultyLevel difficultyLevel) {
        eventPublisher.publish(new QuizCompletedEvent(
                completedAttempt,
                difficultyLevel
        ));
    }
}