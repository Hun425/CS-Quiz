import { Metadata } from "next";
import BattleRoomClientPage from "./BattleRoomClientPage";

export const metadata: Metadata = {
  title: "배틀룸 - 실시간 퀴즈 대결",
  description: "실시간으로 사용자들과 퀴즈 대결을 즐겨보세요!",
};

export default function BattleRoomPage() {
  return <BattleRoomClientPage />;
}
