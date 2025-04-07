package com.quizplatform.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),

    // User Related Errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자명입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "잘못된 비밀번호입니다."),

    // Quiz Related Errors
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "퀴즈를 찾을 수 없습니다."),
    QUIZ_ALREADY_COMPLETED(HttpStatus.CONFLICT, "Q002", "이미 완료된 퀴즈입니다."),
    QUIZ_TIME_EXPIRED(HttpStatus.FORBIDDEN, "Q003", "퀴즈 시간이 만료되었습니다."),
    INVALID_QUESTION(HttpStatus.NOT_FOUND,"Q004","잘못된 퀴즈입니다"),

    // Review Related Errors
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "R002", "이미 리뷰를 작성했습니다."),
    INVALID_REVIEW_RATING(HttpStatus.BAD_REQUEST, "R003", "잘못된 별점입니다."),
    INVALID_REVIEW_CONTENT(HttpStatus.BAD_REQUEST, "R004", "잘못된 리뷰 내용입니다."),

    // Battle Related Errors
    BATTLE_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "배틀룸을 찾을 수 없습니다."),
    BATTLE_ROOM_FULL(HttpStatus.CONFLICT, "B002", "배틀룸이 가득 찼습니다."),
    BATTLE_ALREADY_STARTED(HttpStatus.CONFLICT, "B003", "이미 시작된 배틀입니다."),
    BATTLE_NOT_STARTED(HttpStatus.FORBIDDEN, "B004", "아직 시작되지 않은 배틀입니다."),
    BATTLE_NOT_IN_PROGRESS(HttpStatus.FORBIDDEN, "B005", "진행 중이 아닌 배틀입니다."),
    BATTLE_ALREADY_FINISHED(HttpStatus.CONFLICT, "B006", "이미 종료된 배틀입니다."),

    // Level Related Errors
    INSUFFICIENT_LEVEL(HttpStatus.FORBIDDEN, "L001", "레벨이 부족합니다."),
    INVALID_EXPERIENCE_POINTS(HttpStatus.BAD_REQUEST, "L002", "잘못된 경험치 값입니다."),

    // 참가자 관련 에러
    ALREADY_PARTICIPATING(HttpStatus.CONFLICT, "B010", "이미 참가 중인 사용자입니다."),
    NOT_READY_TO_START(HttpStatus.FORBIDDEN, "B011", "모든 참가자가 준비되지 않았습니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "B012", "참가자를 찾을 수 없습니다."),
    NO_PARTICIPANTS(HttpStatus.BAD_REQUEST, "B013", "참가자가 없습니다."),

    // 답변 관련 에러
    INVALID_QUESTION_SEQUENCE(HttpStatus.BAD_REQUEST, "B020", "잘못된 문제 순서입니다."),
    ANSWER_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "B021", "이미 답변을 제출했습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "B022", "답변을 찾을 수 없습니다."),
    INVALID_ANSWER_FORMAT(HttpStatus.BAD_REQUEST, "B023", "잘못된 답변 형식입니다."),

    // 시간 관련 에러
    INVALID_TIME_LIMIT(HttpStatus.BAD_REQUEST, "B031", "잘못된 시간 제한입니다."),

    // 점수 관련 에러
    INVALID_BONUS_POINTS(HttpStatus.BAD_REQUEST, "B040", "잘못된 보너스 점수입니다."),
    SCORE_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "B041", "점수 계산 중 오류가 발생했습니다."),

    // 배틀 검증 관련 에러
    INVALID_PARTICIPANT_COUNT(HttpStatus.BAD_REQUEST, "B050", "잘못된 참가자 수입니다."),
    INVALID_BATTLE_SETTINGS(HttpStatus.BAD_REQUEST, "B051", "잘못된 배틀 설정입니다."),
    BATTLE_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "B052", "배틀 검증 오류가 발생했습니다."),

    // 태그 에러
    TAG_NOT_FOUND(HttpStatus.BAD_REQUEST, "T001","태그가 존재하지 않습니다"),

    PARTICIPANT_INACTIVE(HttpStatus.BAD_REQUEST, "11", "배틀 검증 오류가 발생했습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;
}