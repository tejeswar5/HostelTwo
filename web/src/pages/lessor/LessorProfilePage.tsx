import { ProfileView } from '../../components/ProfileView';
import { lessorApi } from '../../api/lessorApi';

export function LessorProfilePage() {
  return <ProfileView getProfile={lessorApi.getProfile} updateProfile={lessorApi.updateProfile} />;
}
