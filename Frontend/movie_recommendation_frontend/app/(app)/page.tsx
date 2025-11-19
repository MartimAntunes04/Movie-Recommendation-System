"use client";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { TbMovie, TbStar, TbTrendingUp, TbPlayerPlay, TbSparkles } from 'react-icons/tb';
import { useEffect, useState } from "react";
import { Movie, popularMovies, topRatedMovies } from "@/Services/API";
import { MovieCarousel } from "@/components/MovieCarousel";
import { useRouter } from "next/navigation";

function MovieSection({
  title,
  movies,
  loading,
  onMovieClick,
}: {
  title: string;
  movies: Movie[];
  loading: boolean;
  onMovieClick: (movie: Movie) => void;
}) {
  return (
    <div className="mb-12">
      <div className="flex items-center gap-3 mb-6">
        <TbTrendingUp className="text-3xl text-yellow-500" />
        <h2 className="text-3xl font-bold text-slate-900 dark:text-white">
          {title}
        </h2>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-20">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
            <p className="text-slate-400">Loading amazing movies...</p>
          </div>
        </div>
      ) : movies.length > 0 ? (
        <MovieCarousel movies={movies} onMovieClick={onMovieClick} />
      ) : (
        <div className="text-center py-20">
          <p className="text-slate-400">No movies found</p>
        </div>
      )}
    </div>
  );
}



export default function Home() {
  const router = useRouter();
  const [moviesPop, setMoviesPop] = useState<Movie[]>([]);
  const [moviesTop, setMoviesTop] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);

   useEffect(() => {
    const fetchMovies = async () => {
      setLoading(true);
      try {
        const pop = await popularMovies();
        setMoviesPop(pop.results || []);
        const top = await topRatedMovies();
        setMoviesTop(top.results || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, []);

  const handleMovieClick = (movie: Movie) => {
    router.push(`/movies/${movie.id}`);
  };



   return (
    <div className="min-h-screen">
      <main className="container mx-auto px-4 py-8">
        {/* Hero Section */}
        <div className="text-center mb-10">
          <h1 className="text-4xl md:text-6xl font-bold text-slate-900 dark:text-white mb-6">
            Discover the{" "}
            <span className="bg-linear-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
              best movies
            </span>
          </h1>
          <p className="text-xl text-slate-600 dark:text-slate-400 mb-8 max-w-2xl mx-auto">
            Find your next favorite film in seconds.
          </p>
        </div>

        {/* Sections */}
        <MovieSection
          title="Popular Movies"
          movies={moviesPop}
          loading={loading}
          onMovieClick={handleMovieClick}
        />

        <MovieSection
          title="Top Movies"
          movies={moviesTop}
          loading={loading}
          onMovieClick={handleMovieClick}
        />
      </main>
    </div>
  );
}