import { Navigate, Route, Routes } from 'react-router-dom';
import { LandingPage } from './pages/LandingPage';
import { RenterLoginPage } from './pages/auth/RenterLoginPage';
import { RenterRegisterPage } from './pages/auth/RenterRegisterPage';
import { LessorLoginPage } from './pages/auth/LessorLoginPage';
import { LessorRegisterPage } from './pages/auth/LessorRegisterPage';
import { RenterLayout } from './pages/renter/RenterLayout';
import { DiscoverPage } from './pages/renter/DiscoverPage';
import { HostelDetailPage } from './pages/renter/HostelDetailPage';
import { MyBookingsPage } from './pages/renter/MyBookingsPage';
import { ComplaintsPage } from './pages/renter/ComplaintsPage';
import { NotificationsPage } from './pages/renter/NotificationsPage';
import { RenterProfilePage } from './pages/renter/RenterProfilePage';
import { LessorLayout } from './pages/lessor/LessorLayout';
import { DashboardPage } from './pages/lessor/DashboardPage';
import { HostelSetupPage } from './pages/lessor/HostelSetupPage';
import { BedBoardPage } from './pages/lessor/BedBoardPage';
import { BookingRequestsPage } from './pages/lessor/BookingRequestsPage';
import { ComplaintsInboxPage } from './pages/lessor/ComplaintsInboxPage';
import { PaymentsPage } from './pages/lessor/PaymentsPage';
import { LessorNotificationsPage } from './pages/lessor/LessorNotificationsPage';
import { LessorProfilePage } from './pages/lessor/LessorProfilePage';
import { useRenterAuth } from './auth/renterAuth';
import { useLessorAuth } from './auth/lessorAuth';
import { ProtectedRoute } from './auth/ProtectedRoute';

export function App() {
  const renterAuth = useRenterAuth();
  const lessorAuth = useLessorAuth();

  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />

      <Route path="/renter" element={<Navigate to={renterAuth.session ? '/renter/discover' : '/renter/login'} replace />} />
      <Route path="/renter/login" element={<RenterLoginPage />} />
      <Route path="/renter/register" element={<RenterRegisterPage />} />
      <Route element={<ProtectedRoute session={renterAuth.session} redirectTo="/renter/login" />}>
        <Route element={<RenterLayout />}>
          <Route path="/renter/discover" element={<DiscoverPage />} />
          <Route path="/renter/hostels/:hostelId" element={<HostelDetailPage />} />
          <Route path="/renter/bookings" element={<MyBookingsPage />} />
          <Route path="/renter/complaints" element={<ComplaintsPage />} />
          <Route path="/renter/notifications" element={<NotificationsPage />} />
          <Route path="/renter/profile" element={<RenterProfilePage />} />
        </Route>
      </Route>

      <Route path="/lessor" element={<Navigate to={lessorAuth.session ? '/lessor/dashboard' : '/lessor/login'} replace />} />
      <Route path="/lessor/login" element={<LessorLoginPage />} />
      <Route path="/lessor/register" element={<LessorRegisterPage />} />
      <Route element={<ProtectedRoute session={lessorAuth.session} redirectTo="/lessor/login" />}>
        <Route element={<LessorLayout />}>
          <Route path="/lessor/dashboard" element={<DashboardPage />} />
          <Route path="/lessor/hostel" element={<HostelSetupPage />} />
          <Route path="/lessor/beds" element={<BedBoardPage />} />
          <Route path="/lessor/bookings" element={<BookingRequestsPage />} />
          <Route path="/lessor/complaints" element={<ComplaintsInboxPage />} />
          <Route path="/lessor/payments" element={<PaymentsPage />} />
          <Route path="/lessor/notifications" element={<LessorNotificationsPage />} />
          <Route path="/lessor/profile" element={<LessorProfilePage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
