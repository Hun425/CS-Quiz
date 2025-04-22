package domain.model;

import lombok.Getter;

/**
 * 퀴즈와 문제의 난이도를 정의하는 열거형
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
@Getter
public enum DifficultyLevel {
    /**
     * 초급 난이도 - 기본 경험치 50
     */
    BEGINNER("초급", 50),
    
    /**
     * 쉬운 난이도 - 기본 경험치 75
     */
    EASY("쉬움", 75),
    
    /**
     * 중급 난이도 - 기본 경험치 100
     */
    INTERMEDIATE("중급", 100),
    
    /**
     * 중간 난이도 - 기본 경험치 100
     */
    MEDIUM("중간", 100),
    
    /**
     * 고급 난이도 - 기본 경험치 150
     */
    ADVANCED("고급", 150),
    
    /**
     * 어려운 난이도 - 기본 경험치 150
     */
    HARD("어려움", 150),
    
    /**
     * 전문가 난이도 - 기본 경험치 200
     */
    EXPERT("전문가", 200);

    /**
     * 난이도별 기본 획득 경험치
     */
    private final int baseExp;
    
    /**
     * 난이도 설명 (사용자에게 표시됨)
     */
    private final String description;

    /**
     * 난이도 생성자
     * 
     * @param description 난이도 설명
     * @param baseExp 기본 획득 경험치
     */
    DifficultyLevel(String description, int baseExp) {
        this.description = description;
        this.baseExp = baseExp;
    }
}