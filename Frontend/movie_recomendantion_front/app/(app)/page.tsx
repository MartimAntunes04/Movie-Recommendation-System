import Image from "next/image";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { TbMovie, TbStar, TbTrendingUp, TbPlayerPlay } from 'react-icons/tb';

export default function Home() {
  return (
    <div className="min-h-screen">
      <main className="container mx-auto px-4 py-8">
        <div className="text-center py-16">
          <h1 className="text-4xl md:text-6xl font-bold text-slate-900 dark:text-white mb-6">
            Descubra os melhores{" "}
            <span className="bg-linear-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
              filmes
            </span>
          </h1>
          <p className="text-xl text-slate-600 dark:text-slate-400 mb-8 max-w-2xl mx-auto">
            Nosso sistema de recomendação inteligente encontra os filmes perfeitos para você baseado nos seus gostos e preferências.
          </p>
          </div>
      </main>
    </div>
  );
}
