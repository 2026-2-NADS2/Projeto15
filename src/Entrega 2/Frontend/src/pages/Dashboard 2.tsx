import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import {
  ArrowRight,
  BookOpen,
  CalendarBlank,
  ChalkboardTeacher,
  CheckCircle,
  ClipboardText,
  GraduationCap,
  NotePencil,
  Plus,
  Student,
  Users,
  WarningCircle,
} from "@phosphor-icons/react";
import { useAuth } from "../contexts/AuthContext";
import { useQuery } from "../hooks/useQuery";
import { cadastroService, relatorioService } from "../services";
import type { Status } from "../types";
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
} from "../components/ui";
import { ResourcePicker } from "../components/ResourcePicker";
import { date, number, isOpen } from "../utils/format";
export function Dashboard() {
  const { user } = useAuth();
  const fetcher = useCallback(
    (signal: AbortSignal) => relatorioService.list({}, 0, signal, 100),
    [],
  );
  const q = useQuery(fetcher);
  const records = q.data?.content ?? [];
  const published = records.filter((item) => item.status === "PUBLICADO").length;
  const average = records.length
    ? records.reduce((sum, item) => sum + item.media, 0) / records.length
    : undefined;
  const bySubject = Object.values(
    records.reduce<Record<string, { name: string; total: number; count: number }>>(
      (result, item) => {
        const current = result[item.disciplina.id] ?? {
          name: item.disciplina.nome,
          total: 0,
          count: 0,
        };
        current.total += item.media;
        current.count += 1;
        result[item.disciplina.id] = current;
        return result;
      },
      {},
    ),
  ).slice(0, 5);
  const admin = user?.perfil === "ADMINISTRADOR";
  const teacher = user?.perfil === "PROFESSOR";
  return (
    <>
      <section className="reference-heading">
        <div>
          <p className="eyebrow">ACOMPANHAMENTO ESCOLAR</p>
          <h1>Olá, bem-vindo!</h1>
          <p>Acompanhe o desenvolvimento dos alunos, um bimestre de cada vez.</p>
        </div>
        <span>Disciplina hoje, um futuro melhor amanhã.</span>
      </section>
      {q.error && <ErrorState error={q.error} retry={q.reload} />}
      <section className="reference-metrics" aria-label="Indicadores">
        <article className="reference-metric">
          <strong>{q.loading ? "—" : number(q.data?.totalElements ?? 0)}</strong>
          <span>Acompanhamentos</span>
        </article>
        <article className="reference-metric">
          <strong>{q.loading ? "—" : number(published)}</strong>
          <span>Publicados</span>
        </article>
        <article className="reference-metric">
          <strong>{average === undefined ? "—" : number(average)}</strong>
          <span>Média geral</span>
        </article>
      </section>
      <section className="reference-grid">
        <article className="card reference-panel">
          <h2>Médias por disciplina</h2>
          {q.loading ? <Loading /> : bySubject.length ? bySubject.map((subject) => {
            const value = subject.total / subject.count;
            return (
              <div className="bar-row" key={subject.name}>
                <div className="bar-label"><span>{subject.name}</span><strong>{number(value)}</strong></div>
                <div className="bar-track"><span className="bar-fill" style={{ width: `${Math.max(0, Math.min(100, value * 10))}%` }} /></div>
              </div>
            );
          }) : <p className="muted">Nenhuma média disponível.</p>}
        </article>
        <article className="card reference-panel">
          <h2>Seu próximo passo</h2>
          <p>{admin ? "Revise os registros enviados pelos professores antes da publicação às famílias." : teacher ? "Registre o desenvolvimento dos seus alunos e envie para revisão." : "Consulte os relatórios publicados, registre ciência e acompanhe a evolução do aluno."}</p>
          <Link className="button primary" to={admin ? "/acompanhamentos?status=ENVIADO_REVISAO" : teacher ? "/acompanhamentos/novo" : "/acompanhamentos"}>
            {admin ? "Abrir fila de revisão" : teacher ? "Criar acompanhamento" : "Consultar relatórios"}
          </Link>
        </article>
      </section>
    </>
  );
}
function Welcome({ kind }: { kind: "admin" | "teacher" | "family" }) {
  const { user } = useAuth();
  return (
    <div className="welcome-banner">
      <div>
        <span className="eyebrow">
          {kind === "family"
            ? "ESCOLA E FAMÍLIA, JUNTAS"
            : "CUIDADO EM CADA ETAPA"}
        </span>
        <h2>
          Olá, {user?.nome.split(" ")[0]}. <br />
          {kind === "admin"
            ? "Vamos acompanhar nossa escola?"
            : kind === "teacher"
              ? "Seu olhar faz a diferença."
              : "Vamos acompanhar a aprendizagem?"}
        </h2>
        <p>
          {kind === "admin"
            ? "Uma visão organizada para cuidar de cada aluno e apoiar quem ensina."
            : kind === "teacher"
              ? "Registre conquistas e ajude a construir os próximos passos dos seus alunos."
              : "Veja as médias e as observações que a escola compartilhou com você."}
        </p>
        <Link
          to={
            kind === "admin"
              ? "/acompanhamentos?status=ENVIADO_REVISAO"
              : kind === "teacher"
                ? "/acompanhamentos/novo"
                : "/acompanhamentos"
          }
          className="banner-link"
        >
          {kind === "admin"
            ? "Ir para as revisões"
            : kind === "teacher"
              ? "Criar acompanhamento"
              : "Ver acompanhamentos"}
          <ArrowRight size={19} />
        </Link>
      </div>
      <div className="banner-art" aria-hidden="true">
        <div className="banner-orbit" />
        <div className="banner-book">
          <BookOpen size={70} weight="duotone" />
        </div>
        <GraduationCap className="floating-cap" size={45} />
        <span className="floating-check">
          <CheckCircle weight="fill" size={27} />
        </span>
      </div>
    </div>
  );
}
function StatCard({
  label,
  value,
  icon: Icon,
  link,
  kind = "",
}: {
  label: string;
  value: number | undefined;
  icon: typeof Users;
  link: string;
  kind?: string;
}) {
  return (
    <Link className={`stat-card ${kind}`} to={link}>
      <div className="stat-top">
        <span>{label}</span>
        <span className="stat-icon">
          <Icon size={22} />
        </span>
      </div>
      <strong>{value === undefined ? "—" : number(value)}</strong>
      <small>
        Ver detalhes
        <ArrowRight size={14} />
      </small>
    </Link>
  );
}
function today() {
  return new Intl.DateTimeFormat("pt-BR", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(new Date());
}
async function countStatus(status: Status, signal: AbortSignal) {
  return (await relatorioService.list({ status }, 0, signal, 1)).totalElements;
}
export function DashboardAdministrador() {
  const fetcher = useCallback(async (s: AbortSignal) => {
    const [
      alunos,
      professores,
      responsaveis,
      turmas,
      disciplinas,
      waiting,
      review,
      published,
      returned,
    ] = await Promise.all([
      cadastroService.list("alunos", 0, undefined, s, 1),
      cadastroService.list("professores", 0, undefined, s, 1),
      cadastroService.list("responsaveis", 0, undefined, s, 1),
      cadastroService.list("turmas", 0, undefined, s, 1),
      cadastroService.list("disciplinas", 0, undefined, s, 1),
      countStatus("ENVIADO_REVISAO", s),
      countStatus("EM_REVISAO", s),
      countStatus("PUBLICADO", s),
      countStatus("DEVOLVIDO_AJUSTES", s),
    ]);
    return {
      alunos: alunos.totalElements,
      professores: professores.totalElements,
      responsaveis: responsaveis.totalElements,
      turmas: turmas.totalElements,
      disciplinas: disciplinas.totalElements,
      waiting,
      review,
      published,
      returned,
    };
  }, []);
  const q = useQuery(fetcher);
  return (
    <>
      <PageHeader
        title="Painel da escola"
        description="Tudo o que importa para acompanhar a vida escolar."
      >
        <span className="today">
          <CalendarBlank size={18} />
          {today()}
        </span>
      </PageHeader>
      <Welcome kind="admin" />
      {q.error && <ErrorState error={q.error} retry={q.reload} />}
      <div className="stats-grid">
        <StatCard
          label="Alunos cadastrados"
          value={q.data?.alunos}
          icon={Student}
          link="/alunos"
        />
        <StatCard
          label="Professores"
          value={q.data?.professores}
          icon={ChalkboardTeacher}
          link="/professores"
        />
        <StatCard
          label="Turmas"
          value={q.data?.turmas}
          icon={GraduationCap}
          link="/turmas"
        />
        <StatCard
          label="Aguardando revisão"
          value={q.data?.waiting}
          icon={ClipboardText}
          link="/acompanhamentos?status=ENVIADO_REVISAO"
          kind="attention"
        />
      </div>
      <div className="dashboard-columns">
        <Card>
          <div className="table-toolbar">
            <div>
              <h2>Aguardando seu olhar</h2>
              <p>Revise os registros antes de compartilhar com as famílias.</p>
            </div>
            <Link
              className="text-link"
              to="/acompanhamentos?status=ENVIADO_REVISAO"
            >
              Ver todos
              <ArrowRight />
            </Link>
          </div>
          <DashboardRecords status="ENVIADO_REVISAO" />
        </Card>
        <div className="dashboard-aside">
          <Periods />
          <Card className="detail-card">
            <h2>Uma escola em movimento</h2>
            <div className="status-summary">
              <Link to="/acompanhamentos?status=PUBLICADO">
                <span>
                  <CheckCircle />
                  Publicados
                </span>
                <strong>{q.data?.published ?? "—"}</strong>
              </Link>
              <Link to="/acompanhamentos?status=EM_REVISAO">
                <span>
                  <NotePencil />
                  Em revisão
                </span>
                <strong>{q.data?.review ?? "—"}</strong>
              </Link>
              <Link to="/acompanhamentos?status=DEVOLVIDO_AJUSTES">
                <span>
                  <WarningCircle />
                  Devolvidos para ajuste
                </span>
                <strong>{q.data?.returned ?? "—"}</strong>
              </Link>
              <Link to="/responsaveis">
                <span>
                  <Users />
                  Responsáveis
                </span>
                <strong>{q.data?.responsaveis ?? "—"}</strong>
              </Link>
              <Link to="/disciplinas">
                <span>
                  <BookOpen />
                  Disciplinas
                </span>
                <strong>{q.data?.disciplinas ?? "—"}</strong>
              </Link>
            </div>
            <small>Totais de cadastros e registros em todos os períodos.</small>
          </Card>
        </div>
      </div>
      <QuickLinks admin />
    </>
  );
}
export function DashboardProfessor() {
  const fetcher = useCallback(async (s: AbortSignal) => {
    const [turmas, alunos, drafts, returned, waiting] = await Promise.all([
      cadastroService.list("turmas", 0, undefined, s, 1),
      cadastroService.list("alunos", 0, undefined, s, 1),
      countStatus("RASCUNHO", s),
      countStatus("DEVOLVIDO_AJUSTES", s),
      countStatus("ENVIADO_REVISAO", s),
    ]);
    return {
      turmas: turmas.totalElements,
      alunos: alunos.totalElements,
      drafts,
      returned,
      waiting,
    };
  }, []);
  const q = useQuery(fetcher);
  return (
    <>
      <PageHeader
        title="Meu painel"
        description="Seus alunos, seus registros e os próximos passos."
      >
        <Link className="button primary" to="/acompanhamentos/novo">
          <Plus />
          Novo acompanhamento
        </Link>
      </PageHeader>
      <Welcome kind="teacher" />
      {q.error && <ErrorState error={q.error} retry={q.reload} />}
      <div className="stats-grid">
        <StatCard
          label="Minhas turmas"
          value={q.data?.turmas}
          icon={GraduationCap}
          link="/turmas"
        />
        <StatCard
          label="Meus alunos"
          value={q.data?.alunos}
          icon={Student}
          link="/alunos"
        />
        <StatCard
          label="Rascunhos"
          value={q.data?.drafts}
          icon={NotePencil}
          link="/acompanhamentos?status=RASCUNHO"
        />
        <StatCard
          label="Devolvidos para ajustes"
          value={q.data?.returned}
          icon={WarningCircle}
          link="/acompanhamentos?status=DEVOLVIDO_AJUSTES"
          kind="attention"
        />
      </div>
      <div className="dashboard-columns">
        <Card>
          <div className="table-toolbar">
            <h2>Registros para continuar</h2>
            <Link className="text-link" to="/acompanhamentos?status=RASCUNHO">
              Ver rascunhos
              <ArrowRight />
            </Link>
          </div>
          <DashboardRecords status="RASCUNHO" />
        </Card>
        <div className="dashboard-aside">
          <Periods />
          <Card className="detail-card">
            <h2>Revisão da escola</h2>
            <p>
              {q.data?.waiting ?? "—"} registros enviados aguardam o início da
              revisão.
            </p>
            <Link
              className="text-link"
              to="/acompanhamentos?status=ENVIADO_REVISAO"
            >
              Acompanhar enviados
              <ArrowRight />
            </Link>
          </Card>
        </div>
      </div>
      <QuickLinks />
    </>
  );
}
function DashboardRecords({ status }: { status?: Status }) {
  const [page, setPage] = useState(0);
  const fetcher = useCallback(
    (s: AbortSignal) => relatorioService.list({ status }, page, s, 5),
    [status, page],
  );
  const q = useQuery(fetcher);
  return q.loading ? (
    <Loading />
  ) : q.error ? (
    <ErrorState error={q.error} retry={q.reload} />
  ) : !q.data?.content.length ? (
    <EmptyState
      title="Nenhum registro nesta situação"
      description="Nenhum acompanhamento nesta situação no momento."
    />
  ) : (
    <>
      <div className="record-list">
        {q.data.content.map((a) => (
          <Link
            key={a.id}
            className="record-row"
            to={`/acompanhamentos/${a.id}`}
          >
            <span className="student-avatar">
              {a.aluno.nome
                .split(" ")
                .slice(0, 2)
                .map((n) => n[0])
                .join("")}
            </span>
            <div>
              <strong>{a.aluno.nome}</strong>
              <p>
                {a.disciplina.nome} · {a.turma.nome}
              </p>
              <small>
                {a.bimestre.numero}º bimestre · {a.bimestre.anoLetivo}
              </small>
            </div>
            <Badge status={a.status} />
            <ArrowRight size={18} />
          </Link>
        ))}
      </div>
      <Pagination page={q.data} onChange={setPage} />
    </>
  );
}
function Periods() {
  const [page, setPage] = useState(0);
  const fetcher = useCallback(
    (s: AbortSignal) =>
      cadastroService.list("bimestres", page, undefined, s, 3),
    [page],
  );
  const q = useQuery(fetcher);
  return (
    <Card className="detail-card">
      <div className="section-heading">
        <h2>
          <CalendarBlank size={21} />
          Calendário escolar
        </h2>
      </div>
      {q.loading ? (
        <Loading />
      ) : q.error ? (
        <ErrorState error={q.error} retry={q.reload} />
      ) : !q.data?.content.length ? (
        <p className="muted">
          A escola ainda não cadastrou períodos de lançamento.
        </p>
      ) : (
        <>
          <div className="periods">
            {q.data.content.map((b) => (
              <div key={b.id}>
                <div className="section-heading">
                  <strong>
                    {b.numero}º bimestre · {b.anoLetivo}
                  </strong>
                  <span
                    className={`simple-badge ${isOpen(b) ? "success" : ""}`}
                  >
                    {isOpen(b) ? "Aberto" : "Fechado"}
                  </span>
                </div>
                <small>
                  {date(b.dataHoraAbertura)}
                  <br />
                  até {date(b.dataHoraEncerramento)}
                </small>
              </div>
            ))}
          </div>
          {q.data.totalPages > 1 && (
            <div className="picker-pagination">
              <Button
                variant="ghost"
                disabled={q.data.first}
                onClick={() => setPage((p) => p - 1)}
              >
                Anterior
              </Button>
              <small>
                {page + 1}/{q.data.totalPages}
              </small>
              <Button
                variant="ghost"
                disabled={q.data.last}
                onClick={() => setPage((p) => p + 1)}
              >
                Próxima
              </Button>
            </div>
          )}
        </>
      )}
    </Card>
  );
}
function QuickLinks({ admin = false }: { admin?: boolean }) {
  return (
    <>
      <h2 className="quick-title">Acesso rápido</h2>
      <div className="quick-links">
        {(admin
          ? [
              {
                title: "Cadastrar aluno",
                text: "Organize a comunidade escolar",
                url: "/alunos/novo",
                icon: Student,
              },
              {
                title: "Organizar turmas",
                text: "Estrutura para aprender",
                url: "/turmas",
                icon: GraduationCap,
              },
              {
                title: "Consultar relatórios",
                text: "Informações para acompanhar",
                url: "/relatorios",
                icon: ClipboardText,
              },
            ]
          : [
              {
                title: "Minhas turmas",
                text: "Conheça suas associações",
                url: "/turmas",
                icon: GraduationCap,
              },
              {
                title: "Registros devolvidos",
                text: "Continue os ajustes solicitados",
                url: "/acompanhamentos?status=DEVOLVIDO_AJUSTES",
                icon: NotePencil,
              },
              {
                title: "Consultar relatórios",
                text: "Acompanhe seus registros",
                url: "/relatorios",
                icon: ClipboardText,
              },
            ]
        ).map((item) => (
          <Link key={item.url} className="quick-card" to={item.url}>
            <span className="quick-icon">
              <item.icon size={24} />
            </span>
            <div>
              <strong>{item.title}</strong>
              <p>{item.text}</p>
            </div>
            <ArrowRight size={20} />
          </Link>
        ))}
      </div>
    </>
  );
}
export function DashboardResponsavel() {
  const [alunoId, setAlunoId] = useState<number>();
  return (
    <>
      <PageHeader
        title="A vida escolar, mais perto"
        description="Um espaço para acompanhar e participar da aprendizagem."
      />
      <Welcome kind="family" />
      <Card className="family-selector">
        <div>
          <h2>
            <Users size={22} />
            Meus filhos
          </h2>
          <p>Selecione o aluno que deseja acompanhar.</p>
        </div>
        <ResourcePicker
          includeInactive
          resource="alunos"
          value={alunoId}
          onChange={(v) => setAlunoId(v as number)}
        />
      </Card>
      {alunoId ? (
        <FamilyPerformance key={alunoId} alunoId={alunoId} />
      ) : (
        <Card className="mt">
          <EmptyState
            title="Vamos começar pelo aluno"
            description="Selecione um dos seus filhos acima para ver as informações publicadas pela escola."
          />
        </Card>
      )}
    </>
  );
}
function FamilyPerformance({ alunoId }: { alunoId: number }) {
  const [year, setYear] = useState("");
  const [period, setPeriod] = useState("");
  const [page, setPage] = useState(0);
  const fetcher = useCallback(
    (s: AbortSignal) =>
      relatorioService.list(
        {
          alunoId,
          anoLetivo: year ? Number(year) : undefined,
          bimestre: period ? Number(period) : undefined,
        },
        page,
        s,
      ),
    [alunoId, year, period, page],
  );
  const q = useQuery(fetcher);
  return (
    <>
      <div className="family-period">
        <Field label="Ano letivo">
          <input
            aria-label="Ano letivo"
            type="number"
            min={2000}
            max={2100}
            placeholder="Todos os anos"
            value={year}
            onChange={(e) => {
              setYear(e.target.value);
              setPage(0);
            }}
          />
        </Field>
        <Field label="Bimestre">
          <select
            value={period}
            onChange={(e) => {
              setPeriod(e.target.value);
              setPage(0);
            }}
          >
            <option value="">Todos os bimestres</option>
            {[1, 2, 3, 4].map((n) => (
              <option key={n} value={n}>
                {n}º bimestre
              </option>
            ))}
          </select>
        </Field>
        <Button
          variant="secondary"
          onClick={() => {
            setYear("");
            setPeriod("");
            setPage(0);
          }}
        >
          Limpar período
        </Button>
      </div>
      <Card>
        <div className="table-toolbar">
          <div>
            <h2>Médias e acompanhamentos</h2>
            <p>Informações publicadas para o aluno selecionado.</p>
          </div>
          <span className="count">
            {q.data?.totalElements ?? "—"} publicados
          </span>
        </div>
        {q.loading ? (
          <Loading />
        ) : q.error ? (
          <ErrorState error={q.error} retry={q.reload} />
        ) : !q.data?.content.length ? (
          <EmptyState
            title="Ainda não há registros neste período"
            description="Quando a escola publicar um acompanhamento, você poderá vê-lo aqui."
          />
        ) : (
          <>
            <div className="family-records">
              {q.data.content.map((a) => (
                <Link
                  className="family-record"
                  to={`/acompanhamentos/${a.id}`}
                  key={a.id}
                >
                  <div className="record-top">
                    <span className="subject-icon">
                      <BookOpen size={24} />
                    </span>
                    <Badge status={a.status} />
                  </div>
                  <h3>{a.disciplina.nome}</h3>
                  <p>{a.professor.nome}</p>
                  <p>
                    {a.turma.nome} · {a.bimestre.numero}º bimestre ·{" "}
                    {a.bimestre.anoLetivo}
                  </p>
                  <div className="family-average">
                    <span>Média informada</span>
                    <strong>{number(a.media)}</strong>
                  </div>
                  <p className="description-clamp">{a.descricao}</p>
                  <span className="text-link">
                    Ver detalhes e confirmar leitura
                    <ArrowRight />
                  </span>
                </Link>
              ))}
            </div>
            <Pagination page={q.data} onChange={setPage} />
          </>
        )}
      </Card>
      <div className="family-info">
        <ShieldInfo />
      </div>
    </>
  );
}
function ShieldInfo() {
  return (
    <>
      <BookOpen size={25} />
      <p>
        Acompanhar é participar. Em caso de dúvida sobre uma média ou
        observação, converse com a escola.
      </p>
    </>
  );
}
