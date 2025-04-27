import { create } from "zustand";
import { persist } from "zustand/middleware";
import { UserProfile } from "@/lib/types/user";

interface ProfileState {
  userProfile: UserProfile | null;
  setUserProfile: (profile: UserProfile) => void;
  clearProfile: () => void;
}

export const useProfileStore = create<ProfileState>()(
  persist(
    (set) => ({
      userProfile: null,
      setUserProfile: (profile) => set({ userProfile: profile }),
      clearProfile: () => set({ userProfile: null }),
    }),
    {
      name: "profile",
    }
  )
);
