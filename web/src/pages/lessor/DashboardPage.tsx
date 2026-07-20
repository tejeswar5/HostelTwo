import { useQuery } from '@tanstack/react-query';
import { PieChart, Pie, Cell, Legend, ResponsiveContainer, Tooltip } from 'recharts';
import { lessorApi } from '../../api/lessorApi';
import { StatCard } from '../../components/StatCard';

const COLORS = { available: '#3dab65', occupied: '#d44a4a', maintenance: '#e6a532' };

export function DashboardPage() {
  const { data, isLoading } = useQuery({ queryKey: ['dashboard'], queryFn: lessorApi.getDashboard });

  if (isLoading || !data) return <p className="p-8 text-sm text-ink-muted">Loading dashboard...</p>;

  const occupancyData = [
    { name: 'Available', value: data.availableBeds, color: COLORS.available },
    { name: 'Occupied', value: data.occupiedBeds, color: COLORS.occupied },
    { name: 'Maintenance', value: data.maintenanceBeds, color: COLORS.maintenance },
  ];

  return (
    <div className="mx-auto max-w-6xl space-y-8 px-6 py-8">
      <h1 className="text-2xl font-bold text-brand">Occupancy &amp; revenue</h1>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard label="Total beds" value={String(data.totalBeds)} />
        <StatCard label="Occupancy" value={`${Math.round(data.occupancyRate * 100)}%`} />
        <StatCard label="Open complaints" value={String(data.openComplaints)} />
        <StatCard label="Pending requests" value={String(data.pendingBookingRequests)} />
        <StatCard label="Collected this month" value={`Rs.${data.monthlyRevenueCollected.toFixed(0)}`} />
        <StatCard label="Due (all invoices)" value={`Rs.${data.monthlyRevenueDue.toFixed(0)}`} />
      </div>

      <div className="rounded-lg border border-border bg-white p-6">
        <h2 className="mb-4 text-lg font-semibold">Bed status breakdown</h2>
        <ResponsiveContainer width="100%" height={280}>
          <PieChart>
            <Pie data={occupancyData} dataKey="value" nameKey="name" innerRadius={60} outerRadius={100} paddingAngle={2}>
              {occupancyData.map((entry) => (
                <Cell key={entry.name} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
