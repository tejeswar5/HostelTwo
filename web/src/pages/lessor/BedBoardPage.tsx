import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessorApi } from '../../api/lessorApi';
import { BedGrid } from '../../components/BedGrid';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';
import type { BedResponse } from '../../api/types';

export function BedBoardPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [maintenanceTarget, setMaintenanceTarget] = useState<BedResponse | null>(null);
  const [reason, setReason] = useState('');

  const { data: beds, isLoading } = useQuery({ queryKey: ['bed-board'], queryFn: lessorApi.getBedBoard });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['bed-board'] });

  const markMaintenance = useMutation({
    mutationFn: () => lessorApi.markMaintenance(maintenanceTarget!.id, reason),
    onSuccess: () => {
      setMaintenanceTarget(null);
      setReason('');
      invalidate();
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const markAvailable = useMutation({
    mutationFn: (bedId: number) => lessorApi.markAvailable(bedId),
    onSuccess: invalidate,
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const toggleRentPaid = useMutation({
    mutationFn: ({ bedId, paid }: { bedId: number; paid: boolean }) => lessorApi.setNextMonthRentPaid(bedId, paid),
    onSuccess: invalidate,
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="mx-auto max-w-6xl space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Bed board</h1>
      <ErrorBanner message={error} />
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}

      {maintenanceTarget && (
        <div className="flex gap-3 rounded-lg border border-maintenance bg-white p-4">
          <span className="text-sm font-medium">Mark {maintenanceTarget.bedNumber} under maintenance:</span>
          <input className="flex-1 rounded-md border border-border px-3 py-1 text-sm" placeholder="Reason" value={reason} onChange={(e) => setReason(e.target.value)} />
          <button disabled={!reason || markMaintenance.isPending} onClick={() => markMaintenance.mutate()} className="rounded-md bg-maintenance px-3 py-1 text-sm font-medium text-white disabled:opacity-50">
            Confirm
          </button>
          <button onClick={() => setMaintenanceTarget(null)} className="text-sm text-ink-muted">
            Cancel
          </button>
        </div>
      )}

      {beds && (
        <BedGrid
          beds={beds}
          renderMeta={(bed) => (
            <div className="space-y-1">
              <div>Room {bed.roomNumber} &middot; Floor {bed.floorNumber} &middot; Rs.{bed.monthlyRent}/mo</div>
              {bed.status === 'BOOKED' && <div>Vacates {bed.checkOutDate ?? 'TBD'}</div>}
              {bed.status === 'BOOKED' && (
                <label className="flex items-center gap-1">
                  <input type="checkbox" checked={bed.nextMonthRentPaid} onChange={(e) => toggleRentPaid.mutate({ bedId: bed.id, paid: e.target.checked })} />
                  Next month rent paid
                </label>
              )}
              {bed.status === 'MAINTENANCE' && <div>Reason: {bed.maintenanceReason}</div>}
            </div>
          )}
          renderActions={(bed) => (
            <div className="flex gap-2">
              {bed.status !== 'MAINTENANCE' && bed.status !== 'BOOKED' && (
                <button onClick={() => setMaintenanceTarget(bed)} className="rounded-md bg-maintenance px-2.5 py-1 text-xs font-medium text-white">
                  Maintenance
                </button>
              )}
              {bed.status === 'MAINTENANCE' && (
                <button onClick={() => markAvailable.mutate(bed.id)} className="rounded-md bg-available px-2.5 py-1 text-xs font-medium text-white">
                  Mark available
                </button>
              )}
            </div>
          )}
        />
      )}
    </div>
  );
}
