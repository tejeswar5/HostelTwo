import { useQuery, useQueryClient } from '@tanstack/react-query';
import type { NotificationResponse } from '../api/types';
import { EmptyState } from './EmptyState';

export function NotificationsView({
  list,
  markRead,
}: {
  list: () => Promise<NotificationResponse[]>;
  markRead: (id: number) => Promise<NotificationResponse>;
}) {
  const queryClient = useQueryClient();
  const { data: notifications, isLoading } = useQuery({ queryKey: ['notifications'], queryFn: list });

  const onRead = async (id: number) => {
    await markRead(id);
    queryClient.invalidateQueries({ queryKey: ['notifications'] });
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Notifications</h1>
      {isLoading && <p className="text-sm text-ink-muted">Loading...</p>}
      {!isLoading && notifications?.length === 0 && <EmptyState message="You're all caught up." />}
      <div className="space-y-2">
        {notifications?.map((n) => (
          <div key={n.id} className={`rounded-lg border p-4 ${n.readAt ? 'border-border bg-white' : 'border-brand bg-surface-muted'}`}>
            <div className="flex items-center justify-between">
              <p className="font-semibold">{n.title}</p>
              {!n.readAt && (
                <button onClick={() => onRead(n.id)} className="text-xs font-medium text-brand">
                  Mark read
                </button>
              )}
            </div>
            <p className="mt-1 text-sm text-ink-muted">{n.body}</p>
            <p className="mt-1 text-xs text-ink-muted">{new Date(n.createdAt).toLocaleString()}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
