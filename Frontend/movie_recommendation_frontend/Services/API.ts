const API_URL = process.env.NEXT_PUBLIC_API_URL
const TOKEN_KEY = 'auth_token';

// Helper para obter o token - VERSÃO CORRIGIDA
function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem("token");
  const headers: HeadersInit = {
    "Content-Type": "application/json",
  };
  
  if (token) {
    return {
      ...headers,
      Authorization: `Bearer ${token}`,
    };
  }
  
  return headers;
}

export type Movie = {
  id: number;
  title: string;
  overview: string;
  poster_path: string | null;
  release_date: string;
  backdrop_path: string | null;
  vote_average: number;
  vote_count: number;
  adult: boolean;
  original_title: string;
  original_language: string;
  genre_ids: number[];
  genres?: { id: number; name: string }[];
  video: boolean;
  popularity: number;
};

function setToken(token: string) {
  if (typeof window !== 'undefined') localStorage.setItem(TOKEN_KEY, token);
}

export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
  if (typeof window !== 'undefined') localStorage.removeItem(TOKEN_KEY);
}

function authHeaders(base?: Record<string, string>) {
  const t = getToken();
  return {
    ...(base || {}),
    ...(t ? { Authorization: `Bearer ${t}` } : {}),
  };
}

function handleUnauthorized(response: Response) {
  if (typeof window !== 'undefined' && response.status === 401) {
    clearToken();
    window.location.href = '/login';
  }
}

export async function login(email: string, password: string) {
  try {
    const response = await fetch(`${API_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    // Parse body safely (JSON or text or empty)
    const ct = response.headers.get("content-type") || "";
    let payload: any = null;
    if (ct.includes("application/json")) {
      payload = await response.json();
    } else {
      const text = await response.text();
      try { payload = text ? JSON.parse(text) : null; } catch { payload = { message: text }; }
    }

    if (!response.ok) {
      return {
        success: false,
        message: payload?.message || payload?.error || (response.status === 401 ? "Credenciais inválidas" : "Erro ao fazer login"),
      };
    }

    return payload; // { success: true, token: "..." }
  } catch (error) {
    console.error("Erro ao fazer login:", error);
    return { success: false, message: "Erro ao conectar ao servidor" };
  }
}

export async function register(email: string, password: string,username:string,firstName:string,lastName:string){
  try{
    const response = await fetch(`${API_URL}/signup`,{
      method:"POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password,username,firstName,lastName }),
    });

    if (!response.ok) {
      throw new Error("Erro na resposta do servidor");
    }

    const data = await response.json();
    return data

  }catch (error) {
    console.error("Erro ao register user:", error);
    return { success: false, message: "Erro ao conectar ao servidor" };
  }
}


export async function searchMovies(
  query: string
): Promise<{ results: Movie[] }> {
  try {
    const response = await fetch(
      `${API_URL}/movies/search?query=${encodeURIComponent(query)}`,
      {
        method: "GET",
        headers: getAuthHeaders(), 
      }
    );

    if (!response.ok) {
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar filmes: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição searchMovies:", error);
    throw error;
  }
}

export async function popularMovies(): Promise<{ results: Movie[] }> {
  try {
    const response = await fetch(
      `${API_URL}/movies/popular`,
      {
        method: "GET",
        headers: getAuthHeaders(), 
      }
    );

    if (!response.ok) {
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar filmes: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição searchMovies:", error);
    throw error;
  }
}


export async function topRatedMovies(): Promise<{ results: Movie[] }> {
  try {
    const response = await fetch(
      `${API_URL}/movies/top`,
      {
        method: "GET",
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar filmes top rated: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição topRatedMovies:", error);
    throw error;
  }
}

export function logout() {
  clearToken();
}

export async function updateProfile(
  token: string,
  username: string,
  firstName: string,
  lastName: string,
  password?: string
) {
  try {
    const response = await fetch(`${API_URL}/profile/update`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({
        username,
        firstName,
        lastName,
        ...(password ? { password } : {}), // só envia se existir
      }),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "Erro ao atualizar perfil");
    }

    const data = await response.json();
    return data;

  } catch (error) {
    console.error("Erro ao atualizar perfil:", error);
    return { success: false, message: "Erro ao conectar ao servidor" };
  }
}


export async function getMovieById(id: string): Promise<Movie> {
  try {
    const response = await fetch(`${API_URL}/movies/${encodeURIComponent(id)}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar filme: ${response.statusText}`);
    }

    const data = await response.json();
    console.log('Dados do filme recebidos:', data);
    return data as Movie;
  } catch (err) {
    console.error('Erro em getMovieById:', err);
    throw err;
  }
}


