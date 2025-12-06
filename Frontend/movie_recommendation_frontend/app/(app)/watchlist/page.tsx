"use client";
import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { getWatchlist } from "@/Services/API";
import { Card, CardContent } from "@/components/ui/card";
import { MovieCard } from "@/components/MovieCard";
import Link from "next/link";

export default function WatchlistPage() {
  const { token, isAuthenticated, loading: authLoading } = useAuth();
  const [watchlist, setWatchlist] = useState<any[] | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authLoading) return; // espera autenticação
    if (!isAuthenticated) {
      setWatchlist(null);
      setLoading(false);
      return;
    }

    let mounted = true;
    setLoading(true);

    getWatchlist()
      .then((data: any) => {
        // Se o componente já tiver sido removido do ecrã, não fazemos nada
        if (!mounted) return;

        // Garantimos que 'list' é sempre um array
        const list = Array.isArray(data) ? data : (data?.results || []);

        // Atualizamos o estado *apenas se o componente ainda existir*
        setWatchlist(list);
      })
      .catch((err) => {
        // Evita atualizar o estado se o componente já tiver desaparecido
        if (!mounted) return;

        console.error("Erro ao carregar watchlist:", err);

        // Guardamos a mensagem de erro
        setError(err?.message || "Erro ao carregar watchlist");
      })
      .finally(() => {
        // Só atualizamos o loading se o componente ainda estiver presente
        if (!mounted) return;

        setLoading(false);
      });

    // Esta função é chamada automaticamente quando o componente é desmontado
    return () => {
      // Aqui marcamos que o componente já foi removido do ecrã
      // Assim evitamos chamadas a setState depois de ele desaparecer
      mounted = false; };
  }, [authLoading, isAuthenticated]);

  if (authLoading || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
          <p className="text-slate-400">Loading watchlist...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <div className="min-h-screen py-8">
      <div className="max-w-6xl mx-auto px-4">
        <h1 className="text-2xl font-bold text-white mb-4">My Watchlist</h1>

        {error && (
          <div className="mb-4 text-red-400">{error}</div>
        )}

        {!watchlist || watchlist.length === 0 ? (
          <Card className="p-6 bg-white/5">
            <CardContent>
              <p className="text-slate-300 mb-4">Your watchlist is empty.</p>
              <Link href="/" className="text-yellow-400 underline">Explore films</Link>
            </CardContent>
          </Card>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
            {watchlist.map((movie) => {
              
              const movieId = movie.id
              if (!movieId) return null;
              return (
                <Link key={movieId} href={`/movies/${movieId}`} className="block">
                  <MovieCard movie={movie} size="small" />
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
