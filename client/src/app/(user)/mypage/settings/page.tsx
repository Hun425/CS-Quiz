"use client";

import { useState, useEffect } from "react";
import { useUpdateMyProfile } from "@/lib/api/user/useUpdateMyProfile";

import { useProfileStore } from "@/store/profileStore";
import Button from "@/app/_components/Button";
import { Loader2, Pencil, Check, X } from "lucide-react";
import Image from "next/image";
import SocialAccountSection from "./../_components/SocialAccountSection";

const SettingPage = () => {
  const { userProfile } = useProfileStore();
  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState(userProfile?.username ?? "");

  const { mutate, isPending } = useUpdateMyProfile();

  const handleSave = () => {
    mutate(
      { username }, // ✅ profileImage 제거
      {
        onSuccess: () => setIsEditing(false),
      }
    );
  };

  useEffect(() => {
    if (userProfile) {
      setUsername(userProfile.username);
    }
  }, [userProfile]);

  return (
    <div className="w-full mx-auto p-6 space-y-6 bg-background rounded-lg ">
      <h1 className="text-2xl font-bold text-primary">프로필 설정</h1>

      <div className="flex items-center space-x-4">
        <Image
          src={userProfile?.profileImage ?? "/default-profile.png"}
          alt="프로필 이미지"
          className="w-20 h-20 rounded-full border object-cover"
          width={80}
          height={80}
        />
        <div className="flex-1 space-y-1">
          {!isEditing ? (
            <>
              <p className="text-lg font-medium text-foreground">{username}</p>
            </>
          ) : (
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              aria-label="유저네임 입력"
              className="w-full border border-border p-2 rounded-md text-sm"
            />
          )}
        </div>

        {!isEditing && (
          <Button
            variant="primary"
            size="small"
            onClick={() => setIsEditing(true)}
            aria-label="프로필 수정"
            className="flex items-center gap-1"
          >
            <Pencil size={14} />
            수정
          </Button>
        )}
      </div>

      {isEditing && (
        <div className="flex gap-2 justify-end">
          <Button
            onClick={handleSave}
            disabled={isPending}
            aria-label="프로필 저장"
            className="flex items-center gap-2"
          >
            {isPending ? (
              <Loader2 className="animate-spin w-4 h-4" />
            ) : (
              <Check size={16} />
            )}
            저장
          </Button>
          <Button
            variant="secondary"
            onClick={() => setIsEditing(false)}
            aria-label="수정 취소"
            className="flex items-center gap-2"
          >
            <X size={16} />
            취소
          </Button>
        </div>
      )}
      <section className="border-t border-border pt-6">
        <SocialAccountSection />
      </section>
    </div>
  );
};

export default SettingPage;
