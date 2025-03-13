import type { Meta, StoryObj } from "@storybook/react";
import Tag from "@/app/_components/Tag";
import "@/styles/globals.css";
import { TagResponse } from "@/lib/types/api";

const StoryTag = ({
  id = 1,
  name = "자바스크립트",
  quizCount = 10,
  description = "자바스크립트 관련 문제들",
  synonyms = ["JS", "ECMAScript"],
  selected = false,
  onClick,
}: {
  id?: number;
  name?: string;
  quizCount?: number;
  description?: string;
  synonyms?: string[];
  selected?: boolean;
  onClick?: () => void;
}) => {
  const tag: TagResponse = { id, name, quizCount, description, synonyms };

  return <Tag tag={tag} selected={selected} onClick={onClick} />;
};

const meta: Meta<typeof StoryTag> = {
  title: "Components/Tag",
  component: StoryTag,
  argTypes: {
    name: {
      control: "select",
      options: [
        "자바스크립트",
        "파이썬",
        "데이터베이스",
        "알고리즘",
        "자료구조",
        "시스템설계",
        "네트워크",
        "운영체제",
        "웹개발",
        "데브옵스",
        "머신러닝",
        "보안",
      ],
      defaultValue: "자바스크립트",
    },
    quizCount: { control: "number", defaultValue: 10 },
    description: { control: "text", defaultValue: "기본 설명" },
    synonyms: {
      control: "object", // ✅ "array" 대신 "object" 사용
      defaultValue: ["기본 태그"],
    },
    selected: { control: "boolean", defaultValue: false },
    onClick: { action: "clicked" }, // ✅ 클릭 이벤트 추가
  },
  args: {
    name: "자바스크립트",
    quizCount: 10,
    description: "자바스크립트 관련 문제들",
    synonyms: ["JS", "ECMAScript"],
    selected: false,
  },
};

export default meta;

type Story = StoryObj<typeof StoryTag>;

/** ✅ 기본 태그 (옵션 조작 가능) */
export const Default: Story = {
  args: {
    name: "자바스크립트",
    quizCount: 10,
    description: "자바스크립트 관련 문제들",
    synonyms: ["JS", "ECMAScript"],
    selected: false,
  },
};

/** ✅ 선택된 태그 */
export const Selected: Story = {
  args: {
    name: "알고리즘",
    quizCount: 10,
    description: "알고리즘 관련 문제들",
    synonyms: ["Algorithm", "알고"],
    selected: true,
  },
};

/** ✅ 다양한 태그 테스트 */
export const MultipleTags: Story = {
  render: () => (
    <div className="flex flex-wrap gap-2">
      {[
        {
          id: 3,
          name: "파이썬",
          quizCount: 15,
          description: "파이썬 문제들",
          synonyms: ["Python"],
        },
        {
          id: 4,
          name: "데이터베이스",
          quizCount: 7,
          description: "SQL & NoSQL",
          synonyms: ["DB"],
        },
        {
          id: 5,
          name: "네트워크",
          quizCount: 9,
          description: "TCP/IP 및 프로토콜",
          synonyms: ["Network"],
        },
        {
          id: 6,
          name: "운영체제",
          quizCount: 5,
          description: "OS 개념 및 원리",
          synonyms: ["OS"],
        },
        {
          id: 7,
          name: "머신러닝",
          quizCount: 6,
          description: "ML 기초",
          synonyms: ["ML"],
        },
      ].map((tag) => (
        <Tag key={tag.id} tag={tag} selected={tag.id === 6} />
      ))}
    </div>
  ),
};
