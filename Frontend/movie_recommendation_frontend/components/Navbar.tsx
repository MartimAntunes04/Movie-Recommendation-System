"use client";

import { useState, useRef, useEffect } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { TbMovie, TbSearch, TbUser, TbLogout, TbSettings } from 'react-icons/tb';
import Link from "next/link";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"

export function Navbar() {
  const [searchQuery, setSearchQuery] = useState("");
  const [showUserMenu, setShowUserMenu] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setShowUserMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    console.log("Pesquisando por:", searchQuery);
  };

  
  return (
    <nav className="bg-white/90 backdrop-blur-md border-b border-slate-200 dark:bg-slate-900/90 dark:border-slate-700 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-3">
            <Link href="/" className="flex items-center gap-3 text-yellow-500 hover:text-yellow-600 transition-colors">
              <TbMovie className="text-2xl" />
              <span className="hidden sm:block text-xl font-bold bg-gradient-to-r from-yellow-500 to-orange-500 bg-clip-text text-transparent">
                MovieRec
              </span>
            </Link>
          </div>

          {/* Barra de Pesquisa */}
          <div className="flex-1 mx-2 sm:mx-4 md:mx-8 max-w-none sm:max-w-2xl">
            <form onSubmit={handleSearch} className="relative">
              <Input
                type="text"
                placeholder="Pesquisar..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 pr-4 py-2 w-full border-slate-200 dark:border-slate-700 focus:ring-2"
              />
              <Button
                type="submit"
                variant="ghost"
                size="sm"
                className="absolute left-2 top-1/2 -translate-y-1/2 p-1 h-6 w-6 text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-700"
              >
                <TbSearch className="h-4 w-4" />
              </Button>
            </form>
          </div>

          {/* Menu de Usuário com Avatar */}
          <div className="flex items-center gap-2 sm:gap-4" ref={userMenuRef}>
            <div className="relative">
                <div className="cursor-pointer rounded-full hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors p-1"onClick={() => setShowUserMenu(!showUserMenu)}>
                <Avatar className="h-8 w-8">
                <AvatarImage src="https://github.com/shadcn.png" alt="Usuário" />
                <AvatarFallback className="bg-gradient-to-r from-yellow-500 to-orange-500 text-white font-medium">
                U
                </AvatarFallback>
        </Avatar>
        </div>
              {/* Dropdown Menu */}
                {showUserMenu && (
                <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-800 rounded-md shadow-lg border border-slate-200 dark:border-slate-700 py-1 z-50">
                        <div className="px-4 py-2 border-b border-slate-200 dark:border-slate-700">
                        <div className="flex items-center gap-3">
                        <Avatar className="h-8 w-8">
                        <AvatarImage src="https://github.com/shadcn.png" alt="Usuário" />
                        <AvatarFallback className="bg-linear-to-r from-yellow-500 to-orange-500 text-white font-medium">
                          U
                        </AvatarFallback>
                      </Avatar>
                      <div>
                        <p className="text-sm font-medium text-slate-900 dark:text-white">Usuário</p>
                        <p className="text-xs text-slate-500 dark:text-slate-400">usuario@email.com</p>
                      </div>
                    </div>
                  </div>
                  
                  <Link href="/profile" className="flex items-center px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors">
                    <TbSettings className="h-4 w-4 mr-2" />
                    Meu Perfil
                  </Link>
                  <Link href="/settings" className="flex items-center px-4 py-2 text-sm text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors">
                    <TbSettings className="h-4 w-4 mr-2" />
                    Configurações
                  </Link>
                  <hr className="my-1 border-slate-200 dark:border-slate-700" />
                  <button className="flex items-center w-full px-4 py-2 text-sm text-red-600 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors">
                    <TbLogout className="h-4 w-4 mr-2" />
                    Sair
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
