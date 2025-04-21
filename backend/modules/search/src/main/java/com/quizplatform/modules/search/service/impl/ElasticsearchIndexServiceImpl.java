package com.quizplatform.modules.search.service.impl;

import com.quizplatform.modules.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Elasticsearch를 사용한 SearchIndexService 구현체입니다.
 * 엔티티 정보를 Elasticsearch 인덱스에 저장하고 관리합니다.
 *
 * @author Claude
 * @since JDK 17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexServiceImpl implements SearchIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private static final String QUIZ_INDEX = "quizzes";

    /**
     * 퀴즈 정보를 Elasticsearch 인덱스에 추가합니다.
     *
     * @param quizId 퀴즈 ID
     * @param creatorId 퀴즈 생성자 ID
     * @param category 퀴즈 카테고리
     * @param difficulty 퀴즈 난이도
     * @param tags 퀴즈 태그 목록
     */
    @Override
    public void indexQuiz(UUID quizId, UUID creatorId, String category, String difficulty, List<String> tags) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("id", quizId.toString());
            document.put("creatorId", creatorId.toString());
            document.put("category", category);
            document.put("difficulty", difficulty);
            document.put("tags", tags);
            document.put("createdAt", System.currentTimeMillis());

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(quizId.toString())
                    .withObject(document)
                    .build();

            String documentId = elasticsearchOperations.index(indexQuery, IndexCoordinates.of(QUIZ_INDEX));
            log.debug("Quiz indexed with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Error indexing quiz {}: {}", quizId, e.getMessage(), e);
            throw new RuntimeException("Failed to index quiz: " + e.getMessage(), e);
        }
    }

    /**
     * 퀴즈 정보를 Elasticsearch 인덱스에서 업데이트합니다.
     *
     * @param quizId 퀴즈 ID
     * @param category 업데이트된 퀴즈 카테고리
     * @param difficulty 업데이트된 퀴즈 난이도
     * @param tags 업데이트된 퀴즈 태그 목록
     */
    @Override
    public void updateQuizIndex(UUID quizId, String category, String difficulty, List<String> tags) {
        try {
            Map<String, Object> document = new HashMap<>();
            document.put("id", quizId.toString());
            document.put("category", category);
            document.put("difficulty", difficulty);
            document.put("tags", tags);
            document.put("updatedAt", System.currentTimeMillis());

            IndexQuery indexQuery = new IndexQueryBuilder()
                    .withId(quizId.toString())
                    .withObject(document)
                    .build();

            String documentId = elasticsearchOperations.index(indexQuery, IndexCoordinates.of(QUIZ_INDEX));
            log.debug("Quiz updated in index with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Error updating quiz {} in index: {}", quizId, e.getMessage(), e);
            throw new RuntimeException("Failed to update quiz index: " + e.getMessage(), e);
        }
    }

    /**
     * 퀴즈를 Elasticsearch 인덱스에서 삭제합니다.
     *
     * @param quizId 삭제할 퀴즈 ID
     */
    @Override
    public void removeQuizFromIndex(UUID quizId) {
        try {
            String documentId = elasticsearchOperations.delete(quizId.toString(), IndexCoordinates.of(QUIZ_INDEX));
            log.debug("Quiz removed from index with ID: {}", documentId);
        } catch (Exception e) {
            log.error("Error removing quiz {} from index: {}", quizId, e.getMessage(), e);
            throw new RuntimeException("Failed to remove quiz from index: " + e.getMessage(), e);
        }
    }
}
