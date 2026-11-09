import axios from "axios";
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  timeout: 20000,
});
let token: string | null = null;
export function setToken(value: string | null) {
  token = value;
}
api.interceptors.request.use((config) => {
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      error.config?.url !== "/usuarios/logar"
    )
      window.dispatchEvent(new Event("kfka-session-expired"));
    return Promise.reject(error);
  },
);
export function errorMessage(error: unknown): string {
  if (!axios.isAxiosError(error))
    return error instanceof Error
      ? error.message
      : "Não foi possível concluir a operação.";
  const status = error.response?.status;
  if (!error.response)
    return "Não foi possível conectar à escola. Verifique sua conexão e tente novamente.";
  if (status === 401)
    return error.config?.url === "/usuarios/logar"
      ? "E-mail ou senha inválidos."
      : "Sua sessão expirou. Entre novamente.";
  if (status === 403)
    return "Você não possui permissão para acessar este recurso.";
  if (status === 404)
    return "Registro não encontrado ou indisponível para sua conta.";
  if (status && status >= 500)
    return "Não foi possível concluir a operação. Tente novamente mais tarde.";
  const data: unknown = error.response.data;
  if (
    data &&
    typeof data === "object" &&
    "mensagem" in data &&
    typeof data.mensagem === "string"
  )
    return data.mensagem;
  return "Não foi possível concluir a operação. Confira os dados e tente novamente.";
}
export async function download(
  path: string,
  filename: string,
  params?: object,
) {
  try {
    const response = await api.get<Blob>(path, {
      params,
      responseType: "blob",
    });
    const url = URL.createObjectURL(response.data);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.append(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.data instanceof Blob) {
      try {
        error.response.data = JSON.parse(await error.response.data.text());
      } catch {
        /* erro não JSON usa mensagem genérica */
      }
    }
    throw error;
  }
}
