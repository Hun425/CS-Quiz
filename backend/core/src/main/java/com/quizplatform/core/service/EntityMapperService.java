package com.quizplatform.core.service;

import com.quizplatform.modules.battle.domain.BattleAnswer;
import com.quizplatform.modules.battle.domain.BattleParticipant;
import com.quizplatform.modules.battle.domain.BattleRoom;
import com.quizplatform.modules.quiz.domain.QuestionAttempt;
import com.quizplatform.modules.quiz.domain.Quiz;
import com.quizplatform.modules.quiz.domain.QuizAttempt;
import com.quizplatform.modules.user.domain.User;
import com.quizplatform.modules.battle.dto.BattleEndResponse;
import com.quizplatform.modules.battle.dto.BattleResult;
import com.quizplatform.modules.battle.dto.BattleRoomResponse;
import com.quizplatform.modules.quiz.dto.QuestionAttemptDto;
import com.quizplatform.modules.quiz.dto.QuizDetailResponse;
import com.quizplatform.modules.quiz.dto.QuizResponse;
import com.quizplatform.modules.quiz.dto.QuizResultResponse;
import com.quizplatform.modules.quiz.dto.QuizSummaryResponse;
import com.quizplatform.modules.user.dto.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 엔티티 객체를 DTO 객체로 변환하는 로직을 담당하는 서비스입니다.
 * 지연 로딩된 연관 관계를 초기화하고 필요한 데이터 형식으로 변환합니다.
 *
 * @author 채기훈
 * @since JDK 17
 */
@Service
@Slf4j
public class EntityMapperService {

    /**
     * Quiz 엔티티를 상세 정보 DTO (QuizDetailResponse)로 변환합니다.
     * 변환 전 필요한 연관 관계(질문, 태그, 생성자)를 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizDetailResponse DTO
     */
    @Transactional(readOnly = true)
    public QuizDetailResponse mapToQuizDetailResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        return QuizDetailResponse.from(quiz);
    }

    /**
     * Quiz 엔티티를 요약 정보 DTO (QuizSummaryResponse)로 변환합니다.
     * 요약 정보에 필요한 최소한의 연관 관계(태그, 생성자)만 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizSummaryResponse DTO
     */
    @Transactional(readOnly = true)
    public QuizSummaryResponse mapToQuizSummaryResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화 (요약 정보에만 필요한 것들)
        quiz.getTags().size(); // 태그 컬렉션 크기 접근으로 초기화
        if (quiz.getCreator() != null) {
            quiz.getCreator().getUsername(); // 생성자 이름 접근으로 초기화
        }
        return QuizSummaryResponse.from(quiz);
    }

    /**
     * Quiz 엔티티 리스트를 요약 정보 DTO (QuizSummaryResponse) 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToQuizSummaryResponse 메서드를 호출합니다.
     *
     * @param quizzes 변환할 Quiz 엔티티 리스트
     * @return 변환된 QuizSummaryResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<QuizSummaryResponse> mapToQuizSummaryResponseList(List<Quiz> quizzes) {
        return quizzes.stream()
                .map(this::mapToQuizSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * BattleRoom 엔티티를 BattleRoomResponse DTO로 변환합니다.
     * 변환 전 필요한 연관 관계(참가자 정보, 퀴즈 정보)를 초기화합니다.
     *
     * @param battleRoom 변환할 BattleRoom 엔티티
     * @return 변환된 BattleRoomResponse DTO
     */
    @Transactional(readOnly = true)
    public BattleRoomResponse mapToBattleRoomResponse(BattleRoom battleRoom) {
        // 필요한 연관 관계 초기화
        battleRoom.getParticipants().forEach(participant -> {
            participant.getUser().getUsername(); // 참가자 사용자 정보 초기화
            participant.getUser().getProfileImage();
            participant.getUser().getLevel();
        });
        battleRoom.getQuiz().getTitle(); // 배틀 퀴즈 정보 초기화
        battleRoom.getQuiz().getTimeLimit();
        battleRoom.getQuiz().getQuestions().size();
        
        // 방 생성자 ID 확인
        Long creatorId = battleRoom.getCreatorId();

        return BattleRoomResponse.from(battleRoom);
    }

    /**
     * BattleRoom 엔티티 리스트를 BattleRoomResponse DTO 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToBattleRoomResponse 메서드를 호출합니다.
     *
     * @param battleRooms 변환할 BattleRoom 엔티티 리스트
     * @return 변환된 BattleRoomResponse DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<BattleRoomResponse> mapToBattleRoomResponseList(List<BattleRoom> battleRooms) {
        return battleRooms.stream()
                .map(this::mapToBattleRoomResponse)
                .collect(Collectors.toList());
    }

    /**
     * User 엔티티를 사용자 프로필 정보 DTO (UserProfileDto)로 변환합니다.
     * 날짜 정보는 ISO 8601 형식의 문자열로 포맷합니다.
     *
     * @param user 변환할 User 엔티티
     * @return 변환된 UserProfileDto
     */
    @Transactional(readOnly = true)
    public UserProfileDto mapToUserProfileDto(User user) {
        // 사용자 기본 정보를 UserProfileDto로 매핑
        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileImage(),
                user.getLevel(),
                user.getExperience(),
                user.getRequiredExperience(),
                user.getTotalPoints(),
                formatDateTime(user.getCreatedAt()), // 가입일 포맷팅
                user.getLastLogin() != null ? formatDateTime(user.getLastLogin()) : null // 마지막 로그인 포맷팅
        );
    }

    /**
     * BattleResult 객체(배틀 결과 정보)를 BattleEndResponse DTO (배틀 종료 응답)로 변환합니다.
     * 각 참가자의 최종 점수, 정답 수, 평균 답변 시간, 획득 경험치 등을 계산하고 포함합니다.
     *
     * @param result 변환할 BattleResult 객체
     * @return 변환된 BattleEndResponse DTO
     */
    @Transactional(readOnly = true)
    public BattleEndResponse mapToBattleEndResponse(BattleResult result) {
        // BattleResult로부터 BattleEndResponse 생성
        List<BattleEndResponse.ParticipantResult> participantResults = result.getParticipants().stream()
                .map(participant -> {
                    // 각 참가자의 답변 목록 초기화 (지연 로딩)
                    participant.getAnswers().size();

                    // 각 질문 ID에 대한 정답 여부 맵 생성
                    Map<Long, Boolean> questionResults = participant.getAnswers().stream()
                            .collect(Collectors.toMap(
                                    answer -> answer.getQuestion().getId(), // Key: 질문 ID
                                    answer -> answer.isCorrect()        // Value: 정답 여부
                            ));

                    return BattleEndResponse.ParticipantResult.builder()
                            .userId(participant.getUser().getId())
                            .username(participant.getUser().getUsername())
                            .finalScore(participant.getCurrentScore())
                            .correctAnswers(participant.getCorrectAnswersCount())
                            .averageTimeSeconds(calculateAverageTime(participant)) // 평균 답변 시간 계산
                            .experienceGained(calculateExperienceGained(participant, result)) // 획득 경험치 계산 (추정치)
                            .isWinner(participant.equals(result.getWinner())) // 승자 여부
                            .questionResults(questionResults) // 질문별 정답 여부
                            .build();
                })
                .collect(Collectors.toList());

        return BattleEndResponse.builder()
                .roomId(result.getRoomId())
                .results(participantResults) // 참가자별 결과 리스트
                .totalQuestions(result.getTotalQuestions())
                .timeTakenSeconds(result.getTotalTimeSeconds())
                .endTime(result.getEndTime())
                .build();
    }

    /**
     * QuestionAttempt 엔티티 리스트를 QuestionAttemptDto 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToQuestionAttemptDto 메서드를 호출합니다.
     *
     * @param questionAttempts 변환할 QuestionAttempt 엔티티 리스트
     * @return 변환된 QuestionAttemptDto 리스트
     */
    @Transactional(readOnly = true)
    public List<QuestionAttemptDto> mapToQuestionAttemptDtoList(List<QuestionAttempt> questionAttempts) {
        return questionAttempts.stream()
                .map(this::mapToQuestionAttemptDto)
                .collect(Collectors.toList());
    }

    /**
     * QuestionAttempt 엔티티를 QuestionAttemptDto로 변환합니다.
     * 변환 전 필요한 연관 관계(질문 정보)를 초기화합니다.
     *
     * @param attempt 변환할 QuestionAttempt 엔티티
     * @return 변환된 QuestionAttemptDto
     */
    @Transactional(readOnly = true)
    public QuestionAttemptDto mapToQuestionAttemptDto(QuestionAttempt attempt) {
        // 필요한 연관 관계 초기화 (질문 텍스트, 설명 등)
        attempt.getQuestion().getQuestionText();
        attempt.getQuestion().getExplanation();

        return QuestionAttemptDto.from(attempt);
    }

    /**
     * QuizAttempt 엔티티와 획득 경험치를 QuizResultResponse DTO (퀴즈 결과 응답)로 변환합니다.
     * 퀴즈 정보, 총 점수, 정답 수, 문제별 결과, 획득 경험치 등을 포함합니다.
     *
     * @param quizAttempt      변환할 QuizAttempt 엔티티
     * @param experienceGained 해당 퀴즈 시도에서 획득한 경험치
     * @return 변환된 QuizResultResponse DTO
     */
    @Transactional(readOnly = true)
    public QuizResultResponse mapToQuizResultResponse(QuizAttempt quizAttempt, int experienceGained) {
        // QuizResultResponse 생성에 필요한 초기화 작업
        Quiz quiz = quizAttempt.getQuiz();
        quiz.getQuestions().size(); // 퀴즈의 질문 목록 초기화

        // 문제별 결과(QuestionResultDto) 리스트 생성
        List<QuizResultResponse.QuestionResultDto> questionResults = new ArrayList<>();
        quizAttempt.getQuestionAttempts().forEach(qa -> {
            // 각 문제 시도(QuestionAttempt)에 연결된 질문 정보 초기화
            qa.getQuestion().getQuestionText();
            qa.getQuestion().getCorrectAnswer();
            qa.getQuestion().getExplanation();

            questionResults.add(
                    QuizResultResponse.QuestionResultDto.builder()
                            .id(qa.getQuestion().getId())
                            .questionText(qa.getQuestion().getQuestionText())
                            .yourAnswer(qa.getUserAnswer()) // 사용자가 제출한 답
                            .correctAnswer(qa.getQuestion().getCorrectAnswer()) // 실제 정답
                            .isCorrect(qa.isCorrect()) // 정답 여부
                            .explanation(qa.getQuestion().getExplanation()) // 해설
                            .points(qa.getQuestion().getPoints()) // 문제 배점
                            .build()
            );
        });

        // 해당 퀴즈의 총 가능 점수 계산
        int totalPossibleScore = quiz.getQuestions().stream()
                .mapToInt(q -> q.getPoints())
                .sum();

        // 최종 결과 응답 생성
        return QuizResultResponse.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .totalQuestions(quiz.getQuestions().size())
                .correctAnswers((int) quizAttempt.getQuestionAttempts().stream()
                        .filter(QuestionAttempt::isCorrect)
                        .count())
                .score(quizAttempt.getScore())
                .totalPossibleScore(totalPossibleScore)
                .timeTaken(quizAttempt.getTimeTaken())
                .completedAt(quizAttempt.getEndTime())
                .experienceGained(experienceGained)
                .newTotalExperience(quizAttempt.getUser().getExperience()) // 퀴즈 완료 후 사용자의 총 경험치
                .questions(questionResults) // 문제별 결과 상세 목록
                .build();
    }

    // --- 헬퍼 메서드 ---

    /**
     * Quiz 엔티티의 지연 로딩된 연관 관계(질문, 태그, 생성자)를 초기화합니다.
     * DTO 변환 전 호출하여 LazyInitializationException을 방지합니다. (내부 헬퍼 메서드)
     *
     * @param quiz 초기화할 Quiz 엔티티
     */
    private void initializeQuizAssociations(Quiz quiz) {
        quiz.getQuestions().size(); // 질문 컬렉션 초기화
        quiz.getTags().size(); // 태그 컬렉션 초기화
        if (quiz.getCreator() != null) {
            quiz.getCreator().getUsername(); // 생성자 정보 초기화
        }
    }

    /**
     * ZonedDateTime 객체를 ISO_OFFSET_DATE_TIME 형식의 문자열로 변환합니다.
     * null 입력 시 null을 반환합니다. (내부 헬퍼 메서드)
     *
     * @param dateTime 변환할 ZonedDateTime 객체
     * @return 변환된 날짜/시간 문자열 또는 null
     */
    private String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) return null;
        // ISO 8601 형식 (예: '2011-12-03T10:15:30+01:00')
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 배틀 참가자의 평균 답변 시간을 초 단위로 계산합니다.
     * 답변이 없을 경우 0을 반환합니다. (내부 헬퍼 메서드)
     *
     * @param participant 계산할 BattleParticipant 객체
     * @return 평균 답변 시간 (초)
     */
    private int calculateAverageTime(BattleParticipant participant) {
        List<BattleAnswer> answers = participant.getAnswers();
        if (answers.isEmpty()) {
            return 0;
        }
        // 모든 답변의 소요 시간을 합산
        int totalTime = answers.stream()
                .mapToInt(answer -> answer.getTimeTaken()) // 각 답변의 timeTaken 값 사용
                .sum();
        // 평균 계산 (총 시간 / 답변 수)
        return totalTime / answers.size();
    }

    /**
     * 배틀 결과에 따라 참가자가 얻을 것으로 예상되는 경험치를 계산합니다.
     * 점수, 승리 여부, 정답률에 기반한 간단한 추정치입니다.
     * 실제 경험치 부여는 LevelingService에서 처리될 수 있습니다. (내부 헬퍼 메서드)
     *
     * @param participant 경험치를 계산할 BattleParticipant
     * @param result      해당 배틀의 결과 정보 (BattleResult)
     * @return 추정된 획득 경험치
     */
    private int calculateExperienceGained(BattleParticipant participant, BattleResult result) {
        // 기본 경험치 (점수의 10%, 최소 0점)
        int baseExp = Math.max(0, participant.getCurrentScore() / 10);

        // 승리 보너스 (승자일 경우 1.5배)
        if (participant.equals(result.getWinner())) {
            baseExp = (int) (baseExp * 1.5);
        }

        // 정답률 보너스 (80% 이상 정답 시 추가 50점)
        if (result.getTotalQuestions() > 0) { // 0으로 나누는 것 방지
            double correctRate = (double) participant.getCorrectAnswersCount() / result.getTotalQuestions();
            if (correctRate >= 0.8) {
                baseExp += 50;
            }
        }

        return baseExp;
    }

    /**
     * Quiz 엔티티를 QuizResponse DTO로 변환합니다.
     * 변환 전 필요한 연관 관계를 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizResponse DTO
     */
    @Transactional(readOnly = true)
    public QuizResponse mapToQuizResponse(Quiz quiz) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        return QuizResponse.from(quiz);
    }

    /**
     * Quiz 엔티티로부터 QuizResponse를 생성하며, 퀴즈 시도 ID(quizAttemptId)를 추가로 설정합니다.
     * 퀴즈를 시작하거나 이어할 때 사용될 수 있습니다.
     *
     * @param quiz          변환할 Quiz 엔티티
     * @param quizAttemptId 설정할 퀴즈 시도 ID
     * @return 퀴즈 시도 ID가 포함된 QuizResponse DTO
     */
    @Transactional(readOnly = true)
    public QuizResponse mapToQuizResponseWithAttemptId(Quiz quiz, Long quizAttemptId) {
        // 필요한 연관 관계 초기화
        initializeQuizAssociations(quiz);
        // QuizResponse 생성 후 quizAttemptId 설정
        return QuizResponse.from(quiz).withQuizAttemptId(quizAttemptId);
    }
}