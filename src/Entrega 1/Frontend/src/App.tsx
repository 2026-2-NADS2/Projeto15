import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
import { useAuth, AuthProvider } from "./contexts/AuthContext";
import type { Perfil, Recurso } from "./types";
import { perfilLabel } from "./types";
import { Layout } from "./components/Layout";
import { Card, PageHeader, ToastProvider } from "./components/ui";
import { Login } from "./pages/Login";
import { Dashboard } from "./pages/Dashboard";
import { ResourceList, ResourceDetail, ResourceForm } from "./pages/Resources";
import { Records } from "./pages/Records";
import { RecordDetail } from "./pages/RecordDetail";
import { RecordForm } from "./pages/RecordForm";
import { Audit, PeriodManagement } from "./pages/ReferenceAdmin";
import { AccessDenied, MyClasses, MyClassDetail, MyStudents, MySubjects, NotFound } from "./pages/ScopedViews";
function ProtectedRoute({profiles}:{profiles?:Perfil[]}){const{user}=useAuth();if(!user)return <Navigate to="/login" replace/>;return profiles&&!profiles.includes(user.perfil)?<AccessDenied/>:<Outlet/>}
const adminResources:Recurso[]=["alunos","turmas","professores","responsaveis","areas","disciplinas","tags","matriculas","vinculos"];
function DetailRoute(){const location=useLocation();return <RecordDetail key={location.pathname}/>}
function Account(){const{user}=useAuth();return <><PageHeader title="Minha conta" description="Suas informações de acesso à plataforma."/><Card className="detail-card"><h2>Informações pessoais</h2><dl className="detail-grid"><div><dt>Nome</dt><dd>{user?.nome}</dd></div><div><dt>E-mail</dt><dd>{user?.email}</dd></div><div><dt>Perfil</dt><dd>{user&&perfilLabel[user.perfil]}</dd></div></dl><p className="notice">Para alterar seus dados ou sua senha, procure a secretaria da escola.</p></Card></>}
export default function App(){return <AuthProvider><ToastProvider><BrowserRouter><Routes><Route path="/login" element={<Login/>}/><Route element={<ProtectedRoute/>}><Route element={<Layout/>}><Route index element={<Navigate to="/dashboard" replace/>}/><Route path="dashboard" element={<Dashboard/>}/><Route path="minha-conta" element={<Account/>}/>
 <Route element={<ProtectedRoute profiles={["ADMINISTRADOR"]}/>}>{adminResources.map(r=><Route key={r}><Route path={r} element={<ResourceList resource={r}/>}/><Route path={`${r}/novo`} element={<ResourceForm resource={r}/>}/><Route path={`${r}/:id`} element={<ResourceDetail resource={r}/>}/><Route path={`${r}/:id/editar`} element={<ResourceForm resource={r}/>} /></Route>)}<Route path="bimestres" element={<PeriodManagement/>}/><Route path="bimestres/:id/editar" element={<ResourceForm resource="bimestres"/>}/><Route path="auditoria" element={<Audit/>}/><Route path="acompanhamentos/:id/revisao" element={<RecordForm review/>}/></Route>
 <Route element={<ProtectedRoute profiles={["PROFESSOR"]}/> }><Route path="minhas-turmas" element={<MyClasses/>}/><Route path="minhas-turmas/:id" element={<MyClassDetail/>}/><Route path="minhas-disciplinas" element={<MySubjects/>}/><Route path="acompanhamentos/novo" element={<RecordForm/>}/><Route path="acompanhamentos/:id/editar" element={<RecordForm/>}/></Route>
 <Route element={<ProtectedRoute profiles={["RESPONSAVEL"]}/> }><Route path="meus-alunos" element={<MyStudents/>}/></Route>
 <Route path="acompanhamentos" element={<Records/>}/><Route path="relatorios" element={<Records report/>}/><Route path="acompanhamentos/:id" element={<DetailRoute/>}/><Route path="*" element={<NotFound/>}/></Route></Route><Route path="*" element={<Navigate to="/login" replace/>}/></Routes></BrowserRouter></ToastProvider></AuthProvider>}
