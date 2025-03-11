import type { NextPage } from "next";

type Props = {
  error500Props: string;
};

const Custom500: NextPage<Props> = ({ error500Props }) => {
  // 해당 페이지에서 getStaticProps props로 받아온 데이터를 사용할 수 있습니다.
  return (
    <>
      <div> 500 - {error500Props}</div>
    </>
  );
};

export async function getStaticProps() {
  // 서버에서 데이터를 미리 받아옵니다.
  // 아래 props는 서버쪽에서 데이터를 받아왔다는 가정하에 props 데이터를 넘겨줍니다.
  return {
    props: { error500Props: "서버에서 문제가 발생했습니다." },
  };
}

export default Custom500;
