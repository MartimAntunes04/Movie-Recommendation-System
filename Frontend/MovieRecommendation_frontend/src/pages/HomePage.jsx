import React, { useState } from "react";
import { searchMovies } from "../services/API";

export default function HomePage() {

const [query, setQuery] = useState("");
const [movies, setMovies] = useState([]);

// Lista de filmes mockados
  const mockMovies = [
    { id: 1, title: "Inception" },
    { id: 2, title: "Interstellar" },
    { id: 3, title: "The Dark Knight" },
    { id: 4, title: "Tenet" },
  ];
    
const handleSearch = async (e) => {
    e.preventDefault();

    try{
        //COM BACKEND
        //const data =  await searchMovies(query);
        //setMovies(data.results);

        //SEM BACKEND - USANDO MOCK
        const filtered = mockMovies.filter((m) =>
        m.title.toLowerCase().includes(query.toLowerCase())
        );
        setMovies(filtered);

        alert(`Encontrados ${filtered.length} filmes.`);
    }catch (err) {
      console.error(err);
      alert("Erro ao buscar filmes");
    }
  
  };

 return(
    <div>
        <h1>Procure filmes</h1>
        <form onSubmit={handleSearch}>

            <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Digite nome de filme"                    
            />
            <button type="submit">Buscar</button>
        </form>
       
      <ul>
        {movies.map((m) => (
          <li key={m.id}>{m.title}</li>
        ))}
      </ul>
  
    </div>
    );
}