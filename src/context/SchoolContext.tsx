import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  User, UserRole, Student, Staff, ClassItem, SubjectItem,
  TimetableSlot, Exam, GradeBand, GradeEntry,
  QuestionPaper, SyllabusItem, Invoice,
  PaymentReceipt, PayrollItem, Expense, LibraryBook, TransportRoute,
  Notice, Assignment, LeaveRequest, School,
  Family, StudentAttendance, StaffAttendance, PeriodTiming,
  SchoolAsset, ParentAccount, RolePermission, SmtpConfig,
  AcademicSession, DemoCredentials, SchoolChangeRequest,
  RegistrationRequest, SchoolBranding, AppMessage
} from '../types';
import {
  initialSchools, initialAcademicSessions, initialDemoCredentials,
  initialSmtpConfig, initialClasses, initialSubjects, initialStaff,
  initialFamilies, initialStudents, initialTimetable, initialGradeBands,
  initialExams, initialGradeEntries, initialQuestionPapers, initialSyllabus,
  initialInvoices, initialReceipts, initialPayroll, initialExpenses,
  initialBooks, initialRoutes, initialNotices, initialAssignments,
  initialAttendanceRecords, initialLeaveRequests, initialPeriodTimings,
  initialAssets, initialParentAccounts, initialRolePermissions,
  initialSchoolChangeRequests, initialRegistrationRequests, initialMessages
} from '../data/initialData';

interface SchoolContextType {
  // Session & User
  currentUser: User;
  switchRole: (role: UserRole) => void;
  updateUserEmail: (email: string) => void;
  isOwner: boolean;
  isDemoVisitor: boolean;
  toggleDemoVisitorMode: () => void;

  // Active School Multi-Tenant
  currentSchool: School;
  schools: School[];
  switchSchool: (schoolId: string) => void;
  addSchool: (schoolData: Omit<School, 'id' | 'studentCount' | 'teacherCount' | 'createdAt' | 'expiresAt'>) => void;
  updateSchool: (id: string, s: Partial<School>) => void;
  deleteSchool: (id: string) => void;
  cancelSchoolMembership: (schoolId: string, reason?: string) => void;
  reactivateSchoolMembership: (schoolId: string) => void;
  branding: SchoolBranding;
  updateBranding: (b: Partial<SchoolBranding>) => void;

  // Demo Credentials & Single Universal Visitor Link
  demoCredentials: DemoCredentials[];
  updateDemoCredential: (role: UserRole, loginId: string, passcode: string) => void;
  getDemoShareUrl: () => string;

  // Owner Inspection Mode & Perspectives
  isOwnerInspectionMode: boolean;
  ownerInspectedRole: 'admin' | 'teacher' | 'student' | 'parent';
  setOwnerInspectionMode: (active: boolean) => void;
  setOwnerInspectedRole: (role: 'admin' | 'teacher' | 'student' | 'parent') => void;
  toggleOwnerInspectionMode: () => void;
  turnOffOwnerInspection: () => void;
  turnOnOwnerInspection: (schoolId?: string, tab?: string, role?: 'admin' | 'teacher' | 'student' | 'parent') => void;

  // Inter-Messaging
  messages: AppMessage[];
  sendMessage: (msg: Omit<AppMessage, 'id' | 'createdAt' | 'read'>) => void;
  markMessageAsRead: (messageId: string) => void;
  deleteMessage: (messageId: string) => void;

  // Change Requests & Registrations
  schoolChangeRequests: SchoolChangeRequest[];
  submitSchoolChangeRequest: (changes: SchoolChangeRequest['requestedChanges']) => void;
  reviewSchoolChangeRequest: (id: string, status: 'approved' | 'rejected', note?: string) => void;
  registrationRequests: RegistrationRequest[];
  submitRegistrationRequest: (req: Omit<RegistrationRequest, 'id' | 'createdAt' | 'status'>) => void;
  reviewRegistrationRequest: (id: string, status: 'approved' | 'rejected') => void;

  // Academic Sessions CRUD
  academicSessions: AcademicSession[];
  addAcademicSession: (sess: Omit<AcademicSession, 'id'>) => void;
  updateAcademicSession: (id: string, sess: Partial<AcademicSession>) => void;
  deleteAcademicSession: (id: string) => void;
  setCurrentSession: (id: string) => void;

  // Navigation
  activeTab: string;
  setActiveTab: (tab: string) => void;
  goBack: () => void;

  // Real-time synchronization state
  lastSyncTime: number;
  triggerRealtimeSync: () => void;

  // Data Collections & CRUD
  students: Student[];
  addStudent: (s: Omit<Student, 'id' | 'createdAt' | 'schoolId'>) => void;
  updateStudent: (id: string, s: Partial<Student>) => void;
  deleteStudent: (id: string) => void;

  families: Family[];
  addFamily: (f: Omit<Family, 'id' | 'schoolId'>) => void;
  updateFamily: (id: string, f: Partial<Family>) => void;
  deleteFamily: (id: string) => void;

  staff: Staff[];
  addStaff: (s: Omit<Staff, 'id' | 'schoolId'>) => void;
  updateStaff: (id: string, s: Partial<Staff>) => void;
  deleteStaff: (id: string) => void;

  classes: ClassItem[];
  addClass: (c: Omit<ClassItem, 'id' | 'studentsCount' | 'schoolId'>) => void;
  updateClass: (id: string, c: Partial<ClassItem>) => void;
  deleteClass: (id: string) => void;

  subjects: SubjectItem[];
  addSubject: (s: Omit<SubjectItem, 'id' | 'schoolId'>) => void;
  updateSubject: (id: string, s: Partial<SubjectItem>) => void;
  deleteSubject: (id: string) => void;

  timetable: TimetableSlot[];
  updateTimetableSlot: (slot: Omit<TimetableSlot, 'id' | 'schoolId'>) => void;
  clearTimetableForClass: (classId: string) => void;
  copyTimetableDay: (classId: string, fromDay: string, toDays: string[]) => void;

  periodTimings: PeriodTiming[];
  updatePeriodTiming: (num: number, time: string) => void;
  addPeriodTiming: (time: string) => void;
  deletePeriodTiming: (num: number) => void;

  studentAttendance: StudentAttendance[];
  markStudentAttendance: (studentId: string, classId: string, date: string, status: StudentAttendance['status']) => void;
  bulkMarkStudentAttendance: (records: { studentId: string; studentName: string; classId: string; date: string; status: StudentAttendance['status'] }[]) => void;

  staffAttendance: StaffAttendance[];
  markStaffAttendance: (staffId: string, staffName: string, date: string, status: StaffAttendance['status']) => void;

  exams: Exam[];
  addExam: (e: Omit<Exam, 'id' | 'schoolId'>) => void;
  updateExam: (id: string, e: Partial<Exam>) => void;
  deleteExam: (id: string) => void;

  gradeBands: GradeBand[];
  addGradeBand: (gb: Omit<GradeBand, 'id' | 'schoolId'>) => void;
  updateGradeBand: (id: string, gb: Partial<GradeBand>) => void;
  deleteGradeBand: (id: string) => void;
  calculateGrade: (percent: number) => { grade: string; remarks: string };

  gradeEntries: GradeEntry[];
  saveGradeEntries: (entries: Omit<GradeEntry, 'id' | 'schoolId'>[]) => void;

  questionPapers: QuestionPaper[];
  addQuestionPaper: (qp: Omit<QuestionPaper, 'id' | 'schoolId'>) => void;
  updateQuestionPaper: (id: string, qp: Partial<QuestionPaper>) => void;
  deleteQuestionPaper: (id: string) => void;

  syllabus: SyllabusItem[];
  addSyllabus: (s: Omit<SyllabusItem, 'id' | 'schoolId'>) => void;
  deleteSyllabus: (id: string) => void;

  invoices: Invoice[];
  generateInvoice: (inv: Omit<Invoice, 'id' | 'voucherNo' | 'status' | 'createdAt' | 'schoolId'>) => void;
  bulkGenerateVouchers: (classId: string, month: string, dueDate: string) => number;
  recordFeePayment: (invoiceId: string, amount: number, method: string) => PaymentReceipt;
  bulkRecordPayments: (payments: { invoiceId: string; amount: number; method: string; date?: string; ref?: string }[]) => PaymentReceipt[];
  receipts: PaymentReceipt[];

  payroll: PayrollItem[];
  runPayrollForMonth: (month: string) => void;
  markPayrollPaid: (id: string) => void;

  expenses: Expense[];
  addExpense: (e: Omit<Expense, 'id' | 'voucherNo' | 'schoolId'>) => void;
  deleteExpense: (id: string) => void;

  books: LibraryBook[];
  addBook: (b: Omit<LibraryBook, 'id' | 'available' | 'schoolId'>) => void;
  updateBook: (id: string, b: Partial<LibraryBook>) => void;
  deleteBook: (id: string) => void;

  routes: TransportRoute[];
  addRoute: (r: Omit<TransportRoute, 'id' | 'schoolId'>) => void;
  updateRoute: (id: string, r: Partial<TransportRoute>) => void;
  deleteRoute: (id: string) => void;

  notices: Notice[];
  addNotice: (n: Omit<Notice, 'id' | 'createdAt' | 'schoolId'>) => void;
  updateNotice: (id: string, n: Partial<Notice>) => void;
  deleteNotice: (id: string) => void;

  assignments: Assignment[];
  addAssignment: (a: Omit<Assignment, 'id' | 'submissionCount' | 'schoolId'>) => void;
  deleteAssignment: (id: string) => void;

  leaveRequests: LeaveRequest[];
  submitLeaveRequest: (lr: Omit<LeaveRequest, 'id' | 'status' | 'createdAt' | 'schoolId'>) => void;
  updateLeaveStatus: (id: string, status: LeaveRequest['status'], responseRemarks?: string) => void;

  assets: SchoolAsset[];
  addAsset: (a: Omit<SchoolAsset, 'id' | 'schoolId'>) => void;
  updateAsset: (id: string, a: Partial<SchoolAsset>) => void;
  deleteAsset: (id: string) => void;

  parentAccounts: ParentAccount[];
  addParentAccount: (pa: Omit<ParentAccount, 'id' | 'schoolId'>) => void;
  updateParentAccount: (id: string, pa: Partial<ParentAccount>) => void;
  deleteParentAccount: (id: string) => void;
  toggleParentStatus: (id: string) => void;

  rolePermissions: RolePermission[];
  updateRolePermission: (id: string, perms: RolePermission['permissions']) => void;

  smtpConfig: SmtpConfig;
  updateSmtpConfig: (cfg: Partial<SmtpConfig>) => void;

  // Modals
  activePrintModal: 'idcard' | 'voucher' | 'reportcard' | 'payment' | null;
  activePrintData: any;
  openPrintModal: (type: 'idcard' | 'voucher' | 'reportcard' | 'payment', data: any) => void;
  closePrintModal: () => void;

  isDownloadPackageModalOpen: boolean;
  openDownloadPackageModal: () => void;
  closeDownloadPackageModal: () => void;

  isRegistrationModalOpen: boolean;
  openRegistrationModal: () => void;
  closeRegistrationModal: () => void;

  resetAllData: () => void;
}

const SchoolContext = createContext<SchoolContextType | null>(null);

function loadFromStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(`sm_v5_${key}`);
    if (raw) return JSON.parse(raw);
  } catch (err) {
    console.error(`Error loading ${key}`, err);
  }
  return fallback;
}

function saveToStorage<T>(key: string, data: T) {
  try {
    localStorage.setItem(`sm_v5_${key}`, JSON.stringify(data));
  } catch (err) {
    console.error(`Error saving ${key}`, err);
  }
}

export const SchoolProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const urlParams = typeof window !== 'undefined' ? new URLSearchParams(window.location.search) : null;
  const isDemoFromUrl = urlParams?.get('demo') === 'visitor' || urlParams?.get('demo') === 'true';

  const [schools, setSchools] = useState<School[]>(() =>
    loadFromStorage('schools', initialSchools)
  );

  const [selectedSchoolId, setSelectedSchoolId] = useState<string>(() =>
    loadFromStorage('selectedSchoolId', initialSchools[0].id)
  );

  const currentSchool = schools.find(s => s.id === selectedSchoolId) || schools[0];

  const [currentUser, setCurrentUser] = useState<User>(() =>
    loadFromStorage('user', {
      uid: 'u-owner-1',
      name: 'Sajjad Qasmi',
      email: 'socialman121@gmail.com',
      role: 'owner' as UserRole,
      status: 'approved',
      schoolId: initialSchools[0].id,
      createdAt: Date.now()
    })
  );

  const isOwner = currentUser.email.toLowerCase() === 'socialman121@gmail.com' || currentUser.role === 'owner';
  const [isDemoVisitor, setIsDemoVisitor] = useState(isDemoFromUrl);
  const [lastSyncTime, setLastSyncTime] = useState<number>(Date.now());

  const triggerRealtimeSync = () => {
    setLastSyncTime(Date.now());
  };

  // Heartbeat to keep live clock & real-time sync fresh
  useEffect(() => {
    const interval = setInterval(() => {
      setLastSyncTime(Date.now());
    }, 15000);
    return () => clearInterval(interval);
  }, []);

  const [messages, setMessages] = useState<AppMessage[]>(() =>
    loadFromStorage('messages', initialMessages)
  );
  const [demoCredentials, setDemoCredentials] = useState<DemoCredentials[]>(() =>
    loadFromStorage('demoCredentials', initialDemoCredentials)
  );
  const [academicSessions, setAcademicSessions] = useState<AcademicSession[]>(() =>
    loadFromStorage('academicSessions', initialAcademicSessions)
  );
  const [schoolChangeRequests, setSchoolChangeRequests] = useState<SchoolChangeRequest[]>(() =>
    loadFromStorage('schoolChangeRequests', initialSchoolChangeRequests)
  );
  const [registrationRequests, setRegistrationRequests] = useState<RegistrationRequest[]>(() =>
    loadFromStorage('registrationRequests', initialRegistrationRequests)
  );

  const [isOwnerInspectionMode, setIsOwnerInspectionMode] = useState<boolean>(false);
  const [ownerInspectedRole, setOwnerInspectedRole] = useState<'admin' | 'teacher' | 'student' | 'parent'>('admin');
  const [activeTab, setActiveTab] = useState<string>(() => {
    return currentUser.role === 'owner' ? 'owner-overview' : 'dashboard';
  });

  const [students, setStudents] = useState<Student[]>(() =>
    loadFromStorage('students', initialStudents)
  );
  const [families, setFamilies] = useState<Family[]>(() =>
    loadFromStorage('families', initialFamilies)
  );
  const [staff, setStaff] = useState<Staff[]>(() =>
    loadFromStorage('staff', initialStaff)
  );
  const [classes, setClasses] = useState<ClassItem[]>(() =>
    loadFromStorage('classes', initialClasses)
  );
  const [subjects, setSubjects] = useState<SubjectItem[]>(() =>
    loadFromStorage('subjects', initialSubjects)
  );
  const [timetable, setTimetable] = useState<TimetableSlot[]>(() =>
    loadFromStorage('timetable', initialTimetable)
  );
  const [periodTimings, setPeriodTimings] = useState<PeriodTiming[]>(() =>
    loadFromStorage('periodTimings', initialPeriodTimings)
  );
  const [studentAttendance, setStudentAttendance] = useState<StudentAttendance[]>(() =>
    loadFromStorage('studentAttendance', initialAttendanceRecords)
  );
  const [staffAttendance, setStaffAttendance] = useState<StaffAttendance[]>(() =>
    loadFromStorage('staffAttendance', [
      { id: "sa-1", schoolId: "sch-1", staffId: "st-1", staffName: "Ms. Fatima Noor", date: "2026-10-01", status: "present" },
      { id: "sa-2", schoolId: "sch-1", staffId: "st-2", staffName: "Ms. Sadia Bibi", date: "2026-10-01", status: "present" },
      { id: "sa-3", schoolId: "sch-1", staffId: "st-7", staffName: "Ms. Hina Zahid", date: "2026-10-01", status: "present" },
      { id: "sa-4", schoolId: "sch-1", staffId: "st-8", staffName: "Mr. Bilal Ahmad", date: "2026-10-01", status: "present" }
    ])
  );
  const [exams, setExams] = useState<Exam[]>(() =>
    loadFromStorage('exams', initialExams)
  );
  const [gradeBands, setGradeBands] = useState<GradeBand[]>(() =>
    loadFromStorage('gradeBands', initialGradeBands)
  );
  const [gradeEntries, setGradeEntries] = useState<GradeEntry[]>(() =>
    loadFromStorage('gradeEntries', initialGradeEntries)
  );
  const [questionPapers, setQuestionPapers] = useState<QuestionPaper[]>(() =>
    loadFromStorage('questionPapers', initialQuestionPapers)
  );
  const [syllabus, setSyllabus] = useState<SyllabusItem[]>(() =>
    loadFromStorage('syllabus', initialSyllabus)
  );
  const [invoices, setInvoices] = useState<Invoice[]>(() =>
    loadFromStorage('invoices', initialInvoices)
  );
  const [receipts, setReceipts] = useState<PaymentReceipt[]>(() =>
    loadFromStorage('receipts', initialReceipts)
  );
  const [payroll, setPayroll] = useState<PayrollItem[]>(() =>
    loadFromStorage('payroll', initialPayroll)
  );
  const [expenses, setExpenses] = useState<Expense[]>(() =>
    loadFromStorage('expenses', initialExpenses)
  );
  const [books, setBooks] = useState<LibraryBook[]>(() =>
    loadFromStorage('books', initialBooks)
  );
  const [routes, setRoutes] = useState<TransportRoute[]>(() =>
    loadFromStorage('routes', initialRoutes)
  );
  const [notices, setNotices] = useState<Notice[]>(() =>
    loadFromStorage('notices', initialNotices)
  );
  const [assignments, setAssignments] = useState<Assignment[]>(() =>
    loadFromStorage('assignments', initialAssignments)
  );
  const [leaveRequests, setLeaveRequests] = useState<LeaveRequest[]>(() =>
    loadFromStorage('leaveRequests', initialLeaveRequests)
  );
  const [assets, setAssets] = useState<SchoolAsset[]>(() =>
    loadFromStorage('assets', initialAssets)
  );
  const [parentAccounts, setParentAccounts] = useState<ParentAccount[]>(() =>
    loadFromStorage('parentAccounts', initialParentAccounts)
  );
  const [rolePermissions, setRolePermissions] = useState<RolePermission[]>(() =>
    loadFromStorage('rolePermissions', initialRolePermissions)
  );
  const [smtpConfig, setSmtpConfig] = useState<SmtpConfig>(() =>
    loadFromStorage('smtpConfig', initialSmtpConfig)
  );

  // Modals state
  const [activePrintModal, setActivePrintModal] = useState<'idcard' | 'voucher' | 'reportcard' | 'payment' | null>(null);
  const [activePrintData, setActivePrintData] = useState<any>(null);
  const [isDownloadPackageModalOpen, setIsDownloadPackageModalOpen] = useState(false);
  const [isRegistrationModalOpen, setIsRegistrationModalOpen] = useState(false);

  // Sync to storage
  useEffect(() => saveToStorage('schools', schools), [schools]);
  useEffect(() => saveToStorage('selectedSchoolId', selectedSchoolId), [selectedSchoolId]);
  useEffect(() => saveToStorage('user', currentUser), [currentUser]);
  useEffect(() => saveToStorage('demoCredentials', demoCredentials), [demoCredentials]);
  useEffect(() => saveToStorage('academicSessions', academicSessions), [academicSessions]);
  useEffect(() => saveToStorage('schoolChangeRequests', schoolChangeRequests), [schoolChangeRequests]);
  useEffect(() => saveToStorage('registrationRequests', registrationRequests), [registrationRequests]);
  useEffect(() => saveToStorage('students', students), [students]);
  useEffect(() => saveToStorage('families', families), [families]);
  useEffect(() => saveToStorage('staff', staff), [staff]);
  useEffect(() => saveToStorage('classes', classes), [classes]);
  useEffect(() => saveToStorage('subjects', subjects), [subjects]);
  useEffect(() => saveToStorage('timetable', timetable), [timetable]);
  useEffect(() => saveToStorage('periodTimings', periodTimings), [periodTimings]);
  useEffect(() => saveToStorage('studentAttendance', studentAttendance), [studentAttendance]);
  useEffect(() => saveToStorage('staffAttendance', staffAttendance), [staffAttendance]);
  useEffect(() => saveToStorage('exams', exams), [exams]);
  useEffect(() => saveToStorage('gradeBands', gradeBands), [gradeBands]);
  useEffect(() => saveToStorage('gradeEntries', gradeEntries), [gradeEntries]);
  useEffect(() => saveToStorage('questionPapers', questionPapers), [questionPapers]);
  useEffect(() => saveToStorage('syllabus', syllabus), [syllabus]);
  useEffect(() => saveToStorage('invoices', invoices), [invoices]);
  useEffect(() => saveToStorage('receipts', receipts), [receipts]);
  useEffect(() => saveToStorage('payroll', payroll), [payroll]);
  useEffect(() => saveToStorage('expenses', expenses), [expenses]);
  useEffect(() => saveToStorage('books', books), [books]);
  useEffect(() => saveToStorage('routes', routes), [routes]);
  useEffect(() => saveToStorage('notices', notices), [notices]);
  useEffect(() => saveToStorage('assignments', assignments), [assignments]);
  useEffect(() => saveToStorage('leaveRequests', leaveRequests), [leaveRequests]);
  useEffect(() => saveToStorage('assets', assets), [assets]);
  useEffect(() => saveToStorage('parentAccounts', parentAccounts), [parentAccounts]);
  useEffect(() => saveToStorage('rolePermissions', rolePermissions), [rolePermissions]);
  useEffect(() => saveToStorage('smtpConfig', smtpConfig), [smtpConfig]);
  useEffect(() => saveToStorage('messages', messages), [messages]);

  const branding: SchoolBranding = {
    name: currentSchool.name,
    tagline: currentSchool.tagline,
    phone: currentSchool.phone,
    email: currentSchool.email,
    address: currentSchool.address,
    currency: currentSchool.currency,
    session: currentSchool.currentSession,
    logoUrl: currentSchool.logoUrl,
    stampUrl: currentSchool.stampUrl
  };

  const updateBranding = (b: Partial<SchoolBranding>) => {
    setSchools(prev => prev.map(s => s.id === currentSchool.id ? { ...s, ...b } : s));
    triggerRealtimeSync();
  };

  const switchSchool = (schoolId: string) => {
    setSelectedSchoolId(schoolId);
    triggerRealtimeSync();
  };

  const addSchool = (schoolData: Omit<School, 'id' | 'studentCount' | 'teacherCount' | 'createdAt' | 'expiresAt'>) => {
    const newId = `sch-${Date.now()}`;
    const newSch: School = {
      ...schoolData,
      id: newId,
      studentCount: 0,
      teacherCount: 0,
      createdAt: Date.now(),
      expiresAt: Date.now() + 365 * 86400000
    };
    setSchools(prev => [...prev, newSch]);
    triggerRealtimeSync();
  };

  const updateSchool = (id: string, s: Partial<School>) => {
    setSchools(prev => prev.map(sch => sch.id === id ? { ...sch, ...s } : sch));
    triggerRealtimeSync();
  };

  const deleteSchool = (id: string) => {
    if (schools.length <= 1) {
      alert("At least one school campus must remain active in the system.");
      return;
    }
    setSchools(prev => prev.filter(s => s.id !== id));
    if (selectedSchoolId === id) {
      const remaining = schools.filter(s => s.id !== id);
      setSelectedSchoolId(remaining[0].id);
    }
    triggerRealtimeSync();
  };

  const cancelSchoolMembership = (schoolId: string, reason?: string) => {
    setSchools(prev => prev.map(s => {
      if (s.id === schoolId) {
        return {
          ...s,
          status: 'cancelled',
          cancellationReason: reason || 'Membership revoked by System Owner (Sajjad Qasmi)',
          cancelledAt: Date.now()
        };
      }
      return s;
    }));
    triggerRealtimeSync();
  };

  const reactivateSchoolMembership = (schoolId: string) => {
    setSchools(prev => prev.map(s => {
      if (s.id === schoolId) {
        return {
          ...s,
          status: 'active',
          cancellationReason: undefined,
          cancelledAt: undefined
        };
      }
      return s;
    }));
    triggerRealtimeSync();
  };

  const switchRole = (newRole: UserRole) => {
    let mockName = "Campus Administrator";
    let mockEmail = "socialman121@gmail.com";
    let rollNo = undefined;
    let classId = undefined;
    let className = undefined;

    if (newRole === 'owner') {
      mockName = "Sajjad Qasmi";
      mockEmail = "socialman121@gmail.com";
    } else if (newRole === 'admin') {
      mockName = "Campus Administrator";
      mockEmail = "socialman121@gmail.com";
    } else if (newRole === 'teacher') {
      mockName = "Ms. Hina Zahid";
      mockEmail = "hina@sajjadqasmi.edu.pk";
    } else if (newRole === 'student') {
      mockName = "Zain Raza";
      mockEmail = "zain.raza@student.sajjadqasmi.edu.pk";
      rollNo = "01";
      classId = "c-4";
      className = "Class 4 - A";
    } else if (newRole === 'parent') {
      mockName = "Muhammad Raza (Father)";
      mockEmail = "raza.parent@gmail.com";
    }

    setCurrentUser({
      uid: `u-${newRole}-1`,
      name: mockName,
      email: mockEmail,
      role: newRole,
      status: 'approved',
      schoolId: currentSchool.id,
      rollNo,
      classId,
      className,
      createdAt: Date.now()
    });

    setIsOwnerInspectionMode(false);
    if (newRole === 'owner') {
      setActiveTab('owner-overview');
    } else {
      setActiveTab('dashboard');
    }
    triggerRealtimeSync();
  };

  const updateUserEmail = (email: string) => {
    setCurrentUser(prev => ({ ...prev, email }));
    triggerRealtimeSync();
  };

  // ONE UNIFIED DEMO LINK FOR ALL ROLES
  const getDemoShareUrl = () => {
    if (typeof window === 'undefined') return 'https://schoolmanager.app/?demo=visitor';
    return `${window.location.origin}${window.location.pathname}?demo=visitor`;
  };

  const toggleDemoVisitorMode = () => {
    setIsDemoVisitor(prev => {
      const next = !prev;
      if (typeof window !== 'undefined') {
        const url = new URL(window.location.href);
        if (next) {
          url.searchParams.set('demo', 'visitor');
        } else {
          url.searchParams.delete('demo');
        }
        window.history.replaceState({}, '', url.toString());
      }
      return next;
    });
  };

  // Inter-Messaging Handlers
  const sendMessage = (msg: Omit<AppMessage, 'id' | 'createdAt' | 'read'>) => {
    const newMsg: AppMessage = {
      ...msg,
      id: `msg-${Date.now()}`,
      createdAt: Date.now(),
      read: false
    };
    setMessages(prev => [newMsg, ...prev]);
    triggerRealtimeSync();
  };

  const markMessageAsRead = (messageId: string) => {
    setMessages(prev => prev.map(m => m.id === messageId ? { ...m, read: true } : m));
    triggerRealtimeSync();
  };

  const deleteMessage = (messageId: string) => {
    setMessages(prev => prev.filter(m => m.id !== messageId));
    triggerRealtimeSync();
  };

  const updateDemoCredential = (role: UserRole, loginId: string, passcode: string) => {
    setDemoCredentials(prev => prev.map(dc => dc.role === role ? { ...dc, loginId, passcode } : dc));
    triggerRealtimeSync();
  };

  const submitSchoolChangeRequest = (changes: SchoolChangeRequest['requestedChanges']) => {
    const newReq: SchoolChangeRequest = {
      id: `scr-${Date.now()}`,
      schoolId: currentSchool.id,
      schoolName: currentSchool.name,
      adminEmail: currentUser.email,
      requestedChanges: changes,
      status: 'pending',
      createdAt: Date.now()
    };
    setSchoolChangeRequests(prev => [newReq, ...prev]);
    triggerRealtimeSync();
  };

  const reviewSchoolChangeRequest = (id: string, status: 'approved' | 'rejected', note?: string) => {
    setSchoolChangeRequests(prev => prev.map(req => {
      if (req.id !== id) return req;
      if (status === 'approved') {
        updateSchool(req.schoolId, {
          ...(req.requestedChanges.name ? { name: req.requestedChanges.name } : {}),
          ...(req.requestedChanges.tagline ? { tagline: req.requestedChanges.tagline } : {}),
          ...(req.requestedChanges.phone ? { phone: req.requestedChanges.phone } : {}),
          ...(req.requestedChanges.address ? { address: req.requestedChanges.address } : {})
        });
      }
      return { ...req, status, ownerNote: note };
    }));
    triggerRealtimeSync();
  };

  const submitRegistrationRequest = (req: Omit<RegistrationRequest, 'id' | 'createdAt' | 'status'>) => {
    const newReq: RegistrationRequest = {
      ...req,
      id: `reg-${Date.now()}`,
      status: 'pending',
      createdAt: Date.now()
    };
    setRegistrationRequests(prev => [newReq, ...prev]);
    triggerRealtimeSync();
  };

  const reviewRegistrationRequest = (id: string, status: 'approved' | 'rejected') => {
    setRegistrationRequests(prev => prev.map(r => r.id === id ? { ...r, status } : r));
    triggerRealtimeSync();
  };

  // Academic Sessions CRUD
  const addAcademicSession = (sess: Omit<AcademicSession, 'id'>) => {
    const newSess: AcademicSession = {
      ...sess,
      id: `sess-${Date.now()}`
    };
    if (sess.isCurrent) {
      setAcademicSessions(prev => prev.map(s => ({ ...s, isCurrent: false })).concat(newSess));
      updateBranding({ session: sess.label });
    } else {
      setAcademicSessions(prev => [...prev, newSess]);
    }
    triggerRealtimeSync();
  };

  const updateAcademicSession = (id: string, sess: Partial<AcademicSession>) => {
    setAcademicSessions(prev => prev.map(s => {
      if (s.id !== id) {
        return sess.isCurrent ? { ...s, isCurrent: false } : s;
      }
      return { ...s, ...sess };
    }));
    if (sess.isCurrent && sess.label) {
      updateBranding({ session: sess.label });
    }
    triggerRealtimeSync();
  };

  const deleteAcademicSession = (id: string) => {
    if (academicSessions.length <= 1) {
      alert("At least one academic session must remain in system.");
      return;
    }
    setAcademicSessions(prev => prev.filter(s => s.id !== id));
    triggerRealtimeSync();
  };

  const setCurrentSession = (id: string) => {
    const target = academicSessions.find(s => s.id === id);
    if (!target) return;
    setAcademicSessions(prev => prev.map(s => ({ ...s, isCurrent: s.id === id })));
    updateBranding({ session: target.label });
    triggerRealtimeSync();
  };

  const toggleOwnerInspectionMode = () => {
    setIsOwnerInspectionMode(prev => {
      const next = !prev;
      if (!next) {
        setActiveTab('owner-overview');
      } else {
        setActiveTab('dashboard');
      }
      return next;
    });
    triggerRealtimeSync();
  };

  const turnOffOwnerInspection = () => {
    setIsOwnerInspectionMode(false);
    setActiveTab('owner-overview');
    triggerRealtimeSync();
  };

  const turnOnOwnerInspection = (schoolId?: string, tab?: string, role?: 'admin' | 'teacher' | 'student' | 'parent') => {
    if (schoolId) {
      setSelectedSchoolId(schoolId);
    }
    if (role) {
      setOwnerInspectedRole(role);
    }
    setIsOwnerInspectionMode(true);
    setActiveTab(tab || 'dashboard');
    triggerRealtimeSync();
  };

  const goBack = () => {
    if (currentUser.role === 'owner') {
      if (isOwnerInspectionMode) {
        if (activeTab === 'dashboard') {
          turnOffOwnerInspection();
        } else {
          setActiveTab('dashboard');
        }
      } else {
        setActiveTab('owner-overview');
      }
    } else {
      setActiveTab('dashboard');
    }
  };

  // Student CRUD
  const addStudent = (s: Omit<Student, 'id' | 'createdAt' | 'schoolId'>) => {
    const id = `stu-${Date.now()}`;
    const newStudent: Student = {
      ...s,
      id,
      schoolId: currentSchool.id,
      createdAt: new Date().toISOString().split('T')[0]
    };
    setStudents(prev => [newStudent, ...prev]);
    setClasses(prev => prev.map(c => c.id === s.classId ? { ...c, studentsCount: c.studentsCount + 1 } : c));

    const total = (s.admissionFee || 0) + (s.monthlyFee || 0) + (s.vanFee || 0) + (s.examFee || 0);
    if (total > 0) {
      const voucherNo = `VCH-${new Date().getFullYear()}-${1000 + invoices.length + 1}`;
      const inv: Invoice = {
        id: `inv-${Date.now()}`,
        schoolId: currentSchool.id,
        voucherNo,
        studentId: id,
        studentName: s.name,
        classId: s.classId,
        className: s.className,
        month: `${new Date().toLocaleString('default', { month: 'long' })} ${new Date().getFullYear()}`,
        tuitionFee: s.monthlyFee,
        vanFee: s.vanFee,
        admissionFee: s.admissionFee,
        examFee: s.examFee,
        arrears: 0,
        discount: 0,
        total,
        paidAmount: 0,
        dueDate: new Date(Date.now() + 10 * 86400000).toISOString().split('T')[0],
        status: 'pending',
        createdAt: new Date().toISOString().split('T')[0],
        items: [
          ...(s.admissionFee > 0 ? [{ label: "Admission Fee (One-Time)", amount: s.admissionFee }] : []),
          ...(s.monthlyFee > 0 ? [{ label: "Monthly Tuition Fee", amount: s.monthlyFee }] : []),
          ...(s.vanFee > 0 ? [{ label: "Van Transport Fee", amount: s.vanFee }] : []),
          ...(s.examFee > 0 ? [{ label: "Exam Fund", amount: s.examFee }] : [])
        ]
      };
      setInvoices(prev => [inv, ...prev]);
    }
    triggerRealtimeSync();
  };

  const updateStudent = (id: string, s: Partial<Student>) => {
    setStudents(prev => prev.map(st => st.id === id ? { ...st, ...s } : st));
    triggerRealtimeSync();
  };

  const deleteStudent = (id: string) => {
    setStudents(prev => prev.filter(st => st.id !== id));
    triggerRealtimeSync();
  };

  // Family CRUD
  const addFamily = (f: Omit<Family, 'id' | 'schoolId'>) => {
    setFamilies(prev => [{ ...f, id: `fam-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateFamily = (id: string, f: Partial<Family>) => {
    setFamilies(prev => prev.map(fam => fam.id === id ? { ...fam, ...f } : fam));
    triggerRealtimeSync();
  };

  const deleteFamily = (id: string) => {
    setFamilies(prev => prev.filter(fam => fam.id !== id));
    triggerRealtimeSync();
  };

  // Staff CRUD
  const addStaff = (st: Omit<Staff, 'id' | 'schoolId'>) => {
    setStaff(prev => [{ ...st, id: `st-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateStaff = (id: string, st: Partial<Staff>) => {
    setStaff(prev => prev.map(s => s.id === id ? { ...s, ...st } : s));
    triggerRealtimeSync();
  };

  const deleteStaff = (id: string) => {
    setStaff(prev => prev.filter(s => s.id !== id));
    triggerRealtimeSync();
  };

  // Class CRUD
  const addClass = (c: Omit<ClassItem, 'id' | 'studentsCount' | 'schoolId'>) => {
    setClasses(prev => [...prev, { ...c, id: `c-${Date.now()}`, schoolId: currentSchool.id, studentsCount: 0 }]);
    triggerRealtimeSync();
  };

  const updateClass = (id: string, c: Partial<ClassItem>) => {
    setClasses(prev => prev.map(cls => cls.id === id ? { ...cls, ...c } : cls));
    triggerRealtimeSync();
  };

  const deleteClass = (id: string) => {
    setClasses(prev => prev.filter(cls => cls.id !== id));
    triggerRealtimeSync();
  };

  // Subject CRUD
  const addSubject = (s: Omit<SubjectItem, 'id' | 'schoolId'>) => {
    setSubjects(prev => [...prev, { ...s, id: `sub-${Date.now()}`, schoolId: currentSchool.id }]);
    triggerRealtimeSync();
  };

  const updateSubject = (id: string, s: Partial<SubjectItem>) => {
    setSubjects(prev => prev.map(sub => sub.id === id ? { ...sub, ...s } : sub));
    triggerRealtimeSync();
  };

  const deleteSubject = (id: string) => {
    setSubjects(prev => prev.filter(sub => sub.id !== id));
    triggerRealtimeSync();
  };

  // Timetable
  const updateTimetableSlot = (slot: Omit<TimetableSlot, 'id' | 'schoolId'>) => {
    setTimetable(prev => {
      const idx = prev.findIndex(s => s.classId === slot.classId && s.day === slot.day && s.period === slot.period && s.schoolId === currentSchool.id);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = { ...slot, id: copy[idx].id, schoolId: currentSchool.id };
        return copy;
      }
      return [...prev, { ...slot, id: `tt-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`, schoolId: currentSchool.id }];
    });
    triggerRealtimeSync();
  };

  const clearTimetableForClass = (classId: string) => {
    setTimetable(prev => prev.filter(s => !(s.classId === classId && s.schoolId === currentSchool.id)));
    triggerRealtimeSync();
  };

  const copyTimetableDay = (classId: string, fromDay: string, toDays: string[]) => {
    setTimetable(prev => {
      const fromSlots = prev.filter(s => s.classId === classId && s.day === fromDay && s.schoolId === currentSchool.id);
      let updated = prev.filter(s => !(s.classId === classId && toDays.includes(s.day) && s.schoolId === currentSchool.id));
      toDays.forEach(day => {
        fromSlots.forEach(s => {
          updated.push({
            ...s,
            id: `tt-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
            day,
            schoolId: currentSchool.id
          });
        });
      });
      return updated;
    });
    triggerRealtimeSync();
  };

  // Period Timings
  const updatePeriodTiming = (num: number, time: string) => {
    setPeriodTimings(prev => prev.map(p => p.num === num ? { ...p, time } : p));
    triggerRealtimeSync();
  };

  const addPeriodTiming = (time: string) => {
    setPeriodTimings(prev => [...prev, { num: prev.length + 1, time }]);
    triggerRealtimeSync();
  };

  const deletePeriodTiming = (num: number) => {
    setPeriodTimings(prev => prev.filter(p => p.num !== num).map((p, idx) => ({ ...p, num: idx + 1 })));
    triggerRealtimeSync();
  };

  // Attendance
  const markStudentAttendance = (studentId: string, classId: string, date: string, status: StudentAttendance['status']) => {
    setStudentAttendance(prev => {
      const student = students.find(s => s.id === studentId);
      const studentName = student?.name || "Student";
      const idx = prev.findIndex(a => a.studentId === studentId && a.date === date && a.schoolId === currentSchool.id);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = { ...copy[idx], status };
        return copy;
      }
      return [{ id: `att-${Date.now()}`, schoolId: currentSchool.id, studentId, studentName, classId, date, status }, ...prev];
    });
    triggerRealtimeSync();
  };

  const bulkMarkStudentAttendance = (records: { studentId: string; studentName: string; classId: string; date: string; status: StudentAttendance['status'] }[]) => {
    setStudentAttendance(prev => {
      const keysToReplace = new Set(records.map(r => `${r.studentId}_${r.date}`));
      const remaining = prev.filter(p => !keysToReplace.has(`${p.studentId}_${p.date}`));
      const newItems: StudentAttendance[] = records.map((r, i) => ({
        id: `att-${Date.now()}-${i}`,
        schoolId: currentSchool.id,
        studentId: r.studentId,
        studentName: r.studentName,
        classId: r.classId,
        date: r.date,
        status: r.status
      }));
      return [...newItems, ...remaining];
    });
    triggerRealtimeSync();
  };

  const markStaffAttendance = (staffId: string, staffName: string, date: string, status: StaffAttendance['status']) => {
    setStaffAttendance(prev => {
      const idx = prev.findIndex(a => a.staffId === staffId && a.date === date && a.schoolId === currentSchool.id);
      if (idx >= 0) {
        const copy = [...prev];
        copy[idx] = { ...copy[idx], status };
        return copy;
      }
      return [{ id: `sa-${Date.now()}`, schoolId: currentSchool.id, staffId, staffName, date, status }, ...prev];
    });
    triggerRealtimeSync();
  };

  // Exams CRUD
  const addExam = (e: Omit<Exam, 'id' | 'schoolId'>) => {
    setExams(prev => [{ ...e, id: `ex-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateExam = (id: string, e: Partial<Exam>) => {
    setExams(prev => prev.map(ex => ex.id === id ? { ...ex, ...e } : ex));
    triggerRealtimeSync();
  };

  const deleteExam = (id: string) => {
    setExams(prev => prev.filter(ex => ex.id !== id));
    triggerRealtimeSync();
  };

  // Grade Bands
  const addGradeBand = (gb: Omit<GradeBand, 'id' | 'schoolId'>) => {
    setGradeBands(prev => [...prev, { ...gb, id: `gb-${Date.now()}`, schoolId: currentSchool.id }].sort((a, b) => b.minPct - a.minPct));
    triggerRealtimeSync();
  };

  const updateGradeBand = (id: string, gb: Partial<GradeBand>) => {
    setGradeBands(prev => prev.map(b => b.id === id ? { ...b, ...gb } : b).sort((a, b) => b.minPct - a.minPct));
    triggerRealtimeSync();
  };

  const deleteGradeBand = (id: string) => {
    setGradeBands(prev => prev.filter(b => b.id !== id));
    triggerRealtimeSync();
  };

  const calculateGrade = (percent: number) => {
    for (const band of gradeBands) {
      if (percent >= band.minPct && percent <= band.maxPct) {
        return { grade: band.grade, remarks: band.remarks };
      }
    }
    return { grade: "F", remarks: "Needs Improvement" };
  };

  const saveGradeEntries = (entries: Omit<GradeEntry, 'id' | 'schoolId'>[]) => {
    setGradeEntries(prev => {
      const keysToReplace = new Set(entries.map(e => `${e.examId}_${e.subjectId}_${e.studentId}`));
      const remaining = prev.filter(p => !keysToReplace.has(`${p.examId}_${p.subjectId}_${p.studentId}`));
      const mapped = entries.map((e, idx) => ({ ...e, id: `ge-${Date.now()}-${idx}`, schoolId: currentSchool.id }));
      return [...mapped, ...remaining];
    });
    triggerRealtimeSync();
  };

  // Question Papers CRUD
  const addQuestionPaper = (qp: Omit<QuestionPaper, 'id' | 'schoolId'>) => {
    setQuestionPapers(prev => [{ ...qp, id: `qp-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateQuestionPaper = (id: string, qp: Partial<QuestionPaper>) => {
    setQuestionPapers(prev => prev.map(q => q.id === id ? { ...q, ...qp } : q));
    triggerRealtimeSync();
  };

  const deleteQuestionPaper = (id: string) => {
    setQuestionPapers(prev => prev.filter(q => q.id !== id));
    triggerRealtimeSync();
  };

  // Syllabus CRUD
  const addSyllabus = (s: Omit<SyllabusItem, 'id' | 'schoolId'>) => {
    setSyllabus(prev => [{ ...s, id: `syl-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const deleteSyllabus = (id: string) => {
    setSyllabus(prev => prev.filter(s => s.id !== id));
    triggerRealtimeSync();
  };

  // Invoices & Finance
  const generateInvoice = (inv: Omit<Invoice, 'id' | 'voucherNo' | 'status' | 'createdAt' | 'schoolId'>) => {
    const voucherNo = `VCH-${new Date().getFullYear()}-${1000 + invoices.length + 1}`;
    setInvoices(prev => [{
      ...inv,
      id: `inv-${Date.now()}`,
      schoolId: currentSchool.id,
      voucherNo,
      status: 'pending',
      createdAt: new Date().toISOString().split('T')[0]
    }, ...prev]);
    triggerRealtimeSync();
  };

  const bulkGenerateVouchers = (classId: string, month: string, dueDate: string) => {
    const targetStudents = students.filter(s => (classId === 'all' || s.classId === classId) && s.schoolId === currentSchool.id);
    let count = 0;
    const newInvoices: Invoice[] = targetStudents.map(st => {
      count++;
      const total = st.monthlyFee + st.vanFee + (st.examFee || 0);
      return {
        id: `inv-${Date.now()}-${count}`,
        schoolId: currentSchool.id,
        voucherNo: `VCH-${new Date().getFullYear()}-${1000 + invoices.length + count}`,
        studentId: st.id,
        studentName: st.name,
        classId: st.classId,
        className: st.className,
        month,
        tuitionFee: st.monthlyFee,
        vanFee: st.vanFee,
        admissionFee: 0,
        examFee: st.examFee || 0,
        arrears: 0,
        discount: 0,
        total,
        paidAmount: 0,
        dueDate,
        status: 'pending',
        createdAt: new Date().toISOString().split('T')[0],
        items: [
          { label: "Monthly Tuition Fee", amount: st.monthlyFee },
          ...(st.vanFee > 0 ? [{ label: "Van Transport Fee", amount: st.vanFee }] : []),
          ...(st.examFee > 0 ? [{ label: "Exam Fund", amount: st.examFee }] : [])
        ]
      };
    });
    setInvoices(prev => [...newInvoices, ...prev]);
    triggerRealtimeSync();
    return count;
  };

  const recordFeePayment = (invoiceId: string, amount: number, method: string): PaymentReceipt => {
    const inv = invoices.find(i => i.id === invoiceId);
    const receiptNo = `RCPT-${new Date().getFullYear()}-${9000 + receipts.length + 1}`;
    const newReceipt: PaymentReceipt = {
      id: `rcpt-${Date.now()}`,
      schoolId: currentSchool.id,
      receiptNo,
      voucherNo: inv?.voucherNo || "VCH-MANUAL",
      studentId: inv?.studentId || "unknown",
      studentName: inv?.studentName || "Student",
      className: inv?.className || "Class 4 - A",
      amount,
      method,
      date: new Date().toISOString().split('T')[0],
      ref: `TXN-${Math.random().toString(36).substring(2, 9).toUpperCase()}`,
      cashierName: currentUser.name,
      status: "Approved",
      notes: "Direct fee payment verified"
    };
    setReceipts(prev => [newReceipt, ...prev]);
    setInvoices(prev => prev.map(i => {
      if (i.id !== invoiceId) return i;
      const newPaid = i.paidAmount + amount;
      const status: Invoice['status'] = newPaid >= i.total ? 'paid' : (newPaid > 0 ? 'partial' : 'pending');
      return { ...i, paidAmount: newPaid, status };
    }));
    triggerRealtimeSync();
    return newReceipt;
  };

  const bulkRecordPayments = (payments: { invoiceId: string; amount: number; method: string; date?: string; ref?: string }[]): PaymentReceipt[] => {
    const newReceipts: PaymentReceipt[] = [];
    const paymentMap = new Map<string, number>();

    payments.forEach((p, idx) => {
      const inv = invoices.find(i => i.id === p.invoiceId);
      const receiptNo = `RCPT-${new Date().getFullYear()}-${9000 + receipts.length + idx + 1}`;
      const newReceipt: PaymentReceipt = {
        id: `rcpt-${Date.now()}-${idx}`,
        schoolId: currentSchool.id,
        receiptNo,
        voucherNo: inv?.voucherNo || "VCH-BULK",
        studentId: inv?.studentId || "unknown",
        studentName: inv?.studentName || "Student",
        className: inv?.className || "Class 4 - A",
        amount: p.amount,
        method: p.method || "Cash",
        date: p.date || new Date().toISOString().split('T')[0],
        ref: p.ref || `BATCH-${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
        cashierName: currentUser.name,
        status: "Approved",
        notes: "Batch payment ledger import"
      };
      newReceipts.push(newReceipt);
      paymentMap.set(p.invoiceId, (paymentMap.get(p.invoiceId) || 0) + p.amount);
    });

    setReceipts(prev => [...newReceipts, ...prev]);
    setInvoices(prev => prev.map(i => {
      const addPaid = paymentMap.get(i.id);
      if (!addPaid) return i;
      const newPaid = i.paidAmount + addPaid;
      const status: Invoice['status'] = newPaid >= i.total ? 'paid' : (newPaid > 0 ? 'partial' : 'pending');
      return { ...i, paidAmount: newPaid, status };
    }));
    triggerRealtimeSync();
    return newReceipts;
  };

  // Payroll
  const runPayrollForMonth = (month: string) => {
    const schoolStaff = staff.filter(s => s.schoolId === currentSchool.id);
    const newSlips: PayrollItem[] = schoolStaff.map(st => {
      const gross = st.monthlySalary;
      const medical = Math.round(gross * 0.10);
      const houseRent = Math.round(gross * 0.15);
      const tax = Math.round(gross * 0.02);
      const net = gross + medical + houseRent - tax;
      return {
        id: `pr-${Date.now()}-${st.id}`,
        schoolId: currentSchool.id,
        staffId: st.id,
        staffName: st.name,
        designation: st.designation,
        department: st.department,
        month,
        bankAccountNo: st.bankAccountNo || "PK82HABB01427901000",
        gross,
        medicalAllowance: medical,
        houseRentAllowance: houseRent,
        taxDeduction: tax,
        absenceDeduction: 0,
        net,
        paymentMode: "Direct Bank Transfer",
        status: 'Paid',
        disbursedAt: new Date().toISOString().split('T')[0]
      };
    });
    setPayroll(prev => [...newSlips, ...prev]);
    triggerRealtimeSync();
  };

  const markPayrollPaid = (id: string) => {
    setPayroll(prev => prev.map(p => p.id === id ? { ...p, status: 'Paid', disbursedAt: new Date().toISOString().split('T')[0] } : p));
    triggerRealtimeSync();
  };

  // Expenses
  const addExpense = (e: Omit<Expense, 'id' | 'voucherNo' | 'schoolId'>) => {
    const voucherNo = `EXP-${800 + expenses.length + 1}`;
    setExpenses(prev => [{ ...e, id: `exp-${Date.now()}`, voucherNo, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const deleteExpense = (id: string) => {
    setExpenses(prev => prev.filter(e => e.id !== id));
    triggerRealtimeSync();
  };

  // Library Books
  const addBook = (b: Omit<LibraryBook, 'id' | 'available' | 'schoolId'>) => {
    setBooks(prev => [{ ...b, id: `bk-${Date.now()}`, available: b.copies, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateBook = (id: string, b: Partial<LibraryBook>) => {
    setBooks(prev => prev.map(bk => bk.id === id ? { ...bk, ...b } : bk));
    triggerRealtimeSync();
  };

  const deleteBook = (id: string) => {
    setBooks(prev => prev.filter(bk => bk.id !== id));
    triggerRealtimeSync();
  };

  // Transport Routes
  const addRoute = (r: Omit<TransportRoute, 'id' | 'schoolId'>) => {
    setRoutes(prev => [{ ...r, id: `tr-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateRoute = (id: string, r: Partial<TransportRoute>) => {
    setRoutes(prev => prev.map(rt => rt.id === id ? { ...rt, ...r } : rt));
    triggerRealtimeSync();
  };

  const deleteRoute = (id: string) => {
    setRoutes(prev => prev.filter(rt => rt.id !== id));
    triggerRealtimeSync();
  };

  // Notices
  const addNotice = (n: Omit<Notice, 'id' | 'createdAt' | 'schoolId'>) => {
    setNotices(prev => [{ ...n, id: `not-${Date.now()}`, createdAt: Date.now(), schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateNotice = (id: string, n: Partial<Notice>) => {
    setNotices(prev => prev.map(not => not.id === id ? { ...not, ...n } : not));
    triggerRealtimeSync();
  };

  const deleteNotice = (id: string) => {
    setNotices(prev => prev.filter(n => n.id !== id));
    triggerRealtimeSync();
  };

  // Assignments
  const addAssignment = (a: Omit<Assignment, 'id' | 'submissionCount' | 'schoolId'>) => {
    setAssignments(prev => [{ ...a, id: `asg-${Date.now()}`, submissionCount: 0, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const deleteAssignment = (id: string) => {
    setAssignments(prev => prev.filter(a => a.id !== id));
    triggerRealtimeSync();
  };

  // Leave Requests
  const submitLeaveRequest = (lr: Omit<LeaveRequest, 'id' | 'status' | 'createdAt' | 'schoolId'>) => {
    setLeaveRequests(prev => [{ ...lr, id: `lr-${Date.now()}`, status: 'pending', createdAt: Date.now(), schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateLeaveStatus = (id: string, status: LeaveRequest['status'], responseRemarks?: string) => {
    setLeaveRequests(prev => prev.map(l => l.id === id ? {
      ...l,
      status,
      ...(responseRemarks ? { responseNote: responseRemarks } : {}),
      respondedBy: currentUser.name,
      respondedAt: Date.now()
    } : l));
    triggerRealtimeSync();
  };

  // Assets
  const addAsset = (a: Omit<SchoolAsset, 'id' | 'schoolId'>) => {
    setAssets(prev => [{ ...a, id: `ast-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateAsset = (id: string, a: Partial<SchoolAsset>) => {
    setAssets(prev => prev.map(ast => ast.id === id ? { ...ast, ...a } : ast));
    triggerRealtimeSync();
  };

  const deleteAsset = (id: string) => {
    setAssets(prev => prev.filter(ast => ast.id !== id));
    triggerRealtimeSync();
  };

  // Parent Accounts
  const addParentAccount = (pa: Omit<ParentAccount, 'id' | 'schoolId'>) => {
    setParentAccounts(prev => [{ ...pa, id: `pa-${Date.now()}`, schoolId: currentSchool.id }, ...prev]);
    triggerRealtimeSync();
  };

  const updateParentAccount = (id: string, pa: Partial<ParentAccount>) => {
    setParentAccounts(prev => prev.map(p => p.id === id ? { ...p, ...pa } : p));
    triggerRealtimeSync();
  };

  const deleteParentAccount = (id: string) => {
    setParentAccounts(prev => prev.filter(p => p.id !== id));
    triggerRealtimeSync();
  };

  const toggleParentStatus = (id: string) => {
    setParentAccounts(prev => prev.map(p => p.id === id ? { ...p, status: p.status === 'active' ? 'suspended' : 'active' } : p));
    triggerRealtimeSync();
  };

  // Role Permissions
  const updateRolePermission = (id: string, perms: RolePermission['permissions']) => {
    setRolePermissions(prev => prev.map(rp => rp.id === id ? { ...rp, permissions: perms } : rp));
    triggerRealtimeSync();
  };

  // SMTP Settings
  const updateSmtpConfig = (cfg: Partial<SmtpConfig>) => {
    setSmtpConfig(prev => ({ ...prev, ...cfg }));
    triggerRealtimeSync();
  };

  const openPrintModal = (type: 'idcard' | 'voucher' | 'reportcard' | 'payment', data: any) => {
    setActivePrintModal(type);
    setActivePrintData(data);
  };

  const closePrintModal = () => {
    setActivePrintModal(null);
    setActivePrintData(null);
  };

  const openDownloadPackageModal = () => setIsDownloadPackageModalOpen(true);
  const closeDownloadPackageModal = () => setIsDownloadPackageModalOpen(false);

  const openRegistrationModal = () => setIsRegistrationModalOpen(true);
  const closeRegistrationModal = () => setIsRegistrationModalOpen(false);

  const resetAllData = () => {
    localStorage.clear();
    setSchools(initialSchools);
    setSelectedSchoolId(initialSchools[0].id);
    setAcademicSessions(initialAcademicSessions);
    setDemoCredentials(initialDemoCredentials);
    setStudents(initialStudents);
    setFamilies(initialFamilies);
    setStaff(initialStaff);
    setClasses(initialClasses);
    setSubjects(initialSubjects);
    setTimetable(initialTimetable);
    setPeriodTimings(initialPeriodTimings);
    setStudentAttendance(initialAttendanceRecords);
    setExams(initialExams);
    setGradeBands(initialGradeBands);
    setGradeEntries(initialGradeEntries);
    setQuestionPapers(initialQuestionPapers);
    setSyllabus(initialSyllabus);
    setInvoices(initialInvoices);
    setReceipts(initialReceipts);
    setPayroll(initialPayroll);
    setExpenses(initialExpenses);
    setBooks(initialBooks);
    setRoutes(initialRoutes);
    setNotices(initialNotices);
    setAssignments(initialAssignments);
    setLeaveRequests(initialLeaveRequests);
    setAssets(initialAssets);
    setParentAccounts(initialParentAccounts);
    setRolePermissions(initialRolePermissions);
    setSmtpConfig(initialSmtpConfig);
    setSchoolChangeRequests(initialSchoolChangeRequests);
    setRegistrationRequests(initialRegistrationRequests);
    setActiveTab('dashboard');
    triggerRealtimeSync();
  };

  return (
    <SchoolContext.Provider
      value={{
        currentUser,
        switchRole,
        updateUserEmail,
        isOwner,
        isDemoVisitor,
        toggleDemoVisitorMode,
        currentSchool,
        schools,
        switchSchool,
        addSchool,
        updateSchool,
        deleteSchool,
        cancelSchoolMembership,
        reactivateSchoolMembership,
        branding,
        updateBranding,
        demoCredentials,
        updateDemoCredential,
        getDemoShareUrl,
        isOwnerInspectionMode,
        ownerInspectedRole,
        setOwnerInspectionMode: setIsOwnerInspectionMode,
        setOwnerInspectedRole,
        toggleOwnerInspectionMode,
        turnOffOwnerInspection,
        turnOnOwnerInspection,
        messages,
        sendMessage,
        markMessageAsRead,
        deleteMessage,
        schoolChangeRequests,
        submitSchoolChangeRequest,
        reviewSchoolChangeRequest,
        registrationRequests,
        submitRegistrationRequest,
        reviewRegistrationRequest,
        academicSessions,
        addAcademicSession,
        updateAcademicSession,
        deleteAcademicSession,
        setCurrentSession,
        activeTab,
        setActiveTab,
        goBack,
        lastSyncTime,
        triggerRealtimeSync,
        students,
        addStudent,
        updateStudent,
        deleteStudent,
        families,
        addFamily,
        updateFamily,
        deleteFamily,
        staff,
        addStaff,
        updateStaff,
        deleteStaff,
        classes,
        addClass,
        updateClass,
        deleteClass,
        subjects,
        addSubject,
        updateSubject,
        deleteSubject,
        timetable,
        updateTimetableSlot,
        clearTimetableForClass,
        copyTimetableDay,
        periodTimings,
        updatePeriodTiming,
        addPeriodTiming,
        deletePeriodTiming,
        studentAttendance,
        markStudentAttendance,
        bulkMarkStudentAttendance,
        staffAttendance,
        markStaffAttendance,
        exams,
        addExam,
        updateExam,
        deleteExam,
        gradeBands,
        addGradeBand,
        updateGradeBand,
        deleteGradeBand,
        calculateGrade,
        gradeEntries,
        saveGradeEntries,
        questionPapers,
        addQuestionPaper,
        updateQuestionPaper,
        deleteQuestionPaper,
        syllabus,
        addSyllabus,
        deleteSyllabus,
        invoices,
        generateInvoice,
        bulkGenerateVouchers,
        recordFeePayment,
        bulkRecordPayments,
        receipts,
        payroll,
        runPayrollForMonth,
        markPayrollPaid,
        expenses,
        addExpense,
        deleteExpense,
        books,
        addBook,
        updateBook,
        deleteBook,
        routes,
        addRoute,
        updateRoute,
        deleteRoute,
        notices,
        addNotice,
        updateNotice,
        deleteNotice,
        assignments,
        addAssignment,
        deleteAssignment,
        leaveRequests,
        submitLeaveRequest,
        updateLeaveStatus,
        assets,
        addAsset,
        updateAsset,
        deleteAsset,
        parentAccounts,
        addParentAccount,
        updateParentAccount,
        deleteParentAccount,
        toggleParentStatus,
        rolePermissions,
        updateRolePermission,
        smtpConfig,
        updateSmtpConfig,
        activePrintModal,
        activePrintData,
        openPrintModal,
        closePrintModal,
        isDownloadPackageModalOpen,
        openDownloadPackageModal,
        closeDownloadPackageModal,
        isRegistrationModalOpen,
        openRegistrationModal,
        closeRegistrationModal,
        resetAllData
      }}
    >
      {children}
    </SchoolContext.Provider>
  );
};

export const useSchool = () => {
  const context = useContext(SchoolContext);
  if (!context) {
    throw new Error('useSchool must be used within a SchoolProvider');
  }
  return context;
};
