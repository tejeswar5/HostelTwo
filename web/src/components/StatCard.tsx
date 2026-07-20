export function StatCard({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <div className="rounded-lg border border-border bg-white p-5">
      <p className="font-mono text-[11px] uppercase tracking-wider text-ink-muted">{label}</p>
      <p className="mt-2 text-2xl font-extrabold text-brand">{value}</p>
      {hint && <p className="mt-1 text-xs text-ink-muted">{hint}</p>}
    </div>
  );
}
