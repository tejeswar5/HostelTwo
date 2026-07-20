import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

export function AuthLayout({ title, subtitle, children, footer }: { title: string; subtitle: string; children: ReactNode; footer: ReactNode }) {
  return (
    <main className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-6 py-12">
      <Link to="/" className="mb-8 font-mono text-xs tracking-[0.12em] text-ink-muted">
        &larr; HOSTEL ONE
      </Link>
      <h1 className="text-2xl font-bold text-brand">{title}</h1>
      <p className="mt-1 text-sm text-ink-muted">{subtitle}</p>
      <div className="mt-8">{children}</div>
      <div className="mt-6 text-sm text-ink-muted">{footer}</div>
    </main>
  );
}

export function FormField({ label, error, children }: { label: string; error?: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="text-sm font-medium text-ink">{label}</span>
      <div className="mt-1">{children}</div>
      {error && <span className="mt-1 block text-xs text-booked">{error}</span>}
    </label>
  );
}

export const inputClass = 'w-full rounded-md border border-border px-3 py-2 text-sm outline-none focus:border-brand';
export const primaryButtonClass = 'w-full rounded-md bg-brand px-4 py-2.5 font-semibold text-white disabled:opacity-50';
