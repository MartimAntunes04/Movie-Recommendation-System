"use client";
import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { TbMovie, TbStar, TbTrendingUp, TbPlayerPlay } from 'react-icons/tb';
import { FormEvent, useState,useEffect } from "react";
import { searchMovies } from "@/Services/API";
import { Movie } from "@/Services/API";
import { useSearchParams } from "next/navigation";


export default function SearchContent(){

    
  const searchParams = useSearchParams();
  const query = searchParams.get("query") || "";
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
    

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

    return (
    <div className="min-h-screen bg-slate-900 text-white">
      <main className="container mx-auto px-4 py-8">
        <h2 className="text-2xl font-semibold mb-4">
          Results for: {query}
        </h2>

        {loading && <p className="text-slate-400">Carregando filmes...</p>}

        {!loading && movies.length > 0 && (
          <ul className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3 mx-auto">
            {movies.map((m) => (
              <li
                key={m.id}
                className="bg-slate-800 p-2 rounded-lg text-white hover:bg-slate-700 transition"
              >
                {m.poster_path ? (
                  <img
                    src={`https://image.tmdb.org/t/p/w200${m.poster_path}`}
                    alt={m.title}
                    className="w-full h-auto rounded-md mb-1"
                  />
                ) : (
                  <div className="w-full h-40 bg-gray-700 flex items-center justify-center text-gray-300 mb-1">
                    No image
                  </div>
                )}
                <h3 className="font-semibold text-sm truncate">{m.title}</h3>
              </li>
            ))}
          </ul>
        )}

        {!loading && movies.length === 0 && query && (
          <p className="text-slate-400 mt-4">No movies found</p>
        )}
      </main>
    </div>
  );
}