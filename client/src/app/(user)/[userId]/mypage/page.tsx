"use client";
import { useParams } from "next/navigation";
import Dashboard from "@/app/(user)/_components/Dashboard";

export default function OtherUserMypage() {
  const { userId } = useParams();

  return <Dashboard userId={Number(userId)} />;
}
