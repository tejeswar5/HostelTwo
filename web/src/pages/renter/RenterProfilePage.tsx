import { ProfileView } from '../../components/ProfileView';
import { renterApi } from '../../api/renterApi';

export function RenterProfilePage() {
  return <ProfileView getProfile={renterApi.getProfile} updateProfile={renterApi.updateProfile} />;
}
