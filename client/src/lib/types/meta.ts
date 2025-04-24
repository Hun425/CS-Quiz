export type TwitterCardType =
  | "summary"
  | "summary_large_image"
  | "app"
  | "player";

export interface IMetadata {
  title?:
    | string
    | {
        default: string;
        template: string;
      };
  description?: string;
  openGraph?: {
    title?: string;
    description?: string;
    url?: string;
    siteName?: string;
    images?: {
      url: string;
      width?: number;
      height?: number;
      alt?: string;
    }[];
    locale?: string;
    type?: string;
  };
  twitter?: {
    card?: TwitterCardType;
    title?: string;
    description?: string;
    images?: string[]; // twitter는 string[] 형식
  };
}
