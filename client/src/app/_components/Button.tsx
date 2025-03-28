"use client";

import React, { ButtonHTMLAttributes } from "react";
import classNames from "classnames";

type ButtonVariant =
  | "primary"
  | "secondary"
  | "danger"
  | "outline"
  | "success"
  | "warning"
  | "ghost"
  | "rounded"
  | "icon";

type ButtonSize = "small" | "medium" | "large";

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  onClick?: React.MouseEventHandler<HTMLButtonElement>;
  disabled?: boolean;
  className?: string;
  type?: "button" | "submit" | "reset";
}

const colorVariants: Record<ButtonVariant, string> = {
  primary: "bg-primary text-white hover:bg-primary-hover",
  secondary: "bg-secondary text-foreground hover:bg-secondary-hover",
  danger: "bg-danger text-foreground hover:bg-danger-light",
  outline:
    "border border-primary text-primary hover:bg-primary hover:text-white",
  success: "bg-success text-foreground hover:bg-success-light",
  warning: "bg-warning text-foreground hover:bg-warning-light",
  ghost: "text-muted bg-transparent hover:bg-gray-200 bg-primay",
  rounded: "rounded-full",
  icon: "p-2 rounded-full hover:bg-gray-200",
};

const sizeVariants: Record<ButtonSize, string> = {
  small: "px-3 py-1 text-sm",
  medium: "px-4 py-2 text-base",
  large: "px-6 py-3 text-lg",
};

const Button: React.FC<ButtonProps> = ({
  children,
  variant = "primary",
  size = "medium",
  className = "",
  onClick,
  disabled = false,
  type = "button",
  ...rest
}) => {
  const finalClassName = classNames(
    "btn font-semibold rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 disabled:opacity-50 disabled:cursor-not-allowed",
    colorVariants[variant],
    sizeVariants[size],
    className
  );

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={finalClassName}
      {...rest}
    >
      {children}
    </button>
  );
};

export default Button;
