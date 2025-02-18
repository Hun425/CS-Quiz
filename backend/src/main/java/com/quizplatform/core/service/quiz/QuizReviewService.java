package com.quizplatform.core.service.quiz;

import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizReview;
import com.quizplatform.core.domain.quiz.QuizReviewComment;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.repository.UserRepository;
import com.quizplatform.core.repository.quiz.QuizRepository;
import com.quizplatform.core.repository.quiz.QuizReviewCommentRepository;
import com.quizplatform.core.repository.quiz.QuizReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizReviewService {
    private final QuizReviewRepository reviewRepository;
    private final QuizReviewCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizReview createReview(ReviewCreateRequest request) {
        User reviewer = userRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new UserNotFoundException("리뷰어를 찾을 수 없습니다."));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new QuizNotFoundException("퀴즈를 찾을 수 없습니다."));

        validateReviewRequest(reviewer, quiz, request);

        QuizReview review = QuizReview.builder()
                .quiz(quiz)
                .reviewer(reviewer)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        return reviewRepository.save(review);
    }

    public QuizReviewComment addComment(CommentCreateRequest request) {
        QuizReview review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new ReviewNotFoundException("리뷰를 찾을 수 없습니다."));

        User commenter = userRepository.findById(request.getCommenterId())
                .orElseThrow(() -> new UserNotFoundException("댓글 작성자를 찾을 수 없습니다."));

        QuizReviewComment comment = QuizReviewComment.builder()
                .parentReview(review)
                .commenter(commenter)
                .content(request.getContent())
                .build();

        return commentRepository.save(comment);
    }

    private void validateReviewRequest(User reviewer, Quiz quiz, ReviewCreateRequest request) {
        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByReviewerAndQuiz(reviewer, quiz)) {
            throw new DuplicateReviewException("이미 리뷰를 작성하셨습니다.");
        }

        // 퀴즈를 풀어본 사용자인지 확인
        if (!quiz.hasAttemptedBy(reviewer)) {
            throw new InvalidReviewException("퀴즈를 풀어본 사용자만 리뷰를 작성할 수 있습니다.");
        }

        // 별점 유효성 검사
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("별점은 1점에서 5점 사이여야 합니다.");
        }
    }
}
