"use client";
import { TbTrendingUp, TbArrowBigUpLine, TbFilter, TbSparkles, TbX } from 'react-icons/tb';
import { useEffect, useState } from "react";
import { Movie, popularMovies, topRatedMovies, filteredSearch, FilteredSearchParams } from "@/Services/API";
import { MovieCarousel } from "@/components/MovieCarousel";
import { useRouter } from "next/navigation";
import { MovieCarouselSkeleton } from "@/components/MovieCarouselSkeleton";

// shadcn components
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";
import { Slider } from "@/components/ui/slider";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const GENRES = [
  "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
  "Drama", "Family", "Fantasy", "History", "Horror", "Music", "Mystery",
  "Romance", "Science Fiction", "Thriller", "War", "Western"
];

function MovieSection({
  title,
  movies,
  loading,
  onMovieClick,
  icon: Icon,
}: {
  title: string;
  movies: Movie[];
  loading: boolean;
  onMovieClick: (movie: Movie) => void;
  icon: React.ComponentType<{ className?: string }>;
}) {
  return (
    <div className="mb-12">
      <div className="flex items-center gap-3 mb-6">
        <Icon className="text-3xl text-yellow-500" />
        <h2 className="text-3xl font-bold text-white">
          {title}
        </h2>
      </div>

      {loading ? (
        <MovieCarouselSkeleton count={5} />
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
    <Collapsible open={isOpen} onOpenChange={setIsOpen} className="mb-8">
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
            {/* Active Filters Badges */}
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
              {/* Rating Slider */}
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
                  className="[&_[data-slot=slider-track]]:bg-slate-700 [&_[data-slot=slider-range]]:bg-linear-to-r [&_[data-slot=slider-range]]:from-yellow-500 [&_[data-slot=slider-range]]:to-orange-500 [&_[data-slot=slider-thumb]]:border-yellow-500 [&_[data-slot=slider-thumb]]:shadow-lg"
                />
              </div>

              {/* Year Input */}
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

              {/* Director Input */}
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

            {/* Genre Select - full width */}
            <div className="mt-6 space-y-2">
              <Label className="text-slate-400">Genre</Label>
              <Select value={genre} onValueChange={setGenre}>
                <SelectTrigger className="w-full md:w-64 border-slate-700 bg-slate-900/50">
                  <SelectValue placeholder="All Genres" />
                </SelectTrigger>
                <SelectContent className="bg-slate-800 border-slate-700">
                  <SelectItem value="all">All Genres</SelectItem>
                  {GENRES.map((g) => (
                    <SelectItem key={g} value={g}>
                      {g}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <Separator className="my-6 bg-slate-700" />

            {/* Action Buttons */}
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

export default function Home() {
  const router = useRouter();
  const [moviesPop, setMoviesPop] = useState<Movie[]>([]);
  const [moviesTop, setMoviesTop] = useState<Movie[]>([]);
  const [moviesFiltered, setMoviesFiltered] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
  const [filterLoading, setFilterLoading] = useState(false);
  const [activeFilters, setActiveFilters] = useState<FilteredSearchParams>({});

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

  const handleApplyFilters = async (filters: FilteredSearchParams) => {
    if (filters.genre === "all") {
      delete filters.genre;
    }

    setActiveFilters(filters);
    const hasFilters = Object.keys(filters).length > 0;

    if (!hasFilters) {
      setMoviesFiltered([]);
      return;
    }

    setFilterLoading(true);
    try {
      const result = await filteredSearch(filters);
      setMoviesFiltered(result.results || []);
    } catch (err) {
      console.error(err);
      setMoviesFiltered([]);
    } finally {
      setFilterLoading(false);
    }
  };

  const hasActiveFilters = Object.keys(activeFilters).length > 0;

  return (
    <div className="min-h-screen">
      <main className="container mx-auto px-4 py-8">
        {/* Hero Section */}
        <div className="text-center mb-10">
          <h1 className="text-4xl md:text-6xl font-bold text-white mb-6">
            Discover the{" "}
            <span className="bg-linear-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
              best movies
            </span>
          </h1>
          <p className="text-xl text-slate-400 mb-8 max-w-2xl mx-auto">
            Find your next favorite film in seconds.
          </p>
        </div>

        {/* Discover Panel */}
        <DiscoverPanel
          onApplyFilters={handleApplyFilters}
          loading={filterLoading}
          activeFilters={activeFilters}
        />

        {/* Filtered Results Section */}
        {hasActiveFilters && (
          <MovieSection
            title="Discover Results"
            movies={moviesFiltered}
            loading={filterLoading}
            icon={TbSparkles}
            onMovieClick={handleMovieClick}
          />
        )}

        {/* Popular Movies Section */}
        <MovieSection
          title="Popular Movies"
          movies={moviesPop}
          loading={loading}
          icon={TbTrendingUp}
          onMovieClick={handleMovieClick}
        />

        {/* Top Movies Section */}
        <MovieSection
          title="Top Movies"
          movies={moviesTop}
          loading={loading}
          icon={TbArrowBigUpLine}
          onMovieClick={handleMovieClick}
        />
      </main>
    </div>
  );
}