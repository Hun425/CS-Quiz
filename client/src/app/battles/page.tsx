import { Suspense } from "react";
import BattlesPage from "./BattlesPage";
import { METADATA } from "@/lib/constants/seo";
import { genPageMetadata } from "@/lib/utils/metadata";
import Loading from "../_components/Loading";

export const metadata = genPageMetadata(METADATA.battles);

export default function Page() {
  return (
    <Suspense fallback={<Loading />}>
      <BattlesPage />
    </Suspense>
  );
}
