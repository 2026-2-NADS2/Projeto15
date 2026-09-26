import type { Bimestre } from "../types";
export const number = (value: number) =>
  new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 2 }).format(value);
export function date(value: string | null | undefined) {
  if (!value) return "Não informado";
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}
export function schoolNow() {
  const parts = new Intl.DateTimeFormat("sv-SE", {
    timeZone: import.meta.env.VITE_FUSO_HORARIO || "America/Sao_Paulo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hourCycle: "h23",
  }).format(new Date());
  return parts.replace(" ", "T");
}
export const isOpen = (b: Bimestre) => {
  const now = schoolNow();
  return now >= b.dataHoraAbertura && now < b.dataHoraEncerramento;
};
export const mediaMin = Number(import.meta.env.VITE_MEDIA_MINIMA || 0);
export const mediaMax = Number(import.meta.env.VITE_MEDIA_MAXIMA || 10);
