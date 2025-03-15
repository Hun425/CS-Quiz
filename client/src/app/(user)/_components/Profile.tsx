"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { format } from "date-fns";
import { useGetMyProfile } from "@/lib/api/user/useGetMyProfile";
import Button from "@/app/_components/Button";
import { Edit } from "lucide-react";

export default function Profile() {
  const router = useRouter();
  const { data: userProfile, isLoading, error, refetch } = useGetMyProfile();

  const onEditClick = () => {
    router.push("/profile/edit");
  };

  if (isLoading) {
    return <p className="text-center">프로필 정보를 불러오는 중...</p>;
  }

  if (error) {
    return (
      <div className="text-center text-red-500">
        오류 발생: 프로필 정보를 불러오지 못했습니다.
        <Button
          onClick={() => refetch()}
          className="ml-2 text-blue-500 underline text-sm px-2 py-1"
        >
          다시 시도
        </Button>
      </div>
    );
  }

  if (!userProfile) {
    return (
      <p className="text-center text-muted">
        프로필 정보를 불러올 수 없습니다.
      </p>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-4 border border-border rounded bg-background shadow-sm">
      {/* ✅ 프로필 정보 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Image
            src={userProfile.profileImage || "/default-avatar.png"}
            alt="Profile"
            width={64}
            height={64}
            className="w-16 h-16 rounded-full border border-border shadow-sm"
          />
          <div>
            <p className="text-lg font-semibold">{userProfile.username}</p>
            <p className="text-sm text-muted">{userProfile.email}</p>
            <p className="text-xs text-muted">
              가입일:{" "}
              {userProfile.joinedAt
                ? format(new Date(userProfile.joinedAt), "yyyy-MM-dd")
                : "정보 없음"}
            </p>
            <p className="text-xs text-muted">
              마지막 로그인:{" "}
              {userProfile.lastLogin
                ? format(new Date(userProfile.lastLogin), "yyyy-MM-dd HH:mm")
                : "정보 없음"}
            </p>
          </div>
        </div>
      </div>
      {/* ✅ 총 포인트 */}
      <div className="mt-3 text-base font-semibold flex items-center justify-between">
        <div>
          총 포인트:{" "}
          <span className="text-blue-600">
            {userProfile.totalPoints ?? 0} P
          </span>
        </div>
        {/* ✅ 프로필 수정 버튼 */}
        <Button onClick={onEditClick} className="text-sm p-1 rounded-full">
          <Edit className="w-4 h-4 text-white" />
        </Button>
      </div>

      {/* ✅ 레벨 & 경험치 */}
      <div className="mt-4 p-3 bg-gray-100 rounded-lg">
        <p className="text-base font-semibold">레벨 {userProfile.level}</p>
        <div className="relative w-full h-3 bg-gray-300 rounded-full mt-1">
          <div
            className="absolute top-0 left-0 h-3 bg-blue-500 rounded-full transition-all"
            style={{
              width: `${
                userProfile.requiredExperience
                  ? (userProfile.experience / userProfile.requiredExperience) *
                    100
                  : 0
              }%`,
            }}
          ></div>
        </div>
        <p className="text-xs text-gray-600 mt-1">
          경험치: {userProfile.experience} /{" "}
          {userProfile.requiredExperience ?? "정보 없음"}
        </p>
      </div>
    </div>
  );
}
