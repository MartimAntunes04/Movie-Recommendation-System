"use client";

import { Movie } from "@/Services/API";
import { Card, CardContent } from "@/components/ui/card";
import { TbStar } from "react-icons/tb";

interface MovieCardProps {
  movie: Movie;
  onClick?: () => void;
  size?: "small" | "medium" | "large";
  showOverlay?: boolean;
}

export function MovieCard({ 
  movie, 
  onClick, 
  size = "small",
  showOverlay = true 
}: MovieCardProps) {
  
  // Configurações de tamanho
  const sizeConfig = {
    small: {
      imageUrl: "w185",
      iconSize: "text-2xl",
      titleSize: "text-[11px]",
      ratingSize: "text-[10px]",
      padding: "p-1.5",
      overlayPadding: "p-2",
      overlayTextSize: "text-[10px]",
    },
    medium: {
      imageUrl: "w342",
      iconSize: "text-4xl",
      titleSize: "text-sm",
      ratingSize: "text-xs",
      padding: "p-3",
      overlayPadding: "p-3",
      overlayTextSize: "text-xs",
    },
    large: {
      imageUrl: "w500",
      iconSize: "text-5xl",
      titleSize: "text-base",
      ratingSize: "text-sm",
      padding: "p-4",
      overlayPadding: "p-4",
      overlayTextSize: "text-sm",
    },
  };

  const config = sizeConfig[size];

  return (
    <Card 
      className="border-0 bg-slate-800/50 hover:bg-slate-700/50 transition-all duration-300 hover:scale-105 cursor-pointer overflow-hidden group h-full"
      onClick={onClick}
    >
      <CardContent className="p-0 h-full flex flex-col">
        <div className="relative aspect-2/3">
          {movie.poster_path ? (
            <img
              src={`https://image.tmdb.org/t/p/${config.imageUrl}${movie.poster_path}`}
              alt={movie.title}
              className="w-full h-full object-cover rounded-t-lg"
              loading="lazy"
            />
          ) : (
            <div className="w-full h-full bg-slate-700 flex items-center justify-center text-slate-400">
              <TbStar className={config.iconSize} />
            </div>
          )}
          
          {/* Overlay com informações ao hover */}
          {showOverlay && (
            <div className={`absolute inset-0 bg-linear-to-t from-black/90 via-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col justify-end ${config.overlayPadding}`}>
              <p className={`text-white ${config.overlayTextSize} line-clamp-2 mb-2`}>
                {movie.overview || "No description available"}
              </p>
              <div className="flex items-center gap-1 text-yellow-400">
                <TbStar className={config.overlayTextSize} />
                <span className={`${config.overlayTextSize} font-semibold`}>
                  {movie.vote_average.toFixed(1)}
                </span>
                <span className={`${config.overlayTextSize} text-slate-400`}>
                  ({movie.vote_count})
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Título fixo abaixo da imagem */}
        <div className={config.padding}>
          <h3 className={`font-semibold ${config.titleSize} text-white truncate mb-1`}>
            {movie.title}
          </h3>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1 text-yellow-400">
              <TbStar className={config.ratingSize} />
              <span className={`${config.ratingSize} font-small`}>
                {movie.vote_average.toFixed(1)}
              </span>
            </div>
            <span className={`${config.ratingSize} text-slate-400`}>
              {movie.release_date 
                ? new Date(movie.release_date).getFullYear() 
                : 'N/A'}
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}