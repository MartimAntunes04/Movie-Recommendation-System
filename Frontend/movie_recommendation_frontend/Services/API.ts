
const TOKEN_KEY = 'auth_token';
const API_URL = process.env.NEXT_PUBLIC_API_URL
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
  credits?: {
    crew: {
      id: number;
      job: string;
      name: string;
      department: string;
    }[];
    cast: {
      id: number;
      name: string;
      character: string;
      profile_path: string | null;
    }[];
  };
};

export type FilteredSearchParams = {
  ratingMin?: number;
  ratingMax?: number;
  year?: number;
  genre?: string;
  director?: string;
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

export async function filteredSearch(
  params: FilteredSearchParams
): Promise<{ results: Movie[] }> {
  try {

    const queryParams = new URLSearchParams();
    
    if (params.ratingMin !== undefined) queryParams.append('ratingMin', params.ratingMin.toString());
    if (params.ratingMax !== undefined) queryParams.append('ratingMax', params.ratingMax.toString());
    if (params.year !== undefined) queryParams.append('year', params.year.toString());
    if (params.genre) queryParams.append('genre', params.genre);
    if (params.director) queryParams.append('director', params.director);

    const response = await fetch(
      `${API_URL}/movies/filtered_search?${queryParams.toString()}`,
      {
        method: "GET",
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar filmes filtrados: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição filteredSearch:", error);
    throw error;
  }
}

export async function getRecommendedMovies(): Promise<{ results: Movie[] }> {
  try {
   
    const response = await fetch(
      `${API_URL}/movies/recommended`,
      {
        method: "GET",
        headers: getAuthHeaders(),
      }
    );

    if (!response.ok) {
      // If unauthorized, we might want to just return empty results or throw
      if (response.status === 401) {
        return { results: [] };
      }
      handleUnauthorized(response);
      throw new Error(`Erro ao buscar recomendações: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição getRecommendedMovies:", error);
    // Return empty results on error to not break the page
    return { results: [] };
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

export async function getWatchlist(page = 0, size = 1000): Promise<Movie[]> {
  try {
    
    const response = await fetch(`${API_URL}/watchlist?page=${page}&size=${size}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      if (response.status === 404) return [];
      throw new Error(`Erro ao buscar watchlist: ${response.statusText}`);
    }

    const data = await response.json();
    
    // Backend returns Page with `content` array
    const items = Array.isArray(data) ? data : (data.content || data.results || []);
    
    // Fetch full movie details for each watchlist item
    const movies = await Promise.all(
      items.map(async (item: any) => {
        const movieId = item.movieId ?? item.id;
        if (!movieId) return null;
        try {
          return await getMovieById(String(movieId));
        } catch (err) {
          console.error(`Erro ao buscar filme ${movieId}:`, err);
          return null;
        }
      })
    );
    
    // Filter out null entries
    return movies.filter((m): m is Movie => m !== null);
  } catch (err) {
    console.error('Erro em getWatchlist:', err);
    throw err;
  }
}

export async function addToWatchlist(movieId: number) {
  try {
    
    const response = await fetch(`${API_URL}/watchlist/${encodeURIComponent(movieId)}`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      const text = await response.text();
      throw new Error(text || `Erro ao adicionar à watchlist: ${response.statusText}`);
    }

    return await response.json();
  } catch (err) {
    console.error('Erro em addToWatchlist:', err);
    throw err;
  }
}

export async function removeFromWatchlist(movieId: number) {
  try {
   
    const response = await fetch(`${API_URL}/watchlist/${encodeURIComponent(movieId)}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      const text = await response.text();
      throw new Error(text || `Erro ao remover da watchlist: ${response.statusText}`);
    }

    return await response.json();
  } catch (err) {
    console.error('Erro em removeFromWatchlist:', err);
    throw err;
  }
}

export async function isMovieInWatchlist(movieId: number): Promise<boolean> {
  try {
 
    const response = await fetch(`${API_URL}/watchlist/checkWatchlist/${movieId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      console.error("Erro no servidor ao verificar watchlist.");
      return false;
    }

    const data = await response.json();

    // O backend devolve: { success: true/false, message: "..." }
    return data.success === true;
    
  } catch (err) {
    console.error('Erro em isMovieInWatchlist:', err);
    return false;
  }
}

export async function addToHistory(movieId: number) {
  try {
   
    const response = await fetch(`${API_URL}/history/${encodeURIComponent(movieId)}`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      const text = await response.text();
      // Ignore conflict error (already in history)
      if (response.status !== 409) {
          throw new Error(text || `Erro ao adicionar ao histórico: ${response.statusText}`);
      }
    }

    return await response.json().catch(() => ({})); 
  } catch (err) {
    console.error('Erro em addToHistory:', err);
    throw err;
  }
}

export async function removeFromHistory(movieId: number) {
  try {
    
    const response = await fetch(`${API_URL}/history/${encodeURIComponent(movieId)}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      const text = await response.text();
      throw new Error(text || `Erro ao remover do histórico: ${response.statusText}`);
    }

    return await response.json();
  } catch (err) {
    console.error('Erro em removeFromHistory:', err);
    throw err;
  }
}

export async function isMovieInHistory(movieId: number): Promise<boolean> {
  try {

    const response = await fetch(`${API_URL}/history/checkHistory/${movieId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      console.error("Erro no servidor ao verificar histórico.");
      return false;
    }

    const data = await response.json();
    return data.success === true;
    
  } catch (err) {
    console.error('Erro em isMovieInHistory:', err);
    return false;
  }
}

export async function getHistory(page = 0, size = 1000): Promise<Movie[]> {
  try {
  
    const response = await fetch(`${API_URL}/history?page=${page}&size=${size}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      handleUnauthorized(response);
      if (response.status === 404) return [];
      const data = await response.json();
      if(data.message && data.message === "No movies in your history.") return [];
      throw new Error(`Erro ao buscar histórico: ${response.statusText}`);
    }

    const data = await response.json();
    
    const items = data.historyList || [];
    
    const movies = items.map((item: any) => {
      const m = item.movie;
      return {
        id: m.id,
        title: m.title,
        overview: m.description || "",
        poster_path: m.posterPath,
        release_date: m.releaseDate,
        backdrop_path: m.backdropPath || null,
        vote_average: m.averageRating !== undefined ? m.averageRating : (m.vote_average || 0),
        vote_count: m.voteCount !== undefined ? m.voteCount : (m.vote_count || 0),
        adult: false,
        original_title: m.title,
        original_language: 'en',
        genre_ids: [],
        video: false,
        popularity: 0
      } as Movie;
    });
    return movies;

  } catch (err) {
    console.error('Erro em getHistory:', err);
    throw err;
  }
}

export async function checkRating(movieId: number): Promise<number | null> {
  
  const response = await fetch(`${API_URL}/rating/${movieId}`, {
    method: "GET",
    headers: getAuthHeaders(),
  });

  if (response.status === 401) {
    handleUnauthorized(response);
    return null;
  }

  if (!response.ok) {
    throw new Error("Erro ao obter rating");
  }

  const data = await response.json();

  if (data.success === true && typeof data.rating === "number") {
    return data.rating;
  }

  return null;
}

export async function updateRating(movieId: number, rating: number) {

  console.log(`Atualizando rating do filme ${movieId} para ${rating}`);
  const response = await fetch(
    `${API_URL}/rating/${movieId}?rating=${rating}`,
    {
      method: "PUT",
      headers: getAuthHeaders(),
    }
  );

  if (response.status === 401) {
    handleUnauthorized(response);
    return null;
  }

  const text = await response.text();

  try {
    return JSON.parse(text);
  } catch {
    return { success: false, message: text };
  }
}
