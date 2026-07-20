import { Outlet, useNavigate } from 'react-router-dom';
import { Navbar } from '../../components/Navbar';
import { useLessorAuth } from '../../auth/lessorAuth';

const LINKS = [
  { to: '/lessor/dashboard', label: 'Dashboard' },
  { to: '/lessor/hostel', label: 'Hostel setup' },
  { to: '/lessor/beds', label: 'Bed board' },
  { to: '/lessor/bookings', label: 'Booking requests' },
  { to: '/lessor/complaints', label: 'Complaints' },
  { to: '/lessor/payments', label: 'Payments' },
  { to: '/lessor/notifications', label: 'Notifications' },
  { to: '/lessor/profile', label: 'Profile' },
];

export function LessorLayout() {
  const { session, logout } = useLessorAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-surface">
      <Navbar
        brandLabel="Lessor"
        links={LINKS}
        userName={`${session?.fname ?? ''} ${session?.lname ?? ''}`.trim()}
        onLogout={() => {
          logout();
          navigate('/lessor/login');
        }}
      />
      <Outlet />
    </div>
  );
}
