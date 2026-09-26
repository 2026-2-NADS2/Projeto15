import {
  useEffect,
  useId,
  isValidElement,
  cloneElement,
  type ReactElement,
  useRef,
  useState,
  createContext,
  useContext,
  type ReactNode,
  type ButtonHTMLAttributes,
} from "react";
import {
  ArrowLeft,
  ArrowRight,
  CheckCircle,
  ClipboardText,
  WarningCircle,
  X,
} from "@phosphor-icons/react";
import type { Page, Status } from "../types";
import { statusLabel } from "../types";
export function Button({
  children,
  variant = "primary",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "danger" | "ghost";
}) {
  return (
    <button {...props} className={`button ${variant} ${props.className || ""}`}>
      {children}
    </button>
  );
}
export function Card({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return <section className={`card ${className}`}>{children}</section>;
}
export function PageHeader({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children?: ReactNode;
}) {
  return (
    <div className="page-heading">
      <div>
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      <div className="actions">{children}</div>
    </div>
  );
}
export function Badge({ status }: { status: Status }) {
  return (
    <span className={`badge ${status.toLowerCase()}`}>
      <span className="badge-dot" />
      {statusLabel[status]}
    </span>
  );
}
export function Field({
  label,
  children,
  hint,
}: {
  label: string;
  children: ReactNode;
  hint?: string;
}) {
  const id = useId();
  const directControl =
    isValidElement(children) &&
    typeof children.type === "string" &&
    ["input", "select", "textarea"].includes(children.type);
  if (directControl) {
    return (
      <div className="field">
        <label htmlFor={id}>{label}</label>
        {cloneElement(
          children as ReactElement<{
            id?: string;
            "aria-describedby"?: string;
          }>,
          { id, "aria-describedby": hint ? `${id}-hint` : undefined },
        )}
        {hint && <small id={`${id}-hint`}>{hint}</small>}
      </div>
    );
  }
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {hint && <small>{hint}</small>}
    </label>
  );
}
export function EmptyState({
  title = "Nenhum registro encontrado",
  description = "Os registros aparecerão aqui quando estiverem disponíveis.",
  children,
}: {
  title?: string;
  description?: string;
  children?: ReactNode;
}) {
  return (
    <div className="empty-state">
      <div className="empty-icon">
        <ClipboardText size={30} />
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
      {children}
    </div>
  );
}
export function Loading() {
  return (
    <div role="status" aria-label="Carregando" className="loading">
      <div className="skeleton heading" />
      <div className="skeleton" />
      <div className="skeleton" />
      <div className="skeleton" />
      <span className="sr-only">Carregando dados…</span>
    </div>
  );
}
export function ErrorState({
  error,
  retry,
}: {
  error: string;
  retry?: () => void;
}) {
  return (
    <div role="alert" className="error-state">
      <WarningCircle size={24} />
      <div>
        <strong>Não foi possível carregar</strong>
        <p>{error}</p>
        {retry && (
          <Button variant="secondary" onClick={retry}>
            Tentar novamente
          </Button>
        )}
      </div>
    </div>
  );
}
export function Pagination({
  page,
  onChange,
}: {
  page: Pick<
    Page<unknown>,
    "number" | "totalPages" | "totalElements" | "first" | "last"
  >;
  onChange: (page: number) => void;
}) {
  if (!page.totalElements) return null;
  return (
    <nav className="pagination" aria-label="Paginação">
      <span>
        {page.totalElements} registros · Página {page.number + 1} de{" "}
        {page.totalPages}
      </span>
      <div className="actions">
        <Button
          variant="secondary"
          disabled={page.first}
          onClick={() => onChange(page.number - 1)}
        >
          <ArrowLeft />
          Anterior
        </Button>
        <Button
          variant="secondary"
          disabled={page.last}
          onClick={() => onChange(page.number + 1)}
        >
          Próxima
          <ArrowRight />
        </Button>
      </div>
    </nav>
  );
}
export function ConfirmDialog({
  title,
  description,
  confirm,
  close,
  busy,
}: {
  title: string;
  description: string;
  confirm: () => void;
  close: () => void;
  busy: boolean;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const previous = document.activeElement as HTMLElement;
    const d = dialog.current;
    d?.showModal();
    return () => {
      d?.close();
      previous?.focus();
    };
  }, []);
  return (
    <dialog
      ref={dialog}
      className="modal"
      onCancel={(event) => {
        event.preventDefault();
        if (!busy) close();
      }}
    >
      <div className="modal-heading">
        <h2>{title}</h2>
        <Button
          variant="ghost"
          aria-label="Fechar"
          disabled={busy}
          onClick={close}
        >
          <X />
        </Button>
      </div>
      <p>{description}</p>
      <div className="actions">
        <Button variant="secondary" disabled={busy} onClick={close}>
          Voltar
        </Button>
        <Button disabled={busy} onClick={confirm}>
          {busy ? "Aguarde…" : "Confirmar"}
        </Button>
      </div>
    </dialog>
  );
}
const ToastContext = createContext<(message: string, error?: boolean) => void>(
  () => {},
);
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<{ message: string; error: boolean }>();
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(undefined), 6000);
    return () => clearTimeout(timer);
  }, [toast]);
  return (
    <ToastContext.Provider
      value={(message, error = false) => setToast({ message, error })}
    >
      {children}
      {toast && (
        <div
          className={`toast ${toast.error ? "error" : ""}`}
          role={toast.error ? "alert" : "status"}
        >
          {toast.error ? (
            <WarningCircle size={22} />
          ) : (
            <CheckCircle size={22} />
          )}
          <span>{toast.message}</span>
          <button
            aria-label="Fechar mensagem"
            onClick={() => setToast(undefined)}
          >
            <X />
          </button>
        </div>
      )}
    </ToastContext.Provider>
  );
}
export const useToast = () => useContext(ToastContext);
