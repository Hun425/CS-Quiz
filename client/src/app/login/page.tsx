import LoginClient from "./LoginClient";
import Loading from "../_components/Loading";
import { Suspense } from "react";

const page = () => {
  return (
    <Suspense fallback={<Loading />}>
      <LoginClient />
    </Suspense>
  );
};

export default page;
