package com.quizplatform.battle.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 배틀 참가자 엔티티 클래스
 * 
 * <p>배틀에 참여한 사용자 정보와 배틀 내 활동을 관리합니다.
 * 점수, 답변, 준비 상태 등의 정보를 포함합니다.</p>
 */
@Entity
@Table(name = "battle_participants", schema = "battle_schema")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BattleParticipant {

    /**
     * 참가자 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 배틀 방
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_room_id", nullable = false)
    private BattleRoom battleRoom;

    /**
     * 사용자 ID (User 모듈 참조)
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 사용자명 (캐싱)
     */
    @Column(length = 50, nullable = false)
    private String username;

    /**
     * 프로필 이미지 URL (캐싱)
     */
    @Column(name = "profile_image")
    private String profileImage;

    /**
     * 참가자 총점
     */
    @Column(nullable = false)
    private double score = 0.0;

    /**
     * 정답 개수
     */
    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    /**
     * 오답 개수
     */
    @Column(name = "wrong_answers", nullable = false)
    private int wrongAnswers = 0;

    /**
     * 참가자 순위
     */
    private Integer rank;

    /**
     * 준비 상태
     */
    @Column(name = "is_ready", nullable = false)
    private boolean isReady = false;

    /**
     * 배틀 완료 여부
     */
    @Column(name = "is_finished", nullable = false)
    private boolean isFinished = false;

    /**
     * 포기 여부
     */
    @Column(name = "has_forfeited", nullable = false)
    private boolean hasForfeited = false;

    /**
     * 배틀 시작 시간 (참가자 기준)
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 배틀 종료 시간 (참가자 기준)
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 획득 포인트
     */
    @Column(name = "earned_points")
    private Integer earnedPoints = 0;

    /**
     * 획득 경험치
     */
    @Column(name = "earned_experience")
    private Integer earnedExperience = 0;

    /**
     * 참가자 제출 답변 목록
     */
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BattleAnswer> answers = new ArrayList<>();

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 최종 수정 시간
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 배틀 참가자 생성자
     * 
     * @param battleRoom 배틀 방
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param profileImage 프로필 이미지
     */
    @Builder
    public BattleParticipant(BattleRoom battleRoom, Long userId, String username, String profileImage) {
        this.battleRoom = battleRoom;
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
    }

    /**
     * 준비 상태 토글
     * 
     * @return 새 준비 상태
     */
    public boolean toggleReady() {
        this.isReady = !this.isReady;
        return this.isReady;
    }

    /**
     * 배틀 시작 처리
     */
    public void startBattle() {
        this.startTime = LocalDateTime.now();
    }

    /**
     * 배틀 종료 처리
     */
    public void finishBattle() {
        this.isFinished = true;
        this.endTime = LocalDateTime.now();
    }

    /**
     * 배틀 포기 처리
     */
    public void forfeit() {
        this.hasForfeited = true;
        this.finishBattle();
    }

    /**
     * 답변 추가
     * 
     * @param answer 새 답변
     */
    public void addAnswer(BattleAnswer answer) {
        this.answers.add(answer);
        
        if (answer.isCorrect()) {
            this.correctAnswers++;
            this.score += answer.getScore();
        } else {
            this.wrongAnswers++;
        }
    }

    /**
     * 특정 문제에 대한 답변 조회
     * 
     * @param questionIndex 문제 인덱스
     * @return 해당 문제의 답변 (없으면 빈 Optional)
     */
    public Optional<BattleAnswer> getAnswerForQuestion(int questionIndex) {
        return this.answers.stream()
                .filter(answer -> answer.getQuestionIndex() == questionIndex)
                .findFirst();
    }

    /**
     * 현재 문제에 답변했는지 확인
     * 
     * @param questionIndex 확인할 문제 인덱스
     * @return 답변했으면 true, 아니면 false
     */
    public boolean hasAnsweredCurrentQuestion(int questionIndex) {
        return getAnswerForQuestion(questionIndex).isPresent();
    }

    /**
     * 획득 포인트 설정
     * 
     * @param points 획득 포인트
     */
    public void setEarnedPoints(int points) {
        this.earnedPoints = points;
    }

    /**
     * 획득 경험치 설정
     * 
     * @param experience 획득 경험치
     */
    public void setEarnedExperience(int experience) {
        this.earnedExperience = experience;
    }

    /**
     * 순위 설정
     * 
     * @param rank 새 순위
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * 총 시도한 문제 수 조회
     * 
     * @return 답변한 문제 수
     */
    public int getTotalAnsweredQuestions() {
        return this.correctAnswers + this.wrongAnswers;
    }

    /**
     * 정답률 계산
     * 
     * @return 정답률 (0.0 ~ 1.0)
     */
    public double getAccuracyRate() {
        int total = getTotalAnsweredQuestions();
        if (total == 0) {
            return 0.0;
        }
        return (double) this.correctAnswers / total;
    }

    /**
     * 포기 여부 확인
     * 
     * @return 포기했으면 true, 아니면 false
     */
    public boolean hasForfeited() {
        return this.hasForfeited;
    }
} 