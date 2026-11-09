import { useCallback, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  ArrowLeft,
  ArrowRight,
  MagnifyingGlass,
  Plus,
  PencilSimple,
  Archive,
} from "@phosphor-icons/react";
import { cadastroService, relatorioService } from "../services";
import { useQuery, useDebounce } from "../hooks/useQuery";
import { useAuth } from "../contexts/AuthContext";
import type { Payloads, Recurso, Recursos, Perfil } from "../types";
import { perfilLabel } from "../types";
import { titles } from "../components/Layout";
import {
  Badge,
  Button,
  Card,
  ConfirmDialog,
  EmptyState,
  ErrorState,
  Field,
  Loading,
  PageHeader,
  Pagination,
  useToast,
} from "../components/ui";
import { ResourcePicker, resourceName } from "../components/ResourcePicker";
import { errorMessage } from "../services/api";
import { date } from "../utils/format";
interface SchemaField {
  name: string;
  label: string;
  type?:
    | "text"
    | "number"
    | "datetime-local"
    | "email"
    | "password"
    | "checkbox"
    | "profile"
    | "textarea";
  resource?: Recurso;
  profile?: Perfil;
  multiple?: boolean;
  min?: number;
  max?: number;
  maxLength?: number;
  immutable?: boolean;
  optional?: boolean;
}
export const schemas: Record<Recurso, SchemaField[]> = {
  alunos: [
    { name: "nome", label: "Nome completo", maxLength: 150 },
    { name: "ativo", label: "Cadastro ativo", type: "checkbox" },
  ],
  areas: [
    { name: "nome", label: "Nome da área", maxLength: 150 },
    { name: "ativo", label: "Área ativa", type: "checkbox" },
  ],
  tags: [
    { name: "nome", label: "Nome da tag", maxLength: 150 },
    {
      name: "descricao",
      label: "Descrição",
      type: "textarea",
      maxLength: 500,
      optional: true,
    },
    { name: "ativo", label: "Tag ativa", type: "checkbox" },
  ],
  usuarios: [
    { name: "nome", label: "Nome completo", maxLength: 150 },
    { name: "email", label: "E-mail", type: "email", maxLength: 254 },
    {
      name: "senha",
      label: "Senha (nova senha ao editar)",
      type: "password",
      maxLength: 72,
    },
    { name: "perfil", label: "Perfil", type: "profile" },
    { name: "ativo", label: "Usuário ativo", type: "checkbox" },
  ],
  professores: [
    {
      name: "usuarioId",
      label: "Usuário professor",
      resource: "usuarios",
      profile: "PROFESSOR",
      immutable: true,
    },
  ],
  responsaveis: [
    {
      name: "usuarioId",
      label: "Usuário responsável",
      resource: "usuarios",
      profile: "RESPONSAVEL",
      immutable: true,
    },
    {
      name: "alunosIds",
      label: "Alunos vinculados",
      resource: "alunos",
      multiple: true,
    },
  ],
  turmas: [
    { name: "nome", label: "Nome da turma", maxLength: 150 },
    { name: "serie", label: "Série", maxLength: 50 },
    {
      name: "anoLetivo",
      label: "Ano letivo",
      type: "number",
      min: 2000,
      max: 2100,
      immutable: true,
    },
    { name: "ativo", label: "Turma ativa", type: "checkbox" },
  ],
  disciplinas: [
    { name: "nome", label: "Nome da disciplina", maxLength: 150 },
    {
      name: "areaDisciplinaId",
      label: "Área de conhecimento",
      resource: "areas",
    },
    { name: "ativo", label: "Disciplina ativa", type: "checkbox" },
  ],
  bimestres: [
    {
      name: "numero",
      label: "Número do bimestre",
      type: "number",
      min: 1,
      max: 4,
      immutable: true,
    },
    {
      name: "anoLetivo",
      label: "Ano letivo",
      type: "number",
      min: 2000,
      max: 2100,
      immutable: true,
    },
    {
      name: "dataHoraAbertura",
      label: "Abertura do lançamento",
      type: "datetime-local",
    },
    {
      name: "dataHoraEncerramento",
      label: "Encerramento do lançamento",
      type: "datetime-local",
    },
  ],
  matriculas: [
    { name: "alunoId", label: "Aluno", resource: "alunos", immutable: true },
    { name: "turmaId", label: "Turma", resource: "turmas", immutable: true },
    { name: "ativo", label: "Matrícula ativa", type: "checkbox" },
  ],
  vinculos: [
    {
      name: "professorId",
      label: "Professor",
      resource: "professores",
      immutable: true,
    },
    {
      name: "disciplinaId",
      label: "Disciplina",
      resource: "disciplinas",
      immutable: true,
    },
    { name: "turmaId", label: "Turma", resource: "turmas", immutable: true },
    { name: "ativo", label: "Vínculo ativo", type: "checkbox" },
  ],
};
function description(record: Recursos[Recurso]): string {
  if ("perfil" in record)
    return `${record.email} · ${perfilLabel[record.perfil]}`;
  if ("usuario" in record)
    return `${record.usuario.email}${"alunos" in record ? ` · ${record.alunos.length} alunos vinculados` : ""}`;
  if ("serie" in record) return `${record.serie} · ${record.anoLetivo}`;
  if ("areaDisciplina" in record) return record.areaDisciplina.nome;
  if ("numero" in record)
    return `${date(record.dataHoraAbertura)} até ${date(record.dataHoraEncerramento)}`;
  if ("descricao" in record) return record.descricao || "Sem descrição";
  return `Cadastro #${record.id}`;
}
export function ResourceList({ resource }: { resource: Recurso }) {
  const { user } = useAuth();
  const admin = user?.perfil === "ADMINISTRADOR";
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const debounced = useDebounce(search);
  const [target, setTarget] = useState<number>();
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const fetcher = useCallback(
    (signal: AbortSignal) =>
      cadastroService.list(resource, page, debounced, signal),
    [resource, page, debounced],
  );
  const query = useQuery(fetcher);
  async function deactivate() {
    if (!target || resource === "bimestres") return;
    setBusy(true);
    try {
      await cadastroService.inactivate(resource, target);
      toast("Cadastro inativado.");
      setTarget(undefined);
      query.reload();
    } catch (e) {
      toast(errorMessage(e), true);
    } finally {
      setBusy(false);
    }
  }
  const title =
    !admin && resource === "alunos"
      ? user?.perfil === "RESPONSAVEL"
        ? "Meus filhos"
        : "Meus alunos"
      : !admin && resource === "turmas"
        ? "Minhas turmas"
        : titles[resource];
  return (
    <>
      <PageHeader
        title={title}
        description={
          admin
            ? "Organize os cadastros e mantenha as informações da escola atualizadas."
            : "Informações disponíveis para o seu perfil."
        }
      >
        {admin && (
          <Link className="button primary" to={`/${resource}/novo`}>
            <Plus size={18} />
            Novo cadastro
          </Link>
        )}
      </PageHeader>
      <Card>
        <div className="table-toolbar">
          <h2>
            {title}
            <span className="count">{query.data?.totalElements ?? "—"}</span>
          </h2>
          {["alunos", "usuarios"].includes(resource) && (
            <div className="search-input">
              <MagnifyingGlass size={19} />
              <input
                aria-label="Buscar por nome"
                placeholder="Buscar por nome…"
                value={search}
                onChange={(e) => {
                  setSearch(e.target.value);
                  setPage(0);
                }}
              />
              {search && (
                <Button
                  variant="ghost"
                  onClick={() => {
                    setSearch("");
                    setPage(0);
                  }}
                >
                  Limpar
                </Button>
              )}
            </div>
          )}
        </div>
        {query.loading ? (
          <Loading />
        ) : query.error ? (
          <ErrorState error={query.error} retry={query.reload} />
        ) : !query.data?.content.length ? (
          <EmptyState
            description={
              admin
                ? "Adicione um cadastro para começar ou ajuste a busca."
                : "A escola ainda não disponibilizou registros para sua conta."
            }
          />
        ) : (
          <>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Nome / identificação</th>
                    <th>Informações</th>
                    <th>Situação</th>
                    <th className="align-right">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {query.data.content.map((record) => {
                    const active =
                      "ativo" in record
                        ? record.ativo
                        : "usuario" in record
                          ? record.usuario.ativo
                          : undefined;
                    return (
                      <tr key={record.id}>
                        <td>
                          <Link
                            className="record-name"
                            to={`/${resource}/${record.id}`}
                          >
                            {resourceName(record)}
                          </Link>
                          <small className="record-id">#{record.id}</small>
                        </td>
                        <td>{description(record)}</td>
                        <td>
                          {active === undefined ? (
                            <span className="muted">Período cadastrado</span>
                          ) : (
                            <span
                              className={`simple-badge ${active ? "success" : ""}`}
                            >
                              {active ? "Ativo" : "Inativo"}
                            </span>
                          )}
                        </td>
                        <td>
                          <div className="actions align-right">
                            <Link
                              className="icon-button"
                              aria-label={`Ver ${resourceName(record)}`}
                              to={`/${resource}/${record.id}`}
                            >
                              <ArrowRight size={20} />
                            </Link>
                            {admin && (
                              <Link
                                className="icon-button"
                                aria-label={`Editar ${resourceName(record)}`}
                                to={`/${resource}/${record.id}/editar`}
                              >
                                <PencilSimple size={19} />
                              </Link>
                            )}
                            {admin && active && resource !== "bimestres" && (
                              <Button
                                variant="ghost"
                                aria-label={`Inativar ${resourceName(record)}`}
                                onClick={() => setTarget(record.id)}
                              >
                                <Archive size={18} />
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination page={query.data} onChange={setPage} />
          </>
        )}
      </Card>
      {target && (
        <ConfirmDialog
          title="Inativar cadastro?"
          description="O cadastro ficará inativo. As informações e o histórico serão preservados."
          confirm={deactivate}
          close={() => setTarget(undefined)}
          busy={busy}
        />
      )}
    </>
  );
}
function payloadFrom(
  resource: Recurso,
  record?: Recursos[Recurso],
): Record<string, unknown> {
  const value: Record<string, unknown> = { ativo: true, alunosIds: [] };
  if (!record) return value;
  const source = record as unknown as Record<string, unknown>;
  for (const field of schemas[resource]) {
    if (field.name === "senha") continue;
    if (field.name === "alunosIds" && "alunos" in record)
      value.alunosIds = record.alunos.map((a) => a.id);
    else if (field.name.endsWith("Id")) {
      const relation = field.name.replace(/Id$/, "");
      const r = source[relation] as { id: number } | undefined;
      value[field.name] = r?.id;
    } else value[field.name] = source[field.name] ?? "";
  }
  return value;
}
export function ResourceForm({ resource }: { resource: Recurso }) {
  const { id } = useParams();
  const fetcher = useCallback(
    (signal: AbortSignal) =>
      id
        ? cadastroService.get(resource, Number(id), signal)
        : Promise.resolve(undefined),
    [resource, id],
  );
  const query = useQuery(fetcher);
  return (
    <>
      <PageHeader
        title={`${id ? "Editar cadastro" : "Novo cadastro"} · ${titles[resource]}`}
        description="Preencha os dados do cadastro."
      />
      <Link className="back-link" to={`/${resource}`}>
        <ArrowLeft />
        Voltar à lista
      </Link>
      {!id && (resource === "professores" || resource === "responsaveis") ? (
        <ProfileRegistration resource={resource} />
      ) : query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : (
        <ResourceFormContent
          key={`${resource}-${id || "new"}`}
          resource={resource}
          record={query.data}
        />
      )}
    </>
  );
}
function ProfileRegistration({resource}:{resource:"professores"|"responsaveis"}){
  const [name,setName]=useState(""),[email,setEmail]=useState(""),[password,setPassword]=useState(""),[students,setStudents]=useState<number[]>([]),[busy,setBusy]=useState(false),[error,setError]=useState("");
  const navigate=useNavigate();const toast=useToast();const profile=resource==="professores"?"PROFESSOR":"RESPONSAVEL";
  async function submit(e:FormEvent){e.preventDefault();setError("");if(password.length<8){setError("A senha deve ter pelo menos 8 caracteres.");return}setBusy(true);try{const user=await cadastroService.save("usuarios",{nome:name,email,senha:password,perfil:profile,ativo:true});const saved=resource==="professores"?await cadastroService.save("professores",{usuarioId:user.id}):await cadastroService.save("responsaveis",{usuarioId:user.id,alunosIds:students});toast("Cadastro salvo com sucesso.");navigate(`/${resource}/${saved.id}`)}catch(e){setError(errorMessage(e))}finally{setBusy(false)}}
  return <Card className="form-card"><form onSubmit={submit}><div className="form-grid"><Field label="Nome completo"><input required maxLength={150} value={name} onChange={e=>setName(e.target.value)} disabled={busy}/></Field><Field label="E-mail"><input required type="email" maxLength={254} value={email} onChange={e=>setEmail(e.target.value)} disabled={busy}/></Field><Field label="Senha inicial" hint="Mínimo de 8 caracteres."><input required type="password" minLength={8} maxLength={72} value={password} onChange={e=>setPassword(e.target.value)} disabled={busy}/></Field>{resource==="responsaveis"&&<Field label="Alunos vinculados"><ResourcePicker resource="alunos" multiple value={students} onChange={v=>setStudents(v as number[])} disabled={busy}/></Field>}</div>{error&&<p className="inline-error" role="alert">{error}</p>}<div className="form-actions"><Button disabled={busy}>{busy?"Salvando…":"Salvar cadastro"}</Button></div></form></Card>
}
function relationshipLabels(
  record: Recursos[Recurso] | undefined,
  field: SchemaField,
): Record<number, string> {
  if (!record) return {};
  if (field.multiple && "alunos" in record)
    return Object.fromEntries(
      record.alunos.map((aluno) => [aluno.id, aluno.nome]),
    );
  const relation = (record as unknown as Record<string, unknown>)[
    field.name.replace(/Id$/, "")
  ];
  if (relation && typeof relation === "object" && "id" in relation) {
    const linked = relation as Recursos[Recurso];
    return { [linked.id]: resourceName(linked) };
  }
  return {};
}

function ResourceFormContent({
  resource,
  record,
}: {
  resource: Recurso;
  record?: Recursos[Recurso];
}) {
  const [values, setValues] = useState(() => payloadFrom(resource, record));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const toast = useToast();
  const navigate = useNavigate();
  async function submit(e: FormEvent) {
    e.preventDefault();
    setError("");
    if (
      resource === "bimestres" &&
      String(values.dataHoraEncerramento) <= String(values.dataHoraAbertura)
    ) {
      setError("O encerramento deve ser posterior à abertura.");
      return;
    }
    if (
      typeof values.senha === "string" &&
      (values.senha.length < 8 ||
        new TextEncoder().encode(values.senha).length > 72)
    ) {
      setError("A senha deve ter de 8 a 72 caracteres e até 72 bytes UTF-8.");
      return;
    }
    setBusy(true);
    try {
      const payload: Record<string, unknown> = {};
      for (const f of schemas[resource])
        payload[f.name] =
          values[f.name] ?? (f.multiple ? [] : f.optional ? null : "");
      const saved = await cadastroService.save(
        resource,
        payload as unknown as Payloads[typeof resource],
        record?.id,
      );
      toast("Cadastro salvo com sucesso.");
      navigate(`/${resource}/${saved.id}`);
    } catch (error) {
      setError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }
  return (
    <Card className="form-card">
      <form onSubmit={submit}>
        <div className="form-grid">
          {schemas[resource].map((field) => {
            const disabled = busy || (!!record && !!field.immutable);
            const value = values[field.name];
            return (
              <Field
                key={field.name}
                label={field.label}
                hint={
                  record && field.immutable
                    ? "Esta informação não pode ser alterada neste cadastro."
                    : field.type === "datetime-local"
                      ? `Horário da escola (${import.meta.env.VITE_FUSO_HORARIO || "America/Sao_Paulo"}). O lançamento encerra no horário informado.`
                      : undefined
                }
              >
                {field.resource ? (
                  <ResourcePicker
                    resource={field.resource}
                    value={value as number | number[] | undefined}
                    onChange={(v) =>
                      setValues((x) => ({ ...x, [field.name]: v }))
                    }
                    multiple={field.multiple}
                    disabled={disabled}
                    profile={field.profile}
                    initialLabels={relationshipLabels(record, field)}
                  />
                ) : field.type === "checkbox" ? (
                  <span className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={!!value}
                      disabled={disabled}
                      onChange={(e) =>
                        setValues((x) => ({
                          ...x,
                          [field.name]: e.target.checked,
                        }))
                      }
                    />
                    Sim
                  </span>
                ) : field.type === "profile" ? (
                  <select
                    value={String(value || "")}
                    required
                    disabled={disabled}
                    onChange={(e) =>
                      setValues((x) => ({ ...x, [field.name]: e.target.value }))
                    }
                  >
                    <option value="">Selecione…</option>
                    {Object.entries(perfilLabel).map(([p, label]) => (
                      <option key={p} value={p}>
                        {label}
                      </option>
                    ))}
                  </select>
                ) : field.type === "textarea" ? (
                  <textarea
                    value={String(value || "")}
                    maxLength={field.maxLength}
                    disabled={disabled}
                    onChange={(e) =>
                      setValues((x) => ({ ...x, [field.name]: e.target.value }))
                    }
                  />
                ) : (
                  <input
                    type={field.type || "text"}
                    value={value === undefined ? "" : String(value)}
                    required={!field.optional}
                    min={field.min}
                    max={field.max}
                    step={field.type === "datetime-local" ? 1 : undefined}
                    minLength={field.type === "password" ? 8 : undefined}
                    maxLength={field.maxLength}
                    disabled={disabled}
                    autoComplete={
                      field.type === "password" ? "new-password" : undefined
                    }
                    onChange={(e) =>
                      setValues((x) => ({
                        ...x,
                        [field.name]:
                          field.type === "number"
                            ? e.target.value
                              ? Number(e.target.value)
                              : ""
                            : e.target.value,
                      }))
                    }
                  />
                )}
              </Field>
            );
          })}
        </div>
        {error && (
          <p className="inline-error" role="alert">
            {error}
          </p>
        )}
        <div className="form-actions">
          <Link className="button secondary" to={`/${resource}`}>
            Cancelar
          </Link>
          <Button type="submit" disabled={busy}>
            {busy ? "Salvando…" : "Salvar cadastro"}
          </Button>
        </div>
      </form>
    </Card>
  );
}
export function ResourceDetail({ resource }: { resource: Recurso }) {
  const { id } = useParams();
  const { user } = useAuth();
  const fetcher = useCallback(
    (signal: AbortSignal) => cadastroService.get(resource, Number(id), signal),
    [resource, id],
  );
  const query = useQuery(fetcher);
  return (
    <>
      <Link className="back-link" to={`/${resource}`}>
        <ArrowLeft />
        Voltar à lista
      </Link>
      {query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : (
        query.data && (
          <>
            <PageHeader
              title={resourceName(query.data)}
              description={description(query.data)}
            >
              {user?.perfil === "ADMINISTRADOR" && (
                <Link
                  className="button secondary"
                  to={`/${resource}/${id}/editar`}
                >
                  <PencilSimple />
                  Editar cadastro
                </Link>
              )}
            </PageHeader>
            <Card className="detail-card">
              <h2>Informações do cadastro</h2>
              <dl className="detail-grid">
                {schemas[resource]
                  .filter((f) => f.name !== "senha")
                  .map((f) => (
                    <div key={f.name}>
                      <dt>{f.label}</dt>
                      <dd>{detailValue(query.data!, f.name)}</dd>
                    </div>
                  ))}
              </dl>
            </Card>
            {(resource === "professores" || resource === "responsaveis") && "usuario" in query.data && (
              <ProfileAccount record={query.data} reload={query.reload} />
            )}
            {resource === "alunos" && (
              <RelatedRecords filters={{ alunoId: Number(id) }} />
            )}{" "}
            {resource === "turmas" && (
              <>
                <RelatedRecords filters={{ turmaId: Number(id) }} />
                {user?.perfil !== "RESPONSAVEL" && (
                  <ClassRelations turmaId={Number(id)} />
                )}
              </>
            )}
          </>
        )
      )}
    </>
  );
}
function ProfileAccount({record,reload}:{record:Recursos["professores"]|Recursos["responsaveis"];reload:()=>void}){
 const [password,setPassword]=useState(""),[busy,setBusy]=useState(false),[error,setError]=useState("");const toast=useToast();
 if(record.usuario.ativo)return <Card className="detail-card mt"><h2>Situação da conta</h2><p className="notice success">Conta ativa. A inativação pode ser feita na lista de cadastros.</p></Card>;
 async function reactivate(e:FormEvent){e.preventDefault();setError("");if(password.length<8){setError("Informe uma nova senha com pelo menos 8 caracteres.");return}setBusy(true);try{await cadastroService.save("usuarios",{nome:record.usuario.nome,email:record.usuario.email,senha:password,perfil:record.usuario.perfil,ativo:true},record.usuario.id);toast("Conta reativada. A nova senha já pode ser utilizada.");reload()}catch(e){setError(errorMessage(e))}finally{setBusy(false)}}
 return <Card className="form-card mt"><h2>Reativar conta</h2><p>Defina uma nova senha inicial para reativar o acesso deste cadastro.</p><form onSubmit={reactivate}><Field label="Nova senha inicial"><input type="password" minLength={8} maxLength={72} required value={password} onChange={e=>setPassword(e.target.value)} disabled={busy}/></Field>{error&&<p className="inline-error" role="alert">{error}</p>}<Button disabled={busy}>{busy?"Reativando…":"Reativar conta"}</Button></form></Card>
}
function detailValue(record: Recursos[Recurso], name: string): string {
  if (name === "alunosIds" && "alunos" in record)
    return record.alunos.map((a) => a.nome).join(", ") || "Nenhum aluno";
  if (name.endsWith("Id")) {
    const r = (record as unknown as Record<string, unknown>)[
      name.replace(/Id$/, "")
    ] as Recursos[Recurso] | undefined;
    return r ? resourceName(r) : "Não informado";
  }
  const v = (record as unknown as Record<string, unknown>)[name];
  if (typeof v === "boolean") return v ? "Sim" : "Não";
  if (name.startsWith("dataHora")) return date(String(v));
  if (name === "perfil") return perfilLabel[v as Perfil];
  return v === undefined || v === null || v === ""
    ? "Não informado"
    : String(v);
}
function RelatedRecords({
  filters,
}: {
  filters: { alunoId?: number; turmaId?: number };
}) {
  const [page, setPage] = useState(0);
  const fetcher = useCallback(
    (s: AbortSignal) => relatorioService.list(filters, page, s),
    [filters.alunoId, filters.turmaId, page],
  );
  const query = useQuery(fetcher);
  return (
    <Card className="mt">
      <div className="table-toolbar">
        <h2>Acompanhamentos</h2>
        <Link
          className="text-link"
          to={`/acompanhamentos?${new URLSearchParams(Object.entries(filters).map(([k, v]) => [k, String(v)]))}`}
        >
          Ver todos
          <ArrowRight />
        </Link>
      </div>
      {query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : !query.data?.content.length ? (
        <EmptyState title="Nenhum acompanhamento disponível" />
      ) : (
        <>
          <div className="record-list">
            {query.data.content.map((a) => (
              <Link
                className="record-row"
                key={a.id}
                to={`/acompanhamentos/${a.id}`}
              >
                <div>
                  <strong>
                    {a.aluno.nome} · {a.disciplina.nome}
                  </strong>
                  <p>
                    {a.bimestre.numero}º bimestre · {a.bimestre.anoLetivo}
                  </p>
                </div>
                <Badge status={a.status} />
                <ArrowRight />
              </Link>
            ))}
          </div>
          <Pagination page={query.data} onChange={setPage} />
        </>
      )}
    </Card>
  );
}
function ClassRelations({ turmaId }: { turmaId: number }) {
  const [page, setPage] = useState(0);
  const fetcher = useCallback(
    async (signal: AbortSignal) => {
      const [matriculas, vinculos] = await Promise.all([
        cadastroService.list("matriculas", page, undefined, signal, 20),
        cadastroService.list("vinculos", page, undefined, signal, 20),
      ]);
      return { matriculas, vinculos };
    },
    [page],
  );
  const query = useQuery(fetcher);
  return (
    <Card className="mt">
      <div className="table-toolbar">
        <h2>Alunos e vínculos da turma</h2>
      </div>
      <p className="form-hint">
        Consulta paginada das matrículas e vínculos acessíveis. Cada página
        mostra as associações desta turma nela encontradas.
      </p>
      {query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : (
        query.data && (
          <div className="detail-card">
            <h3>Alunos nesta página</h3>
            {query.data.matriculas.content
              .filter((m) => m.turma.id === turmaId && m.ativo)
              .map((m) => (
                <p key={m.id}>
                  <Link className="text-link" to={`/alunos/${m.aluno.id}`}>
                    {m.aluno.nome}
                    <ArrowRight />
                  </Link>
                </p>
              ))}
            <h3>Disciplinas e professores nesta página</h3>
            {query.data.vinculos.content
              .filter((v) => v.turma.id === turmaId && v.ativo)
              .map((v) => (
                <p key={v.id}>
                  {v.disciplina.nome} · {v.professor.usuario.nome}
                </p>
              ))}
            <div className="actions">
              <Button
                variant="secondary"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Anterior
              </Button>
              <span>Página {page + 1}</span>
              <Button
                variant="secondary"
                disabled={
                  query.data.matriculas.last && query.data.vinculos.last
                }
                onClick={() => setPage((p) => p + 1)}
              >
                Próxima
              </Button>
            </div>
          </div>
        )
      )}
    </Card>
  );
}
