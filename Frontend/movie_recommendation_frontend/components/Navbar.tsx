"use client";

import { useState, useRef, useEffect, FormEvent } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { TbMovie, TbSearch, TbUser, TbLogout, TbSettings } from 'react-icons/tb';
import Link from "next/link";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { useUserProfile, getInitials, clearUserProfileCache } from "@/hooks/useUserProfile";

export function Navbar() {
  const [searchQuery, setSearchQuery] = useState("");
  const [showUserMenu, setShowUserMenu] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);
  const router = useRouter();
  const { token, logout } = useAuth();
  
  // Buscar perfil do user com cache
  const { profile, loading } = useUserProfile(token);
  
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearch = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    router.push(`/search?query=${encodeURIComponent(searchQuery)}`);
  };

  const handleLogout = () => {
    logout();
    setShowUserMenu(false);
    clearUserProfileCache();
  };

  // Obter iniciais para o avatar
  const userInitials = getInitials(profile?.firstName, profile?.lastName);
  const displayName = profile 
    ? `${profile.firstName} ${profile.lastName}` 
    : 'User';

  return (
    <nav className="bg-white/90 backdrop-blur-md border-b border-slate-200 dark:bg-slate-900/90 dark:border-slate-700 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <Link 
            href="/" 
            className="flex items-center gap-3 text-yellow-500 hover:text-yellow-600 transition-colors"
            onClick={() => setSearchQuery("")}
            >
              <TbMovie className="text-2xl" />
              <span className="hidden sm:block text-xl font-bold bg-linear-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
                MovieRec
              </span>
            </Link>
          </div>

          {/* Barra de Pesquisa */}
          <div className="flex-1 mx-2 sm:mx-4 md:mx-8 max-w-none sm:max-w-2xl">
            <form onSubmit={handleSearch} className="relative">
              <Input
                type="text"
                placeholder="Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-3 pr-3 py-2 w-full border-slate-200 dark:border-slate-700 focus:ring-2"
              />
              <Button
                type="submit"
                variant="ghost"
                size="sm"
                className="absolute right-2 top-1/2 -translate-y-1/2 p-1 h-6 w-6 text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-700"
              >
                <TbSearch className="h-4 w-4" />
              </Button>
            </form>
          </div>

          {/* Menu de Usuário com Avatar */}
          <div className="flex items-center gap-2 sm:gap-4" ref={userMenuRef}>
            <div className="relative">
              <div 
                className="cursor-pointer rounded-full hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors p-1"
                onClick={() => setShowUserMenu(!showUserMenu)}
              >
                <Avatar className="h-8 w-8">
                  <AvatarFallback className="bg-linear-to-r from-yellow-500 to-orange-500 text-white font-medium">
                    {loading ? '...' : userInitials}
                  </AvatarFallback>
                </Avatar>
              </div>

              {/* Dropdown Menu */}
              {showUserMenu && (
                <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-slate-800 rounded-md shadow-lg border border-slate-200 dark:border-slate-700 py-1 z-50">
                  <div className="px-4 py-3 border-b border-slate-200 dark:border-slate-700">
                    <div className="flex items-center gap-3">
                      <Avatar className="h-10 w-10">
                        <AvatarFallback className="bg-linear-to-r from-yellow-500 to-orange-500 text-white font-medium">
                          {userInitials}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1 min-w-0">
                        {loading ? (
                          <>
                            <div className="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse mb-1"></div>
                            <div className="h-3 bg-slate-200 dark:bg-slate-700 rounded animate-pulse w-3/4"></div>
                          </>
                        ) : (
                          <>
                            <p className="text-sm font-medium text-slate-900 dark:text-white truncate">
                              {displayName}
                            </p>
                            <p className="text-xs text-slate-500 dark:text-slate-400 truncate">
                              {profile?.email || 'user@email.com'}
                            </p>
                            {profile?.username && (
                              <p className="text-xs text-slate-400 dark:text-slate-500 truncate">
                                @{profile.username}
                              </p>
                            )}
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                  
                  <Link 
                    href="/profile" 
                    className="flex items-center px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                    onClick={() => setShowUserMenu(false)}
                  >
                    <TbUser className="h-4 w-4 mr-2" />
                    My Profile
                  </Link>
                  <Link 
                    href="/settings" 
                    className="flex items-center px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                    onClick={() => setShowUserMenu(false)}
                  >
                    <TbSettings className="h-4 w-4 mr-2" />
                    Settings
                  </Link>
                  <hr className="my-1 border-slate-200 dark:border-slate-700" />
                  <button 
                    onClick={handleLogout}
                    className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                  >
                    <TbLogout className="h-4 w-4 mr-2" />
                    Logout
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
}