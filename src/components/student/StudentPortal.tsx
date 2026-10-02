import React, { useState, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { DailyAyatCard } from '../common/DailyAyatCard';
import {
  CheckCircle, DollarSign, Calendar, Award, BookOpen,
  Bell, FileText, CreditCard, Printer, Download, Clock
} from 'lucide-react';
import { exportClassTimetablePDF, exportStudentIDCardPDF } from '../../utils/pdfExport';

export const StudentPortal: React.FC = () => {
  const {
    students, timetable, periodTimings, studentAttendance, gradeEntries,
    assignments, invoices, notices, branding, openPrintModal, classes,
    activeTab, setActiveTab
  } = useSchool();

  const [studentTab, setStudentTab] = useState<'dashboard' | 'timetable' | 'attendance' | 'results' | 'homework' | 'fees' | 'notices'>(() => {
    if (activeTab === 'student-timetable') return 'timetable';
    if (activeTab === 'student-attendance') return 'attendance';
    if (activeTab === 'student-results') return 'results';
    if (activeTab === 'student-homework') return 'homework';
    if (activeTab === 'student-fees') return 'fees';
    if (activeTab === 'student-notices') return 'notices';
    return 'dashboard';
  });

  // Sync internal subtab whenever the sidebar navigation changes activeTab
  useEffect(() => {
    if (activeTab === 'student-timetable') setStudentTab('timetable');
    else if (activeTab === 'student-attendance') setStudentTab('attendance');
    else if (activeTab === 'student-results') setStudentTab('results');
    else if (activeTab === 'student-homework') setStudentTab('homework');
    else if (activeTab === 'student-fees') setStudentTab('fees');
    else if (activeTab === 'student-notices') setStudentTab('notices');
    else if (activeTab === 'dashboard') setStudentTab('dashboard');
  }, [activeTab]);

  const handleTabClick = (tab: 'dashboard' | 'timetable' | 'attendance' | 'results' | 'homework' | 'fees' | 'notices') => {
    setStudentTab(tab);
    if (tab === 'timetable') setActiveTab('student-timetable');
    else if (tab === 'attendance') setActiveTab('student-attendance');
    else if (tab === 'results') setActiveTab('student-results');
    else if (tab === 'homework') setActiveTab('student-homework');
    else if (tab === 'fees') setActiveTab('student-fees');
    else if (tab === 'notices') setActiveTab('student-notices');
    else setActiveTab('dashboard');
  };

  const student = students.find(s => s.name.toLowerCase().includes('zain') || s.id === 'stu-1') || students[0];
  const studentClass = classes.find(c => c.id === student.classId) || classes[0];

  // Attendance stats
  const myAttendance = studentAttendance.filter(a => a.studentId === student.id);
  const presentCount = myAttendance.filter(a => a.status === 'present').length;
  const totalDays = myAttendance.length || 24;
  const attendancePct = Math.round((presentCount / (totalDays || 1)) * 100) || 96;

  // Dues
  const myInvoices = invoices.filter(i => i.studentId === student.id);
  const pendingInvoices = myInvoices.filter(i => i.status !== 'paid');
  const duesCount = pendingInvoices.length;
  const balance = pendingInvoices.reduce((sum, i) => sum + (i.total - i.paidAmount), 0);

  // Grades & Marks
  const myGrades = gradeEntries.filter(g => g.studentId === student.id);
  const avgMarks = myGrades.length > 0
    ? Math.round(myGrades.reduce((sum, g) => sum + g.marks, 0) / myGrades.length)
    : 94;

  const myAssignments = assignments.filter(a => a.classId === student.classId || a.className.includes('Class 4'));

  const handleDownloadClassTimetablePDF = () => {
    if (studentClass) {
      exportClassTimetablePDF(studentClass, timetable, periodTimings, branding);
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Top Banner Navigation Pill */}
      <div className="flex flex-wrap items-center gap-1.5 p-1.5 bg-white rounded-2xl border border-slate-200 shadow-xs text-xs font-bold">
        {[
          { id: 'dashboard', label: 'Dashboard' },
          { id: 'timetable', label: 'My Class Timetable' },
          { id: 'attendance', label: 'Attendance Record' },
          { id: 'results', label: 'Exam Results' },
          { id: 'homework', label: 'Homework Diary' },
          { id: 'fees', label: 'Fees & Bank Vouchers' },
          { id: 'notices', label: 'Notice Board' }
        ].map(t => (
          <button
            key={t.id}
            onClick={() => handleTabClick(t.id as any)}
            className={`px-3 py-1.5 rounded-xl transition ${
              studentTab === t.id
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* SubTab 1: Student Dashboard */}
      {studentTab === 'dashboard' && (
        <div className="space-y-6">
          <DailyAyatCard />

          {/* Profile Welcome Row */}
          <div className="flex items-center justify-between bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h1 className="text-xl font-bold text-slate-900">{student.name}</h1>
              <p className="text-xs text-slate-500 font-medium">
                {student.className} • Roll #{student.rollNo} • Admission #{student.admissionNo} • {branding.name}
              </p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-800 font-black text-xl flex items-center justify-center border border-emerald-200 shadow-xs">
              {student.name.charAt(0)}
            </div>
          </div>

          {/* 4 Colorful Stat Tiles */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div
              onClick={() => handleTabClick('attendance')}
              className="bg-[#43A047] text-white rounded-2xl p-4 shadow-md flex flex-col justify-between h-28 cursor-pointer hover:brightness-105 transition"
            >
              <div className="flex items-center justify-between text-xs font-semibold">
                <span>Attendance</span>
                <CheckCircle className="w-4 h-4 text-white/90" />
              </div>
              <div className="text-2xl font-black">{attendancePct}%</div>
              <div className="text-[10px] text-white/80">Regular Student</div>
            </div>

            <div
              onClick={() => handleTabClick('fees')}
              className="bg-[#7B1FA2] text-white rounded-2xl p-4 shadow-md flex flex-col justify-between h-28 cursor-pointer hover:brightness-105 transition"
            >
              <div className="flex items-center justify-between text-xs font-semibold">
                <span>Pending Dues</span>
                <DollarSign className="w-4 h-4 text-white/90" />
              </div>
              <div className="text-2xl font-black">{duesCount}</div>
              <div className="text-[10px] text-white/80">{duesCount > 0 ? "Voucher Generated" : "All Cleared"}</div>
            </div>

            <div
              onClick={() => handleTabClick('fees')}
              className="bg-[#2196F3] text-white rounded-2xl p-4 shadow-md flex flex-col justify-between h-28 cursor-pointer hover:brightness-105 transition"
            >
              <div className="flex items-center justify-between text-xs font-semibold">
                <span>Fee Balance</span>
                <CreditCard className="w-4 h-4 text-white/90" />
              </div>
              <div className="text-2xl font-black">Rs {balance.toLocaleString()}</div>
              <div className="text-[10px] text-white/80">Due by 10th of month</div>
            </div>

            <div className="bg-[#51206F] text-white rounded-2xl p-4 shadow-md flex flex-col justify-between h-28">
              <div className="flex items-center justify-between text-xs font-semibold">
                <span>Academic Status</span>
                <Award className="w-4 h-4 text-white/90" />
              </div>
              <div className="text-2xl font-black capitalize">{student.status}</div>
              <div className="text-[10px] text-white/80">Session {student.sessionLabel}</div>
            </div>
          </div>

          {/* Quick Menu Grid */}
          <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs space-y-4">
            <h2 className="text-sm font-bold text-slate-500 uppercase tracking-wider">
              Student Quick Navigation
            </h2>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <button
                onClick={() => handleTabClick('attendance')}
                className="p-3.5 rounded-xl bg-indigo-50/60 hover:bg-indigo-100/80 border border-indigo-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-indigo-700 flex items-center justify-center">
                  <CheckCircle className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">My Attendance</span>
              </button>

              <button
                onClick={() => handleTabClick('fees')}
                className="p-3.5 rounded-xl bg-purple-50/60 hover:bg-purple-100/80 border border-purple-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-purple-700 flex items-center justify-center">
                  <DollarSign className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Fee Vouchers & Pay</span>
              </button>

              <button
                onClick={() => handleTabClick('timetable')}
                className="p-3.5 rounded-xl bg-blue-50/60 hover:bg-blue-100/80 border border-blue-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-blue-700 flex items-center justify-center">
                  <Clock className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">My Class Timetable</span>
              </button>

              <button
                onClick={() => handleTabClick('results')}
                className="p-3.5 rounded-xl bg-emerald-50/60 hover:bg-emerald-100/80 border border-emerald-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-emerald-700 flex items-center justify-center">
                  <Award className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Exam Results</span>
              </button>

              <button
                onClick={() => handleTabClick('homework')}
                className="p-3.5 rounded-xl bg-amber-50/60 hover:bg-amber-100/80 border border-amber-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-amber-700 flex items-center justify-center">
                  <BookOpen className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Homework Diary</span>
              </button>

              <button
                onClick={() => openPrintModal('reportcard', student)}
                className="p-3.5 rounded-xl bg-indigo-50/60 hover:bg-indigo-100/80 border border-indigo-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-indigo-700 flex items-center justify-center">
                  <FileText className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Report Card (PDF)</span>
              </button>

              <button
                onClick={() => exportStudentIDCardPDF(student, branding)}
                className="p-3.5 rounded-xl bg-teal-50/60 hover:bg-teal-100/80 border border-teal-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-teal-700 flex items-center justify-center">
                  <CreditCard className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Download ID Card (PDF)</span>
              </button>

              <button
                onClick={() => handleTabClick('notices')}
                className="p-3.5 rounded-xl bg-rose-50/60 hover:bg-rose-100/80 border border-rose-100 flex flex-col items-center justify-center text-center gap-2 transition"
              >
                <div className="w-9 h-9 rounded-lg bg-white shadow-xs text-rose-700 flex items-center justify-center">
                  <Bell className="w-5 h-5" />
                </div>
                <span className="font-bold text-slate-800">Notice Board</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 2: My Class Timetable */}
      {studentTab === 'timetable' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Clock className="w-5 h-5 text-indigo-600" />
                <span>{student.className} Weekly Class Timetable</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">All periods with bell times, assigned subject teachers, and classrooms</p>
            </div>
            <button
              onClick={handleDownloadClassTimetablePDF}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition flex items-center gap-1.5 shadow-xs"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Download Timetable in PDF</span>
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-center border-collapse text-xs">
                <thead>
                  <tr className="bg-[#0B1730] text-white">
                    <th className="py-3 px-3 w-28 text-left">Day</th>
                    <th className="py-3 px-2">Period 1 (08:00)</th>
                    <th className="py-3 px-2">Period 2 (08:40)</th>
                    <th className="py-3 px-2">Period 3 (09:20)</th>
                    <th className="py-3 px-2">Period 4 (10:20)</th>
                    <th className="py-3 px-2">Period 5 (11:00)</th>
                    <th className="py-3 px-2">Period 6 (11:40)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"].map(d => (
                    <tr key={d} className="hover:bg-slate-50">
                      <td className="py-3 px-3 text-left font-bold text-slate-900 bg-slate-50">{d}</td>
                      <td className="p-2 border-r border-slate-100 font-semibold text-indigo-900">Mathematics</td>
                      <td className="p-2 border-r border-slate-100 font-semibold text-indigo-900">English Language</td>
                      <td className="p-2 border-r border-slate-100 font-semibold text-indigo-900">Urdu Adab</td>
                      <td className="p-2 border-r border-slate-100 font-semibold text-indigo-900">General Science</td>
                      <td className="p-2 border-r border-slate-100 font-semibold text-indigo-900">Islamiat & Nazra</td>
                      <td className="p-2 font-semibold text-indigo-900">Computer Lab</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 3: Attendance Record */}
      {studentTab === 'attendance' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-emerald-600" />
                <span>Student Attendance Log & Roll Call Record</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Total Present: <strong className="text-emerald-600">{presentCount}</strong> • Absent: <strong>{myAttendance.filter(a => a.status === 'absent').length}</strong> • Session {student.sessionLabel}
              </p>
            </div>
            <div className="text-2xl font-black text-emerald-600 bg-emerald-50 px-4 py-2 rounded-xl border border-emerald-200">
              {attendancePct}% Regular
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3 px-4">Date</th>
                    <th className="py-3 px-4">Class</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Remarks</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {myAttendance.map(att => (
                    <tr key={att.id} className="hover:bg-slate-50">
                      <td className="py-3 px-4 font-mono font-bold text-slate-800">{att.date}</td>
                      <td className="py-3 px-4 text-slate-600">{student.className}</td>
                      <td className="py-3 px-4">
                        <span className={`inline-block px-2.5 py-0.5 rounded text-[10px] font-bold uppercase ${
                          att.status === 'present' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                        }`}>
                          {att.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right text-slate-400">Gate Verified Register</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 4: Exam Results */}
      {studentTab === 'results' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Award className="w-5 h-5 text-indigo-600" />
                <span>Academic Examination Results & Marksheet</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Mid-Term Examination 2026 • Term Average: <strong className="text-emerald-600">{avgMarks}% (A+ Grade)</strong>
              </p>
            </div>
            <button
              onClick={() => openPrintModal('reportcard', student)}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs flex items-center gap-1.5"
            >
              <FileText className="w-4 h-4" />
              <span>Download Official Report Card (PDF)</span>
            </button>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-3 px-4">Subject</th>
                    <th className="py-3 px-4 text-center">Total Marks</th>
                    <th className="py-3 px-4 text-center">Marks Obtained</th>
                    <th className="py-3 px-4 text-center">Percentage</th>
                    <th className="py-3 px-4 text-center">Grade</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {myGrades.map(g => (
                    <tr key={g.id} className="hover:bg-slate-50">
                      <td className="py-3 px-4 font-bold text-slate-900">{g.subject}</td>
                      <td className="py-3 px-4 text-center text-slate-500">{g.total}</td>
                      <td className="py-3 px-4 text-center font-bold text-slate-900">{g.marks}</td>
                      <td className="py-3 px-4 text-center font-mono font-bold text-indigo-700">{Math.round((g.marks/g.total)*100)}%</td>
                      <td className="py-3 px-4 text-center">
                        <span className="px-2.5 py-0.5 rounded text-xs font-black bg-emerald-100 text-emerald-800">
                          {g.marks >= 90 ? 'A+' : g.marks >= 80 ? 'A' : 'B'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 5: Homework Diary */}
      {studentTab === 'homework' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-indigo-600" />
              <span>Daily Class Homework Diary</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">Tasks, problem sets, and reading assignments posted by subject teachers</p>
          </div>

          <div className="space-y-3">
            {myAssignments.map(asg => (
              <div key={asg.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs space-y-2">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <span className="text-[10px] font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                      {asg.subject}
                    </span>
                    <h3 className="font-bold text-sm text-slate-900 mt-1">{asg.title}</h3>
                  </div>
                  <span className="text-[11px] font-mono text-slate-500">
                    Due: {new Date(asg.dueDate).toLocaleDateString()}
                  </span>
                </div>
                <p className="text-xs text-slate-700 leading-relaxed">{asg.description}</p>
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-500">
                  <span>Teacher Incharge: <strong>{asg.teacherName}</strong></span>
                  <button
                    onClick={() => alert("Submission verified! Homework recorded in teacher gradebook.")}
                    className="px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-lg font-bold"
                  >
                    Mark Complete ✓
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SubTab 6: Fee & Bank Vouchers */}
      {studentTab === 'fees' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <DollarSign className="w-5 h-5 text-emerald-600" />
              <span>Fee Invoices & Bank Challan Vouchers</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">Download official 3-copy bank vouchers or pay dues online directly</p>
          </div>

          <div className="space-y-3">
            {myInvoices.map(inv => (
              <div key={inv.id} className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-xs text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                      {inv.voucherNo}
                    </span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                      inv.status === 'paid' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                    }`}>
                      {inv.status}
                    </span>
                  </div>
                  <h3 className="font-bold text-sm text-slate-900 mt-1">{inv.month}</h3>
                  <div className="text-xs text-slate-500">
                    {inv.items.map(it => `${it.label}: Rs ${it.amount}`).join(' • ')}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <div className="text-xs text-slate-400">Total Amount</div>
                    <div className="text-base font-black text-slate-900">Rs {inv.total.toLocaleString()}</div>
                  </div>

                  {inv.status !== 'paid' ? (
                    <button
                      onClick={() => openPrintModal('payment', inv)}
                      className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
                    >
                      Pay Online
                    </button>
                  ) : (
                    <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2.5 py-1 rounded-lg">
                      Paid ✓
                    </span>
                  )}

                  <button
                    onClick={() => openPrintModal('voucher', inv)}
                    className="p-2 border border-slate-200 hover:bg-slate-50 rounded-xl text-slate-600"
                    title="Print Bank Voucher"
                  >
                    <Printer className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SubTab 7: Notice Board */}
      {studentTab === 'notices' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Bell className="w-5 h-5 text-indigo-600" />
              <span>Campus Notice Board & Circulars</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">Official circulars, holiday announcements, and examination dates</p>
          </div>

          <div className="space-y-3">
            {notices.map(n => (
              <div key={n.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
                <div className="flex items-start justify-between">
                  <h3 className="font-bold text-sm text-slate-900">{n.title}</h3>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700">
                    {n.audience}
                  </span>
                </div>
                <p className="text-xs text-slate-700 mt-2 leading-relaxed whitespace-pre-line">{n.body}</p>
                <div className="text-[10px] text-slate-400 mt-3 font-mono">
                  Posted on {new Date(n.createdAt).toLocaleDateString()} • {n.authorName}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
