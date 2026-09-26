import { useEffect, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import {
  CaretDown,
  House,
  List,
  X,
} from "@phosphor-icons/react";
import { useAuth } from "../contexts/AuthContext";
import { Button } from "./ui";
export const titles: Record<string, string> = {
  dashboard: "Painel",
  alunos: "Alunos",
  professores: "Professores",
  responsaveis: "Responsáveis",
  turmas: "Turmas",
  areas: "Áreas de conhecimento",
  disciplinas: "Disciplinas",
  matriculas: "Matrículas",
  vinculos: "Vínculos acadêmicos",
  bimestres: "Bimestres",
  tags: "Tags",
  "minhas-turmas": "Minhas turmas",
  "minhas-disciplinas": "Minhas disciplinas",
  "meus-alunos": "Meus alunos",
  acompanhamentos: "Acompanhamentos",
  relatorios: "Relatórios",
  auditoria: "Auditoria",
  "minha-conta": "Minha conta",
};
export function Logo({ small = false }: { small?: boolean }) {
  return (
    <Link to="/dashboard" className="brand" aria-label="KFKA — início">
      <img className="brand-mark" src="/kfka-logo.png" alt="" />
      {!small && (
        <div>
          <strong>
            K F K A
          </strong>
          <small>TECNOLOGIA QUE TRANSFORMA.</small>
        </div>
      )}
    </Link>
  );
}
export function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [drawer, setDrawer] = useState(false);
  const [openGroup, setOpenGroup] = useState<string>();
  const drawerRef = useRef<HTMLElement>(null);
  useEffect(() => {
    setDrawer(false);
  }, [location]);
  useEffect(() => {
    if (!drawer) return;
    const old = document.activeElement as HTMLElement;
    drawerRef.current?.querySelector<HTMLButtonElement>("button")?.focus();
    const key = (e: KeyboardEvent) => {
      if (e.key === "Escape") setDrawer(false);
      if (e.key === "Tab") {
        const nodes =
          drawerRef.current?.querySelectorAll<HTMLElement>("a,button");
        if (!nodes?.length) return;
        const first = nodes[0],
          last = nodes[nodes.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", key);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", key);
      document.body.style.overflow = "";
      old?.focus();
    };
  }, [drawer]);
  const admin = user?.perfil === "ADMINISTRADOR";
  const teacher = user?.perfil === "PROFESSOR";
  type MenuItem = {label:string;url:string};
  type MenuGroup = {id:string;label:string;items:MenuItem[]};
  const groups:MenuGroup[]=admin?[
    {id:"pessoas",label:"Pessoas",items:[{label:"Alunos",url:"/alunos"},{label:"Responsáveis",url:"/responsaveis"},{label:"Professores",url:"/professores"}]},
    {id:"escola",label:"Estrutura escolar",items:[{label:"Áreas",url:"/areas"},{label:"Disciplinas",url:"/disciplinas"},{label:"Turmas",url:"/turmas"},{label:"Alunos por turma",url:"/matriculas"},{label:"Vínculos",url:"/vinculos"},{label:"Tags",url:"/tags"},{label:"Bimestres",url:"/bimestres"}]},
    {id:"acompanhamento",label:"Acompanhamento",items:[{label:"Todos os registros",url:"/acompanhamentos"},{label:"Fila de revisão",url:"/acompanhamentos?status=ENVIADO_REVISAO"},{label:"Relatórios",url:"/relatorios"}]},
    {id:"administracao",label:"Administração",items:[{label:"Auditoria",url:"/auditoria"}]},
  ]:teacher?[
    {id:"atuacao",label:"Minha atuação",items:[{label:"Minhas turmas",url:"/minhas-turmas"},{label:"Minhas disciplinas",url:"/minhas-disciplinas"}]},
    {id:"acompanhamento",label:"Acompanhamentos",items:[{label:"Consultar registros",url:"/acompanhamentos"},{label:"Novo acompanhamento",url:"/acompanhamentos/novo"},{label:"Relatórios",url:"/relatorios"}]},
  ]:[
    {id:"familia",label:"Família",items:[{label:"Meus alunos",url:"/meus-alunos"}]},
    {id:"acompanhamento",label:"Acompanhamentos",items:[{label:"Registros publicados",url:"/acompanhamentos"},{label:"Relatórios",url:"/relatorios"}]},
  ];
  useEffect(()=>{
    const group=groups.find(g=>g.items.some(item=>item.url.split("?")[0]===location.pathname||location.pathname.startsWith(item.url.split("?")[0]+"/")));
    if(group)setOpenGroup(group.id);
  },[location.pathname,user?.perfil]);
  if (!user) return null;
  const title = titles[location.pathname.split("/")[1]] || "KFKA";
  const link = (label: string, url: string) => (
    <NavLink
      key={url}
      to={url}
      className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
    >
      <span>{label}</span>
    </NavLink>
  );
  const accordion=(group:MenuGroup)=>{
    const open=openGroup===group.id;
    const contains=group.items.some(item=>location.pathname===item.url.split("?")[0]||location.pathname.startsWith(item.url.split("?")[0]+"/"));
    return <div className={`nav-group ${contains?"current":""}`} key={group.id}>
      <button type="button" className="nav-link nav-group-toggle" aria-expanded={open} aria-controls={`menu-${group.id}`} onClick={()=>setOpenGroup(open?undefined:group.id)}>
        <span>{group.label}</span><CaretDown className={open?"rotated":""} size={17}/>
      </button>
      {open&&<div className="nav-children" id={`menu-${group.id}`}>{group.items.map(item=>link(item.label,item.url))}</div>}
    </div>
  };
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Ir para o conteúdo
      </a>
      {drawer && (
        <button
          className="drawer-backdrop"
          aria-label="Fechar menu lateral"
          onClick={() => setDrawer(false)}
        />
      )}
      <aside
        ref={drawerRef}
        className={`sidebar ${drawer ? "drawer-open" : ""}`}
        aria-label="Menu principal"
        role={drawer ? "dialog" : undefined}
        aria-modal={drawer || undefined}
      >
        <Button className="mobile-only close-menu" variant="ghost" onClick={() => setDrawer(false)} aria-label="Fechar menu">
          <X />
        </Button>
        <nav>
          {link("Início","/dashboard")}
          {groups.map(accordion)}
          {link("Minha conta","/minha-conta")}
        </nav>
        <div className="sidebar-bottom">
          <button className="button quiet logout-button" onClick={logout}>
            Sair da conta
          </button>
        </div>
      </aside>
      <div className="main-shell">
        <header className="header">
          <Logo />
          <div className="header-title">
            <Button
              variant="ghost"
              className="mobile-only"
              aria-label="Abrir menu"
              onClick={() => setDrawer(true)}
            >
              <List size={23} />
            </Button>
            <span className="breadcrumb">
              <House size={16} />
              <span>/</span>
              <strong>{title}</strong>
            </span>
          </div>
          <Link className="profile" to="/minha-conta">Meu perfil</Link>
        </header>
        <main id="main-content" tabIndex={-1}>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
