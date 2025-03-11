import type { Meta, StoryObj } from "@storybook/react";
import Tag from "@/app/_components/Tag";
import "@/styles/globals.css";

const meta: Meta<typeof Tag> = {
  title: "Components/Tag",
  component: Tag,
  argTypes: {
    tag: {
      control: "object",
      defaultValue: {
        id: 1,
        name: "JavaScript",
        quizCount: 12,
        description: "자바스크립트 관련 문제들",
        synonyms: ["JS", "ECMAScript"],
      },
    },
    selected: { control: "boolean", defaultValue: false },
  },
};

export default meta;

type Story = StoryObj<typeof Tag>;

/** ✅ 기본 태그 */
export const Default: Story = {
  args: {
    tag: {
      id: 1,
      name: "JavaScript",
      quizCount: 12,
      description: "자바스크립트 관련 문제들",
      synonyms: ["JS", "ECMAScript"],
    },
  },
};

/** ✅ 선택된 태그 */
export const Selected: Story = {
  args: {
    tag: {
      id: 2,
      name: "React",
      quizCount: 8,
      description: "React.js 관련 문제들",
      synonyms: ["ReactJS", "리액트"],
    },
    selected: true,
  },
};
