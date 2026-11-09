import { useCallback, useState } from "react";
import { cadastroService } from "../services";
import { useDebounce, useQuery } from "../hooks/useQuery";
import type { Perfil, Recurso, Recursos } from "../types";
import { resourceLabels, perfilLabel } from "../types";
import { ErrorState } from "./ui";
export function resourceName(record: Recursos[Recurso]): string {
  if ("nome" in record) return record.nome;
  if ("usuario" in record) return record.usuario.nome;
  if ("numero" in record)
    return `${record.numero}º bimestre · ${record.anoLetivo}`;
  if ("disciplina" in record)
    return `${record.professor.usuario.nome} · ${record.disciplina.nome} · ${record.turma.nome}`;
  return `${record.aluno.nome} · ${record.turma.nome}`;
}
export function ResourcePicker({
  resource,
  value,
  onChange,
  disabled = false,
  multiple = false,
  profile,
  initialLabels = {},
  required = true,
  includeInactive = false,
}: {
  resource: Recurso;
  value: number | number[] | undefined;
  onChange: (value: number | number[]) => void;
  disabled?: boolean;
  multiple?: boolean;
  profile?: Perfil;
  initialLabels?: Record<number, string>;
  required?: boolean;
  includeInactive?: boolean;
}) {
  const [search, setSearch] = useState("");
  const debounced = useDebounce(search);
  const [labels, setLabels] = useState(initialLabels);
  const fetcher = useCallback(
    (signal: AbortSignal) =>
      cadastroService.all(resource, signal),
    [resource],
  );
  const query = useQuery(fetcher);
  const selected = Array.isArray(value) ? value : value ? [value] : [];
  const options =
    query.data?.filter(
      (item) =>
        (includeInactive ||
          ((!("ativo" in item) || item.ativo) &&
            (!("usuario" in item) || item.usuario.ativo))) &&
        (!profile || ("perfil" in item && item.perfil === profile)) &&
        (!debounced || resourceName(item).toLocaleLowerCase("pt-BR").includes(debounced.toLocaleLowerCase("pt-BR"))),
    ) || [];
  function pick(id: number) {
    const record = options.find((r) => r.id === id);
    if (record) setLabels((l) => ({ ...l, [id]: resourceName(record) }));
    onChange(multiple ? [...new Set([...selected, id])] : id);
  }
  return (
    <div className="picker">
      {multiple && selected.length > 0 && (
        <div className="chips">
          {selected.map((id) => (
            <button
              type="button"
              key={id}
              disabled={disabled}
              onClick={() => onChange(selected.filter((v) => v !== id))}
            >
              {labels[id] || initialLabels[id] || `Selecionado #${id}`} ×
            </button>
          ))}
        </div>
      )}
      {!disabled && (
        <input
          aria-label={`Buscar ${resourceLabels[resource]}`}
          placeholder="Buscar por nome…"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
          }}
        />
      )}
      <select
        aria-label={`Selecionar ${resourceLabels[resource]}`}
        required={required && !multiple}
        disabled={disabled || query.loading}
        value={multiple ? "" : selected[0] || ""}
        onChange={(e) => {
          if (e.target.value) pick(Number(e.target.value));
        }}
      >
        <option value="">
          {query.loading
            ? "Carregando opções…"
            : multiple
              ? resource === "alunos"
                ? "Adicionar aluno…"
                : "Adicionar tag…"
              : "Selecione…"}
        </option>
        {!multiple &&
          selected[0] &&
          !options.some((r) => r.id === selected[0]) && (
            <option value={selected[0]}>
              {labels[selected[0]] ||
                initialLabels[selected[0]] ||
                `Selecionado #${selected[0]}`}
            </option>
          )}
        {options.map((record) => (
          <option key={record.id} value={record.id}>
            {resourceName(record)}
          </option>
        ))}
      </select>
      {query.error && <ErrorState error={query.error} retry={query.reload} />}{" "}
      {profile && (
        <small>
          São exibidos usuários ativos com o perfil {perfilLabel[profile].toLowerCase()}.
          Cadastre o usuário antes de associá-lo.
        </small>
      )}
    </div>
  );
}
