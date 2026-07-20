import { NotificationsView } from '../../components/NotificationsView';
import { renterApi } from '../../api/renterApi';

export function NotificationsPage() {
  return <NotificationsView list={renterApi.listNotifications} markRead={renterApi.markNotificationRead} />;
}
