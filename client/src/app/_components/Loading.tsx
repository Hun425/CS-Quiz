"use client";

import { Player } from "@lottiefiles/react-lottie-player";

const Loading = () => (
  <div className="min-h-screen flex items-center justify-center bg-background">
    <Player
      autoplay
      loop
      src="/lottie/loading.json"
      style={{ height: "200px", width: "200px" }}
    />
  </div>
);

export default Loading;
