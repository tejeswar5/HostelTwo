import { Outlet, useNavigate } from 'react-router-dom';
import { Navbar } from '../../components/Navbar';
import { useRenterAuth } from '../../auth/renterAuth';

const LINKS = [
  { to: '/renter/discover', label: 'Discover' },
  { to: '/renter/bookings', label: 'My bookings' },
  { to: '/renter/complaints', label: 'Complaints' },
  { to: '/renter/notifications', label: 'Notifications' },
  { to: '/renter/profile', label: 'Profile' },
];

export function RenterLayout() {
  const { session, logout } = useRenterAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-surface">
      <Navbar
        brandLabel="Renter"
        links={LINKS}
        userName={`${session?.fname ?? ''} ${session?.lname ?? ''}`.trim()}
        onLogout={() => {
          logout();
          navigate('/renter/login');
        }}
      />
      <Outlet />
    </div>
  );
}
