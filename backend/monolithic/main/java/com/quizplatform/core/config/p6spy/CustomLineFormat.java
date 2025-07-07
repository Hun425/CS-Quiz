package com.quizplatform.core.config.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * P6Spy 로그 출력 형식 커스터마이징 클래스
 * SQL 실행 시간, 날짜, 연결 ID, SQL 문을 포함한 로그 형식을 제공합니다.
 */
public class CustomLineFormat implements MessageFormattingStrategy {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA);

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, 
                               String prepared, String sql, String url) {
        if (sql.trim().isEmpty()) {
            return "";
        }
        
        // SQL 포맷팅 및 다듬기
        String formattedSql = formatSql(sql);
        
        // 로그 메시지 생성
        return String.format("[P6Spy] %s | %d ms | %s | %s | %s", 
                DATE_FORMAT.format(new Date()),
                elapsed,
                category,
                connectionId,
                formattedSql);
    }
    
    /**
     * SQL 문을 포맷팅하여 가독성을 높입니다.
     */
    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        
        // 여러 줄의 SQL을 한 줄로 변환
        String oneLine = sql.replaceAll("\\s+", " ").trim();
        
        // SQL이 너무 길면 줄바꿈 추가
        if (oneLine.length() > 100) {
            return NEW_LINE + sql;
        }
        
        return oneLine;
    }
}
