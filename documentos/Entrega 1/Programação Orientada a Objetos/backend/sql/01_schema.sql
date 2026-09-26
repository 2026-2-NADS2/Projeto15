-- KFKA: schema inicial MySQL 8+, compatível com o mapeamento JPA.
-- Execute uma única vez em um banco vazio, antes de usar o perfil prod.
-- Não contém usuários, senhas nem segredo JWT.

create table audit_log (
    status integer not null,
    data_hora datetime(6) not null,
    id bigint not null auto_increment,
    usuario_id bigint not null,
    metodo varchar(20) not null,
    usuario varchar(150) not null,
    recurso varchar(500) not null,
    primary key (id)
) engine=InnoDB;
create index idx_audit_data on audit_log (data_hora);

create table acompanhamento (
    media decimal(7,2) not null,
    aluno_id bigint not null,
    bimestre_id bigint not null,
    data_atualizacao datetime(6) not null,
    data_criacao datetime(6) not null,
    disciplina_id bigint not null,
    id bigint not null auto_increment,
    professor_id bigint not null,
    turma_id bigint not null,
    versao bigint,
    descricao varchar(10000) not null,
    status enum ('CANCELADO','DEVOLVIDO_AJUSTES','EM_REVISAO','ENVIADO_REVISAO','PUBLICADO','RASCUNHO') not null,
    primary key (id)
) engine=InnoDB;
create table acompanhamento_tag (
    acompanhamento_id bigint not null,
    tag_id bigint not null,
    primary key (acompanhamento_id,
    tag_id)
) engine=InnoDB;
create table aluno (
    ativo bit not null,
    id bigint not null auto_increment,
    nome varchar(150) not null,
    primary key (id)
) engine=InnoDB;
create table area_disciplina (
    ativo bit not null,
    id bigint not null auto_increment,
    nome varchar(150) not null,
    primary key (id)
) engine=InnoDB;
create table bimestre (
    ano_letivo integer not null,
    numero integer not null,
    data_hora_abertura datetime(6) not null,
    data_hora_encerramento datetime(6) not null,
    id bigint not null auto_increment,
    primary key (id)
) engine=InnoDB;
create table ciencia_responsavel (
    acompanhamento_id bigint not null,
    data_hora_ciencia datetime(6) not null,
    id bigint not null auto_increment,
    responsavel_id bigint not null,
    observacao varchar(1000),
    primary key (id)
) engine=InnoDB;
create table disciplina (
    ativo bit not null,
    area_disciplina_id bigint not null,
    id bigint not null auto_increment,
    nome varchar(150) not null,
    primary key (id)
) engine=InnoDB;
create table historico_acompanhamento (
    acompanhamento_id bigint not null,
    data_hora datetime(6) not null,
    id bigint not null auto_increment,
    usuario_responsavel_pela_acao_id bigint not null,
    acao varchar(1000) not null,
    dados_anteriores LONGTEXT,
    dados_novos LONGTEXT,
    status_anterior enum ('CANCELADO','DEVOLVIDO_AJUSTES','EM_REVISAO','ENVIADO_REVISAO','PUBLICADO','RASCUNHO'),
    status_novo enum ('CANCELADO','DEVOLVIDO_AJUSTES','EM_REVISAO','ENVIADO_REVISAO','PUBLICADO','RASCUNHO') not null,
    primary key (id)
) engine=InnoDB;
create table matricula (
    ativo bit not null,
    aluno_id bigint not null,
    id bigint not null auto_increment,
    turma_id bigint not null,
    primary key (id)
) engine=InnoDB;
create table professor (
    id bigint not null auto_increment,
    usuario_id bigint not null,
    primary key (id)
) engine=InnoDB;
create table responsavel (
    id bigint not null auto_increment,
    usuario_id bigint not null,
    primary key (id)
) engine=InnoDB;
create table responsavel_aluno (
    aluno_id bigint not null,
    responsavel_id bigint not null,
    primary key (responsavel_id,
    aluno_id)
) engine=InnoDB;
create table tag (
    ativo bit not null,
    id bigint not null auto_increment,
    nome varchar(150) not null,
    descricao varchar(500),
    primary key (id)
) engine=InnoDB;
create table turma (
    ano_letivo integer not null,
    ativo bit not null,
    id bigint not null auto_increment,
    serie varchar(50) not null,
    nome varchar(150) not null,
    primary key (id)
) engine=InnoDB;
create table usuario (
    ativo bit not null,
    id bigint not null auto_increment,
    senha varchar(100) not null,
    nome varchar(150) not null,
    email varchar(254) not null,
    perfil enum ('ADMINISTRADOR','PROFESSOR','RESPONSAVEL') not null,
    primary key (id)
) engine=InnoDB;
create table recuperacao_senha_token (
    expira_em datetime(6) not null,
    id bigint not null auto_increment,
    usado_em datetime(6),
    usuario_id bigint not null,
    token_hash varchar(64) not null,
    primary key (id)
) engine=InnoDB;
create unique index idx_recuperacao_token on recuperacao_senha_token (token_hash);
create table vinculo_turma (
    ativo bit not null,
    disciplina_id bigint not null,
    id bigint not null auto_increment,
    professor_id bigint not null,
    turma_id bigint not null,
    primary key (id)
) engine=InnoDB;
create index idx_acomp_status on acompanhamento (status);
create index idx_acomp_aluno on acompanhamento (aluno_id);
create index idx_acomp_professor on acompanhamento (professor_id);
create index idx_acomp_turma on acompanhamento (turma_id);
create index idx_acomp_disciplina on acompanhamento (disciplina_id);
create index idx_acomp_bimestre on acompanhamento (bimestre_id);
alter table acompanhamento add constraint uk_acompanhamento unique (aluno_id, turma_id, disciplina_id, professor_id, bimestre_id);
alter table bimestre add constraint uk_bimestre unique (ano_letivo, numero);
alter table ciencia_responsavel add constraint uk_ciencia unique (responsavel_id, acompanhamento_id);
alter table matricula add constraint uk_matricula unique (aluno_id, turma_id);
alter table professor add constraint uk_professor_usuario unique (usuario_id);
alter table responsavel add constraint uk_responsavel_usuario unique (usuario_id);
create index idx_turma_ano on turma (ano_letivo);
alter table usuario add constraint uk_usuario_email unique (email);
alter table vinculo_turma add constraint uk_vinculo unique (professor_id, disciplina_id, turma_id);
alter table acompanhamento add constraint fk_acompanhamento_aluno_id foreign key (aluno_id) references aluno (id);
alter table acompanhamento add constraint fk_acompanhamento_bimestre_id foreign key (bimestre_id) references bimestre (id);
alter table acompanhamento add constraint fk_acompanhamento_disciplina_id foreign key (disciplina_id) references disciplina (id);
alter table acompanhamento add constraint fk_acompanhamento_professor_id foreign key (professor_id) references professor (id);
alter table acompanhamento add constraint fk_acompanhamento_turma_id foreign key (turma_id) references turma (id);
alter table acompanhamento_tag add constraint fk_acompanhamento_tag_tag_id foreign key (tag_id) references tag (id);
alter table acompanhamento_tag add constraint fk_acompanhamento_tag_acompanhamento_id foreign key (acompanhamento_id) references acompanhamento (id);
alter table ciencia_responsavel add constraint fk_ciencia_responsavel_acompanhamento_id foreign key (acompanhamento_id) references acompanhamento (id);
alter table ciencia_responsavel add constraint fk_ciencia_responsavel_responsavel_id foreign key (responsavel_id) references responsavel (id);
alter table disciplina add constraint fk_disciplina_area_disciplina_id foreign key (area_disciplina_id) references area_disciplina (id);
alter table historico_acompanhamento add constraint fk_historico_acompanhamento_acompanhamento_id foreign key (acompanhamento_id) references acompanhamento (id);
alter table historico_acompanhamento add constraint fk_historico_acompanhamento_usuario_responsavel_pela_acao_id foreign key (usuario_responsavel_pela_acao_id) references usuario (id);
alter table matricula add constraint fk_matricula_aluno_id foreign key (aluno_id) references aluno (id);
alter table matricula add constraint fk_matricula_turma_id foreign key (turma_id) references turma (id);
alter table professor add constraint fk_professor_usuario_id foreign key (usuario_id) references usuario (id);
alter table responsavel add constraint fk_responsavel_usuario_id foreign key (usuario_id) references usuario (id);
alter table responsavel_aluno add constraint fk_responsavel_aluno_aluno_id foreign key (aluno_id) references aluno (id);
alter table responsavel_aluno add constraint fk_responsavel_aluno_responsavel_id foreign key (responsavel_id) references responsavel (id);
alter table recuperacao_senha_token add constraint fk_recuperacao_usuario foreign key (usuario_id) references usuario (id);
alter table vinculo_turma add constraint fk_vinculo_turma_disciplina_id foreign key (disciplina_id) references disciplina (id);
alter table vinculo_turma add constraint fk_vinculo_turma_professor_id foreign key (professor_id) references professor (id);
alter table vinculo_turma add constraint fk_vinculo_turma_turma_id foreign key (turma_id) references turma (id);

alter table bimestre add constraint ck_bimestre_numero check (numero between 1 and 4);
alter table bimestre add constraint ck_bimestre_periodo check (data_hora_encerramento > data_hora_abertura);
