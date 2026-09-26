import { useState, type FormEvent } from "react";
import { Navigate } from "react-router-dom";
import {
  ArrowRight,
  Eye,
  EyeSlash,
  BookOpen,
  ShieldCheck,
  Users,
  GraduationCap,
} from "@phosphor-icons/react";
import { useAuth } from "../contexts/AuthContext";
import { errorMessage } from "../services/api";
import { authService } from "../services";
import { Button, Field } from "../components/ui";
import { Logo } from "../components/Layout";
export function Login() {
  const { user, login, expired } = useAuth();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [show, setShow] = useState(false);
  const [recovery, setRecovery] = useState(false);
  const [recoveryCode, setRecoveryCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [recoveryMessage, setRecoveryMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  if (user) return <Navigate to="/dashboard" replace />;
  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await login(email.trim(), senha);
    } catch (error) {
      setError(errorMessage(error));
    } finally {
      setBusy(false);
    }
  }
  async function requestRecovery() {
    setError(""); setRecoveryMessage("");
    if (!email.trim()) { setError("Informe seu e-mail para recuperar a senha."); return; }
    setBusy(true);
    try {
      const response = await authService.requestPasswordRecovery(email.trim());
      if (response.codigo) setRecoveryCode(response.codigo);
      setRecoveryMessage(response.codigo
        ? "Código temporário gerado. Defina sua nova senha."
        : response.mensagem);
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  }
  async function resetPassword() {
    setError(""); setRecoveryMessage("");
    if (!recoveryCode.trim()) { setError("Informe o código de recuperação."); return; }
    if (newPassword.length < 8) { setError("A nova senha deve ter pelo menos 8 caracteres."); return; }
    if (newPassword !== confirmPassword) { setError("A confirmação da senha não confere."); return; }
    setBusy(true);
    try {
      await authService.resetPassword(recoveryCode.trim(), newPassword);
      setSenha(newPassword); setRecovery(false); setRecoveryCode(""); setNewPassword(""); setConfirmPassword("");
      setRecoveryMessage("Senha redefinida. Entre com sua nova senha.");
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  }
  return (
    <div className="login-page">
      <div className="login-story">
        <Logo />
        <div className="story-content">
          <span className="eyebrow">CONEXÕES QUE TRANSFORMAM</span>
          <h1>
            Cada aprendizagem <br />
            merece ser <br />
            <span>acompanhada.</span>
          </h1>
          <p>
            Um espaço para aproximar a escola e a família, valorizar a evolução
            e cuidar de cada aluno.
          </p>
          <div className="education-art" aria-hidden="true">
            <div className="art-orbit" />
            <div className="art-book">
              <BookOpen size={92} weight="duotone" />
            </div>
            <div className="art-icon one">
              <GraduationCap size={36} />
            </div>
            <div className="art-icon two">
              <Users size={30} />
            </div>
            <div className="art-label">
              <ShieldCheck size={20} />
              Escola e família, juntas.
            </div>
          </div>
        </div>
        <small>Plataforma de Acompanhamento Escolar</small>
      </div>
      <div className="login-form-side">
        <div className="login-form">
          <span className="login-tag">
            <GraduationCap size={19} /> BEM-VINDO À KFKA
          </span>
          <h2>LOGIN</h2>
          <p>Acompanhamento escolar em um só lugar.</p>
          <form onSubmit={submit}>
            <Field label="E-mail">
              <input
                type="email"
                autoComplete="username"
                placeholder="seu.email@escola.com"
                maxLength={254}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </Field>
            <Field label="Senha">
              <div className="password-field">
                <input
                  type={show ? "text" : "password"}
                  autoComplete="current-password"
                  placeholder="Digite sua senha"
                  maxLength={72}
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  required
                />
                <button
                  type="button"
                  aria-label={show ? "Ocultar senha" : "Mostrar senha"}
                  onClick={() => setShow(!show)}
                >
                  {show ? <EyeSlash size={21} /> : <Eye size={21} />}
                </button>
              </div>
            </Field>
            <button
              className="password-recovery-link"
              type="button"
              aria-expanded={recovery}
              aria-controls="password-recovery-help"
              onClick={() => setRecovery((value) => !value)}
            >
              Esqueci minha senha
            </button>
            {recovery && (
              <div id="password-recovery-help" className="password-recovery-help">
                <strong>Recuperação de acesso</strong>
                <p>Informe seu e-mail acima para gerar um código temporário válido por 15 minutos.</p>
                <Button type="button" variant="secondary" disabled={busy} onClick={requestRecovery}>
                  {busy ? "Solicitando…" : "Solicitar código"}
                </Button>
                {recoveryCode && <>
                  <Field label="Código de recuperação"><input value={recoveryCode} onChange={e=>setRecoveryCode(e.target.value)} autoComplete="one-time-code" required /></Field>
                  <Field label="Nova senha"><input type="password" minLength={8} maxLength={72} value={newPassword} onChange={e=>setNewPassword(e.target.value)} autoComplete="new-password" required /></Field>
                  <Field label="Confirmar nova senha"><input type="password" minLength={8} maxLength={72} value={confirmPassword} onChange={e=>setConfirmPassword(e.target.value)} autoComplete="new-password" required /></Field>
                  <Button type="button" disabled={busy} onClick={resetPassword}>{busy ? "Redefinindo…" : "Redefinir senha"}</Button>
                </>}
              </div>
            )}
            {recoveryMessage && <p role="status" className="notice success">{recoveryMessage}</p>}
            {(error || expired) && (
              <p role="alert" className="inline-error">
                {error || "Sua sessão expirou. Entre novamente."}
              </p>
            )}
            <Button
              type="submit"
              disabled={busy}
              aria-label="Entrar na plataforma"
            >
              {busy ? "Entrando…" : "Entrar"}
              <ArrowRight size={20} />
            </Button>
          </form>
          <div className="login-help">
            <ShieldCheck size={21} />
            <p>
              Seu acesso e a redefinição de senha são fornecidos pela escola.
            </p>
          </div>
        </div>
        <div className="login-footer">
          KFKA · Educação com proximidade e cuidado.
        </div>
      </div>
    </div>
  );
}
