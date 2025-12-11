"use client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { TbMovie, TbStar, TbTrendingUp, TbPlayerPlay } from 'react-icons/tb';
import { FormEvent, useState, useEffect } from "react";
import { searchMovies } from "@/Services/API";
import { Movie } from "@/Services/API";
import { useSearchParams } from "next/navigation";
import { useRouter } from "next/navigation";
import { MovieCard } from "@/components/MovieCard";
import { MovieCardSkeleton } from "@/components/MovieCardSkeleton";

export default function SearchContent() {
  const searchParams = useSearchParams();
  const query = searchParams.get("query") || "";
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const fetchMovies = async () => {
      if (!query) return;
      setLoading(true);

      try {
        const data = await searchMovies(query);
        setMovies(data.results || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, [query]);

  const handleMovieClick = (movie: Movie) => {
    router.push(`/movies/${movie.id}`);
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white">
      <main className="container mx-auto px-4 py-8">
        <h2 className="text-2xl font-semibold mb-6 text-white">
          Results for: <span className="text-yellow-500">{query}</span>
        </h2>

        {loading && (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {Array.from({ length: 20 }).map((_, idx) => (
              <MovieCardSkeleton key={idx} />
            ))}
          </div>
        )}

        {!loading && movies.length > 0 && (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
            {movies.map((movie) => (
              <MovieCard
                key={movie.id}
                movie={movie}
                size="small"
                showOverlay={true}
                onClick={() => handleMovieClick(movie)}
              />
            ))}
          </div>
        )}

        {!loading && movies.length === 0 && query && (
          <div className="text-center py-20">
            <p className="text-slate-400 text-lg">No movies found for "{query}"</p>
          </div>
        )}

        {!loading && !query && (
          <div className="text-center py-20">
            <p className="text-slate-400 text-lg">Enter a search query to find movies</p>
          </div>
        )}
      </main>
    </div>
  );
}