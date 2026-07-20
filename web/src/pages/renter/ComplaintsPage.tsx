import { useQuery } from '@tanstack/react-query';
import { renterApi } from '../../api/renterApi';
import { EmptyState } from '../../components/EmptyState';

export function ComplaintsPage() {
  const { data: complaints, isLoading } = useQuery({ queryKey: ['my-complaints'], queryFn: renterApi.myComplaints });

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-8">
      <div>
        <h1 className="text-2xl font-bold text-brand">Complaints</h1>
        <p className="text-sm text-ink-muted">Raise a complaint from a hostel's page; track status here.</p>
      </div>
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}
      {!isLoading && complaints?.length === 0 && <EmptyState message="No complaints raised yet." />}
      <div className="space-y-3">
        {complaints?.map((c) => (
          <div key={c.id} className="rounded-lg border border-border bg-white p-4">
            <div className="flex items-center justify-between">
              <p className="font-semibold">
                {c.category} &middot; {c.hostelName}
              </p>
              <span className="text-xs font-semibold text-brand">{c.status.replace('_', ' ')}</span>
            </div>
            <p className="mt-1 text-sm text-ink-muted">{c.description}</p>
            <p className="mt-1 text-xs text-ink-muted">Raised {new Date(c.createdAt).toLocaleString()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
