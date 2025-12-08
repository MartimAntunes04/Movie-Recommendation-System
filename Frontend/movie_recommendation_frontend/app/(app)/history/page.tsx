"use client";
import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { getHistory } from "@/Services/API";
import { Card, CardContent } from "@/components/ui/card";
import { MovieCard } from "@/components/MovieCard";
import Link from "next/link";
import { TbClock } from "react-icons/tb";

export default function HistoryPage() {
  const { token, isAuthenticated, loading: authLoading } = useAuth();
  const [history, setHistory] = useState<any[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return;
    if (!isAuthenticated) {
      setHistory(null);
      setLoading(false);
      return;
    }

    let mounted = true;
    setLoading(true);

    getHistory()
      .then((data: any) => {
        if (!mounted) return;
        const list = Array.isArray(data) ? data : (data?.results || []);
        setHistory(list);
      })
      .catch((err) => {
        if (!mounted) return;
        console.error("Erro ao carregar histórico:", err);
        setError(err?.message || "Erro ao carregar histórico");
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [authLoading, isAuthenticated]);

  if (authLoading || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
          <p className="text-slate-400">Loading history...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <div className="min-h-screen py-8 bg-slate-900">
      <div className="max-w-6xl mx-auto px-4">
        <div className="flex items-center gap-3 mb-8">
            <div className="p-3 bg-yellow-500/10 rounded-full">
                <TbClock className="text-2xl text-yellow-500" />
            </div>
            <div>
                <h1 className="text-2xl font-bold text-white">Movie History</h1>
            </div>
        </div>

        {error && (
          <div className="mb-4 text-red-400">{error}</div>
        )}

        {!history || history.length === 0 ? (
          <Card className="p-6 bg-slate-800/50 border-slate-700">
            <CardContent>
              <div className="text-center py-12">
                <TbClock className="text-4xl text-slate-600 mx-auto mb-3" />
                <p className="text-slate-400 font-medium">No movies in your history.</p>
                <Link href="/" className="text-yellow-400 hover:text-yellow-300 underline mt-2 inline-block">Explore films</Link>
              </div>
            </CardContent>
          </Card>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
            {history.map((movie) => {
              const movieId = movie.id;
              if (!movieId) return null;
              return (
                <Link key={movieId} href={`/movies/${movieId}`} className="block">
                  <MovieCard movie={movie} size="medium" />
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}

