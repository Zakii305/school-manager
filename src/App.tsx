import React, { useState } from 'react';
import { SchoolProvider, useSchool } from './context/SchoolContext';
import { Navbar } from './components/layout/Navbar';
import { Sidebar } from './components/layout/Sidebar';
import { AdminDashboard } from './components/admin/AdminDashboard';
import { StudentsManager } from './components/admin/StudentsManager';
import { FamiliesManager } from './components/admin/FamiliesManager';
import { StaffManager } from './components/admin/StaffManager';
import { ClassesManager } from './components/admin/ClassesManager';
import { AttendanceManager } from './components/admin/AttendanceManager';
import { ExamsManager } from './components/admin/ExamsManager';
import { TimetableManager } from './components/admin/TimetableManager';
import { FinanceManager } from './components/admin/FinanceManager';
import { FacilitiesAndSystem } from './components/admin/FacilitiesAndSystem';
import { SettingsManager } from './components/admin/SettingsManager';
import { OwnerConsole } from './components/owner/OwnerConsole';
import { TeacherPortal } from './components/teacher/TeacherPortal';
import { StudentPortal } from './components/student/StudentPortal';
import { ParentPortal } from './components/parent/ParentPortal';
import { MessagesManager } from './components/common/MessagesManager';
import { GlobalSearchModal } from './components/common/GlobalSearchModal';
import { RegistrationModal } from './components/common/RegistrationModal';
import { DownloadPackageModal } from './components/common/DownloadPackageModal';
import { PrintModals } from './components/modals/PrintModals';
import { Building2, Shield, Eye, ArrowLeft, Radio, Power, Users, Check, ChevronDown } from 'lucide-react';

const AppContent: React.FC = () => {
  const {
    currentUser, activeTab, setActiveTab,
    isOwner, currentSchool, schools, switchSchool,
    isOwnerInspectionMode, ownerInspectedRole, setOwnerInspectedRole,
    turnOffOwnerInspection, turnOnOwnerInspection
  } = useSchool();

  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  // Helper to render Admin Tab Content (usable by Admin OR Owner inspecting a school)
  const renderAdminView = () => {
    switch (activeTab) {
      case 'dashboard':
        return <AdminDashboard />;
      case 'students':
        return <StudentsManager />;
      case 'families':
        return <FamiliesManager />;
      case 'staff':
        return <StaffManager />;
      case 'classes':
      case 'subjects':
      case 'syllabus':
        return <ClassesManager />;
      case 'attendance':
      case 'staff-attendance':
      case 'scan-attendance':
        return <AttendanceManager />;
      case 'exams':
      case 'exam-marks':
      case 'question-papers':
      case 'grade-settings':
        return <ExamsManager />;
      case 'timetable':
        return <TimetableManager />;
      case 'fee-collection':
      case 'family-fee':
      case 'receipts':
      case 'payroll':
      case 'expenses':
        return <FinanceManager />;
      case 'library':
        return <FacilitiesAndSystem section="library" />;
      case 'transport':
        return <FacilitiesAndSystem section="transport" />;
      case 'notices':
        return <FacilitiesAndSystem section="notices" />;
      case 'leave-requests':
        return <FacilitiesAndSystem section="leave" />;
      case 'school-assets':
        return <FacilitiesAndSystem section="assets" />;
      case 'reports':
        return <FacilitiesAndSystem section="reports" />;
      case 'id-cards':
        return <FacilitiesAndSystem section="id-cards" />;
      case 'result-cards':
        return <FacilitiesAndSystem section="result-cards" />;
      case 'parent-accounts':
        return <FacilitiesAndSystem section="parent-accounts" />;
      case 'branding':
      case 'system-settings':
        return <SettingsManager />;
      case 'messages':
        return <MessagesManager />;
      default:
        return <AdminDashboard />;
    }
  };

  // Main View Router
  const renderCurrentView = () => {
    // 1. OWNER ROLE
    if (currentUser.role === 'owner') {
      // If inspection mode is OFF or owner is viewing owner console tabs
      if (!isOwnerInspectionMode || activeTab.startsWith('owner-')) {
        if (activeTab === 'messages') {
          return <MessagesManager />;
        }
        return <OwnerConsole />;
      }
      if (activeTab === 'messages') {
        return <MessagesManager />;
      }

      // Owner is inspecting school with active inspection mode!
      const renderInspectedPerspective = () => {
        if (ownerInspectedRole === 'teacher') return <TeacherPortal />;
        if (ownerInspectedRole === 'student') return <StudentPortal />;
        if (ownerInspectedRole === 'parent') return <ParentPortal />;
        return renderAdminView();
      };

      return (
        <div className="space-y-4">
          {/* Owner Campus Inspection Bar */}
          <div className="bg-gradient-to-r from-purple-900 via-indigo-900 to-slate-900 border border-purple-700/60 p-3 sm:p-4 rounded-2xl text-white shadow-lg flex flex-col md:flex-row md:items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-purple-500/20 border border-purple-400/30 flex items-center justify-center text-purple-300 shrink-0">
                <Eye className="w-5 h-5 text-amber-300" />
              </div>
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-amber-400 text-slate-950 px-2 py-0.5 rounded font-mono">
                    INSPECTION MODE: ACTIVE
                  </span>
                  <span className="text-xs font-mono text-purple-200">
                    Campus: <strong className="text-white">{currentSchool.name}</strong> ({currentSchool.code})
                  </span>
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-indigo-500/40 text-indigo-200 px-2 py-0.5 rounded font-mono border border-indigo-400/30">
                    Role: {ownerInspectedRole.toUpperCase()}
                  </span>
                </div>
                <p className="text-[11px] text-purple-200 mt-0.5 truncate">
                  Real-time live multi-tenant inspection. Switch campuses and roles or turn inspection OFF.
                </p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 shrink-0">
              {/* Switch Campus */}
              <select
                value={currentSchool.id}
                onChange={e => switchSchool(e.target.value)}
                className="px-2.5 py-1.5 rounded-xl bg-purple-950/90 border border-purple-500 text-xs font-bold text-amber-300 focus:outline-none"
                title="Switch Inspected Campus"
              >
                {schools.map(s => (
                  <option key={s.id} value={s.id}>
                    Campus: {s.name} ({s.code})
                  </option>
                ))}
              </select>

              {/* Switch Inspected Perspective */}
              <select
                value={ownerInspectedRole}
                onChange={e => setOwnerInspectedRole(e.target.value as any)}
                className="px-2.5 py-1.5 rounded-xl bg-indigo-950/90 border border-indigo-500 text-xs font-bold text-indigo-200 focus:outline-none"
                title="Switch Inspected Perspective"
              >
                <option value="admin">Inspect as School Admin</option>
                <option value="teacher">Inspect as Teacher Portal</option>
                <option value="student">Inspect as Student Portal</option>
                <option value="parent">Inspect as Parent Portal</option>
              </select>

              {/* Turn Inspection OFF button */}
              <button
                onClick={turnOffOwnerInspection}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs shadow-md transition"
                title="Turn Inspection Mode OFF and return to Owner Console"
              >
                <Power className="w-3.5 h-3.5" />
                <span>Turn OFF Inspection</span>
              </button>
            </div>
          </div>

          {/* Render the full view for this school */}
          {renderInspectedPerspective()}
        </div>
      );
    }

    // 2. SCHOOL ADMIN ROLE
    if (currentUser.role === 'admin') {
      return renderAdminView();
    }

    // 3. TEACHER ROLE
    if (currentUser.role === 'teacher') {
      if (activeTab === 'messages') return <MessagesManager />;
      return <TeacherPortal />;
    }

    // 4. STUDENT ROLE
    if (currentUser.role === 'student') {
      if (activeTab === 'messages') return <MessagesManager />;
      return <StudentPortal />;
    }

    // 5. PARENT ROLE
    if (currentUser.role === 'parent') {
      if (activeTab === 'messages') return <MessagesManager />;
      return <ParentPortal />;
    }

    return <AdminDashboard />;
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans antialiased text-slate-800 selection:bg-indigo-600 selection:text-white">
      {/* Top Navbar */}
      <Navbar
        onToggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)}
        onOpenSearch={() => setIsSearchOpen(true)}
      />

      {/* Main Layout Body */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Sidebar */}
        <Sidebar
          isOpen={isSidebarOpen}
          onClose={() => setIsSidebarOpen(false)}
        />

        {/* Center Content View */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full">
          {renderCurrentView()}
        </main>
      </div>

      {/* Global Interactive Modals */}
      <GlobalSearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
      />
      <RegistrationModal />
      <DownloadPackageModal />
      <PrintModals />
    </div>
  );
};

export default function App() {
  return (
    <SchoolProvider>
      <AppContent />
    </SchoolProvider>
  );
}
