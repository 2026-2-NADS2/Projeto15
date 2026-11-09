import { useCallback } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { useQuery } from "../hooks/useQuery";
import { relatorioService } from "../services";
import { Card, ErrorState, Loading } from "../components/ui";
import { number } from "../utils/format";
export function Dashboard() {
  const { user } = useAuth();
  const query = useQuery(useCallback((signal: AbortSignal) => relatorioService.indicators(signal), []));
  const admin=user?.perfil==="ADMINISTRADOR", teacher=user?.perfil==="PROFESSOR";
  const next=admin?"/acompanhamentos?status=ENVIADO_REVISAO":teacher?"/acompanhamentos/novo":"/relatorios";
  return <><section className="reference-heading"><div><p className="eyebrow">ACOMPANHAMENTO ESCOLAR</p><h1>Olá, {user?.nome.split(" ")[0]}!</h1><p>Informações atualizadas conforme as permissões da sua conta.</p></div><span>Disciplina hoje, um futuro melhor amanhã.</span></section>
  {query.error&&<ErrorState error={query.error} retry={query.reload}/>} {query.loading?<Loading/>:<><section className="reference-metrics" aria-label="Indicadores"><article className="reference-metric"><strong>{number(query.data?.acompanhamentos??0)}</strong><span>Acompanhamentos</span></article><article className="reference-metric"><strong>{number(query.data?.publicados??0)}</strong><span>Publicados</span></article><article className="reference-metric"><strong>{query.data?.acompanhamentos?number(query.data.mediaGeral):"—"}</strong><span>Média geral</span></article></section><section className="reference-grid"><Card className="reference-panel"><h2>Médias por disciplina</h2>{query.data?.mediasPorDisciplina.length?query.data.mediasPorDisciplina.map(item=><div className="bar-row" key={item.disciplinaId}><div className="bar-label"><span>{item.disciplina}</span><strong>{number(item.media)}</strong></div><div className="bar-track"><span className="bar-fill" style={{width:`${Math.max(0,Math.min(100,item.media*10))}%`}}/></div></div>):<p className="muted">Nenhuma média disponível.</p>}</Card><Card className="reference-panel"><h2>Próximo passo</h2><p>{admin?"Revise os registros enviados antes de publicá-los.":teacher?"Registre o desenvolvimento dos alunos das suas turmas.":"Consulte os relatórios publicados dos seus alunos."}</p><Link className="button primary" to={next}>{admin?"Abrir fila de revisão":teacher?"Criar acompanhamento":"Consultar relatórios"}</Link></Card></section></>}</>;
}
