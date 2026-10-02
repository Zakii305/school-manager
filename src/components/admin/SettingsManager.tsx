import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  Building2, Settings, Shield, Mail, Smartphone,
  Save, Check, Copy, RefreshCw, Key, Download
} from 'lucide-react';

export const SettingsManager: React.FC = () => {
  const {
    currentSchool, branding, updateBranding,
    rolePermissions, updateRolePermission,
    smtpConfig, updateSmtpConfig,
    getDemoShareUrl, openDownloadPackageModal,
    triggerRealtimeSync, lastSyncTime
  } = useSchool();

  const [activeTab, setActiveTab] = useState<'branding' | 'permissions' | 'smtp' | 'system'>('branding');
  const [copiedLink, setCopiedLink] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  // Branding Form
  const [brandingForm, setBrandingForm] = useState({
    name: branding.name,
    tagline: branding.tagline,
    phone: branding.phone,
    email: branding.email,
    address: branding.address,
    currency: branding.currency,
    session: branding.session,
    logoUrl: branding.logoUrl,
    stampUrl: branding.stampUrl
  });

  // SMTP Form
  const [smtpForm, setSmtpForm] = useState({
    host: smtpConfig.host,
    port: smtpConfig.port,
    user: smtpConfig.user,
    pass: smtpConfig.pass,
    senderEmail: smtpConfig.senderEmail,
    senderName: smtpConfig.senderName,
    encryption: smtpConfig.encryption
  });

  const handleSaveBranding = (e: React.FormEvent) => {
    e.preventDefault();
    updateBranding(brandingForm);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const handleSaveSmtp = (e: React.FormEvent) => {
    e.preventDefault();
    updateSmtpConfig(smtpForm);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const handleCopyDemoLink = () => {
    const url = getDemoShareUrl();
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(url);
    }
    setCopiedLink(true);
    setTimeout(() => setCopiedLink(false), 3000);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900">Campus Profile &amp; Settings</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono">
              Code: {currentSchool.code}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Configure school branding, reports letterhead, SMTP notifications, and visitor demo link.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {savedSuccess && (
            <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-50 text-emerald-700 font-bold text-xs border border-emerald-200 animate-in fade-in">
              <Check className="w-3.5 h-3.5" />
              <span>Settings Saved!</span>
            </div>
          )}

          <button
            onClick={openDownloadPackageModal}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white font-bold rounded-xl text-xs shadow-md transition"
          >
            <Smartphone className="w-4 h-4" />
            <span>Download APK / WebApp</span>
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-1.5 bg-white p-1.5 rounded-2xl border border-slate-200 shadow-xs text-xs font-bold">
        {[
          { id: 'branding', label: 'School Profile & Branding', icon: Building2 },
          { id: 'permissions', label: 'Role Permissions Matrix', icon: Shield },
          { id: 'smtp', label: 'Email & SMS Gateway', icon: Mail },
          { id: 'system', label: 'Universal Demo Link & Sync', icon: RefreshCw }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id as any)}
            className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl transition ${
              activeTab === tab.id
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            <tab.icon className="w-3.5 h-3.5" />
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* 1. BRANDING TAB */}
      {activeTab === 'branding' && (
        <form onSubmit={handleSaveBranding} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-5 text-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Campus Official Identity</h3>
              <p className="text-slate-500 text-[11px] mt-0.5">These details print on official fee challans, student ID cards, and result cards.</p>
            </div>
            <button
              type="submit"
              className="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-md transition"
            >
              <Save className="w-4 h-4" />
              <span>Save Profile</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">Institution Legal Name *</label>
              <input
                type="text"
                required
                value={brandingForm.name}
                onChange={e => setBrandingForm({ ...brandingForm, name: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-bold"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Motto / Tagline</label>
              <input
                type="text"
                value={brandingForm.tagline}
                onChange={e => setBrandingForm({ ...brandingForm, tagline: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Contact Phone</label>
              <input
                type="text"
                value={brandingForm.phone}
                onChange={e => setBrandingForm({ ...brandingForm, phone: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Official Email</label>
              <input
                type="email"
                value={brandingForm.email}
                onChange={e => setBrandingForm({ ...brandingForm, email: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Billing Currency Code</label>
              <input
                type="text"
                value={brandingForm.currency}
                onChange={e => setBrandingForm({ ...brandingForm, currency: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Current Academic Session</label>
              <input
                type="text"
                value={brandingForm.session}
                onChange={e => setBrandingForm({ ...brandingForm, session: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
              />
            </div>

            <div className="sm:col-span-2">
              <label className="block font-bold text-slate-700 mb-1">Physical Campus Address</label>
              <input
                type="text"
                value={brandingForm.address}
                onChange={e => setBrandingForm({ ...brandingForm, address: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>
        </form>
      )}

      {/* 2. ROLE PERMISSIONS TAB */}
      {activeTab === 'permissions' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-200">
            <h3 className="font-bold text-slate-900 text-sm">Role-Based Access Control (RBAC)</h3>
            <p className="text-xs text-slate-500 mt-0.5">Toggle fine-grained module privileges for each user role.</p>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3 px-4">Role / Title</th>
                  <th className="py-3 px-4 text-center">Students</th>
                  <th className="py-3 px-4 text-center">Fees &amp; Dues</th>
                  <th className="py-3 px-4 text-center">Exams &amp; Marks</th>
                  <th className="py-3 px-4 text-center">Timetable</th>
                  <th className="py-3 px-4 text-center">Payroll</th>
                  <th className="py-3 px-4 text-center">Settings</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {rolePermissions.map(rp => (
                  <tr key={rp.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-4 font-bold capitalize text-slate-900">
                      {rp.name}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManageStudents}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManageStudents: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManageFees}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManageFees: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManageExams}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManageExams: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManageTimetable}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManageTimetable: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManagePayroll}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManagePayroll: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                    <td className="py-3 px-4 text-center">
                      <input
                        type="checkbox"
                        checked={rp.permissions.canManageSettings}
                        onChange={e =>
                          updateRolePermission(rp.id, {
                            ...rp.permissions,
                            canManageSettings: e.target.checked
                          })
                        }
                        className="rounded text-indigo-600"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 3. SMTP CONFIG TAB */}
      {activeTab === 'smtp' && (
        <form onSubmit={handleSaveSmtp} className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4 text-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Outbound SMTP Email Gateway</h3>
              <p className="text-slate-500 text-[11px] mt-0.5">Used for automated fee reminders and notice delivery.</p>
            </div>
            <button
              type="submit"
              className="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-md transition"
            >
              <Save className="w-4 h-4" />
              <span>Save SMTP Settings</span>
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block font-bold text-slate-700 mb-1">SMTP Host</label>
              <input
                type="text"
                value={smtpForm.host}
                onChange={e => setSmtpForm({ ...smtpForm, host: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">SMTP Port</label>
              <input
                type="text"
                value={smtpForm.port}
                onChange={e => setSmtpForm({ ...smtpForm, port: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Username / User</label>
              <input
                type="text"
                value={smtpForm.user}
                onChange={e => setSmtpForm({ ...smtpForm, user: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>

            <div>
              <label className="block font-bold text-slate-700 mb-1">Password</label>
              <input
                type="password"
                value={smtpForm.pass}
                onChange={e => setSmtpForm({ ...smtpForm, pass: e.target.value })}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
              />
            </div>
          </div>
        </form>
      )}

      {/* 4. UNIVERSAL DEMO LINK & REAL-TIME SYNC TAB */}
      {activeTab === 'system' && (
        <div className="space-y-4">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4 text-xs">
            <h3 className="font-bold text-slate-900 text-sm">Unified Demo Link For All Roles &amp; Visitors</h3>
            <p className="text-slate-600 text-xs leading-relaxed">
              Share one universal link with external visitors, examiners, or stakeholders.
              When opened, visitors can immediately preview the school live across Owner, Admin, Teacher, Student, and Parent roles with all demo characters enabled.
            </p>

            <div className="flex flex-col sm:flex-row sm:items-center gap-3 p-4 bg-slate-50 rounded-2xl border border-slate-200">
              <input
                type="text"
                readOnly
                value={getDemoShareUrl()}
                className="flex-1 bg-white px-3 py-2.5 rounded-xl border border-slate-200 text-slate-700 font-mono text-xs select-all focus:outline-none"
              />

              <button
                onClick={handleCopyDemoLink}
                className="flex items-center justify-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-md transition shrink-0"
              >
                {copiedLink ? <Check className="w-4 h-4 text-emerald-300" /> : <Copy className="w-4 h-4" />}
                <span>{copiedLink ? "Link Copied!" : "Copy Visitor Demo Link"}</span>
              </button>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 text-xs">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Real-Time State Synchronization</h3>
              <p className="text-slate-500 text-xs mt-0.5">
                Last synchronised at: <span className="font-mono font-bold text-indigo-700">{lastSyncTime}</span>
              </p>
            </div>

            <button
              onClick={triggerRealtimeSync}
              className="flex items-center gap-1.5 px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl transition"
            >
              <RefreshCw className="w-4 h-4" />
              <span>Force Synchronize</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
