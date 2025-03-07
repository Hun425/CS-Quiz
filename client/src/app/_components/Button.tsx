"use client";
import React, { ButtonHTMLAttributes } from "react";

type ButtonVariant =
  | "primary"
  | "secondary"
  | "danger"
  | "outline"
  | "success"
  | "warning";

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
  className = "",
  type = "button",
  ...rest
}) => {
  const baseStyles =
    "font-medium rounded-lg shadow-md focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed";

  const variantStyles = {
    primary: "bg-primary text-white hover:bg-primary-hover hover:scale-105",
    secondary:
      "bg-secondary text-white hover:bg-secondary-hover hover:scale-105",
    danger: "bg-danger text-white hover:bg-danger-light hover:scale-105",
    success: "bg-success text-white hover:bg-success-light hover:scale-105",
    warning: "bg-warning text-white hover:bg-warning-light hover:scale-105",
    outline:
      "border border-primary text-primary hover:bg-primary hover:text-white hover:scale-105",
    ghost: "text-neutral hover:bg-gray-100 hover:scale-105",
    rounded:
      "bg-primary text-white rounded-full px-6 py-3 hover:bg-primary-hover hover:scale-110",
    icon: "p-2 rounded-full hover:bg-gray-200 transition-all",
  };

  const sizeStyles = {
    small: "px-3 py-1 text-sm",
    medium: "px-4 py-2 text-base",
    large: "px-6 py-3 text-lg",
  };

  const combinedClasses = `${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${className}`;

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={combinedClasses}
      {...rest}
    >
      {children}
    </button>
  );
};

export default Button;
