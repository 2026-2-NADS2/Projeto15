export const perfis = ["ADMINISTRADOR", "PROFESSOR", "RESPONSAVEL"] as const;
export type Perfil = (typeof perfis)[number];
export const statuses = [
  "RASCUNHO",
  "ENVIADO_REVISAO",
  "EM_REVISAO",
  "DEVOLVIDO_AJUSTES",
  "PUBLICADO",
  "CANCELADO",
] as const;
export type Status = (typeof statuses)[number];
export const perfilLabel: Record<Perfil, string> = {
  ADMINISTRADOR: "Administrador",
  PROFESSOR: "Professor",
  RESPONSAVEL: "Responsável",
};
export const statusLabel: Record<Status, string> = {
  RASCUNHO: "Rascunho",
  ENVIADO_REVISAO: "Aguardando revisão",
  EM_REVISAO: "Em revisão",
  DEVOLVIDO_AJUSTES: "Devolvido para ajustes",
  PUBLICADO: "Publicado",
  CANCELADO: "Cancelado",
};
export interface Page<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
export interface NomeAtivo {
  id: number;
  nome: string;
  ativo: boolean;
}
export interface Usuario extends NomeAtivo {
  email: string;
  perfil: Perfil;
}
export interface Sessao {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  token: string;
}
export type Aluno = NomeAtivo;
export type AreaDisciplina = NomeAtivo;
export interface Professor {
  id: number;
  usuario: Usuario;
}
export interface Responsavel extends Professor {
  alunos: Aluno[];
}
export interface Turma extends NomeAtivo {
  serie: string;
  anoLetivo: number;
}
export interface Disciplina extends NomeAtivo {
  areaDisciplina: AreaDisciplina;
}
export interface Bimestre {
  id: number;
  numero: number;
  anoLetivo: number;
  dataHoraAbertura: string;
  dataHoraEncerramento: string;
}
export interface Tag extends NomeAtivo {
  descricao: string | null;
}
export interface Matricula {
  id: number;
  aluno: Aluno;
  turma: Turma;
  ativo: boolean;
}
export interface Vinculo {
  id: number;
  professor: Professor;
  disciplina: Disciplina;
  turma: Turma;
  ativo: boolean;
}
export interface Recursos {
  alunos: Aluno;
  usuarios: Usuario;
  professores: Professor;
  responsaveis: Responsavel;
  turmas: Turma;
  areas: AreaDisciplina;
  disciplinas: Disciplina;
  bimestres: Bimestre;
  tags: Tag;
  matriculas: Matricula;
  vinculos: Vinculo;
}
export type Recurso = keyof Recursos;
export interface Payloads {
  alunos: Omit<Aluno, "id">;
  usuarios: Omit<Usuario, "id"> & { senha: string };
  professores: { usuarioId: number };
  responsaveis: { usuarioId: number; alunosIds: number[] };
  turmas: Omit<Turma, "id">;
  areas: Omit<AreaDisciplina, "id">;
  disciplinas: { nome: string; areaDisciplinaId: number; ativo: boolean };
  bimestres: Omit<Bimestre, "id">;
  tags: Omit<Tag, "id">;
  matriculas: { alunoId: number; turmaId: number; ativo: boolean };
  vinculos: {
    professorId: number;
    disciplinaId: number;
    turmaId: number;
    ativo: boolean;
  };
}
export interface Acompanhamento {
  id: number;
  aluno: Aluno;
  turma: Turma;
  disciplina: Disciplina;
  professor: { id: number; nome: string };
  bimestre: Bimestre;
  descricao: string;
  media: number;
  tags: Tag[];
  status: Status;
  dataCriacao: string;
  dataAtualizacao: string;
}
export interface Edicao {
  descricao: string;
  media: number;
  tagsIds: number[];
}
export interface CadastroAcompanhamento extends Edicao {
  alunoId: number;
  turmaId: number;
  disciplinaId: number;
  bimestreId: number;
}
export interface Ciencia {
  id: number;
  responsavelId: number;
  acompanhamentoId: number;
  dataHoraCiencia: string;
  observacao: string | null;
}
export interface Indicadores {
  acompanhamentos: number;
  publicados: number;
  mediaGeral: number;
  mediasPorDisciplina: { disciplinaId: number; disciplina: string; media: number }[];
}
export interface AnoLetivo { ano: number; totalBimestres: number }
export interface Auditoria {
  id: number; dataHora: string; usuarioId: number; usuario: string;
  metodo: string; recurso: string; status: number;
}
export interface Historico {
  id: number;
  usuarioId: number;
  usuarioNome: string;
  dataHora: string;
  statusAnterior: Status | null;
  statusNovo: Status;
  acao: string;
  dadosAnteriores: string | null;
  dadosNovos: string | null;
}
export interface Filtro {
  turmaId?: number;
  disciplinaId?: number;
  professorId?: number;
  bimestreId?: number;
  alunoId?: number;
  tagId?: number;
  anoLetivo?: number;
  bimestre?: number;
  status?: Status;
}

// Nomes exibidos na interface; as chaves dos endpoints continuam sem acentos.
export const resourceLabels: Record<Recurso, string> = {
  alunos: "alunos",
  usuarios: "usuários",
  professores: "professores",
  responsaveis: "responsáveis",
  turmas: "turmas",
  areas: "áreas",
  disciplinas: "disciplinas",
  bimestres: "bimestres",
  tags: "tags",
  matriculas: "matrículas",
  vinculos: "vínculos",
};
