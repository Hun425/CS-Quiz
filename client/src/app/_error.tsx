import type { NextPage, NextPageContext } from "next";

type Props = {
  statusCode?: number;
};

const Error: NextPage<Props> = ({ statusCode }) => {
  return (
    <div>
      {statusCode
        ? `${statusCode} - 서버에서 문제가 발생했습니다.`
        : "클라이언트에서 문제가 발생했습니다."}
    </div>
  );
};

Error.getInitialProps = ({ res, err }: NextPageContext) => {
  const statusCode = res ? res.statusCode : err ? err.statusCode : 404;
  return { statusCode };
};

export default Error;
