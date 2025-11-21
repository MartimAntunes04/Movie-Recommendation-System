"use client";
import { useState, useEffect } from 'react';

const API_URL = process.env.NEXT_PUBLIC_API_URL;

interface UserProfile {
  email: string;
  username: string;
  firstName: string;
  lastName: string;
}

// Cache global
let cachedProfile: UserProfile | null = null;
// Evento customizado para notificar atualizações do cache
const PROFILE_UPDATE_EVENT = 'profileUpdated';

// Função para disparar evento de atualização
function notifyProfileUpdate() {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(PROFILE_UPDATE_EVENT));
  }
}

export function useUserProfile(token: string | null) {
  const [profile, setProfile] = useState<UserProfile | null>(cachedProfile);
  const [loading, setLoading] = useState(!cachedProfile);
  const [error, setError] = useState<string | null>(null);

  // Listener para atualizações do cache
  useEffect(() => {
    const handleProfileUpdate = () => {
      if (cachedProfile) {
        setProfile(cachedProfile);
        setLoading(false);
      }
    };

    window.addEventListener(PROFILE_UPDATE_EVENT, handleProfileUpdate);
    return () => {
      window.removeEventListener(PROFILE_UPDATE_EVENT, handleProfileUpdate);
    };
  }, []);

  useEffect(() => {
    if (!token) return;

    // Se há cache, usar diretamente (sem verificação de tempo)
    if (cachedProfile) {
      setProfile(cachedProfile);
      setLoading(false);
      return;
    }

    const fetchProfile = async () => {
      setLoading(true);
      setError(null);

      try {
        const response = await fetch(`${API_URL}/profile`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error('Failed to fetch profile');
        }

        const data = await response.json();
        
        // Atualizar cache global
        cachedProfile = data;
        
        setProfile(data);
        notifyProfileUpdate(); // Notificar outros componentes
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Unknown error';
        setError(errorMessage);
        console.error('Error fetching profile:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [token]);

  const refetch = async () => {
    if (!token) return;

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_URL}/profile`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error('Failed to fetch profile');

      const data = await response.json();
      cachedProfile = data; // Atualizar cache com novos dados
      setProfile(data);
      notifyProfileUpdate(); // Notificar outros componentes (incluindo Navbar)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  const clearCache = () => {
    cachedProfile = null;
    setProfile(null);
  };

  return {
    profile,
    loading,
    error,
    refetch,
    clearCache,
  };
}

//função para obter iniciais pra foto de perfil
export function getInitials(firstName?: string, lastName?: string): string {
  if (!firstName && !lastName) return 'U';
  const first = firstName?.[0]?.toUpperCase() || '';
  const last = lastName?.[0]?.toUpperCase() || '';
  return `${first}${last}` || 'U';
}

export function clearUserProfileCache() {
  cachedProfile = null;
  notifyProfileUpdate(); // Notificar quando o cache é limpo
}