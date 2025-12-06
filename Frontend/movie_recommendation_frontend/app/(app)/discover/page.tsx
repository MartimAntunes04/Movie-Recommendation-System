"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { TbArrowLeft, TbFilter, TbSparkles, TbX } from "react-icons/tb";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Slider } from "@/components/ui/slider";
import { MovieCard } from "@/components/MovieCard";
import { MovieCardSkeleton } from "@/components/MovieCardSkeleton";
import { filteredSearch, FilteredSearchParams, Movie } from "@/Services/API";

const GENRES = [
  "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
  "Drama", "Family", "Fantasy", "History", "Horror", "Music", "Mystery",
  "Romance", "Science Fiction", "Thriller", "War", "Western"
];

function parseFilters(params: URLSearchParams): FilteredSearchParams {
  const filters: FilteredSearchParams = {};

  const ratingMin = params.get("ratingMin");
  const ratingMax = params.get("ratingMax");
  const year = params.get("year");
  const genre = params.get("genre");
  const director = params.get("director");

  if (ratingMin) filters.ratingMin = Number(ratingMin);
  if (ratingMax) filters.ratingMax = Number(ratingMax);
  if (year) filters.year = Number(year);
  if (genre) filters.genre = genre;
  if (director) filters.director = director;

  return filters;
}

export default function DiscoverPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);

  const filters = useMemo(
    () => parseFilters(new URLSearchParams(searchParams.toString())),
    [searchParams]
  );

  useEffect(() => {
    const fetchMovies = async () => {
      setLoading(true);
      try {
        const data = await filteredSearch(filters);
        setMovies(data.results || []);
      } catch (err) {
        console.error(err);
        setMovies([]);
      } finally {
        setLoading(false);
      }
    };

    fetchMovies();
  }, [filters]);

  const handleMovieClick = (movie: Movie) => router.push(`/movies/${movie.id}`);
  const hasFilters = Object.keys(filters).length > 0;

  const buildDiscoverUrl = (newFilters: FilteredSearchParams) => {
    const params = new URLSearchParams();
    if (newFilters.ratingMin !== undefined) params.set("ratingMin", newFilters.ratingMin.toString());
    if (newFilters.ratingMax !== undefined) params.set("ratingMax", newFilters.ratingMax.toString());
    if (newFilters.year !== undefined) params.set("year", newFilters.year.toString());
    if (newFilters.genre) params.set("genre", newFilters.genre);
    if (newFilters.director) params.set("director", newFilters.director);

    const query = params.toString();
    return query ? `/discover?${query}` : "/discover";
  };

  const handleApplyFilters = (newFilters: FilteredSearchParams) => {
    if (newFilters.genre === "all") {
      delete newFilters.genre;
    }
    router.push(buildDiscoverUrl(newFilters));
  };

  return (
    <div className="min-h-screen bg-slate-900 text-white">
      <main className="container mx-auto px-4 py-8 space-y-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-3">
            <TbSparkles className="text-3xl text-yellow-500" />
            <div>
              <h1 className="text-3xl font-bold">Discover</h1>
              <p className="text-slate-400 text-sm">
                {hasFilters
                  ? "Todos os filmes com os filtros aplicados"
                  : "Todos os filmes populares"}
              </p>
            </div>
          </div>
          <div className="flex gap-2">
            {hasFilters && (
              <Button
                variant="ghost"
                onClick={() => router.push("/discover")}
                className="text-slate-300 hover:text-white hover:bg-slate-800 border-slate-700"
              >
                Limpar filtros
              </Button>
            )}
            <Button
              variant="outline"
              onClick={() => router.push("/")}
              className="text-slate-300 border-slate-700 hover:border-yellow-500 hover:text-white"
            >
              <TbArrowLeft className="mr-2" />
              Voltar
            </Button>
          </div>
        </div>

        <DiscoverPanel
          onApplyFilters={handleApplyFilters}
          loading={loading}
          activeFilters={filters}
        />

        <Separator className="bg-slate-800" />

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
                showOverlay
                onClick={() => handleMovieClick(movie)}
              />
            ))}
          </div>
        )}

        {!loading && movies.length === 0 && (
          <div className="text-center py-20">
            <p className="text-slate-400 text-lg">
              No movies found for this filters.
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

function DiscoverPanel({
  onApplyFilters,
  loading,
  activeFilters,
}: {
  onApplyFilters: (filters: FilteredSearchParams) => void;
  loading: boolean;
  activeFilters: FilteredSearchParams;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [ratingRange, setRatingRange] = useState<[number, number]>([0, 10]);
  const [year, setYear] = useState<string>("");
  const [genre, setGenre] = useState<string>("");
  const [director, setDirector] = useState<string>("");

  useEffect(() => {
    setRatingRange([
      activeFilters.ratingMin ?? 0,
      activeFilters.ratingMax ?? 10,
    ]);
    setYear(activeFilters.year ? activeFilters.year.toString() : "");
    setGenre(activeFilters.genre ?? "");
    setDirector(activeFilters.director ?? "");
  }, [activeFilters]);

  const activeFilterCount = Object.keys(activeFilters).length;

  const handleApply = () => {
    const filters: FilteredSearchParams = {};
    if (ratingRange[0] > 0) filters.ratingMin = ratingRange[0];
    if (ratingRange[1] < 10) filters.ratingMax = ratingRange[1];
    if (year) filters.year = parseInt(year);
    if (genre) filters.genre = genre;
    if (director.trim()) filters.director = director.trim();
    onApplyFilters(filters);
  };

  const handleClear = () => {
    setRatingRange([0, 10]);
    setYear("");
    setGenre("");
    setDirector("");
    onApplyFilters({});
  };

  const hasAnyFilter = ratingRange[0] > 0 || ratingRange[1] < 10 || year || genre || director;

  return (
    <Collapsible open={isOpen} onOpenChange={setIsOpen} className="mb-6">
      <div className="flex justify-center">
        <CollapsibleTrigger asChild>
          <Button
            variant={isOpen ? "default" : "outline"}
            className={`gap-2 rounded-full px-6 transition-all duration-300 ${
              isOpen
                ? "bg-linear-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white border-transparent shadow-lg shadow-yellow-500/25"
                : "border-slate-700 hover:border-yellow-500"
            }`}
          >
            <TbSparkles className={isOpen ? "animate-pulse" : ""} />
            Discover
            {activeFilterCount > 0 && (
              <Badge className="ml-1 bg-white/20 text-white border-0">
                {activeFilterCount}
              </Badge>
            )}
          </Button>
        </CollapsibleTrigger>
      </div>

      <CollapsibleContent className="mt-6 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:slide-out-to-top-2 data-[state=open]:slide-in-from-top-2 duration-300">
        <Card className="border-slate-700 bg-slate-800/50 backdrop-blur-md shadow-sm">
          <CardContent className="pt-6">
            {activeFilterCount > 0 && (
              <div className="flex flex-wrap gap-2 mb-4">
                {activeFilters.ratingMin !== undefined && (
                  <Badge className="gap-1 bg-yellow-500/10 text-yellow-400 border-yellow-500/30">
                    Rating ≥ {activeFilters.ratingMin}
                  </Badge>
                )}
                {activeFilters.ratingMax !== undefined && (
                  <Badge className="gap-1 bg-yellow-500/10 text-yellow-400 border-yellow-500/30">
                    Rating ≤ {activeFilters.ratingMax}
                  </Badge>
                )}
                {activeFilters.year && (
                  <Badge className="gap-1 bg-yellow-500/10 text-yellow-400 border-yellow-500/30">
                    Year: {activeFilters.year}
                  </Badge>
                )}
                {activeFilters.genre && (
                  <Badge className="gap-1 bg-yellow-500/10 text-yellow-400 border-yellow-500/30">
                    {activeFilters.genre}
                  </Badge>
                )}
                {activeFilters.director && (
                  <Badge className="gap-1 bg-yellow-500/10 text-yellow-400 border-yellow-500/30">
                    Director: {activeFilters.director}
                  </Badge>
                )}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleClear}
                  className="h-6 px-2 text-slate-500 hover:text-red-400"
                >
                  <TbX className="mr-1" />
                  Clear all
                </Button>
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="space-y-4 lg:col-span-2">
                <div className="flex items-center justify-between">
                  <Label className="text-slate-400">Rating Range</Label>
                  <span className="text-sm font-medium tabular-nums text-yellow-400">
                    {ratingRange[0]} - {ratingRange[1]}
                  </span>
                </div>
                <Slider
                  value={ratingRange}
                  onValueChange={(value) => setRatingRange(value as [number, number])}
                  min={0}
                  max={10}
                  step={0.5}
                  className="**:data-[slot=slider-track]:bg-slate-700 **:data-[slot=slider-range]:bg-linear-to-r **:data-[slot=slider-range]:from-yellow-500 **:data-[slot=slider-range]:to-orange-500 **:data-[slot=slider-thumb]:border-yellow-500 **:data-[slot=slider-thumb]:shadow-lg"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="year" className="text-slate-400">
                  Release Year
                </Label>
                <Input
                  id="year"
                  type="number"
                  min="1900"
                  max={new Date().getFullYear()}
                  value={year}
                  onChange={(e) => setYear(e.target.value)}
                  placeholder={new Date().getFullYear().toString()}
                  className="border-slate-700 bg-slate-900/50 focus:ring-yellow-500/50 focus:border-yellow-500"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="director" className="text-slate-400">
                  Director
                </Label>
                <Input
                  id="director"
                  type="text"
                  value={director}
                  onChange={(e) => setDirector(e.target.value)}
                  placeholder="e.g. Spielberg"
                  className="border-slate-700 bg-slate-900/50 focus:ring-yellow-500/50 focus:border-yellow-500"
                />
              </div>
            </div>

            <div className="mt-6 space-y-2">
              <Label className="text-slate-400">Genre</Label>
              <select
                value={genre}
                onChange={(e) => setGenre(e.target.value)}
                className="w-full md:w-64 border-slate-700 bg-slate-900/50 text-white rounded-md px-3 py-2"
              >
                <option value="">All Genres</option>
                <option value="all">All Genres</option>
                {GENRES.map((g) => (
                  <option key={g} value={g}>
                    {g}
                  </option>
                ))}
              </select>
            </div>

            <Separator className="my-6 bg-slate-700" />

            <div className="flex justify-end gap-3">
              <Button
                variant="ghost"
                onClick={handleClear}
                disabled={!hasAnyFilter}
                className="text-slate-400 hover:text-white hover:bg-slate-700"
              >
                Clear All
              </Button>
              <Button
                onClick={handleApply}
                disabled={loading}
                className="bg-linear-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white min-w-[120px] shadow-lg shadow-yellow-500/25"
              >
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                    Searching...
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    <TbFilter />
                    Apply
                  </span>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      </CollapsibleContent>
    </Collapsible>
  );
}

