"use client";

import { useState, useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import httpClient from "../api/httpClient";

const useTokenExpirationWarning = () => {
  const [showPopup, setShowPopup] = useState(false);
  const { isAuthenticated, token, refreshToken, expiresAt, setToken, logout } =
    useAuthStore();

  useEffect(() => {
    if (!token || !expiresAt || isAuthenticated) {
      setShowPopup(false);
      return;
    }

    const checkExpiration = () => {
      const now = Date.now();
      const remainingTime = expiresAt - now;

      if (remainingTime < 5 * 60 * 1000 && remainingTime > 0) {
        setShowPopup(true);
      } else {
        setShowPopup(false);
      }
    };

    checkExpiration();
    const interval = setInterval(checkExpiration, 1000 * 30);
    return () => clearInterval(interval);
  }, [token, expiresAt, isAuthenticated]);

  const handleRefreshToken = async () => {
    try {
      if (!refreshToken) return;

      const response = await httpClient.post("/oauth2/refresh", {
        refreshToken,
      });

      if (response.data?.accessToken) {
        const newExpiresAt = Date.now() + response.data.expiresIn * 1000;

        setToken(
          response.data.accessToken,
          response.data.refreshToken,
          newExpiresAt
        );

        setShowPopup(false);
      } else {
        logout(); // 🔴 응답에 accessToken이 없을 경우 강제 로그아웃
      }
    } catch {
      logout(); // 🔴 요청 실패 시 강제 로그아웃
    }
  };

  return { showPopup, handleRefreshToken };
};

export default useTokenExpirationWarning;
