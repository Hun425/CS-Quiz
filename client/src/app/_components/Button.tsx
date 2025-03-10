"use client";
import React, { ButtonHTMLAttributes } from "react";
import "./button.css";

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

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
  type?: "button" | "submit" | "reset";
}

const Button: React.FC<ButtonProps> = ({
  children,
  variant = "primary",
  size = "medium",
  onClick,
  disabled = false,
  type = "button",
  ...rest
}) => {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`btn btn-${variant} btn-${size}`}
      {...rest}
    >
      {children}
    </button>
  );
};

export default Button;
