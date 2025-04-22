package com.quizplatform.quiz.application.service;

import com.quizplatform.quiz.application.port.in.CreateQuizUseCase;
import com.quizplatform.quiz.application.port.in.UpdateQuizUseCase;
import com.quizplatform.quiz.application.port.in.command.CreateQuizCommand;
import com.quizplatform.quiz.application.port.in.command.CreateQuestionCommand;
import com.quizplatform.quiz.application.port.in.command.UpdateQuizCommand;
import com.quizplatform.quiz.application.port.out.*;
import com.quizplatform.quiz.domain.event.DailyQuizCreatedEvent;
import com.quizplatform.quiz.domain.event.QuizCreatedEvent;
import com.quizplatform.quiz.domain.model.Question;
import com.quizplatform.quiz.domain.model.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 퀴즈 명령 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizCommandService implements CreateQuizUseCase, UpdateQuizUseCase {
    private final LoadQuizPort loadQuizPort;
    private final SaveQuizPort saveQuizPort;
    private final SaveQuestionPort saveQuestionPort;
    private final LoadTagPort loadTagPort;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    public Quiz createQuiz(CreateQuizCommand command) {
        // 태그 유효성 검증
        validateTags(command.getTagIds());

        // 퀴즈 생성
        Quiz quiz = Quiz.builder()
                .creatorId(command.getCreatorId())
                .title(command.getTitle())
                .description(command.getDescription())
                .quizType(command.getQuizType())
                .difficultyLevel(command.getDifficultyLevel())
                .timeLimit(command.getTimeLimit())
                .isPublic(command.isPublic())
                .tagIds(command.getTagIds())
                .build();

        // 문제 추가
        Set<Question> questions = createQuestionsFromCommands(command.getQuestions());
        quiz.setQuestions(questions);

        // 퀴즈 저장
        Quiz savedQuiz = saveQuizPort.save(quiz);

        // 이벤트 발행
        eventPublisher.publish(new QuizCreatedEvent(savedQuiz));

        return savedQuiz;
    }

    @Override
    public Quiz createDailyQuiz() {
        // 최근 7일간의 데일리 퀴즈에서 사용된 태그 ID와 난이도를 조회
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Optional<Quiz> recentDailyQuizOptional = loadQuizPort.findCurrentDailyQuiz(sevenDaysAgo);

        Set<Long> excludeTagIds = new HashSet<>();
        Set<String> excludeDifficultyLevels = new HashSet<>();

        if (recentDailyQuizOptional.isPresent()) {
            Quiz recentDailyQuiz = recentDailyQuizOptional.get();
            excludeTagIds.addAll(recentDailyQuiz.getTagIds());
            excludeDifficultyLevels.add(recentDailyQuiz.getDifficultyLevel().name());
        }

        // 적절한 퀴즈 선택
        Optional<Quiz> candidateQuizOptional = loadQuizPort.findQuizForDaily(excludeTagIds, excludeDifficultyLevels);
        
        if (candidateQuizOptional.isEmpty()) {
            throw new IllegalStateException("적합한 데일리 퀴즈 후보를 찾을 수 없습니다.");
        }

        Quiz candidateQuiz = candidateQuizOptional.get();

        // 데일리 퀴즈 복제 및 설정
        Quiz dailyQuiz = candidateQuiz.createDailyCopy();
        dailyQuiz.setValidUntil(LocalDateTime.now().plusDays(1));

        // 데일리 퀴즈 저장
        Quiz savedDailyQuiz = saveQuizPort.save(dailyQuiz);

        // 이벤트 발행
        eventPublisher.publish(new DailyQuizCreatedEvent(
                savedDailyQuiz.getId(),
                savedDailyQuiz.getTitle(),
                savedDailyQuiz.getTagIds(),
                savedDailyQuiz.getQuestionCount()));

        return savedDailyQuiz;
    }

    @Override
    public Quiz updateQuiz(Long quizId, UpdateQuizCommand command) {
        // 퀴즈 조회
        Optional<Quiz> quizOptional = loadQuizPort.findByIdWithQuestions(quizId);
        if (quizOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + quizId);
        }
        
        Quiz quiz = quizOptional.get();
        
        // 권한 확인
        if (!Objects.equals(quiz.getCreatorId(), command.getCreatorId())) {
            throw new IllegalStateException("퀴즈를 수정할 권한이 없습니다.");
        }
        
        // 태그 유효성 검증
        validateTags(command.getTagIds());
        
        // 퀴즈 정보 업데이트
        quiz.update(
                command.getTitle(),
                command.getDescription(),
                command.getDifficultyLevel(),
                command.getTimeLimit()
        );
        quiz.updateTags(command.getTagIds());
        
        // 문제 업데이트
        saveQuestionPort.deleteByQuizId(quizId);
        Set<Question> newQuestions = createQuestionsFromCommands(command.getQuestions());
        quiz.setQuestions(newQuestions);
        
        // 퀴즈 저장
        return saveQuizPort.update(quiz);
    }

    @Override
    public Quiz setQuizPublic(Long quizId, boolean isPublic) {
        Optional<Quiz> quizOptional = loadQuizPort.findById(quizId);
        if (quizOptional.isEmpty()) {
            throw new NoSuchElementException("퀴즈를 찾을 수 없습니다: " + quizId);
        }
        
        return saveQuizPort.setPublic(quizId, isPublic);
    }

    /**
     * 태그 ID 목록의 유효성을 검증합니다.
     */
    private void validateTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        List<Tag> tags = loadTagPort.findByIds(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 태그가 포함되어 있습니다.");
        }
    }

    /**
     * 문제 생성 명령을 문제 객체로 변환합니다.
     */
    private Set<Question> createQuestionsFromCommands(List<CreateQuestionCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return Collections.emptySet();
        }
        
        return commands.stream()
                .map(this::createQuestionFromCommand)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 문제 생성 명령을 문제 객체로 변환합니다.
     */
    private Question createQuestionFromCommand(CreateQuestionCommand command) {
        List<Question.Option> options = command.getOptions().stream()
                .map(opt -> Question.Option.builder()
                        .key(opt.getKey())
                        .value(opt.getValue())
                        .build())
                .collect(Collectors.toList());
        
        return Question.builder()
                .questionType(command.getQuestionType())
                .questionText(command.getQuestionText())
                .codeSnippet(command.getCodeSnippet())
                .options(options)
                .correctAnswer(command.getCorrectAnswer())
                .explanation(command.getExplanation())
                .difficultyLevel(command.getDifficultyLevel())
                .points(command.getPoints())
                .timeLimitSeconds(command.getTimeLimitSeconds())
                .build();
    }
}