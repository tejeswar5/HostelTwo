import { createApiClient } from './client';
import type {
  AuthResponse,
  BedResponse,
  DashboardResponse,
  FloorResponse,
  HostelResponse,
  InvoiceResponse,
  LessorBookingResponse,
  LessorComplaintResponse,
  NotificationResponse,
  PaymentResponse,
  ProfileResponse,
  ReceiptResponse,
} from './types';

const BASE_URL = import.meta.env.VITE_LESSOR_API_URL ?? 'http://localhost:8081/api/lessor';
const NOTIFICATION_BASE_URL = import.meta.env.VITE_NOTIFICATION_API_URL ?? 'http://localhost:8083/api/notifications';

export const lessorSession = createApiClient(BASE_URL, 'hostelone_lessor_session');
const { client } = lessorSession;

export const lessorApi = {
  register: (body: { fname: string; lname: string; email: string; phoneNumber: string; password: string }) =>
    client.post<AuthResponse>('/auth/register', body).then((r) => r.data),
  login: (body: { email: string; password: string }) =>
    client.post<AuthResponse>('/auth/login', body).then((r) => r.data),

  getMyHostel: () => client.get<HostelResponse>('/hostel').then((r) => r.data),
  createHostel: (body: Partial<HostelResponse>) => client.post<HostelResponse>('/hostel', body).then((r) => r.data),
  updateHostel: (body: Partial<HostelResponse>) => client.put<HostelResponse>('/hostel', body).then((r) => r.data),

  listFloors: () => client.get<FloorResponse[]>('/hostel/floors').then((r) => r.data),
  addFloor: (floorNumber: number) => client.post<FloorResponse>('/hostel/floors', { floorNumber }).then((r) => r.data),
  deleteFloor: (floorId: number) => client.delete(`/hostel/floors/${floorId}`),

  addRoom: (body: { floorId: number; roomNumber: string; capacity: number; monthlyRent: number; sharingType: number; airConditioned: boolean }) =>
    client.post('/hostel/rooms', body).then((r) => r.data),
  deleteRoom: (roomId: number) => client.delete(`/hostel/rooms/${roomId}`),

  getBedBoard: () => client.get<BedResponse[]>('/hostel/beds').then((r) => r.data),
  addBed: (body: { roomId: number; bedNumber: string }) => client.post<BedResponse>('/hostel/beds', body).then((r) => r.data),
  deleteBed: (bedId: number) => client.delete(`/hostel/beds/${bedId}`),
  markMaintenance: (bedId: number, reason: string) =>
    client.patch<BedResponse>(`/hostel/beds/${bedId}/maintenance`, { reason }).then((r) => r.data),
  markAvailable: (bedId: number) => client.patch<BedResponse>(`/hostel/beds/${bedId}/available`).then((r) => r.data),
  setNextMonthRentPaid: (bedId: number, paid: boolean) =>
    client.patch<BedResponse>(`/hostel/beds/${bedId}/next-month-rent?paid=${paid}`).then((r) => r.data),

  assignHostelAmenity: (name: string, quantity: number) => client.post('/hostel/amenities', { name, quantity }).then((r) => r.data),
  assignFloorAmenity: (floorId: number, name: string, quantity: number) =>
    client.post(`/hostel/floors/${floorId}/amenities`, { name, quantity }).then((r) => r.data),
  assignRoomAmenity: (roomId: number, name: string, quantity: number) =>
    client.post(`/hostel/rooms/${roomId}/amenities`, { name, quantity }).then((r) => r.data),

  listBookings: (status?: string) =>
    client.get<LessorBookingResponse[]>('/bookings', { params: status ? { status } : {} }).then((r) => r.data),
  approveBooking: (id: number) => client.post<LessorBookingResponse>(`/bookings/${id}/approve`).then((r) => r.data),
  rejectBooking: (id: number) => client.post<LessorBookingResponse>(`/bookings/${id}/reject`).then((r) => r.data),

  listComplaints: (status?: string) =>
    client.get<LessorComplaintResponse[]>('/complaints', { params: status ? { status } : {} }).then((r) => r.data),
  updateComplaintStatus: (id: number, status: string) =>
    client.patch<LessorComplaintResponse>(`/complaints/${id}/status`, { status }).then((r) => r.data),

  generateInvoice: (body: { bedId: number; totalAmount: number; dueDate: string }) =>
    client.post<InvoiceResponse>('/invoices', body).then((r) => r.data),
  listInvoices: (bedId?: number) => client.get<InvoiceResponse[]>('/invoices', { params: bedId ? { bedId } : {} }).then((r) => r.data),
  recordPayment: (body: { invoiceId: number; amount: number; method: string; transactionId?: string; notes?: string }) =>
    client.post<PaymentResponse>('/payments', body).then((r) => r.data),
  listPayments: (invoiceId?: number) =>
    client.get<PaymentResponse[]>('/payments', { params: invoiceId ? { invoiceId } : {} }).then((r) => r.data),
  getReceipt: (paymentId: number) => client.get<ReceiptResponse>(`/payments/${paymentId}/receipt`).then((r) => r.data),

  getDashboard: () => client.get<DashboardResponse>('/dashboard').then((r) => r.data),

  // notification-service is a separate backend, but it validates the same JWT
  // lessor-service issues (shared app.jwt.secret), so this reuses the lessor
  // client's Authorization header / 401-refresh handling and only overrides the
  // request URL to point at notification-service's absolute address.
  listNotifications: () => client.get<NotificationResponse[]>(NOTIFICATION_BASE_URL).then((r) => r.data),
  markNotificationRead: (id: number) => client.patch<NotificationResponse>(`${NOTIFICATION_BASE_URL}/${id}/read`).then((r) => r.data),

  getProfile: () => client.get<ProfileResponse>('/profile').then((r) => r.data),
  updateProfile: (body: { fname: string; lname: string; email: string; phoneNumber: string; profilePictureUrl?: string }) =>
    client.put<ProfileResponse>('/profile', body).then((r) => r.data),
};
