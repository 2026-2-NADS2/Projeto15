import { api, download } from "./api";
import type {
  Recursos,
  Recurso,
  Payloads,
  Page,
  Sessao,
  Acompanhamento,
  CadastroAcompanhamento,
  Edicao,
  Filtro,
  Historico,
  Ciencia,
  Indicadores,
  AnoLetivo,
  Auditoria,
} from "../types";
export const authService = {
  async login(email: string, senha: string) {
    return (await api.post<Sessao>("/usuarios/logar", { email, senha })).data;
  },
  async requestPasswordRecovery(email: string) {
    return (await api.post<{ mensagem: string; codigo: string | null }>("/usuarios/recuperacao-senha", { email })).data;
  },
  async resetPassword(codigo: string, novaSenha: string) {
    await api.post("/usuarios/redefinir-senha", { codigo, novaSenha });
  },
};
export const cadastroService = {
  async list<K extends Recurso>(
    resource: K,
    page = 0,
    nome?: string,
    signal?: AbortSignal,
    size = 12,
  ) {
    return (
      await api.get<Page<Recursos[K]>>(`/${resource}`, {
        params: {
          page,
          size,
          sort: "id,asc",
          ...(["alunos", "usuarios"].includes(resource) ? { nome } : {}),
        },
        signal,
      })
    ).data;
  },
  async get<K extends Recurso>(resource: K, id: number, signal?: AbortSignal) {
    return (await api.get<Recursos[K]>(`/${resource}/${id}`, { signal })).data;
  },
  async all<K extends Recurso>(resource: K, signal?: AbortSignal) {
    const first = await this.list(resource, 0, undefined, signal, 100);
    if (first.totalPages <= 1) return first.content;
    const rest = await Promise.all(Array.from({ length: first.totalPages - 1 }, (_, i) =>
      this.list(resource, i + 1, undefined, signal, 100)));
    return [first, ...rest].flatMap((page) => page.content);
  },
  async save<K extends Recurso>(
    resource: K,
    payload: Payloads[K],
    id?: number,
  ) {
    return (
      id
        ? await api.put<Recursos[K]>(`/${resource}/${id}`, payload)
        : await api.post<Recursos[K]>(`/${resource}`, payload)
    ).data;
  },
  async inactivate(resource: Exclude<Recurso, "bimestres">, id: number) {
    await api.delete(`/${resource}/${id}`);
  },
};
export type Acao =
  "enviar" | "iniciar-revisao" | "devolver" | "publicar" | "cancelar";
export const acompanhamentoService = {
  async get(id: number, signal?: AbortSignal) {
    return (await api.get<Acompanhamento>(`/acompanhamentos/${id}`, { signal }))
      .data;
  },
  async create(payload: CadastroAcompanhamento) {
    return (await api.post<Acompanhamento>("/acompanhamentos", payload)).data;
  },
  async edit(id: number, payload: Edicao, review = false) {
    return (
      await api.put<Acompanhamento>(
        `/acompanhamentos/${id}${review ? "/revisao" : ""}`,
        payload,
      )
    ).data;
  },
  async action(id: number, action: Acao) {
    return (await api.patch<Acompanhamento>(`/acompanhamentos/${id}/${action}`))
      .data;
  },
  async history(id: number, signal?: AbortSignal) {
    return (
      await api.get<Historico[]>(`/acompanhamentos/${id}/historico`, { signal })
    ).data;
  },
  async science(id: number) {
    return (
      await api.post<Ciencia>(`/acompanhamentos/${id}/ciencia`, {
        observacao: null,
      })
    ).data;
  },
  pdf(id: number) {
    return download(`/acompanhamentos/${id}/pdf`, `acompanhamento-${id}.pdf`);
  },
};
export const relatorioService = {
  async indicators(signal?: AbortSignal) {
    return (await api.get<Indicadores>("/relatorios/indicadores", { signal })).data;
  },
  async list(filters: Filtro = {}, page = 0, signal?: AbortSignal, size = 12) {
    return (
      await api.get<Page<Acompanhamento>>("/relatorios", {
        params: { ...filters, page, size, sort: "id,desc" },
        signal,
      })
    ).data;
  },
  xlsx(filters: Filtro) {
    return download("/relatorios/exportar/xlsx", "relatorio-kfka.xlsx", filters);
  },
};

export const referenciaService = {
  async years(signal?: AbortSignal) {
    return (await api.get<AnoLetivo[]>("/anos-letivos", { signal })).data;
  },
  async createYear(ano: number) {
    return (await api.post<AnoLetivo>("/anos-letivos", { ano, totalBimestres: 0 })).data;
  },
  async audit(page: number, signal?: AbortSignal) {
    return (await api.get<Page<Auditoria>>("/auditoria", { params: { page, size: 25 }, signal })).data;
  },
};
