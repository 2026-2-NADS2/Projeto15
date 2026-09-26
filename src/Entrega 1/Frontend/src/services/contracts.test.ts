import { describe, expect, it, afterEach } from "vitest";
import { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { api, errorMessage, setToken } from "./api";
import {
  cadastroService,
  acompanhamentoService,
  relatorioService,
  authService,
} from "./index";
const original = api.defaults.adapter;
afterEach(() => {
  api.defaults.adapter = original;
  setToken(null);
});
function capture() {
  const calls: InternalAxiosRequestConfig[] = [];
  api.defaults.adapter = async (config) => {
    calls.push(config);
    return {
      config,
      data: { id: 1, content: [], totalElements: 0 },
      status: 200,
      statusText: "OK",
      headers: {},
    };
  };
  return calls;
}
describe("Contratos presentes no backend Java", () => {
  it("envia Bearer e mantém busca somente nos recursos suportados", async () => {
    const calls = capture();
    setToken("token-de-teste");
    await cadastroService.list("alunos", 2, "Ana");
    await cadastroService.list("turmas", 0, "ignorado");
    expect(calls[0].headers.Authorization).toBe("Bearer token-de-teste");
    expect(calls[0].params).toMatchObject({ page: 2, size: 12, nome: "Ana" });
    expect(calls[1].params).not.toHaveProperty("nome");
  });
  it("login usa corpo real e não envia campos adicionais", async () => {
    const calls = capture();
    await authService.login("escola@test.org", "senha");
    expect(calls[0].url).toBe("/usuarios/logar");
    expect(JSON.parse(calls[0].data)).toEqual({
      email: "escola@test.org",
      senha: "senha",
    });
  });
  it("criação não adiciona professor ou status e revisão usa PUT próprio", async () => {
    const calls = capture();
    const edit = { descricao: "Evolução", media: 8.5, tagsIds: [2] };
    await acompanhamentoService.create({
      ...edit,
      alunoId: 1,
      turmaId: 3,
      disciplinaId: 4,
      bimestreId: 5,
    });
    await acompanhamentoService.edit(9, edit, true);
    await acompanhamentoService.action(9, "publicar");
    expect(calls[0].url).toBe("/acompanhamentos");
    expect(JSON.parse(calls[0].data)).not.toHaveProperty("professorId");
    expect(JSON.parse(calls[0].data)).not.toHaveProperty("status");
    expect(calls[1].url).toBe("/acompanhamentos/9/revisao");
    expect(calls[1].method).toBe("put");
    expect(calls[2].method).toBe("patch");
    expect(calls[2].data).toBeUndefined();
  });
  it("filtros são enviados no relatório paginado", async () => {
    const calls = capture();
    await relatorioService.list(
      { alunoId: 7, bimestre: 2, status: "PUBLICADO" },
      3,
    );
    expect(calls[0].url).toBe("/relatorios");
    expect(calls[0].params).toEqual({
      alunoId: 7,
      bimestre: 2,
      status: "PUBLICADO",
      page: 3,
      size: 12,
      sort: "id,desc",
    });
  });
  it("ciência registra somente a confirmação", async () => {
    const calls = capture();
    await acompanhamentoService.science(8);
    expect(calls[0].url).toBe("/acompanhamentos/8/ciencia");
    expect(JSON.parse(calls[0].data)).toEqual({ observacao: null });
  });
  it("preserva conflitos seguros e oculta erro interno", () => {
    function error(status: number, message: string) {
      return new AxiosError("erro", "ERR_BAD_RESPONSE", undefined, undefined, {
        status,
        statusText: "erro",
        headers: {},
        config: {} as InternalAxiosRequestConfig,
        data: { mensagem: message },
      });
    }
    expect(errorMessage(error(409, "Período fechado"))).toBe("Período fechado");
    expect(errorMessage(error(500, "stacktrace confidencial"))).not.toContain(
      "confidencial",
    );
    expect(errorMessage(error(403, "qualquer"))).toContain("permissão");
  });
});
