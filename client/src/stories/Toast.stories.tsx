import React from "react";
import { Meta, StoryFn } from "@storybook/react";
import { Toast } from "@/app/_components/Toast";

export default {
  title: "Components/Toast",
  component: Toast,
  argTypes: {
    type: {
      control: { type: "select" },
      options: ["success", "warning", "error", "info"],
    },
  },
} as Meta<typeof Toast>;

// 🔹 스토리 템플릿
const Template: StoryFn<typeof Toast> = (args) => <Toast {...args} />;

export const Success = Template.bind({});
Success.args = {
  message: "✅ 성공적으로 처리되었습니다!",
  type: "success",
} as ToastItem;

export const Warning = Template.bind({});
Warning.args = {
  message: "💦 오류가 발생했습니다!",
  type: "warning",
} as ToastItem;

export const Error = Template.bind({});
Error.args = {
  message: "🕳️ 오류가 발생했습니다!",
  type: "error",
} as ToastItem;

export const Info = Template.bind({});
Info.args = {
  message: "ℹ️ 정보 메시지입니다.",
  type: "info",
} as ToastItem;
