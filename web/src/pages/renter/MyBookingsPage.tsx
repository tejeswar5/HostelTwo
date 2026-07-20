import { useQuery } from '@tanstack/react-query';
import { renterApi } from '../../api/renterApi';
import { EmptyState } from '../../components/EmptyState';
import type { BookingStatus } from '../../api/types';

const STATUS_COLOR: Record<BookingStatus, string> = {
  PENDING: 'text-maintenance',
  APPROVED: 'text-available',
  REJECTED: 'text-booked',
  CANCELLED: 'text-ink-muted',
};

export function MyBookingsPage() {
  const { data: bookings, isLoading } = useQuery({ queryKey: ['my-bookings'], queryFn: renterApi.myBookings });

  return (
    <div className="mx-auto max-w-4xl space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">My bookings</h1>
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}
      {!isLoading && bookings?.length === 0 && <EmptyState message="You haven't requested a bed yet." />}
      <div className="space-y-3">
        {bookings?.map((b) => (
          <div key={b.id} className="flex items-center justify-between rounded-lg border border-border bg-white p-4">
            <div>
              <p className="font-semibold">{b.hostelName}</p>
              <p className="text-xs text-ink-muted">
                Bed {b.bedNumber} &middot; requested {new Date(b.requestedCheckIn).toLocaleDateString()}
              </p>
            </div>
            <span className={`text-sm font-semibold ${STATUS_COLOR[b.status]}`}>{b.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
