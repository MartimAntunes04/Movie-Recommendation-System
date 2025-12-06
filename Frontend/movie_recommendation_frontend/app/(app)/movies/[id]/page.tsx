
"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getMovieById, Movie, addToWatchlist, removeFromWatchlist, isMovieInWatchlist } from "@/Services/API";
import { TbStar, TbLibraryPlus, TbLibraryMinus } from "react-icons/tb";
import { Button } from "@/components/ui/button";
import {Tooltip, TooltipContent, TooltipTrigger,} from "@/components/ui/tooltip";

export default function MovieDetailsPage() {
  const params = useParams();
  const id = params?.id as string; // captura o id da URL
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);
  const [watched, setWatched] = useState(false);
  const [watchlist, setWatchlist] = useState(false);
  const [watchlistLoading, setWatchlistLoading] = useState(false);

  useEffect(() => {
    if (!id) return;

    const fetchMovie = async () => {
      try {
        console.log;
        setLoading(true);
        const data = await getMovieById(id);
        setMovie(data);
        
        // Check if movie is already in watchlist
        const inWatchlist = await isMovieInWatchlist(Number(id));
        setWatchlist(inWatchlist);
      } catch (err) {
        console.error("Erro ao buscar filme:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchMovie();
  }, [id]);

  if (loading) return <p className="text-center py-20">Carregando filme...</p>;
  if (!movie) return <p className="text-center py-20">Filme não encontrado</p>;

  return (
    <div className="min-h-screen bg-slate-900 text-white p-8">
      <div className="flex flex-col md:flex-row gap-8">
        <img
          src={`https://image.tmdb.org/t/p/w500${movie.poster_path}`}
          alt={movie.title}
          className="w-full md:w-1/3 rounded-lg"
        />
        <div className="flex-1">
          <h1 className="text-3xl font-bold mb-4">{movie.title}</h1>
          <p className="text-slate-400 mb-4">{movie.overview}</p>
          <p className="text-sm text-slate-500">
            <strong>Genres:</strong>{" "}
            {movie.genres?.map((g) => g.name).join(", ")}
          </p>
          <p className="text-sm text-slate-500">
            <strong>Release Date:</strong> {movie.release_date}
          </p>
          <p className="text-sm text-slate-500 flex items-center gap-1">
            <strong>Rating:</strong>{" "}
            {movie.vote_average !== undefined && movie.vote_average !== null
              ? movie.vote_average.toFixed(1)
              : "N/A"}
            <TbStar className="text-white-400" />
          </p>

          <div className="mt-6 flex items-center gap-4">
            {/* Botão Watched */}
            <Button
              onClick={() => setWatched(!watched)}
              className={watched ? "bg-green-600" : ""}
            >
              {watched ? "Watched" : "Mark as Watched"}
            </Button>

            {/* Botão Biblioteca com Tooltip */}
            <Tooltip>
              <TooltipTrigger asChild>
                <button
                  onClick={async () => {
                    if (!movie) return;
                    setWatchlistLoading(true);
                    try {
                      if (!watchlist) {
                        await addToWatchlist(movie.id);
                        setWatchlist(true);
                      } else {
                        await removeFromWatchlist(movie.id);
                        setWatchlist(false);
                      }
                    } catch (err) {
                      console.error('Erro ao atualizar watchlist:', err);
                    } finally {
                      setWatchlistLoading(false);
                    }
                  }}
                  className={`cursor-pointer transition-colors duration-300 p-2 rounded-md hover:bg-slate-800 ${
                    watchlist ? "text-yellow-500" : "text-gray-400"
                  }`}
                  aria-label={watchlist ? "Remove from watchlist" : "Add to watchlist"}
                  disabled={watchlistLoading}
                >
                  {watchlistLoading ? (
                    <div className="inline-block animate-spin rounded-full h-6 w-6 border-2 border-slate-400 border-t-yellow-500"></div>
                  ) : watchlist ? (
                    <TbLibraryMinus size={32} />
                  ) : (
                    <TbLibraryPlus size={32} />
                  )}
                </button>
              </TooltipTrigger>
              <TooltipContent>
                <p>{watchlist? "Remove from watchlist" : "Add to watchlist"}</p>
              </TooltipContent>
            </Tooltip>
          </div>
        </div>
      </div>
    </div>
  );
}