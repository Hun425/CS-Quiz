import type { Preview, StoryFn } from "@storybook/react";
import React from "react";
import "@/styles/globals.css";
import { useDarkMode } from "storybook-dark-mode";

const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
  },
  decorators: [
    (Story: StoryFn) => {
      const isDark = useDarkMode();
      document.body.style.margin = "0";
      document.body.style.padding = "0";
      document.body.style.boxSizing = "border-box";
      document.body.style.backgroundColor = "var(--theme-color-background)";

      return (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100vh",
            boxSizing: "border-box",
            backgroundColor: "var(--theme-color-background)",
          }}
          data-theme={isDark ? "dark" : "light"}
        >
          <Story />
        </div>
      );
    },
  ],
};

export default preview;
