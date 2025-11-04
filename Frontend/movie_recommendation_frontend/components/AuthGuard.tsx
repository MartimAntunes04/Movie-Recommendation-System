'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getToken, clearToken } from '@/Services/API';

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

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const t = getToken();
    if (!t || isTokenExpired(t)) {
      clearToken();
      router.replace('/login');
      return;
    }
    setReady(true);
  }, [router]);

  if (!ready) return null;
  return <>{children}</>;
}