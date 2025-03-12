"use client";

import { useProfileStore } from "@/store/profileStore";
import Image from "next/image";
import { useRouter } from "next/navigation";

export default function Profile() {
  const { userProfile } = useProfileStore();
  const router = useRouter();

  const onEditClick = () => {
    router.push("/profile/edit"); // ✅ 프로필 수정 페이지로 이동 (추후 구현 가능)
  };

  return (
    <div className="max-w-4xl mx-auto p-6 border border-border rounded bg-background shadow">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold">프로필</h1>
        {/* ✅ 편집 버튼 추가 */}
        <button
          onClick={onEditClick}
          className="px-4 py-2 bg-primary text-white rounded hover:bg-primary-hover transition"
        >
          프로필 수정
        </button>
      </div>

      {userProfile ? (
        <div className="mt-4 p-4 flex items-center space-x-4">
          <Image
            src={userProfile.profileImage || "/default-avatar.png"}
            alt="Profile"
            width={82}
            height={82}
            className="w-12 h-12 rounded-full"
          />
          <div>
            <p className="text-lg font-semibold">{userProfile.username}</p>
            <p className="text-neutral">{userProfile.email}</p>
          </div>
        </div>
      ) : (
        <p>프로필 정보를 불러오는 중...</p>
      )}
    </div>
  );
}
