import React, { useState, useEffect } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  Users, MessageSquare, DollarSign, Bell, Award,
  CheckCircle, CreditCard, Printer, Send, Download
} from 'lucide-react';
import { exportStudentIDCardPDF } from '../../utils/pdfExport';

export const ParentPortal: React.FC = () => {
  const {
    currentUser, students, invoices, notices, openPrintModal,
    activeTab, setActiveTab, branding
  } = useSchool();

  const [activeSubTab, setActiveSubTab] = useState<'children' | 'fees' | 'messages' | 'notices'>(() => {
    if (activeTab === 'parent-fees') return 'fees';
    if (activeTab === 'parent-notices') return 'notices';
    if (activeTab === 'messages') return 'messages';
    return 'children';
  });

  // Sync internal subtab whenever the sidebar navigation changes activeTab
  useEffect(() => {
    if (activeTab === 'parent-fees') {
      setActiveSubTab('fees');
    } else if (activeTab === 'parent-notices') {
      setActiveSubTab('notices');
    } else if (activeTab === 'messages') {
      setActiveSubTab('messages');
    } else if (activeTab === 'dashboard') {
      setActiveSubTab('children');
    }
  }, [activeTab]);

  const handleSubTabClick = (tab: 'children' | 'fees' | 'messages' | 'notices') => {
    setActiveSubTab(tab);
    if (tab === 'fees') setActiveTab('parent-fees');
    else if (tab === 'notices') setActiveTab('parent-notices');
    else if (tab === 'messages') setActiveTab('messages');
    else setActiveTab('dashboard');
  };

  // Enrolled children linked to parent family or matching father
  const myChildren = students.filter(s =>
    s.fatherName.toLowerCase().includes('raza') ||
    s.familyId === 'fam-1' ||
    s.fatherContact === currentUser.phone
  );

  // Messages simulator
  const [messages, setMessages] = useState([
    { id: "1", from: "Ms. Hina Zahid (English Teacher)", text: "Zain performed exceptionally well in the reading comprehension test today!", time: "Yesterday, 02:15 PM" },
    { id: "2", from: "Principal Office", text: "Reminder: Parent-Teacher Meeting is scheduled for Saturday 9:00 AM.", time: "2 days ago" }
  ]);
  const [newMsg, setNewMsg] = useState('');

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMsg) return;
    setMessages(prev => [
      ...prev,
      { id: Date.now().toString(), from: "You (Parent)", text: newMsg, time: "Just now" }
    ]);
    setNewMsg('');
  };

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-gradient-to-r from-amber-700 to-amber-900 rounded-2xl p-5 text-white shadow-md flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider bg-white/10 px-2 py-0.5 rounded text-amber-200">
            Parent Guardian Portal • {branding.name}
          </span>
          <h1 className="text-xl font-bold mt-1">Welcome, {currentUser.name}</h1>
          <p className="text-xs text-amber-100">
            Track your children&apos;s attendance, fee dues, report cards, and communicate with teachers.
          </p>
        </div>

        <div className="flex items-center gap-1.5 p-1 bg-black/20 rounded-xl text-xs">
          {[
            { id: 'children', label: 'My Children' },
            { id: 'fees', label: 'Fee Vouchers & Pay' },
            { id: 'messages', label: 'Teacher Chat' },
            { id: 'notices', label: 'Announcements' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => handleSubTabClick(tab.id as any)}
              className={`px-3 py-1.5 rounded-lg font-bold capitalize transition ${
                activeSubTab === tab.id ? 'bg-white text-amber-950 shadow-xs' : 'text-amber-100 hover:bg-white/10'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* SubTab 1: My Children */}
      {activeSubTab === 'children' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-900">Enrolled Children ({myChildren.length})</h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {myChildren.map(child => {
              const childInvoices = invoices.filter(i => i.studentId === child.id);
              const pendingInv = childInvoices.filter(i => i.status !== 'paid');
              const pendingFee = pendingInv.reduce((sum, i) => sum + (i.total - i.paidAmount), 0);

              return (
                <div key={child.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between space-y-4">
                  <div>
                    <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-amber-500 to-indigo-600 flex items-center justify-center font-bold text-white text-lg">
                          {child.name.charAt(0)}
                        </div>
                        <div>
                          <h3 className="font-bold text-base text-slate-900 leading-tight">{child.name}</h3>
                          <div className="text-xs text-slate-500">{child.className} • Roll #{child.rollNo} • Adm #{child.admissionNo}</div>
                        </div>
                      </div>
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700">
                        Active
                      </span>
                    </div>

                    <div className="grid grid-cols-2 gap-3 mt-4">
                      <div className="bg-slate-50 p-3 rounded-xl border border-slate-100">
                        <div className="text-[10px] font-bold uppercase text-slate-400">Attendance</div>
                        <div className="text-lg font-black text-emerald-600 mt-0.5">96%</div>
                        <div className="text-[10px] text-slate-500">Present this month</div>
                      </div>
                      <div className="bg-slate-50 p-3 rounded-xl border border-slate-100">
                        <div className="text-[10px] font-bold uppercase text-slate-400">Pending Dues</div>
                        <div className={`text-lg font-black mt-0.5 ${pendingFee > 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                          Rs {pendingFee.toLocaleString()}
                        </div>
                        <div className="text-[10px] text-slate-500">{pendingFee > 0 ? "Voucher due" : "All cleared"}</div>
                      </div>
                    </div>
                  </div>

                  <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-2">
                    <button
                      onClick={() => openPrintModal('reportcard', child)}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-700 rounded-lg text-xs font-bold transition"
                    >
                      <Award className="w-3.5 h-3.5" />
                      <span>Report Card</span>
                    </button>
                    <button
                      onClick={() => exportStudentIDCardPDF(child, branding)}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 hover:bg-amber-100 text-amber-800 rounded-lg text-xs font-bold transition border border-amber-200"
                    >
                      <Download className="w-3.5 h-3.5 text-amber-700" />
                      <span>ID Card (PDF)</span>
                    </button>
                    {pendingFee > 0 && pendingInv[0] && (
                      <button
                        onClick={() => openPrintModal('payment', pendingInv[0])}
                        className="flex items-center gap-1.5 px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold shadow-xs transition"
                      >
                        <DollarSign className="w-3.5 h-3.5" />
                        <span>Pay Rs {pendingFee.toLocaleString()}</span>
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* SubTab 2: Fee Vouchers & Pay */}
      {activeSubTab === 'fees' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <DollarSign className="w-5 h-5 text-emerald-600" />
                <span>Family Fee Vouchers & Online Pay</span>
              </h2>
              <p className="text-xs text-slate-500">Pay online via Card / JazzCash or print official bank fee vouchers for Habib Bank</p>
            </div>
          </div>

          <div className="space-y-3">
            {myChildren.length === 0 ? (
              <div className="p-8 bg-white rounded-2xl border border-slate-200 text-center text-slate-400 text-xs">
                No children linked to this parent profile yet. Contact school administration.
              </div>
            ) : (
              myChildren.map(child => {
                const childInvoices = invoices.filter(i => i.studentId === child.id);
                return childInvoices.map(inv => (
                  <div key={inv.id} className="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-xs text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                          {inv.voucherNo}
                        </span>
                        <span className="text-xs font-bold text-slate-900">{inv.studentName} ({inv.className})</span>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase ${
                          inv.status === 'paid' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-800'
                        }`}>
                          {inv.status}
                        </span>
                      </div>
                      <div className="text-xs text-slate-500 mt-1">
                        {inv.month} • Due by {inv.dueDate} • {inv.items.map(it => `${it.label}: Rs ${it.amount}`).join(' • ')}
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <div className="text-right">
                        <div className="text-xs text-slate-400">Total Due</div>
                        <div className="text-base font-black text-slate-900">Rs {inv.total.toLocaleString()}</div>
                      </div>
                      {inv.status !== 'paid' ? (
                        <button
                          onClick={() => openPrintModal('payment', inv)}
                          className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold shadow-xs transition"
                        >
                          Pay Online
                        </button>
                      ) : (
                        <span className="text-xs text-emerald-700 font-bold bg-emerald-50 px-2.5 py-1 rounded-lg">
                          Paid ✓
                        </span>
                      )}
                      <button
                        onClick={() => openPrintModal('voucher', inv)}
                        className="p-2 border border-slate-200 hover:bg-slate-50 rounded-xl text-slate-600"
                        title="Print Bank Fee Voucher"
                      >
                        <Printer className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                ));
              })
            )}
          </div>
        </div>
      )}

      {/* SubTab 3: Messages */}
      {activeSubTab === 'messages' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 max-w-2xl space-y-4">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-base font-bold text-slate-900">Direct Messages with Class Incharges</h2>
            <p className="text-xs text-slate-500">Communicate directly with your children&apos;s teachers and school staff.</p>
          </div>

          <div className="space-y-3 max-h-[350px] overflow-y-auto pr-1">
            {messages.map(m => (
              <div key={m.id} className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 space-y-1">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-bold text-slate-900">{m.from}</span>
                  <span className="text-[10px] text-slate-400 font-mono">{m.time}</span>
                </div>
                <p className="text-xs text-slate-700 leading-relaxed">{m.text}</p>
              </div>
            ))}
          </div>

          <form onSubmit={handleSendMessage} className="flex gap-2 pt-2 border-t border-slate-100">
            <input
              type="text"
              placeholder="Type message to teacher or school admin..."
              value={newMsg}
              onChange={e => setNewMsg(e.target.value)}
              className="flex-1 p-2 text-xs border border-slate-300 rounded-xl focus:outline-indigo-500"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition flex items-center gap-1.5"
            >
              <Send className="w-3.5 h-3.5" />
              <span>Send</span>
            </button>
          </form>
        </div>
      )}

      {/* SubTab 4: Announcements & Notices */}
      {activeSubTab === 'notices' && (
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <Bell className="w-5 h-5 text-indigo-600" />
              <span>School Announcements & Parent Circulars</span>
            </h2>
            <p className="text-xs text-slate-500">Announcements regarding exams, holidays, and school timings</p>
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
