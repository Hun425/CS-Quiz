"use client";

import { useState, useMemo } from "react";
import { TagResponse } from "@/lib/types/tag";
import { useCreateTag } from "@/lib/api/tag/useCreateTags";
import { useDebounce } from "@/lib/hooks/useDebounce";

interface TagSelectorProps {
  allTags: TagResponse[];
  selectedTagIds: number[];
  onChange: (tagIds: number[]) => void;
}

const TagSelector = ({
  allTags,
  selectedTagIds,
  onChange,
}: TagSelectorProps) => {
  const [input, setInput] = useState("");
  const [description, setDescription] = useState("");
  const { mutate: createTag, isPending } = useCreateTag();
  const debouncedInput = useDebounce(input, 300);

  const selectedTags = allTags.filter((tag) => selectedTagIds.includes(tag.id));

  const filteredTags = useMemo(() => {
    return allTags.filter(
      (tag) =>
        tag.name.toLowerCase().includes(debouncedInput.toLowerCase()) &&
        !selectedTagIds.includes(tag.id)
    );
  }, [debouncedInput, allTags, selectedTagIds]);

  const exactMatch = allTags.find(
    (tag) => tag.name.toLowerCase() === input.toLowerCase()
  );

  const handleSelect = (tag: TagResponse) => {
    onChange([...selectedTagIds, tag.id]);
    setInput("");
  };

  const handleRemove = (id: number) => {
    onChange(selectedTagIds.filter((tagId) => tagId !== id));
  };

  const handleCreateTag = () => {
    if (!input.trim() || exactMatch) return;

    createTag(
      {
        name: input.trim(),
        description: description.trim() || `${input.trim()} 태그.`,
        parentId: null,
        synonyms: [],
      },
      {
        onSuccess: (res) => {
          handleSelect(res.data);
          setInput("");
          setDescription("");
        },
      }
    );
  };

  return (
    <div className="space-y-2">
      {/* 선택된 태그 */}
      <div className="flex flex-wrap gap-2">
        {selectedTags.map((tag) => (
          <span
            key={tag.id}
            className="bg-primary/10 text-primary text-sm px-3 py-1 rounded-full flex items-center gap-2"
          >
            #{tag.name}
            <button
              type="button"
              onClick={() => handleRemove(tag.id)}
              className="ml-1 text-xs hover:text-red-500"
            >
              ×
            </button>
          </span>
        ))}
      </div>

      {/* 입력 + 자동완성 */}
      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="태그 이름 입력"
        className="w-full border p-2 rounded-md"
      />

      {input && filteredTags.length > 0 && (
        <ul className="border rounded bg-white max-h-40 overflow-y-auto">
          {filteredTags.map((tag) => (
            <li
              key={tag.id}
              className="px-4 py-2 text-sm hover:bg-gray-100 cursor-pointer"
              onClick={() => handleSelect(tag)}
            >
              + #{tag.name}
            </li>
          ))}
        </ul>
      )}

      {/* 태그 설명 및 추가 버튼 */}
      {input && !exactMatch && (
        <div className="mt-2 space-y-2">
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="태그 설명 (선택 입력)"
            className="w-full border p-2 rounded-md text-sm"
            rows={2}
          />

          <button
            type="button"
            onClick={handleCreateTag}
            disabled={isPending}
            className="text-sm text-blue-600 hover:underline disabled:text-gray-400"
          >
            {isPending ? "추가 중..." : `"${input}" 태그 추가하기`}
          </button>
        </div>
      )}
    </div>
  );
};

export default TagSelector;
