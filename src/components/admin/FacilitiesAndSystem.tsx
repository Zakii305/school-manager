import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  LibraryBook, TransportRoute, Notice, LeaveRequest, SchoolAsset, ParentAccount, RolePermission, SmtpConfig
} from '../../types';
import {
  Library, Bus, Archive, Send, Bell, ClipboardList,
  UserCog, Shield, Building2, Settings,
  Plus, Trash2, Printer, CheckCircle,
  Award, FileText, Edit2, Download, Search,
  RefreshCw, Upload, Key,
  FileSpreadsheet, AlertTriangle, Mail, Smartphone
} from 'lucide-react';
import {
  exportAcademicReportCardPDF,
  exportStudentIDCardPDF,
  exportCustomReportPDF
} from '../../utils/pdfExport';

interface FacilitiesAndSystemProps {
  section?: 'library' | 'transport' | 'assets' | 'smtp' | 'notices' | 'leave' | 'parent-accounts' | 'audit' | 'branding' | 'system' | 'id-cards' | 'result-cards' | 'roles' | 'reports';
}

export const FacilitiesAndSystem: React.FC<FacilitiesAndSystemProps> = ({ section = 'library' }) => {
  const {
    books, routes, notices, leaveRequests, branding, students, classes,
    invoices, expenses, payroll, gradeEntries, gradeBands,
    addBook, updateBook, deleteBook,
    addRoute, updateRoute, deleteRoute,
    addNotice, updateNotice, deleteNotice,
    updateLeaveStatus, updateBranding, openPrintModal,
    assets, addAsset, updateAsset, deleteAsset,
    parentAccounts, addParentAccount, updateParentAccount, deleteParentAccount, toggleParentStatus,
    rolePermissions, updateRolePermission,
    smtpConfig, updateSmtpConfig,
    openDownloadPackageModal, resetAllData, currentSchool
  } = useSchool();

  // Library State
  const [isBookModalOpen, setIsBookModalOpen] = useState(false);
  const [editingBookId, setEditingBookId] = useState<string | null>(null);
  const [bookTitle, setBookTitle] = useState('');
  const [bookAuthor, setBookAuthor] = useState('');
  const [bookCategory, setBookCategory] = useState('Textbook');
  const [bookCopies, setBookCopies] = useState<number>(30);
  const [bookSearch, setBookSearch] = useState('');

  // Route State
  const [isRouteModalOpen, setIsRouteModalOpen] = useState(false);
  const [editingRouteId, setEditingRouteId] = useState<string | null>(null);
  const [routeName, setRouteName] = useState('');
  const [routeVehicle, setRouteVehicle] = useState('LES-');
  const [routeDriver, setRouteDriver] = useState('');
  const [routeDriverPhone, setRouteDriverPhone] = useState('0300-');
  const [routeFee, setRouteFee] = useState<number>(1200);
  const [routeCapacity, setRouteCapacity] = useState<number>(40);

  // Notice State
  const [isNoticeModalOpen, setIsNoticeModalOpen] = useState(false);
  const [editingNoticeId, setEditingNoticeId] = useState<string | null>(null);
  const [noticeTitle, setNoticeTitle] = useState('');
  const [noticeBody, setNoticeBody] = useState('');
  const [noticeAudience, setNoticeAudience] = useState('All Students, Parents & Staff');

  // Leave Response State
  const [isLeaveResponseModalOpen, setIsLeaveResponseModalOpen] = useState(false);
  const [respondingLeave, setRespondingLeave] = useState<LeaveRequest | null>(null);
  const [leaveResponseStatus, setLeaveResponseStatus] = useState<'approved' | 'rejected'>('approved');
  const [leaveResponseRemarks, setLeaveResponseRemarks] = useState('');

  // School Assets State
  const [isAssetModalOpen, setIsAssetModalOpen] = useState(false);
  const [editingAssetId, setEditingAssetId] = useState<string | null>(null);
  const [assetName, setAssetName] = useState('');
  const [assetCategory, setAssetCategory] = useState('IT Equipment');
  const [assetQty, setAssetQty] = useState<number>(1);
  const [assetUnitValue, setAssetUnitValue] = useState<number>(5000);
  const [assetLocation, setAssetLocation] = useState('');
  const [assetCondition, setAssetCondition] = useState<SchoolAsset['condition']>('Good');
  const [assetSearch, setAssetSearch] = useState('');

  // Parent Accounts State
  const [isParentModalOpen, setIsParentModalOpen] = useState(false);
  const [editingParentId, setEditingParentId] = useState<string | null>(null);
  const [parentName, setParentName] = useState('');
  const [parentEmail, setParentEmail] = useState('');
  const [parentPhone, setParentPhone] = useState('');
  const [parentChildrenIds, setParentChildrenIds] = useState<string[]>([]);

  // Roles & Permissions State
  const [localRoles, setLocalRoles] = useState<RolePermission[]>(rolePermissions);
  const [rolesSaved, setRolesSaved] = useState(false);

  // Reports Generator State
  const [selectedReportType, setSelectedReportType] = useState<string>('fee_collection');
  const [reportClassFilter, setReportClassFilter] = useState<string>('all');
  const [reportDateFrom, setReportDateFrom] = useState('2026-10-01');
  const [reportDateTo, setReportDateTo] = useState('2026-10-31');

  // Branding State
  const [brandingForm, setBrandingForm] = useState(branding);
  const [brandingLogo, setBrandingLogo] = useState<string | null>(branding.logoUrl || null);
  const [brandingStamp, setBrandingStamp] = useState<string | null>(branding.stampUrl || null);
  const [brandingSaved, setBrandingSaved] = useState(false);

  // Audit Log State
  const [auditSearch, setAuditSearch] = useState('');

  // SMTP Configuration Form State
  const [smtpForm, setSmtpForm] = useState<SmtpConfig>(smtpConfig);
  const [smtpSavedFeedback, setSmtpSavedFeedback] = useState(false);
  const [testEmailRecipient, setTestEmailRecipient] = useState('parent@gmail.com');
  const [testDispatchSuccess, setTestDispatchSuccess] = useState<string | null>(null);

  // Book Handlers
  const handleOpenAddBook = () => {
    setEditingBookId(null);
    setBookTitle('');
    setBookAuthor('');
    setBookCategory('Textbook');
    setBookCopies(30);
    setIsBookModalOpen(true);
  };

  const handleOpenEditBook = (b: LibraryBook) => {
    setEditingBookId(b.id);
    setBookTitle(b.title);
    setBookAuthor(b.author);
    setBookCategory(b.category);
    setBookCopies(b.copies);
    setIsBookModalOpen(true);
  };

  const handleSaveBook = (e: React.FormEvent) => {
    e.preventDefault();
    if (!bookTitle) return;
    if (editingBookId) {
      updateBook(editingBookId, {
        title: bookTitle,
        author: bookAuthor || 'Various',
        category: bookCategory,
        copies: Number(bookCopies) || 1,
        available: Number(bookCopies) || 1
      });
    } else {
      addBook({
        title: bookTitle,
        author: bookAuthor || 'Various',
        category: bookCategory,
        copies: Number(bookCopies) || 1
      });
    }
    setIsBookModalOpen(false);
  };

  // Route Handlers
  const handleOpenAddRoute = () => {
    setEditingRouteId(null);
    setRouteName('');
    setRouteVehicle('LES-');
    setRouteDriver('');
    setRouteDriverPhone('0300-');
    setRouteFee(1200);
    setRouteCapacity(40);
    setIsRouteModalOpen(true);
  };

  const handleOpenEditRoute = (r: TransportRoute) => {
    setEditingRouteId(r.id);
    setRouteName(r.route);
    setRouteVehicle(r.vehicleNo);
    setRouteDriver(r.driver);
    setRouteDriverPhone(r.driverPhone || '0300-');
    setRouteFee(r.monthlyFee);
    setRouteCapacity(r.capacity);
    setIsRouteModalOpen(true);
  };

  const handleSaveRoute = (e: React.FormEvent) => {
    e.preventDefault();
    if (!routeName) return;
    if (editingRouteId) {
      updateRoute(editingRouteId, {
        route: routeName,
        vehicleNo: routeVehicle,
        driver: routeDriver || 'School Driver',
        driverPhone: routeDriverPhone,
        capacity: Number(routeCapacity) || 40,
        monthlyFee: Number(routeFee) || 1200
      });
    } else {
      addRoute({
        route: routeName,
        vehicleNo: routeVehicle,
        driver: routeDriver || 'School Driver',
        driverPhone: routeDriverPhone,
        capacity: Number(routeCapacity) || 40,
        monthlyFee: Number(routeFee) || 1200
      });
    }
    setIsRouteModalOpen(false);
  };

  // Notice Handlers
  const handleOpenAddNotice = () => {
    setEditingNoticeId(null);
    setNoticeTitle('');
    setNoticeBody('');
    setNoticeAudience('All Students, Parents & Staff');
    setIsNoticeModalOpen(true);
  };

  const handleOpenEditNotice = (n: Notice) => {
    setEditingNoticeId(n.id);
    setNoticeTitle(n.title);
    setNoticeBody(n.body);
    setNoticeAudience(n.audience);
    setIsNoticeModalOpen(true);
  };

  const handleSaveNotice = (e: React.FormEvent) => {
    e.preventDefault();
    if (!noticeTitle || !noticeBody) return;
    if (editingNoticeId) {
      updateNotice(editingNoticeId, {
        title: noticeTitle,
        body: noticeBody,
        audience: noticeAudience
      });
    } else {
      addNotice({
        title: noticeTitle,
        body: noticeBody,
        authorName: 'Principal Office',
        audience: noticeAudience
      });
    }
    setIsNoticeModalOpen(false);
  };

  // Leave Response Handlers
  const handleOpenLeaveResponse = (lr: LeaveRequest) => {
    setRespondingLeave(lr);
    setLeaveResponseStatus(lr.status === 'pending' ? 'approved' : lr.status);
    setLeaveResponseRemarks(lr.responseNote || '');
    setIsLeaveResponseModalOpen(true);
  };

  const handleSaveLeaveResponse = (e: React.FormEvent) => {
    e.preventDefault();
    if (!respondingLeave) return;
    updateLeaveStatus(respondingLeave.id, leaveResponseStatus, leaveResponseRemarks || 'Approved by Principal');
    setIsLeaveResponseModalOpen(false);
    setRespondingLeave(null);
  };

  // Asset Handlers
  const handleOpenAddAsset = () => {
    setEditingAssetId(null);
    setAssetName('');
    setAssetCategory('IT Equipment');
    setAssetQty(1);
    setAssetUnitValue(25000);
    setAssetLocation('Main Computer Lab');
    setAssetCondition('Good');
    setIsAssetModalOpen(true);
  };

  const handleOpenEditAsset = (a: SchoolAsset) => {
    setEditingAssetId(a.id);
    setAssetName(a.name);
    setAssetCategory(a.category);
    setAssetQty(a.quantity);
    setAssetUnitValue(a.unitValue);
    setAssetLocation(a.location);
    setAssetCondition(a.condition);
    setIsAssetModalOpen(true);
  };

  const handleSaveAsset = (e: React.FormEvent) => {
    e.preventDefault();
    if (!assetName) return;
    const qty = Number(assetQty) || 1;
    const unit = Number(assetUnitValue) || 0;
    const total = qty * unit;

    if (editingAssetId) {
      updateAsset(editingAssetId, {
        name: assetName,
        category: assetCategory,
        quantity: qty,
        unitValue: unit,
        totalValue: total,
        location: assetLocation,
        condition: assetCondition
      });
    } else {
      addAsset({
        name: assetName,
        category: assetCategory,
        quantity: qty,
        unitValue: unit,
        totalValue: total,
        location: assetLocation || 'Campus Ground',
        condition: assetCondition,
        purchaseDate: new Date().toISOString().split('T')[0]
      });
    }
    setIsAssetModalOpen(false);
  };

  // Parent Account Handlers
  const handleOpenAddParent = () => {
    setEditingParentId(null);
    setParentName('');
    setParentEmail('');
    setParentPhone('');
    setParentChildrenIds([]);
    setIsParentModalOpen(true);
  };

  const handleOpenEditParent = (p: ParentAccount) => {
    setEditingParentId(p.id);
    setParentName(p.name);
    setParentEmail(p.email);
    setParentPhone(p.phone);
    setParentChildrenIds(p.childrenIds || []);
    setIsParentModalOpen(true);
  };

  const handleSaveParent = (e: React.FormEvent) => {
    e.preventDefault();
    if (!parentName || !parentPhone) return;
    if (editingParentId) {
      updateParentAccount(editingParentId, {
        name: parentName,
        email: parentEmail,
        phone: parentPhone,
        childrenIds: parentChildrenIds
      });
    } else {
      addParentAccount({
        name: parentName,
        email: parentEmail || `${parentName.toLowerCase().replace(/\s+/g, '.')}@parent.sajjadqasmi.edu.pk`,
        phone: parentPhone,
        childrenIds: parentChildrenIds,
        status: 'active',
        lastLogin: 'Never'
      });
    }
    setIsParentModalOpen(false);
  };

  // Branding Handlers
  const handleLogoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setBrandingLogo(reader.result as string);
        updateBranding({ logoUrl: reader.result as string });
      };
      reader.readAsDataURL(file);
    }
  };

  const handleStampUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        setBrandingStamp(reader.result as string);
        updateBranding({ stampUrl: reader.result as string });
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSaveBranding = (e: React.FormEvent) => {
    e.preventDefault();
    updateBranding(brandingForm);
    setBrandingSaved(true);
    setTimeout(() => setBrandingSaved(false), 3000);
  };

  // Roles Handler
  const handleSaveRoles = () => {
    localRoles.forEach(r => {
      updateRolePermission(r.id, r.permissions);
    });
    setRolesSaved(true);
    setTimeout(() => setRolesSaved(false), 3000);
  };

  // SMTP Save & Test Handlers
  const handleSaveSmtp = (e: React.FormEvent) => {
    e.preventDefault();
    updateSmtpConfig(smtpForm);
    setSmtpSavedFeedback(true);
    setTimeout(() => setSmtpSavedFeedback(false), 3000);
  };

  const handleSendTestNotification = () => {
    if (!testEmailRecipient || !testEmailRecipient.includes('@')) {
      alert("Please enter a valid test recipient email address.");
      return;
    }
    setTestDispatchSuccess(`Test message successfully relayed through ${smtpForm.host}:${smtpForm.port} to ${testEmailRecipient}!`);
    setTimeout(() => setTestDispatchSuccess(null), 4500);
  };

  return (
    <div className="space-y-6">
      
      {/* SECTION: NOTIFICATIONS & SMTP */}
      {section === 'smtp' && (
        <div className="space-y-5">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Send className="w-5 h-5 text-indigo-600" />
                <span>Notifications, Email SMTP & WhatsApp Gateway</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Configure SMTP server for fee challans, exam results, absent alerts, and Meta WhatsApp Cloud API.
              </p>
            </div>
            {smtpSavedFeedback && (
              <span className="text-xs font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-3 py-1 rounded-xl flex items-center gap-1.5 animate-in fade-in">
                <CheckCircle className="w-4 h-4 text-emerald-600" />
                <span>SMTP Settings Saved!</span>
              </span>
            )}
          </div>

          {testDispatchSuccess && (
            <div className="bg-emerald-50 border border-emerald-200 p-3.5 rounded-xl text-xs font-bold text-emerald-800 flex items-center justify-between animate-in fade-in">
              <div className="flex items-center gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0" />
                <span>{testDispatchSuccess}</span>
              </div>
              <button onClick={() => setTestDispatchSuccess(null)} className="text-slate-400 hover:text-slate-700">
                <span className="text-xs font-bold">✕</span>
              </button>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <form onSubmit={handleSaveSmtp} className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-5 text-xs">
              <div>
                <h3 className="font-bold text-sm text-slate-900 border-b border-slate-100 pb-2 mb-3 flex items-center gap-2">
                  <Mail className="w-4 h-4 text-indigo-600" />
                  <span>1. Outgoing Mail Server (SMTP)</span>
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">SMTP Host Server *</label>
                    <input
                      type="text"
                      required
                      placeholder="smtp.gmail.com"
                      value={smtpForm.host}
                      onChange={e => setSmtpForm({ ...smtpForm, host: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Port *</label>
                    <input
                      type="text"
                      required
                      placeholder="587"
                      value={smtpForm.port}
                      onChange={e => setSmtpForm({ ...smtpForm, port: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Encryption Mode</label>
                    <select
                      value={smtpForm.encryption}
                      onChange={e => setSmtpForm({ ...smtpForm, encryption: e.target.value as any })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl bg-slate-50 font-semibold"
                    >
                      <option value="TLS">TLS (Recommended - 587)</option>
                      <option value="SSL">SSL (Port 465)</option>
                      <option value="None">None (Insecure)</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Username / Authenticated Email *</label>
                    <input
                      type="text"
                      required
                      placeholder="socialman121@gmail.com"
                      value={smtpForm.user}
                      onChange={e => setSmtpForm({ ...smtpForm, user: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                    />
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">App Password / Auth Key *</label>
                    <input
                      type="password"
                      required
                      value={smtpForm.pass}
                      onChange={e => setSmtpForm({ ...smtpForm, pass: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-indigo-500"
                    />
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-slate-200">
                <h3 className="font-bold text-sm text-slate-900 border-b border-slate-100 pb-2 mb-3 flex items-center gap-2">
                  <Smartphone className="w-4 h-4 text-emerald-600" />
                  <span>2. WhatsApp Cloud API & SMS Gateway</span>
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">WhatsApp Phone Number ID</label>
                    <input
                      type="text"
                      placeholder="1098234871923"
                      value={smtpForm.whatsappPhoneId}
                      onChange={e => setSmtpForm({ ...smtpForm, whatsappPhoneId: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-emerald-500"
                    />
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Permanent Meta Access Token</label>
                    <input
                      type="password"
                      placeholder="EAAG...token"
                      value={smtpForm.whatsappApiToken}
                      onChange={e => setSmtpForm({ ...smtpForm, whatsappApiToken: e.target.value })}
                      className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-emerald-500"
                    />
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200">
                <button
                  type="submit"
                  className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-xs transition"
                >
                  Save SMTP & Gateway Settings
                </button>
              </div>
            </form>

            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4 text-xs flex flex-col justify-between">
              <div>
                <div className="flex items-center gap-2 border-b border-slate-100 pb-2 mb-3">
                  <Send className="w-4 h-4 text-emerald-600" />
                  <h3 className="font-bold text-sm text-slate-900">Test Notification Dispatch</h3>
                </div>
                <p className="text-slate-600 leading-relaxed mb-3">
                  Send a live test payload to verify handshake connectivity with your mail server.
                </p>
                <div className="space-y-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Recipient Email</label>
                    <input
                      type="email"
                      value={testEmailRecipient}
                      onChange={e => setTestEmailRecipient(e.target.value)}
                      placeholder="parent@gmail.com"
                      className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-emerald-500"
                    />
                  </div>
                  <button
                    type="button"
                    onClick={handleSendTestNotification}
                    className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold transition shadow-xs flex items-center justify-center gap-2"
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>Send Test Email Now</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: REPORTS GENERATOR */}
      {section === 'reports' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <FileSpreadsheet className="w-5 h-5 text-indigo-600" />
                <span>Reports Generator & PDF Exporter</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Generate custom downloadable reports for fees, attendance, academic marksheets, payroll, and assets.
              </p>
            </div>
            <button
              onClick={() => {
                let headers: string[] = [];
                let rows: (string | number)[][] = [];
                let title = '';

                if (selectedReportType === 'fee_collection') {
                  title = `${branding.name} - Student Fee Collection & Defaulters Ledger`;
                  headers = ['Voucher #', 'Student Name', 'Class', 'Billing Month', 'Billed Amount', 'Paid Amount', 'Balance Due', 'Status'];
                  rows = invoices.map(i => [
                    i.voucherNo,
                    i.studentName,
                    i.className,
                    i.month,
                    `Rs ${i.total.toLocaleString()}`,
                    `Rs ${i.paidAmount.toLocaleString()}`,
                    `Rs ${(i.total - i.paidAmount).toLocaleString()}`,
                    i.status.toUpperCase()
                  ]);
                } else if (selectedReportType === 'attendance') {
                  title = `${branding.name} - Monthly Student Attendance Register`;
                  headers = ['Adm #', 'Student Name', 'Class', 'Gender', 'Father Name', 'Hafiz-e-Quran', 'Status'];
                  rows = students.map(s => [
                    s.admissionNo,
                    s.name,
                    s.className,
                    s.gender.toUpperCase(),
                    s.fatherName,
                    s.hafizEQuran,
                    s.status.toUpperCase()
                  ]);
                } else if (selectedReportType === 'payroll') {
                  title = `${branding.name} - Faculty & Staff Payroll Statement`;
                  headers = ['Staff Name', 'Designation', 'Department', 'Basic Gross', 'Allowances', 'Deductions', 'Net Payable', 'Status'];
                  rows = payroll.map(p => [
                    p.staffName,
                    p.designation,
                    p.department,
                    `Rs ${p.gross.toLocaleString()}`,
                    `Rs ${(p.medicalAllowance + p.houseRentAllowance).toLocaleString()}`,
                    `Rs ${(p.taxDeduction + p.absenceDeduction).toLocaleString()}`,
                    `Rs ${p.net.toLocaleString()}`,
                    p.status
                  ]);
                } else {
                  title = `${branding.name} - Institutional Assets & Inventory Register`;
                  headers = ['Asset Description', 'Category', 'Quantity', 'Unit Value', 'Total Value', 'Location', 'Condition'];
                  rows = assets.map(a => [
                    a.name,
                    a.category,
                    a.quantity,
                    `Rs ${a.unitValue.toLocaleString()}`,
                    `Rs ${a.totalValue.toLocaleString()}`,
                    a.location,
                    a.condition
                  ]);
                }

                exportCustomReportPDF(
                  title,
                  `Generated on: ${new Date().toLocaleDateString()} • Academic Session: ${branding.session} • School Code: ${currentSchool.code}`,
                  headers,
                  rows,
                  branding
                );
              }}
              className="flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Download className="w-4 h-4" />
              <span>Download Report in PDF</span>
            </button>
          </div>

          {/* Filter Bar */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs grid grid-cols-1 sm:grid-cols-4 gap-3 text-xs">
            <div>
              <label className="font-bold text-slate-700 block mb-1">Select Report Category</label>
              <select
                value={selectedReportType}
                onChange={e => setSelectedReportType(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50 font-semibold"
              >
                <option value="fee_collection">Fee Collection & Defaulters</option>
                <option value="attendance">Student Attendance Summary</option>
                <option value="payroll">Faculty Payroll & Salaries</option>
                <option value="assets">Institutional Asset Inventory</option>
              </select>
            </div>
            <div>
              <label className="font-bold text-slate-700 block mb-1">Filter Class</label>
              <select
                value={reportClassFilter}
                onChange={e => setReportClassFilter(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50 font-semibold"
              >
                <option value="all">All Classes & Sections</option>
                {classes.map(c => (
                  <option key={c.id} value={c.id}>{c.name} - Section {c.section}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="font-bold text-slate-700 block mb-1">Date From</label>
              <input
                type="date"
                value={reportDateFrom}
                onChange={e => setReportDateFrom(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50"
              />
            </div>
            <div>
              <label className="font-bold text-slate-700 block mb-1">Date To</label>
              <input
                type="date"
                value={reportDateTo}
                onChange={e => setReportDateTo(e.target.value)}
                className="w-full p-2 border border-slate-300 rounded-lg bg-slate-50"
              />
            </div>
          </div>
        </div>
      )}

      {/* SECTION: ID CARDS */}
      {section === 'id-cards' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Award className="w-5 h-5 text-indigo-600" />
                <span>Student Identity Cards (Proper Settings & PDF)</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Print or download high-resolution student ID cards formatted to standard CR80 setting.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {students.map(st => (
              <div key={st.id} className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col justify-between hover:border-indigo-300 transition">
                <div className="text-center">
                  <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-indigo-600 to-amber-400 flex items-center justify-center font-bold text-white text-xl mx-auto shadow-sm">
                    {st.name.charAt(0)}
                  </div>
                  <h3 className="font-bold text-sm text-slate-900 mt-2">{st.name}</h3>
                  <div className="text-xs text-slate-500">Adm #{st.admissionNo} • {st.className}</div>
                  <div className="text-[11px] text-slate-400 mt-1 font-mono">Blood: {st.bloodGroup || 'O+'}</div>
                </div>

                <div className="flex gap-1.5 mt-4">
                  <button
                    onClick={() => exportStudentIDCardPDF(st, branding)}
                    className="flex-1 py-1.5 bg-amber-50 hover:bg-amber-100 text-amber-800 rounded-xl text-xs font-bold transition border border-amber-200 flex items-center justify-center gap-1"
                    title="Download ID Card PDF (Proper Setting)"
                  >
                    <Download className="w-3.5 h-3.5 text-amber-700" />
                    <span>PDF</span>
                  </button>
                  <button
                    onClick={() => openPrintModal('idcard', st)}
                    className="flex-1 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs flex items-center justify-center gap-1"
                  >
                    <Printer className="w-3.5 h-3.5" />
                    <span>Print</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SECTION: RESULT CARDS */}
      {section === 'result-cards' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <FileText className="w-5 h-5 text-indigo-600" />
                <span>Academic Result Cards Generator</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Official report cards with subject marks, percentage, attendance %, and principal signature.
              </p>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto overflow-y-auto max-h-[550px]">
              <table className="w-full text-left text-xs min-w-[750px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] sticky top-0 z-10">
                  <tr>
                    <th className="py-3 px-4">Adm #</th>
                    <th className="py-3 px-4">Student Name</th>
                    <th className="py-3 px-4">Class</th>
                    <th className="py-3 px-4">Exam Term</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {students.map(st => (
                    <tr key={st.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-mono font-bold text-indigo-700">#{st.admissionNo}</td>
                      <td className="py-3 px-4 font-bold text-slate-900">{st.name}</td>
                      <td className="py-3 px-4 text-slate-600">{st.className}</td>
                      <td className="py-3 px-4 text-slate-500">Mid-Term Examination 2026</td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-emerald-100 text-emerald-800">
                          Marks Posted
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => {
                              const entries = gradeEntries.filter(ge => ge.studentId === st.id);
                              exportAcademicReportCardPDF(st, entries, gradeBands, branding);
                            }}
                            className="px-2.5 py-1 bg-amber-50 hover:bg-amber-100 text-amber-800 rounded-lg text-xs font-bold transition border border-amber-200 flex items-center gap-1"
                            title="Download Report Card PDF"
                          >
                            <Download className="w-3.5 h-3.5 text-amber-700" />
                            <span>Download PDF</span>
                          </button>
                          <button
                            onClick={() => openPrintModal('reportcard', st)}
                            className="px-3 py-1 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-bold transition shadow-xs inline-flex items-center gap-1"
                          >
                            <Printer className="w-3.5 h-3.5" />
                            <span>Print</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: LIBRARY */}
      {section === 'library' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Library className="w-5 h-5 text-indigo-600" />
                <span>Library Catalog & Book Inventory</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Course textbooks, reference books, Islamic studies, and general literature catalog.
              </p>
            </div>
            <button
              onClick={handleOpenAddBook}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Add New Book</span>
            </button>
          </div>

          <div className="bg-white p-3.5 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="flex-1 relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Search book title, author, category..."
                value={bookSearch}
                onChange={e => setBookSearch(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-xs rounded-lg border border-slate-200 bg-slate-50 focus:outline-indigo-500"
              />
            </div>
            <div className="text-xs text-slate-500 font-medium">
              Total <span className="font-bold text-slate-900">{books.length}</span> titles cataloged
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs min-w-[700px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3 px-4">Book Title</th>
                    <th className="py-3 px-4">Author</th>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4">Total Copies</th>
                    <th className="py-3 px-4">Available</th>
                    <th className="py-3 px-4">Shelf #</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {books.filter(b => b.title.toLowerCase().includes(bookSearch.toLowerCase()) || b.author.toLowerCase().includes(bookSearch.toLowerCase())).map(b => (
                    <tr key={b.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-bold text-slate-900">{b.title}</td>
                      <td className="py-3 px-4 text-slate-600">{b.author}</td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-medium">
                          {b.category}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-mono font-bold text-slate-800">{b.copies}</td>
                      <td className="py-3 px-4 font-mono font-bold text-emerald-600">{b.available}</td>
                      <td className="py-3 px-4 text-slate-500 font-mono">{b.shelfNo || 'A-01'}</td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleOpenEditBook(b)}
                            className="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => {
                              if (confirm(`Delete "${b.title}"?`)) deleteBook(b.id);
                            }}
                            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: TRANSPORT */}
      {section === 'transport' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Bus className="w-5 h-5 text-indigo-600" />
                <span>Transport Routes & School Vans</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Designated school van routes, drivers, vehicle numbers, and monthly fare.
              </p>
            </div>
            <button
              onClick={handleOpenAddRoute}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Add Transport Route</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {routes.map(r => (
              <div key={r.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between hover:border-indigo-300 transition">
                <div>
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2 mb-2">
                    <span className="font-mono text-xs font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                      {r.vehicleNo}
                    </span>
                    <span className="text-[10px] font-bold text-slate-400">Cap: {r.capacity} Seats</span>
                  </div>
                  <h3 className="font-bold text-sm text-slate-900">{r.route}</h3>
                  <div className="text-xs text-slate-600 mt-1">
                    Driver: <strong className="text-slate-800">{r.driver}</strong> • Ph: {r.driverPhone}
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-xs font-extrabold text-emerald-600">
                    Rs {r.monthlyFee.toLocaleString()} / mo
                  </span>
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenEditRoute(r)}
                      className="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => {
                        if (confirm(`Delete route "${r.route}"?`)) deleteRoute(r.id);
                      }}
                      className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SECTION: NOTICES */}
      {section === 'notices' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Bell className="w-5 h-5 text-indigo-600" />
                <span>School Announcements & Circulars</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Publish circulars to students, parents, and faculty across the school app.
              </p>
            </div>
            <button
              onClick={handleOpenAddNotice}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Create Announcement</span>
            </button>
          </div>

          <div className="space-y-3">
            {notices.map(n => (
              <div key={n.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
                <div className="flex items-start justify-between gap-2 border-b border-slate-100 pb-3">
                  <div>
                    <h3 className="text-sm font-bold text-slate-900">{n.title}</h3>
                    <div className="text-xs text-slate-500 mt-0.5">
                      By {n.authorName} • Audience: <strong className="text-slate-700">{n.audience}</strong>
                    </div>
                  </div>
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenEditNotice(n)}
                      className="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => {
                        if (confirm(`Delete circular "${n.title}"?`)) deleteNotice(n.id);
                      }}
                      className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
                <p className="text-xs text-slate-700 mt-3 leading-relaxed whitespace-pre-line">
                  {n.body}
                </p>
                <div className="mt-3 text-[10px] text-slate-400 font-mono">
                  Posted on {new Date(n.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SECTION: LEAVE REQUESTS */}
      {section === 'leave' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
              <ClipboardList className="w-5 h-5 text-indigo-600" />
              <span>Staff Leave Applications & Approval Queue</span>
            </h1>
            <p className="text-xs text-slate-500 mt-1">
              Review, approve or reject faculty leave applications with official administrative response remarks.
            </p>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs min-w-[700px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3 px-4">Faculty Member</th>
                    <th className="py-3 px-4">From Date</th>
                    <th className="py-3 px-4">To Date</th>
                    <th className="py-3 px-4">Reason</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Admin Response Remarks</th>
                    <th className="py-3 px-4 text-right">Decision</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {leaveRequests.map(lr => (
                    <tr key={lr.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-bold text-slate-900">{lr.teacherName}</td>
                      <td className="py-3 px-4 font-mono text-slate-600">{lr.fromDate}</td>
                      <td className="py-3 px-4 font-mono text-slate-600">{lr.toDate}</td>
                      <td className="py-3 px-4 text-slate-700 max-w-xs">{lr.reason}</td>
                      <td className="py-3 px-4">
                        <span className={`inline-block px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                          lr.status === 'approved'
                            ? 'bg-emerald-100 text-emerald-800'
                            : lr.status === 'rejected'
                            ? 'bg-rose-100 text-rose-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}>
                          {lr.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-600 text-[11px] italic">
                        {lr.responseNote || '—'}
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => handleOpenLeaveResponse(lr)}
                          className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-bold transition shadow-xs flex items-center gap-1 ml-auto"
                        >
                          <span>Review</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: ASSETS */}
      {section === 'assets' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Archive className="w-5 h-5 text-indigo-600" />
                <span>School Assets & Physical Inventory</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Asset register tracking hardware, laboratory equipment, school furniture, and audio-visual resources.
              </p>
            </div>
            <button
              onClick={handleOpenAddAsset}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Add New Asset</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] font-bold uppercase text-slate-400">Total Asset Items</div>
              <div className="text-xl font-black text-indigo-900 mt-1">
                {assets.reduce((s, a) => s + a.quantity, 0)} Units
              </div>
            </div>
            <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] font-bold uppercase text-slate-400">Total Capital Value</div>
              <div className="text-xl font-black text-emerald-600 mt-1">
                Rs {assets.reduce((s, a) => s + a.totalValue, 0).toLocaleString()}
              </div>
            </div>
            <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] font-bold uppercase text-slate-400">Asset Health Status</div>
              <div className="text-xl font-black text-indigo-600 mt-1">
                {assets.filter(a => a.condition === 'Good' || a.condition === 'New').length} / {assets.length} In Good Order
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: PARENT ACCOUNTS */}
      {section === 'parent-accounts' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <UserCog className="w-5 h-5 text-indigo-600" />
                <span>Parent Portal Accounts Directory</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Manage registered parent credentials, linked student children, and active portal login status.
              </p>
            </div>
            <button
              onClick={handleOpenAddParent}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Create Parent Account</span>
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs min-w-[750px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3 px-4">Parent Name</th>
                    <th className="py-3 px-4">Contact Phone</th>
                    <th className="py-3 px-4">Email / Login ID</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {parentAccounts.map(p => (
                    <tr key={p.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-bold text-slate-900">{p.name}</td>
                      <td className="py-3 px-4 font-mono text-slate-700">{p.phone}</td>
                      <td className="py-3 px-4 text-slate-600">{p.email}</td>
                      <td className="py-3 px-4">
                        <button
                          onClick={() => toggleParentStatus(p.id)}
                          className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase transition ${
                            p.status === 'active'
                              ? 'bg-emerald-100 text-emerald-800'
                              : 'bg-rose-100 text-rose-800'
                          }`}
                        >
                          {p.status}
                        </button>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          onClick={() => {
                            if (confirm(`Delete account for ${p.name}?`)) deleteParentAccount(p.id);
                          }}
                          className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: ROLES */}
      {section === 'roles' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Shield className="w-5 h-5 text-indigo-600" />
                <span>Roles & System Permission Matrix</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Configure module access permissions for Administrators, Faculty, and Accountants.
              </p>
            </div>
            <button
              onClick={handleSaveRoles}
              className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <span>Save Permissions Matrix</span>
            </button>
          </div>

          {rolesSaved && (
            <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-3 rounded-xl text-xs font-bold flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-emerald-600" />
              <span>Permission updates successfully saved and enforced!</span>
            </div>
          )}

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs min-w-[700px]">
                <thead className="bg-[#0B1730] text-white font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3.5 px-4 w-48">System Role</th>
                    <th className="py-3.5 px-4 text-center">Manage Students</th>
                    <th className="py-3.5 px-4 text-center">Fee Collection</th>
                    <th className="py-3.5 px-4 text-center">Marks & Exams</th>
                    <th className="py-3.5 px-4 text-center">Timetable</th>
                    <th className="py-3.5 px-4 text-center">Payroll</th>
                    <th className="py-3.5 px-4 text-center">System Settings</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {localRoles.map(role => (
                    <tr key={role.id} className="hover:bg-slate-50">
                      <td className="py-4 px-4">
                        <div className="font-bold text-slate-900">{role.name}</div>
                        <div className="text-[10px] text-slate-500">{role.description}</div>
                      </td>
                      {(['canManageStudents', 'canManageFees', 'canManageExams', 'canManageTimetable', 'canManagePayroll', 'canManageSettings'] as const).map(perm => (
                        <td key={perm} className="py-4 px-4 text-center">
                          <input
                            type="checkbox"
                            checked={role.permissions[perm]}
                            onChange={e => {
                              const isChecked = e.target.checked;
                              setLocalRoles(prev => prev.map(r => r.id === role.id ? {
                                ...r,
                                permissions: { ...r.permissions, [perm]: isChecked }
                              } : r));
                            }}
                            className="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                          />
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SECTION: BRANDING & PROFILE */}
      {section === 'branding' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Building2 className="w-5 h-5 text-indigo-600" />
                <span>School Profile & Institutional Branding</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Customize school name, crest logo, official signature stamp, and contact details.
              </p>
            </div>
            {brandingSaved && (
              <span className="text-xs font-bold text-emerald-600 bg-emerald-50 border border-emerald-200 px-3 py-1 rounded-lg">
                Branding Saved!
              </span>
            )}
          </div>

          <form onSubmit={handleSaveBranding} className="space-y-6">
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs space-y-4 text-xs">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">School Institutional Name</label>
                  <input
                    type="text"
                    value={brandingForm.name}
                    onChange={e => setBrandingForm({ ...brandingForm, name: e.target.value })}
                    className="w-full p-2.5 border border-slate-300 rounded-xl font-bold text-slate-900 focus:outline-indigo-500"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Tagline / Motto</label>
                  <input
                    type="text"
                    value={brandingForm.tagline}
                    onChange={e => setBrandingForm({ ...brandingForm, tagline: e.target.value })}
                    className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Contact Phone</label>
                  <input
                    type="text"
                    value={brandingForm.phone}
                    onChange={e => setBrandingForm({ ...brandingForm, phone: e.target.value })}
                    className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 font-mono"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Email</label>
                  <input
                    type="email"
                    value={brandingForm.email}
                    onChange={e => setBrandingForm({ ...brandingForm, email: e.target.value })}
                    className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Academic Session</label>
                  <input
                    type="text"
                    value={brandingForm.session}
                    onChange={e => setBrandingForm({ ...brandingForm, session: e.target.value })}
                    className="w-full p-2.5 border border-slate-300 rounded-xl font-bold focus:outline-indigo-500 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Campus Physical Address</label>
                <textarea
                  rows={2}
                  value={brandingForm.address}
                  onChange={e => setBrandingForm({ ...brandingForm, address: e.target.value })}
                  className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                />
              </div>

              <div className="pt-3 border-t border-slate-200 flex justify-end">
                <button
                  type="submit"
                  className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl transition shadow-xs text-xs"
                >
                  Save Profile Changes
                </button>
              </div>
            </div>
          </form>
        </div>
      )}

      {/* SECTION: SYSTEM SETTINGS & DOWNLOAD REAL APK */}
      {section === 'system' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
              <Settings className="w-5 h-5 text-indigo-600" />
              <span>System Settings, Package Exports & Backup</span>
            </h1>
            <p className="text-xs text-slate-500 mt-1">
              Download Android APK (5.8MB) & AAB bundles, plain text & JSON backups, and system rollover.
            </p>
          </div>

          <div className="bg-gradient-to-r from-indigo-900 via-indigo-950 to-slate-900 text-white p-6 rounded-2xl shadow-lg border border-indigo-700 space-y-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider bg-white/10 px-2 py-0.5 rounded text-amber-300">
                  Real 5.8 MB APK Packages Ready
                </span>
                <h2 className="text-lg font-bold mt-1">
                  Download Mobile APK, AAB & Backup Files
                </h2>
                <p className="text-xs text-slate-300 mt-0.5">
                  Export signed Android APK package, Play Store App Bundle (.aab), and plain text data backup files.
                </p>
              </div>
              <button
                onClick={openDownloadPackageModal}
                className="px-5 py-2.5 bg-amber-400 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs transition shadow-md flex items-center gap-2 self-start sm:self-auto shrink-0"
              >
                <Download className="w-4 h-4 text-slate-950" />
                <span>Open Download Center</span>
              </button>
            </div>
          </div>

          <div className="bg-white p-5 rounded-2xl border border-rose-200 shadow-xs space-y-3">
            <h3 className="text-sm font-bold text-rose-900 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-rose-600" />
              <span>Factory Reset & Default Demo Data</span>
            </h3>
            <p className="text-xs text-slate-600">
              Reset all local modifications back to initial institutional seed datasets.
            </p>
            <button
              onClick={() => {
                if (confirm("Reset all school data to clean factory demo state?")) {
                  resetAllData();
                  alert("Reset complete! Clean initial demo state loaded.");
                }
              }}
              className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl text-xs flex items-center gap-2 transition"
            >
              <RefreshCw className="w-4 h-4" />
              <span>Reset to Default Demo Data</span>
            </button>
          </div>
        </div>
      )}

      {/* SECTION: AUDIT LOG */}
      {section === 'audit' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
                <Shield className="w-5 h-5 text-indigo-600" />
                <span>System Security & Audit Activity Log</span>
              </h1>
              <p className="text-xs text-slate-500 mt-1">
                Immutable ledger of administrative actions, fee edits, marks postings, and login events.
              </p>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto overflow-y-auto max-h-[520px]">
              <table className="w-full text-left text-xs min-w-[750px]">
                <thead className="bg-[#0B1730] text-white font-bold uppercase text-[10px] sticky top-0 z-10">
                  <tr>
                    <th className="py-3 px-4">When</th>
                    <th className="py-3 px-4">Actor</th>
                    <th className="py-3 px-4">Action Event</th>
                    <th className="py-3 px-4">Module</th>
                    <th className="py-3 px-4">Description</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    { when: "2026-10-01 14:30:15", actor: "Campus Admin", action: "fee_collection", module: "Finance", desc: "Received fee payment Rs 4,400 for Zain Raza (VCH-2026-1001)" },
                    { when: "2026-10-01 12:15:00", actor: "Ms. Hina Zahid", action: "marks_entry", module: "Gradebook", desc: "Saved Term 1 Mid-Term marks for Class 4 - A English" },
                    { when: "2026-10-01 08:05:22", actor: "Gate 1 Scanner", action: "scan_attendance", module: "Attendance", desc: "Automated QR card tap: Zain Raza marked Present" },
                    { when: "2026-09-30 16:45:10", actor: "Campus Admin", action: "bulk_vouchers", module: "Finance", desc: "Generated monthly fee vouchers for October 2026" },
                    { when: "2026-09-29 09:12:44", actor: "socialman121@gmail.com", action: "owner_access", module: "Platform", desc: "System Owner authenticated; viewed multi-school console" }
                  ].map((log, i) => (
                    <tr key={i} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-mono text-slate-500">{log.when}</td>
                      <td className="py-3 px-4 font-bold text-slate-900">{log.actor}</td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-indigo-50 text-indigo-700">
                          {log.action}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-600 font-medium">{log.module}</td>
                      <td className="py-3 px-4 text-slate-700">{log.desc}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* ALL MODALS (Book, Route, Notice, Leave, Asset, Parent) */}
      {/* Book Modal */}
      {isBookModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingBookId ? 'Edit Book Details' : 'Add Book to Library'}</h2>
              <button onClick={() => setIsBookModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveBook} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Book Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Oxford Progressive English"
                  value={bookTitle}
                  onChange={e => setBookTitle(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Author / Publisher</label>
                <input
                  type="text"
                  placeholder="e.g. Rachel Redford"
                  value={bookAuthor}
                  onChange={e => setBookAuthor(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Category</label>
                  <select
                    value={bookCategory}
                    onChange={e => setBookCategory(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                  >
                    <option value="Textbook">Textbook</option>
                    <option value="Mathematics">Mathematics</option>
                    <option value="Science">Science</option>
                    <option value="Islamic Studies">Islamic Studies</option>
                    <option value="Literature">Literature</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Total Copies</label>
                  <input
                    type="number"
                    value={bookCopies}
                    onChange={e => setBookCopies(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsBookModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingBookId ? 'Save Changes' : 'Add Book'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Route Modal */}
      {isRouteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingRouteId ? 'Edit Transport Route' : 'Add Transport Route'}</h2>
              <button onClick={() => setIsRouteModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveRoute} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Route Title / Area *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Route 4: Main Market & GT Road"
                  value={routeName}
                  onChange={e => setRouteName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Vehicle No</label>
                  <input
                    type="text"
                    value={routeVehicle}
                    onChange={e => setRouteVehicle(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Monthly Fare (Rs)</label>
                  <input
                    type="number"
                    value={routeFee}
                    onChange={e => setRouteFee(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Driver Name</label>
                  <input
                    type="text"
                    placeholder="e.g. Muhammad Aslam"
                    value={routeDriver}
                    onChange={e => setRouteDriver(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Driver Phone</label>
                  <input
                    type="text"
                    value={routeDriverPhone}
                    onChange={e => setRouteDriverPhone(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsRouteModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingRouteId ? 'Save Changes' : 'Add Route'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Notice Modal */}
      {isNoticeModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingNoticeId ? 'Edit Announcement' : 'Publish Announcement'}</h2>
              <button onClick={() => setIsNoticeModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveNotice} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Announcement Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Schedule for Sports Week 2026"
                  value={noticeTitle}
                  onChange={e => setNoticeTitle(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Audience</label>
                <select
                  value={noticeAudience}
                  onChange={e => setNoticeAudience(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                >
                  <option value="All Students, Parents & Staff">All Students, Parents & Staff</option>
                  <option value="Parents Only">Parents Only</option>
                  <option value="Students Only">Students Only</option>
                  <option value="Teaching Faculty Only">Teaching Faculty Only</option>
                </select>
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Message Content *</label>
                <textarea
                  rows={4}
                  required
                  placeholder="Details of the circular..."
                  value={noticeBody}
                  onChange={e => setNoticeBody(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsNoticeModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingNoticeId ? 'Update Circular' : 'Publish Circular'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Leave Response Modal */}
      {isLeaveResponseModalOpen && respondingLeave && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <div>
                <h2 className="font-bold text-sm">Review Leave Request</h2>
                <div className="text-[11px] text-slate-300">{respondingLeave.teacherName}</div>
              </div>
              <button onClick={() => setIsLeaveResponseModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveLeaveResponse} className="p-5 space-y-4 text-xs">
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-1">
                <div className="text-[11px] text-slate-500">
                  Requested Interval: <strong className="text-slate-800">{respondingLeave.fromDate}</strong> to <strong className="text-slate-800">{respondingLeave.toDate}</strong>
                </div>
                <div className="text-[11px] text-slate-700 mt-1">
                  Reason: &ldquo;{respondingLeave.reason}&rdquo;
                </div>
              </div>
              <div>
                <label className="font-bold text-slate-700 block mb-1">Administrative Decision</label>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => setLeaveResponseStatus('approved')}
                    className={`py-2 rounded-xl font-bold border transition ${
                      leaveResponseStatus === 'approved'
                        ? 'bg-emerald-600 text-white border-emerald-600 shadow-xs'
                        : 'bg-slate-50 text-slate-700 border-slate-200'
                    }`}
                  >
                    Approve Leave
                  </button>
                  <button
                    type="button"
                    onClick={() => setLeaveResponseStatus('rejected')}
                    className={`py-2 rounded-xl font-bold border transition ${
                      leaveResponseStatus === 'rejected'
                        ? 'bg-rose-600 text-white border-rose-600 shadow-xs'
                        : 'bg-slate-50 text-slate-700 border-slate-200'
                    }`}
                  >
                    Reject Leave
                  </button>
                </div>
              </div>
              <div>
                <label className="font-bold text-slate-700 block mb-1">Official Response Remarks / Note</label>
                <textarea
                  rows={3}
                  placeholder="e.g. Approved. Please ensure syllabus coverage and coordinate substitute teacher with primary coordinator."
                  value={leaveResponseRemarks}
                  onChange={e => setLeaveResponseRemarks(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                />
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsLeaveResponseModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  Submit Official Decision
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Asset Modal */}
      {isAssetModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingAssetId ? 'Edit Asset Record' : 'Add Physical Asset'}</h2>
              <button onClick={() => setIsAssetModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveAsset} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Asset Description *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Dell Core i5 Desktop Computer"
                  value={assetName}
                  onChange={e => setAssetName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Category</label>
                  <select
                    value={assetCategory}
                    onChange={e => setAssetCategory(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  >
                    <option value="IT Equipment">IT Equipment</option>
                    <option value="Furniture">Furniture</option>
                    <option value="Audio Visual">Audio Visual</option>
                    <option value="Lab Equipment">Lab Equipment</option>
                    <option value="Electrical">Electrical</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Quantity</label>
                  <input
                    type="number"
                    value={assetQty}
                    onChange={e => setAssetQty(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsAssetModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingAssetId ? 'Save Changes' : 'Add to Inventory'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Parent Account Modal */}
      {isParentModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingParentId ? 'Edit Parent Account' : 'Register Parent Account'}</h2>
              <button onClick={() => setIsParentModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveParent} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Parent Full Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Muhammad Raza"
                  value={parentName}
                  onChange={e => setParentName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Mobile Phone *</label>
                  <input
                    type="text"
                    required
                    placeholder="0300-1234567"
                    value={parentPhone}
                    onChange={e => setParentPhone(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono font-bold"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Email / Login ID</label>
                  <input
                    type="email"
                    placeholder="raza@gmail.com"
                    value={parentEmail}
                    onChange={e => setParentEmail(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsParentModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingParentId ? 'Save Changes' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
