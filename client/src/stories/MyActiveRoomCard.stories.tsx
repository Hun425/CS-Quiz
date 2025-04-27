import type { Meta, StoryObj } from "@storybook/react";
import MyActiveRoomCard from "@/app/battles/_components/MyActiveRoomCard";
import { BattleStatus, type BattleRoomResponse } from "@/lib/types/battle";

const meta: Meta<typeof MyActiveRoomCard> = {
  title: "Battle/MyActiveRoomCard",
  component: MyActiveRoomCard,
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof MyActiveRoomCard>;

const baseRoom: BattleRoomResponse = {
  id: 1,
  quizId: 1001,
  quizTitle: "자료구조 기초 퀴즈",
  questionCount: 10,
  timeLimit: 60,
  status: BattleStatus.WAITING,
  currentParticipants: 2,
  maxParticipants: 4,
  roomCode: "ROOM1234",
  createdAt: new Date().toISOString(),
  participants: [
    {
      userId: 1,
      username: "알고리즘고수",
      profileImage: "https://via.placeholder.com/40",
      level: 12,
      ready: true,
    },
    {
      userId: 2,
      username: "자료구조짱짱",
      profileImage: "https://via.placeholder.com/40",
      level: 8,
      ready: false,
    },
  ],
};

export const Waiting: Story = {
  args: {
    room: {
      ...baseRoom,
      status: BattleStatus.WAITING,
    },
  },
};

export const InProgress: Story = {
  args: {
    room: {
      ...baseRoom,
      status: BattleStatus.IN_PROGRESS,
      currentParticipants: 4,
    },
  },
};

export const Ended: Story = {
  args: {
    room: {
      ...baseRoom,
      status: BattleStatus.FINISHED,
      currentParticipants: 3,
    },
  },
};
