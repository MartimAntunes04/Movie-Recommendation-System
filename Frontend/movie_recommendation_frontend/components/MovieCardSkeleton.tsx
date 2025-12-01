"use client";

import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

interface MovieCardSkeletonProps {
  size?: "small" | "medium" | "large";
}

export function MovieCardSkeleton({ size = "small" }: MovieCardSkeletonProps) {
  const sizeConfig = {
    small: { padding: "p-1.5", titleHeight: "h-3", ratingHeight: "h-2.5" },
    medium: { padding: "p-3", titleHeight: "h-4", ratingHeight: "h-3" },
    large: { padding: "p-4", titleHeight: "h-5", ratingHeight: "h-4" },
  };

  const config = sizeConfig[size];

  return (
    <Card className="border-0 bg-slate-800/50 overflow-hidden h-full">
      <CardContent className="p-0 h-full flex flex-col">
        {/* Poster Skeleton */}
        <Skeleton className="aspect-2/3 w-full rounded-t-lg rounded-b-none bg-slate-700" />

        {/* Info Skeleton */}
        <div className={config.padding}>
          <Skeleton className={`${config.titleHeight} w-3/4 mb-2 bg-slate-700`} />
          <div className="flex items-center justify-between">
            <Skeleton className={`${config.ratingHeight} w-12 bg-slate-700`} />
            <Skeleton className={`${config.ratingHeight} w-10 bg-slate-700`} />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}