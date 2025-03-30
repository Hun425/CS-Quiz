import { IMetadata } from "../types/meta";
import { METADATA } from "../constants/seo";

export function genPageMetadata({ title, description, openGraph }: IMetadata) {
  const base = METADATA.home;

  return {
    title: title || base.title,
    description: description || base.description,
    openGraph: {
      ...base.openGraph,
      ...openGraph,
      title: openGraph?.title || base.openGraph.title,
      description: openGraph?.description || base.openGraph.description,
      images: openGraph?.images || base.openGraph.images,
    },
  };
}
