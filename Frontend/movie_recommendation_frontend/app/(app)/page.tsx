"use client";
import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { TbMovie, TbStar, TbTrendingUp, TbPlayerPlay } from 'react-icons/tb';
import { FormEvent, useEffect, useState } from "react";
import { Movie, popularMovies } from "@/Services/API";

export default function Home() {

  const [moviesPop,setMoviesPop] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchPopular = async () => {
      setLoading(true)
      try{
        const data = await popularMovies();
        setMoviesPop(data.results || []);
      }catch(err){
        console.error(err);
      }finally{
        setLoading(false)
      }
    };

    fetchPopular();
  },[]);

  return (
    <div className="min-h-screen">
      <main className="container mx-auto px-4 py-8">
        <div className="text-center">
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


        <div>
          <h2 className="text-2xl font-semibold mb-4">Popular Movies</h2>

          {loading && (
            <p className="text-slate-400 text-center mb-4">Carregando filmes...</p>
          )}

          {!loading && moviesPop.length > 0 && (
            <ul className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-2 mx-auto">
              {moviesPop.map((m) => (
                <li
                  key={m.id}
                  className="bg-slate-800 p-1 rounded-lg hover:bg-slate-700 transition"
                >
                  {m.poster_path ? (
                    <img
                      src={`https://image.tmdb.org/t/p/w200${m.poster_path}`}
                      alt={m.title}
                      className="w-full h-32 sm:h-40 md:h-44 rounded-md mb-1 object-cover"
                    />
                  ) : (
                    <div className="w-full h-32 sm:h-40 md:h-44 bg-gray-700 flex items-center justify-center text-gray-300 mb-1">
                      Sem imagem
                    </div>
                  )}
                  <h3 className="font-semibold text-xs sm:text-sm truncate">{m.title}</h3>
                  <p className="text-yellow-400 text-xs">{m.vote_average}/10</p>
                </li>
              ))}
            </ul>
          )}


          </div>  


      </main>
    </div>
  );
}
