export const useRouter = () => ({
  push: (url: string) => {
    console.log("[MockRouter] push to:", url);
  },
});
