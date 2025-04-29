"use client";

import { useEffect } from "react";
import Image from "next/image";
import { format } from "date-fns";
import { useGetMyProfile } from "@/lib/api/user/useGetMyProfile";
import Button from "@/app/_components/Button";
import { useParams } from "next/navigation";
import { useProfileStore } from "@/store/profileStore"; // ✅ 추가

export default function Profile() {
  const params = useParams();
  const userId = params?.userId ? Number(params.userId) : undefined;

  const {
    data: fetchedProfile,
    isLoading,
    error,
    refetch,
  } = useGetMyProfile(userId);

  const { userProfile, setUserProfile } = useProfileStore();

  // 프로필을 처음 불러왔을 때 store에 저장
  useEffect(() => {
    if (fetchedProfile) {
      setUserProfile(fetchedProfile);
    }
  }, [fetchedProfile, setUserProfile]);

  // ✅ 스토어에 데이터가 없는 경우만 로딩/에러 처리
  if (isLoading && !userProfile) {
    return <p className="text-center">프로필 정보를 불러오는 중...</p>;
  }

  if (error && !userProfile) {
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
    <div className="max-w-full min-w-200 rounded-md mx-auto text-foreground p-4 border border-border bg-background shadow-sm">
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
            <p className="text-lg font-semibold text-foreground">
              {userProfile.username}
            </p>
            <p className="text-sm text-foreground">{userProfile.email}</p>
            <p className="text-xs text-foreground">
              가입일:{" "}
              {userProfile.joinedAt
                ? format(new Date(userProfile.joinedAt), "yyyy-MM-dd")
                : "정보 없음"}
            </p>
            <p className="text-xs text-foreground">
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
