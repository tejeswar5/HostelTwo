import { createApiClient } from './client';
import type {
  AuthResponse,
  ConnectionResponse,
  DiscoverHostelResponse,
  HostelDetailResponse,
  NotificationResponse,
  ProfileResponse,
  RenterBookingResponse,
  RenterComplaintResponse,
} from './types';

const BASE_URL = import.meta.env.VITE_RENTER_API_URL ?? 'http://localhost:8082/api/renter';
const NOTIFICATION_BASE_URL = import.meta.env.VITE_NOTIFICATION_API_URL ?? 'http://localhost:8083/api/notifications';

export const renterSession = createApiClient(BASE_URL, 'hostelone_renter_session');
const { client } = renterSession;

export const renterApi = {
  register: (body: { fname: string; lname: string; email: string; phoneNumber: string; password: string }) =>
    client.post<AuthResponse>('/auth/register', body).then((r) => r.data),
  login: (body: { email: string; password: string }) =>
    client.post<AuthResponse>('/auth/login', body).then((r) => r.data),

  discover: (params: { city?: string; sharingType?: number; airConditioned?: boolean }) =>
    client.get<DiscoverHostelResponse[]>('/hostels', { params }).then((r) => r.data),
  getHostelDetail: (hostelId: number) => client.get<HostelDetailResponse>(`/hostels/${hostelId}`).then((r) => r.data),

  // bedNumber/hostelId/hostelName are a snapshot from the discovery page the
  // renter is already looking at - renter-service no longer holds hostel/bed
  // data locally to look these up itself after the DB split.
  createBooking: (body: {
    bedId: number;
    bedNumber: string;
    roomNumber?: string;
    hostelId: number;
    hostelName: string;
    checkIn: string;
    checkOut?: string;
  }) => client.post<RenterBookingResponse>('/bookings', body).then((r) => r.data),
  myBookings: () => client.get<RenterBookingResponse[]>('/bookings/mine').then((r) => r.data),

  raiseComplaint: (body: { hostelId: number; hostelName: string; category: string; description: string }) =>
    client.post<RenterComplaintResponse>('/complaints', body).then((r) => r.data),
  myComplaints: () => client.get<RenterComplaintResponse[]>('/complaints/mine').then((r) => r.data),

  // notification-service is a separate backend; see lessorApi.ts for why this
  // reuses the renter client but targets an absolute URL.
  listNotifications: () => client.get<NotificationResponse[]>(NOTIFICATION_BASE_URL).then((r) => r.data),
  markNotificationRead: (id: number) => client.patch<NotificationResponse>(`${NOTIFICATION_BASE_URL}/${id}/read`).then((r) => r.data),

  getProfile: () => client.get<ProfileResponse>('/profile').then((r) => r.data),
  updateProfile: (body: { fname: string; lname: string; email: string; phoneNumber: string; profilePictureUrl?: string }) =>
    client.put<ProfileResponse>('/profile', body).then((r) => r.data),

  findConnections: (hostelId: number, body: { phoneNumbers: string[]; emails: string[] }) =>
    client.post<ConnectionResponse[]>(`/hostels/${hostelId}/connections`, body).then((r) => r.data),
};
