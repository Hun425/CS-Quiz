import type { Meta, StoryObj } from "@storybook/react";
import Button from "@/app/_components/Button";
import "@/styles/globals.css";

const meta: Meta<typeof Button> = {
  title: "Components/Button",
  component: Button,
  argTypes: {
    variant: {
      control: "select",
      options: [
        "primary",
        "secondary",
        "danger",
        "outline",
        "success",
        "warning",
        "ghost",
        "rounded",
        "icon",
      ],
    },
    size: {
      control: "select",
      options: ["small", "medium", "large"],
    },
    disabled: { control: "boolean" },
    className: { control: "text" },
    onClick: { action: "clicked" },
  },
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof Button>;

/** ✅ 기본 Primary 버튼 */
export const Default: Story = {
  args: {
    variant: "primary",
    size: "medium",
    children: "Primary Button",
  },
};

/** ✅ Secondary 버튼 */
export const Secondary: Story = {
  args: {
    variant: "secondary",
    size: "medium",
    children: "Secondary Button",
  },
};

/** ✅ Danger 버튼 */
export const Danger: Story = {
  args: {
    variant: "danger",
    size: "medium",
    children: "Danger Button",
  },
};

/** ✅ Outline 버튼 */
export const Outline: Story = {
  args: {
    variant: "outline",
    size: "medium",
    children: "Outline Button",
  },
};

/** ✅ Success 버튼 */
export const Success: Story = {
  args: {
    variant: "success",
    size: "medium",
    children: "Success Button",
  },
};

/** ✅ Warning 버튼 */
export const Warning: Story = {
  args: {
    variant: "warning",
    size: "medium",
    children: "Warning Button",
  },
};

/** ✅ Ghost 버튼 */
export const Ghost: Story = {
  args: {
    variant: "ghost",
    size: "medium",
    children: "Ghost Button",
  },
};

/** ✅ Rounded 버튼 */
export const Rounded: Story = {
  args: {
    variant: "rounded",
    size: "medium",
    children: "Rounded Button",
  },
};

/** ✅ Icon 버튼 */
export const Icon: Story = {
  args: {
    variant: "icon",
    size: "small",
    children: "🔔",
  },
};

/** ✅ Disabled 버튼 */
export const Disabled: Story = {
  args: {
    variant: "primary",
    size: "medium",
    children: "Disabled Button",
    disabled: true,
  },
};

/** ✅ Large Primary 버튼 */
export const LargePrimary: Story = {
  args: {
    variant: "primary",
    size: "large",
    children: "Large Primary Button",
  },
};

/** ✅ Small Secondary 버튼 */
export const SmallSecondary: Story = {
  args: {
    variant: "secondary",
    size: "small",
    children: "Small Secondary Button",
  },
};
