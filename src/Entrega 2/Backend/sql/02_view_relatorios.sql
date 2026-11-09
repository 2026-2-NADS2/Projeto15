-- Visão administrativa de relatórios publicados; não expõe senha ou e-mail.
-- A API continua consultando JPA com filtros de propriedade e perfil.
CREATE OR REPLACE VIEW vw_acompanhamentos_publicados AS
SELECT
    a.id AS acompanhamento_id,
    al.id AS aluno_id,
    al.nome AS aluno_nome,
    t.id AS turma_id,
    t.nome AS turma_nome,
    t.serie,
    b.ano_letivo,
    b.id AS bimestre_id,
    b.numero AS bimestre_numero,
    d.id AS disciplina_id,
    d.nome AS disciplina_nome,
    ar.nome AS area_nome,
    p.id AS professor_id,
    u.nome AS professor_nome,
    a.descricao,
    a.media,
    a.data_criacao,
    a.data_atualizacao,
    (SELECT COUNT(*) FROM ciencia_responsavel c WHERE c.acompanhamento_id = a.id) AS total_ciencias
FROM acompanhamento a
JOIN aluno al ON al.id = a.aluno_id
JOIN turma t ON t.id = a.turma_id
JOIN bimestre b ON b.id = a.bimestre_id
JOIN disciplina d ON d.id = a.disciplina_id
JOIN area_disciplina ar ON ar.id = d.area_disciplina_id
JOIN professor p ON p.id = a.professor_id
JOIN usuario u ON u.id = p.usuario_id
WHERE a.status = 'PUBLICADO';

-- Exemplo: SELECT * FROM vw_acompanhamentos_publicados WHERE ano_letivo = 2026;
-- Demonstração dos índices: SHOW INDEX FROM acompanhamento;
-- EXPLAIN SELECT * FROM acompanhamento WHERE aluno_id = 1 AND status = 'PUBLICADO';
