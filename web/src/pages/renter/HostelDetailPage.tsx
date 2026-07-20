import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { renterApi } from '../../api/renterApi';
import { BedGrid } from '../../components/BedGrid';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';
import type { ConnectionResponse, RenterBedResponse } from '../../api/types';

export function HostelDetailPage() {
  const { hostelId } = useParams();
  const id = Number(hostelId);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [complaintOpen, setComplaintOpen] = useState(false);
  const [category, setCategory] = useState('Cleaning');
  const [description, setDescription] = useState('');
  const [connections, setConnections] = useState<ConnectionResponse[] | null>(null);
  const [contacts, setContacts] = useState('');

  const { data: hostel, isLoading } = useQuery({
    queryKey: ['hostel-detail', id],
    queryFn: () => renterApi.getHostelDetail(id),
  });

  const reserve = useMutation({
    mutationFn: (bed: RenterBedResponse) => {
      const room = hostel?.floors.flatMap((f) => f.rooms).find((r) => r.beds.some((b) => b.id === bed.id));
      return renterApi.createBooking({
        bedId: bed.id,
        bedNumber: bed.bedNumber,
        roomNumber: room?.roomNumber,
        hostelId: id,
        hostelName: hostel?.name ?? '',
        checkIn: new Date().toISOString().slice(0, 10),
      });
    },
    onSuccess: () => {
      setMessage('Booking requested! Track its status under "My bookings".');
      queryClient.invalidateQueries({ queryKey: ['hostel-detail', id] });
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const raiseComplaint = useMutation({
    mutationFn: () => renterApi.raiseComplaint({ hostelId: id, hostelName: hostel?.name ?? '', category, description }),
    onSuccess: () => {
      setMessage('Complaint raised. You can track its status under "Complaints".');
      setComplaintOpen(false);
      setDescription('');
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const findConnections = useMutation({
    mutationFn: () =>
      renterApi.findConnections(id, {
        phoneNumbers: contacts.split(',').map((c) => c.trim()).filter(Boolean),
        emails: [],
      }),
    onSuccess: (data) => setConnections(data),
    onError: (e) => setError(extractErrorMessage(e)),
  });

  if (isLoading) return <p className="p-8 text-sm text-ink-muted">Loading hostel...</p>;
  if (!hostel) return <p className="p-8 text-sm text-ink-muted">Hostel not found.</p>;

  const allBeds: RenterBedResponse[] = hostel.floors.flatMap((f) => f.rooms.flatMap((r) => r.beds));

  return (
    <div className="mx-auto max-w-6xl space-y-6 px-6 py-8">
      <button onClick={() => navigate(-1)} className="text-sm text-ink-muted">
        &larr; Back to search
      </button>
      <ErrorBanner message={error} />
      {message && <div className="rounded-md bg-emerald-100 px-4 py-3 text-sm text-emerald-900">{message}</div>}

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-brand">{hostel.name}</h1>
          <p className="text-sm text-ink-muted">
            {[hostel.area, hostel.city, hostel.state].filter(Boolean).join(', ')} &middot; {hostel.hasLift ? 'Lift available' : 'No lift'}
          </p>
          <p className="font-mono text-xs text-ink-muted">
            {hostel.contactPhone} {hostel.contactEmail && `· ${hostel.contactEmail}`}
          </p>
          {hostel.hostelAmenities.length > 0 && <p className="mt-1 text-xs text-ink-muted">Amenities: {hostel.hostelAmenities.join(', ')}</p>}
        </div>
        <button onClick={() => setComplaintOpen((v) => !v)} className="rounded-md border border-brand px-4 py-2 text-sm font-medium text-brand">
          Raise a complaint
        </button>
      </div>

      {complaintOpen && (
        <div className="space-y-3 rounded-lg border border-border bg-white p-4">
          <div className="flex gap-3">
            <select className="rounded-md border border-border px-3 py-1.5 text-sm" value={category} onChange={(e) => setCategory(e.target.value)}>
              <option>Cleaning</option>
              <option>Water issue</option>
              <option>Electrical</option>
              <option>Wi-Fi</option>
              <option>Other</option>
            </select>
            <input
              className="flex-1 rounded-md border border-border px-3 py-1.5 text-sm"
              placeholder="Describe the issue"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <button
              disabled={!description || raiseComplaint.isPending}
              onClick={() => raiseComplaint.mutate()}
              className="rounded-md bg-brand px-4 py-1.5 text-sm font-medium text-white disabled:opacity-50"
            >
              Submit
            </button>
          </div>
        </div>
      )}

      <section>
        <h2 className="mb-3 text-lg font-semibold">Availability</h2>
        <BedGrid
          beds={allBeds}
          renderMeta={(bed) => (bed.status === 'BOOKED' ? `Vacates ${bed.expectedVacateDate ?? 'soon'}` : bed.status === 'AVAILABLE' ? 'Ready to move in' : 'Not bookable right now')}
          renderActions={(bed) =>
            bed.status === 'AVAILABLE' ? (
              <button
                disabled={reserve.isPending}
                onClick={() => reserve.mutate(bed)}
                className="rounded-md bg-available px-3 py-1.5 text-xs font-semibold text-white disabled:opacity-50"
              >
                Reserve
              </button>
            ) : null
          }
        />
      </section>

      <section className="rounded-lg border border-border bg-white p-5">
        <h2 className="text-lg font-semibold">A few connections here?</h2>
        <p className="mt-1 text-xs text-ink-muted">Enter phone numbers you know (comma separated) to see who's already staying here.</p>
        <div className="mt-3 flex gap-3">
          <input
            className="flex-1 rounded-md border border-border px-3 py-1.5 text-sm"
            placeholder="e.g. 9876543210, 9123456789"
            value={contacts}
            onChange={(e) => setContacts(e.target.value)}
          />
          <button
            disabled={!contacts || findConnections.isPending}
            onClick={() => findConnections.mutate()}
            className="rounded-md bg-surface-muted px-4 py-1.5 text-sm font-medium text-brand disabled:opacity-50"
          >
            Check
          </button>
        </div>
        {connections && (
          <ul className="mt-3 space-y-1 text-sm">
            {connections.length === 0 && <li className="text-ink-muted">No connections found here yet.</li>}
            {connections.map((c) => (
              <li key={c.userId}>
                {c.fullName} &middot; Room {c.roomNumber}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
