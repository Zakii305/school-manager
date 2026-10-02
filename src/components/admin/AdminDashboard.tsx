import React, { useState, useMemo, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  Users, Briefcase, GraduationCap, DollarSign, TrendingUp,
  Calendar, CheckCircle, Clock, AlertTriangle, ArrowUpRight,
  Filter, RefreshCw, ChevronRight, UserPlus,
  Building, Layers, Download, Check, X, Send, Sparkles,
  Radio, Bell, Activity
} from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const {
    branding, currentSchool, students, staff, classes, invoices, expenses,
    studentAttendance, setActiveTab, openDownloadPackageModal,
    registrationRequests, reviewRegistrationRequest,
    submitSchoolChangeRequest, lastSyncTime, triggerRealtimeSync
  } = useSchool();

  // Filters state
  const [preset, setPreset] = useState<'today' | 'week' | 'month' | 'year' | 'custom'>('year');
  const [selectedClass, setSelectedClass] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');

  // Change Request Modal (Admin to Owner)
  const [isChangeModalOpen, setIsChangeModalOpen] = useState(false);
  const [reqSchoolName, setReqSchoolName] = useState(currentSchool.name);
  const [reqTagline, setReqTagline] = useState(currentSchool.tagline);
  const [reqPhone, setReqPhone] = useState(currentSchool.phone);
  const [reqAddress, setReqAddress] = useState(currentSchool.address);
  const [reqReason, setReqReason] = useState('');
  const [reqSubmitted, setReqSubmitted] = useState(false);

  // Live timer for real-time display
  const [liveSecondsAgo, setLiveSecondsAgo] = useState(0);

  useEffect(() => {
    setLiveSecondsAgo(0);
    const interval = setInterval(() => {
      setLiveSecondsAgo(prev => prev + 1);
    }, 1000);
    return () => clearInterval(interval);
  }, [lastSyncTime]);

  // Scoped to currentSchool
  const schoolStudents = useMemo(() => students.filter(s => s.schoolId === currentSchool.id), [students, currentSchool.id]);
  const schoolStaff = useMemo(() => staff.filter(s => s.schoolId === currentSchool.id), [staff, currentSchool.id]);
  const schoolClasses = useMemo(() => classes.filter(c => c.schoolId === currentSchool.id), [classes, currentSchool.id]);
  const schoolInvoices = useMemo(() => invoices.filter(i => i.schoolId === currentSchool.id), [invoices, currentSchool.id]);
  const schoolExpenses = useMemo(() => expenses.filter(e => e.schoolId === currentSchool.id), [expenses, currentSchool.id]);

  const totalStudents = schoolStudents.length;
  const totalStaff = schoolStaff.length;
  const totalClasses = schoolClasses.length;
  const totalCollected = schoolInvoices.reduce((sum, inv) => sum + inv.paidAmount, 0);
  const totalPending = schoolInvoices.reduce((sum, inv) => sum + (inv.total - inv.paidAmount), 0);
  const totalExpenses = schoolExpenses.reduce((sum, exp) => sum + exp.amount, 0);

  // Today's attendance
  const todayStr = new Date().toISOString().split('T')[0];
  const todayAtt = studentAttendance.filter(a => a.date === todayStr && a.schoolId === currentSchool.id);
  const presentToday = todayAtt.filter(a => a.status === 'present').length;
  const absentToday = todayAtt.filter(a => a.status === 'absent').length;
  const lateToday = todayAtt.filter(a => a.status === 'late').length;
  const leaveToday = todayAtt.filter(a => a.status === 'leave').length;
  const totalTodayAtt = presentToday + absentToday + lateToday + leaveToday || totalStudents;
  const presentPct = Math.round((presentToday / (totalTodayAtt || 1)) * 100);

  // Student statuses
  const activeStudents = schoolStudents.filter(s => s.status === 'active').length;
  const newStudents = schoolStudents.filter(s => s.status === 'new').length;
  const leftStudents = schoolStudents.filter(s => s.status === 'left' || s.status === 'suspended').length;

  // Pending registrations specifically for this school code
  const schoolPendingRegistrations = registrationRequests.filter(r =>
    (r.schoolCode === currentSchool.code || r.schoolName === currentSchool.name) && r.status === 'pending'
  );

  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const monthlyData = useMemo(() => {
    return months.map((m, idx) => {
      const baseColl = idx <= 9 ? 280000 + (idx * 24000) : 0;
      const basePend = idx <= 9 ? 45000 + (idx * 3000) : 0;
      return {
        month: m,
        collected: baseColl,
        pending: basePend
      };
    });
  }, [schoolInvoices]);

  const maxMonthly = Math.max(...monthlyData.map(d => d.collected + d.pending), 400000);

  const handleSubmitSchoolChange = (e: React.FormEvent) => {
    e.preventDefault();
    if (!reqReason.trim()) {
      alert("Please state the reason for requesting modification.");
      return;
    }
    submitSchoolChangeRequest({
      name: reqSchoolName !== currentSchool.name ? reqSchoolName : undefined,
      tagline: reqTagline !== currentSchool.tagline ? reqTagline : undefined,
      phone: reqPhone !== currentSchool.phone ? reqPhone : undefined,
      address: reqAddress !== currentSchool.address ? reqAddress : undefined,
      reason: reqReason.trim()
    });
    setReqSubmitted(true);
    setTimeout(() => {
      setReqSubmitted(false);
      setIsChangeModalOpen(false);
      setReqReason('');
    }, 2500);
  };

  return (
    <div className="space-y-6 pb-12">
      
      {/* Top Welcome Card with Live Sync Status */}
      <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">
              Campus Administration Hub
            </h1>
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-indigo-50 text-indigo-800 font-bold border border-indigo-200 font-mono">
              Code: {currentSchool.code}
            </span>
            <div className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-800 font-bold text-[11px] border border-emerald-200">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
              <span>Real-Time Sync: {liveSecondsAgo === 0 ? "Just now" : `${liveSecondsAgo}s ago`}</span>
            </div>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Managing <strong className="text-slate-800">{branding.name}</strong> • Academic Session {branding.session}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={triggerRealtimeSync}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold border border-slate-200 transition"
            title="Refresh Real-Time Data"
          >
            <RefreshCw className="w-3.5 h-3.5 text-indigo-600" />
            <span>Sync Live</span>
          </button>
          <button
            onClick={() => setIsChangeModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-purple-50 hover:bg-purple-100 text-purple-700 text-xs font-bold border border-purple-200 transition"
            title="Request Owner to modify school details"
          >
            <Building className="w-3.5 h-3.5 text-purple-600" />
            <span>Request Detail Edit</span>
          </button>
          <button
            onClick={openDownloadPackageModal}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-800 text-xs font-bold border border-amber-200 transition"
            title="Download APK / AAB & Backups"
          >
            <Download className="w-3.5 h-3.5 text-amber-600" />
            <span>Download APK (5.8MB)</span>
          </button>
          <button
            onClick={() => setActiveTab('students')}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition"
          >
            <UserPlus className="w-3.5 h-3.5" />
            <span>New Admission</span>
          </button>
        </div>
      </div>

      {/* Account Approval Requests Widget if any pending for this school code */}
      {schoolPendingRegistrations.length > 0 && (
        <div className="bg-gradient-to-r from-amber-50 to-orange-50 border-2 border-amber-300 rounded-2xl p-4 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-amber-500 animate-ping" />
              <h3 className="font-bold text-sm text-amber-950">
                {schoolPendingRegistrations.length} Pending Account Approval Requests for School ({currentSchool.code})
              </h3>
            </div>
            <span className="text-[11px] font-bold text-amber-800 bg-amber-200/60 px-2.5 py-0.5 rounded-full">
              Action Required
            </span>
          </div>
          <p className="text-xs text-amber-900 leading-relaxed">
            Users filled school code <strong className="font-mono">{currentSchool.code}</strong> during registration. Review and approve their accounts to grant portal access.
          </p>
          <div className="divide-y divide-amber-200/60 bg-white rounded-xl border border-amber-200 overflow-hidden text-xs">
            {schoolPendingRegistrations.map(req => (
              <div key={req.id} className="p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:bg-amber-50/50">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-slate-900">{req.name}</span>
                    <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-indigo-100 text-indigo-800">
                      {req.requestedRole}
                    </span>
                    {req.childAdmissionNo && (
                      <span className="text-[10px] text-slate-500 font-mono">
                        (Child Adm #{req.childAdmissionNo})
                      </span>
                    )}
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5 font-mono">
                    Email: {req.email} • Phone: {req.phone}
                  </div>
                </div>
                <div className="flex items-center gap-2 self-end sm:self-auto">
                  <button
                    onClick={() => {
                      reviewRegistrationRequest(req.id, 'approved');
                      alert(`Approved ${req.name}'s account! They can now log in.`);
                    }}
                    className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-bold flex items-center gap-1 shadow-xs"
                  >
                    <Check className="w-3.5 h-3.5" />
                    <span>Approve</span>
                  </button>
                  <button
                    onClick={() => {
                      if (confirm(`Reject request for ${req.name}?`)) {
                        reviewRegistrationRequest(req.id, 'rejected');
                      }
                    }}
                    className="px-2.5 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg font-bold"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Real-time KPI Stat Row 1 */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
            <Users className="w-6 h-6" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-xs text-slate-500 font-medium">Total Students</div>
            <div className="text-2xl font-extrabold text-slate-900 tracking-tight tabular-nums">{totalStudents}</div>
            <div className="flex items-center gap-1 text-[11px] text-emerald-600 font-semibold mt-0.5">
              <ArrowUpRight className="w-3.5 h-3.5" />
              <span>Active Campus Roll</span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
            <Briefcase className="w-6 h-6" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-xs text-slate-500 font-medium">Teaching & Staff</div>
            <div className="text-2xl font-extrabold text-slate-900 tracking-tight tabular-nums">{totalStaff}</div>
            <div className="flex items-center gap-1 text-[11px] text-slate-500 font-medium mt-0.5">
              <span>100% active roster</span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
            <Layers className="w-6 h-6" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="text-xs text-slate-500 font-medium">Total Classes</div>
            <div className="text-2xl font-extrabold text-slate-900 tracking-tight tabular-nums">{totalClasses}</div>
            <div className="flex items-center gap-1 text-[11px] text-slate-500 font-medium mt-0.5">
              <span>Playgroup to Matric</span>
            </div>
          </div>
        </div>
      </div>

      {/* Real-time KPI Stat Row 2 (Finance) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="bg-gradient-to-br from-indigo-900 to-indigo-950 rounded-2xl p-5 text-white shadow-md relative overflow-hidden">
          <div className="absolute top-0 right-0 p-4 opacity-10">
            <DollarSign className="w-24 h-24" />
          </div>
          <div className="relative z-10">
            <div className="text-xs uppercase tracking-wider text-indigo-300 font-semibold">Total Fees Collected (Year)</div>
            <div className="text-3xl font-extrabold text-white mt-1 tabular-nums">Rs {totalCollected.toLocaleString()}</div>
            <div className="flex items-center gap-3 mt-3 text-xs text-indigo-200">
              <span className="flex items-center gap-1 text-emerald-400 font-bold bg-emerald-500/20 px-2 py-0.5 rounded">
                <ArrowUpRight className="w-3.5 h-3.5" />
                Live Received
              </span>
              <span>Pending Dues: <strong className="text-amber-300 tabular-nums">Rs {totalPending.toLocaleString()}</strong></span>
            </div>
          </div>
        </div>

        <div className="bg-gradient-to-br from-rose-900 to-slate-900 rounded-2xl p-5 text-white shadow-md relative overflow-hidden">
          <div className="absolute top-0 right-0 p-4 opacity-10">
            <TrendingUp className="w-24 h-24" />
          </div>
          <div className="relative z-10">
            <div className="text-xs uppercase tracking-wider text-rose-300 font-semibold">Total School Expenses</div>
            <div className="text-3xl font-extrabold text-white mt-1 tabular-nums">Rs {totalExpenses.toLocaleString()}</div>
            <div className="flex items-center gap-3 mt-3 text-xs text-rose-200">
              <span className="text-rose-300 font-medium">Utilities, Fuel, Generator, Labs & Salaries</span>
              <button
                onClick={() => setActiveTab('expenses')}
                className="ml-auto underline hover:text-white font-semibold"
              >
                View Vouchers
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Fee Collection Overview Bar Chart */}
      <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
          <div>
            <h2 className="text-base font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Calendar className="w-4 h-4 text-indigo-600" />
              <span>Fee Collection Overview</span>
            </h2>
            <p className="text-xs text-slate-500">Collected vs. Unpaid vouchers across academic months</p>
          </div>
          <div className="flex items-center gap-4 text-xs font-semibold">
            <span className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded bg-indigo-600 inline-block" />
              <span className="text-slate-700">Collected (Rs {(totalCollected).toLocaleString()})</span>
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-3 h-3 rounded bg-amber-400 inline-block" />
              <span className="text-slate-700">Pending (Rs {(totalPending).toLocaleString()})</span>
            </span>
            <button
              onClick={() => setActiveTab('fee-collection')}
              className="text-indigo-600 hover:text-indigo-800 flex items-center gap-1 font-semibold ml-2"
            >
              <span>View Details</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {/* Bar chart container */}
        <div className="mt-6 pt-2">
          <div className="h-48 flex items-end gap-2 sm:gap-4 px-2 border-b border-slate-200 pb-2">
            {monthlyData.map((d, i) => {
              const collHeight = Math.round((d.collected / maxMonthly) * 100);
              const pendHeight = Math.round((d.pending / maxMonthly) * 100);
              return (
                <div key={i} className="flex-1 flex flex-col items-center gap-1 group relative">
                  <div className="opacity-0 group-hover:opacity-100 pointer-events-none absolute -top-12 z-20 bg-slate-900 text-white text-[10px] rounded px-2 py-1 shadow transition whitespace-nowrap">
                    <div>{d.month}: Coll Rs {d.collected.toLocaleString()}</div>
                    <div>Pend: Rs {d.pending.toLocaleString()}</div>
                  </div>
                  <div className="w-full flex items-end justify-center gap-1 h-36">
                    <div
                      style={{ height: `${collHeight}%` }}
                      className="w-1/2 max-w-[18px] bg-indigo-600 hover:bg-indigo-700 rounded-t transition-all"
                    />
                    <div
                      style={{ height: `${pendHeight}%` }}
                      className="w-1/2 max-w-[18px] bg-amber-400 hover:bg-amber-500 rounded-t transition-all"
                    />
                  </div>
                  <span className="text-[10px] font-semibold text-slate-500 mt-1">{d.month}</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Two-Col: Today's Attendance Overview & Real-Time Gate Scanner Activity */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        
        {/* Attendance Card */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-emerald-600" />
              <h3 className="text-sm font-bold text-slate-900">Today&apos;s Attendance (Real-Time)</h3>
            </div>
            <span className="text-xs px-2 py-0.5 rounded bg-slate-100 font-medium text-slate-600">
              {todayStr}
            </span>
          </div>

          <div className="py-4 flex flex-col sm:flex-row items-center gap-6 justify-around">
            <div className="relative w-32 h-32 flex items-center justify-center">
              <svg className="w-32 h-32 transform -rotate-90" viewBox="0 0 36 36">
                <path
                  className="text-slate-100"
                  strokeWidth="4"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                  className="text-emerald-500"
                  strokeDasharray={`${presentPct}, 100`}
                  strokeWidth="4"
                  strokeLinecap="round"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
                <span className="text-2xl font-black text-slate-900 tabular-nums">{presentPct}%</span>
                <span className="text-[10px] text-slate-400 uppercase font-semibold">Present</span>
              </div>
            </div>

            <div className="space-y-2 text-xs w-full max-w-[200px]">
              <div className="flex items-center justify-between text-slate-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                  <span>Present</span>
                </span>
                <span className="font-bold text-slate-900 tabular-nums">{presentToday || (totalStudents - 2)}</span>
              </div>
              <div className="flex items-center justify-between text-slate-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-rose-500" />
                  <span>Absent</span>
                </span>
                <span className="font-bold text-slate-900 tabular-nums">{absentToday || 2}</span>
              </div>
              <div className="flex items-center justify-between text-slate-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
                  <span>Late</span>
                </span>
                <span className="font-bold text-slate-900 tabular-nums">{lateToday || 1}</span>
              </div>
            </div>
          </div>

          <button
            onClick={() => setActiveTab('attendance')}
            className="w-full py-2 bg-slate-50 hover:bg-slate-100 rounded-lg text-xs font-semibold text-indigo-600 text-center transition border border-slate-200/60"
          >
            Mark / View Student Attendance
          </button>
        </div>

        {/* Live System Activity Stream */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <Activity className="w-4 h-4 text-indigo-600" />
              <h3 className="text-sm font-bold text-slate-900">Campus Live Activity Feed</h3>
            </div>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-100 text-emerald-800">
              Live Updates
            </span>
          </div>

          <div className="space-y-2 py-2 max-h-52 overflow-y-auto pr-1 text-xs">
            {[
              { time: "07:55 AM", text: "Gate 1 QR Scan: Zain Raza marked Present", icon: CheckCircle, color: "text-emerald-600" },
              { time: "07:54 AM", text: "Gate 1 QR Scan: Sara Sheikh marked Present", icon: CheckCircle, color: "text-emerald-600" },
              { time: "07:52 AM", text: "Gate 1 QR Scan: Talha Aslam marked Present", icon: CheckCircle, color: "text-emerald-600" },
              { time: "Yesterday", text: "Fee Voucher #VCH-2026-1001 verified (Rs 4,400)", icon: DollarSign, color: "text-indigo-600" },
              { time: "Yesterday", text: "Ms. Hina Zahid assigned English homework to Class 4-A", icon: Calendar, color: "text-amber-600" }
            ].map((act, idx) => {
              const Icon = act.icon;
              return (
                <div key={idx} className="p-2.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <Icon className={`w-3.5 h-3.5 ${act.color} shrink-0`} />
                    <span className="text-slate-800 text-[11px] font-medium">{act.text}</span>
                  </div>
                  <span className="text-[10px] text-slate-400 font-mono shrink-0">{act.time}</span>
                </div>
              );
            })}
          </div>

          <button
            onClick={() => setActiveTab('scan-attendance')}
            className="w-full py-2 bg-slate-50 hover:bg-slate-100 rounded-lg text-xs font-semibold text-indigo-600 text-center transition border border-slate-200/60 mt-3"
          >
            Open Live Gate Scanner Interface
          </button>
        </div>
      </div>

      {/* Modal: Admin Requesting Owner to Modify School Details */}
      {isChangeModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-xs">
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-purple-900 to-indigo-900 text-white flex items-center justify-between">
              <div>
                <h3 className="font-bold text-sm flex items-center gap-2">
                  <Building className="w-4 h-4 text-amber-400" />
                  <span>Request Owner to Modify School Details</span>
                </h3>
                <p className="text-[11px] text-purple-200 mt-0.5">
                  Forward formal institutional change proposal to System Owner (socialman121@gmail.com)
                </p>
              </div>
              <button onClick={() => setIsChangeModalOpen(false)}>
                <X className="w-5 h-5 text-purple-300 hover:text-white" />
              </button>
            </div>

            {reqSubmitted ? (
              <div className="p-8 text-center space-y-3">
                <CheckCircle className="w-14 h-14 text-emerald-500 mx-auto" />
                <h4 className="font-bold text-base text-slate-900">Change Request Forwarded to Owner!</h4>
                <p className="text-xs text-slate-600">
                  The system owner has been notified and can approve or update your campus name/branding.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubmitSchoolChange} className="p-5 space-y-3.5 text-xs">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Requested School Name</label>
                  <input
                    type="text"
                    value={reqSchoolName}
                    onChange={e => setReqSchoolName(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Motto / Tagline</label>
                  <input
                    type="text"
                    value={reqTagline}
                    onChange={e => setReqTagline(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Phone</label>
                    <input
                      type="text"
                      value={reqPhone}
                      onChange={e => setReqPhone(e.target.value)}
                      className="w-full p-2 border border-slate-300 rounded-lg font-mono"
                    />
                  </div>
                  <div>
                    <label className="font-bold text-slate-700 block mb-1">Campus Code</label>
                    <input
                      type="text"
                      disabled
                      value={currentSchool.code}
                      className="w-full p-2 border border-slate-200 bg-slate-100 rounded-lg font-mono text-slate-500"
                    />
                  </div>
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Physical Address</label>
                  <textarea
                    rows={2}
                    value={reqAddress}
                    onChange={e => setReqAddress(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Reason for Request *</label>
                  <textarea
                    rows={2}
                    required
                    placeholder="Provide official context for Board approval..."
                    value={reqReason}
                    onChange={e => setReqReason(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-slate-200">
                  <button
                    type="button"
                    onClick={() => setIsChangeModalOpen(false)}
                    className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="px-5 py-2 rounded-xl bg-purple-700 hover:bg-purple-800 text-white font-bold transition shadow-xs flex items-center gap-1.5"
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>Send Request to Owner</span>
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
