import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessorApi } from '../../api/lessorApi';
import { EmptyState } from '../../components/EmptyState';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';
import { useState } from 'react';

export function BookingRequestsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const { data: bookings, isLoading } = useQuery({ queryKey: ['bookings', 'PENDING'], queryFn: () => lessorApi.listBookings('PENDING') });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['bookings'] });
    queryClient.invalidateQueries({ queryKey: ['bed-board'] });
    queryClient.invalidateQueries({ queryKey: ['dashboard'] });
  };

  const approve = useMutation({
    mutationFn: (id: number) => lessorApi.approveBooking(id),
    onSuccess: invalidate,
    onError: (e) => setError(extractErrorMessage(e)),
  });
  const reject = useMutation({
    mutationFn: (id: number) => lessorApi.rejectBooking(id),
    onSuccess: invalidate,
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Booking requests</h1>
      <ErrorBanner message={error} />
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}
      {!isLoading && bookings?.length === 0 && <EmptyState message="No pending booking requests." />}
      <div className="space-y-3">
        {bookings?.map((b) => (
          <div key={b.id} className="flex items-center justify-between rounded-lg border border-border bg-white p-4">
            <div>
              <p className="font-semibold">
                {b.renterName} &middot; Bed {b.bedNumber} (Room {b.roomNumber})
              </p>
              <p className="text-xs text-ink-muted">
                {b.renterPhone} &middot; requested {new Date(b.requestedCheckIn).toLocaleDateString()}
              </p>
            </div>
            <div className="flex gap-2">
              <button onClick={() => approve.mutate(b.id)} className="rounded-md bg-available px-3 py-1.5 text-sm font-medium text-white">
                Approve
              </button>
              <button onClick={() => reject.mutate(b.id)} className="rounded-md bg-booked px-3 py-1.5 text-sm font-medium text-white">
                Reject
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
