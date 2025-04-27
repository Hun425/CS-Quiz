// BattleRoomCard.stories.tsx
import type { Meta, StoryObj } from "@storybook/react";
import BattleRoomCard from "@/app/battles/_components/BattleRoomCard";
import { BattleStatus } from "@/lib/types/battle";

const meta: Meta<typeof BattleRoomCard> = {
  title: "Battle/BattleRoomCard",
  component: BattleRoomCard,
};

export default meta;
type Story = StoryObj<typeof BattleRoomCard>;

const mockRoom = {
  id: 1,
  quizId: 101,
  quizTitle: "자료구조 마스터 테스트",
  status: BattleStatus.WAITING,
  roomCode: "XYZ123",
  currentParticipants: 3,
  maxParticipants: 5,
  questionCount: 10,
  timeLimit: 300,
  participants: [],
  createdAt: new Date().toISOString(),
};

export const 기본: Story = {
  args: {
    room: mockRoom,
  },
};
