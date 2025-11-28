"use client";

import {
  Carousel,
  CarouselContent,
  CarouselItem,
} from "@/components/ui/carousel";
import { MovieCardSkeleton } from "@/components/MovieCardSkeleton";

interface MovieCarouselSkeletonProps {
  size?: "small" | "medium" | "large";
  count?: number;
}

export function MovieCarouselSkeleton({ 
  size = "small", 
  count = 5 
}: MovieCarouselSkeletonProps) {
  return (
    <Carousel
      opts={{
        align: "start",
        loop: false,
      }}
      className="w-full"
    >
      <CarouselContent className="-ml-2 md:-ml-4">
        {Array.from({ length: count }).map((_, index) => (
          <CarouselItem
            key={index}
            className="pl-2 md:pl-4 basis-1/2 sm:basis-1/3 md:basis-1/4 lg:basis-1/5"
          >
            <MovieCardSkeleton size={size} />
          </CarouselItem>
        ))}
      </CarouselContent>
    </Carousel>
  );
}