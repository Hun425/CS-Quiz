"use client";

import React, { useEffect, useState } from "react";
// import { tagApi } from "@/lib/api/tagApi";
import { TagResponse } from "@/lib/types/tag";

interface TagSelectorProps {
  selectedTagIds: number[];
  onChange: (tagIds: number[]) => void;
  label?: string;
  maxTags?: number;
  required?: boolean;
}

const TagSelector: React.FC<TagSelectorProps> = ({
  selectedTagIds,
  onChange,
  label = "태그 선택",
  maxTags = 5,
  required = false,
}) => {
  const [allTags] = useState<TagResponse[]>([]);
  const [loading] = useState<boolean>(true);
  const [error] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [filteredTags, setFilteredTags] = useState<TagResponse[]>([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState<boolean>(false);

  // 모든 태그 로드
  useEffect(() => {
    // const fetchTags = async () => {
    //   try {
    //     setLoading(true);
    //     const response = await tagApi.getAllTags();
    //     if (response.data.success) {
    //       setAllTags(response.data.data);
    //       setFilteredTags(response.data.data);
    //     } else {
    //       setError("태그를 불러오는 데 실패했습니다.");
    //     }
    //   } catch (err: any) {
    //     console.error("태그 로딩 중 오류:", err);
    //     setError("태그를 불러오는 중 오류가 발생했습니다.");
    //   } finally {
    //     setLoading(false);
    //   }
    // };
    // fetchTags();
  }, []);

  // 검색어에 따라 태그 필터링
  useEffect(() => {
    if (searchTerm.trim()) {
      const filtered = allTags.filter(
        (tag) =>
          tag.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          (tag.synonyms &&
            Array.from(tag.synonyms).some((synonym) =>
              synonym.toLowerCase().includes(searchTerm.toLowerCase())
            ))
      );
      setFilteredTags(filtered);
    } else {
      setFilteredTags(allTags);
    }
  }, [searchTerm, allTags]);

  // 태그 선택 또는 해제
  const toggleTag = (tagId: number) => {
    if (selectedTagIds.includes(tagId)) {
      // 태그 해제
      onChange(selectedTagIds.filter((id) => id !== tagId));
    } else {
      // 최대 태그 수 체크
      if (selectedTagIds.length >= maxTags) {
        alert(`최대 ${maxTags}개의 태그만 선택할 수 있습니다.`);
        return;
      }
      // 태그 선택
      onChange([...selectedTagIds, tagId]);
    }
  };

  // 선택한 태그 정보 가져오기
  const getSelectedTags = (): TagResponse[] => {
    return allTags.filter((tag) => selectedTagIds.includes(tag.id));
  };

  // 드롭다운 외부 클릭 감지를 위한 이벤트 핸들러
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest(".tag-selector")) {
        setIsDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div className="tag-selector">
      <label
        htmlFor="tag-search"
        style={{
          display: "block",
          marginBottom: "0.5rem",
          fontWeight: "bold",
        }}
      >
        {label} {required && <span style={{ color: "red" }}>*</span>}
      </label>

      {/* 선택된 태그 표시 */}
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "0.5rem",
          marginBottom: "0.75rem",
        }}
      >
        {getSelectedTags().map((tag) => (
          <div
            key={tag.id}
            style={{
              backgroundColor: "#e3f2fd",
              padding: "0.25rem 0.5rem",
              borderRadius: "20px",
              display: "flex",
              alignItems: "center",
              gap: "0.5rem",
            }}
          >
            <span>{tag.name}</span>
            <button
              type="button"
              onClick={() => toggleTag(tag.id)}
              style={{
                backgroundColor: "transparent",
                border: "none",
                color: "#f44336",
                cursor: "pointer",
                padding: "0",
                fontSize: "1rem",
                lineHeight: "1",
              }}
            >
              ✕
            </button>
          </div>
        ))}
        {selectedTagIds.length === 0 && (
          <div
            style={{
              color: "#666",
              fontSize: "0.9rem",
              padding: "0.25rem 0",
            }}
          >
            선택된 태그 없음
          </div>
        )}
      </div>

      {/* 태그 검색 및 드롭다운 */}
      <div style={{ position: "relative" }}>
        <input
          id="tag-search"
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="태그 검색..."
          onFocus={() => setIsDropdownOpen(true)}
          style={{
            width: "100%",
            padding: "0.5rem",
            borderRadius: "4px",
            border: "1px solid #ccc",
          }}
        />

        {isDropdownOpen && (
          <div
            style={{
              position: "absolute",
              top: "100%",
              left: 0,
              right: 0,
              maxHeight: "200px",
              overflowY: "auto",
              border: "1px solid #ccc",
              borderRadius: "4px",
              backgroundColor: "white",
              zIndex: 10,
              boxShadow: "0 2px 5px rgba(0, 0, 0, 0.1)",
            }}
          >
            {loading ? (
              <div
                style={{
                  padding: "0.5rem",
                  textAlign: "center",
                  color: "#666",
                }}
              >
                로딩 중...
              </div>
            ) : error ? (
              <div
                style={{
                  padding: "0.5rem",
                  textAlign: "center",
                  color: "#d32f2f",
                }}
              >
                {error}
              </div>
            ) : filteredTags.length === 0 ? (
              <div
                style={{
                  padding: "0.5rem",
                  textAlign: "center",
                  color: "#666",
                }}
              >
                태그를 찾을 수 없습니다
              </div>
            ) : (
              filteredTags.map((tag) => (
                <div
                  key={tag.id}
                  onClick={() => toggleTag(tag.id)}
                  style={{
                    padding: "0.5rem",
                    cursor: "pointer",
                    backgroundColor: selectedTagIds.includes(tag.id)
                      ? "#e3f2fd"
                      : "white",
                    borderBottom: "1px solid #eee",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <div>
                    <span
                      style={{
                        fontWeight: selectedTagIds.includes(tag.id)
                          ? "bold"
                          : "normal",
                      }}
                    >
                      {tag.name}
                    </span>
                    {tag.description && (
                      <span
                        style={{
                          fontSize: "0.8rem",
                          color: "#666",
                          marginLeft: "0.5rem",
                        }}
                      >
                        -{" "}
                        {tag.description.length > 30
                          ? `${tag.description.substring(0, 30)}...`
                          : tag.description}
                      </span>
                    )}
                  </div>
                  <span style={{ color: "#666", fontSize: "0.8rem" }}>
                    퀴즈 {tag.quizCount}개
                  </span>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {/* 도움말 */}
      <div style={{ marginTop: "0.5rem", fontSize: "0.8rem", color: "#666" }}>
        최대 {maxTags}개의 태그를 선택할 수 있습니다.
      </div>
    </div>
  );
};

export default TagSelector;
