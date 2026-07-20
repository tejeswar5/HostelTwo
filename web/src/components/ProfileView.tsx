import { useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import type { ProfileResponse } from '../api/types';
import { ErrorBanner, extractErrorMessage } from './ErrorBanner';

export function ProfileView({
  getProfile,
  updateProfile,
}: {
  getProfile: () => Promise<ProfileResponse>;
  updateProfile: (body: { fname: string; lname: string; email: string; phoneNumber: string; profilePictureUrl?: string }) => Promise<ProfileResponse>;
}) {
  const { data: profile } = useQuery({ queryKey: ['profile'], queryFn: getProfile });
  const [form, setForm] = useState({ fname: '', lname: '', email: '', phoneNumber: '', profilePictureUrl: '' });
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (profile) {
      setForm({
        fname: profile.fname,
        lname: profile.lname,
        email: profile.email,
        phoneNumber: profile.phoneNumber,
        profilePictureUrl: profile.profilePictureUrl ?? '',
      });
    }
  }, [profile]);

  const mutation = useMutation({
    mutationFn: () => updateProfile(form),
    onSuccess: () => setSaved(true),
    onError: (e) => setError(extractErrorMessage(e)),
  });

  return (
    <div className="mx-auto max-w-lg space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Your profile</h1>
      <ErrorBanner message={error} />
      {saved && <div className="rounded-md bg-emerald-100 px-4 py-3 text-sm text-emerald-900">Profile updated.</div>}
      <form
        className="space-y-4 rounded-lg border border-border bg-white p-5"
        onSubmit={(e) => {
          e.preventDefault();
          setSaved(false);
          setError(null);
          mutation.mutate();
        }}
      >
        <div className="grid grid-cols-2 gap-3">
          <label className="text-sm">
            <span className="text-ink-muted">First name</span>
            <input className="mt-1 w-full rounded-md border border-border px-3 py-1.5" value={form.fname} onChange={(e) => setForm({ ...form, fname: e.target.value })} />
          </label>
          <label className="text-sm">
            <span className="text-ink-muted">Last name</span>
            <input className="mt-1 w-full rounded-md border border-border px-3 py-1.5" value={form.lname} onChange={(e) => setForm({ ...form, lname: e.target.value })} />
          </label>
        </div>
        <label className="block text-sm">
          <span className="text-ink-muted">Email</span>
          <input className="mt-1 w-full rounded-md border border-border px-3 py-1.5" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <label className="block text-sm">
          <span className="text-ink-muted">Phone number</span>
          <input className="mt-1 w-full rounded-md border border-border px-3 py-1.5" value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
        </label>
        <button disabled={mutation.isPending} className="w-full rounded-md bg-brand px-4 py-2.5 font-semibold text-white disabled:opacity-50" type="submit">
          Save changes
        </button>
      </form>
    </div>
  );
}
