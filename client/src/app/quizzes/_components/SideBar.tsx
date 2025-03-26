import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import { useProfileStore } from "@/store/profileStore";
import { useToastStore } from "@/store/toastStore";
import { useGetDailyQuizzes } from "@/lib/api/quiz/useGetDailyQuizzes";
import { useGetRecommendedQuizzes } from "@/lib/api/quiz/useGetRecommendedQuizzes";
import Image from "next/image";
import Progress from "@/app/_components/Progress";
import Link from "next/link";
import Button from "@/app/_components/Button";

const Sidebar: React.FC = () => {
  const router = useRouter();
  const { showToast } = useToastStore();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const userProfile = useProfileStore((state) => state.userProfile);

  // ✅ 오늘의 퀴즈 데이터 가져오기
  const { data: dailyQuiz, isLoading: isLoadingDaily } = useGetDailyQuizzes();

  // ✅ 추천 퀴즈 데이터 가져오기
  const { data: recommendedQuizzes, isLoading: isLoadingRecommended } =
    useGetRecommendedQuizzes({ limit: 3 });

  useEffect(() => {
    if (isAuthenticated && !userProfile) {
      showToast(
        "사용자 정보를 찾을 수 없습니다. 고객센터에 문의하세요.",
        "error"
      );
      router.push("/");
    }
  }, [isAuthenticated, userProfile, router, showToast]);

  if (!isAuthenticated) {
    return (
      <aside className="hidden lg:block w-1/4 bg-background border border-border p-6 rounded-md">
        <h2 className="text-lg font-semibold mb-4 text-primary">
          📢 로그인하고 연습을 시작하세요!
        </h2>
        <Button
          variant="primary"
          className="w-full text-white"
          onClick={() => router.push("/login")}
        >
          로그인
        </Button>

        {/* 로그인 혜택 설명 */}
        <p className="text-sm text-muted mt-3">
          🔥 로그인하면 나에게 맞춘{" "}
          <span className="font-semibold text-primary">추천 퀴즈</span>를 받을
          수 있어요.
          <br />
          📊 내 학습량을 분석하고{" "}
          <span className="font-semibold text-primary">취약한 개념</span>을
          보완해 보세요!
        </p>

        <h3 className="text-md font-semibold mt-6 mb-3">📚 추천 코스</h3>
        <ul>
          <li>🔹 백엔드 개발</li>
          <li>🔹 자바 중급</li>
          <li>🔹 데이터 엔지니어링</li>
        </ul>

        <h3 className="text-md font-semibold mt-6 mb-3">💼 추천 포지션</h3>
        <ul>
          <li>💼 미들급 백엔드 개발자</li>
          <li>💼 웹 프론트엔드/백엔드 개발자</li>
        </ul>
      </aside>
    );
  }

  if (!userProfile) return null; // 프로필 없으면 토스트 후 리다이렉트

  // 🔹 경험치 진행률 계산
  const expPercentage = Math.min(
    (userProfile.experience / userProfile.requiredExperience) * 100,
    100
  );

  return (
    <aside className="hidden lg:block w-1/4 bg-background border border-border p-6 rounded-md shadow-sm">
      {/* 🔹 사용자 프로필 정보 */}
      <section className="space-y-4 mb-6">
        <div className="flex items-center space-x-4">
          {userProfile.profileImage ? (
            <Image
              src={userProfile.profileImage}
              alt="프로필 이미지"
              width={50}
              height={50}
              className="rounded-full border border-border"
            />
          ) : (
            <div className="w-12 h-12 bg-muted rounded-full flex items-center justify-center text-lg font-semibold">
              {userProfile.username.charAt(0)}
            </div>
          )}
          <div>
            <p className="text-lg font-semibold text-primary">
              {userProfile.username}님 <br></br> 환영합니다!
            </p>
            <p className="text-sm text-muted">Lv. {userProfile.level}</p>
          </div>
        </div>
        {/* 🔹 경험치 진행 바 */}
        <div className="mt-4">
          <p className="text-xs text-muted">
            경험치: {userProfile.experience} / {userProfile.requiredExperience}
          </p>
          <Progress value={expPercentage} />
        </div>

        {/* 🔹 총 포인트 & 마지막 로그인 정보 */}
        <div className="mt-4 text-sm text-foreground">
          <p>🌟 총 포인트: {userProfile.totalPoints.toLocaleString()} P</p>
          <p>
            ⏳ 마지막 로그인:{" "}
            {new Date(userProfile.lastLogin).toLocaleDateString()}
          </p>
        </div>
      </section>

      {/* 🔹 퀴즈 추천 섹션 */}
      <section className="mt-6 space-y-4">
        <div className="space-y-4">
          {/* ✅ 오늘의 퀴즈 카드 */}
          <div className="bg-card border border-border p-4 rounded-lg ">
            <h3 className="text-md font-semibold mb-2">📅 오늘의 퀴즈</h3>
            {isLoadingDaily ? (
              <p className="text-sm text-muted">불러오는 중...</p>
            ) : dailyQuiz?.data ? (
              <Link
                href={`/quiz/daily/${dailyQuiz.data.id}`}
                className="block text-md font-semibold bg-primary text-white p-2 rounded-md text-center hover:bg-primary/90 transition"
              >
                {dailyQuiz.data.title} 🚀
              </Link>
            ) : (
              <p className="text-sm text-muted">오늘의 퀴즈가 없습니다.</p>
            )}
          </div>

          {/* ✅ 추천 퀴즈 카드 */}
          <div className="bg-card border border-border p-4 rounded-lg ">
            <h3 className="text-md font-semibold mb-2">🌟 추천 퀴즈</h3>
            {isLoadingRecommended ? (
              <p className="text-sm text-muted">불러오는 중...</p>
            ) : recommendedQuizzes?.data?.length ? (
              <div className="space-y-2">
                {recommendedQuizzes.data.map((quiz) => (
                  <Link
                    key={quiz.id}
                    href={`/quiz/recommended/${quiz.id}`}
                    className="block text-sm bg-secondary text-white p-2 rounded-md hover:bg-secondary/90 transition"
                  >
                    {quiz.title}
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted">추천 퀴즈가 없습니다.</p>
            )}
          </div>
        </div>
      </section>
    </aside>
  );
};

export default Sidebar;
