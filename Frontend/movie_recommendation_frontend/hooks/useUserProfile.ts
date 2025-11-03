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
let cacheTimestamp: number = 0;
const CACHE_DURATION = 5 * 60 * 1000; // 5 min

export function useUserProfile(token: string | null) {
  const [profile, setProfile] = useState<UserProfile | null>(cachedProfile);
  const [loading, setLoading] = useState(!cachedProfile);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // Se não tem token, limpar perfil
    if (!token) {
      setProfile(null);
      cachedProfile = null;
      return;
    }

    // Verificar se o cache ainda é válido
    const isCacheValid = 
      cachedProfile && 
      cacheTimestamp && 
      Date.now() - cacheTimestamp < CACHE_DURATION;

    if (isCacheValid) {
      // Usar cache válido
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
        cacheTimestamp = Date.now();
        
        setProfile(data);
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
      cachedProfile = data;
      cacheTimestamp = Date.now();
      setProfile(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  const clearCache = () => {
    cachedProfile = null;
    cacheTimestamp = 0;
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