"use client";

import { useState, useEffect } from "react";
import { useAuthStore } from "@/store/authStore";
import httpClient from "../api/httpClient";

const useTokenExpirationWarning = () => {
  const [showPopup, setShowPopup] = useState(false);
  const { token, expiresAt, setToken } = useAuthStore();

  useEffect(() => {
    if (!token || !expiresAt) return;

    const checkExpiration = () => {
      const now = Date.now();
      const remainingTime = expiresAt - now;

      if (remainingTime < 5 * 60 * 1000 && remainingTime > 0) {
        // 5분 이내면 팝업 표시
        setShowPopup(true);
      }
    };

    const interval = setInterval(checkExpiration, 1000 * 30); // 30초마다 체크
    return () => clearInterval(interval);
  }, [token, expiresAt]);

  const handleRefreshToken = async () => {
    try {
      const refreshToken = localStorage.getItem("refresh_token");
      const response = await httpClient.post("/oauth2/refresh", {
        refreshToken,
      });

      if (response.data?.accessToken) {
        setToken(
          response.data.accessToken,
          response.data.refreshToken,
          Date.now() + response.data.expiresIn * 1000
        );

        localStorage.setItem("access_token", response.data.accessToken);
        localStorage.setItem("refresh_token", response.data.refreshToken);
        localStorage.setItem(
          "expires_in",
          (Date.now() + response.data.expiresIn * 1000).toString()
        );

        setShowPopup(false);
      }
    } catch (error) {
      console.error("토큰 갱신 실패:", error);
    }
  };

  return { showPopup, handleRefreshToken };
};

export default useTokenExpirationWarning;
