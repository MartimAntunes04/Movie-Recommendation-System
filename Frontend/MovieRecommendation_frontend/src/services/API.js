//Chamar endpoints do backend

const API_URL = import.meta.env.VITE_API_URL

export async function searchMovies(query) {
  const response = await fetch(
    `${API_URL}/movies/search?query=${encodeURIComponent(query)}`
  );

  if (!response.ok) {
    throw new Error("Erro ao buscar filmes");
  }

  return await response.json(); // retorna os dados JSON do backend
}