import React, { useState, useRef, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { UserRole } from '../../types';
import {
  Search, Shield, ChevronDown, Check,
  ArrowLeft, X,
  Download, UserPlus, Building2, Eye, Copy,
  RefreshCw, MessageSquare, Power, EyeOff, Radio
} from 'lucide-react';
import { SchoolLogo } from '../common/SchoolLogo';

interface NavbarProps {
  onToggleSidebar?: () => void;
  onOpenSearch?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ onToggleSidebar, onOpenSearch }) => {
  const {
    currentUser, switchRole, branding, currentSchool, schools, switchSchool,
    isOwner, isDemoVisitor, toggleDemoVisitorMode, getDemoShareUrl,
    activeTab, setActiveTab, goBack,
    students, staff, invoices, routes,
    openDownloadPackageModal, openRegistrationModal, triggerRealtimeSync,
    isOwnerInspectionMode, ownerInspectedRole, setOwnerInspectedRole,
    toggleOwnerInspectionMode, turnOffOwnerInspection, turnOnOwnerInspection
  } = useSchool();

  const [roleMenuOpen, setRoleMenuOpen] = useState(false);
  const [schoolMenuOpen, setSchoolMenuOpen] = useState(false);
  const [inspectionMenuOpen, setInspectionMenuOpen] = useState(false);
  const [topSearchQuery, setTopSearchQuery] = useState('');
  const [searchFocused, setSearchFocused] = useState(false);
  const [copiedDemo, setCopiedDemo] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);
  const searchContainerRef = useRef<HTMLDivElement>(null);
  const mobileSearchInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (searchContainerRef.current && !searchContainerRef.current.contains(e.target as Node)) {
        setSearchFocused(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (isMobileSearchOpen && mobileSearchInputRef.current) {
      mobileSearchInputRef.current.focus();
    }
  }, [isMobileSearchOpen]);

  const roles: { role: UserRole; title: string; badge: string; color: string; dotColor: string }[] = [
    { role: 'owner', title: 'System Owner', badge: 'socialman121@gmail.com', color: 'bg-purple-600', dotColor: 'bg-purple-400' },
    { role: 'admin', title: 'School Admin', badge: 'Campus Management', color: 'bg-indigo-600', dotColor: 'bg-indigo-400' },
    { role: 'teacher', title: 'Teacher Portal', badge: 'Academics & Marks', color: 'bg-emerald-600', dotColor: 'bg-emerald-400' },
    { role: 'student', title: 'Student Portal', badge: 'Results & Diary', color: 'bg-blue-600', dotColor: 'bg-blue-400' },
    { role: 'parent', title: 'Parent Portal', badge: 'Children & Fees', color: 'bg-amber-600', dotColor: 'bg-amber-400' }
  ];

  const currentRoleConfig = roles.find(r => r.role === currentUser.role) || roles[0];

  const handleCopyUniversalDemo = () => {
    const url = getDemoShareUrl();
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(url);
    }
    setCopiedDemo(true);
    setTimeout(() => setCopiedDemo(false), 3000);
  };

  // Quick live search matches
  const q = topSearchQuery.trim().toLowerCase();
  const matchedStudents = q
    ? students.filter(s => s.name.toLowerCase().includes(q) || s.admissionNo.toLowerCase().includes(q) || s.className.toLowerCase().includes(q)).slice(0, 3)
    : [];
  const matchedStaff = q
    ? staff.filter(st => st.name.toLowerCase().includes(q) || st.designation.toLowerCase().includes(q)).slice(0, 2)
    : [];
  const matchedVouchers = q
    ? invoices.filter(inv => inv.voucherNo.toLowerCase().includes(q) || inv.studentName.toLowerCase().includes(q)).slice(0, 2)
    : [];
  const matchedRoutes = q
    ? routes.filter(r => r.route.toLowerCase().includes(q) || r.driver.toLowerCase().includes(q)).slice(0, 2)
    : [];
  const totalMatches = matchedStudents.length + matchedStaff.length + matchedVouchers.length + matchedRoutes.length;

  return (
    <>
      {/* Demo Visitor Mode Banner */}
      {isDemoVisitor && (
        <div className="bg-gradient-to-r from-amber-500 via-amber-600 to-yellow-500 text-slate-950 font-bold px-3 py-1 text-xs flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-1.5 truncate">
            <Eye className="w-3.5 h-3.5 text-slate-950 shrink-0" />
            <span className="truncate text-[11px] sm:text-xs">
              <strong>Demo Visitor Mode:</strong> Explore all roles and facilities safely.
            </span>
          </div>
          <button
            onClick={toggleDemoVisitorMode}
            className="text-[10px] sm:text-[11px] underline font-extrabold hover:text-white shrink-0 ml-2"
          >
            Exit Demo
          </button>
        </div>
      )}

      {/* Main Top Header Bar - Optimized for Mobile & Desktop across all roles */}
      <header className="sticky top-0 z-30 bg-[#0B1730] border-b border-slate-800 text-white shadow-md w-full overflow-x-hidden">
        <div className="max-w-7xl mx-auto px-2 sm:px-4 md:px-6 h-14 sm:h-16 flex items-center justify-between gap-1 sm:gap-2.5">
          
          {/* Left: Hamburger, Back Button, School Monogram & Title */}
          <div className="flex items-center gap-1 sm:gap-2 min-w-0 shrink">
            {/* Mobile Hamburger Menu Toggle */}
            <button
              onClick={onToggleSidebar}
              className="md:hidden p-1.5 sm:p-2 rounded-xl bg-slate-800/90 hover:bg-slate-700 text-slate-200 focus:outline-none shrink-0"
              title="Open Navigation Menu"
              aria-label="Open Navigation Menu"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            {/* Back Button (Role-Aware for all roles) */}
            {(() => {
              if (currentUser.role === 'owner') {
                if (isOwnerInspectionMode) {
                  return (
                    <button
                      onClick={goBack}
                      className="flex items-center gap-1 p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl bg-purple-600 hover:bg-purple-700 text-white text-xs font-bold transition shadow-xs shrink-0"
                      title={activeTab === 'dashboard' ? "Exit Inspection Mode" : "Back to Admin Dashboard"}
                    >
                      <ArrowLeft className="w-3.5 h-3.5 text-amber-300" />
                      <span className="hidden sm:inline">{activeTab === 'dashboard' ? 'Exit Inspection' : 'Back'}</span>
                    </button>
                  );
                }
                if (activeTab !== 'owner-overview') {
                  return (
                    <button
                      onClick={goBack}
                      className="flex items-center gap-1 p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold transition shadow-xs shrink-0"
                      title="Back to Platform Overview"
                    >
                      <ArrowLeft className="w-3.5 h-3.5 text-amber-300" />
                      <span className="hidden sm:inline">Overview</span>
                    </button>
                  );
                }
                return null;
              }

              // Admin, Teacher, Student, Parent
              if (activeTab !== 'dashboard') {
                return (
                  <button
                    onClick={goBack}
                    className="flex items-center gap-1 p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold transition shadow-xs shrink-0"
                    title="Back to Dashboard"
                  >
                    <ArrowLeft className="w-3.5 h-3.5 text-amber-300" />
                    <span className="hidden sm:inline">Back</span>
                  </button>
                );
              }
              return null;
            })()}

            {/* School Branding & Monogram with SchoolLogo SVG */}
            <div
              onClick={() => {
                if (currentUser.role === 'owner' && !isOwnerInspectionMode) {
                  setActiveTab('owner-overview');
                } else {
                  setActiveTab('dashboard');
                }
              }}
              className="flex items-center gap-1.5 min-w-0 shrink cursor-pointer hover:opacity-95 transition"
              title="Go to Home"
            >
              <SchoolLogo className="w-7 h-7 sm:w-8 sm:h-8" size={30} />

              <div className="min-w-0">
                <div className="flex items-center gap-1 leading-tight">
                  <span className="font-bold text-xs sm:text-sm text-white truncate max-w-[70px] xs:max-w-[100px] sm:max-w-[180px] md:max-w-xs">
                    {branding.name}
                  </span>
                  <span className="hidden xs:inline-block text-[9px] uppercase font-mono font-bold px-1.5 py-0.5 rounded bg-amber-400 text-slate-950 shrink-0">
                    {currentSchool.code}
                  </span>
                </div>
                <div className="hidden md:block text-[10px] text-slate-400 font-medium truncate max-w-[180px]">
                  {branding.tagline}
                </div>
              </div>
            </div>

            {/* OWNER INSPECTION CONTROLS (ON / OFF + PERSPECTIVE SWITCHER) */}
            {currentUser.role === 'owner' && isOwnerInspectionMode && (
              <div className="relative shrink-0 ml-0.5">
                <div className="flex items-center rounded-xl bg-purple-950/90 border border-purple-500/70 shadow-xs overflow-hidden text-xs">
                  <button
                    onClick={() => setInspectionMenuOpen(!inspectionMenuOpen)}
                    className="flex items-center gap-1 px-1.5 sm:px-2 py-1 sm:py-1.5 text-xs font-bold text-amber-300 hover:bg-purple-900/60 transition"
                    title="Inspection Mode is ACTIVE. Click to switch campus or perspective."
                  >
                    <Eye className="w-3.5 h-3.5 text-amber-400 animate-pulse shrink-0" />
                    <span className="hidden lg:inline text-[10px] text-purple-200">Inspect:</span>
                    <span className="font-mono text-[10px] sm:text-xs">{currentSchool.code}</span>
                    <ChevronDown className="w-2.5 h-2.5 text-purple-300 shrink-0" />
                  </button>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      turnOffOwnerInspection();
                    }}
                    className="px-1.5 py-1 sm:py-1.5 bg-rose-600 hover:bg-rose-700 text-white font-bold text-[10px] tracking-tight transition flex items-center gap-0.5 border-l border-purple-700"
                    title="Turn Inspection Mode OFF"
                  >
                    <Power className="w-2.5 h-2.5" />
                    <span>OFF</span>
                  </button>
                </div>

                {inspectionMenuOpen && (
                  <div className="absolute left-0 mt-2 w-72 bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl py-2 z-50 text-xs animate-in fade-in zoom-in-95">
                    <div className="px-3 py-1.5 text-[10px] font-bold uppercase text-slate-400 tracking-wider border-b border-slate-800 flex items-center justify-between">
                      <span>Inspection Control</span>
                      <span className="text-amber-400 text-[9px] font-mono">Owner Live Mode</span>
                    </div>

                    <div className="p-2 space-y-1">
                      <div className="text-[10px] uppercase font-bold text-slate-400 px-2 pt-1">
                        Switch Inspected Campus:
                      </div>
                      {schools.map(s => (
                        <button
                          key={s.id}
                          onClick={() => {
                            switchSchool(s.id);
                            turnOnOwnerInspection(s.id, 'dashboard', ownerInspectedRole);
                            setInspectionMenuOpen(false);
                          }}
                          className={`w-full text-left px-3 py-1.5 rounded-xl flex items-center justify-between hover:bg-slate-800 transition ${
                            s.id === currentSchool.id ? 'bg-indigo-600/30 text-white font-bold' : 'text-slate-300'
                          }`}
                        >
                          <div>
                            <div className="font-bold text-white text-xs">{s.name}</div>
                            <div className="text-[10px] text-slate-400 font-mono">Code: {s.code}</div>
                          </div>
                          {s.id === currentSchool.id && (
                            <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                          )}
                        </button>
                      ))}
                    </div>

                    <div className="p-2 border-t border-slate-800 space-y-1">
                      <div className="text-[10px] uppercase font-bold text-slate-400 px-2 pt-1">
                        Inspect As Perspective:
                      </div>
                      {(['admin', 'teacher', 'student', 'parent'] as const).map(r => (
                        <button
                          key={r}
                          onClick={() => {
                            setOwnerInspectedRole(r);
                            setInspectionMenuOpen(false);
                          }}
                          className={`w-full text-left px-3 py-1 rounded-lg text-xs capitalize flex items-center justify-between transition ${
                            ownerInspectedRole === r ? 'bg-purple-600/30 text-amber-300 font-bold' : 'text-slate-300 hover:bg-slate-800'
                          }`}
                        >
                          <span>{r} perspective</span>
                          {ownerInspectedRole === r && <Check className="w-3.5 h-3.5 text-amber-300" />}
                        </button>
                      ))}
                    </div>

                    <div className="p-2 border-t border-slate-800">
                      <button
                        onClick={() => {
                          turnOffOwnerInspection();
                          setInspectionMenuOpen(false);
                        }}
                        className="w-full py-1.5 px-3 rounded-xl bg-rose-600/20 hover:bg-rose-600 border border-rose-500/40 text-rose-300 hover:text-white font-bold text-xs flex items-center justify-center gap-1.5 transition"
                      >
                        <Power className="w-3.5 h-3.5" />
                        <span>Turn Inspection Mode OFF</span>
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* When Owner has Inspection OFF: Show compact Turn ON button */}
            {currentUser.role === 'owner' && !isOwnerInspectionMode && (
              <div className="relative shrink-0 ml-0.5">
                <button
                  onClick={() => setInspectionMenuOpen(!inspectionMenuOpen)}
                  className="flex items-center gap-1 px-1.5 sm:px-2 py-1 sm:py-1.5 rounded-xl bg-slate-800/80 hover:bg-slate-700 border border-slate-700 text-xs font-bold text-slate-300 transition"
                  title="Owner Campus Inspection is OFF. Tap to inspect a school."
                >
                  <EyeOff className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  <span className="hidden lg:inline text-[10px] text-slate-400">Inspect:</span>
                  <span className="text-[10px] bg-slate-700 text-slate-300 px-1 py-0.5 rounded font-mono font-bold">OFF</span>
                  <ChevronDown className="w-2.5 h-2.5 text-slate-400 shrink-0" />
                </button>

                {inspectionMenuOpen && (
                  <div className="absolute left-0 mt-2 w-72 bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl p-2 z-50 text-xs animate-in fade-in zoom-in-95 space-y-2">
                    <div className="p-2 border-b border-slate-800 text-[10px] font-bold uppercase text-slate-400 flex items-center justify-between">
                      <span>Turn ON Campus Inspection</span>
                      <span className="text-amber-400 text-[9px] font-mono">Multi-Tenant</span>
                    </div>
                    <p className="text-[11px] text-slate-300 px-1">
                      Choose any allotted school campus to start live inspection:
                    </p>
                    <div className="space-y-1">
                      {schools.map(s => (
                        <button
                          key={s.id}
                          onClick={() => {
                            turnOnOwnerInspection(s.id, 'dashboard', 'admin');
                            setInspectionMenuOpen(false);
                          }}
                          className="w-full text-left p-2 rounded-xl hover:bg-slate-800 transition flex items-center justify-between text-white"
                        >
                          <div>
                            <div className="font-bold text-xs">{s.name}</div>
                            <div className="text-[10px] text-slate-400 font-mono">Code: {s.code}</div>
                          </div>
                          <span className="text-[10px] bg-indigo-600 text-white font-bold px-2 py-0.5 rounded">
                            Turn ON
                          </span>
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* Campus Switcher Dropdown (for School Admin) */}
            {currentUser.role === 'admin' && (
              <div className="relative shrink-0 ml-0.5">
                <button
                  onClick={() => setSchoolMenuOpen(!schoolMenuOpen)}
                  className="flex items-center gap-1 px-1.5 sm:px-2 py-1 sm:py-1.5 bg-slate-800/90 hover:bg-slate-700 border border-slate-700 rounded-xl text-xs font-bold text-amber-300 transition"
                  title="Switch Campus"
                >
                  <Building2 className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                  <span className="hidden lg:inline text-[11px]">Campus:</span>
                  <span className="font-mono text-[10px] sm:text-xs">{currentSchool.code}</span>
                  <ChevronDown className="w-2.5 h-2.5 text-slate-400 shrink-0" />
                </button>

                {schoolMenuOpen && (
                  <div className="absolute left-0 mt-2 w-72 bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl py-2 z-50 text-xs animate-in fade-in zoom-in-95">
                    <div className="px-3 py-1.5 text-[10px] font-bold uppercase text-slate-400 tracking-wider border-b border-slate-800 flex items-center justify-between">
                      <span>Available Schools ({schools.length})</span>
                      <span className="text-amber-400 text-[9px] font-mono">Admin Switch</span>
                    </div>
                    {schools.map(s => (
                      <button
                        key={s.id}
                        onClick={() => {
                          switchSchool(s.id);
                          setSchoolMenuOpen(false);
                        }}
                        className={`w-full text-left px-3 py-2 flex items-center justify-between hover:bg-slate-800 transition ${
                          s.id === currentSchool.id ? 'bg-indigo-600/30 text-white font-bold' : 'text-slate-300'
                        }`}
                      >
                        <div>
                          <div className="font-bold text-white text-xs">{s.name}</div>
                          <div className="text-[10px] text-slate-400 font-mono">
                            Code: {s.code} • {s.studentCount} Students
                          </div>
                        </div>
                        {s.id === currentSchool.id && (
                          <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                        )}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Center: Desktop Inline Search Bar (Hidden on Mobile) */}
          <div ref={searchContainerRef} className="hidden md:flex flex-1 max-w-sm lg:max-w-md mx-2 lg:mx-4 relative">
            <div className="relative flex items-center w-full">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
              <input
                type="text"
                value={topSearchQuery}
                onChange={e => {
                  setTopSearchQuery(e.target.value);
                  setSearchFocused(true);
                }}
                onFocus={() => setSearchFocused(true)}
                onKeyDown={e => {
                  if (e.key === 'Enter' && onOpenSearch) onOpenSearch();
                }}
                placeholder="Search students, staff, vouchers, routes..."
                className="w-full pl-9 pr-8 py-1.5 rounded-xl bg-slate-900/90 border border-slate-700 focus:border-indigo-500 focus:bg-slate-900 text-white placeholder-slate-400 text-xs transition focus:outline-none"
              />
              {topSearchQuery && (
                <button
                  onClick={() => setTopSearchQuery('')}
                  className="absolute right-2.5 p-1 text-slate-400 hover:text-white"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              )}
            </div>

            {/* Desktop Quick Search Results */}
            {searchFocused && topSearchQuery.trim().length > 0 && (
              <div className="absolute left-0 right-0 top-10 mt-1 bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl overflow-hidden z-50 max-h-96 overflow-y-auto">
                <div className="p-2 border-b border-slate-800 text-[10px] font-bold text-slate-400 uppercase tracking-wider flex items-center justify-between">
                  <span>Results for &ldquo;{topSearchQuery}&rdquo;</span>
                  <span>{totalMatches} Found</span>
                </div>
                {totalMatches === 0 ? (
                  <div className="p-4 text-center text-xs text-slate-400">
                    No matching records found.
                  </div>
                ) : (
                  <div className="p-2 space-y-1 text-xs">
                    {matchedStudents.map(st => (
                      <button
                        key={st.id}
                        onClick={() => {
                          setActiveTab('students');
                          setSearchFocused(false);
                        }}
                        className="w-full flex items-center justify-between p-2 rounded-xl hover:bg-slate-800 text-left transition"
                      >
                        <div>
                          <div className="font-bold text-white text-xs">{st.name}</div>
                          <div className="text-[10px] text-slate-400">
                            Roll #{st.rollNo} • {st.className}
                          </div>
                        </div>
                        <span className="text-[10px] px-2 py-0.5 bg-indigo-950 text-indigo-300 font-mono rounded">
                          Student
                        </span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Right Section: Mobile Search Toggle, Demo Link, APK Button, Messages, Role Selector */}
          <div className="flex items-center gap-1 sm:gap-1.5 shrink-0">
            {/* Mobile Search Toggle Button */}
            <button
              onClick={() => setIsMobileSearchOpen(!isMobileSearchOpen)}
              className="md:hidden p-1.5 sm:p-2 rounded-xl bg-slate-800/90 hover:bg-slate-700 text-slate-300 hover:text-white transition shrink-0"
              title="Search"
              aria-label="Toggle Search"
            >
              <Search className="w-4 h-4 text-slate-300" />
            </button>

            {/* Desktop Real-time Live Sync Indicator */}
            <div
              onClick={triggerRealtimeSync}
              className="hidden lg:flex items-center gap-1.5 px-2.5 py-1 rounded-xl bg-emerald-950/60 border border-emerald-500/30 text-emerald-400 text-[10px] font-bold cursor-pointer hover:bg-emerald-900/60 transition shrink-0"
              title="Real-time live sync active. Click to refresh."
            >
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span>LIVE SYNC</span>
              <RefreshCw className="w-3 h-3 ml-0.5 text-emerald-400/80" />
            </div>

            {/* Desktop Single Universal Demo Link Button */}
            <button
              onClick={handleCopyUniversalDemo}
              className="hidden sm:flex items-center gap-1.5 px-2 sm:px-2.5 py-1.5 rounded-xl bg-amber-500/10 hover:bg-amber-500/20 border border-amber-400/30 text-xs font-bold text-amber-300 transition shrink-0"
              title="Copy the Universal Demo Link"
            >
              {copiedDemo ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5 text-amber-400" />}
              <span className="hidden md:inline">{copiedDemo ? "Copied!" : "Demo Link"}</span>
            </button>

            {/* Download Mobile APK Package Button */}
            <button
              onClick={openDownloadPackageModal}
              className="flex items-center gap-1 p-1.5 sm:px-2.5 sm:py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-xs font-bold text-white transition shadow-sm shrink-0"
              title="Install App & Download Android APK"
            >
              <Download className="w-3.5 h-3.5 text-white" />
              <span className="hidden sm:inline">Install APK</span>
            </button>

            {/* Messages Button */}
            <button
              onClick={() => {
                if (activeTab === 'messages') {
                  goBack();
                } else {
                  setActiveTab('messages');
                }
              }}
              className={`p-1.5 sm:p-2 rounded-xl border transition flex items-center justify-center shrink-0 ${
                activeTab === 'messages'
                  ? 'bg-indigo-600 border-indigo-400 text-white'
                  : 'bg-slate-800/90 hover:bg-slate-700 border-slate-700 text-slate-300 hover:text-white'
              }`}
              title="Campus & Inter-School Messaging"
            >
              <MessageSquare className="w-3.5 h-3.5" />
            </button>

            {/* Role Switcher Menu (Tailored to mobile and desktop for all roles) */}
            <div className="relative shrink-0">
              <button
                onClick={() => setRoleMenuOpen(!roleMenuOpen)}
                className="flex items-center gap-1 sm:gap-1.5 px-1.5 sm:px-2.5 py-1 sm:py-1.5 rounded-xl bg-indigo-950/90 hover:bg-indigo-900 border border-indigo-700/60 text-indigo-200 text-xs font-medium transition shadow-sm"
                title={`Switch Role Perspective (Current: ${currentUser.role})`}
              >
                <span className={`w-2 h-2 rounded-full ${currentRoleConfig.dotColor} shrink-0`} />
                <span className="font-bold capitalize text-white text-[11px] sm:text-xs truncate max-w-[50px] xs:max-w-none">
                  {currentUser.role}
                </span>
                <ChevronDown className="w-3 h-3 opacity-70 shrink-0" />
              </button>

              {roleMenuOpen && (
                <div className="absolute right-0 mt-2 w-64 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl py-2 z-50 animate-in fade-in zoom-in-95 duration-100">
                  <div className="px-3 py-1.5 border-b border-slate-800 mb-1">
                    <div className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                      Switch Perspective
                    </div>
                    <div className="text-[10px] text-slate-500">
                      Experience all 5 role portals
                    </div>
                  </div>

                  {roles.map(r => (
                    <button
                      key={r.role}
                      onClick={() => {
                        switchRole(r.role);
                        setRoleMenuOpen(false);
                      }}
                      className={`w-full flex items-center justify-between px-3 py-2 text-xs text-left transition ${
                        currentUser.role === r.role
                          ? 'bg-indigo-600/20 text-white font-semibold'
                          : 'text-slate-300 hover:bg-slate-800/80'
                      }`}
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className={`w-2 h-2 rounded-full ${r.color}`} />
                          <span className="font-bold">{r.title}</span>
                        </div>
                        <span className="text-[10px] text-slate-400 ml-4">{r.badge}</span>
                      </div>
                      {currentUser.role === r.role && (
                        <Check className="w-4 h-4 text-indigo-400" />
                      )}
                    </button>
                  ))}

                  <div className="border-t border-slate-800 mt-1 pt-1.5 px-3 space-y-1">
                    <button
                      onClick={() => {
                        handleCopyUniversalDemo();
                        setRoleMenuOpen(false);
                      }}
                      className="w-full text-left flex items-center gap-2 py-1 text-[11px] text-amber-400 hover:underline"
                    >
                      <Copy className="w-3 h-3" />
                      <span>Copy Universal Demo Link</span>
                    </button>
                    <button
                      onClick={() => {
                        openRegistrationModal();
                        setRoleMenuOpen(false);
                      }}
                      className="w-full text-left flex items-center gap-2 py-1 text-[11px] text-indigo-300 hover:underline"
                    >
                      <UserPlus className="w-3 h-3" />
                      <span>Register via School Code</span>
                    </button>
                  </div>
                </div>
              )}
            </div>

          </div>
        </div>

        {/* Expandable Mobile Search Dropdown Bar */}
        {isMobileSearchOpen && (
          <div className="md:hidden p-2.5 bg-slate-900 border-t border-slate-800 animate-in slide-in-from-top-2">
            <div className="relative flex items-center">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 pointer-events-none" />
              <input
                ref={mobileSearchInputRef}
                type="text"
                value={topSearchQuery}
                onChange={e => setTopSearchQuery(e.target.value)}
                placeholder="Search students, staff, vouchers, routes..."
                className="w-full pl-9 pr-8 py-2 rounded-xl bg-slate-800 border border-slate-700 text-white placeholder-slate-400 text-xs focus:outline-none focus:border-indigo-500"
              />
              <button
                onClick={() => {
                  setTopSearchQuery('');
                  setIsMobileSearchOpen(false);
                }}
                className="absolute right-2.5 p-1 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Mobile Live Search Results */}
            {topSearchQuery.trim().length > 0 && (
              <div className="mt-2 bg-slate-950 border border-slate-800 rounded-xl overflow-hidden max-h-60 overflow-y-auto">
                {totalMatches === 0 ? (
                  <div className="p-3 text-center text-xs text-slate-400">
                    No matching records found.
                  </div>
                ) : (
                  <div className="p-1 space-y-1 text-xs">
                    {matchedStudents.map(st => (
                      <button
                        key={st.id}
                        onClick={() => {
                          setActiveTab('students');
                          setIsMobileSearchOpen(false);
                        }}
                        className="w-full flex items-center justify-between p-2 rounded-xl hover:bg-slate-800 text-left transition"
                      >
                        <div>
                          <div className="font-bold text-white text-xs">{st.name}</div>
                          <div className="text-[10px] text-slate-400">
                            Roll #{st.rollNo} • {st.className}
                          </div>
                        </div>
                        <span className="text-[10px] px-2 py-0.5 bg-indigo-950 text-indigo-300 font-mono rounded">
                          Student
                        </span>
                      </button>
                    ))}
                    {matchedStaff.map(st => (
                      <button
                        key={st.id}
                        onClick={() => {
                          setActiveTab('staff');
                          setIsMobileSearchOpen(false);
                        }}
                        className="w-full flex items-center justify-between p-2 rounded-xl hover:bg-slate-800 text-left transition"
                      >
                        <div>
                          <div className="font-bold text-white text-xs">{st.name}</div>
                          <div className="text-[10px] text-slate-400">{st.designation}</div>
                        </div>
                        <span className="text-[10px] px-2 py-0.5 bg-emerald-950 text-emerald-300 font-mono rounded">
                          Staff
                        </span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </header>
    </>
  );
};
