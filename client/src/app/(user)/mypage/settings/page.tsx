"use client";

import { useState } from "react";
import { useUpdateMyProfile } from "@/lib/api/user/useUpdateMyProfile";
import Button from "@/app/_components/Button";
import { Loader2, Pencil, Check, X } from "lucide-react";

const SettingPage = () => {
  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState("사용자123"); // 기본값 (실제 프로필 정보 가져오면 됨)
  const [profileImage, setProfileImage] = useState(
    "https://via.placeholder.com/100"
  ); // 기본값 (기본 프로필 이미지)
  const { mutate, isLoading } = useUpdateMyProfile();

  const handleSave = () => {
    mutate(
      { username, profileImage },
      { onSuccess: () => setIsEditing(false) }
    );
  };

  return (
    <div className="max-w-lg flex justify-between space-y-6">
      <h1 className="text-2xl font-semibold text-primary mb-4">프로필</h1>

      {/* ✅ 프로필 정보 (기본 상태) */}
      {!isEditing ? (
        <div className="flex items-center space-x-4">
          <div className="flex flex-col">
            <Button
              variant="primary"
              className="mt-1 text-sm hover:underline flex items-center"
              onClick={() => setIsEditing(true)}
            >
              <Pencil size={14} className="mr-1" /> 수정
            </Button>
          </div>
        </div>
      ) : (
        /* ✅ 수정 모드 */
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-muted mb-1">
              유저네임
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full border border-border p-2 rounded-md"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-muted mb-1">
              프로필 이미지 (URL)
            </label>
            <input
              type="url"
              value={profileImage}
              onChange={(e) => setProfileImage(e.target.value)}
              className="w-full border border-border p-2 rounded-md"
            />
          </div>

          <div className="flex space-x-2">
            <Button
              onClick={handleSave}
              disabled={isLoading}
              className="flex-1"
            >
              {isLoading ? (
                <Loader2 className="animate-spin w-5 h-5" />
              ) : (
                <Check size={16} />
              )}
              저장
            </Button>
            <Button
              onClick={() => setIsEditing(false)}
              variant="secondary"
              className="flex-1"
            >
              <X size={16} /> 취소
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SettingPage;
