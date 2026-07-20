import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { lessorApi } from '../../api/lessorApi';
import { ErrorBanner, extractErrorMessage } from '../../components/ErrorBanner';

export function HostelSetupPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const { data: hostel, isLoading, isError } = useQuery({ queryKey: ['my-hostel'], queryFn: lessorApi.getMyHostel, retry: false });
  const { data: floors } = useQuery({ queryKey: ['floors'], queryFn: lessorApi.listFloors, enabled: !!hostel });

  const [hostelForm, setHostelForm] = useState({
    name: '',
    contactPhone: '',
    contactEmail: '',
    hasLift: false,
    city: '',
    area: '',
    state: '',
  });

  const saveHostel = useMutation({
    mutationFn: () => (hostel ? lessorApi.updateHostel(hostelForm) : lessorApi.createHostel(hostelForm)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-hostel'] }),
    onError: (e) => setError(extractErrorMessage(e)),
  });

  const invalidateFloors = () => queryClient.invalidateQueries({ queryKey: ['floors'] });

  const [newFloorNumber, setNewFloorNumber] = useState('');
  const addFloor = useMutation({
    mutationFn: () => lessorApi.addFloor(Number(newFloorNumber)),
    onSuccess: () => {
      setNewFloorNumber('');
      invalidateFloors();
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  if (isLoading) return <p className="p-8 text-sm text-ink-muted">Loading...</p>;

  if (!hostel || isError) {
    return (
      <div className="mx-auto max-w-lg space-y-6 px-6 py-8">
        <h1 className="text-2xl font-bold text-brand">Set up your hostel</h1>
        <ErrorBanner message={error} />
        <HostelForm form={hostelForm} setForm={setHostelForm} onSubmit={() => saveHostel.mutate()} pending={saveHostel.isPending} submitLabel="Create hostel" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-8 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">{hostel.name}</h1>
      <ErrorBanner message={error} />

      <details className="rounded-lg border border-border bg-white p-5">
        <summary className="cursor-pointer text-sm font-medium text-brand">Edit hostel details</summary>
        <div className="mt-4">
          <HostelForm
            form={{ ...hostelForm, name: hostelForm.name || hostel.name, contactPhone: hostelForm.contactPhone || (hostel.contactPhone ?? '') }}
            setForm={setHostelForm}
            onSubmit={() => saveHostel.mutate()}
            pending={saveHostel.isPending}
            submitLabel="Save changes"
          />
        </div>
      </details>

      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Floors &amp; rooms</h2>
          <div className="flex gap-2">
            <input
              className="w-24 rounded-md border border-border px-3 py-1.5 text-sm"
              placeholder="Floor #"
              value={newFloorNumber}
              onChange={(e) => setNewFloorNumber(e.target.value)}
            />
            <button
              disabled={!newFloorNumber || addFloor.isPending}
              onClick={() => addFloor.mutate()}
              className="rounded-md bg-brand px-4 py-1.5 text-sm font-medium text-white disabled:opacity-50"
            >
              Add floor
            </button>
          </div>
        </div>

        {floors?.map((floor) => (
          <FloorCard key={floor.id} floor={floor} onChanged={invalidateFloors} setError={setError} />
        ))}
      </section>
    </div>
  );
}

function HostelForm({
  form,
  setForm,
  onSubmit,
  pending,
  submitLabel,
}: {
  form: { name: string; contactPhone: string; contactEmail: string; hasLift: boolean; city: string; area: string; state: string };
  setForm: (f: typeof form) => void;
  onSubmit: () => void;
  pending: boolean;
  submitLabel: string;
}) {
  return (
    <form
      className="space-y-3"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit();
      }}
    >
      <input className="w-full rounded-md border border-border px-3 py-2 text-sm" placeholder="Hostel name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
      <div className="grid grid-cols-2 gap-3">
        <input className="rounded-md border border-border px-3 py-2 text-sm" placeholder="Contact phone" value={form.contactPhone} onChange={(e) => setForm({ ...form, contactPhone: e.target.value })} />
        <input className="rounded-md border border-border px-3 py-2 text-sm" placeholder="Contact email" value={form.contactEmail} onChange={(e) => setForm({ ...form, contactEmail: e.target.value })} />
      </div>
      <div className="grid grid-cols-3 gap-3">
        <input className="rounded-md border border-border px-3 py-2 text-sm" placeholder="City" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
        <input className="rounded-md border border-border px-3 py-2 text-sm" placeholder="Area" value={form.area} onChange={(e) => setForm({ ...form, area: e.target.value })} />
        <input className="rounded-md border border-border px-3 py-2 text-sm" placeholder="State" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} />
      </div>
      <label className="flex items-center gap-2 text-sm">
        <input type="checkbox" checked={form.hasLift} onChange={(e) => setForm({ ...form, hasLift: e.target.checked })} />
        Lift available
      </label>
      <button disabled={pending} className="rounded-md bg-brand px-4 py-2 text-sm font-semibold text-white disabled:opacity-50" type="submit">
        {submitLabel}
      </button>
    </form>
  );
}

function FloorCard({
  floor,
  onChanged,
  setError,
}: {
  floor: import('../../api/types').FloorResponse;
  onChanged: () => void;
  setError: (m: string) => void;
}) {
  const [roomForm, setRoomForm] = useState({ roomNumber: '', capacity: 2, monthlyRent: 6000, sharingType: 2, airConditioned: false });

  const addRoom = useMutation({
    mutationFn: () => lessorApi.addRoom({ floorId: floor.id, ...roomForm }),
    onSuccess: () => {
      setRoomForm({ ...roomForm, roomNumber: '' });
      onChanged();
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="rounded-lg border border-border bg-white p-5">
      <h3 className="font-semibold">Floor {floor.floorNumber}</h3>
      <div className="mt-3 space-y-3">
        {floor.rooms.map((room) => (
          <RoomRow key={room.id} room={room} onChanged={onChanged} setError={setError} />
        ))}
      </div>
      <div className="mt-4 flex flex-wrap gap-2 border-t border-border pt-4">
        <input className="w-20 rounded-md border border-border px-2 py-1 text-sm" placeholder="Room #" value={roomForm.roomNumber} onChange={(e) => setRoomForm({ ...roomForm, roomNumber: e.target.value })} />
        <input
          type="number"
          className="w-24 rounded-md border border-border px-2 py-1 text-sm"
          placeholder="Sharing"
          value={roomForm.sharingType}
          onChange={(e) => setRoomForm({ ...roomForm, sharingType: Number(e.target.value), capacity: Number(e.target.value) })}
        />
        <input
          type="number"
          className="w-28 rounded-md border border-border px-2 py-1 text-sm"
          placeholder="Rent/mo"
          value={roomForm.monthlyRent}
          onChange={(e) => setRoomForm({ ...roomForm, monthlyRent: Number(e.target.value) })}
        />
        <label className="flex items-center gap-1 text-xs">
          <input type="checkbox" checked={roomForm.airConditioned} onChange={(e) => setRoomForm({ ...roomForm, airConditioned: e.target.checked })} />
          A/C
        </label>
        <button
          disabled={!roomForm.roomNumber || addRoom.isPending}
          onClick={() => addRoom.mutate()}
          className="rounded-md bg-surface-muted px-3 py-1 text-xs font-medium text-brand disabled:opacity-50"
        >
          Add room
        </button>
      </div>
    </div>
  );
}

function RoomRow({
  room,
  onChanged,
  setError,
}: {
  room: import('../../api/types').RoomResponse;
  onChanged: () => void;
  setError: (m: string) => void;
}) {
  const [bedNumber, setBedNumber] = useState('');
  const addBed = useMutation({
    mutationFn: () => lessorApi.addBed({ roomId: room.id, bedNumber }),
    onSuccess: () => {
      setBedNumber('');
      onChanged();
    },
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="rounded-md bg-surface-muted/50 p-3">
      <div className="flex flex-wrap items-center justify-between gap-2 text-sm">
        <span className="font-medium">
          Room {room.roomNumber} &middot; {room.sharingType}-sharing {room.airConditioned && '· A/C'} &middot; Rs.{room.monthlyRent}/mo
        </span>
        <div className="flex gap-2">
          {room.beds.map((bed) => (
            <span key={bed.id} className="rounded bg-white px-2 py-0.5 font-mono text-xs">
              {bed.bedNumber}: {bed.status}
            </span>
          ))}
        </div>
      </div>
      <div className="mt-2 flex gap-2">
        <input className="w-24 rounded-md border border-border px-2 py-1 text-xs" placeholder="Bed #" value={bedNumber} onChange={(e) => setBedNumber(e.target.value)} />
        <button
          disabled={!bedNumber || addBed.isPending}
          onClick={() => addBed.mutate()}
          className="rounded-md bg-white px-3 py-1 text-xs font-medium text-brand"
        >
          Add bed
        </button>
      </div>
    </div>
  );
}
