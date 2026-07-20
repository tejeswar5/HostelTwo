import { NotificationsView } from '../../components/NotificationsView';
import { lessorApi } from '../../api/lessorApi';

export function LessorNotificationsPage() {
  return <NotificationsView list={lessorApi.listNotifications} markRead={lessorApi.markNotificationRead} />;
}
