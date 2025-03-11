import type { Meta, StoryObj } from "@storybook/react";
import Skeleton from "@/app/_components/Skeleton";
import QuizCardSkeleton from "@/app/quizzes/_components/QuizCardSkeleton";

const meta: Meta<typeof Skeleton> = {
  title: "Components/Skeleton",
  component: Skeleton,
};

export default meta;

type Story = StoryObj<typeof Skeleton>;

/** ✅ 기본 스켈레톤 */
export const Default: Story = {
  args: {
    className: "w-32 h-10 rounded-md",
  },
};

/** ✅ 카드 형태 */
export const CardSkeleton: StoryObj = {
  render: () => (
    <div>
      <QuizCardSkeleton />
    </div>
  ),
};

/** ✅ 리스트 아이템 */
export const ListItemSkeleton: Story = {
  args: {
    className: "w-full h-6 my-2",
  },
};
