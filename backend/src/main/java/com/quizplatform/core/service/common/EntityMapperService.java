package com.quizplatform.core.service.common;

import com.quizplatform.core.domain.battle.BattleParticipant;
import com.quizplatform.core.domain.battle.BattleRoom;
import com.quizplatform.core.domain.question.QuestionAttempt;
import com.quizplatform.core.domain.quiz.Quiz;
import com.quizplatform.core.domain.quiz.QuizAttempt;
import com.quizplatform.core.domain.user.User;
import com.quizplatform.core.dto.battle.BattleEndResponse;
import com.quizplatform.core.dto.battle.BattleResult;
import com.quizplatform.core.dto.battle.BattleRoomResponse;
import com.quizplatform.core.dto.question.QuestionAttemptDto;
import com.quizplatform.core.dto.quiz.QuizDetailResponse;
import com.quizplatform.core.dto.quiz.QuizResponse;
import com.quizplatform.core.dto.quiz.QuizResultResponse;
import com.quizplatform.core.dto.quiz.QuizSummaryResponse;
import com.quizplatform.core.dto.user.UserProfileDto;

import java.util.List;

/**
 * 엔티티 객체를 DTO 객체로 변환하는 로직을 담당하는 서비스 인터페이스입니다.
 * 지연 로딩된 연관 관계를 초기화하고 필요한 데이터 형식으로 변환합니다.
 *
 * @author 채기훈
 * @since JDK 21 eclipse temurin 21.0.6
 */
public interface EntityMapperService {

    /**
     * Quiz 엔티티를 상세 정보 DTO (QuizDetailResponse)로 변환합니다.
     * 변환 전 필요한 연관 관계(질문, 태그, 생성자)를 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizDetailResponse DTO
     */
    QuizDetailResponse mapToQuizDetailResponse(Quiz quiz);

    /**
     * Quiz 엔티티를 요약 정보 DTO (QuizSummaryResponse)로 변환합니다.
     * 요약 정보에 필요한 최소한의 연관 관계(태그, 생성자)만 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizSummaryResponse DTO
     */
    QuizSummaryResponse mapToQuizSummaryResponse(Quiz quiz);

    /**
     * Quiz 엔티티 리스트를 요약 정보 DTO (QuizSummaryResponse) 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToQuizSummaryResponse 메서드를 호출합니다.
     *
     * @param quizzes 변환할 Quiz 엔티티 리스트
     * @return 변환된 QuizSummaryResponse DTO 리스트
     */
    List<QuizSummaryResponse> mapToQuizSummaryResponseList(List<Quiz> quizzes);

    /**
     * BattleRoom 엔티티를 BattleRoomResponse DTO로 변환합니다.
     * 변환 전 필요한 연관 관계(참가자 정보, 퀴즈 정보)를 초기화합니다.
     *
     * @param battleRoom 변환할 BattleRoom 엔티티
     * @return 변환된 BattleRoomResponse DTO
     */
    BattleRoomResponse mapToBattleRoomResponse(BattleRoom battleRoom);

    /**
     * BattleRoom 엔티티 리스트를 BattleRoomResponse DTO 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToBattleRoomResponse 메서드를 호출합니다.
     *
     * @param battleRooms 변환할 BattleRoom 엔티티 리스트
     * @return 변환된 BattleRoomResponse DTO 리스트
     */
    List<BattleRoomResponse> mapToBattleRoomResponseList(List<BattleRoom> battleRooms);

    /**
     * User 엔티티를 사용자 프로필 정보 DTO (UserProfileDto)로 변환합니다.
     * 날짜 정보는 ISO 8601 형식의 문자열로 포맷합니다.
     *
     * @param user 변환할 User 엔티티
     * @return 변환된 UserProfileDto
     */
    UserProfileDto mapToUserProfileDto(User user);

    /**
     * BattleResult 객체(배틀 결과 정보)를 BattleEndResponse DTO (배틀 종료 응답)로 변환합니다.
     * 각 참가자의 최종 점수, 정답 수, 평균 답변 시간, 획득 경험치 등을 계산하고 포함합니다.
     *
     * @param result 변환할 BattleResult 객체
     * @return 변환된 BattleEndResponse DTO
     */
    BattleEndResponse mapToBattleEndResponse(BattleResult result);

    /**
     * QuestionAttempt 엔티티 리스트를 QuestionAttemptDto 리스트로 변환합니다.
     * 각 엔티티에 대해 mapToQuestionAttemptDto 메서드를 호출합니다.
     *
     * @param questionAttempts 변환할 QuestionAttempt 엔티티 리스트
     * @return 변환된 QuestionAttemptDto 리스트
     */
    List<QuestionAttemptDto> mapToQuestionAttemptDtoList(List<QuestionAttempt> questionAttempts);

    /**
     * QuestionAttempt 엔티티를 QuestionAttemptDto로 변환합니다.
     * 변환 전 필요한 연관 관계(질문 정보)를 초기화합니다.
     *
     * @param attempt 변환할 QuestionAttempt 엔티티
     * @return 변환된 QuestionAttemptDto
     */
    QuestionAttemptDto mapToQuestionAttemptDto(QuestionAttempt attempt);

    /**
     * QuizAttempt 엔티티와 획득 경험치를 QuizResultResponse DTO (퀴즈 결과 응답)로 변환합니다.
     * 퀴즈 정보, 총 점수, 정답 수, 문제별 결과, 획득 경험치 등을 포함합니다.
     *
     * @param quizAttempt      변환할 QuizAttempt 엔티티
     * @param experienceGained 해당 퀴즈 시도에서 획득한 경험치
     * @return 변환된 QuizResultResponse DTO
     */
    QuizResultResponse mapToQuizResultResponse(QuizAttempt quizAttempt, int experienceGained);

    /**
     * Quiz 엔티티를 QuizResponse DTO로 변환합니다.
     * 변환 전 필요한 연관 관계를 초기화합니다.
     *
     * @param quiz 변환할 Quiz 엔티티
     * @return 변환된 QuizResponse DTO
     */
    QuizResponse mapToQuizResponse(Quiz quiz);

    /**
     * Quiz 엔티티로부터 QuizResponse를 생성하며, 퀴즈 시도 ID(quizAttemptId)를 추가로 설정합니다.
     * 퀴즈를 시작하거나 이어할 때 사용될 수 있습니다.
     *
     * @param quiz          변환할 Quiz 엔티티
     * @param quizAttemptId 설정할 퀴즈 시도 ID
     * @return 퀴즈 시도 ID가 포함된 QuizResponse DTO
     */
    QuizResponse mapToQuizResponseWithAttemptId(Quiz quiz, Long quizAttemptId);
}