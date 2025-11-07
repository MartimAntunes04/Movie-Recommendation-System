"use client";
import { createContext, useContext, useState, useEffect, ReactNode, useRef } from "react";
import { useRouter, usePathname } from "next/navigation";

interface AuthContextType {
  token: string | null;
  login: (newToken: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function isTokenExpired(token: string): boolean {
  try {
    const base64 = token.split('.')[1]?.replace(/-/g, '+').replace(/_/g, '/');
    if (!base64) return true;
    const payload = JSON.parse(atob(base64));
    return !payload?.exp || payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isValidating, setIsValidating] = useState(false);
  const router = useRouter();
  const pathname = usePathname();
  const redirectingRef = useRef(false); // Prevenir loops de redirecionamento

  // Carregar token do localStorage quando o componente montar
  useEffect(() => {
    // Verificar se está no cliente (não SSR)
    if (typeof window === 'undefined') {
      setLoading(false);
      return;
    }

    // Carregar token do localStorage
    const storedToken = localStorage.getItem("token");
    
    // Verificar se o token existe, não está vazio e não está expir\ado
    if (storedToken && storedToken.trim() !== '') {
      // Validar se o token não está expirado
      if (isTokenExpired(storedToken)) {
        // Token expirado, remover
        localStorage.removeItem("token");
        setToken(null);
      } else {
        setToken(storedToken);
      }
    } else {
      setToken(null);
    }
    
    setLoading(false);
  }, []);

  // Verificar autenticação e redirecionar se necessário
  useEffect(() => {
    // Só verificar após o loading terminar e não estiver validando
    if (loading || isValidating || redirectingRef.current) return;

    const publicRoutes = ["/login", "/register"];
    const isPublicRoute = publicRoutes.includes(pathname);

    // Verificar se não tem token e não está em rota pública
    if (!token && !isPublicRoute) {
      redirectingRef.current = true;
      router.push("/login");
      setTimeout(() => { redirectingRef.current = false; }, 1000);
      return;
    }

    // Se tem token e está em rota pública, redirecionar para home
    // Mas só se o token não estiver expirado
    if (token && isPublicRoute) {
      if (!isTokenExpired(token)) {
        redirectingRef.current = true;
        router.push("/");
        setTimeout(() => { redirectingRef.current = false; }, 1000);
      } else {
        // Token expirado, limpar e deixar na página de login
        localStorage.removeItem("token");
        setToken(null);
      }
    }
  }, [token, loading, isValidating, pathname, router]);

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
    redirectingRef.current = true;
    router.push("/login");
    setTimeout(() => { redirectingRef.current = false; }, 1000);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        login,
        logout,
        isAuthenticated: !!token && token.trim() !== '' && !isTokenExpired(token),
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