import React, { useState, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { Search, X, Users, Briefcase, Layers, FileText, ArrowRight } from 'lucide-react';

interface GlobalSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GlobalSearchModal: React.FC<GlobalSearchModalProps> = ({ isOpen, onClose }) => {
  const { students, staff, classes, invoices, setActiveTab, openPrintModal } = useSchool();
  const [query, setQuery] = useState('');

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
      }
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const q = query.toLowerCase().trim();
  const matchedStudents = q
    ? students.filter(s => s.name.toLowerCase().includes(q) || s.admissionNo.includes(q) || s.fatherName.toLowerCase().includes(q)).slice(0, 5)
    : [];

  const matchedStaff = q
    ? staff.filter(s => s.name.toLowerCase().includes(q) || s.designation.toLowerCase().includes(q) || s.staffId.toLowerCase().includes(q)).slice(0, 4)
    : [];

  const matchedClasses = q
    ? classes.filter(c => c.name.toLowerCase().includes(q)).slice(0, 3)
    : [];

  const matchedInvoices = q
    ? invoices.filter(i => i.voucherNo.toLowerCase().includes(q) || i.studentName.toLowerCase().includes(q)).slice(0, 4)
    : [];

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center pt-16 px-4 bg-slate-950/70 backdrop-blur-xs">
      <div className="w-full max-w-xl bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95 duration-100">
        
        {/* Search Input Bar */}
        <div className="p-3 border-b border-slate-100 flex items-center gap-2">
          <Search className="w-5 h-5 text-slate-400 ml-2" />
          <input
            type="text"
            autoFocus
            placeholder="Search students, teachers, classes, fee vouchers..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="flex-1 p-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none"
          />
          <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg">
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Results Container */}
        <div className="max-h-[60vh] overflow-y-auto p-3 space-y-4 text-xs">
          {!q ? (
            <div className="py-8 text-center text-slate-400 space-y-2">
              <Search className="w-8 h-8 mx-auto text-slate-300" />
              <p>Type student name, admission number, faculty, or voucher #</p>
              <div className="flex flex-wrap justify-center gap-1.5 pt-2">
                {['Zain Raza', 'Mid-Term', 'Class 4', 'VCH-2026', 'Ms. Hina'].map(chip => (
                  <button
                    key={chip}
                    onClick={() => setQuery(chip)}
                    className="px-2 py-0.5 rounded bg-slate-100 text-slate-600 hover:bg-slate-200 font-medium"
                  >
                    {chip}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <>
              {/* Students */}
              {matchedStudents.length > 0 && (
                <div>
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1.5 px-2">
                    Students ({matchedStudents.length})
                  </div>
                  <div className="space-y-1">
                    {matchedStudents.map(s => (
                      <div
                        key={s.id}
                        onClick={() => {
                          setActiveTab('students');
                          onClose();
                        }}
                        className="p-2 rounded-xl hover:bg-slate-50 cursor-pointer flex items-center justify-between transition"
                      >
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-lg bg-indigo-50 text-indigo-700 flex items-center justify-center font-bold text-xs">
                            {s.name.charAt(0)}
                          </div>
                          <div>
                            <div className="font-bold text-slate-900">{s.name}</div>
                            <div className="text-[11px] text-slate-500">Adm #{s.admissionNo} • {s.className} • Father: {s.fatherName}</div>
                          </div>
                        </div>
                        <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Staff */}
              {matchedStaff.length > 0 && (
                <div>
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1.5 px-2">
                    Faculty & Staff ({matchedStaff.length})
                  </div>
                  <div className="space-y-1">
                    {matchedStaff.map(st => (
                      <div
                        key={st.id}
                        onClick={() => {
                          setActiveTab('staff');
                          onClose();
                        }}
                        className="p-2 rounded-xl hover:bg-slate-50 cursor-pointer flex items-center justify-between transition"
                      >
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-lg bg-emerald-50 text-emerald-700 flex items-center justify-center font-bold text-xs">
                            <Briefcase className="w-3.5 h-3.5" />
                          </div>
                          <div>
                            <div className="font-bold text-slate-900">{st.name}</div>
                            <div className="text-[11px] text-slate-500">{st.designation} • {st.department}</div>
                          </div>
                        </div>
                        <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Invoices */}
              {matchedInvoices.length > 0 && (
                <div>
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1.5 px-2">
                    Fee Invoices ({matchedInvoices.length})
                  </div>
                  <div className="space-y-1">
                    {matchedInvoices.map(inv => (
                      <div
                        key={inv.id}
                        onClick={() => {
                          openPrintModal('voucher', inv);
                          onClose();
                        }}
                        className="p-2 rounded-xl hover:bg-slate-50 cursor-pointer flex items-center justify-between transition"
                      >
                        <div>
                          <div className="font-mono font-bold text-indigo-700">{inv.voucherNo}</div>
                          <div className="text-[11px] text-slate-600">{inv.studentName} ({inv.className}) • {inv.month}</div>
                        </div>
                        <span className="font-bold text-slate-900">Rs {inv.total.toLocaleString()}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Classes */}
              {matchedClasses.length > 0 && (
                <div>
                  <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-1.5 px-2">
                    Classes
                  </div>
                  <div className="space-y-1">
                    {matchedClasses.map(c => (
                      <div
                        key={c.id}
                        onClick={() => {
                          setActiveTab('classes');
                          onClose();
                        }}
                        className="p-2 rounded-xl hover:bg-slate-50 cursor-pointer flex items-center justify-between transition"
                      >
                        <span className="font-bold text-slate-900">{c.name} - Section {c.section}</span>
                        <span className="text-[11px] text-slate-500">{c.studentsCount} Students</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {matchedStudents.length === 0 && matchedStaff.length === 0 && matchedInvoices.length === 0 && matchedClasses.length === 0 && (
                <div className="py-8 text-center text-slate-400">
                  No records found matching &ldquo;{query}&rdquo;
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-2.5 bg-slate-50 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-400">
          <span>Press ESC to close</span>
          <span>School Manager System Search</span>
        </div>
      </div>
    </div>
  );
};
