"use client";
import { useBattleSocket } from "@/lib/services/websocket/useBattleSocket";

const BattleSocketHandler = ({ roomId }: { roomId: number }) => {
  useBattleSocket(roomId);
  return null;
};

export default BattleSocketHandler;
