import { METADATA } from "@/lib/constants/seo";
import { genPageMetadata } from "@/lib/utils/metadata";

export const metadata = genPageMetadata(METADATA.quizList);

export default function QuizzesLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <>{children}</>;
}
