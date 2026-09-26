import { useCallback, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, FloppyDisk, PaperPlaneTilt } from "@phosphor-icons/react";
import { resourceLabels } from "../types";
import { useAuth } from "../contexts/AuthContext";
import { useQuery } from "../hooks/useQuery";
import { cadastroService, acompanhamentoService } from "../services";
import type {
  Acompanhamento,
  Bimestre,
  Edicao,
  Matricula,
  Recurso,
  Recursos,
  Vinculo,
} from "../types";
import {
  Badge,
  Button,
  Card,
  ConfirmDialog,
  ErrorState,
  Field,
  Loading,
  PageHeader,
  useToast,
} from "../components/ui";
import { ResourcePicker, resourceName } from "../components/ResourcePicker";
import { errorMessage } from "../services/api";
import { date, isOpen, mediaMin, mediaMax } from "../utils/format";
export function RecordForm({ review = false }: { review?: boolean }) {
  const { id } = useParams();
  const fetcher = useCallback(
    (signal: AbortSignal) =>
      id
        ? acompanhamentoService.get(Number(id), signal)
        : Promise.resolve(undefined),
    [id],
  );
  const query = useQuery(fetcher);
  return (
    <>
      <Link
        className="back-link"
        to={id ? `/acompanhamentos/${id}` : "/acompanhamentos"}
      >
        <ArrowLeft />
        Voltar
      </Link>
      <PageHeader
        title={
          review
            ? "Editar durante a revisão"
            : id
              ? "Editar acompanhamento"
              : "Novo acompanhamento"
        }
        description="Registre a evolução, as conquistas e os próximos passos do aluno."
      />
      {query.loading ? (
        <Loading />
      ) : query.error ? (
        <ErrorState error={query.error} retry={query.reload} />
      ) : (
        <RecordFormContent
          key={id || "new"}
          record={query.data}
          review={review}
        />
      )}
    </>
  );
}
function AcademicPicker<K extends Recurso>({
  resource,
  filter,
  onSelect,
  selected,
  disabled,
}: {
  resource: K;
  filter: (r: Recursos[K]) => boolean;
  onSelect: (r: Recursos[K]) => void;
  selected?: Recursos[K];
  disabled: boolean;
}) {
  const fetcher = useCallback(
    (s: AbortSignal) => cadastroService.all(resource, s),
    [resource],
  );
  const q = useQuery(fetcher);
  const options = q.data?.filter(filter) || [];
  return (
    <div className="picker">
      <select
        required
        disabled={disabled || q.loading}
        aria-label={`Selecionar ${resourceLabels[resource]}`}
        value={selected?.id || ""}
        onChange={(e) => {
          const r = options.find((r) => r.id === Number(e.target.value));
          if (r) onSelect(r);
        }}
      >
        <option value="">{q.loading ? "Carregando…" : "Selecione…"}</option>
        {selected && !options.some((r) => r.id === selected.id) && (
          <option value={selected.id}>{resourceName(selected)}</option>
        )}
        {options.map((r) => (
          <option key={r.id} value={r.id}>
            {resourceName(r)}
          </option>
        ))}
      </select>
      {q.error && <ErrorState error={q.error} retry={q.reload} />}{" "}
      {!q.loading && !options.length && (
        <small>Sem opções compatíveis. Peça à escola que confira as associações.</small>
      )}
    </div>
  );
}
function RecordFormContent({
  record,
  review,
}: {
  record?: Acompanhamento;
  review: boolean;
}) {
  const { user } = useAuth();
  const [vinculo, setVinculo] = useState<Vinculo>();
  const [turmaId, setTurmaId] = useState<number>();
  const [matricula, setMatricula] = useState<Matricula>();
  const [bimestre, setBimestre] = useState<Bimestre>();
  const [descricao, setDescricao] = useState(record?.descricao || "");
  const [media, setMedia] = useState(record ? String(record.media) : "");
  const [tags, setTags] = useState<number[]>(
    record?.tags.map((t) => t.id) || [],
  );
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [send, setSend] = useState(false);
  const toast = useToast();
  const navigate = useNavigate();
  const links = useQuery(useCallback((s:AbortSignal)=>cadastroService.all("vinculos",s),[]));
  const activeLinks=(links.data||[]).filter(v=>v.ativo&&v.turma.ativo&&v.disciplina.ativo&&v.disciplina.areaDisciplina.ativo);
  const classes=[...new Map(activeLinks.map(v=>[v.turma.id,v.turma])).values()];
  const classLinks=activeLinks.filter(v=>v.turma.id===turmaId);
  const canEdit = !record
    ? user?.perfil === "PROFESSOR"
    : review
      ? record.status === "EM_REVISAO" && user?.perfil === "ADMINISTRADOR"
      : ["RASCUNHO", "DEVOLVIDO_AJUSTES"].includes(record.status) &&
        isOpen(record.bimestre) &&
        user?.perfil === "PROFESSOR";
  async function save(shouldSend = false) {
    setError("");
    if (
      !descricao.trim() ||
      !media ||
      !Number.isFinite(Number(media)) ||
      Number(media) < mediaMin ||
      Number(media) > mediaMax ||
      !/^\d+(\.\d{1,2})?$/.test(media)
    ) {
      setError(
        `Informe uma descrição e uma média entre ${mediaMin} e ${mediaMax}, com até duas casas decimais.`,
      );
      return;
    }
    if (!record && (!vinculo || !matricula || !bimestre)) {
      setError("Selecione o vínculo, o aluno e o bimestre.");
      return;
    }
    setBusy(true);
    try {
      const payload: Edicao = {
        descricao,
        media: Number(media),
        tagsIds: tags,
      };
      const a = record
        ? await acompanhamentoService.edit(record.id, payload, review)
        : await acompanhamentoService.create({
            ...payload,
            alunoId: matricula!.aluno.id,
            turmaId: vinculo!.turma.id,
            disciplinaId: vinculo!.disciplina.id,
            bimestreId: bimestre!.id,
          });
      if (shouldSend) {
        try {
          await acompanhamentoService.action(a.id, "enviar");
        } catch (e) {
          toast(`Rascunho salvo. ${errorMessage(e)}`, true);
          navigate(`/acompanhamentos/${a.id}`);
          return;
        }
      }
      toast(
        shouldSend
          ? "Acompanhamento enviado para revisão."
          : "Acompanhamento salvo.",
      );
      navigate(`/acompanhamentos/${a.id}`);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
      setSend(false);
    }
  }
  function submit(e: FormEvent) {
    e.preventDefault();
    void save();
  }
  if (!canEdit)
    return (
      <ErrorState error="Este acompanhamento não está disponível para edição nesta situação ou neste período." />
    );
  return (
    <Card className="form-card">
      <form onSubmit={submit}>
        {record ? (
          <div className="notice">
            <div>
              <strong>
                {record.aluno.nome} · {record.disciplina.nome}
              </strong>
              <p>
                {record.turma.nome} · {record.bimestre.numero}º bimestre de{" "}
                {record.bimestre.anoLetivo}
              </p>
            </div>
            <Badge status={record.status} />
          </div>
        ) : (
          <div className="form-grid">
            <Field label="Turma"><select required disabled={busy||links.loading} value={turmaId||""} onChange={e=>{setTurmaId(Number(e.target.value));setVinculo(undefined);setMatricula(undefined);setBimestre(undefined)}}><option value="">Selecione…</option>{classes.map(t=><option key={t.id} value={t.id}>{t.nome} · {t.serie} · {t.anoLetivo}</option>)}</select></Field>
            <Field label="Disciplina"><select required disabled={busy||!turmaId} value={vinculo?.id||""} onChange={e=>{const v=classLinks.find(x=>x.id===Number(e.target.value));setVinculo(v);setMatricula(undefined);setBimestre(undefined)}}><option value="">Selecione…</option>{classLinks.map(v=><option key={v.id} value={v.id}>{v.disciplina.nome}</option>)}</select></Field>
            <Field label="Aluno matriculado na turma">
              {vinculo ? (
                <AcademicPicker
                  key={vinculo.id}
                  resource="matriculas"
                  disabled={busy}
                  selected={matricula}
                  filter={(m) =>
                    m.ativo && m.aluno.ativo && m.turma.id === vinculo.turma.id
                  }
                  onSelect={setMatricula}
                />
              ) : (
                <select disabled aria-label="Aluno">
                  <option>Selecione turma e disciplina primeiro</option>
                </select>
              )}
            </Field>
            <Field label="Bimestre aberto para lançamento">
              {vinculo ? (
                <AcademicPicker
                  key={vinculo.turma.anoLetivo}
                  resource="bimestres"
                  disabled={busy}
                  selected={bimestre}
                  filter={(b) =>
                    b.anoLetivo === vinculo.turma.anoLetivo && isOpen(b)
                  }
                  onSelect={setBimestre}
                />
              ) : (
                <select disabled aria-label="Bimestre">
                  <option>Selecione o vínculo primeiro</option>
                </select>
              )}
            </Field>
            {bimestre && (
              <p className="form-hint">
                Lançamentos de {date(bimestre.dataHoraAbertura)} até{" "}
                {date(bimestre.dataHoraEncerramento)}.
              </p>
            )}
          </div>
        )}
        <div className="form-grid mt">
          <Field
            label="Média informada"
            hint={`Escala configurada: ${mediaMin} a ${mediaMax}. Use até duas casas decimais.`}
          >
            <input
              type="number"
              required
              min={mediaMin}
              max={mediaMax}
              step="0.01"
              value={media}
              onChange={(e) => setMedia(e.target.value)}
              disabled={busy}
            />
          </Field>
          <Field label="Tags do acompanhamento">
            <ResourcePicker
              resource="tags"
              multiple
              value={tags}
              onChange={(v) => setTags(v as number[])}
              disabled={busy}
              initialLabels={Object.fromEntries(
                record?.tags.map((t) => [t.id, t.nome]) || [],
              )}
            />
          </Field>
        </div>
        <Field
          label="Descrição do acompanhamento"
          hint={`${descricao.length}/10000 caracteres`}
        >
          <textarea
            className="long-textarea"
            required
            maxLength={10000}
            placeholder="Descreva a evolução e os pontos que merecem acompanhamento…"
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            disabled={busy}
          />
        </Field>
        {error && (
          <p role="alert" className="inline-error">
            {error}
          </p>
        )}
        <div className="form-actions">
          <Button variant="secondary" type="submit" disabled={busy}>
            <FloppyDisk />
            {busy ? "Salvando…" : review ? "Salvar revisão" : "Salvar rascunho"}
          </Button>
          {!review && (
            <Button
              type="button"
              disabled={busy}
              onClick={(e) => {
                if (e.currentTarget.form?.reportValidity()) setSend(true);
              }}
            >
              <PaperPlaneTilt />
              Salvar e enviar para revisão
            </Button>
          )}
        </div>
      </form>
      {send && (
        <ConfirmDialog
          title="Enviar para revisão?"
          description="As informações serão salvas e enviadas à escola. Durante a revisão, o professor não poderá editar o registro."
          confirm={() => void save(true)}
          close={() => setSend(false)}
          busy={busy}
        />
      )}
    </Card>
  );
}
