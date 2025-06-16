package com.quizplatform.quiz.application.service;

import com.quizplatform.common.exception.BusinessException;
import com.quizplatform.common.exception.ErrorCode;
import com.quizplatform.quiz.application.dto.QuizCreateRequest;
import com.quizplatform.quiz.application.dto.QuizResponse;
import com.quizplatform.quiz.application.dto.QuizUpdateRequest;
import com.quizplatform.quiz.domain.model.Quiz;
import com.quizplatform.quiz.domain.model.Tag;
import com.quizplatform.quiz.domain.model.User;
import com.quizplatform.quiz.domain.service.QuizService;
import com.quizplatform.quiz.domain.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private final TagService tagService;
    
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
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
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
    
    // ===== Tag 기반 퀴즈 조회 =====
    
    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByTag(Long tagId, Pageable pageable) {
        log.debug("Getting quizzes by tag: tagId={}", tagId);
        
        // 태그 존재 여부 확인
        Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        if (!tag.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비활성화된 태그입니다: " + tagId);
        }
        
        // QuizService를 통해 태그별 퀴즈 조회
        Page<Quiz> quizzes = quizService.getQuizzesByTag(tagId, pageable);
        return quizzes.map(this::convertToQuizResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getQuizzesByTags(List<Long> tagIds, String operator, Pageable pageable) {
        log.debug("Getting quizzes by tags: tagIds={}, operator={}", tagIds, operator);
        
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "태그 ID 목록이 비어있습니다");
        }
        
        // 모든 태그 존재 여부 및 활성화 상태 확인
        List<Tag> tags = tagIds.stream()
                .map(tagId -> tagService.getTagById(tagId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                               "태그를 찾을 수 없습니다: " + tagId)))
                .filter(tag -> {
                    if (!tag.isActive()) {
                        log.warn("Inactive tag found: {}", tag.getId());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        if (tags.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "활성화된 태그가 없습니다");
        }
        
        // 유효한 operator 검증
        if (!"AND".equalsIgnoreCase(operator) && !"OR".equalsIgnoreCase(operator)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                      "올바르지 않은 논리 연산자입니다. AND 또는 OR을 사용하세요");
        }
        
        // QuizService를 통해 다중 태그 조건 퀴즈 조회
        List<Long> validTagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
        Page<Quiz> quizzes = quizService.getQuizzesByTags(validTagIds, operator, pageable);
        return quizzes.map(this::convertToQuizResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> advancedSearchQuizzes(String keyword, List<Long> tagIds, 
                                                   String category, Integer difficulty, Pageable pageable) {
        log.debug("Advanced search: keyword={}, tagIds={}, category={}, difficulty={}", 
                 keyword, tagIds, category, difficulty);
        
        // 태그 ID가 제공된 경우 유효성 검증
        List<Long> validTagIds = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            validTagIds = tagIds.stream()
                    .filter(tagId -> tagService.getTagById(tagId)
                            .filter(Tag::isActive)
                            .isPresent())
                    .collect(Collectors.toList());
        }
        
        // QuizService를 통해 고급 검색 수행
        Page<Quiz> quizzes = quizService.advancedSearchQuizzes(keyword, validTagIds, category, difficulty, pageable);
        return quizzes.map(this::convertToQuizResponse);
    }
    
    // ===== Quiz-Tag 관계 관리 =====
    
    @Override
    @Transactional
    public QuizResponse updateQuizTags(Long quizId, List<Long> tagIds) {
        log.info("Updating quiz tags: quizId={}, tagIds={}", quizId, tagIds);
        
        // 퀴즈 존재 여부 확인
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "퀴즈를 찾을 수 없습니다: " + quizId));
        
        // 태그 ID 유효성 검증
        if (tagIds != null && !tagIds.isEmpty()) {
            if (tagIds.size() > Tag.MAX_TAGS_PER_QUIZ) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                        String.format("퀴즈당 최대 %d개의 태그만 할당할 수 있습니다", Tag.MAX_TAGS_PER_QUIZ));
            }
            
            // 모든 태그 존재 여부 및 활성화 상태 확인
            tagIds.forEach(tagId -> {
                Tag tag = tagService.getTagById(tagId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                               "태그를 찾을 수 없습니다: " + tagId));
                if (!tag.isActive()) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                              "비활성화된 태그는 할당할 수 없습니다: " + tagId);
                }
            });
        }
        
        // QuizService를 통해 태그 업데이트
        Quiz updatedQuiz = quizService.setQuizTags(quizId, tagIds != null ? tagIds : List.of());
        return convertToQuizResponse(updatedQuiz);
    }
    
    @Override
    @Transactional
    public QuizResponse addTagToQuiz(Long quizId, Long tagId) {
        log.info("Adding tag to quiz: quizId={}, tagId={}", quizId, tagId);
        
        // 퀴즈와 태그 존재 여부 확인
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "퀴즈를 찾을 수 없습니다: " + quizId));
        
        Tag tag = tagService.getTagById(tagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "태그를 찾을 수 없습니다: " + tagId));
        
        if (!tag.isActive()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                      "비활성화된 태그는 할당할 수 없습니다: " + tagId);
        }
        
        // 퀴즈에 태그 추가 가능 여부 확인
        if (!tagService.canAddTagToQuiz(quizId, tagId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                      "태그를 퀴즈에 추가할 수 없습니다. 최대 태그 수를 초과했거나 이미 할당된 태그입니다");
        }
        
        // QuizService를 통해 태그 추가
        Quiz updatedQuiz = quizService.addTagToQuiz(quizId, tagId);
        return convertToQuizResponse(updatedQuiz);
    }
    
    @Override
    @Transactional
    public QuizResponse removeTagFromQuiz(Long quizId, Long tagId) {
        log.info("Removing tag from quiz: quizId={}, tagId={}", quizId, tagId);
        
        // 퀴즈 존재 여부 확인
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, 
                                                       "퀴즈를 찾을 수 없습니다: " + quizId));
        
        // QuizService를 통해 태그 제거
        Quiz updatedQuiz = quizService.removeTagFromQuiz(quizId, tagId);
        return convertToQuizResponse(updatedQuiz);
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