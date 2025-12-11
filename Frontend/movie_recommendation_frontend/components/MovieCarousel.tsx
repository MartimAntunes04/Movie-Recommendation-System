"use client";

import { Movie } from "@/Services/API";
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel";
import { MovieCard } from "@/components/MovieCard";
import Autoplay from "embla-carousel-autoplay";
import React from "react";

interface MovieCarouselProps {
  movies: Movie[];
  size?: "small" | "medium" | "large";
  onMovieClick?: (movie: Movie) => void;
}

export function MovieCarousel({ movies, size = "small", onMovieClick }: MovieCarouselProps) {
  const plugin = React.useRef(
    Autoplay({ 
      delay: 3000,
      stopOnInteraction: true,
      stopOnMouseEnter: false,
      stopOnFocusIn: true,
      playOnInit: true,
    })
  );

  return (
    <Carousel
      plugins={[plugin.current]}
      opts={{
        align: "start",
        loop: true,
        slidesToScroll: 1,
      }}
      className="w-full"
      onMouseEnter={plugin.current.stop}
      onMouseLeave={plugin.current.reset}
    >
      <CarouselContent className="-ml-2 md:-ml-4">
        {movies.map((movie) => (
          <CarouselItem
            key={movie.id}
            className="pl-2 md:pl-4 basis-1/2 sm:basis-1/3 md:basis-1/4 lg:basis-1/5"
          >
            <MovieCard 
              movie={movie} 
              size={size}
              onClick={() => onMovieClick?.(movie)}
            />
          </CarouselItem>
        ))}
      </CarouselContent>
      
      <CarouselPrevious className="hidden sm:flex -left-4 bg-slate-800/90 hover:bg-slate-700 border-slate-700 text-white" />
      <CarouselNext className="hidden sm:flex -right-4 bg-slate-800/90 hover:bg-slate-700 border-slate-700 text-white" />
    </Carousel>
  );
}