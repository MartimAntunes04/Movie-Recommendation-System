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
  video: boolean;
  popularity: number;
};

export async function login(email: string, password: string) {
  try {
    const response = await fetch(`${API_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const data = await response.json();

    if (!response.ok) {
      return { success: false, message: data.message || "Erro ao fazer login" };
    }

    return data;

  } catch (error) {
    console.error("Erro ao fazer login:", error);
    return { success: false, message: "Erro ao conectar ao servidor" };
  }
}

export async function register(email: string, password: string,userName:string,firstName:string,lastName:string){
  try{
    const response = await fetch(`${API_URL}/signup`,{
      method:"POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password,userName,firstName,lastName }),
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
      throw new Error(`Erro ao buscar filmes: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Erro na requisição searchMovies:", error);
    throw error;
  }
}