/**
 * AuthContext.tsx
 * Gerencia autenticação global: login, logout, estado do banker logado.
 * ProtectedRoute redireciona para / se não autenticado.
 */

import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { authApi, LoginRequest } from "@/services/api";

interface Banker {
  id: string;
  name: string;
  email: string;
}

interface AuthContextType {
  banker: Banker | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem("token"));
  const [banker, setBanker] = useState<Banker | null>(() => {
    const stored = localStorage.getItem("banker");
    try {
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const login = async (credentials: LoginRequest) => {
    const data = await authApi.login(credentials);
    localStorage.setItem("token", data.token);
    localStorage.setItem("banker", JSON.stringify(data.banker));
    setToken(data.token);
    setBanker(data.banker);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("banker");
    setToken(null);
    setBanker(null);
  };

  return (
    <AuthContext.Provider value={{ banker, token, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  return ctx;
}

/**
 * Envolve rotas que exigem autenticação.
 * Se não autenticado, redireciona para /.
 */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/" replace />;
  return <>{children}</>;
}
