"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { getMovieById, Movie, addToWatchlist, removeFromWatchlist, isMovieInWatchlist, addToHistory, removeFromHistory, isMovieInHistory, checkRating, updateRating} from "@/Services/API";
import { TbStar, TbLibraryPlus, TbLibraryMinus, TbCalendar, TbClock } from "react-icons/tb";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";



export default function MovieDetailsPage() {
  const params = useParams();
  const id = params?.id as string;
  const [movie, setMovie] = useState<Movie | null>(null);
  const [loading, setLoading] = useState(true);
  const [watched, setWatched] = useState(false);
  const [watchedLoading, setWatchedLoading] = useState(false);
  const [watchlist, setWatchlist] = useState(false);
  const [watchlistLoading, setWatchlistLoading] = useState(false);
  const [userRating, setUserRating] = useState<number | null>(null); // 1..5 or null
  const [userHasRated, setUserHasRated] = useState<boolean>(false);
  const [ratingSubmitting, setRatingSubmitting] = useState<boolean>(false);
  const [dbRating, setDbRating] = useState<number | null>(null);

  useEffect(() => {
    if (!id) return;

    const fetchMovie = async () => {
      try {
        setLoading(true);
        const data = await getMovieById(id);
        setMovie(data);
        
        const inWatchlist = await isMovieInWatchlist(Number(id));
        setWatchlist(inWatchlist);

        const inHistory = await isMovieInHistory(Number(id));
        setWatched(inHistory);

        try {
          const rating = await checkRating(Number(id));
          if (rating !== null && rating !== undefined) {
            setUserRating(rating);
            setDbRating(rating);
            setUserHasRated(true);
          }
        } catch (err) {
          console.debug("checkRating falhou (pode ser que o utilizador não esteja logado):", err);
        }
      } catch (err) {
        console.error("Erro ao buscar filme:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchMovie();
  }, [id]);

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
    </div>
  );

  if (!movie) return (
    <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
      <p className="text-xl">Movie not found</p>
    </div>
  );

  const director = movie.credits?.crew?.find(person => person.job === "Director");
  const cast = movie.credits?.cast?.slice(0, 5) || [];

  const handleRatingSubmit = async () => {
    if (!movie) return;
    if (userHasRated) return;
    if (userRating === null) return;

    try {
      setRatingSubmitting(true);
      const res = await updateRating(movie.id, userRating);
      setDbRating(userRating);
      if (res && (res.success === true || res.message)) {
        setUserHasRated(true);
      } else {
        setUserHasRated(true);
      }
    } catch (err) {
      console.error('Erro ao enviar rating:', err);
    } finally {

      setRatingSubmitting(false);
    }
  };

  const handleRatingUpdate = async () => {
    if (!movie) return;
    if (userRating === null) return;

    try {
      setRatingSubmitting(true);
      const res = await updateRating(movie.id, userRating);
      const refreshedRating = await checkRating(movie.id);
      if (refreshedRating !== null && refreshedRating !== undefined) {
        setDbRating(refreshedRating); // atualiza o valor estático mostrado no texto
      }
      console.log("Update Rating Result:", res);
    } catch (err) {
      console.error("Erro ao atualizar rating:", err);
    } finally {
      setRatingSubmitting(false);
    }
  };


  const StarWidget = () => (
  <div className="flex gap-2 text-3xl items-center justify-center">
    {[1, 2, 3, 4, 5].map((value) => (
      <TbStar
        key={value}
        className={`transition ${
          (userRating ?? 0) >= value
            ? "text-yellow-400 fill-yellow-400"
            : "text-slate-500"
        } cursor-pointer hover:text-yellow-300`}
        onClick={() => {
          setUserRating(value);
        }}
      />
    ))}
  </div>
);

  return (
    <div className="min-h-screen bg-slate-900 text-white">
      {/* Hero Section with Backdrop */}
      <div className="relative h-[60vh] w-full overflow-hidden">
        {movie.backdrop_path ? (
          <>
            <div className="absolute inset-0 bg-linear-to-t from-slate-900 via-slate-900/60 to-transparent z-10" />
            <img
              src={`https://image.tmdb.org/t/p/original${movie.backdrop_path}`}
              alt={movie.title}
              className="w-full h-full object-cover"
            />
          </>
        ) : (
          <div className="w-full h-full bg-slate-800" />
        )}
      </div>

      <div className="container mx-auto px-4 -mt-32 relative z-20 pb-12">
        <div className="flex flex-col md:flex-row gap-8">
          {/* Poster Card */}
          <div className="shrink-0 mx-auto md:mx-0 flex flex-col gap-4">
            <Card className="w-64 md:w-80 border-0 shadow-2xl bg-slate-800 rounded-xl overflow-hidden">
              <CardContent className="p-0">
                <img
                  src={movie.poster_path 
                    ? `https://image.tmdb.org/t/p/w500${movie.poster_path}`
                    : "https://placehold.co/500x750?text=No+Poster"
                  }
                  alt={movie.title}
                  className="w-full h-auto"
                />
              </CardContent>
            </Card>
            {/* Watchlist and Watched Buttons */}
            <div className="flex gap-3 w-64 md:w-80">
              <Button
                onClick={async () => {
                  if (!movie) return;
                  setWatchedLoading(true);
                  try {
                    if (!watched) {
                      await addToHistory(movie.id);
                      setWatched(true);
                    } else {
                      await removeFromHistory(movie.id);
                      setWatched(false);
                    }
                  } catch (err) {
                    console.error('Erro ao atualizar histórico:', err);
                  } finally {
                    setWatchedLoading(false);
                  }
                }}
                disabled={watchedLoading}
                size="lg"
                className={`flex-1 gap-2 px-2 transition-all duration-300 ${
                  watched 
                    ? "bg-yellow-500 hover:bg-yellow-600 text-black" 
                    : "bg-slate-800 hover:bg-slate-700 text-white"
                }`}
                title={watched ? "Watched" : "Mark as Watched"}
              >
                {watchedLoading ? (
                  <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent" />
                ) : watched ? (
                  "Watched"
                ) : (
                  "Mark as Watched"
                )}
              </Button>

              <Button
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
                disabled={watchlistLoading}
                variant="default"
                size="lg"
                className={`flex-1 gap-2 px-2 bg-slate-800 hover:bg-slate-700 ${
                  watchlist ? "text-yellow-500 border-yellow-500/50" : "text-slate-200"
                }`}
                title={watchlist ? "Remove from Watchlist" : "Add to Watchlist"}
              >
                {watchlistLoading ? (
                  <div className="h-5 w-5 animate-spin rounded-full border-2 border-current border-t-transparent" />
                ) : watchlist ? (
                  <TbLibraryMinus className="text-xl" />
                ) : (
                  <TbLibraryPlus className="text-xl" />
                )}
                <span className="truncate">{watchlist ? "List" : "List"}</span>
              </Button>
            </div>

            <div className="mt-4 bg-slate-800 p-4 rounded-xl shadow-lg">
              <h3 className="text-lg font-semibold mb-2">Your Rating</h3>

              <StarWidget />

              {!userHasRated && (
                <Button
                  onClick={handleRatingSubmit}
                  disabled={ratingSubmitting || userRating === null}
                  className="w-full mt-3 bg-yellow-500 hover:bg-yellow-600 text-black"
                >
                  {ratingSubmitting ? "Saving..." : "Submit Rating"}
                </Button>
              )}

              {userHasRated && (
                <>
                  <p className="text-yellow-400 text-center mt-2">You already rated this film: {dbRating} ★</p>

                  <Button
                    onClick={handleRatingUpdate}
                    disabled={ratingSubmitting || userRating === null}
                    className="w-full mt-3 bg-yellow-500 hover:bg-yellow-600 text-black"
                  >
                    {ratingSubmitting ? "Updating..." : "Update Rating"}
                  </Button>
                </>
              )}
            </div>
          </div>

          {/* Details */}
          <div className="flex-1 space-y-6">
            <div>
              <h1 className="text-4xl md:text-5xl font-bold mb-2 text-white">{movie.title}</h1>
              {movie.original_title !== movie.title && (
                <p className="text-slate-400 text-lg italic">{movie.original_title}</p>
              )}
            </div>

            <div className="flex flex-wrap items-center gap-4 text-sm text-slate-300">
              {movie.release_date && (
                <div className="flex items-center gap-1">
                  <TbCalendar className="text-yellow-500 text-lg" />
                  <span>{new Date(movie.release_date).getFullYear()}</span>
                </div>
              )}
              {movie.vote_average > 0 && (
                <div className="flex items-center gap-1">
                  <TbStar className="text-yellow-500 text-lg fill-yellow-500" />
                  <span>{movie.vote_average.toFixed(1)}</span>
                </div>
              )}
            </div>

            <div className="flex flex-wrap gap-2">
              {movie.genres?.map((g) => (
                <Badge key={g.id} className="bg-slate-800 text-slate-200 hover:bg-slate-700">
                  {g.name}
                </Badge>
              ))}
            </div>

            {director && (
              <div className="py-2">
                <span className="text-slate-400 block text-sm mb-1">Director</span>
                <span className="text-lg font-semibold text-white">{director.name}</span>
              </div>
            )}

            <div>
              <h3 className="text-xl font-semibold mb-2 text-white">Overview</h3>
              <p className="text-slate-300 leading-relaxed text-lg">{movie.overview}</p>
            </div>

            {/* Cast Grid */}
            {cast.length > 0 && (
              <div>
                <h3 className="text-xl font-semibold mb-4 text-white">Top Cast</h3>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4">
                  {cast.map((actor) => (
                    <div key={actor.id} className="text-center">
                      <div className="w-20 h-20 md:w-24 md:h-24 mx-auto mb-2 rounded-full overflow-hidden border-2 border-slate-700 bg-slate-800">
                        {actor.profile_path ? (
                          <img
                            src={`https://image.tmdb.org/t/p/w185${actor.profile_path}`}
                            alt={actor.name}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-slate-500 text-xs">
                            No Image
                          </div>
                        )}
                      </div>
                      <p className="font-medium text-sm text-white truncate">{actor.name}</p>
                      <p className="text-xs text-slate-400 truncate">{actor.character}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
