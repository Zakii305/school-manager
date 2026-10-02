import React, { useState, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  LayoutDashboard, Layers, Clock, CheckCircle, BookOpen,
  ClipboardList, Bell, Plus, Trash2, Calendar, Check,
  Download, UserCheck, Users, Edit3
} from 'lucide-react';
import { exportTeacherTimetablePDF } from '../../utils/pdfExport';

export const TeacherPortal: React.FC = () => {
  const {
    currentUser, classes, subjects, timetable, studentAttendance,
    assignments, leaveRequests, notices, addAssignment, deleteAssignment,
    submitLeaveRequest, bulkMarkStudentAttendance, students, periodTimings, branding,
    activeTab, setActiveTab
  } = useSchool();

  const [activeSubTab, setActiveSubTab] = useState<'dashboard' | 'classes' | 'attendance' | 'homework' | 'timetable' | 'leave' | 'notices'>(() => {
    if (activeTab === 'teacher-classes') return 'classes';
    if (activeTab === 'teacher-timetable') return 'timetable';
    if (activeTab === 'teacher-attendance') return 'attendance';
    if (activeTab === 'teacher-homework') return 'homework';
    if (activeTab === 'teacher-leave') return 'leave';
    if (activeTab === 'teacher-notices') return 'notices';
    return 'dashboard';
  });

  // Sync internal subtab whenever the sidebar navigation changes activeTab
  useEffect(() => {
    if (activeTab === 'teacher-classes') setActiveSubTab('classes');
    else if (activeTab === 'teacher-timetable') setActiveSubTab('timetable');
    else if (activeTab === 'teacher-attendance') setActiveSubTab('attendance');
    else if (activeTab === 'teacher-homework') setActiveSubTab('homework');
    else if (activeTab === 'teacher-leave') setActiveSubTab('leave');
    else if (activeTab === 'teacher-notices') setActiveSubTab('notices');
    else if (activeTab === 'dashboard') setActiveSubTab('dashboard');
  }, [activeTab]);

  const handleSubTabClick = (tab: 'dashboard' | 'classes' | 'attendance' | 'homework' | 'timetable' | 'leave' | 'notices') => {
    setActiveSubTab(tab);
    if (tab === 'classes') setActiveTab('teacher-classes');
    else if (tab === 'timetable') setActiveTab('teacher-timetable');
    else if (tab === 'attendance') setActiveTab('teacher-attendance');
    else if (tab === 'homework') setActiveTab('teacher-homework');
    else if (tab === 'leave') setActiveTab('teacher-leave');
    else if (tab === 'notices') setActiveTab('teacher-notices');
    else setActiveTab('dashboard');
  };

  // Homework form state
  const [hwTitle, setHwTitle] = useState('');
  const [hwSubject, setHwSubject] = useState('English Language & Comp');
  const [hwClassId, setHwClassId] = useState('c-4');
  const [hwDueDate, setHwDueDate] = useState('2026-10-10');
  const [hwDesc, setHwDesc] = useState('');

  // Leave form state
  const [leaveFrom, setLeaveFrom] = useState('');
  const [leaveTo, setLeaveTo] = useState('');
  const [leaveReason, setLeaveReason] = useState('');
  const [leaveSubmitted, setLeaveSubmitted] = useState(false);

  // Attendance local state
  const [attClassId, setAttClassId] = useState('c-4');
  const [attDate, setAttDate] = useState(new Date().toISOString().split('T')[0]);
  const [attStatusMap, setAttStatusMap] = useState<Record<string, 'present' | 'absent' | 'late' | 'leave'>>({});
  const [attSaved, setAttSaved] = useState(false);

  const classStudents = students.filter(s => s.classId === attClassId);
  const teacherSubjects = subjects.filter(s => s.teacherName?.toLowerCase().includes('hina') || s.teacherId === 'st-7');

  const handlePostHomework = (e: React.FormEvent) => {
    e.preventDefault();
    if (!hwTitle) return;
    const cls = classes.find(c => c.id === hwClassId);
    addAssignment({
      title: hwTitle,
      description: hwDesc,
      subject: hwSubject,
      classId: hwClassId,
      className: cls ? `${cls.name} - ${cls.section}` : "Class 4 - A",
      dueDate: new Date(hwDueDate).getTime() || Date.now() + 5 * 86400000,
      teacherName: currentUser.name
    });
    setHwTitle('');
    setHwDesc('');
    alert("Homework successfully assigned to class!");
  };

  const handleSaveAttendance = () => {
    const records = classStudents.map(s => ({
      studentId: s.id,
      studentName: s.name,
      classId: s.classId,
      date: attDate,
      status: attStatusMap[s.id] || 'present'
    }));
    bulkMarkStudentAttendance(records);
    setAttSaved(true);
    setTimeout(() => setAttSaved(false), 3000);
  };

  const handleApplyLeave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!leaveFrom || !leaveReason) return;
    submitLeaveRequest({
      teacherId: currentUser.uid,
      teacherName: currentUser.name,
      fromDate: leaveFrom,
      toDate: leaveTo || leaveFrom,
      reason: leaveReason
    });
    setLeaveFrom('');
    setLeaveTo('');
    setLeaveReason('');
    setLeaveSubmitted(true);
    setTimeout(() => setLeaveSubmitted(false), 3000);
  };

  const handleDownloadMyTimetablePDF = () => {
    exportTeacherTimetablePDF(currentUser.name, timetable, classes, periodTimings, branding);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-emerald-800 to-teal-900 rounded-2xl p-5 text-white shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider bg-white/10 px-2 py-0.5 rounded text-emerald-200">
            Faculty Workspace • {branding.name}
          </span>
          <h1 className="text-xl font-bold mt-1">Hello, {currentUser.name}</h1>
          <p className="text-xs text-emerald-100">
            Subject Lead & Class Incharge • Academic Session {branding.session}
          </p>
        </div>

        {/* Quick Tabs Pill */}
        <div className="flex flex-wrap items-center gap-1.5 p-1 bg-black/20 backdrop-blur-xs rounded-xl text-xs font-bold">
          {[
            { id: 'dashboard', label: 'Dashboard' },
            { id: 'classes', label: 'My Classes & Subjects' },
            { id: 'timetable', label: 'Timetable' },
            { id: 'attendance', label: 'Mark Attendance' },
            { id: 'homework', label: 'Homework' },
            { id: 'leave', label: 'Leave' },
            { id: 'notices', label: 'Notices' }
          ].map(t => (
            <button
              key={t.id}
              onClick={() => handleSubTabClick(t.id as any)}
              className={`px-3 py-1.5 rounded-lg transition ${
                activeSubTab === t.id ? 'bg-white text-emerald-950 shadow-xs' : 'text-emerald-100 hover:bg-white/10'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* SubTab 1: Dashboard */}
      {activeSubTab === 'dashboard' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div
              onClick={() => handleSubTabClick('timetable')}
              className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4 cursor-pointer hover:border-emerald-300 transition"
            >
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
                <Layers className="w-6 h-6" />
              </div>
              <div>
                <div className="text-2xl font-black text-slate-900">8</div>
                <div className="text-xs text-slate-500 font-medium">Periods Scheduled</div>
              </div>
            </div>

            <div
              onClick={() => handleSubTabClick('classes')}
              className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4 cursor-pointer hover:border-emerald-300 transition"
            >
              <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
                <UserCheck className="w-6 h-6" />
              </div>
              <div>
                <div className="text-2xl font-black text-slate-900">{classStudents.length || 34}</div>
                <div className="text-xs text-slate-500 font-medium">My Assigned Students</div>
              </div>
            </div>

            <div
              onClick={() => handleSubTabClick('leave')}
              className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs flex items-center gap-4 cursor-pointer hover:border-emerald-300 transition"
            >
              <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
                <ClipboardList className="w-6 h-6" />
              </div>
              <div>
                <div className="text-2xl font-black text-slate-900">
                  {leaveRequests.filter(l => l.teacherName.includes(currentUser.name.split(' ')[1] || 'Hina')).length || 1}
                </div>
                <div className="text-xs text-slate-500 font-medium">Leave Applications</div>
              </div>
            </div>
          </div>

          {/* Today's Timetable card */}
          <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-3">
              <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                <Clock className="w-4 h-4 text-indigo-600" />
                <span>Today&apos;s Class Schedule (Monday)</span>
              </h3>
              <button
                onClick={handleDownloadMyTimetablePDF}
                className="px-3 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 rounded-lg text-xs font-bold border border-emerald-200 flex items-center gap-1 transition"
              >
                <Download className="w-3.5 h-3.5 text-emerald-700" />
                <span>Download Schedule (PDF)</span>
              </button>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-2.5 px-3">Period</th>
                    <th className="py-2.5 px-3">Class</th>
                    <th className="py-2.5 px-3">Subject</th>
                    <th className="py-2.5 px-3">Time</th>
                    <th className="py-2.5 px-3 text-right">Room / Lab</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    { p: "1", cls: "Class 4 - A", subj: "English Language & Comp", time: "08:00 - 08:40", room: "Room 102" },
                    { p: "2", cls: "Class 4 - A", subj: "English Comprehension", time: "08:40 - 09:20", room: "Room 102" },
                    { p: "4", cls: "Class 5 - A", subj: "Grammar & Creative Writing", time: "10:20 - 11:00", room: "Room 104" }
                  ].map((row, i) => (
                    <tr key={i} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-3 font-mono font-bold text-indigo-600">Period {row.p}</td>
                      <td className="py-3 px-3 font-bold text-slate-900">{row.cls}</td>
                      <td className="py-3 px-3 text-slate-700 font-medium">{row.subj}</td>
                      <td className="py-3 px-3 font-mono text-slate-600">{row.time}</td>
                      <td className="py-3 px-3 text-right text-slate-500">{row.room}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 2: My Classes & Subjects */}
      {activeSubTab === 'classes' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Layers className="w-5 h-5 text-indigo-600" />
                <span>My Allocated Classes & Academic Subjects</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">Classes and subjects under your direct faculty supervision</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs space-y-3">
              <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                <span className="font-bold text-sm text-slate-900">Class Incharge Responsibility</span>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-800">
                  Assigned
                </span>
              </div>
              <div className="text-xs space-y-2">
                <div>Class: <strong className="text-slate-900 text-sm">Class 4 - Section A</strong></div>
                <div>Enrolled Students: <strong className="text-indigo-700 font-bold">{classStudents.length} Students</strong></div>
                <div>Classroom: <strong className="text-slate-700">Junior Wing Room 102</strong></div>
              </div>
              <button
                onClick={() => handleSubTabClick('attendance')}
                className="w-full py-2 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-xl text-xs font-bold transition"
              >
                Open Daily Register for Class 4-A
              </button>
            </div>

            <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs space-y-3">
              <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                <span className="font-bold text-sm text-slate-900">Assigned Subjects</span>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-100 text-indigo-800">
                  {teacherSubjects.length} Courses
                </span>
              </div>
              <div className="space-y-2 text-xs">
                {teacherSubjects.map(sub => (
                  <div key={sub.id} className="p-2.5 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
                    <div>
                      <div className="font-bold text-slate-900">{sub.name}</div>
                      <div className="text-[11px] text-slate-500 font-mono">Code: {sub.code} • {sub.className}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* SubTab 3: Mark Attendance */}
      {activeSubTab === 'attendance' && (
        <div className="space-y-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <label className="text-xs font-bold text-slate-700">Class:</label>
              <select
                value={attClassId}
                onChange={e => setAttClassId(e.target.value)}
                className="text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-indigo-900"
              >
                {classes.map(c => (
                  <option key={c.id} value={c.id}>{c.name} - Section {c.section}</option>
                ))}
              </select>
              <label className="text-xs font-bold text-slate-700 ml-2">Date:</label>
              <input
                type="date"
                value={attDate}
                onChange={e => setAttDate(e.target.value)}
                className="text-xs font-semibold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5"
              />
            </div>
            <button
              onClick={handleSaveAttendance}
              className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold transition shadow-xs"
            >
              <Check className="w-4 h-4" />
              <span>Save Class Attendance</span>
            </button>
          </div>

          {attSaved && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800">
              Attendance submitted for {classStudents.length} students!
            </div>
          )}

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <table className="w-full text-left text-xs">
              <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                <tr>
                  <th className="py-3 px-4">Roll</th>
                  <th className="py-3 px-4">Student</th>
                  <th className="py-3 px-4">Father Name</th>
                  <th className="py-3 px-4 text-center">Status (P / A / L / Lv)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {classStudents.map(student => {
                  const status = attStatusMap[student.id] || 'present';
                  return (
                    <tr key={student.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-4 font-mono font-bold text-indigo-700">{student.rollNo}</td>
                      <td className="py-3 px-4 font-bold text-slate-900">{student.name}</td>
                      <td className="py-3 px-4 text-slate-600">{student.fatherName}</td>
                      <td className="py-3 px-4 text-center">
                        <div className="inline-flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
                          {(['present', 'absent', 'late', 'leave'] as const).map(s => (
                            <button
                              key={s}
                              onClick={() => setAttStatusMap({ ...attStatusMap, [student.id]: s })}
                              className={`w-7 h-7 rounded-lg text-xs font-bold uppercase transition ${
                                status === s
                                  ? (s === 'present' ? 'bg-emerald-600 text-white' : s === 'absent' ? 'bg-rose-600 text-white' : s === 'late' ? 'bg-amber-500 text-white' : 'bg-indigo-600 text-white')
                                  : 'text-slate-600 hover:bg-slate-200'
                              }`}
                            >
                              {s === 'present' ? 'P' : s === 'absent' ? 'A' : s === 'late' ? 'L' : 'Lv'}
                            </button>
                          ))}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SubTab 4: Homework & Assignments */}
      {activeSubTab === 'homework' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4">
            <h3 className="font-bold text-sm text-slate-900 border-b border-slate-100 pb-2">
              Post Homework Task
            </h3>
            <form onSubmit={handlePostHomework} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Class</label>
                <select
                  value={hwClassId}
                  onChange={e => setHwClassId(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                >
                  {classes.map(c => (
                    <option key={c.id} value={c.id}>{c.name} - Section {c.section}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Subject</label>
                <input
                  type="text"
                  required
                  value={hwSubject}
                  onChange={e => setHwSubject(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Chapter 4 Exercise 2 Questions 1-5"
                  value={hwTitle}
                  onChange={e => setHwTitle(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Due Date</label>
                <input
                  type="date"
                  value={hwDueDate}
                  onChange={e => setHwDueDate(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Instructions</label>
                <textarea
                  rows={3}
                  placeholder="Write clear instructions for students and parents..."
                  value={hwDesc}
                  onChange={e => setHwDesc(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <button
                type="submit"
                className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold shadow-xs transition"
              >
                + Post Homework to Class
              </button>
            </form>
          </div>

          <div className="md:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-5">
            <h3 className="font-bold text-sm text-slate-900 border-b border-slate-100 pb-3 mb-3">
              Assigned Homework & Submissions
            </h3>
            <div className="space-y-3">
              {assignments.map(a => (
                <div key={a.id} className="p-4 rounded-xl border border-slate-100 bg-slate-50/50 space-y-2">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <span className="text-[10px] font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                        {a.className} • {a.subject}
                      </span>
                      <h4 className="font-bold text-sm text-slate-900 mt-1">{a.title}</h4>
                    </div>
                    <button
                      onClick={() => deleteAssignment(a.id)}
                      className="text-slate-400 hover:text-rose-600 p-1"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed">{a.description}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* SubTab 5: Timetable */}
      {activeSubTab === 'timetable' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3">
            <div>
              <h2 className="text-base font-bold text-slate-900">
                Weekly Teaching Schedule: {currentUser.name}
              </h2>
              <p className="text-xs text-slate-500">Periods allocated in {branding.name}</p>
            </div>
            <button
              onClick={handleDownloadMyTimetablePDF}
              className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-xs transition"
            >
              <Download className="w-4 h-4" />
              <span>Download Timetable in PDF</span>
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-center border-collapse text-xs">
              <thead>
                <tr className="bg-[#0B1730] text-white">
                  <th className="py-2.5 px-3 w-28 text-left">Day</th>
                  <th className="py-2.5 px-3">Period 1 (08:00)</th>
                  <th className="py-2.5 px-3">Period 2 (08:40)</th>
                  <th className="py-2.5 px-3">Period 3 (09:20)</th>
                  <th className="py-2.5 px-3">Period 4 (10:20)</th>
                  <th className="py-2.5 px-3">Period 5 (11:00)</th>
                  <th className="py-2.5 px-3">Period 6 (11:40)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"].map(d => (
                  <tr key={d} className="hover:bg-slate-50">
                    <td className="py-3 px-3 text-left font-bold text-slate-900 bg-slate-50">{d}</td>
                    <td className="p-2 border-r border-slate-100">
                      <div className="font-bold text-indigo-900">Class 4 - A</div>
                      <div className="text-[10px] text-slate-500">English</div>
                    </td>
                    <td className="p-2 border-r border-slate-100">
                      <div className="font-bold text-indigo-900">Class 5 - A</div>
                      <div className="text-[10px] text-slate-500">Grammar</div>
                    </td>
                    <td className="p-2 border-r border-slate-100 text-slate-300 italic">Free</td>
                    <td className="p-2 border-r border-slate-100">
                      <div className="font-bold text-indigo-900">Class 4 - A</div>
                      <div className="text-[10px] text-slate-500">Comprehension</div>
                    </td>
                    <td className="p-2 border-r border-slate-100 text-slate-300 italic">Free</td>
                    <td className="p-2">
                      <div className="font-bold text-indigo-900">Class 6 - A</div>
                      <div className="text-[10px] text-slate-500">Literature</div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SubTab 6: Leave Application */}
      {activeSubTab === 'leave' && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs max-w-xl space-y-5">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <ClipboardList className="w-5 h-5 text-indigo-600" />
              <span>Faculty Leave Application</span>
            </h2>
            <p className="text-xs text-slate-500">
              Submit planned leaves for administration approval.
            </p>
          </div>

          {leaveSubmitted && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800 flex items-center gap-2">
              <Check className="w-4 h-4 text-emerald-600" />
              <span>Leave request submitted! Awaiting administrator review.</span>
            </div>
          )}

          <form onSubmit={handleApplyLeave} className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">From Date *</label>
                <input
                  type="date"
                  required
                  value={leaveFrom}
                  onChange={e => setLeaveFrom(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">To Date</label>
                <input
                  type="date"
                  value={leaveTo}
                  onChange={e => setLeaveTo(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
            </div>

            <div>
              <label className="font-semibold text-slate-700 block mb-1">Reason for Leave *</label>
              <textarea
                rows={3}
                required
                placeholder="Mention valid reason for school management record..."
                value={leaveReason}
                onChange={e => setLeaveReason(e.target.value)}
                className="w-full p-2.5 border border-slate-300 rounded-lg focus:outline-indigo-500"
              />
            </div>

            <button
              type="submit"
              className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold shadow-xs transition"
            >
              Submit Application
            </button>
          </form>
        </div>
      )}

      {/* SubTab 7: School Notices */}
      {activeSubTab === 'notices' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Bell className="w-5 h-5 text-indigo-600" />
              <span>Institutional Circulars & School Notices</span>
            </h2>
            <p className="text-xs text-slate-500">Official circulars, holiday announcements, and examination dates</p>
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
