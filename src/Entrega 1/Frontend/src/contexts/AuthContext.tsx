import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import type { Sessao } from "../types";
import { perfis } from "../types";
import { authService } from "../services";
import { setToken } from "../services/api";
function read(): Sessao | null {
  try {
    const s = JSON.parse(sessionStorage.getItem("kfka-session") || "null");
    if (
      s &&
      typeof s.id === "number" &&
      typeof s.nome === "string" &&
      typeof s.email === "string" &&
      perfis.includes(s.perfil) &&
      typeof s.token === "string"
    ) {
      const payload = JSON.parse(
        atob(s.token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
      );
      if (payload.exp * 1000 > Date.now()) return s;
    }
  } catch {
    /* sessão inválida */
  }
  sessionStorage.removeItem("kfka-session");
  return null;
}
const hadStoredSession = !!sessionStorage.getItem("kfka-session");
const initial = read();
setToken(initial?.token || null);
const AuthContext = createContext<{
  user: Sessao | null;
  token: string | null;
  perfil: Sessao["perfil"] | null;
  isAuthenticated: boolean;
  expired: boolean;
  login: (email: string, senha: string) => Promise<void>;
  logout: () => void;
} | null>(null);
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState(initial);
  const [expired, setExpired] = useState(hadStoredSession && !initial);
  function logout() {
    sessionStorage.removeItem("kfka-session");
    setToken(null);
    setUser(null);
  }
  useEffect(() => {
    const expire = () => {
      logout();
      setExpired(true);
    };
    window.addEventListener("kfka-session-expired", expire);
    let timer: ReturnType<typeof setTimeout> | undefined;
    if (user) {
      try {
        const payload = JSON.parse(
          atob(user.token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
        );
        timer = setTimeout(
          expire,
          Math.max(0, payload.exp * 1000 - Date.now()),
        );
      } catch {
        expire();
      }
    }
    return () => {
      window.removeEventListener("kfka-session-expired", expire);
      clearTimeout(timer);
    };
  }, [user]);
  async function login(email: string, senha: string) {
    const session = await authService.login(email, senha);
    sessionStorage.setItem("kfka-session", JSON.stringify(session));
    setToken(session.token);
    setUser(session);
    setExpired(false);
  }
  return (
    <AuthContext.Provider
      value={{
        user,
        token: user?.token || null,
        perfil: user?.perfil || null,
        isAuthenticated: !!user,
        expired,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
export function useAuth() {
  const c = useContext(AuthContext);
  if (!c) throw new Error("AuthProvider ausente");
  return c;
}
