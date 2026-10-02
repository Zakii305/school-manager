import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  LayoutDashboard, Users, UserCheck, Calendar, BookOpen,
  DollarSign, Wallet, FileText, Settings,
  Clock, Shield, Building2, Bell, Library, Bus, Award,
  CheckCircle, FileQuestion, Layers, MessageSquare, Briefcase,
  Archive, ClipboardList, Send, FileBarChart,
  UserPlus, UserCog, Database, ChevronRight, UserSquare2,
  Edit2, Check, X, Download, Key, LogOut, Power
} from 'lucide-react';
import { SchoolLogo } from '../common/SchoolLogo';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const {
    currentUser, switchRole, toggleDemoVisitorMode, isDemoVisitor,
    updateUserEmail, activeTab, setActiveTab,
    branding, currentSchool, schools, switchSchool, isOwner, openDownloadPackageModal,
    isOwnerInspectionMode, ownerInspectedRole, turnOffOwnerInspection, turnOnOwnerInspection
  } = useSchool();

  const [isEditingEmail, setIsEditingEmail] = useState(false);
  const [emailDraft, setEmailDraft] = useState(currentUser.email);
  const [showSignOutConfirm, setShowSignOutConfirm] = useState(false);

  const handleSaveEmail = () => {
    if (emailDraft.trim() && emailDraft.includes('@')) {
      updateUserEmail(emailDraft.trim());
      setIsEditingEmail(false);
    } else {
      alert("Please enter a valid email address.");
    }
  };

  const handleSelect = (tab: string) => {
    if (currentUser.role === 'owner') {
      if (!tab.startsWith('owner-') && tab !== 'messages') {
        turnOnOwnerInspection(currentSchool.id, tab, ownerInspectedRole);
      } else {
        setActiveTab(tab);
      }
    } else {
      setActiveTab(tab);
    }
    onClose();
  };

  const handleSignOutToDemo = () => {
    if (!isDemoVisitor) {
      toggleDemoVisitorMode();
    }
    switchRole('owner');
    setActiveTab('dashboard');
    setShowSignOutConfirm(false);
    onClose();
  };

  const handleSignOutToRole = (role: 'owner' | 'admin' | 'teacher' | 'student' | 'parent') => {
    switchRole(role);
    setActiveTab('dashboard');
    setShowSignOutConfirm(false);
    onClose();
  };

  const getNavGroups = () => {
    // 1. OWNER PORTAL: Both Owner SaaS Console AND Full Campus Inspection as Admin
    if (currentUser.role === 'owner') {
      return [
        {
          title: "OWNER SAAS CONSOLE",
          items: [
            { id: 'owner-overview', label: 'Platform Overview', icon: LayoutDashboard },
            { id: 'owner-schools', label: 'All Schools & Allotments', icon: Building2 },
            { id: 'owner-demo-creds', label: 'Demo Character Passwords', icon: Key },
            { id: 'owner-requests', label: 'Campus Change Requests', icon: MessageSquare },
            { id: 'owner-registrations', label: 'Pending Registrations', icon: UserPlus },
            { id: 'messages', label: 'Platform Inter-Messaging', icon: MessageSquare },
            { id: 'owner-backups', label: 'System Backups & Real APK', icon: Database },
            { id: 'owner-audit', label: 'Platform Activity Log', icon: ClipboardList }
          ]
        },
        {
          title: `INSPECT CAMPUS: ${currentSchool.code}`,
          items: [
            { id: 'dashboard', label: 'Campus Admin Dashboard', icon: LayoutDashboard },
            { id: 'students', label: 'Students & Admissions', icon: Users },
            { id: 'families', label: 'Families (Siblings)', icon: UserSquare2 },
            { id: 'staff', label: 'Teachers & Staff', icon: Briefcase },
            { id: 'classes', label: 'Classes & Sections', icon: Layers },
            { id: 'attendance', label: 'Student Attendance', icon: CheckCircle },
            { id: 'scan-attendance', label: 'Scan QR Attendance', icon: Clock },
            { id: 'timetable', label: 'Master Timetable', icon: Clock },
            { id: 'exams', label: 'Exams & Marks', icon: Calendar },
            { id: 'question-papers', label: 'Question Papers', icon: FileQuestion },
            { id: 'fee-collection', label: 'Fee Collection', icon: DollarSign },
            { id: 'family-fee', label: 'Family Fee Vouchers', icon: Wallet },
            { id: 'receipts', label: 'Payment Receipts', icon: FileText },
            { id: 'payroll', label: 'Staff Payroll', icon: Briefcase },
            { id: 'expenses', label: 'Expense Vouchers', icon: DollarSign },
            { id: 'library', label: 'Library Catalog', icon: Library },
            { id: 'transport', label: 'Transport Routes', icon: Bus },
            { id: 'id-cards', label: 'Student ID Cards (PDF)', icon: Award },
            { id: 'result-cards', label: 'Result Cards (PDF)', icon: FileBarChart },
            { id: 'school-assets', label: 'School Assets', icon: Archive },
            { id: 'reports', label: 'Reports Generator (PDF)', icon: FileText },
            { id: 'notices', label: 'School Notices', icon: Bell },
            { id: 'leave-requests', label: 'Leave Requests', icon: ClipboardList },
            { id: 'parent-accounts', label: 'Parent Accounts', icon: UserCog },
            { id: 'branding', label: 'School Profile & Branding', icon: Building2 },
            { id: 'system-settings', label: 'System Settings', icon: Settings }
          ]
        }
      ];
    }

    // 2. SCHOOL ADMIN PORTAL
    if (currentUser.role === 'admin') {
      return [
        {
          title: "OVERVIEW",
          items: [
            { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard }
          ]
        },
        {
          title: "ACADEMICS",
          items: [
            { id: 'students', label: 'Students & Admissions', icon: Users },
            { id: 'families', label: 'Families (Siblings)', icon: UserSquare2 },
            { id: 'staff', label: 'Teachers & Staff', icon: Briefcase },
            { id: 'classes', label: 'Classes & Sections', icon: Layers },
            { id: 'attendance', label: 'Student Attendance', icon: CheckCircle },
            { id: 'staff-attendance', label: 'Staff Attendance', icon: UserCheck },
            { id: 'scan-attendance', label: 'Scan QR Attendance', icon: Clock },
            { id: 'exams', label: 'Exams & Sessions', icon: Calendar },
            { id: 'exam-marks', label: 'Exam Schedule & Marks', icon: Award },
            { id: 'question-papers', label: 'Question Papers', icon: FileQuestion },
            { id: 'grade-settings', label: 'Grade Settings', icon: Award },
            { id: 'timetable', label: 'Master Timetable', icon: Clock },
            { id: 'subjects', label: 'Subjects', icon: BookOpen },
            { id: 'syllabus', label: 'Syllabus Outlines', icon: FileText }
          ]
        },
        {
          title: "FINANCE",
          items: [
            { id: 'fee-collection', label: 'Fee Collection', icon: DollarSign },
            { id: 'family-fee', label: 'Family Fee Collection', icon: Wallet },
            { id: 'receipts', label: 'Payment Receipts', icon: FileText },
            { id: 'payroll', label: 'Payroll & Salaries', icon: Briefcase },
            { id: 'expenses', label: 'Expense Vouchers', icon: DollarSign }
          ]
        },
        {
          title: "FACILITIES",
          items: [
            { id: 'library', label: 'Library Catalog', icon: Library },
            { id: 'transport', label: 'Transport Routes', icon: Bus }
          ]
        },
        {
          title: "SYSTEM & OUTPUTS",
          items: [
            { id: 'messages', label: 'Inter-School Messaging', icon: MessageSquare },
            { id: 'notices', label: 'Notices & Circulars', icon: Bell },
            { id: 'leave-requests', label: 'Faculty Leave Requests', icon: ClipboardList },
            { id: 'parent-accounts', label: 'Parent Portal Logins', icon: UserCog },
            { id: 'id-cards', label: 'Student ID Cards (PDF)', icon: Award },
            { id: 'result-cards', label: 'Student Result Cards (PDF)', icon: FileBarChart },
            { id: 'school-assets', label: 'Campus Asset Register', icon: Archive },
            { id: 'reports', label: 'Reports Generator (PDF)', icon: FileText },
            { id: 'branding', label: 'School Profile & Branding', icon: Building2 },
            { id: 'system-settings', label: 'System & SMS Settings', icon: Settings }
          ]
        }
      ];
    }

    // 3. TEACHER PORTAL (All working sidebar options)
    if (currentUser.role === 'teacher') {
      return [
        {
          title: "TEACHER WORKSPACE",
          items: [
            { id: 'dashboard', label: 'Teacher Dashboard', icon: LayoutDashboard },
            { id: 'teacher-classes', label: 'My Classes & Subjects', icon: Layers },
            { id: 'teacher-timetable', label: 'Timetable', icon: Clock },
            { id: 'teacher-attendance', label: 'Mark Attendance', icon: CheckCircle },
            { id: 'teacher-homework', label: 'Homework & Assignments', icon: BookOpen },
            { id: 'messages', label: 'Messages & Student Chat', icon: MessageSquare },
            { id: 'teacher-leave', label: 'Leave Application', icon: ClipboardList },
            { id: 'teacher-notices', label: 'School Notices', icon: Bell }
          ]
        }
      ];
    }

    // 4. STUDENT PORTAL (All working sidebar options)
    if (currentUser.role === 'student') {
      return [
        {
          title: "STUDENT WORKSPACE",
          items: [
            { id: 'dashboard', label: 'Student Dashboard', icon: LayoutDashboard },
            { id: 'student-timetable', label: 'My Class Timetable', icon: Clock },
            { id: 'student-attendance', label: 'Attendance Record', icon: CheckCircle },
            { id: 'student-results', label: 'Exam Results', icon: Award },
            { id: 'student-homework', label: 'Homework Diary', icon: BookOpen },
            { id: 'messages', label: 'Teacher Messages', icon: MessageSquare },
            { id: 'student-fees', label: 'Fee & Bank Vouchers', icon: DollarSign },
            { id: 'student-notices', label: 'Notice Board', icon: Bell }
          ]
        }
      ];
    }

    // 5. PARENT PORTAL (All working sidebar options)
    return [
      {
        title: "PARENT GUARDIAN WORKSPACE",
        items: [
          { id: 'dashboard', label: 'My Children', icon: Users },
          { id: 'parent-fees', label: 'Fee Vouchers & Pay', icon: DollarSign },
          { id: 'messages', label: 'Teacher & Admin Chat', icon: MessageSquare },
          { id: 'parent-notices', label: 'Announcement', icon: Bell }
        ]
      }
    ];
  };

  const navGroups = getNavGroups();

  return (
    <>
      {isOpen && (
        <div
          onClick={onClose}
          className="fixed inset-0 bg-slate-950/70 z-40 md:hidden backdrop-blur-xs transition-opacity"
        />
      )}

      <aside
        className={`fixed md:sticky top-0 md:top-16 z-50 md:z-20 h-screen md:h-[calc(100vh-4rem)] w-68 bg-[#0B1730] border-r border-slate-800 flex flex-col transition-transform duration-200 ease-in-out ${
          isOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        }`}
      >
        {/* User Card at top of sidebar */}
        <div className="p-3.5 border-b border-slate-800 bg-[#0E1E3F]">
          <div className="flex items-center gap-3">
            <SchoolLogo className="w-10 h-10" size={38} />
            <div className="flex-1 min-w-0">
              <div className="text-xs font-bold text-white truncate">
                {currentUser.name}
              </div>
              <div className="text-[10px] text-amber-400 font-semibold uppercase tracking-wider flex items-center gap-1">
                <span>{currentUser.role} PORTAL</span>
                <span className="text-[9px] text-slate-400 font-mono">({currentSchool.code})</span>
              </div>

              {/* Editable Email Section */}
              {isEditingEmail ? (
                <div className="mt-1 flex items-center gap-1">
                  <input
                    type="email"
                    value={emailDraft}
                    onChange={e => setEmailDraft(e.target.value)}
                    className="w-full text-[10px] py-0.5 px-1.5 rounded bg-slate-900 border border-indigo-400 text-white focus:outline-none font-mono"
                    placeholder="name@school.edu.pk"
                    autoFocus
                  />
                  <button
                    onClick={handleSaveEmail}
                    className="p-1 rounded bg-emerald-600 hover:bg-emerald-700 text-white shrink-0"
                    title="Save Email"
                  >
                    <Check className="w-3 h-3" />
                  </button>
                  <button
                    onClick={() => {
                      setEmailDraft(currentUser.email);
                      setIsEditingEmail(false);
                    }}
                    className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-400 shrink-0"
                    title="Cancel"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-1.5 mt-0.5 group">
                  <span className="text-[10px] text-slate-300 truncate max-w-[140px] font-mono">
                    {currentUser.email}
                  </span>
                  <button
                    onClick={() => {
                      setEmailDraft(currentUser.email);
                      setIsEditingEmail(true);
                    }}
                    className="text-slate-400 hover:text-amber-400 p-0.5 rounded transition"
                    title="Edit email address"
                  >
                    <Edit2 className="w-2.5 h-2.5" />
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Quick Campus Switcher for Owner right in Sidebar */}
          {isOwner && (
            <div className="mt-2.5 pt-2 border-t border-slate-700/60 flex items-center justify-between gap-1 text-[11px]">
              <span className="text-slate-400 font-medium">Campus:</span>
              <select
                value={currentSchool.id}
                onChange={e => switchSchool(e.target.value)}
                className="bg-slate-900 border border-slate-700 rounded-lg px-2 py-1 text-amber-300 font-bold font-mono text-[10px] focus:outline-none"
              >
                {schools.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.code} - {s.name.substring(0, 16)}...
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>

        {/* Scrollable Navigation items */}
        <div className="flex-1 overflow-y-auto px-3 py-3 space-y-4 text-xs scrollbar-thin scrollbar-thumb-slate-800">
          {navGroups.map((group, gIdx) => (
            <div key={gIdx} className="space-y-1">
              <div className="px-3 py-1 flex items-center justify-between text-[10px] font-bold text-slate-400 tracking-wider uppercase">
                <span>{group.title}</span>
                {currentUser.role === 'owner' && group.title.startsWith('INSPECT CAMPUS') && (
                  isOwnerInspectionMode ? (
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        turnOffOwnerInspection();
                      }}
                      className="px-2 py-0.5 rounded bg-rose-600/30 hover:bg-rose-600 text-rose-300 hover:text-white text-[9px] font-bold transition flex items-center gap-1"
                      title="Turn Inspection Mode OFF"
                    >
                      <Power className="w-2.5 h-2.5" />
                      <span>Turn OFF</span>
                    </button>
                  ) : (
                    <span className="text-[9px] text-slate-500 bg-slate-800 px-1.5 py-0.5 rounded font-mono">
                      OFF
                    </span>
                  )
                )}
              </div>
              {group.items.map(item => {
                const IconComp = item.icon;
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => handleSelect(item.id)}
                    className={`w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-left transition font-medium ${
                      isActive
                        ? 'bg-indigo-600 text-white shadow-xs font-semibold'
                        : 'text-slate-300 hover:bg-slate-800/80 hover:text-white'
                    }`}
                  >
                    <IconComp className={`w-4 h-4 shrink-0 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                    <span className="truncate">{item.label}</span>
                    {isActive && (
                      <ChevronRight className="w-3.5 h-3.5 ml-auto opacity-75" />
                    )}
                  </button>
                );
              })}
            </div>
          ))}
        </div>

        {/* Bottom Pinned Footer: Download APK and SIGN OUT BUTTON AT THE VERY BOTTOM */}
        <div className="p-3 border-t border-slate-800 text-[11px] text-slate-400 bg-slate-900/95 space-y-2 mt-auto">
          {/* Download APK Package button */}
          <button
            onClick={openDownloadPackageModal}
            className="w-full py-2 px-2.5 bg-gradient-to-r from-emerald-600 to-indigo-600 hover:from-emerald-700 hover:to-indigo-700 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition shadow-xs"
          >
            <Download className="w-3.5 h-3.5 text-amber-300" />
            <span>Download APK (5.8MB)</span>
          </button>

          {/* School Version & Session */}
          <div className="flex items-center justify-between text-[10px] px-1 text-slate-400">
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              <span className="text-slate-300 font-medium">{branding.name.split(' ')[0]} v2.28</span>
            </span>
            <span className="font-mono">
              {branding.session}
            </span>
          </div>

          {/* SIGN OUT BUTTON AT THE VERY BOTTOM OF SIDEBAR MENU */}
          <button
            onClick={() => setShowSignOutConfirm(true)}
            className="w-full py-2.5 px-3 rounded-xl bg-rose-600/15 hover:bg-rose-600 text-rose-300 hover:text-white border border-rose-500/40 hover:border-rose-600 font-bold text-xs flex items-center justify-center gap-2 transition shadow-sm"
            title="Sign Out of Session"
          >
            <LogOut className="w-4 h-4 text-rose-400" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* Sign Out Confirmation Modal */}
      {showSignOutConfirm && (
        <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl max-w-sm w-full p-6 text-white shadow-2xl relative animate-in fade-in zoom-in-95 text-xs">
            <button
              onClick={() => setShowSignOutConfirm(false)}
              className="absolute top-4 right-4 p-1.5 rounded-full hover:bg-slate-800 text-slate-400 hover:text-white"
            >
              <X className="w-4 h-4" />
            </button>

            <div className="w-12 h-12 rounded-2xl bg-rose-500/20 border border-rose-500/40 text-rose-400 flex items-center justify-center mb-3">
              <LogOut className="w-6 h-6" />
            </div>

            <h3 className="text-base font-bold text-white">Sign Out of Session</h3>
            <p className="text-slate-300 text-xs mt-1 leading-relaxed">
              You are currently logged in as <strong className="text-white">{currentUser.name}</strong> ({currentUser.role} portal). Choose how you would like to proceed:
            </p>

            <div className="space-y-2 mt-4">
              <button
                onClick={handleSignOutToDemo}
                className="w-full py-2.5 px-3 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-md transition"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span>Sign Out (Switch to Visitor Demo)</span>
              </button>

              <div className="pt-2 border-t border-slate-800 text-[11px] text-slate-400 text-center font-semibold">
                Or Switch Role Perspective:
              </div>

              <div className="grid grid-cols-2 gap-1.5 text-[11px]">
                <button
                  onClick={() => handleSignOutToRole('owner')}
                  className="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-200 font-bold text-center"
                >
                  Owner Console
                </button>
                <button
                  onClick={() => handleSignOutToRole('admin')}
                  className="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-200 font-bold text-center"
                >
                  Campus Admin
                </button>
                <button
                  onClick={() => handleSignOutToRole('teacher')}
                  className="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-200 font-bold text-center"
                >
                  Teacher Portal
                </button>
                <button
                  onClick={() => handleSignOutToRole('student')}
                  className="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-200 font-bold text-center"
                >
                  Student Portal
                </button>
                <button
                  onClick={() => handleSignOutToRole('parent')}
                  className="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-slate-200 font-bold text-center col-span-2"
                >
                  Parent Portal
                </button>
              </div>

              <button
                onClick={() => setShowSignOutConfirm(false)}
                className="w-full py-2 rounded-xl border border-slate-700 text-slate-400 hover:text-white font-bold text-xs transition mt-2"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
