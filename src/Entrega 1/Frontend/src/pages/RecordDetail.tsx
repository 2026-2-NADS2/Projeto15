import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  ArrowLeft,
  DownloadSimple,
  PencilSimple,
  CheckCircle,
  PaperPlaneTilt,
  Clock,
} from "@phosphor-icons/react";
import { useAuth } from "../contexts/AuthContext";
import { useQuery } from "../hooks/useQuery";
import { acompanhamentoService, type Acao } from "../services";
import type { Ciencia } from "../types";
import {
  Badge,
  Button,
  Card,
  ConfirmDialog,
  ErrorState,
  Loading,
  PageHeader,
  useToast,
} from "../components/ui";
import { errorMessage } from "../services/api";
import { date, number, isOpen } from "../utils/format";
export function RecordDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const fetcher = useCallback(
    (signal: AbortSignal) => acompanhamentoService.get(Number(id), signal),
    [id],
  );
  const query = useQuery(fetcher);
  const [action, setAction] = useState<Acao | "science">();
  const [busy, setBusy] = useState(false);
  const [pdfBusy, setPdfBusy] = useState(false);
  const [science, setScience] = useState<Ciencia>();
  const [already, setAlready] = useState(false);
  const toast = useToast();
  async function confirm() {
    if (!action) return;
    setBusy(true);
    try {
      if (action === "science") {
        setScience(await acompanhamentoService.science(Number(id)));
        toast("Ciência registrada com sucesso.");
      } else {
        await acompanhamentoService.action(Number(id), action);
        query.reload();
        toast("Acompanhamento atualizado.");
      }
      setAction(undefined);
    } catch (e) {
      const message = errorMessage(e);
      if (action === "science" && message.includes("Ciência já registrada")) {
        setAlready(true);
        setAction(undefined);
      }
      toast(message, true);
    } finally {
      setBusy(false);
    }
  }
  async function pdf() {
    setPdfBusy(true);
    try {
      await acompanhamentoService.pdf(Number(id));
    } catch (e) {
      toast(errorMessage(e), true);
    } finally {
      setPdfBusy(false);
    }
  }
  const actionLabel: Record<Acao | "science", string> = {
    enviar: "Enviar para revisão",
    "iniciar-revisao": "Iniciar revisão",
    devolver: "Devolver para ajustes",
    publicar: "Publicar acompanhamento",
    cancelar: "Cancelar acompanhamento",
    science: "Confirmar ciência",
  };
  if (query.loading) return <Loading />;
  if (query.error)
    return <ErrorState error={query.error} retry={query.reload} />;
  const a = query.data;
  if (!a || !user) return null;
  const admin = user.perfil === "ADMINISTRADOR";
  const teacher = user.perfil === "PROFESSOR";
  const editable =
    teacher &&
    ["RASCUNHO", "DEVOLVIDO_AJUSTES"].includes(a.status) &&
    isOpen(a.bimestre);
  return (
    <>
      <Link className="back-link" to="/acompanhamentos">
        <ArrowLeft />
        Voltar aos acompanhamentos
      </Link>
      <PageHeader
        title={a.aluno.nome}
        description={`${a.disciplina.nome} · ${a.turma.nome} · ${a.bimestre.numero}º bimestre de ${a.bimestre.anoLetivo}`}
      >
        <Badge status={a.status} />
        {(admin || user.perfil === "RESPONSAVEL") &&
          a.status === "PUBLICADO" && (
            <Button variant="secondary" disabled={pdfBusy} onClick={pdf}>
              <DownloadSimple />
              {pdfBusy ? "Preparando PDF…" : "Baixar PDF"}
            </Button>
          )}
      </PageHeader>
      <div className="detail-layout">
        <Card className="detail-card">
          <div className="section-heading">
            <h2>Acompanhamento da aprendizagem</h2>
            <span className="muted">#{a.id}</span>
          </div>
          <p className="record-description">{a.descricao}</p>
          <h3>Observações em destaque</h3>
          <div className="chips">
            {a.tags.length ? (
              a.tags.map((t) => <span key={t.id}>{t.nome}</span>)
            ) : (
              <span className="muted">Nenhuma tag atribuída.</span>
            )}
          </div>
          <div className="record-dates">
            <span>Criado em {date(a.dataCriacao)}</span>
            <span>Atualizado em {date(a.dataAtualizacao)}</span>
          </div>
        </Card>
        <Card className="detail-card">
          <h2>Resumo do registro</h2>
          <div className="summary-average">
            <span>Média informada</span>
            <strong>{number(a.media)}</strong>
          </div>
          <dl className="summary-list">
            <dt>Professor</dt>
            <dd>{a.professor.nome}</dd>
            <dt>Turma</dt>
            <dd>
              {a.turma.nome} · {a.turma.serie}
            </dd>
            <dt>Período</dt>
            <dd>
              {a.bimestre.numero}º bimestre · {a.bimestre.anoLetivo}
            </dd>
          </dl>
          <small>A média foi informada no acompanhamento pelo professor.</small>
        </Card>
      </div>
      {(admin || teacher) && (
        <Card className="detail-card mt">
          <h2>Próximos passos</h2>
          <div className="actions wrap">
            {editable && (
              <>
                <Link
                  className="button secondary"
                  to={`/acompanhamentos/${id}/editar`}
                >
                  <PencilSimple />
                  Editar acompanhamento
                </Link>
                <Button disabled={busy} onClick={() => setAction("enviar")}>
                  <PaperPlaneTilt />
                  Enviar para revisão
                </Button>
              </>
            )}
            {teacher &&
              !isOpen(a.bimestre) &&
              ["RASCUNHO", "DEVOLVIDO_AJUSTES"].includes(a.status) && (
                <p className="notice">
                  O período de lançamento deste bimestre está encerrado.
                </p>
              )}
            {admin && a.status === "ENVIADO_REVISAO" && (
              <Button onClick={() => setAction("iniciar-revisao")}>
                Iniciar revisão
              </Button>
            )}
            {admin && a.status === "EM_REVISAO" && (
              <>
                <Link
                  className="button secondary"
                  to={`/acompanhamentos/${id}/revisao`}
                >
                  <PencilSimple />
                  Editar revisão
                </Link>
                <Button
                  variant="secondary"
                  onClick={() => setAction("devolver")}
                >
                  Devolver para ajustes
                </Button>
                <Button onClick={() => setAction("publicar")}>
                  <CheckCircle />
                  Publicar
                </Button>
              </>
            )}
            {admin && !["PUBLICADO", "CANCELADO"].includes(a.status) && (
              <Button variant="danger" onClick={() => setAction("cancelar")}>
                Cancelar acompanhamento
              </Button>
            )}
            {["PUBLICADO", "CANCELADO"].includes(a.status) && (
              <p className="muted">
                Este registro foi concluído e não permite alterações.
              </p>
            )}
            {teacher &&
              ["ENVIADO_REVISAO", "EM_REVISAO"].includes(a.status) && (
                <p className="notice">Aguardando a revisão da escola.</p>
              )}
          </div>
        </Card>
      )}
      {user.perfil === "RESPONSAVEL" && (
        <Card className="detail-card mt">
          <h2>Ciência do responsável</h2>
          {science ? (
            <p className="notice success">
              <CheckCircle />
              Ciência registrada em {date(science.dataHoraCiencia)}.
            </p>
          ) : already ? (
            <p className="notice success">
              <CheckCircle />A escola já recebeu sua ciência deste
              acompanhamento.
            </p>
          ) : (
            <>
              <p>
                Ao confirmar, você informa à escola que leu este acompanhamento.
              </p>
              <p className="muted">
                Se já confirmou anteriormente, a escola informará que sua
                ciência está registrada.
              </p>
              <Button disabled={busy} onClick={() => setAction("science")}>
                <CheckCircle />
                Confirmar ciência
              </Button>
            </>
          )}
        </Card>
      )}
      {(admin || teacher) && <History id={a.id} />}{" "}
      {action && (
        <ConfirmDialog
          title={`${actionLabel[action]}?`}
          description={
            action === "publicar"
              ? "O acompanhamento ficará disponível aos responsáveis e não poderá mais ser alterado."
              : action === "science"
                ? "Confirme que leu as informações deste acompanhamento."
                : action === "cancelar"
                  ? "O cancelamento encerra este registro e não pode ser desfeito."
                  : "Confirme para atualizar a situação deste acompanhamento."
          }
          confirm={confirm}
          close={() => setAction(undefined)}
          busy={busy}
        />
      )}
    </>
  );
}
function History({ id }: { id: number }) {
  const fetcher = useCallback(
    (s: AbortSignal) => acompanhamentoService.history(id, s),
    [id],
  );
  const query = useQuery(fetcher);
  return (
    <Card className="detail-card mt">
      <h2>Histórico do acompanhamento</h2>
      {query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : (
        <ol className="timeline">
          {query.data?.map((h) => (
            <li key={h.id}>
              <span className="timeline-icon">
                <Clock size={18} />
              </span>
              <div>
                <Badge status={h.statusNovo} />
                <p>
                  {h.acao === "CRIACAO"
                    ? "Acompanhamento criado"
                    : h.acao === "EDICAO_PROFESSOR"
                      ? "Informações ajustadas pelo professor"
                      : h.acao === "EDICAO_ADMINISTRADOR"
                        ? "Informações ajustadas na revisão"
                        : "Situação atualizada"}{" "}
                  · {h.usuarioNome}
                </p>
                <small>{date(h.dataHora)}</small>
                {(h.dadosAnteriores || h.dadosNovos) && (
                  <details>
                    <summary>Ver alterações registradas</summary>
                    {h.dadosAnteriores && (
                      <>
                        <strong>Antes</strong>
                        <pre>{h.dadosAnteriores}</pre>
                      </>
                    )}
                    {h.dadosNovos && (
                      <>
                        <strong>Depois</strong>
                        <pre>{h.dadosNovos}</pre>
                      </>
                    )}
                  </details>
                )}
              </div>
            </li>
          ))}
        </ol>
      )}
    </Card>
  );
}
