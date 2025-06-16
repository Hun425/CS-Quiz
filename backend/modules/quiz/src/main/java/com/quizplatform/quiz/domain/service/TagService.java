package com.quizplatform.quiz.domain.service;

import com.quizplatform.common.auth.CurrentUserInfo;
import com.quizplatform.quiz.domain.model.Tag;

import java.util.List;
import java.util.Optional;

/**
 * 태그 도메인 서비스 인터페이스
 * 
 * <p>태그 관리에 관련된 비즈니스 로직을 정의합니다.
 * 계층구조 관리, 검색, 통계, 관리자 권한 체크 등의 기능을 제공합니다.</p>
 *
 * @author 채기훈
 * @since JDK 21.0.6 Eclipse Temurin
 */
public interface TagService {
    
    // ===== 기본 CRUD 작업 =====
    
    /**
     * 새로운 태그 생성
     * 
     * @param name 태그 이름
     * @param description 태그 설명
     * @param parentId 부모 태그 ID (루트 태그인 경우 null)
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 생성된 태그
     * @throws IllegalArgumentException 권한이 없거나 유효하지 않은 데이터인 경우
     */
    Tag createTag(String name, String description, Long parentId, CurrentUserInfo currentUser);
    
    /**
     * 태그 정보 수정
     * 
     * @param tagId 수정할 태그 ID
     * @param name 새 태그 이름
     * @param description 새 태그 설명
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 수정된 태그
     * @throws IllegalArgumentException 권한이 없거나 태그를 찾을 수 없는 경우
     */
    Tag updateTag(Long tagId, String name, String description, CurrentUserInfo currentUser);
    
    /**
     * 태그 삭제
     * 
     * @param tagId 삭제할 태그 ID
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @throws IllegalArgumentException 권한이 없거나 삭제할 수 없는 태그인 경우
     */
    void deleteTag(Long tagId, CurrentUserInfo currentUser);
    
    /**
     * 태그 활성화/비활성화
     * 
     * @param tagId 대상 태그 ID
     * @param active 활성화 상태
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 수정된 태그
     */
    Tag setTagActive(Long tagId, boolean active, CurrentUserInfo currentUser);
    
    // ===== 조회 작업 =====
    
    /**
     * 태그 ID로 조회
     * 
     * @param tagId 태그 ID
     * @return 태그 Optional
     */
    Optional<Tag> getTagById(Long tagId);
    
    /**
     * 태그 이름으로 조회
     * 
     * @param name 태그 이름
     * @return 태그 Optional
     */
    Optional<Tag> getTagByName(String name);
    
    /**
     * 모든 활성화된 태그 조회
     * 
     * @return 활성화된 태그 목록
     */
    List<Tag> getAllActiveTags();
    
    /**
     * 특정 레벨의 활성화된 태그들 조회
     * 
     * @param level 계층 레벨
     * @return 해당 레벨의 활성 태그 목록
     */
    List<Tag> getTagsByLevel(int level);
    
    // ===== 계층구조 관련 =====
    
    /**
     * 루트 태그들 조회
     * 
     * @return 루트 태그 목록
     */
    List<Tag> getRootTags();
    
    /**
     * 특정 태그의 자식 태그들 조회
     * 
     * @param parentId 부모 태그 ID
     * @return 자식 태그 목록
     */
    List<Tag> getChildrenTags(Long parentId);
    
    /**
     * 특정 태그의 모든 후손 태그들 조회
     * 
     * @param tagId 기준 태그 ID
     * @return 모든 후손 태그 목록
     */
    List<Tag> getAllDescendants(Long tagId);
    
    /**
     * 특정 태그의 모든 조상 태그들 조회
     * 
     * @param tagId 기준 태그 ID
     * @return 모든 조상 태그 목록
     */
    List<Tag> getAllAncestors(Long tagId);
    
    /**
     * 태그를 다른 부모로 이동
     * 
     * @param tagId 이동할 태그 ID
     * @param newParentId 새 부모 태그 ID (루트로 이동시 null)
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 이동된 태그
     */
    Tag moveTag(Long tagId, Long newParentId, CurrentUserInfo currentUser);
    
    // ===== 검색 기능 =====
    
    /**
     * 키워드로 태그 검색
     * 
     * @param keyword 검색 키워드
     * @return 검색된 태그 목록
     */
    List<Tag> searchTags(String keyword);
    
    /**
     * 고급 검색 (이름, 설명, 계층 레벨 조건)
     * 
     * @param keyword 검색 키워드
     * @param level 계층 레벨 (null이면 모든 레벨)
     * @param activeOnly 활성화된 태그만 검색 여부
     * @return 검색된 태그 목록
     */
    List<Tag> advancedSearch(String keyword, Integer level, boolean activeOnly);
    
    // ===== 통계 및 분석 =====
    
    /**
     * 인기 태그 조회 (사용 횟수 기준)
     * 
     * @param limit 조회할 태그 수
     * @return 인기 태그 목록
     */
    List<Tag> getPopularTags(int limit);
    
    /**
     * 최근 생성된 태그 조회
     * 
     * @param limit 조회할 태그 수
     * @return 최근 생성된 태그 목록
     */
    List<Tag> getRecentTags(int limit);
    
    /**
     * 사용되지 않는 태그 조회
     * 
     * @return 미사용 태그 목록
     */
    List<Tag> getUnusedTags();
    
    /**
     * 태그 사용 통계 조회
     * 
     * @param tagId 태그 ID
     * @return 사용 통계 정보
     */
    TagUsageStats getTagUsageStats(Long tagId);
    
    // ===== 퀴즈와의 연동 =====
    
    /**
     * 특정 퀴즈에 연결된 태그들 조회
     * 
     * @param quizId 퀴즈 ID
     * @return 퀴즈의 태그 목록
     */
    List<Tag> getQuizTags(Long quizId);
    
    /**
     * 퀴즈에 태그 연결 가능 여부 확인
     * 
     * @param quizId 퀴즈 ID
     * @param tagId 태그 ID
     * @return 연결 가능 여부
     */
    boolean canAddTagToQuiz(Long quizId, Long tagId);
    
    // ===== 관리자 전용 기능 =====
    
    /**
     * 태그 계층구조 전체 조회 (관리자용)
     * 
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 전체 태그 계층구조
     */
    List<Tag> getFullHierarchy(CurrentUserInfo currentUser);
    
    /**
     * 태그 대량 가져오기 (Legacy 데이터 마이그레이션용)
     * 
     * @param tags 가져올 태그 목록
     * @param currentUser 현재 사용자 정보 (관리자 권한 체크용)
     * @return 처리 결과 요약
     */
    TagImportResult importTags(List<TagImportData> tags, CurrentUserInfo currentUser);
    
    /**
     * 태그 사용량 통계를 나타내는 클래스
     */
    record TagUsageStats(
        Long tagId,
        String tagName,
        String fullPath,
        int usageCount,
        int connectedQuizCount,
        int descendantCount
    ) {}
    
    /**
     * 태그 가져오기 데이터를 나타내는 클래스
     */
    record TagImportData(
        String name,
        String description,
        String parentPath,
        boolean active
    ) {}
    
    /**
     * 태그 가져오기 결과를 나타내는 클래스
     */
    record TagImportResult(
        int totalProcessed,
        int successCount,
        int errorCount,
        List<String> errorMessages
    ) {}
}