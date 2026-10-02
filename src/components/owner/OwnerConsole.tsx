// Component ke andar sab se upar
const { currentUser, schools, currentSchool } = useAppStore() // ya jo bhi store use ho raha hai

// Owner nahi hai to kuch mat dikhao
if (currentUser?.role !== 'owner') {
  return null;
}
import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { School, DemoCredentials, UserRole } from '../../types';
import {
  Building2, Shield, Users, Briefcase, Plus, Trash2,
  CheckCircle, AlertTriangle, Clock, Database, RefreshCw, X,
  Key, Lock, Link, Copy, Check, Eye, Edit2, Send, Sparkles, Download, Layers,
  MessageSquare, UserPlus, FileText, Ban, PlayCircle, ShieldAlert, ArrowRight,
  Power, EyeOff, Radio, ToggleLeft, ToggleRight
} from 'lucide-react';

export const OwnerConsole: React.FC = () => {
  // AUTO_ROLE_CHECK - only owner
  try { const _raw = localStorage.getItem('app-storage'); const _j = JSON.parse(_raw || '{}'); const _r = _j?.state?.currentUser?.role; if (_r => {=> { _r !== 'owner') return null; } catch(e) {} 
  const {
    schools, addSchool, updateSchool, deleteSchool, switchSchool, currentSchool,
    cancelSchoolMembership, reactivateSchoolMembership,
    demoCredentials, updateDemoCredential, getDemoShareUrl, toggleDemoVisitorMode,
    isDemoVisitor, schoolChangeRequests, reviewSchoolChangeRequest,
    registrationRequests, reviewRegistrationRequest,
    openDownloadPackageModal, activeTab, setActiveTab,
    isOwnerInspectionMode, ownerInspectedRole, setOwnerInspectedRole,
    toggleOwnerInspectionMode, turnOffOwnerInspection, turnOnOwnerInspection
  } = useSchool();

  // Provision School Modal
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [tagline, setTagline] = useState('');
  const [email, setEmail] = useState('socialman121@gmail.com');
  const [phone, setPhone] = useState('+92 300 ');
  const [address, setAddress] = useState('Sheikhupura, Punjab, Pakistan');
  const [currency, setCurrency] = useState('Rs');
  const [session, setSession] = useState('2026-2027');
  const [plan, setPlan] = useState<'trial' | 'standard' | 'enterprise'>('standard');

  // Cancel Membership Modal
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
  const [cancellingSchool, setCancellingSchool] = useState<School | null>(null);
  const [cancellationReason, setCancellationReason] = useState('Administrative compliance review or overdue membership dues.');

  // Demo Credentials Edit Modal (EXCLUSIVE TO OWNER)
  const [isDemoModalOpen, setIsDemoModalOpen] = useState(false);
  const [editingCredRole, setEditingCredRole] = useState<UserRole | null>(null);
  const [credLoginId, setCredLoginId] = useState('');
  const [credPasscode, setCredPasscode] = useState('');

  // Link copy feedback
  const [copiedLink, setCopiedLink] = useState(false);

  const totalStudents = schools.reduce((s, sch) => s + sch.studentCount, 0);
  const totalTeachers = schools.reduce((s, sch) => s + sch.teacherCount, 0);
  const activeSchools = schools.filter(s => s.status === 'active').length;
  const cancelledSchools = schools.filter(s => s.status === 'cancelled').length;

  const handleOpenAddSchool = () => {
    setCode(`SAQ-0${schools.length + 1}`);
    setName('');
    setTagline('Excellence in Education • Leadership & Character');
    setEmail('socialman121@gmail.com');
    setPhone('+92 300 1234567');
    setAddress('Sheikhupura, Punjab, Pakistan');
    setCurrency('Rs');
    setSession('2026-2027');
    setPlan('standard');
    setIsModalOpen(true);
  };

  const handleCreateSchool = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !code) return;
    addSchool({
      code: code.trim().toUpperCase(),
      name: name.trim(),
      tagline: tagline.trim(),
      email: email.trim() || 'socialman121@gmail.com',
      phone: phone.trim() || "+92 300 0000000",
      address: address.trim(),
      currency,
      currentSession: session,
      status: "active",
      plan
    });
    setIsModalOpen(false);
  };

  const handleOpenCancelMembership = (s: School) => {
    setCancellingSchool(s);
    setCancellationReason('Administrative compliance review or overdue membership dues.');
    setIsCancelModalOpen(true);
  };

  const handleConfirmCancelMembership = () => {
    if (cancellingSchool) {
      cancelSchoolMembership(cancellingSchool.id, cancellationReason);
      setIsCancelModalOpen(false);
      setCancellingSchool(null);
    }
  };

  const handleOpenEditCred = (cred: DemoCredentials) => {
    setEditingCredRole(cred.role);
    setCredLoginId(cred.loginId);
    setCredPasscode(cred.passcode);
    setIsDemoModalOpen(true);
  };

  const handleSaveDemoCred = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingCredRole && credLoginId && credPasscode) {
      updateDemoCredential(editingCredRole, credLoginId.trim(), credPasscode.trim());
      setIsDemoModalOpen(false);
      setEditingCredRole(null);
    }
  };

  const handleCopyUniversalDemo = () => {
    const url = getDemoShareUrl();
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(url);
    }
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 3000);
  };

  // Determine current active view based on sidebar selection
  const currentView = (() => {
    if (activeTab === 'owner-schools') return 'schools';
    if (activeTab === 'owner-demo-creds') return 'demo-creds';
    if (activeTab === 'owner-requests') return 'requests';
    if (activeTab === 'owner-registrations') return 'registrations';
    if (activeTab === 'owner-backups') return 'backups';
    if (activeTab === 'owner-audit') return 'audit';
    return 'overview';
  })();

  const handleGoToSchoolAsAdmin = (schoolId: string) => {
    turnOnOwnerInspection(schoolId, 'dashboard', 'admin');
  };

  return (
    <div className="space-y-6 pb-12">
      
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-purple-900 via-indigo-950 to-slate-900 p-6 rounded-2xl text-white shadow-xl border border-purple-800">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-bold uppercase tracking-wider bg-amber-400 text-slate-950 px-2 py-0.5 rounded font-mono">
              SYSTEM OWNER CONSOLE
            </span>
            <span className="text-xs text-purple-200 font-mono">socialman121@gmail.com</span>
          </div>
          <h1 className="text-xl sm:text-2xl font-black mt-1">Sajjad Qasmi Multi-School SaaS Command Center</h1>
          <p className="text-xs text-purple-200 mt-1 max-w-2xl leading-relaxed">
            Allot individual apps to schools, watch and inspect any school as its admin, revoke memberships, configure character passwords, and deliver verified mobile APK packages.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* SINGLE DEMO LINK FOR ALL ROLES */}
          <button
            onClick={handleCopyUniversalDemo}
            className="flex items-center gap-1.5 px-3.5 py-2.5 bg-amber-400 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs shadow-md transition"
            title="Copy Single Universal Demo Link for all roles and visitors"
          >
            {copiedLink ? <Check className="w-4 h-4 text-emerald-950" /> : <Copy className="w-4 h-4 text-slate-950" />}
            <span>{copiedLink ? "Demo Link Copied!" : "Copy Visitor Demo Link"}</span>
          </button>
          
          <button
            onClick={handleOpenAddSchool}
            className="flex items-center gap-1.5 px-4 py-2.5 bg-white/10 hover:bg-white/20 text-white font-bold rounded-xl text-xs backdrop-blur-xs border border-white/20 transition"
          >
            <Plus className="w-4 h-4" />
            <span>Allot App to New School</span>
          </button>
        </div>
      </div>

      {/* CAMPUS LIVE INSPECTION CONTROL (ON / OFF + SWITCHING) */}
      <div className={`p-4 sm:p-5 rounded-2xl border shadow-md transition ${
        isOwnerInspectionMode
          ? 'bg-gradient-to-r from-purple-900 via-indigo-950 to-slate-900 border-purple-500/70 text-white'
          : 'bg-white border-slate-200 text-slate-800'
      }`}>
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4">
          <div className="flex items-start sm:items-center gap-3">
            <div className={`w-11 h-11 rounded-2xl flex items-center justify-center shrink-0 ${
              isOwnerInspectionMode
                ? 'bg-purple-500/30 text-amber-300 border border-purple-400/40'
                : 'bg-slate-100 text-slate-500 border border-slate-200'
            }`}>
              {isOwnerInspectionMode ? <Eye className="w-6 h-6 animate-pulse text-amber-400" /> : <EyeOff className="w-6 h-6 text-slate-400" />}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <span className={`text-[10px] font-mono font-bold uppercase px-2 py-0.5 rounded ${
                  isOwnerInspectionMode
                    ? 'bg-amber-400 text-slate-950'
                    : 'bg-slate-200 text-slate-700'
                }`}>
                  {isOwnerInspectionMode ? 'INSPECTION MODE: ACTIVE (ON)' : 'INSPECTION MODE: OFF'}
                </span>
                {isOwnerInspectionMode && (
                  <span className="text-xs text-purple-200 font-mono">
                    Campus: <strong className="text-amber-300">{currentSchool.name}</strong> ({currentSchool.code})
                  </span>
                )}
              </div>
              <p className={`text-xs mt-1 leading-relaxed ${
                isOwnerInspectionMode ? 'text-purple-200' : 'text-slate-500'
              }`}>
                {isOwnerInspectionMode
                  ? `You can watch and administer ${currentSchool.name} in real-time as ${ownerInspectedRole.toUpperCase()}. Switch campus, change perspective, or turn OFF below.`
                  : "Inspection Mode is currently turned OFF. All campus overlays are disabled and you are operating purely in SaaS Master Owner Mode."}
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            {/* Campus Switcher Dropdown */}
            <select
              value={currentSchool.id}
              onChange={e => {
                switchSchool(e.target.value);
                if (isOwnerInspectionMode) {
                  turnOnOwnerInspection(e.target.value, 'dashboard', ownerInspectedRole);
                }
              }}
              className={`px-3 py-2 rounded-xl text-xs font-bold border focus:outline-none ${
                isOwnerInspectionMode
                  ? 'bg-purple-950/80 border-purple-500 text-amber-300'
                  : 'bg-slate-50 border-slate-300 text-slate-800'
              }`}
              title="Select Campus"
            >
              {schools.map(s => (
                <option key={s.id} value={s.id}>
                  Campus: {s.name} ({s.code})
                </option>
              ))}
            </select>

            {/* Role Perspective Selector */}
            <select
              value={ownerInspectedRole}
              onChange={e => setOwnerInspectedRole(e.target.value as any)}
              className={`px-3 py-2 rounded-xl text-xs font-bold border focus:outline-none ${
                isOwnerInspectionMode
                  ? 'bg-indigo-950/80 border-indigo-500 text-indigo-200'
                  : 'bg-slate-50 border-slate-300 text-slate-700'
              }`}
              title="Select Inspected Perspective"
            >
              <option value="admin">As School Admin</option>
              <option value="teacher">As Teacher Portal</option>
              <option value="student">As Student Portal</option>
              <option value="parent">As Parent Portal</option>
            </select>

            {/* Toggle Switch Button: Turn ON / OFF */}
            {isOwnerInspectionMode ? (
              <div className="flex items-center gap-2">
                <button
                  onClick={turnOffOwnerInspection}
                  className="px-3.5 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition"
                  title="Turn Inspection Mode OFF"
                >
                  <Power className="w-3.5 h-3.5" />
                  <span>Turn Inspection OFF</span>
                </button>
                <button
                  onClick={() => setActiveTab('dashboard')}
                  className="px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition"
                >
                  <Eye className="w-3.5 h-3.5 text-amber-300" />
                  <span>Open Inspected View</span>
                </button>
              </div>
            ) : (
              <button
                onClick={() => turnOnOwnerInspection(currentSchool.id, 'dashboard', ownerInspectedRole)}
                className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition"
                title="Turn Inspection Mode ON"
              >
                <Power className="w-3.5 h-3.5 text-amber-300" />
                <span>Turn Inspection Mode ON</span>
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Internal Navigation Tabs for Quick Switching */}
      <div className="flex items-center gap-1.5 overflow-x-auto p-1 bg-white rounded-2xl border border-slate-200 shadow-xs text-xs font-bold text-slate-600">
        {[
          { id: 'owner-overview', label: 'Platform Overview', icon: Building2 },
          { id: 'owner-schools', label: `Schools & Allotments (${schools.length})`, icon: Building2 },
          { id: 'owner-demo-creds', label: 'Demo Character Passwords', icon: Key },
          { id: 'owner-requests', label: `Campus Requests (${schoolChangeRequests.filter(r => r.status === 'pending').length})`, icon: MessageSquare },
          { id: 'owner-registrations', label: `Registrations (${registrationRequests.filter(r => r.status === 'pending').length})`, icon: UserPlus },
          { id: 'owner-backups', label: 'APK & Backups', icon: Database },
          { id: 'owner-audit', label: 'Platform Audit Log', icon: Clock }
        ].map(tab => {
          const isActive = (tab.id === 'owner-overview' && currentView === 'overview') || activeTab === tab.id;
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl whitespace-nowrap transition ${
                isActive ? 'bg-indigo-600 text-white shadow-xs' : 'hover:bg-slate-100 text-slate-700'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* VIEW 1: PLATFORM OVERVIEW */}
      {currentView === 'overview' && (
        <div className="space-y-6">
          {/* Platform KPIs */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] uppercase font-bold text-slate-400">Allotted Campuses</div>
              <div className="text-2xl font-black text-indigo-900 mt-1">{schools.length}</div>
              <div className="text-[10px] text-emerald-600 font-bold mt-0.5">{activeSchools} Active • {cancelledSchools} Cancelled</div>
            </div>

            <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] uppercase font-bold text-slate-400">Total Enrolled Students</div>
              <div className="text-2xl font-black text-emerald-600 mt-1">{totalStudents}</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Across all campuses</div>
            </div>

            <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] uppercase font-bold text-slate-400">Faculty & Staff</div>
              <div className="text-2xl font-black text-blue-600 mt-1">{totalTeachers}</div>
              <div className="text-[10px] text-slate-500 mt-0.5">Active teacher roster</div>
            </div>

            <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-xs">
              <div className="text-[11px] uppercase font-bold text-slate-400">Pending Requests</div>
              <div className="text-2xl font-black text-purple-700 mt-1">
                {schoolChangeRequests.filter(r => r.status === 'pending').length}
              </div>
              <div className="text-[10px] text-amber-600 font-bold mt-0.5">Campus Updates Awaiting Review</div>
            </div>
          </div>

          {/* SINGLE DEMO LINK FOR ALL ROLES (Visitor Demo Source) */}
          <div className="bg-gradient-to-r from-indigo-50 via-purple-50 to-amber-50 border-2 border-indigo-200 rounded-2xl p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-xs">
            <div className="space-y-1.5">
              <div className="flex items-center gap-2 text-xs font-bold text-indigo-950">
                <Sparkles className="w-4 h-4 text-amber-600 shrink-0" />
                <span>One Unified Visitor Demo Link (All Roles in One)</span>
              </div>
              <p className="text-xs text-slate-600 leading-relaxed">
                Send this single universal link to visitors or prospective school owners. They can switch freely between all 5 role perspectives (Owner, Admin, Teacher, Student, Parent) with 1 click without separate passwords.
              </p>
              <div className="pt-1 font-mono text-[11px] text-indigo-700 bg-white/90 p-2.5 rounded-xl border border-indigo-100 select-all truncate max-w-xl">
                {getDemoShareUrl()}
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 shrink-0">
              <button
                onClick={toggleDemoVisitorMode}
                className="px-3.5 py-2 bg-amber-400 hover:bg-amber-500 text-slate-950 font-bold rounded-xl text-xs transition shadow-xs flex items-center gap-1.5"
              >
                <PlayCircle className="w-4 h-4" />
                <span>{isDemoVisitor ? "Exit Demo Mode" : "Launch Demo Now"}</span>
              </button>

              <button
                onClick={handleCopyUniversalDemo}
                className="px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs transition shadow-xs flex items-center gap-1.5"
              >
                {copiedLink ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                <span>{copiedLink ? "Copied!" : "Copy Unified Link"}</span>
              </button>
            </div>
          </div>

          {/* Quick Shortcuts to Sections with Instant Actions */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div
              onClick={() => setActiveTab('owner-schools')}
              className="bg-white p-5 rounded-2xl border border-slate-200 hover:border-indigo-400 hover:shadow-md cursor-pointer transition space-y-2"
            >
              <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
                <Building2 className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-sm text-slate-900">Go to School & Inspect as Admin</h3>
              <p className="text-xs text-slate-500">Owner can go to any school and watch everything as an admin of that school can.</p>
            </div>

            <div
              onClick={() => setActiveTab('owner-demo-creds')}
              className="bg-white p-5 rounded-2xl border border-slate-200 hover:border-amber-400 hover:shadow-md cursor-pointer transition space-y-2"
            >
              <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
                <Key className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-sm text-slate-900">Demo Character Passwords</h3>
              <p className="text-xs text-slate-500">Configure login credentials and passcodes for Owner, Admin, Teacher, Student, and Parent.</p>
            </div>

            <div
              onClick={() => setActiveTab('owner-requests')}
              className="bg-white p-5 rounded-2xl border border-slate-200 hover:border-purple-400 hover:shadow-md cursor-pointer transition space-y-2"
            >
              <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center font-bold">
                <MessageSquare className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-sm text-slate-900">Campus Change Requests</h3>
              <p className="text-xs text-slate-500">Review modification requests for school names, helplines, addresses, and taglines.</p>
            </div>
          </div>
        </div>
      )}

      {/* VIEW 2: ALL SCHOOLS & ALLOTMENTS (WITH GO TO SCHOOL AS ADMIN) */}
      {currentView === 'schools' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-5 animate-in fade-in">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Building2 className="w-5 h-5 text-indigo-600" />
                <span>All Allotted School Campuses ({schools.length})</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Owner can click <strong>&ldquo;Go to School (Watch as Admin)&rdquo;</strong> on any campus to view and manage everything as that school&apos;s administrator.
              </p>
            </div>
            <button
              onClick={handleOpenAddSchool}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs flex items-center gap-1.5 transition self-start sm:self-auto"
            >
              <Plus className="w-4 h-4" />
              <span>+ Allot App to New School</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {schools.map(sch => {
              const isSelected = sch.id === currentSchool.id;
              const isCancelled = sch.status === 'cancelled';
              const isSuspended = sch.status === 'suspended';

              return (
                <div
                  key={sch.id}
                  className={`p-5 rounded-2xl border transition relative flex flex-col justify-between space-y-4 ${
                    isCancelled
                      ? 'bg-red-50/40 border-red-200'
                      : isSelected
                      ? 'bg-indigo-50/40 border-indigo-300 ring-2 ring-indigo-500/20 shadow-xs'
                      : 'bg-white border-slate-200 hover:border-slate-300'
                  }`}
                >
                  <div className="space-y-2">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex items-center gap-2.5">
                        <div className={`w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm ${
                          isCancelled ? 'bg-red-100 text-red-800' : 'bg-indigo-600 text-white shadow-xs'
                        }`}>
                          {sch.code.substring(0, 3)}
                        </div>
                        <div>
                          <h3 className="font-bold text-sm text-slate-900 leading-tight">
                            {sch.name}
                          </h3>
                          <div className="text-[11px] text-slate-500 font-mono">
                            Campus Code: <strong className="text-indigo-950 font-bold">{sch.code}</strong> • Session: {sch.currentSession}
                          </div>
                        </div>
                      </div>

                      <span className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full uppercase font-mono tracking-wider ${
                        isCancelled ? 'bg-red-200 text-red-900 border border-red-300' :
                        isSuspended ? 'bg-amber-100 text-amber-800' : 'bg-emerald-100 text-emerald-800'
                      }`}>
                        {sch.status}
                      </span>
                    </div>

                    <p className="text-xs text-slate-600 italic">
                      &ldquo;{sch.tagline}&rdquo;
                    </p>

                    {isCancelled && sch.cancellationReason && (
                      <div className="p-2.5 bg-red-100/70 border border-red-200 rounded-xl text-[11px] text-red-900 flex items-start gap-2">
                        <Ban className="w-4 h-4 text-red-600 shrink-0 mt-0.5" />
                        <div>
                          <strong>Membership Cancelled:</strong> {sch.cancellationReason}
                        </div>
                      </div>
                    )}

                    <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100 text-xs">
                      <div>
                        <span className="text-[10px] uppercase font-bold text-slate-400 block">Contact Email</span>
                        <span className="text-slate-800 font-medium truncate block">{sch.email}</span>
                      </div>
                      <div>
                        <span className="text-[10px] uppercase font-bold text-slate-400 block">Phone / Helpline</span>
                        <span className="text-slate-800 font-medium">{sch.phone}</span>
                      </div>
                      <div>
                        <span className="text-[10px] uppercase font-bold text-slate-400 block">Enrolled Students</span>
                        <span className="text-slate-900 font-bold">{sch.studentCount} Students</span>
                      </div>
                      <div>
                        <span className="text-[10px] uppercase font-bold text-slate-400 block">Faculty Members</span>
                        <span className="text-slate-900 font-bold">{sch.teacherCount} Teachers</span>
                      </div>
                    </div>
                  </div>

                  {/* Actions - OWNER CAN GO TO ANY SCHOOL AND WATCH EVERYTHING */}
                  <div className="flex flex-wrap items-center justify-between gap-2 pt-3 border-t border-slate-200 text-xs">
                    {isOwnerInspectionMode && currentSchool.id === sch.id ? (
                      <div className="flex items-center gap-1.5">
                        <button
                          onClick={() => setActiveTab('dashboard')}
                          className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold transition text-xs flex items-center gap-1 shadow-xs"
                        >
                          <Eye className="w-3.5 h-3.5 text-amber-300" />
                          <span>Inspecting Now</span>
                        </button>
                        <button
                          onClick={turnOffOwnerInspection}
                          className="px-2.5 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl font-bold transition text-xs flex items-center gap-1 shadow-xs"
                          title="Turn Inspection Mode OFF"
                        >
                          <Power className="w-3 h-3" />
                          <span>Turn OFF</span>
                        </button>
                      </div>
                    ) : isOwnerInspectionMode ? (
                      <button
                        onClick={() => handleGoToSchoolAsAdmin(sch.id)}
                        className="px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition text-xs flex items-center gap-1.5 shadow-xs"
                        title="Switch inspection to this school"
                      >
                        <Eye className="w-3.5 h-3.5 text-amber-300" />
                        <span>Switch Inspection to Campus</span>
                      </button>
                    ) : (
                      <button
                        onClick={() => handleGoToSchoolAsAdmin(sch.id)}
                        className="px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold transition text-xs flex items-center gap-1.5 shadow-xs"
                        title="Turn inspection ON and enter this school as admin"
                      >
                        <Eye className="w-3.5 h-3.5 text-amber-300" />
                        <span>Inspect School (Turn ON)</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    )}

                    <div className="flex items-center gap-1.5">
                      {isCancelled ? (
                        <button
                          onClick={() => reactivateSchoolMembership(sch.id)}
                          className="px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold transition text-xs flex items-center gap-1"
                        >
                          <CheckCircle className="w-3.5 h-3.5" />
                          <span>Reactivate</span>
                        </button>
                      ) : (
                        <button
                          onClick={() => handleOpenCancelMembership(sch)}
                          className="px-3 py-1.5 rounded-xl bg-red-100 hover:bg-red-200 text-red-800 font-bold transition text-xs flex items-center gap-1"
                          title="Revoke / Cancel membership of this school"
                        >
                          <Ban className="w-3.5 h-3.5" />
                          <span>Cancel Membership</span>
                        </button>
                      )}

                      <button
                        onClick={() => {
                          if (confirm(`Permanently remove ${sch.name}?`)) deleteSchool(sch.id);
                        }}
                        className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition"
                        title="Delete school from platform"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* VIEW 3: DEMO CHARACTER PASSWORDS (EXCLUSIVE TO OWNER) */}
      {currentView === 'demo-creds' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-5 animate-in fade-in">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <Key className="w-5 h-5 text-amber-500" />
                <span>Demo Character Credentials (Owner Controlled Only)</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Each character&apos;s login and password for the demo can be set by the owner only. Visitors can also use the single universal demo link to experience all roles.
              </p>
            </div>
            <div className="p-2 rounded-xl bg-amber-50 text-amber-800 border border-amber-200 text-[11px] font-bold">
              Owner Only Access Protected
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {demoCredentials.map(dc => (
              <div key={dc.role} className="bg-slate-50 p-4 rounded-2xl border border-slate-200 flex flex-col justify-between space-y-3">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-900 capitalize flex items-center gap-1.5">
                      <Shield className="w-4 h-4 text-indigo-600" />
                      <span>{dc.label}</span>
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-100 text-indigo-800 uppercase font-mono">
                      {dc.role}
                    </span>
                  </div>

                  <div className="mt-3 space-y-1.5 text-xs bg-white p-3 rounded-xl border border-slate-200">
                    <div>
                      <span className="text-[10px] uppercase font-bold text-slate-400 block">Login ID</span>
                      <code className="text-indigo-950 font-bold font-mono">{dc.loginId}</code>
                    </div>
                    <div>
                      <span className="text-[10px] uppercase font-bold text-slate-400 block">Password</span>
                      <code className="text-emerald-700 font-bold font-mono">{dc.passcode}</code>
                    </div>
                  </div>

                  <p className="text-[11px] text-slate-500 mt-2 leading-relaxed">
                    {dc.notes}
                  </p>
                </div>

                <div className="pt-2 border-t border-slate-200 flex items-center justify-between gap-2">
                  <button
                    onClick={() => handleOpenEditCred(dc)}
                    className="w-full py-2 bg-amber-400 hover:bg-amber-500 text-slate-950 rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 transition shadow-xs"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                    <span>Edit ID & Password</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* VIEW 4: CAMPUS CHANGE REQUESTS */}
      {currentView === 'requests' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4 animate-in fade-in">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-purple-600" />
              <span>Campus Modification Requests ({schoolChangeRequests.length})</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              School admins submit change requests when they want to modify their school name, tagline, address, or phone. Owner reviews and approves them.
            </p>
          </div>

          {schoolChangeRequests.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400">
              No change requests pending review.
            </div>
          ) : (
            <div className="space-y-3">
              {schoolChangeRequests.map(req => (
                <div key={req.id} className="p-4 bg-slate-50 rounded-2xl border border-slate-200 text-xs space-y-3">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-200 pb-2">
                    <div>
                      <span className="font-bold text-slate-900 text-sm">{req.schoolName}</span>
                      <span className="text-slate-500 ml-2 font-mono">({req.adminEmail})</span>
                    </div>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase font-mono ${
                      req.status === 'approved' ? 'bg-emerald-100 text-emerald-800' :
                      req.status === 'rejected' ? 'bg-red-100 text-red-800' : 'bg-amber-100 text-amber-800'
                    }`}>
                      {req.status}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-[11px]">
                    {req.requestedChanges.name && (
                      <div><strong className="text-slate-700">New Name:</strong> {req.requestedChanges.name}</div>
                    )}
                    {req.requestedChanges.tagline && (
                      <div><strong className="text-slate-700">New Tagline:</strong> {req.requestedChanges.tagline}</div>
                    )}
                    {req.requestedChanges.phone && (
                      <div><strong className="text-slate-700">New Phone:</strong> {req.requestedChanges.phone}</div>
                    )}
                    {req.requestedChanges.address && (
                      <div><strong className="text-slate-700">New Address:</strong> {req.requestedChanges.address}</div>
                    )}
                  </div>

                  <div className="bg-white p-2.5 rounded-xl border border-slate-200 text-slate-600 italic">
                    Reason from Admin: &ldquo;{req.requestedChanges.reason}&rdquo;
                  </div>

                  {req.status === 'pending' && (
                    <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-200">
                      <button
                        onClick={() => reviewSchoolChangeRequest(req.id, 'rejected', 'Does not meet board standards.')}
                        className="px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-700 font-bold rounded-lg text-xs"
                      >
                        Reject Request
                      </button>
                      <button
                        onClick={() => reviewSchoolChangeRequest(req.id, 'approved', 'Approved by System Owner.')}
                        className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-lg text-xs shadow-xs"
                      >
                        Approve & Apply Changes
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* VIEW 5: PENDING REGISTRATIONS */}
      {currentView === 'registrations' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4 animate-in fade-in">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <UserPlus className="w-5 h-5 text-indigo-600" />
              <span>Pending Account Registrations via School Code ({registrationRequests.length})</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Review users requesting accounts with their campus school codes (Parents, Students, Teachers).
            </p>
          </div>

          {registrationRequests.length === 0 ? (
            <div className="p-8 text-center text-xs text-slate-400">
              No pending registrations at this time.
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
              {registrationRequests.map(reg => (
                <div key={reg.id} className="py-3 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900">{reg.name}</span>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-100 text-indigo-800 uppercase font-mono">
                        {reg.requestedRole}
                      </span>
                      <span className="text-[10px] font-mono text-slate-400">Campus Code: {reg.schoolCode}</span>
                    </div>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      Email: {reg.email} • Mobile: {reg.phone} {reg.childAdmissionNo ? `• Child Adm #${reg.childAdmissionNo}` : ''}
                    </div>
                  </div>

                  {reg.status === 'pending' ? (
                    <div className="flex items-center gap-2 shrink-0">
                      <button
                        onClick={() => reviewRegistrationRequest(reg.id, 'rejected')}
                        className="px-3 py-1 bg-red-100 hover:bg-red-200 text-red-700 font-bold rounded-lg text-xs"
                      >
                        Reject
                      </button>
                      <button
                        onClick={() => reviewRegistrationRequest(reg.id, 'approved')}
                        className="px-3.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-lg text-xs shadow-xs"
                      >
                        Approve Account
                      </button>
                    </div>
                  ) : (
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-600 uppercase font-mono">
                      {reg.status}
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* VIEW 6: REAL APK & SYSTEM BACKUPS */}
      {currentView === 'backups' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4 animate-in fade-in">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Database className="w-5 h-5 text-indigo-600" />
              <span>System Backups & Real Android APK Package Delivery</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Full 5.8 MB compiled Android installer (.apk), Google Play Store package (.aab), and complete JSON backups.
            </p>
          </div>

          <div className="p-6 bg-slate-50 rounded-2xl border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="space-y-1">
              <h3 className="font-bold text-sm text-slate-900">Direct Download Center</h3>
              <p className="text-xs text-slate-500">
                Click below to launch the package generator dialog for <strong>{currentSchool.name}</strong>.
              </p>
            </div>
            <button
              onClick={openDownloadPackageModal}
              className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl text-xs transition shadow-xs flex items-center gap-2"
            >
              <Download className="w-4 h-4" />
              <span>Download Real APK (5.8MB) & Backups</span>
            </button>
          </div>
        </div>
      )}

      {/* VIEW 7: PLATFORM AUDIT LOG */}
      {currentView === 'audit' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 sm:p-6 space-y-4 animate-in fade-in">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-indigo-600" />
              <span>Platform Activity & Security Audit Trail</span>
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Live audit events recorded by the SaaS kernel for institutional compliance and owner verification.
            </p>
          </div>

          <div className="space-y-2 text-xs">
            {[
              { time: "Just now", action: "Owner Console initialized by Sajjad Qasmi (socialman121@gmail.com)", level: "info" },
              { time: "10 mins ago", action: "Campus Session 2026-2027 certified for SAQ-01 Main Campus", level: "success" },
              { time: "1 hour ago", action: "Inter-messaging broadcast dispatched to all institutional administrators", level: "info" },
              { time: "Yesterday", action: "APK & AAB packaging engine compiled with Android SDK 34 targets (5.8 MB)", level: "success" },
              { time: "3 days ago", action: "Multi-school tenant isolation rules validated for Sheikhupura regional branches", level: "info" }
            ].map((log, idx) => (
              <div key={idx} className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between gap-3">
                <div className="flex items-center gap-2.5">
                  <span className="w-2 h-2 rounded-full bg-indigo-600" />
                  <span className="text-slate-800 font-medium">{log.action}</span>
                </div>
                <span className="text-[10px] text-slate-400 font-mono shrink-0">{log.time}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* MODAL 1: PROVISION NEW SCHOOL */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-xs overflow-y-auto">
          <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl p-6 border border-slate-200 space-y-4 my-auto animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Building2 className="w-5 h-5 text-indigo-600" />
                <h3 className="font-bold text-slate-900 text-sm">Allot App to New School</h3>
              </div>
              <button onClick={() => setIsModalOpen(false)} className="p-1 text-slate-400 hover:text-slate-700">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateSchool} className="space-y-3 text-xs">
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Campus Code *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. SAQ-03"
                    value={code}
                    onChange={e => setCode(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono font-bold uppercase focus:outline-indigo-500"
                  />
                </div>
                <div className="col-span-2">
                  <label className="font-bold text-slate-700 block mb-1">Full School Name *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Sajjad Qasmi High School - Model Branch"
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold focus:outline-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Motto / Tagline</label>
                <input
                  type="text"
                  placeholder="e.g. Inspiring Young Leaders of Tomorrow"
                  value={tagline}
                  onChange={e => setTagline(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Admin Email (Owner&apos;s)</label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Helpline Phone #</label>
                  <input
                    type="text"
                    value={phone}
                    onChange={e => setPhone(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono focus:outline-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Campus Address</label>
                <input
                  type="text"
                  value={address}
                  onChange={e => setAddress(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Currency</label>
                  <input
                    type="text"
                    value={currency}
                    onChange={e => setCurrency(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold focus:outline-indigo-500"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Academic Session</label>
                  <input
                    type="text"
                    value={session}
                    onChange={e => setSession(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono focus:outline-indigo-500"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">SaaS Plan</label>
                  <select
                    value={plan}
                    onChange={e => setPlan(e.target.value as any)}
                    className="w-full p-2 border border-slate-300 rounded-lg capitalize focus:outline-indigo-500"
                  >
                    <option value="trial">Trial</option>
                    <option value="standard">Standard</option>
                    <option value="enterprise">Enterprise</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-3.5 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-lg shadow-xs transition"
                >
                  Allot & Launch School
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: CANCEL MEMBERSHIP CONFIRMATION */}
      {isCancelModalOpen && cancellingSchool && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-xs">
          <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6 border border-slate-200 space-y-4 animate-in fade-in zoom-in-95">
            <div className="flex items-center gap-2.5 text-red-600 border-b border-slate-100 pb-3">
              <ShieldAlert className="w-6 h-6 shrink-0" />
              <h3 className="font-black text-slate-900 text-base">Cancel School Membership</h3>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed">
              Are you sure you want to cancel the membership for <strong>{cancellingSchool.name}</strong> ({cancellingSchool.code})?
              This will suspend administrative operations and display a cancellation notice.
            </p>
            <div className="text-xs space-y-1">
              <label className="font-bold text-slate-700 block">Reason for Cancellation</label>
              <textarea
                rows={3}
                value={cancellationReason}
                onChange={e => setCancellationReason(e.target.value)}
                className="w-full p-2.5 border border-slate-300 rounded-xl text-xs focus:outline-red-500 font-medium"
              />
            </div>
            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100 text-xs">
              <button
                onClick={() => setIsCancelModalOpen(false)}
                className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold"
              >
                Go Back
              </button>
              <button
                onClick={handleConfirmCancelMembership}
                className="px-5 py-2 rounded-xl bg-red-600 hover:bg-red-700 text-white font-bold shadow-md transition flex items-center gap-1.5"
              >
                <Ban className="w-4 h-4" />
                <span>Confirm Cancellation</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 3: EDIT DEMO CREDENTIAL (OWNER ONLY) */}
      {isDemoModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-xs">
          <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl p-6 border border-slate-200 space-y-4 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Key className="w-5 h-5 text-amber-500" />
                <h3 className="font-bold text-slate-900 text-sm">
                  Edit Demo Credentials: <span className="capitalize">{editingCredRole}</span>
                </h3>
              </div>
              <button onClick={() => setIsDemoModalOpen(false)} className="p-1 text-slate-400 hover:text-slate-700">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveDemoCred} className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-slate-700 block mb-1">Demo Login ID / Username *</label>
                <input
                  type="text"
                  required
                  value={credLoginId}
                  onChange={e => setCredLoginId(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-bold text-slate-700 block mb-1">Demo Passcode / Password *</label>
                <input
                  type="text"
                  required
                  value={credPasscode}
                  onChange={e => setCredPasscode(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl font-mono focus:outline-indigo-500 font-bold text-emerald-700"
                />
              </div>
              <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-[11px] text-amber-900 flex items-start gap-2">
                <Shield className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                <span>
                  This login ID and passcode will be published to the visitor demo screen so external visitors can log in as this role.
                </span>
              </div>
              <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsDemoModalOpen(false)}
                  className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-xs transition"
                >
                  Save Passcode
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
