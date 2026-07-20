export type Role = 'RENTER' | 'LESSOR';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  fname: string;
  lname: string;
  email: string;
  role: Role;
}

export interface ProfileResponse {
  id: number;
  fname: string;
  lname: string;
  email: string;
  phoneNumber: string;
  profilePictureUrl: string | null;
  role: Role;
}

export interface NotificationResponse {
  id: number;
  type: string;
  title: string;
  body: string;
  createdAt: string;
  readAt: string | null;
}

// ---- Lessor DTOs ----

export interface HostelResponse {
  id: number;
  name: string;
  contactPhone: string | null;
  contactEmail: string | null;
  hasLift: boolean;
  buildingNameOrNumber: string | null;
  street: string | null;
  area: string | null;
  city: string | null;
  state: string | null;
  pinCode: number | null;
}

export type BedStatus = 'AVAILABLE' | 'BOOKED' | 'MAINTENANCE';

export interface BedResponse {
  id: number;
  floorId: number;
  floorNumber: number;
  roomId: number;
  roomNumber: string;
  bedNumber: string;
  status: BedStatus;
  checkInDate: string | null;
  checkOutDate: string | null;
  nextMonthRentPaid: boolean;
  maintenanceReason: string | null;
  monthlyRent: number;
}

export interface RoomResponse {
  id: number;
  floorId: number;
  roomNumber: string;
  capacity: number;
  monthlyRent: number;
  sharingType: number;
  airConditioned: boolean;
  beds: BedResponse[];
}

export interface FloorResponse {
  id: number;
  floorNumber: number;
  rooms: RoomResponse[];
}

export type BookingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface LessorBookingResponse {
  id: number;
  bedId: number;
  bedNumber: string;
  roomNumber: string;
  renterId: number;
  renterName: string;
  renterPhone: string;
  status: BookingStatus;
  requestedCheckIn: string;
  requestedCheckOut: string | null;
  createdAt: string;
}

export type ComplaintStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'REJECTED';

export interface LessorComplaintResponse {
  id: number;
  hostelId: number;
  raisedByName: string;
  category: string;
  description: string;
  status: ComplaintStatus;
  createdAt: string;
  updatedAt: string;
}

export type PaymentMethod = 'UPI' | 'CASH' | 'BANK_TRANSFER' | 'OTHER';
export type InvoiceStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID';

export interface InvoiceResponse {
  id: number;
  bedId: number;
  bedNumber: string;
  totalAmount: number;
  partialPayment: number;
  dueAmount: number;
  status: InvoiceStatus;
  dueDate: string | null;
}

export interface PaymentResponse {
  id: number;
  invoiceId: number;
  amount: number;
  method: PaymentMethod;
  transactionId: string | null;
  notes: string | null;
  paymentDate: string;
}

export interface ReceiptResponse {
  paymentId: number;
  invoiceId: number;
  hostelName: string;
  roomNumber: string;
  bedNumber: string;
  renterName: string;
  amount: number;
  method: PaymentMethod;
  transactionId: string | null;
  paymentDate: string;
  remainingDue: number;
}

export interface DashboardResponse {
  hostelId: number;
  totalBeds: number;
  availableBeds: number;
  occupiedBeds: number;
  maintenanceBeds: number;
  occupancyRate: number;
  monthlyRevenueCollected: number;
  monthlyRevenueDue: number;
  openComplaints: number;
  pendingBookingRequests: number;
}

// ---- Renter DTOs ----

export interface DiscoverHostelResponse {
  id: number;
  name: string;
  contactPhone: string | null;
  contactEmail: string | null;
  hasLift: boolean;
  area: string | null;
  city: string | null;
  state: string | null;
  availableBeds: number;
  cheapestMonthlyRent: number | null;
}

export interface RenterBedResponse {
  id: number;
  bedNumber: string;
  status: BedStatus;
  expectedVacateDate: string | null;
}

export interface RenterRoomResponse {
  id: number;
  roomNumber: string;
  sharingType: number;
  airConditioned: boolean;
  monthlyRent: number;
  beds: RenterBedResponse[];
}

export interface RenterFloorResponse {
  id: number;
  floorNumber: number;
  amenities: string[];
  rooms: RenterRoomResponse[];
}

export interface HostelDetailResponse {
  id: number;
  name: string;
  contactPhone: string | null;
  contactEmail: string | null;
  hasLift: boolean;
  area: string | null;
  city: string | null;
  state: string | null;
  hostelAmenities: string[];
  floors: RenterFloorResponse[];
}

export interface RenterBookingResponse {
  id: number;
  bedId: number;
  bedNumber: string;
  hostelName: string;
  status: BookingStatus;
  requestedCheckIn: string;
  requestedCheckOut: string | null;
  createdAt: string;
}

export interface RenterComplaintResponse {
  id: number;
  hostelId: number;
  hostelName: string;
  category: string;
  description: string;
  status: ComplaintStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ConnectionResponse {
  userId: number;
  fullName: string;
  phoneNumber: string;
  roomNumber: string;
}

export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  details?: string[];
}
