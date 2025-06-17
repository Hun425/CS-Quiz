package com.quizplatform.core.utils;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.question.Question;
import com.quizplatform.core.domain.question.QuestionType;
import com.quizplatform.core.domain.quiz.DifficultyLevel;
import com.quizplatform.core.domain.quiz.QuizType;
import com.quizplatform.core.domain.tag.Tag;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.domain.user.AuthProvider;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.tag.TagRepository;
import com.quizplatform.core.service.quiz.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 퀴즈 더미 데이터 생성 유틸리티
 * 
 * <p>테스트 및 성능 측정을 위한 대량의 더미 퀴즈 데이터를 생성합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizDataGenerator {

    private final QuizRepository quizRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final QuizService quizService;
    
    // 프로그래밍 언어 관련 키워드 모음
    private static final String[] PROGRAMMING_KEYWORDS = {
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Kotlin", "Swift", "Go", "Rust",
        "Spring", "Django", "React", "Angular", "Vue", "Node.js", "Express", "FastAPI", "Flutter", "Dart",
        "알고리즘", "자료구조", "데이터베이스", "네트워크", "운영체제", "컴파일러", "인공지능", "머신러닝", "딥러닝", "클라우드"
    };
    
    // 퀴즈 제목 템플릿
    private static final String[] QUIZ_TITLE_TEMPLATES = {
        "%s 기초 개념 퀴즈", 
        "%s 심화 학습 테스트", 
        "%s 마스터하기",
        "%s 개발자를 위한 퀴즈",
        "%s 실무 활용 문제",
        "%s 기술 면접 대비",
        "실전 %s 문제 모음",
        "%s 이해도 테스트",
        "%s 핵심 개념 정리",
        "%s 퀴즈 챌린지"
    };
    
    /**
     * 지정된 수만큼 더미 퀴즈 데이터를 생성합니다.
     *
     * @param count 생성할 퀴즈 수
     * @return 생성된 퀴즈 ID 목록
     */
    @Transactional
    public List<Long> generateDummyQuizzes(int count) {
        log.info("{}개의 더미 퀴즈 데이터 생성 시작", count);
        
        List<Long> generatedQuizIds = new ArrayList<>();
        List<Tag> allTags = ensureTags();
        User admin = ensureAdminUser();
        
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            // 퀴즈 생성
            Quiz quiz = createQuiz(random, admin, allTags);
            quizRepository.save(quiz);
            
            // 기본 질문 추가 (5~10개)
            addQuestionsToQuiz(quiz, random.nextInt(6) + 5);
            
            generatedQuizIds.add(quiz.getId());
            
            if (i % 100 == 0 && i > 0) {
                log.info("{}개의 퀴즈 생성 완료", i);
            }
        }
        
        log.info("총 {}개의 더미 퀴즈 데이터 생성 완료", count);
        return generatedQuizIds;
    }
    
    /**
     * 모든 퀴즈에 대한 캐시를 미리 워밍업합니다.
     */
    @Transactional(readOnly = true)
    public void warmupCache() {
        log.info("캐시 워밍업 시작");
        List<Quiz> allQuizzes = quizRepository.findAll();
        
        for (Quiz quiz : allQuizzes) {
            // 캐시 워밍업을 위해 퀴즈 서비스 메소드 호출
            try {
                quizService.getQuizWithQuestions(quiz.getId());
            } catch (Exception e) {
                log.warn("퀴즈 ID {} 캐싱 워밍업 실패: {}", quiz.getId(), e.getMessage());
            }
        }
        
        log.info("{}개 퀴즈에 대한 캐시 워밍업 완료", allQuizzes.size());
    }
    
    /**
     * 모든 더미 퀴즈 데이터를 삭제합니다.
     */
    @Transactional
    public void clearAllDummyData() {
        log.info("더미 데이터 삭제 시작");
        
        List<Quiz> quizzes = quizRepository.findAll();
        quizRepository.deleteAll(quizzes);
        
        log.info("더미 데이터 삭제 완료");
    }
    
    /**
     * 태그가 존재하는지 확인하고, 없으면 기본 태그를 생성합니다.
     */
    private List<Tag> ensureTags() {
        List<Tag> existingTags = tagRepository.findAll();
        
        if (!existingTags.isEmpty()) {
            return existingTags;
        }
        
        // 기본 태그 생성
        List<Tag> newTags = new ArrayList<>();
        for (String keyword : new String[]{"Java", "Python", "JavaScript", "알고리즘", "데이터베이스"}) {
            Tag tag = Tag.builder()
                    .name(keyword)
                    .description(keyword + " 관련 퀴즈")
                    .build();
            tagRepository.save(tag);
            newTags.add(tag);
        }
        
        return newTags;
    }
    
    /**
     * 관리자 계정이 존재하는지 확인하고, 없으면 생성합니다.
     */
    private User ensureAdminUser() {
        Optional<User> adminUser = userRepository.findByEmail("admin@test.com");
        
        if (adminUser.isPresent()) {
            return adminUser.get();
        }
        
        // Builder 패턴을 사용하여 User 객체 생성
        User admin = User.builder()
                .provider(AuthProvider.TEST)
                .providerId("admin")
                .email("admin@test.com")
                .username("Admin")
                .profileImage("/default-profile.png")
                .build();
                
        userRepository.save(admin);
        
        return admin;
    }
    
    /**
     * 랜덤 퀴즈를 생성합니다.
     */
    private Quiz createQuiz(Random random, User creator, List<Tag> availableTags) {
        String keyword = PROGRAMMING_KEYWORDS[random.nextInt(PROGRAMMING_KEYWORDS.length)];
        String titleTemplate = QUIZ_TITLE_TEMPLATES[random.nextInt(QUIZ_TITLE_TEMPLATES.length)];
        String title = String.format(titleTemplate, keyword);
        
        // Builder 패턴을 사용하여 Quiz 객체 생성
        Quiz quiz = Quiz.builder()
                .creator(creator)
                .title(title)
                .description(keyword + "에 관한 퀴즈입니다. 다양한 난이도의 문제가 포함되어 있습니다.")
                .quizType(QuizType.values()[random.nextInt(QuizType.values().length)])
                .difficultyLevel(DifficultyLevel.values()[random.nextInt(DifficultyLevel.values().length)])
                .timeLimit(random.nextInt(20) + 10) // 10-30분
                .build();
        
        // 태그 추가 (1-3개)
        Set<Tag> quizTags = new HashSet<>();
        int tagCount = random.nextInt(3) + 1;
        for (int i = 0; i < tagCount && i < availableTags.size(); i++) {
            quizTags.add(availableTags.get(random.nextInt(availableTags.size())));
        }
        
        // 태그 업데이트
        quiz.updateTags(quizTags);
        
        return quiz;
    }
    
    /**
     * 퀴즈에 기본 질문들을 추가합니다.
     * 실제 구현에서는 Question 클래스의 구조에 맞게 질문을 생성해야 합니다.
     */
    private void addQuestionsToQuiz(Quiz quiz, int questionCount) {
        // 이 부분은 현재 프로젝트의 Question 클래스 구조를 모르기 때문에
        // 실제 구현에서는 QuestionRepository를 주입받아 실제 Question 객체를 생성하고 저장해야 합니다.
        // 아래는 간단한 예시 코드입니다.
        
        for (int i = 0; i < questionCount; i++) {
            // 예시 코드: 질문 생성 및 저장
            // Question question = Question.builder()
            //     .questionType(QuestionType.MULTIPLE_CHOICE)
            //     .questionText("퀴즈 질문 " + (i+1))
            //     .correctAnswer("정답")
            //     .explanation("해설")
            //     .difficultyLevel(quiz.getDifficultyLevel())
            //     .points(1)
            //     .build();
            // question.setQuiz(quiz);
            // questionRepository.save(question);
        }
    }
} 