"use client";
import { useEffect, useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { useUserProfile, getInitials } from "@/hooks/useUserProfile";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { TbMail, TbEdit, TbClock } from 'react-icons/tb';
import Link from "next/link";
import { Separator } from "@/components/ui/separator";
import { MovieCarousel } from "@/components/MovieCarousel";
import { MovieCarouselSkeleton } from "@/components/MovieCarouselSkeleton";
import { getHistory, Movie } from "@/Services/API";
import { useRouter } from "next/navigation";

export default function ProfilePage() {
  const { token, isAuthenticated, loading: authLoading } = useAuth();
  const { profile, loading: profileLoading } = useUserProfile(token);
  const [history, setHistory] = useState<Movie[]>([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated && token) {
      getHistory()
        .then(setHistory)
        .catch((err) => console.error("Erro ao carregar histórico:", err))
        .finally(() => setHistoryLoading(false));
    }
  }, [isAuthenticated, token]);

  if (authLoading || profileLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
          <p className="text-slate-400">Carregando perfil...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  const userInitials = getInitials(profile?.firstName, profile?.lastName);
  const displayName = profile 
    ? `${profile.firstName} ${profile.lastName}` 
    : 'User';

  return (
    <div className="min-h-screen relative">
      <div className="relative z-10">
        <Card className="relative overflow-hidden bg-slate-900 backdrop-blur-md shadow-xl rounded-none">
          <div 
            className="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-10"
            style={{
              backgroundImage: 'url(https://www.ucicinemas.pt/media/1xolwnf3/banner-filmes-2025.jpg?width=1200&height=630&v=1db6bdbe09a9ac0)',
            }}
          ></div>
          
          <div className="relative z-10">
            <CardContent>
              <div className="max-w-4xl mx-auto">
                <div className="flex flex-col sm:flex-row items-center sm:items-start gap-4 sm:gap-6 mb-4 pb-4 ">
                  <div className="relative shrink-0">
                  <div className="relative shrink-0">
                    <Avatar className="h-20 w-20 sm:h-24 sm:w-24 md:h-28 md:w-28 border-2 sm:border-4 border-slate-800 shadow-lg">
                      <AvatarFallback className="bg-linear-to-r from-yellow-500 to-orange-500 text-white text-xl sm:text-2xl md:text-3xl font-bold">
                        {userInitials}
                      </AvatarFallback>
                    </Avatar>
                  </div>
                  </div>
             
                  <div className="flex-1 w-full text-center sm:text-left">
                    <h1 className="text-2xl sm:text-3xl md:text-3xl font-bold text-white mb-1 sm:mb-2 wrap-break-word">
                      {displayName}
                    </h1>
                    {profile?.username && (
                      <p className="text-base sm:text-lg text-slate-400 mb-3 sm:mb-4">
                        @{profile.username}
                      </p>
                    )}
                    
                    <Link href="/profile/edit" className="inline-block">
                      <Button 
                        className="w-auto bg-linear-to-r from-yellow-500 to-orange-500 hover:from-yellow-600 hover:to-orange-600 text-white text-sm sm:text-base"
                      >
                        <TbEdit className="h-4 w-4 mr-2" />
                        Edit Profile
                      </Button>
                    </Link>
                  </div>
                </div>
                <Separator orientation="horizontal" className="my-4 bg-slate-700" />
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-4 mb-8">
                  <div className="flex items-start gap-3 sm:gap-4 p-3 sm:p-4 rounded-lg bg-slate-900/50">
                    <div className="p-1.5 sm:p-2 rounded-full bg-yellow-500/10 shrink-0">
                      <TbMail className="h-4 w-4 sm:h-5 sm:w-5 text-yellow-500" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-xs sm:text-sm text-slate-400 mb-1">Email</p>
                      <p className="text-sm sm:text-base font-medium text-white wrap-break-word">
                        {profile?.email || 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </div>
        </Card>
        
        <div className="max-w-6xl mx-auto px-4 py-12">
          <div className="bg-slate-800/50 backdrop-blur-sm rounded-xl p-6 md:p-8 shadow-lg border border-slate-700/50">
            <div className="flex items-center gap-3 mb-6">
              <div className="p-3 bg-yellow-500/10 rounded-full">
                <TbClock className="text-2xl text-yellow-500" />
              </div>
                <h2 className="text-2xl font-bold text-white">Movie History</h2>
            </div>
            {historyLoading ? (
              <MovieCarouselSkeleton />
            ) : history.length > 0 ? (
              <div className="mt-8">
                <MovieCarousel 
                  movies={history} 
                  onMovieClick={(movie) => router.push(`/movies/${movie.id}`)}
                />
              </div>
            ) : (
              <div className="text-center py-12 bg-slate-900/50 rounded-lg border border-slate-800 border-dashed">
                <TbClock className="text-4xl text-slate-600 mx-auto mb-3" />
                <p className="text-slate-400 font-medium">No movies in your history.</p>
                <p className="text-slate-500 text-sm mt-1">Mark movies as watched to see them here.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}