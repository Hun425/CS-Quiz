package domain.model;

/**
 * 퀴즈 유형을 정의하는 열거형
 *
 * @author 채기훈
 * @since JDK 17.0.2 Eclipse Temurin
 */
public enum QuizType {
    /**
     * 일반 퀴즈 - 기본 퀴즈 유형
     */
    REGULAR("일반 퀴즈"),
    
    /**
     * 데일리 퀴즈 - 매일 갱신되는 오늘의 퀴즈
     */
    DAILY("데일리 퀴즈"),
    
    /**
     * 위클리 퀴즈 - 주간 단위로 갱신되는 퀴즈
     */
    WEEKLY("위클리 퀴즈"),
    
    /**
     * 스페셜 퀴즈 - 특별 이벤트 등에 사용되는 퀴즈
     */
    SPECIAL("스페셜 퀴즈"),
    
    /**
     * 배틀 퀴즈 - 사용자 간 대결에 사용되는 퀴즈
     */
    BATTLE("배틀 퀴즈"),
    
    /**
     * 연습 퀴즈 - 연습용 퀴즈
     */
    PRACTICE("연습 퀴즈"),
    
    /**
     * 평가 퀴즈 - 평가용 퀴즈
     */
    ASSESSMENT("평가 퀴즈"),
    
    /**
     * 주제별 퀴즈 - 특정 주제에 대한 퀴즈
     */
    TOPIC("주제별 퀴즈");

    /**
     * 퀴즈 유형 설명 (사용자에게 표시됨)
     */
    private final String description;

    /**
     * 퀴즈 유형 생성자
     * 
     * @param description 유형 설명
     */
    QuizType(String description) {
        this.description = description;
    }

    /**
     * 퀴즈 유형 설명 조회
     * 
     * @return 설명 문자열
     */
    public String getDescription() {
        return description;
    }
}