import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { renterApi } from '../../api/renterApi';
import { EmptyState } from '../../components/EmptyState';

export function DiscoverPage() {
  const [city, setCity] = useState('');
  const [sharingType, setSharingType] = useState('');
  const [airConditioned, setAirConditioned] = useState(false);

  const { data: hostels, isLoading } = useQuery({
    queryKey: ['discover', city, sharingType, airConditioned],
    queryFn: () =>
      renterApi.discover({
        city: city || undefined,
        sharingType: sharingType ? Number(sharingType) : undefined,
        airConditioned: airConditioned || undefined,
      }),
  });

  return (
    <div className="mx-auto max-w-6xl space-y-6 px-6 py-8">
      <div>
        <h1 className="text-2xl font-bold text-brand">Nearby hostels</h1>
        <p className="text-sm text-ink-muted">Compare rent, sharing type, A/C and amenities before you reserve a bed.</p>
      </div>

      <div className="flex flex-wrap items-end gap-4 rounded-lg border border-border bg-white p-4">
        <label className="text-sm">
          <span className="block text-ink-muted">City</span>
          <input className="mt-1 rounded-md border border-border px-3 py-1.5" value={city} onChange={(e) => setCity(e.target.value)} placeholder="e.g. Hyderabad" />
        </label>
        <label className="text-sm">
          <span className="block text-ink-muted">Sharing</span>
          <select className="mt-1 rounded-md border border-border px-3 py-1.5" value={sharingType} onChange={(e) => setSharingType(e.target.value)}>
            <option value="">Any</option>
            <option value="1">Single</option>
            <option value="2">2 sharing</option>
            <option value="3">3 sharing</option>
            <option value="4">4 sharing</option>
          </select>
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={airConditioned} onChange={(e) => setAirConditioned(e.target.checked)} />
          A/C only
        </label>
      </div>

      {isLoading && <p className="text-sm text-ink-muted">Loading hostels...</p>}
      {!isLoading && hostels?.length === 0 && <EmptyState message="No hostels match those filters yet." />}

      <div className="grid grid-cols-[repeat(auto-fill,minmax(280px,1fr))] gap-4">
        {hostels?.map((hostel) => (
          <Link
            key={hostel.id}
            to={`/renter/hostels/${hostel.id}`}
            className="flex flex-col gap-2 rounded-lg border border-border bg-white p-5 hover:border-brand"
          >
            <h2 className="text-lg font-semibold">{hostel.name}</h2>
            <p className="text-xs text-ink-muted">
              {[hostel.area, hostel.city, hostel.state].filter(Boolean).join(', ')} &middot; {hostel.hasLift ? 'Lift available' : 'No lift'}
            </p>
            <p className="font-mono text-xs text-ink-muted">
              {hostel.contactPhone} {hostel.contactEmail && `· ${hostel.contactEmail}`}
            </p>
            <div className="mt-2 flex items-center justify-between text-sm">
              <span className="font-medium text-available">{hostel.availableBeds} beds available</span>
              {hostel.cheapestMonthlyRent != null && <span className="font-semibold text-brand">from Rs.{hostel.cheapestMonthlyRent}/mo</span>}
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
