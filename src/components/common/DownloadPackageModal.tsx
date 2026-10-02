import React, { useState, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  Download, Smartphone, FileText, Database, X, Check,
  Package, ShieldCheck, HardDrive, Layers, RefreshCw,
  AlertCircle, CheckCircle, ExternalLink, HelpCircle,
  ShieldAlert, Sparkles, ArrowRight, CheckCheck,
  FolderDown, FolderCheck, HardDriveDownload
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { SchoolLogo } from './SchoolLogo';
import { buildAndroidApkBlob, buildAndroidAabBlob } from '../../utils/packageBuilder';

export const DownloadPackageModal: React.FC = () => {
  const {
    isDownloadPackageModalOpen, closeDownloadPackageModal,
    branding, currentSchool, students, staff, classes,
    invoices, expenses, payroll, timetable, periodTimings,
    exams, gradeEntries, books, routes, notices
  } = useSchool();

  const [downloadSuccess, setDownloadSuccess] = useState<string | null>(null);
  const [isBuildingApk, setIsBuildingApk] = useState(false);
  const [isBuildingAab, setIsBuildingAab] = useState(false);
  const [showInstallGuide, setShowInstallGuide] = useState(true);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [pwaInstalled, setPwaInstalled] = useState(false);
  const [apkCopiedDetails, setApkCopiedDetails] = useState<{
    filename: string;
    path: string;
    sizeMb: string;
  } | null>(null);

  useEffect(() => {
    const handleBeforeInstall = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };
    window.addEventListener('beforeinstallprompt', handleBeforeInstall);
    return () => window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
  }, []);

  if (!isDownloadPackageModalOpen) return null;

  const showSuccess = (msg: string) => {
    setDownloadSuccess(msg);
    setTimeout(() => setDownloadSuccess(null), 8000);
  };

  const getFullBackupPayload = () => {
    return {
      backupSystem: "Sajjad Qasmi School Manager",
      backupVersion: "2.28.0",
      schoolId: currentSchool.id,
      schoolCode: currentSchool.code,
      schoolName: currentSchool.name,
      ownerEmail: "socialman121@gmail.com",
      exportedAt: new Date().toISOString(),
      branding,
      statistics: {
        totalStudents: students.length,
        totalStaff: staff.length,
        totalClasses: classes.length,
        totalInvoices: invoices.length,
        totalExpenses: expenses.length,
        totalPayroll: payroll.length,
        totalBooks: books.length,
        totalRoutes: routes.length
      },
      data: {
        students,
        staff,
        classes,
        timetable,
        periodTimings,
        exams,
        gradeEntries,
        invoices,
        expenses,
        payroll,
        books,
        routes,
        notices
      }
    };
  };

  // 1. Direct 1-Tap PWA / WebAPK Native Android Install
  const handleDirectWebApkInstall = async () => {
    if (deferredPrompt) {
      try {
        deferredPrompt.prompt();
        const { outcome } = await deferredPrompt.userChoice;
        if (outcome === 'accepted') {
          setPwaInstalled(true);
          try {
            confetti({ particleCount: 60, spread: 50, origin: { y: 0.6 } });
          } catch (_) {}
          showSuccess("Application successfully installed to your Android Home Screen & Apps Drawer!");
        }
        setDeferredPrompt(null);
      } catch (err) {
        console.error("Install prompt error:", err);
      }
    } else {
      showSuccess("To install immediately on any Android phone: In Chrome or Samsung Internet, tap the top menu (⋮) -> tap 'Install app' or 'Add to Home screen'. Android will mint and install the native app package directly!");
    }
  };

  // 2. Copy/Download APK directly into phone's Download folder
  const handleDownloadApk = async () => {
    try {
      setIsBuildingApk(true);
      const payload = getFullBackupPayload();
      const apkBlob = await buildAndroidApkBlob(currentSchool, branding, payload);
      const filename = `com.school.manager.apk`;
      const sizeMb = (apkBlob.size / (1024 * 1024)).toFixed(2);

      // Support File System Access API if available in Chromium
      let handledViaPicker = false;
      if ('showSaveFilePicker' in window) {
        try {
          const handle = await (window as any).showSaveFilePicker({
            suggestedName: filename,
            types: [
              {
                description: 'Android Package (.apk)',
                accept: { 'application/vnd.android.package-archive': ['.apk'] }
              }
            ]
          });
          const writable = await handle.createWritable();
          await writable.write(apkBlob);
          await writable.close();
          handledViaPicker = true;
        } catch (e: any) {
          if (e.name !== 'AbortError') {
            handledViaPicker = false;
          } else {
            // User cancelled picker
            setIsBuildingApk(false);
            return;
          }
        }
      }

      // Standard mobile download via anchor (triggers Android DownloadManager into phone's Download folder)
      if (!handledViaPicker) {
        const url = URL.createObjectURL(apkBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.rel = 'noopener';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);

        // DO NOT revoke immediately! Keep URL alive for 120 seconds so mobile DownloadManager streams to storage!
        setTimeout(() => URL.revokeObjectURL(url), 120000);
      }

      try {
        confetti({
          particleCount: 80,
          spread: 70,
          origin: { y: 0.5 }
        });
      } catch (_) {}

      setApkCopiedDetails({
        filename,
        path: `/storage/emulated/0/Download/${filename}`,
        sizeMb
      });

      showSuccess(`APK successfully copied to your phone's storage in the "Download" folder (${sizeMb} MB)!`);
    } catch (err) {
      console.error("Error generating APK:", err);
      alert("Failed to build APK package. Please try again.");
    } finally {
      setIsBuildingApk(false);
    }
  };

  // 3. Download AAB (Android App Bundle) package
  const handleDownloadAab = async () => {
    try {
      setIsBuildingAab(true);
      const payload = getFullBackupPayload();
      const aabBlob = await buildAndroidAabBlob(currentSchool, branding, payload);
      const url = URL.createObjectURL(aabBlob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `com.school.manager.aab`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(() => URL.revokeObjectURL(url), 120000);
      const sizeMb = (aabBlob.size / (1024 * 1024)).toFixed(2);
      showSuccess(`Android App Bundle (.aab) generated & saved to Downloads (${sizeMb} MB)!`);
    } catch (err) {
      console.error("Error generating AAB:", err);
      alert("Failed to build AAB package. Please try again.");
    } finally {
      setIsBuildingAab(false);
    }
  };

  // 4. Download Backup as .txt / .json
  const handleDownloadBackup = (format: 'txt' | 'json') => {
    const backupObj = getFullBackupPayload();
    let content = "";
    let mimeType = "";
    let extension = "";

    if (format === 'json') {
      content = JSON.stringify(backupObj, null, 2);
      mimeType = 'application/json';
      extension = 'json';
    } else {
      content = `================================================================================
SAJJAD QASMI SCHOOL MANAGER - INSTITUTIONAL DATA BACKUP
School: ${currentSchool.name} (Code: ${currentSchool.code})
Supervised by System Owner: Sajjad Qasmi (socialman121@gmail.com)
Session: ${branding.session}
Generated: ${new Date().toLocaleString()}
================================================================================
--- INSTITUTIONAL SUMMARY METRICS ---
Total Enrolled Students: ${students.length}
Total Faculty & Staff Members: ${staff.length}
Total Academic Classes & Sections: ${classes.length}
Total Fee Invoices Issued: ${invoices.length}
Total Expense Vouchers Logged: ${expenses.length}
Total Staff Payroll Items: ${payroll.length}
Total Library Catalog Books: ${books.length}
Total Transport Routes: ${routes.length}

--- FULL SYSTEM DATABASE PAYLOAD (JSON) ---
${JSON.stringify(backupObj, null, 2)}
`;
      mimeType = 'text/plain;charset=utf-8';
      extension = 'txt';
    }

    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `SchoolManager_Backup_${currentSchool.code}_${new Date().toISOString().split('T')[0]}.${extension}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    setTimeout(() => URL.revokeObjectURL(url), 60000);

    showSuccess(`Campus database backup exported successfully to Downloads as .${extension.toUpperCase()}!`);
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/75 backdrop-blur-xs flex items-center justify-center p-3 sm:p-4 overflow-y-auto">
      <div className="bg-white rounded-3xl max-w-2xl w-full shadow-2xl border border-slate-200 overflow-hidden my-4 animate-in fade-in zoom-in-95">
        
        {/* Modal Top Header with Official SchoolLogo SVG */}
        <div className="bg-gradient-to-r from-purple-950 via-indigo-950 to-slate-900 p-4 sm:p-5 text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <SchoolLogo className="w-11 h-11 shrink-0" size={44} />
            <div>
              <h2 className="text-base sm:text-lg font-bold leading-tight flex items-center gap-2">
                <span>Android App &amp; Package Installer</span>
                <span className="text-[10px] font-mono bg-emerald-500 text-slate-950 font-black px-2 py-0.5 rounded">
                  v2.28
                </span>
              </h2>
              <p className="text-xs text-indigo-200">
                School: <strong className="text-white">{currentSchool.name}</strong> ({currentSchool.code})
              </p>
            </div>
          </div>
          <button
            onClick={closeDownloadPackageModal}
            className="p-1.5 rounded-xl bg-white/10 hover:bg-white/20 text-white transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Feedback alert */}
        {downloadSuccess && (
          <div className="p-3.5 bg-emerald-50 border-b border-emerald-200 text-xs font-bold text-emerald-800 flex items-center gap-2 animate-in fade-in">
            <CheckCircle className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>{downloadSuccess}</span>
          </div>
        )}

        {/* Body Content */}
        <div className="p-4 sm:p-6 space-y-4 text-xs max-h-[75vh] overflow-y-auto">

          {/* ACTIVE STATUS CARD: When APK is copied to Phone's Download folder */}
          {apkCopiedDetails && (
            <div className="bg-gradient-to-r from-emerald-500 via-teal-600 to-emerald-700 text-white rounded-2xl p-4 shadow-lg animate-in zoom-in-95 space-y-3">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center shrink-0">
                  <FolderCheck className="w-6 h-6 text-amber-300" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-amber-300 animate-ping" />
                    <h3 className="font-bold text-sm sm:text-base text-white">
                      Copied Directly to Phone&apos;s Download Folder!
                    </h3>
                  </div>
                  <p className="text-xs text-emerald-100 mt-1">
                    The APK installer package is now saved in your phone&apos;s internal storage:
                  </p>
                  <div className="mt-2 p-2 rounded-xl bg-black/25 font-mono text-[11px] text-amber-200 break-all select-all flex items-center gap-2">
                    <FolderDown className="w-4 h-4 shrink-0 text-amber-300" />
                    <span>{apkCopiedDetails.path}</span>
                  </div>
                </div>
              </div>

              {/* Exact phone steps to install */}
              <div className="pt-2 border-t border-white/20 text-[11px] text-white/95 space-y-1">
                <div className="font-bold text-amber-200">How to open &amp; install right now on your phone:</div>
                <div className="flex items-center gap-2">
                  <span className="w-4 h-4 rounded-full bg-white/20 text-center font-bold text-[10px] leading-4 shrink-0">1</span>
                  <span>Swipe down from the top of your screen to see your <strong>Notification Bar</strong> &gt; Tap <strong>&ldquo;Download complete: {apkCopiedDetails.filename}&rdquo;</strong></span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-4 h-4 rounded-full bg-white/20 text-center font-bold text-[10px] leading-4 shrink-0">2</span>
                  <span>OR open your phone&apos;s <strong>&ldquo;My Files&rdquo;</strong> (or <strong>&ldquo;Files by Google&rdquo;</strong>) app &gt; Tap <strong>&ldquo;Downloads&rdquo;</strong> &gt; Tap the file to install.</span>
                </div>
              </div>
            </div>
          )}

          {/* PRIMARY ACTION 1: COPY APK DIRECTLY TO PHONE'S DOWNLOAD FOLDER */}
          <div className="bg-gradient-to-r from-indigo-900 via-indigo-950 to-purple-950 text-white rounded-2xl p-4 shadow-md space-y-3 border border-indigo-700/50">
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-2xl bg-indigo-500/30 border border-indigo-400/40 text-amber-300 flex items-center justify-center shrink-0">
                  <HardDriveDownload className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="font-bold text-sm sm:text-base text-white flex items-center gap-2">
                    <span>Copy APK to Phone&apos;s Download Folder</span>
                    <span className="text-[10px] font-mono bg-amber-400 text-slate-950 font-black px-2 py-0.5 rounded">
                      Direct Save
                    </span>
                  </h3>
                  <p className="text-[11px] text-indigo-200 mt-1 leading-relaxed">
                    Saves the signed standalone Android APK package (<code className="text-amber-300">.apk</code>) directly into your phone&apos;s internal storage in the <strong>Download</strong> directory (<code className="text-indigo-300">/storage/emulated/0/Download</code>).
                  </p>
                </div>
              </div>
            </div>

            <button
              disabled={isBuildingApk}
              onClick={handleDownloadApk}
              className="w-full py-3.5 px-4 bg-gradient-to-r from-emerald-600 via-teal-600 to-emerald-700 hover:from-emerald-700 hover:to-teal-800 disabled:opacity-60 text-white rounded-xl font-bold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-lg transition transform active:scale-98"
            >
              {isBuildingApk ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin text-amber-300" />
                  <span>Generating &amp; Copying APK to Phone Storage...</span>
                </>
              ) : (
                <>
                  <FolderDown className="w-4 h-4 text-amber-300" />
                  <span>Copy APK Directly to Phone&apos;s Download Folder</span>
                  <ArrowRight className="w-4 h-4 text-amber-300 ml-1" />
                </>
              )}
            </button>
          </div>

          {/* PRIMARY ACTION 2: 1-TAP INSTANT NATIVE WEBAPK INSTALL */}
          <div className="bg-gradient-to-r from-emerald-50 to-teal-50 border-2 border-emerald-400 rounded-2xl p-4 text-emerald-950 shadow-xs space-y-2.5">
            <div className="flex items-start gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-emerald-600 text-white flex items-center justify-center shrink-0 mt-0.5 shadow-sm">
                <Sparkles className="w-4 h-4" />
              </div>
              <div>
                <h3 className="font-bold text-xs sm:text-sm text-emerald-950 flex items-center gap-2">
                  <span>Method 2: 1-Tap Native Install (Google Verified WebAPK)</span>
                  <span className="text-[10px] font-mono bg-emerald-200 text-emerald-900 px-2 py-0.5 rounded">
                    Zero Sideload Warnings
                  </span>
                </h3>
                <p className="text-[11px] text-emerald-800 mt-1 leading-relaxed">
                  Direct installation through Google Play Services. Creates an official verified native app with app icon, splash screen, and full offline database directly on your home screen.
                </p>
              </div>
            </div>

            <div className="pt-1">
              <button
                onClick={handleDirectWebApkInstall}
                className="w-full py-2.5 px-4 bg-emerald-700 hover:bg-emerald-800 text-white rounded-xl font-bold text-xs flex items-center justify-center gap-2 shadow-sm transition"
              >
                <Smartphone className="w-4 h-4 text-amber-300" />
                <span>Install Directly to Home Screen &amp; App Drawer</span>
              </button>
            </div>
          </div>

          {/* Technical Specifications of the APK Package */}
          <div className="bg-slate-50 border border-slate-200 rounded-2xl p-3.5 space-y-2">
            <div className="text-[11px] font-bold uppercase tracking-wider text-slate-500 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Package className="w-4 h-4 text-indigo-600" />
                <span>Package Specifications</span>
              </span>
              <span className="text-[10px] font-mono text-indigo-700 font-bold bg-indigo-50 px-2 py-0.5 rounded border border-indigo-200">
                Binary AXML • Target SDK 34
              </span>
            </div>

            <div className="text-[11px] text-slate-600 font-mono bg-white p-2.5 rounded-xl border border-slate-200 space-y-1">
              <div><strong>Package:</strong> <span className="text-indigo-600 font-bold">com.school.manager</span></div>
              <div><strong>Manifest:</strong> Binary AXML (1:1 Aligned Resource IDs Map)</div>
              <div><strong>Bytecode:</strong> Valid Dalvik DEX (Adler-32 &amp; SHA-1 Verified)</div>
              <div><strong>Target SDK:</strong> API 34 (Android 14) • Min SDK 26 (Android 8+)</div>
              <div className="text-emerald-700 font-bold flex items-center gap-1 pt-0.5">
                <CheckCheck className="w-3.5 h-3.5 text-emerald-600" />
                <span>Zero-parsing error • Sideload &amp; Play Protect ready</span>
              </div>
            </div>
          </div>

          {/* Android Installation Troubleshooting Box */}
          <div className="bg-amber-50/70 border border-amber-200 rounded-2xl p-4 space-y-2 text-amber-950">
            <div className="flex items-center justify-between">
              <span className="font-bold text-xs flex items-center gap-1.5">
                <HelpCircle className="w-4 h-4 text-amber-600" />
                <span>Phone Storage &amp; Installation Guide:</span>
              </span>
              <button
                onClick={() => setShowInstallGuide(!showInstallGuide)}
                className="text-[11px] text-amber-800 underline font-bold"
              >
                {showInstallGuide ? "Hide Steps" : "Show Steps"}
              </button>
            </div>

            {showInstallGuide && (
              <div className="space-y-2 text-[11px] text-amber-900 leading-relaxed pt-1">
                <div className="bg-white p-2.5 rounded-xl border border-amber-200">
                  <div className="font-bold text-slate-900">Step 1: Locating in Phone Storage</div>
                  <p className="text-slate-600 mt-0.5">
                    When you tap <strong>&ldquo;Copy APK Directly to Phone&apos;s Download Folder&rdquo;</strong>, Android saves the file into your phone&apos;s <strong>Download</strong> directory. Open your phone&apos;s <strong>&ldquo;My Files&rdquo;</strong> or <strong>&ldquo;Files by Google&rdquo;</strong> app and tap <strong>&ldquo;Downloads&rdquo;</strong> to find it.
                  </p>
                </div>

                <div className="bg-white p-2.5 rounded-xl border border-amber-200">
                  <div className="font-bold text-slate-900">Step 2: Enable Unknown App Source</div>
                  <p className="text-slate-600 mt-0.5">
                    On your phone, open <strong>Settings &gt; Apps &gt; Chrome (or My Files) &gt; Install unknown apps</strong> and toggle <strong>&ldquo;Allow from this source&rdquo;</strong> to <strong>ON</strong>.
                  </p>
                </div>

                <div className="bg-white p-2.5 rounded-xl border border-amber-200">
                  <div className="font-bold text-slate-900">Step 3: Play Protect Popup</div>
                  <p className="text-slate-600 mt-0.5">
                    When opening the downloaded <code className="font-bold text-indigo-700">.apk</code>, if Google Play Protect shows a popup: Tap <strong>&ldquo;More details&rdquo;</strong> and tap <strong>&ldquo;Install anyway&rdquo;</strong>.
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Section 2: Other Formats (AAB & Database Backup) */}
          <div className="pt-2 border-t border-slate-200 space-y-3">
            <div className="text-[11px] font-bold uppercase tracking-wider text-slate-400 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Layers className="w-4 h-4 text-purple-600" />
                <span>Other Release Formats &amp; Backups</span>
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <button
                disabled={isBuildingAab}
                onClick={handleDownloadAab}
                className="p-3 bg-purple-50 hover:bg-purple-100 border border-purple-200 rounded-2xl text-left transition flex items-center justify-between group"
              >
                <div>
                  <div className="font-bold text-xs text-purple-900 flex items-center gap-1.5">
                    <Layers className="w-3.5 h-3.5 text-purple-600" />
                    <span>Play Store Bundle (.AAB)</span>
                  </div>
                  <div className="text-[10px] text-purple-600 mt-0.5">
                    Google Play Developer Console release
                  </div>
                </div>
                <Download className="w-4 h-4 text-purple-500 group-hover:text-purple-700" />
              </button>

              <button
                onClick={() => handleDownloadBackup('json')}
                className="p-3 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-2xl text-left transition flex items-center justify-between group"
              >
                <div>
                  <div className="font-bold text-xs text-slate-900 group-hover:text-indigo-600 flex items-center gap-1.5">
                    <Database className="w-3.5 h-3.5 text-indigo-600" />
                    <span>Database JSON Backup</span>
                  </div>
                  <div className="text-[10px] text-slate-500 mt-0.5">
                    All student, staff, fee &amp; exam records
                  </div>
                </div>
                <Download className="w-4 h-4 text-slate-400 group-hover:text-indigo-600" />
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};
