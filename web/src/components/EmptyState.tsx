export function EmptyState({ message }: { message: string }) {
  return <p className="rounded-md border border-dashed border-border p-6 text-center text-sm text-ink-muted">{message}</p>;
}
