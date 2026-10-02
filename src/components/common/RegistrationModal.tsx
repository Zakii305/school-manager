import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { UserRole } from '../../types';
import {
  UserPlus, X, CheckCircle, AlertCircle
} from 'lucide-react';

export const RegistrationModal: React.FC = () => {
  const {
    isRegistrationModalOpen, closeRegistrationModal,
    schools, submitRegistrationRequest
  } = useSchool();

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState<UserRole>('parent');
  const [schoolCode, setSchoolCode] = useState(schools[0]?.code || 'SAQ-01');
  const [childAdmissionNo, setChildAdmissionNo] = useState('');
  const [submitted, setSubmitted] = useState(false);

  if (!isRegistrationModalOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !phone || !schoolCode) {
      alert("Please fill in all required fields including School Code.");
      return;
    }

    const matchedSchool = schools.find(s => s.code.toLowerCase() === schoolCode.trim().toLowerCase());
    const schoolName = matchedSchool ? matchedSchool.name : `School Campus (${schoolCode.toUpperCase()})`;

    submitRegistrationRequest({
      name,
      email,
      phone,
      requestedRole: role,
      schoolCode: schoolCode.toUpperCase().trim(),
      schoolName,
      childAdmissionNo: childAdmissionNo || undefined
    });
    setSubmitted(true);
  };

  const handleDone = () => {
    setSubmitted(false);
    setName('');
    setEmail('');
    setPhone('');
    setChildAdmissionNo('');
    closeRegistrationModal();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-slate-950/80 backdrop-blur-xs overflow-y-auto">
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200 my-auto animate-in fade-in zoom-in-95">
        
        {/* Header */}
        <div className="p-4 sm:p-5 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-amber-400 text-slate-950 flex items-center justify-center font-bold">
              <UserPlus className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold leading-tight">Create School Account</h2>
              <p className="text-xs text-indigo-200">
                Join your campus by entering your official School Code
              </p>
            </div>
          </div>
          <button
            onClick={closeRegistrationModal}
            className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {submitted ? (
          <div className="p-8 text-center space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto">
              <CheckCircle className="w-10 h-10" />
            </div>
            <h3 className="text-lg font-bold text-slate-900">Registration Request Submitted!</h3>
            <p className="text-xs text-slate-600 max-w-md mx-auto leading-relaxed">
              Your account application for <strong>{name}</strong> has been forwarded to the <strong className="text-indigo-700">School Administrator</strong> of code <strong>{schoolCode}</strong>. Once approved, you can log in directly.
            </p>
            <div className="pt-2">
              <button
                onClick={handleDone}
                className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-xs transition"
              >
                Return to Application
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="p-5 sm:p-6 space-y-4 text-xs">
            
            <div className="bg-indigo-50/70 p-3 rounded-xl border border-indigo-200/80 flex items-start gap-2.5 text-indigo-900">
              <AlertCircle className="w-4 h-4 text-indigo-600 shrink-0 mt-0.5" />
              <div>
                <span className="font-bold">Multi-School Routing:</span> Your request is automatically routed to your campus admin based on the <strong className="underline">School Code</strong>.
              </div>
            </div>

            {/* School Code Input & Quick selector */}
            <div>
              <label className="font-bold text-slate-800 block mb-1">
                School Code * (Ask your campus administration)
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  required
                  placeholder="e.g. SAQ-01"
                  value={schoolCode}
                  onChange={e => setSchoolCode(e.target.value)}
                  className="flex-1 p-2.5 border border-slate-300 rounded-xl font-mono uppercase font-black focus:outline-indigo-500 bg-slate-50 text-indigo-950 text-sm"
                />
              </div>
              <div className="flex flex-wrap items-center gap-1.5 mt-1.5">
                <span className="text-[10px] text-slate-400">Sample active codes:</span>
                {schools.map(s => (
                  <button
                    type="button"
                    key={s.id}
                    onClick={() => setSchoolCode(s.code)}
                    className="px-2 py-0.5 rounded bg-slate-100 hover:bg-indigo-100 text-slate-700 hover:text-indigo-800 font-mono text-[10px] font-bold border border-slate-200"
                  >
                    {s.code} ({s.name.split(' ')[0]})
                  </button>
                ))}
              </div>
            </div>

            {/* Account Role */}
            <div>
              <label className="font-bold text-slate-800 block mb-1">Account Role *</label>
              <div className="grid grid-cols-3 gap-2">
                {(['parent', 'student', 'teacher'] as const).map(r => (
                  <button
                    type="button"
                    key={r}
                    onClick={() => setRole(r)}
                    className={`py-2 px-3 rounded-xl border text-center font-bold capitalize transition ${
                      role === r
                        ? 'bg-indigo-600 text-white border-indigo-600 shadow-xs'
                        : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    {r}
                  </button>
                ))}
              </div>
            </div>

            {/* Name */}
            <div>
              <label className="font-bold text-slate-800 block mb-1">Full Legal Name *</label>
              <input
                type="text"
                required
                placeholder="e.g. Muhammad Zubair"
                value={name}
                onChange={e => setName(e.target.value)}
                className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 font-bold"
              />
            </div>

            {/* Contact Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="font-bold text-slate-800 block mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  placeholder="zubair@gmail.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500"
                />
              </div>
              <div>
                <label className="font-bold text-slate-800 block mb-1">Mobile / WhatsApp # *</label>
                <input
                  type="text"
                  required
                  placeholder="0300-1234567"
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 font-mono"
                />
              </div>
            </div>

            {/* If Parent or Student, Admission No */}
            {(role === 'parent' || role === 'student') && (
              <div>
                <label className="font-bold text-slate-800 block mb-1">
                  {role === 'parent' ? "Child's Admission Number (if already enrolled)" : "Your Admission Number"}
                </label>
                <input
                  type="text"
                  placeholder="e.g. 1001"
                  value={childAdmissionNo}
                  onChange={e => setChildAdmissionNo(e.target.value)}
                  className="w-full p-2.5 border border-slate-300 rounded-xl focus:outline-indigo-500 font-mono"
                />
              </div>
            )}

            {/* Submit */}
            <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
              <button
                type="button"
                onClick={closeRegistrationModal}
                className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-xs transition"
              >
                Submit Registration Request
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
