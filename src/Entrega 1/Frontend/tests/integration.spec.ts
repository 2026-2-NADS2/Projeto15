import { test, expect, type Page } from "@playwright/test";
import fs from "node:fs";
import crypto from "node:crypto";
const apiURL = process.env.KFKA_TEST_API || "http://localhost:8081";
const accountFile = process.env.KFKA_TEST_ACCOUNT_FILE;

async function login(page: Page, email: string, senha: string) {
  await page.goto("/login");
  await page.getByLabel("E-mail", { exact: true }).fill(email);
  await page.getByLabel("Senha", { exact: true }).fill(senha);
  await page.getByRole("button", { name: "Entrar na plataforma" }).click();
  await expect(page).toHaveURL(/dashboard/);
}
async function logout(page: Page) {
  await page.getByRole("button", { name: "Menu da conta" }).click();
  await page.getByRole("button", { name: "Sair da plataforma" }).click();
  await expect(page).toHaveURL(/login/);
}
async function confirm(page: Page, name: string) {
  await page.getByRole("button", { name, exact: true }).click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Confirmar", exact: true })
    .click();
  await expect(page.getByRole("dialog")).not.toBeVisible();
}
test("fluxo real: cadastros, professor, devolução, revisão, família, ciência e arquivos", async ({
  page,
  request,
}) => {
  test.skip(
    !accountFile,
    "Forneça KFKA_TEST_ACCOUNT_FILE e uma API de testes descartável.",
  );
  test.setTimeout(180000);
  const admin = JSON.parse(fs.readFileSync(accountFile!, "utf8")) as {
    email: string;
    senha: string;
  };
  const authResponse = await request.post(`${apiURL}/usuarios/logar`, {
    data: admin,
  });
  expect(authResponse.ok()).toBeTruthy();
  const auth = await authResponse.json();
  const headers = { Authorization: `Bearer ${auth.token}` };
  const suffix = crypto.randomBytes(4).toString("hex");
  const senha = crypto.randomBytes(15).toString("hex");
  async function create(resource: string, data: object) {
    const response = await request.post(`${apiURL}/${resource}`, {
      headers,
      data,
    });
    expect(response.status(), `${resource}: ${await response.text()}`).toBe(
      201,
    );
    return response.json();
  }
  const professorUser = await create("usuarios", {
    nome: `Professora Ana ${suffix}`,
    email: `prof-${suffix}@test.org`,
    senha,
    perfil: "PROFESSOR",
    ativo: true,
  });
  const parentUser = await create("usuarios", {
    nome: `Responsável Maria ${suffix}`,
    email: `pais-${suffix}@test.org`,
    senha,
    perfil: "RESPONSAVEL",
    ativo: true,
  });
  const professor = await create("professores", {
    usuarioId: professorUser.id,
  });
  const aluno = await create("alunos", { nome: `João ${suffix}`, ativo: true });
  const stranger = await create("alunos", {
    nome: `Outro aluno ${suffix}`,
    ativo: true,
  });
  const parent = await create("responsaveis", {
    usuarioId: parentUser.id,
    alunosIds: [aluno.id],
  });
  const area = await create("areas", {
    nome: `Linguagens ${suffix}`,
    ativo: true,
  });
  const disciplina = await create("disciplinas", {
    nome: `Português ${suffix}`,
    areaDisciplinaId: area.id,
    ativo: true,
  });
  const year = new Date().getFullYear();
  const turma = await create("turmas", {
    nome: `5º A ${suffix}`,
    serie: "5º ano",
    anoLetivo: year,
    ativo: true,
  });
  const matricula = await create("matriculas", {
    alunoId: aluno.id,
    turmaId: turma.id,
    ativo: true,
  });
  const vinculo = await create("vinculos", {
    professorId: professor.id,
    disciplinaId: disciplina.id,
    turmaId: turma.id,
    ativo: true,
  });
  const periods = await (
    await request.get(`${apiURL}/bimestres`, { headers })
  ).json();
  const bimestre =
    periods.content[0] ||
    (await create("bimestres", {
      numero: 1,
      anoLetivo: year,
      dataHoraAbertura: `${year}-01-01T00:00:00`,
      dataHoraEncerramento: `${year}-12-31T23:59:59`,
    }));
  const tag = await create("tags", {
    nome: `Participação ${suffix}`,
    descricao: "Participa das atividades",
    ativo: true,
  });
  const errors: string[] = [];
  page.on("pageerror", (e) => errors.push(e.message));
  await login(page, admin.email, admin.senha);
  await expect(
    page.getByRole("heading", { name: /Olá,/ }),
  ).toBeVisible();
  await page.goto("/alunos/novo");
  await page.getByLabel("Nome completo").fill(`Aluno UI ${suffix}`);
  await page.getByRole("button", { name: "Salvar cadastro" }).click();
  await expect(
    page.getByRole("heading", { name: `Aluno UI ${suffix}` }),
  ).toBeVisible();
  const uiId = Number(page.url().split("/").pop());
  await page.getByRole("link", { name: "Editar cadastro" }).click();
  await page.getByLabel("Nome completo").fill(`Aluno UI editado ${suffix}`);
  await page.getByRole("button", { name: "Salvar cadastro" }).click();
  await expect(
    page.getByRole("heading", { name: `Aluno UI editado ${suffix}` }),
  ).toBeVisible();
  for (const [resource, id] of [
    ["professores", professor.id],
    ["responsaveis", parent.id],
    ["areas", area.id],
    ["disciplinas", disciplina.id],
    ["turmas", turma.id],
    ["matriculas", matricula.id],
    ["vinculos", vinculo.id],
    ["bimestres", bimestre.id],
    ["tags", tag.id],
  ] as const) {
    await page.goto(`/${resource}/${id}/editar`);
    await expect(
      page.getByRole("button", { name: "Salvar cadastro" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Salvar cadastro" }).click();
    await expect(page).toHaveURL(new RegExp(`/${resource}/${id}$`));
  }
  await logout(page);
  await login(page, professorUser.email, senha);
  await expect(page.getByRole("heading", { name: /Olá,/ })).toBeVisible();
  await page.goto("/turmas");
  await expect(
    page.getByRole("heading", { name: "Acesso não permitido" }),
  ).toBeVisible();
  await page.goto("/acompanhamentos/novo");
  await page.getByLabel("Turma", { exact: true }).selectOption(String(turma.id));
  await page.getByLabel("Disciplina", { exact: true }).selectOption(String(vinculo.id));
  await page
    .getByLabel("Selecionar matrículas")
    .selectOption(String(matricula.id));
  await page
    .getByLabel("Selecionar bimestres")
    .selectOption(String(bimestre.id));
  await page.getByLabel("Média informada").fill("8.5");
  await page
    .getByLabel("Descrição do acompanhamento")
    .fill("Boa evolução na leitura e participação nas atividades.");
  await page
    .getByRole("button", { name: "Salvar rascunho", exact: true })
    .click();
  await expect(page).toHaveURL(/acompanhamentos\/\d+$/);
  const recordId = Number(page.url().split("/").pop());
  await confirm(page, "Enviar para revisão");
  await expect(
    page.getByText("Aguardando a revisão da escola.", { exact: true }),
  ).toBeVisible();
  await logout(page);
  await login(page, admin.email, admin.senha);
  await page.goto(`/acompanhamentos/${recordId}`);
  await confirm(page, "Iniciar revisão");
  await confirm(page, "Devolver para ajustes");
  await logout(page);
  await login(page, professorUser.email, senha);
  await page.goto(`/acompanhamentos/${recordId}/editar`);
  await page
    .getByLabel("Descrição do acompanhamento")
    .fill("Boa evolução na leitura. Acompanhamento ajustado.");
  await page
    .getByRole("button", { name: "Salvar e enviar para revisão" })
    .click();
  await page
    .getByRole("dialog")
    .getByRole("button", { name: "Confirmar", exact: true })
    .click();
  await expect(page).toHaveURL(new RegExp(`/acompanhamentos/${recordId}$`));
  await logout(page);
  await login(page, admin.email, admin.senha);
  await page.goto(`/acompanhamentos/${recordId}`);
  await confirm(page, "Iniciar revisão");
  await page.getByRole("link", { name: "Editar revisão" }).click();
  await page.getByLabel("Média informada").fill("9");
  await page.getByRole("button", { name: "Salvar revisão" }).click();
  await confirm(page, "Publicar");
  await page.goto("/relatorios");
  await page.getByRole("button", { name: "Filtros", exact: true }).click();
  await page.getByLabel("Ano letivo", { exact: true }).fill(String(year));
  await page.getByRole("button", { name: "Aplicar filtros" }).click();
  await expect(page).toHaveURL(/anoLetivo=/);
  const csvPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: "Exportar CSV" }).click();
  const csv = await csvPromise;
  expect(csv.suggestedFilename()).toBe("relatorio-kfka.csv");
  await logout(page);
  await login(page, parentUser.email, senha);
  await expect(
    page.getByRole("heading", { name: /Olá,/ }),
  ).toBeVisible();
  await page.getByLabel("Selecionar alunos").selectOption(String(aluno.id));
  await expect(
    page.getByText("Boa evolução na leitura. Acompanhamento ajustado.", {
      exact: true,
    }),
  ).toBeVisible();
  await expect(
    page.getByText(stranger.nome, { exact: true }),
  ).not.toBeVisible();
  await page.goto(`/acompanhamentos/${recordId}`);
  await confirm(page, "Confirmar ciência");
  await expect(page.getByText(/Ciência registrada em/)).toBeVisible();
  const pdfPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: "Baixar PDF" }).click();
  expect((await pdfPromise).suggestedFilename()).toBe(
    `acompanhamento-${recordId}.pdf`,
  );
  await page.reload();
  await confirm(page, "Confirmar ciência");
  await expect(
    page.getByText("A escola já recebeu sua ciência deste acompanhamento."),
  ).toBeVisible();
  await page.goto(`/alunos/${stranger.id}`);
  await expect(
    page.getByText("Registro não encontrado ou indisponível para sua conta."),
  ).toBeVisible();
  await page.goto("/dashboard");
  await page.setViewportSize({ width: 390, height: 844 });
  await page.getByRole("button", { name: "Abrir menu" }).click();
  await expect(page.locator(".sidebar")).toHaveClass(/drawer-open/);
  await page.keyboard.press("Escape");
  await expect(page.locator(".sidebar")).not.toHaveClass(/drawer-open/);
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBeTruthy();
  await page.screenshot({
    path: test.info().outputPath("family-mobile-dark.png"),
    fullPage: true,
  });
  await page.setViewportSize({ width: 1440, height: 1000 });
  await logout(page);
  await login(page, admin.email, admin.senha);
  await page.screenshot({
    path: test.info().outputPath("admin-desktop-light.png"),
    fullPage: true,
  });
  await page.goto("/alunos");
  const uiRow = page
    .getByRole("row")
    .filter({ hasText: `Aluno UI editado ${suffix}` });
  if (!(await uiRow.count())) {
    await page.getByLabel("Buscar por nome").fill(`Aluno UI editado ${suffix}`);
  }
  await confirm(page, `Inativar Aluno UI editado ${suffix}`);
  expect(
    (await (await request.get(`${apiURL}/alunos/${uiId}`, { headers })).json())
      .ativo,
  ).toBe(false);
  expect(errors).toEqual([]);
});

test("sessão revogada pelo backend retorna ao login", async ({
  page,
  request,
}) => {
  test.skip(!accountFile, "Forneça conta e backend descartável.");
  const admin = JSON.parse(fs.readFileSync(accountFile!, "utf8"));
  const auth = await (
    await request.post(`${apiURL}/usuarios/logar`, { data: admin })
  ).json();
  const headers = { Authorization: `Bearer ${auth.token}` };
  const senha = crypto.randomBytes(15).toString("hex");
  const email = `revogado-${crypto.randomBytes(4).toString("hex")}@test.org`;
  const userResponse = await request.post(`${apiURL}/usuarios`, {
    headers,
    data: {
      nome: "Usuário para verificação de sessão",
      email,
      senha,
      perfil: "PROFESSOR",
      ativo: true,
    },
  });
  expect(userResponse.status()).toBe(201);
  const user = await userResponse.json();
  await login(page, email, senha);
  expect(
    (
      await request.delete(`${apiURL}/usuarios/${user.id}`, { headers })
    ).status(),
  ).toBe(204);
  await page.goto("/acompanhamentos");
  await expect(page).toHaveURL(/login/);
  await expect(page.getByRole("alert")).toContainText(
    "Sua sessão expirou. Entre novamente.",
  );
});
