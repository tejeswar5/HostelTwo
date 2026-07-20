import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessorApi } from '../../api/lessorApi';
import { EmptyState } from '../../components/EmptyState';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';
import { useState } from 'react';
import type { ComplaintStatus } from '../../api/types';

const STATUSES: ComplaintStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'];

export function ComplaintsInboxPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const { data: complaints, isLoading } = useQuery({ queryKey: ['lessor-complaints'], queryFn: () => lessorApi.listComplaints() });

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) => lessorApi.updateComplaintStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['lessor-complaints'] }),
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Complaints</h1>
      <ErrorBanner message={error} />
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}
      {!isLoading && complaints?.length === 0 && <EmptyState message="No complaints raised yet." />}
      <div className="space-y-3">
        {complaints?.map((c) => (
          <div key={c.id} className="rounded-lg border border-border bg-white p-4">
            <div className="flex items-center justify-between">
              <p className="font-semibold">
                {c.category} &middot; {c.raisedByName}
              </p>
              <select
                className="rounded-md border border-border px-2 py-1 text-xs"
                value={c.status}
                onChange={(e) => updateStatus.mutate({ id: c.id, status: e.target.value })}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </div>
            <p className="mt-1 text-sm text-ink-muted">{c.description}</p>
            <p className="mt-1 text-xs text-ink-muted">Raised {new Date(c.createdAt).toLocaleString()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
