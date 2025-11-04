"use client";
import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { useRouter, usePathname } from "next/navigation";

interface AuthContextType {
  token: string | null;
  login: (newToken: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const pathname = usePathname();

  // ⭐ Carregar token do localStorage quando o componente montar
  useEffect(() => {
    // Verificar se está no cliente (não SSR)
    if (typeof window === 'undefined') {
      setLoading(false);
      return;
    }

    // Carregar token do localStorage
    const storedToken = localStorage.getItem("token");
    
    // Verificar se o token existe e não está vazio
    if (storedToken && storedToken.trim() !== '') {
      setToken(storedToken);
    } else {
      setToken(null);
    }
    
    setLoading(false);
  }, []);

  // Verificar autenticação e redirecionar se necessário
  useEffect(() => {
    // Só verificar após o loading terminar
    if (loading) return;

    const publicRoutes = ["/login", "/register"];
    const isPublicRoute = publicRoutes.includes(pathname);

    // Verificar se não tem token e não está em rota pública
    if (!token && !isPublicRoute) {
      router.push("/login");
      return;
    }

    
    if (token && isPublicRoute) {
      router.push("/");
      return;
    }
  }, [token, loading, pathname, router]);

  const login = (newToken: string) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem("token", newToken);
    }
    setToken(newToken);
  };

  const logout = () => {
    if (typeof window !== 'undefined') {
      localStorage.removeItem("token");
      // Limpar cache do perfil
      (window as any).__userProfileCache = null;
    }
    setToken(null);
    router.push("/login");
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        login,
        logout,
        isAuthenticated: !!token && token.trim() !== '',
        loading,
      }}
    >
      {loading ? (
        <div className="min-h-screen flex items-center justify-center bg-slate-900">
          <div className="text-center">
            <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-slate-300 border-t-yellow-500 mb-4"></div>
            <p className="text-slate-400">Carregando...</p>
          </div>
        </div>
      ) : (
        children
      )}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}