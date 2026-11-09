#!/usr/bin/env bash
# Cria contas e dados de DEMONSTRAÇÃO (fictícios) para testar os três perfis do KFKA.
#
# Uso (Git Bash), com o login do administrador principal no ambiente:
#   ADMIN_EMAIL=... ADMIN_PASSWORD=... bash scripts/dados-demo.sh https://SEU-ENDERECO
#
# Execute uma única vez por banco. Precisa de curl e jq.
set -euo pipefail

URL="${1:?Informe o endereço do sistema, ex.: https://app-production-aedab.up.railway.app}"
API="$URL/api"
: "${ADMIN_EMAIL:?Defina ADMIN_EMAIL}" "${ADMIN_PASSWORD:?Defina ADMIN_PASSWORD}"

# Contas de demonstração (públicas, somente para teste)
PROF_EMAIL="professor@kfka.com"; PROF_SENHA="Professor@2026"
RESP_EMAIL="responsavel@kfka.com"; RESP_SENHA="Responsavel@2026"
ANO=2026; BIMESTRE=4
ABERTURA="2026-10-01T00:00:00"; ENCERRAMENTO="2026-12-31T23:59:59"

J='Content-Type: application/json'
# O corpo vai por stdin para preservar acentos (UTF-8) no Git Bash do Windows
post()  { printf '%s' "$3" | curl -sS -H "$J" -H "Authorization: Bearer $1" --data-binary @- "$API$2"; }
patch() { curl -sS -X PATCH -H "Authorization: Bearer $1" "$API$2"; }
get()   { curl -sS -H "Authorization: Bearer $1" "$API$2"; }
login() { printf '{"email":"%s","senha":"%s"}' "$1" "$2" | curl -sS -H "$J" --data-binary @- "$API/usuarios/logar" | jq -r '.token // empty'; }
id_de() { local r; r=$(cat); local id; id=$(jq -r '.id // empty' <<<"$r"); [ -n "$id" ] || { echo "Falhou: $1 -> $r" >&2; exit 1; }; echo "$id"; }
# Reaproveita um registro com o mesmo nome, se já existir
achar_ou_criar() { # token rota nome corpo
  local id; id=$(get "$1" "$2?size=100" | jq -r --arg n "$3" '.content[]? | select(.nome==$n) | .id' | head -1)
  if [ -n "$id" ]; then echo "$id"; else post "$1" "$2" "$4" | id_de "$2 $3"; fi
}

echo "Sistema: $(curl -sS "$URL/status")"
ADM=$(login "$ADMIN_EMAIL" "$ADMIN_PASSWORD"); [ -n "$ADM" ] || { echo "Login do administrador falhou" >&2; exit 1; }

if get "$ADM" "/usuarios?size=100" | jq -e --arg e "$PROF_EMAIL" '.content[] | select(.email==$e)' >/dev/null; then
  echo "As contas de demonstração já existem neste banco. Nada foi alterado."; exit 0
fi

echo "== Estrutura escolar"
LING=$(achar_ou_criar "$ADM" /areas "Linguagens" '{"nome":"Linguagens","ativo":true}')
EXAT=$(achar_ou_criar "$ADM" /areas "Exatas" '{"nome":"Exatas","ativo":true}')
PORT=$(achar_ou_criar "$ADM" /disciplinas "Língua Portuguesa" "{\"nome\":\"Língua Portuguesa\",\"areaDisciplinaId\":$LING,\"ativo\":true}")
MAT=$(achar_ou_criar "$ADM" /disciplinas "Matemática" "{\"nome\":\"Matemática\",\"areaDisciplinaId\":$EXAT,\"ativo\":true}")
TURMA=$(achar_ou_criar "$ADM" /turmas "5º A" "{\"nome\":\"5º A\",\"serie\":\"5º ano\",\"anoLetivo\":$ANO,\"ativo\":true}")
TAG1=$(achar_ou_criar "$ADM" /tags "Ótimo desempenho" '{"nome":"Ótimo desempenho","descricao":"Superou as expectativas do bimestre","ativo":true}')
TAG2=$(achar_ou_criar "$ADM" /tags "Participação" '{"nome":"Participação","descricao":"Participa ativamente das atividades","ativo":true}')
BIM=$(get "$ADM" "/bimestres?size=100" | jq -r --argjson a $ANO --argjson n $BIMESTRE '.content[]? | select(.anoLetivo==$a and .numero==$n) | .id' | head -1)
[ -n "$BIM" ] || BIM=$(post "$ADM" /bimestres "{\"numero\":$BIMESTRE,\"anoLetivo\":$ANO,\"dataHoraAbertura\":\"$ABERTURA\",\"dataHoraEncerramento\":\"$ENCERRAMENTO\"}" | id_de bimestre)
echo "   áreas $LING/$EXAT · disciplinas $PORT/$MAT · turma $TURMA · bimestre $BIM · tags $TAG1/$TAG2"

echo "== Pessoas"
UP=$(post "$ADM" /usuarios "{\"nome\":\"Beatriz Santos\",\"email\":\"$PROF_EMAIL\",\"senha\":\"$PROF_SENHA\",\"perfil\":\"PROFESSOR\",\"ativo\":true}" | id_de professor)
UR=$(post "$ADM" /usuarios "{\"nome\":\"Maria Oliveira\",\"email\":\"$RESP_EMAIL\",\"senha\":\"$RESP_SENHA\",\"perfil\":\"RESPONSAVEL\",\"ativo\":true}" | id_de responsavel)
PROF=$(post "$ADM" /professores "{\"usuarioId\":$UP}" | id_de cadastro-professor)
ALUNO=$(post "$ADM" /alunos '{"nome":"Lucas Oliveira","ativo":true}' | id_de aluno)
ALUNA=$(post "$ADM" /alunos '{"nome":"Sofia Lima","ativo":true}' | id_de aluna)
post "$ADM" /responsaveis "{\"usuarioId\":$UR,\"alunosIds\":[$ALUNO]}" | id_de cadastro-responsavel >/dev/null
for A in $ALUNO $ALUNA; do post "$ADM" /matriculas "{\"alunoId\":$A,\"turmaId\":$TURMA,\"ativo\":true}" | id_de matricula >/dev/null; done
for D in $PORT $MAT; do post "$ADM" /vinculos "{\"professorId\":$PROF,\"disciplinaId\":$D,\"turmaId\":$TURMA,\"ativo\":true}" | id_de vinculo >/dev/null; done
echo "   professora Beatriz Santos · responsável Maria Oliveira (mãe de Lucas) · alunos Lucas e Sofia na turma 5º A"

echo "== Acompanhamentos"
PT=$(login "$PROF_EMAIL" "$PROF_SENHA")
A1=$(post "$PT" /acompanhamentos "{\"alunoId\":$ALUNO,\"turmaId\":$TURMA,\"disciplinaId\":$PORT,\"bimestreId\":$BIM,\"descricao\":\"Lucas evoluiu na leitura e na produção de textos, com boa participação nas atividades em grupo.\",\"media\":8.5,\"tagsIds\":[$TAG1,$TAG2]}" | id_de acompanhamento-1)
A2=$(post "$PT" /acompanhamentos "{\"alunoId\":$ALUNO,\"turmaId\":$TURMA,\"disciplinaId\":$MAT,\"bimestreId\":$BIM,\"descricao\":\"Lucas resolve bem problemas com frações; precisa praticar mais divisão.\",\"media\":7.0,\"tagsIds\":[$TAG2]}" | id_de acompanhamento-2)
A3=$(post "$PT" /acompanhamentos "{\"alunoId\":$ALUNA,\"turmaId\":$TURMA,\"disciplinaId\":$PORT,\"bimestreId\":$BIM,\"descricao\":\"Sofia lê com fluência e interpreta bem os textos.\",\"media\":9.0,\"tagsIds\":[$TAG1]}" | id_de acompanhamento-3)
for A in $A1 $A2; do patch "$PT" "/acompanhamentos/$A/enviar" | id_de enviar >/dev/null; done
patch "$ADM" "/acompanhamentos/$A1/iniciar-revisao" | id_de revisao >/dev/null
patch "$ADM" "/acompanhamentos/$A1/publicar" | id_de publicar >/dev/null
echo "   #$A1 Português (Lucas): PUBLICADO  ·  #$A2 Matemática (Lucas): ENVIADO_REVISAO  ·  #$A3 Português (Sofia): RASCUNHO"

echo
echo "Pronto. Contas de demonstração:"
echo "  Professor:   $PROF_EMAIL / $PROF_SENHA"
echo "  Responsável: $RESP_EMAIL / $RESP_SENHA"
