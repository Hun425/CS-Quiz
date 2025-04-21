package com.quizplatform.modules.search.service;

import java.util.List;
import java.util.UUID;

/**
 * 검색 인덱스 관리를 위한 서비스 인터페이스입니다.
 * 다양한 엔티티를 검색 인덱스에 추가, 업데이트, 삭제하는 기능을 제공합니다.
 *
 * @author Claude
 * @since JDK 17
 */
public interface SearchIndexService {
    
    /**
     * 퀴즈 정보를 검색 인덱스에 추가합니다.
     *
     * @param quizId 퀴즈 ID
     * @param creatorId 퀴즈 생성자 ID
     * @param category 퀴즈 카테고리
     * @param difficulty 퀴즈 난이도
     * @param tags 퀴즈 태그 목록
     */
    void indexQuiz(UUID quizId, UUID creatorId, String category, String difficulty, List<String> tags);
    
    /**
     * 퀴즈 정보를 검색 인덱스에서 업데이트합니다.
     *
     * @param quizId 퀴즈 ID
     * @param category 업데이트된 퀴즈 카테고리
     * @param difficulty 업데이트된 퀴즈 난이도
     * @param tags 업데이트된 퀴즈 태그 목록
     */
    void updateQuizIndex(UUID quizId, String category, String difficulty, List<String> tags);
    
    /**
     * 퀴즈를 검색 인덱스에서 삭제합니다.
     *
     * @param quizId 삭제할 퀴즈 ID
     */
    void removeQuizFromIndex(UUID quizId);
}
