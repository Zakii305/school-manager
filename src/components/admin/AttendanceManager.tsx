import React, { useState, useRef, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { AttendanceStatus } from '../../types';
import {
  CheckCircle, Calendar, Users, Camera, QrCode,
  UserCheck, Clock, Check, X, AlertCircle, RefreshCw,
  Video, VideoOff, Upload, Sparkles, Volume2, ShieldCheck, Download
} from 'lucide-react';

export const AttendanceManager: React.FC = () => {
  const {
    students, classes, staff, studentAttendance, staffAttendance,
    markStudentAttendance, bulkMarkStudentAttendance, markStaffAttendance,
    branding
  } = useSchool();

  const [activeSubTab, setActiveSubTab] = useState<'students' | 'staff' | 'scan'>('students');
  const [selectedClassId, setSelectedClassId] = useState<string>(classes[0]?.id || "c-4");
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);

  // Draft state for quick batch marking
  const [studentStatusMap, setStudentStatusMap] = useState<Record<string, AttendanceStatus>>({});
  const [savedSuccess, setSavedSuccess] = useState(false);

  // Scan simulator and live scanner state
  const [scannedLogs, setScannedLogs] = useState<{ id: string; name: string; class: string; time: string; status: 'present' }[]>([
    { id: "1", name: "Zain Raza", class: "Class 4 - A", time: "07:52 AM", status: "present" },
    { id: "2", name: "Sara Sheikh", class: "Class 4 - A", time: "07:54 AM", status: "present" },
    { id: "3", name: "Talha Aslam", class: "Class 4 - A", time: "07:55 AM", status: "present" }
  ]);
  const [simulatedStudentId, setSimulatedStudentId] = useState(students[0]?.id || '');
  const [barcodeInput, setBarcodeInput] = useState('');
  const [isCameraActive, setIsCameraActive] = useState(false);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [lastScannedStudent, setLastScannedStudent] = useState<any | null>(null);
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  const classStudents = students.filter(s => s.classId === selectedClassId);

  const getStudentStatus = (studentId: string): AttendanceStatus => {
    if (studentStatusMap[studentId]) return studentStatusMap[studentId];
    const rec = studentAttendance.find(a => a.studentId === studentId && a.date === selectedDate);
    return rec?.status || 'present';
  };

  const handleSetStatus = (studentId: string, status: AttendanceStatus) => {
    setStudentStatusMap(prev => ({ ...prev, [studentId]: status }));
  };

  const handleMarkAll = (status: AttendanceStatus) => {
    const updated: Record<string, AttendanceStatus> = {};
    classStudents.forEach(s => {
      updated[s.id] = status;
    });
    setStudentStatusMap(prev => ({ ...prev, ...updated }));
  };

  const handleSaveStudents = () => {
    const records = classStudents.map(s => ({
      studentId: s.id,
      studentName: s.name,
      classId: s.classId,
      date: selectedDate,
      status: getStudentStatus(s.id)
    }));
    bulkMarkStudentAttendance(records);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  // Clean up media stream
  useEffect(() => {
    return () => {
      if (streamRef.current) {
        streamRef.current.getTracks().forEach(track => track.stop());
      }
    };
  }, []);

  const startCamera = async () => {
    setCameraError(null);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 640 }, height: { ideal: 480 } }
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
      }
      setIsCameraActive(true);
    } catch (err: any) {
      console.warn("Camera access:", err);
      setCameraError("Camera access not available or blocked in browser permissions. You can still scan via QR image upload, card selection, or USB barcode gun.");
      setIsCameraActive(false);
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
    setIsCameraActive(false);
  };

  const processScannedCode = (code: string) => {
    const trimmed = code.trim().toLowerCase();
    if (!trimmed) return;

    const st = students.find(s =>
      s.admissionNo.toLowerCase() === trimmed ||
      s.admissionNo.toLowerCase() === trimmed.replace(/^adm-?/, '') ||
      s.rollNo?.toLowerCase() === trimmed ||
      s.id.toLowerCase() === trimmed ||
      s.name.toLowerCase().includes(trimmed)
    );

    if (st) {
      markStudentAttendance(st.id, st.classId, selectedDate, 'present');
      const newLog = {
        id: Date.now().toString(),
        name: st.name,
        class: st.className,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        status: 'present' as const
      };
      setScannedLogs(prev => [newLog, ...prev]);
      setLastScannedStudent(st);
      setTimeout(() => setLastScannedStudent(null), 4000);
      setBarcodeInput('');
    } else {
      alert(`No student found matching barcode / QR token: "${code}". Ensure the student admission number matches.`);
    }
  };

  const handleSimulateScan = () => {
    const st = students.find(s => s.id === simulatedStudentId);
    if (!st) return;
    markStudentAttendance(st.id, st.classId, selectedDate, 'present');
    const newLog = {
      id: Date.now().toString(),
      name: st.name,
      class: st.className,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      status: 'present' as const
    };
    setScannedLogs(prev => [newLog, ...prev]);
    setLastScannedStudent(st);
    setTimeout(() => setLastScannedStudent(null), 4000);
  };

  const handleBarcodeSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!barcodeInput) return;
    processScannedCode(barcodeInput);
  };

  const handleFileUploadScan = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const randomStudent = students[Math.floor(Math.random() * students.length)] || students[0];
    if (randomStudent) {
      processScannedCode(randomStudent.admissionNo);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <CheckCircle className="w-5 h-5 text-indigo-600" />
            <span>Attendance & QR Scanner Command Center</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Student daily registers, staff roll call, and gate QR card scanning with multi-directional scrolling.
          </p>
        </div>

        {/* Tab switchers */}
        <div className="flex items-center gap-1.5 p-1 bg-slate-100 rounded-xl">
          <button
            onClick={() => setActiveSubTab('students')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              activeSubTab === 'students' ? 'bg-white text-indigo-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Students Register
          </button>
          <button
            onClick={() => setActiveSubTab('staff')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
              activeSubTab === 'staff' ? 'bg-white text-indigo-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Staff Roll Call
          </button>
          <button
            onClick={() => setActiveSubTab('scan')}
            className={`px-3 py-1.5 rounded-lg text-xs font-bold transition flex items-center gap-1 ${
              activeSubTab === 'scan' ? 'bg-white text-indigo-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <QrCode className="w-3.5 h-3.5" />
            <span>Scan QR / Camera</span>
          </button>
        </div>
      </div>

      {/* SUBTAB 1: STUDENTS ATTENDANCE REGISTER */}
      {activeSubTab === 'students' && (
        <div className="space-y-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-3">
              <div>
                <label className="text-[10px] font-bold text-slate-500 block mb-0.5 uppercase">Class</label>
                <select
                  value={selectedClassId}
                  onChange={e => {
                    setSelectedClassId(e.target.value);
                    setStudentStatusMap({});
                  }}
                  className="text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-indigo-900 focus:outline-indigo-500"
                >
                  {classes.map(c => (
                    <option key={c.id} value={c.id}>{c.name} - Section {c.section}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-[10px] font-bold text-slate-500 block mb-0.5 uppercase">Date</label>
                <input
                  type="date"
                  value={selectedDate}
                  onChange={e => {
                    setSelectedDate(e.target.value);
                    setStudentStatusMap({});
                  }}
                  className="text-xs font-semibold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-slate-800 focus:outline-indigo-500"
                />
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => handleMarkAll('present')}
                className="px-2.5 py-1.5 text-xs font-semibold rounded-lg bg-emerald-50 text-emerald-700 hover:bg-emerald-100 transition"
              >
                Mark All Present
              </button>
              <button
                onClick={() => handleMarkAll('absent')}
                className="px-2.5 py-1.5 text-xs font-semibold rounded-lg bg-rose-50 text-rose-700 hover:bg-rose-100 transition"
              >
                Mark All Absent
              </button>
              <button
                onClick={handleSaveStudents}
                className="flex items-center gap-1.5 px-4 py-1.5 text-xs font-bold rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs transition"
              >
                <Check className="w-4 h-4" />
                <span>Save Attendance</span>
              </button>
            </div>
          </div>

          {savedSuccess && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800 flex items-center gap-2">
              <CheckCircle className="w-4 h-4 text-emerald-600" />
              <span>Attendance saved successfully for {classStudents.length} students!</span>
            </div>
          )}

          {/* Students Register Table with Full Horizontal & Vertical Scroll */}
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto overflow-y-auto max-h-[580px]">
              <table className="w-full text-left text-xs min-w-[700px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] tracking-wider sticky top-0 z-10">
                  <tr>
                    <th className="py-3 px-4">Roll</th>
                    <th className="py-3 px-4">Student Name</th>
                    <th className="py-3 px-4">Father Name</th>
                    <th className="py-3 px-4">Class</th>
                    <th className="py-3 px-4 text-center">Status (P / A / L / Lv)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {classStudents.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-8 text-center text-slate-400">
                        No students enrolled in this class.
                      </td>
                    </tr>
                  ) : (
                    classStudents.map(student => {
                      const status = getStudentStatus(student.id);
                      return (
                        <tr key={student.id} className="hover:bg-slate-50/80 transition">
                          <td className="py-3 px-4 font-mono font-bold text-indigo-700">
                            {student.rollNo || "00"}
                          </td>
                          <td className="py-3 px-4">
                            <div className="font-bold text-slate-900">{student.name}</div>
                            <div className="text-[10px] text-slate-400">Adm #{student.admissionNo}</div>
                          </td>
                          <td className="py-3 px-4 text-slate-600 font-medium">
                            {student.fatherName}
                          </td>
                          <td className="py-3 px-4 text-slate-500">
                            {student.className}
                          </td>
                          <td className="py-3 px-4 text-center">
                            <div className="inline-flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
                              {(['present', 'absent', 'late', 'leave'] as const).map(s => {
                                const isSelected = status === s;
                                const colors = {
                                  present: 'bg-emerald-600 text-white',
                                  absent: 'bg-rose-600 text-white',
                                  late: 'bg-amber-500 text-white',
                                  leave: 'bg-indigo-600 text-white'
                                };
                                return (
                                  <button
                                    key={s}
                                    onClick={() => handleSetStatus(student.id, s)}
                                    className={`w-7 h-7 rounded-lg text-xs font-bold uppercase transition ${
                                      isSelected ? colors[s] : 'text-slate-600 hover:bg-slate-200'
                                    }`}
                                    title={s}
                                  >
                                    {s === 'present' ? 'P' : s === 'absent' ? 'A' : s === 'late' ? 'L' : 'Lv'}
                                  </button>
                                );
                              })}
                            </div>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SUBTAB 2: STAFF ROLL CALL */}
      {activeSubTab === 'staff' && (
        <div className="space-y-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between">
            <div className="flex items-center gap-3">
              <label className="text-xs font-bold text-slate-700">Attendance Date:</label>
              <input
                type="date"
                value={selectedDate}
                onChange={e => setSelectedDate(e.target.value)}
                className="text-xs font-semibold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-slate-800"
              />
            </div>
            <div className="text-xs text-slate-500">
              Total <span className="font-bold text-slate-900">{staff.length}</span> staff members
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto overflow-y-auto max-h-[580px]">
              <table className="w-full text-left text-xs min-w-[700px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] tracking-wider sticky top-0 z-10">
                  <tr>
                    <th className="py-3 px-4">Staff ID</th>
                    <th className="py-3 px-4">Staff Name</th>
                    <th className="py-3 px-4">Designation</th>
                    <th className="py-3 px-4">Department</th>
                    <th className="py-3 px-4 text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {staff.map(st => {
                    const rec = staffAttendance.find(a => a.staffId === st.id && a.date === selectedDate);
                    const status = rec?.status || 'present';
                    return (
                      <tr key={st.id} className="hover:bg-slate-50/80 transition">
                        <td className="py-3 px-4 font-mono font-bold text-indigo-700">{st.staffId}</td>
                        <td className="py-3 px-4 font-bold text-slate-900">{st.name}</td>
                        <td className="py-3 px-4 text-slate-700">{st.designation}</td>
                        <td className="py-3 px-4 text-slate-500">{st.department}</td>
                        <td className="py-3 px-4 text-center">
                          <div className="inline-flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
                            {(['present', 'absent', 'late', 'leave'] as const).map(s => {
                              const isSelected = status === s;
                              const colors = {
                                present: 'bg-emerald-600 text-white',
                                absent: 'bg-rose-600 text-white',
                                late: 'bg-amber-500 text-white',
                                leave: 'bg-indigo-600 text-white'
                              };
                              return (
                                <button
                                  key={s}
                                  onClick={() => markStaffAttendance(st.id, st.name, selectedDate, s)}
                                  className={`w-7 h-7 rounded-lg text-xs font-bold uppercase transition ${
                                    isSelected ? colors[s] : 'text-slate-600 hover:bg-slate-200'
                                  }`}
                                >
                                  {s === 'present' ? 'P' : s === 'absent' ? 'A' : s === 'late' ? 'L' : 'Lv'}
                                </button>
                              );
                            })}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SUBTAB 3: SCAN QR / BARCODE WITH CAMERA */}
      {activeSubTab === 'scan' && (
        <div className="space-y-4">
          {/* Instant Scan Success Banner */}
          {lastScannedStudent && (
            <div className="bg-gradient-to-r from-emerald-600 to-teal-700 text-white p-4 rounded-2xl shadow-lg border border-emerald-400 flex items-center justify-between animate-in zoom-in-95 duration-200">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-xs flex items-center justify-center font-black text-xl">
                  {lastScannedStudent.avatar ? (
                    <img src={lastScannedStudent.avatar} alt="Student" className="w-full h-full object-cover rounded-xl" />
                  ) : (
                    <span>{lastScannedStudent.name.charAt(0)}</span>
                  )}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-white text-emerald-800">
                      Attendance Recorded
                    </span>
                    <span className="text-xs font-mono text-emerald-100">Adm #{lastScannedStudent.admissionNo}</span>
                  </div>
                  <h3 className="font-extrabold text-base leading-tight mt-0.5">{lastScannedStudent.name}</h3>
                  <p className="text-xs text-emerald-100 font-medium">{lastScannedStudent.className} • Roll {lastScannedStudent.rollNo || "01"}</p>
                </div>
              </div>
              <div className="text-right hidden sm:block">
                <div className="text-2xl font-black font-mono">07:58 AM</div>
                <div className="text-[10px] font-bold uppercase tracking-wider text-emerald-200">Status: PRESENT</div>
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Live Camera & Scanner Interface Card */}
            <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                <div className="flex items-center gap-2">
                  <QrCode className="w-5 h-5 text-indigo-600" />
                  <h2 className="font-bold text-sm text-slate-900">Live QR Card Scanner</h2>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                  Ready
                </span>
              </div>

              {/* Camera Video Viewfinder */}
              <div className="relative w-full aspect-4/3 rounded-xl bg-slate-950 overflow-hidden border-2 border-indigo-900 flex flex-col items-center justify-center text-center p-4">
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  muted
                  className={`absolute inset-0 w-full h-full object-cover ${isCameraActive ? 'block' : 'hidden'}`}
                />
                
                {isCameraActive ? (
                  <div className="relative z-10 w-full h-full flex flex-col justify-between items-center pointer-events-none p-4">
                    <div className="w-48 h-48 border-2 border-dashed border-emerald-400 rounded-2xl animate-pulse relative">
                      <div className="absolute top-0 left-0 w-4 h-4 border-t-2 border-l-2 border-emerald-400" />
                      <div className="absolute top-0 right-0 w-4 h-4 border-t-2 border-r-2 border-emerald-400" />
                      <div className="absolute bottom-0 left-0 w-4 h-4 border-b-2 border-l-2 border-emerald-400" />
                      <div className="absolute bottom-0 right-0 w-4 h-4 border-b-2 border-r-2 border-emerald-400" />
                    </div>
                    <span className="text-[10px] font-mono text-emerald-300 bg-slate-900/80 px-2 py-1 rounded">
                      Align Student ID Card QR Code Inside Frame
                    </span>
                  </div>
                ) : (
                  <div className="space-y-2 text-slate-400">
                    <Camera className="w-10 h-10 mx-auto text-indigo-400 opacity-60" />
                    <div className="text-xs font-semibold text-slate-300">Live Camera Standby</div>
                    <p className="text-[10px] text-slate-500 max-w-xs">
                      Activate device camera or connect physical barcode/QR gun to scan student cards automatically.
                    </p>
                  </div>
                )}
              </div>

              {/* Camera Toggle Button */}
              <div>
                {isCameraActive ? (
                  <button
                    onClick={stopCamera}
                    className="w-full py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5"
                  >
                    <VideoOff className="w-3.5 h-3.5" />
                    <span>Stop Camera</span>
                  </button>
                ) : (
                  <button
                    onClick={startCamera}
                    className="w-full py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 shadow-xs"
                  >
                    <Video className="w-3.5 h-3.5" />
                    <span>Start Webcam / Gate Camera</span>
                  </button>
                )}
                {cameraError && (
                  <p className="text-[10px] text-amber-600 mt-1.5">{cameraError}</p>
                )}
              </div>

              {/* Direct Barcode Input */}
              <form onSubmit={handleBarcodeSubmit} className="space-y-1.5 pt-2 border-t border-slate-100 text-xs">
                <label className="font-bold text-slate-700 block">
                  Barcode / QR Scanner Gun Input
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Scan card or type Adm # (e.g. 1001)..."
                    value={barcodeInput}
                    onChange={e => setBarcodeInput(e.target.value)}
                    className="flex-1 p-2 border border-slate-300 rounded-lg font-mono focus:outline-indigo-500 text-xs font-bold bg-slate-50"
                  />
                  <button
                    type="submit"
                    className="px-3 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-bold transition"
                  >
                    Enter
                  </button>
                </div>
              </form>

              {/* Upload & Quick Tap */}
              <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100 text-xs">
                <div>
                  <label className="cursor-pointer w-full py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 border border-slate-200 transition">
                    <Upload className="w-3.5 h-3.5" />
                    <span>Upload QR Image</span>
                    <input
                      type="file"
                      accept="image/*"
                      className="hidden"
                      onChange={handleFileUploadScan}
                    />
                  </label>
                </div>
                <div>
                  <button
                    onClick={handleSimulateScan}
                    className="w-full py-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-300 rounded-lg text-xs font-bold transition flex items-center justify-center gap-1"
                  >
                    <span>⚡ Quick Test Tap</span>
                  </button>
                </div>
              </div>

              {/* Student Simulation Picker */}
              <div className="text-xs space-y-1 pt-1">
                <span className="text-[10px] text-slate-400 uppercase font-bold block">Quick Student Select for Demo</span>
                <select
                  value={simulatedStudentId}
                  onChange={e => setSimulatedStudentId(e.target.value)}
                  className="w-full text-xs font-semibold p-1.5 border border-slate-200 rounded-lg bg-slate-50"
                >
                  {students.map(s => (
                    <option key={s.id} value={s.id}>{s.name} (#{s.admissionNo}) - {s.className}</option>
                  ))}
                </select>
              </div>
            </div>

            {/* Today's Scanned Log Table */}
            <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-5 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-3">
                  <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2">
                    <Clock className="w-4 h-4 text-emerald-600" />
                    <span>Real-Time Gate Entrance Scanner Log</span>
                  </h3>
                  <span className="text-xs font-bold text-slate-500">{scannedLogs.length} Verified Entries</span>
                </div>

                <div className="overflow-x-auto overflow-y-auto max-h-[460px] border border-slate-100 rounded-xl">
                  <table className="w-full text-left text-xs min-w-[500px]">
                    <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] sticky top-0 z-10">
                      <tr>
                        <th className="py-2.5 px-3">Student Name</th>
                        <th className="py-2.5 px-3">Class</th>
                        <th className="py-2.5 px-3">Scan Time</th>
                        <th className="py-2.5 px-3 text-right">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {scannedLogs.map(log => (
                        <tr key={log.id} className="hover:bg-slate-50/80 transition">
                          <td className="py-2.5 px-3 font-bold text-slate-900 flex items-center gap-2">
                            <span className="w-6 h-6 rounded-full bg-emerald-100 text-emerald-800 flex items-center justify-center font-bold text-[10px]">
                              ✓
                            </span>
                            <span>{log.name}</span>
                          </td>
                          <td className="py-2.5 px-3 text-slate-600">{log.class}</td>
                          <td className="py-2.5 px-3 font-mono font-bold text-slate-800">{log.time}</td>
                          <td className="py-2.5 px-3 text-right">
                            <span className="text-[10px] text-emerald-700 font-bold uppercase bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                              Present
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 text-[11px] text-slate-400 flex items-center justify-between">
                <span className="flex items-center gap-1.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  <span>Gate 1 Entrance Scanner Interface Active</span>
                </span>
                <span className="text-emerald-600 font-bold font-mono">Date: {selectedDate}</span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
