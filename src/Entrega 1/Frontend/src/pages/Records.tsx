import { useCallback, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import {
  ArrowRight,
  DownloadSimple,
  FunnelSimple,
  Plus,
  X,
} from "@phosphor-icons/react";
import { useAuth } from "../contexts/AuthContext";
import { useQuery } from "../hooks/useQuery";
import { relatorioService } from "../services";
import type { Filtro, Recurso, Status } from "../types";
import { statuses, statusLabel } from "../types";
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Field,
  Loading,
  PageHeader,
  Pagination,
  useToast,
} from "../components/ui";
import { ResourcePicker } from "../components/ResourcePicker";
import { errorMessage } from "../services/api";
import { date, number } from "../utils/format";
export function parseFilters(params: URLSearchParams): Filtro {
  const filters: Filtro = {};
  for (const key of [
    "turmaId",
    "disciplinaId",
    "professorId",
    "bimestreId",
    "alunoId",
    "tagId",
    "anoLetivo",
    "bimestre",
  ] as const) {
    const value = Number(params.get(key));
    if (Number.isInteger(value) && value > 0) filters[key] = value;
  }
  const status = params.get("status");
  if (statuses.includes(status as Status)) filters.status = status as Status;
  return filters;
}
export function Records({ report = false }: { report?: boolean }) {
  const { user } = useAuth();
  const responsible = user?.perfil === "RESPONSAVEL";
  const admin = user?.perfil === "ADMINISTRADOR";
  const [params, setParams] = useSearchParams();
  const filters = parseFilters(params);
  const signature = JSON.stringify(filters);
  const page = Math.max(0, Number(params.get("page")) || 0);
  const [showFilters, setShowFilters] = useState(false);
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const fetcher = useCallback(
    (signal: AbortSignal) =>
      relatorioService.list(JSON.parse(signature) as Filtro, page, signal),
    [signature, page],
  );
  const query = useQuery(fetcher);
  function filter(newFilters: Filtro) {
    const p = new URLSearchParams();
    for (const [key, value] of Object.entries(newFilters))
      if (value !== undefined) p.set(key, String(value));
    setParams(p);
  }
  async function exportarExcel() {
    setBusy(true);
    try {
      await relatorioService.xlsx(filters);
      toast("Relatório exportado.");
    } catch (e) {
      toast(errorMessage(e), true);
    } finally {
      setBusy(false);
    }
  }
  const title = report ? "Relatórios" : "Acompanhamentos";
  return (
    <>
      <PageHeader
        title={title}
        description={
          responsible
            ? "Acompanhe as observações dos professores e as médias publicadas pela escola."
            : report
              ? "Consulte os registros por turma, disciplina e período."
              : "Cada registro conta uma parte da trajetória de aprendizagem."
        }
      >
        {user?.perfil === "PROFESSOR" && !report && (
          <Link className="button primary" to="/acompanhamentos/novo">
            <Plus />
            Novo acompanhamento
          </Link>
        )}
        {admin && report && (
          <Button disabled={busy} onClick={exportarExcel}>
            <DownloadSimple />
            {busy ? "Exportando…" : "Exportar Excel"}
          </Button>
        )}
      </PageHeader>
      <Card>
        <div className="table-toolbar">
          <div>
            <h2>
              {filters.status
                ? statusLabel[filters.status]
                : responsible
                  ? "Publicações da escola"
                  : "Todos os registros"}
              <span className="count">{query.data?.totalElements ?? "—"}</span>
            </h2>
          </div>
          <div className="actions">
            {!!Object.keys(filters).length && (
              <Button variant="ghost" onClick={() => filter({})}>
                <X />
                Limpar filtros
              </Button>
            )}
            <Button
              variant="secondary"
              aria-expanded={showFilters}
              onClick={() => setShowFilters(!showFilters)}
            >
              <FunnelSimple size={18} />
              Filtros
            </Button>
          </div>
        </div>
        {showFilters && (
          <RecordFilters
            key={signature}
            initial={filters}
            submit={filter}
            responsible={responsible}
          />
        )}{" "}
        {query.loading ? (
          <Loading />
        ) : query.error ? (
          <ErrorState error={query.error} retry={query.reload} />
        ) : !query.data?.content.length ? (
          <EmptyState
            title={
              responsible
                ? "Ainda não há publicações neste período"
                : "Nenhum acompanhamento encontrado"
            }
            description="Experimente outro período ou limpe os filtros."
          />
        ) : (
          <>
            {responsible ? (
              <div className="family-records">
                {query.data.content.map((a) => (
                  <Link
                    key={a.id}
                    className="family-record"
                    to={`/acompanhamentos/${a.id}`}
                  >
                    <div className="record-top">
                      <span className="subject-icon">
                        {a.disciplina.nome.slice(0, 2).toUpperCase()}
                      </span>
                      <Badge status={a.status} />
                    </div>
                    <h3>{a.disciplina.nome}</h3>
                    <p>
                      {a.aluno.nome} · {a.turma.nome}
                    </p>
                    <p>
                      {a.bimestre.numero}º bimestre · {a.bimestre.anoLetivo}
                    </p>
                    <div className="family-average">
                      <span>Média informada</span>
                      <strong>{number(a.media)}</strong>
                    </div>
                    <p className="description-clamp">{a.descricao}</p>
                    <span className="text-link">
                      Ver acompanhamento
                      <ArrowRight />
                    </span>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="table-scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Aluno</th>
                      <th>Turma / disciplina</th>
                      <th>Professor</th>
                      <th>Período</th>
                      <th>Média</th>
                      <th>Situação</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {query.data.content.map((a) => (
                      <tr key={a.id}>
                        <td>
                          <Link
                            className="record-name"
                            to={`/acompanhamentos/${a.id}`}
                          >
                            {a.aluno.nome}
                          </Link>
                          <small className="record-id">
                            Atualizado em {date(a.dataAtualizacao)}
                          </small>
                        </td>
                        <td>
                          <strong>{a.disciplina.nome}</strong>
                          <small className="record-id">{a.turma.nome}</small>
                        </td>
                        <td>{a.professor.nome}</td>
                        <td>
                          {a.bimestre.numero}º bimestre
                          <small className="record-id">
                            {a.bimestre.anoLetivo}
                          </small>
                        </td>
                        <td className="average">{number(a.media)}</td>
                        <td>
                          <Badge status={a.status} />
                        </td>
                        <td>
                          <Link
                            aria-label={`Ver acompanhamento de ${a.aluno.nome} em ${a.disciplina.nome}`}
                            className="icon-button"
                            to={`/acompanhamentos/${a.id}`}
                          >
                            <ArrowRight size={21} />
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <Pagination
              page={query.data}
              onChange={(p) => {
                const next = new URLSearchParams(params);
                next.set("page", String(p));
                setParams(next);
              }}
            />
          </>
        )}
      </Card>
    </>
  );
}
function RecordFilters({
  initial,
  submit,
  responsible,
}: {
  initial: Filtro;
  submit: (filters: Filtro) => void;
  responsible: boolean;
}) {
  const [values, setValues] = useState<Filtro>(initial);
  const refs: { key: keyof Filtro; label: string; resource: Recurso }[] = [
    {
      key: "alunoId",
      label: responsible ? "Aluno" : "Aluno",
      resource: "alunos",
    },
    ...(!responsible
      ? [
          {
            key: "turmaId" as const,
            label: "Turma",
            resource: "turmas" as const,
          },
          {
            key: "disciplinaId" as const,
            label: "Disciplina",
            resource: "disciplinas" as const,
          },
          {
            key: "professorId" as const,
            label: "Professor",
            resource: "professores" as const,
          },
          {
            key: "bimestreId" as const,
            label: "Bimestre cadastrado",
            resource: "bimestres" as const,
          },
          { key: "tagId" as const, label: "Tag", resource: "tags" as const },
        ]
      : []),
  ];
  function apply(e: FormEvent) {
    e.preventDefault();
    submit(values);
  }
  return (
    <form className="filter-panel" onSubmit={apply}>
      <div className="form-grid">
        {refs.map((ref) => (
          <Field label={ref.label} key={ref.key}>
            <ResourcePicker
              includeInactive
              required={false}
              resource={ref.resource}
              value={values[ref.key] as number | undefined}
              onChange={(value) =>
                setValues((v) => ({ ...v, [ref.key]: value }))
              }
            />
            {values[ref.key] && (
              <button
                type="button"
                className="clear-filter"
                onClick={() =>
                  setValues((v) => ({ ...v, [ref.key]: undefined }))
                }
              >
                Remover seleção
              </button>
            )}
          </Field>
        ))}
        <Field label="Ano letivo">
          <input
            type="number"
            min={2000}
            max={2100}
            value={values.anoLetivo || ""}
            onChange={(e) =>
              setValues((v) => ({
                ...v,
                anoLetivo: e.target.value ? Number(e.target.value) : undefined,
              }))
            }
          />
        </Field>
        <Field label="Bimestre">
          <select
            value={values.bimestre || ""}
            onChange={(e) =>
              setValues((v) => ({
                ...v,
                bimestre: e.target.value ? Number(e.target.value) : undefined,
              }))
            }
          >
            <option value="">Todos</option>
            {[1, 2, 3, 4].map((n) => (
              <option key={n} value={n}>
                {n}º bimestre
              </option>
            ))}
          </select>
        </Field>
        {!responsible && (
          <Field label="Situação">
            <select
              value={values.status || ""}
              onChange={(e) =>
                setValues((v) => ({
                  ...v,
                  status: (e.target.value as Status) || undefined,
                }))
              }
            >
              <option value="">Todas</option>
              {statuses.map((s) => (
                <option key={s} value={s}>
                  {statusLabel[s]}
                </option>
              ))}
            </select>
          </Field>
        )}
      </div>
      <Button type="submit">Aplicar filtros</Button>
    </form>
  );
}
