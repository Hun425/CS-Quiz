import CallbackClient from "./CallbackClient";
import Loading from "@/app/_components/Loading";
import { Suspense } from "react";

const page = () => {
  console.log("🔵 CallbackPage: 로그인 응답 처리 중...");
  return (
    <Suspense fallback={<Loading />}>
      <CallbackClient />
    </Suspense>
  );
};

export default page;
