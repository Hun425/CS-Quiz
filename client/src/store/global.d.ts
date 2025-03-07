//유저 정보를 담을 인터페이스
interface User {
  id: number;
  username: string;
  email: string;
  profileImage?: string;
  level: number;
}
