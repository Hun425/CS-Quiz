import CallbackClient from "./CallbackClient";
import Loading from "@/app/_components/Loading";
import { Suspense } from "react";

const page = () => {
  return (
    <Suspense fallback={<Loading />}>
      <CallbackClient />
    </Suspense>
  );
};

export default page;
