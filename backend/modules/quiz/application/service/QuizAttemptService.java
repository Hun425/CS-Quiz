package application.service;

import application.port.in.FinishQuizAttemptUseCase;
import application.port.in.GetQuizAttemptUseCase;
import application.port.in.StartQuizAttemptUseCase;
import application.port.in.SubmitQuizAnswerUseCase;
import application.port.in.command.FinishQuizAttemptCommand;
import application.port.in.command.StartQuizAttemptCommand;
import application.port.in.command.SubmitAnswerCommand;
import domain.event.HighScoreAchievedEvent;
import domain.event.QuizCompletedEvent;
import domain.model.Question;
import domain.model.QuizAttempt;
import domain.model.Quiz;
import domain.model.QuizType;
import domain.model.QuestionAttempt;
import application.port.out.LoadQuizPort;
import application.port.out.SaveQuizPort;
import application.port.out.LoadQuestionPort;
import application.port.out.LoadQuizAttemptPort;
import application.port.out.SaveQuizAttemptPort;
import application.port.out.SaveQuestionAttemptPort;
import application.port.out.DomainEventPublisherPort;
import domain.model.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * 퀴즈 시도 시작, 답변 제출, 완료, 조회 등 퀴즈 참여 관련 로직을 처리하는 서비스 클래스
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
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
    public Page<QuizAttempt> getUserQuizAttempts(Long userId, Long quizId, Pageable pageable) {
        return loadQuizAttemptPort.findByUserIdAndQuizId(userId, quizId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttempt> getAllUserQuizAttempts(Long userId, Pageable pageable) {
        return loadQuizAttemptPort.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttempt> getQuizAttemptsByUserId(Long userId, Pageable pageable) {
        return loadQuizAttemptPort.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getRecentQuizAttemptsByUserId(Long userId, int limit) {
        // 현재 시간 기준 최근 30일 데이터 조회
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        return loadQuizAttemptPort.findRecentAttempts(userId, since, limit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public QuizAttempt getBestQuizAttempt(Long userId, Long quizId) {
        // 사용자의 특정 퀴즈 시도 목록 조회
        Page<QuizAttempt> attempts = loadQuizAttemptPort.findByUserIdAndQuizId(userId, quizId, Pageable.unpaged());
        
        // 최고 점수의 시도 찾기
        return attempts.getContent().stream()
                .filter(QuizAttempt::isCompleted)
                .max(Comparator.comparing(QuizAttempt::getScore))
                .orElseThrow(() -> new NoSuchElementException("해당 사용자의 퀴즈 시도를 찾을 수 없습니다."));
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
        // 사용자의 이전 시도들 조회 (Pageable 객체 없이 내부 구현에서 모든 결과 반환)
        Page<QuizAttempt> previousAttemptPage = loadQuizAttemptPort.findByUserIdAndQuizId(
                completedAttempt.getUserId(),
                completedAttempt.getQuizId(),
                Pageable.unpaged() // 모든 결과 조회를 위해 unpaged 사용
        );
        List<QuizAttempt> previousAttempts = previousAttemptPage.getContent();
        
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
            // 전역 최고 점수인지 확인
            boolean isGlobalTopScore = isGlobalTopScore(completedAttempt);
            
            // 이벤트 발행
            eventPublisher.publish(new HighScoreAchievedEvent(
                    completedAttempt.getQuizId(),
                    completedAttempt.getUserId(),
                    completedAttempt.getScore(),
                    previousBestScore,
                    isGlobalTopScore
            ));
            
            // 최고 점수 뱃지 부여 등 추가 로직
        }
    }

    /**
     * 전역 최고 점수인지 확인합니다. (선택적 구현)
     *
     * @param completedAttempt 완료된 퀴즈 시도
     * @return 전역 최고 점수 여부
     */
    private boolean isGlobalTopScore(QuizAttempt completedAttempt) {
        // 구현은 필요에 따라 추가
        return false;
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