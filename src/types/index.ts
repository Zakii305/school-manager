export type UserRole = 'owner' | 'admin' | 'teacher' | 'student' | 'parent';

export interface User {
  uid: string;
  name: string;
  email: string;
  role: UserRole;
  phone?: string;
  classId?: string;
  className?: string;
  rollNo?: string;
  avatarUrl?: string;
  status: 'approved' | 'pending' | 'rejected';
  schoolId: string;
  createdAt: number;
}

export interface DemoCredentials {
  role: UserRole;
  label: string;
  loginId: string;
  passcode: string;
  notes: string;
}

export interface AcademicSession {
  id: string;
  label: string; // e.g. "2026-2027"
  startDate: string;
  endDate: string;
  isCurrent: boolean;
  termCount: number;
}

export interface School {
  id: string;
  code: string; // Unique school code e.g. "SAQ-01"
  name: string;
  tagline: string;
  email: string;
  phone: string;
  address: string;
  currency: string;
  currentSession: string;
  status: 'active' | 'suspended' | 'cancelled';
  cancellationReason?: string;
  cancelledAt?: number;
  plan: 'trial' | 'standard' | 'enterprise';
  studentCount: number;
  teacherCount: number;
  createdAt: number;
  expiresAt: number;
  logoUrl?: string;
  stampUrl?: string;
}

export interface SchoolChangeRequest {
  id: string;
  schoolId: string;
  schoolName: string;
  adminEmail: string;
  requestedChanges: {
    name?: string;
    tagline?: string;
    phone?: string;
    address?: string;
    reason: string;
  };
  status: 'pending' | 'approved' | 'rejected';
  ownerNote?: string;
  createdAt: number;
}

export interface Student {
  id: string;
  schoolId: string;
  admissionNo: string;
  rollNo: string;
  sessionLabel: string;
  classId: string;
  className: string;
  name: string;
  nameUrdu?: string;
  cnicBform?: string;
  religion?: string;
  bloodGroup?: string;
  dob?: string;
  admissionDate: string;
  previousSchool?: string;
  nationality: string;
  gender: 'male' | 'female' | 'other';
  hafizEQuran: 'Yes' | 'No';
  address?: string;
  phone?: string;
  email?: string;
  fatherName: string;
  motherName?: string;
  fatherContact?: string;
  motherContact?: string;
  fatherOccupation?: string;
  motherOccupation?: string;
  familyId?: string;
  monthlyFee: number;
  vanFee: number;
  admissionFee: number;
  examFee: number;
  remarks?: string;
  avatar?: string;
  status: 'active' | 'left' | 'suspended' | 'new';
  createdAt: string;
}

export interface Family {
  id: string;
  schoolId: string;
  familyCode: string; // e.g. "FAM-101"
  name: string;
  fatherName: string;
  fatherCnic: string;
  phone: string;
  address: string;
  siblingDiscountPercent: number; // e.g. 10%
  notes?: string;
  childrenIds: string[];
}

export interface Staff {
  id: string;
  schoolId: string;
  staffId: string;
  name: string;
  designation: string;
  department: string;
  phone: string;
  email: string;
  cnic: string;
  bankAccountNo?: string;
  bankName?: string;
  monthlySalary: number;
  hireDate?: string;
  status: 'active' | 'inactive' | 'on_leave' | 'resigned';
  avatar?: string;
}

export interface ClassItem {
  id: string;
  schoolId: string;
  name: string;
  section: string;
  sections: string[];
  studentsCount: number;
  classTeacherId?: string;
  classTeacherName?: string;
}

export interface SubjectItem {
  id: string;
  schoolId: string;
  name: string;
  code?: string;
  classId: string;
  className: string;
  teacherId?: string;
  teacherName?: string;
}

export interface TimetableSlot {
  id: string;
  schoolId: string;
  classId: string;
  day: string; // "Monday", "Tuesday", etc.
  period: number; // 1..8
  subject: string;
  teacher: string;
  startTime?: string;
  endTime?: string;
}

export type AttendanceStatus = 'present' | 'absent' | 'late' | 'leave';

export interface StudentAttendance {
  id: string;
  schoolId: string;
  studentId: string;
  studentName: string;
  classId: string;
  date: string; // YYYY-MM-DD
  status: AttendanceStatus;
}

export interface StaffAttendance {
  id: string;
  schoolId: string;
  staffId: string;
  staffName: string;
  date: string;
  status: AttendanceStatus;
}

export interface Exam {
  id: string;
  schoolId: string;
  sessionId: string;
  name: string;
  term: string;
  startDate: string;
  endDate: string;
  maxMarks: number;
  classId?: string;
  className?: string;
  passingMarks: number;
  status: 'upcoming' | 'ongoing' | 'completed';
}

export interface GradeBand {
  id: string;
  schoolId: string;
  grade: string;
  minPct: number;
  maxPct: number;
  remarks: string;
}

export interface GradeEntry {
  id: string;
  schoolId: string;
  examId: string;
  examName: string;
  subjectId: string;
  subject: string;
  studentId: string;
  studentName: string;
  classId: string;
  marks: number;
  total: number;
}

export interface QuestionPaper {
  id: string;
  schoolId: string;
  title: string;
  subject: string;
  className: string;
  examTerm: string;
  type: string; // e.g. "Objective & Subjective", "Theoretical", "Practical"
  date: string;
  durationMinutes: number;
  maxMarks: number;
  passingMarks: number;
  instructions: string;
  topicsCovered?: string;
}

export interface SyllabusItem {
  id: string;
  schoolId: string;
  title: string;
  className: string;
  subject: string;
  term: string;
  chapters: string;
}

export interface Invoice {
  id: string;
  schoolId: string;
  voucherNo: string;
  studentId: string;
  studentName: string;
  classId: string;
  className: string;
  month: string;
  tuitionFee: number;
  vanFee: number;
  admissionFee: number;
  examFee: number;
  arrears: number;
  discount: number;
  total: number;
  paidAmount: number;
  dueDate: string;
  status: 'paid' | 'pending' | 'overdue' | 'partial';
  createdAt: string;
  items: { label: string; amount: number }[];
}

export interface PaymentReceipt {
  id: string;
  schoolId: string;
  receiptNo: string;
  voucherNo: string;
  studentId: string;
  studentName: string;
  className: string;
  amount: number;
  method: string; // "Habib Bank Deposit" | "JazzCash" | "EasyPaisa" | "Cash"
  date: string;
  ref: string; // Transaction reference or bank deposit slip number
  cashierName: string;
  status: 'Approved' | 'Pending' | 'Rejected';
  notes?: string;
}

export interface PayrollItem {
  id: string;
  schoolId: string;
  staffId: string;
  staffName: string;
  designation: string;
  department: string;
  month: string;
  bankAccountNo: string;
  gross: number;
  medicalAllowance: number;
  houseRentAllowance: number;
  taxDeduction: number;
  absenceDeduction: number;
  net: number;
  paymentMode: 'Direct Bank Transfer' | 'Cash' | 'Cheque';
  status: 'Paid' | 'Pending';
  disbursedAt?: string;
}

export interface Expense {
  id: string;
  schoolId: string;
  voucherNo: string;
  title: string;
  category: 'Utilities & WiFi' | 'Stationery & Printing' | 'Fuel & Generator' | 'Lab Equipment' | 'Campus Maintenance' | 'Sports & Events' | 'Hospitality';
  amount: number;
  date: string;
  paidTo: string;
  paymentMode: 'Cash' | 'Online Bank' | 'Cheque';
  invoiceRef: string;
  approvedBy: string;
  notes: string;
}

export interface LibraryBook {
  id: string;
  schoolId: string;
  title: string;
  author: string;
  category: string;
  isbn?: string;
  copies: number;
  available: number;
  shelfNo?: string;
}

export interface TransportRoute {
  id: string;
  schoolId: string;
  route: string;
  vehicleNo: string;
  driver: string;
  driverPhone: string;
  capacity: number;
  monthlyFee: number;
}

export interface Notice {
  id: string;
  schoolId: string;
  title: string;
  body: string;
  createdAt: number;
  authorName: string;
  audience: string;
}

export interface Assignment {
  id: string;
  schoolId: string;
  title: string;
  description: string;
  subject: string;
  classId: string;
  className: string;
  dueDate: number;
  teacherName: string;
  submissionCount: number;
}

export interface LeaveRequest {
  id: string;
  schoolId: string;
  teacherId: string;
  teacherName: string;
  fromDate: string;
  toDate: string;
  reason: string;
  status: 'pending' | 'approved' | 'rejected';
  responseNote?: string;
  respondedBy?: string;
  respondedAt?: number;
  createdAt: number;
}

export interface SchoolAsset {
  id: string;
  schoolId: string;
  name: string;
  category: string;
  quantity: number;
  unitValue: number;
  totalValue: number;
  location: string;
  condition: 'Good' | 'Needs Repair' | 'New' | 'Damaged';
  purchaseDate: string;
}

export interface ParentAccount {
  id: string;
  schoolId: string;
  name: string;
  email: string;
  phone: string;
  childrenIds: string[];
  status: 'active' | 'suspended';
  lastLogin?: string;
}

export interface RolePermission {
  id: string;
  name: string;
  description: string;
  userCount: number;
  permissions: {
    canManageStudents: boolean;
    canManageFees: boolean;
    canManageExams: boolean;
    canManageTimetable: boolean;
    canManagePayroll: boolean;
    canManageSettings: boolean;
  };
}

export interface SmtpConfig {
  host: string;
  port: string;
  user: string;
  pass: string;
  senderName: string;
  senderEmail: string;
  encryption: 'TLS' | 'SSL' | 'None';
  whatsappApiToken: string;
  whatsappPhoneId: string;
  autoSendFeeSms: boolean;
  autoSendAttendanceSms: boolean;
  autoSendExamNotification: boolean;
}

export interface PeriodTiming {
  num: number;
  time: string;
}

export interface SchoolBranding {
  name: string;
  tagline: string;
  phone: string;
  email: string;
  address: string;
  currency: string;
  session: string;
  logoUrl?: string;
  stampUrl?: string;
}

export interface RegistrationRequest {
  id: string;
  name: string;
  email: string;
  phone: string;
  requestedRole: UserRole;
  schoolCode: string;
  schoolName: string;
  childAdmissionNo?: string;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: number;
}

export interface AppMessage {
  id: string;
  senderId: string;
  senderName: string;
  senderRole: UserRole;
  senderEmail: string;
  recipientId: string;
  recipientName: string;
  recipientRole?: UserRole | 'all';
  schoolId: string;
  schoolName?: string;
  subject: string;
  content: string;
  category: 'general' | 'academic' | 'fees' | 'administrative' | 'leave' | 'notice';
  priority: 'normal' | 'high' | 'urgent';
  createdAt: number;
  read: boolean;
  replyToId?: string;
}
