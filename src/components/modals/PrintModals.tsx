import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import {
  Printer, X, CheckCircle, CreditCard,
  QrCode, Shield, Download, Building, Award
} from 'lucide-react';
import confetti from 'canvas-confetti';
import {
  exportStudentIDCardPDF,
  exportFeeVoucherPDF,
  exportAcademicReportCardPDF
} from '../../utils/pdfExport';

export const PrintModals: React.FC = () => {
  const {
    activePrintModal, activePrintData, closePrintModal,
    branding, gradeBands, gradeEntries, recordFeePayment
  } = useSchool();

  const [paymentMethod, setPaymentMethod] = useState<'card' | 'jazzcash' | 'easypaisa' | 'bank'>('card');
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentDone, setPaymentDone] = useState(false);

  if (!activePrintModal || !activePrintData) return null;

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadIdCard = () => {
    exportStudentIDCardPDF(activePrintData, branding);
  };

  const handleDownloadFeeVoucher = () => {
    exportFeeVoucherPDF(activePrintData, activePrintData.student || null, branding);
  };

  const handleDownloadReportCard = () => {
    const studentEntries = gradeEntries.filter(ge => ge.studentId === activePrintData.id);
    exportAcademicReportCardPDF(
      activePrintData,
      studentEntries,
      gradeBands,
      branding,
      activePrintData.examTitle || 'Term 1 Mid-Term Examination 2026'
    );
  };

  const handleExecutePayment = (e: React.FormEvent) => {
    e.preventDefault();
    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      setPaymentDone(true);
      recordFeePayment(
        activePrintData.id,
        activePrintData.total - (activePrintData.paidAmount || 0),
        paymentMethod === 'card' ? 'Visa / Mastercard' : paymentMethod === 'jazzcash' ? 'JazzCash Mobile' : 'Online Bank Transfer'
      );
      try {
        confetti({
          particleCount: 100,
          spread: 70,
          origin: { y: 0.6 }
        });
      } catch (err) {
        console.warn("Confetti error", err);
      }
    }, 1200);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-slate-950/80 backdrop-blur-xs overflow-y-auto">
      
      {/* 1. STUDENT IDENTITY CARD MODAL (PROPER CR80 CARD SETTING) */}
      {activePrintModal === 'idcard' && (
        <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200">
          <div className="p-3 bg-slate-900 text-white flex items-center justify-between no-print">
            <span className="text-xs font-bold flex items-center gap-1.5">
              <CreditCard className="w-4 h-4 text-amber-400" />
              <span>Official Student ID Card</span>
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={handleDownloadIdCard}
                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-bold flex items-center gap-1 shadow-xs transition"
                title="Download as PDF in proper setting"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Download PDF</span>
              </button>
              <button
                onClick={handlePrint}
                className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-xs font-bold flex items-center gap-1"
              >
                <Printer className="w-3.5 h-3.5" />
                <span>Print</span>
              </button>
              <button onClick={closePrintModal} className="p-1 text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="p-6 bg-slate-100 flex justify-center">
            <div className="w-[320px] h-[480px] bg-white rounded-2xl shadow-xl overflow-hidden border-2 border-indigo-950 flex flex-col justify-between text-left relative">
              
              {/* Card Header */}
              <div className="bg-[#4A148C] text-white p-3.5 text-center relative overflow-hidden">
                <div className="text-[12px] font-black uppercase tracking-wider leading-tight">
                  {branding.name}
                </div>
                <div className="text-[9px] font-bold text-amber-400 tracking-wider uppercase mt-0.5">
                  Student Identity Card
                </div>
                <div className="text-[8px] text-purple-200 font-mono mt-0.5">
                  Session {branding.session}
                </div>
              </div>

              {/* Card Body */}
              <div className="p-4 flex-1 flex flex-col justify-between text-xs space-y-3">
                <div className="flex gap-3 items-center">
                  <div className="w-24 h-28 rounded-xl bg-slate-100 border border-slate-300 flex flex-col items-center justify-center text-slate-400 shrink-0 shadow-inner">
                    <span className="text-3xl font-black text-indigo-900">
                      {activePrintData.name?.charAt(0) || "S"}
                    </span>
                    <span className="text-[9px] text-slate-400 font-bold uppercase mt-1">Photo</span>
                  </div>
                  <div className="min-w-0 space-y-1">
                    <div>
                      <div className="text-[8px] text-slate-400 uppercase font-bold">Student Name</div>
                      <div className="font-extrabold text-slate-900 text-sm leading-tight truncate">
                        {activePrintData.name}
                      </div>
                    </div>
                    <div>
                      <div className="text-[8px] text-slate-400 uppercase font-bold">Admission #</div>
                      <div className="font-mono font-bold text-indigo-700">
                        {activePrintData.admissionNo || "1001"}
                      </div>
                    </div>
                    <div>
                      <div className="text-[8px] text-slate-400 uppercase font-bold">Class & Section</div>
                      <div className="font-bold text-slate-800 text-[11px]">
                        {activePrintData.className || "Class 4 - A"}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-200/80 space-y-1 text-[11px]">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Father Name:</span>
                    <strong className="text-slate-800">{activePrintData.fatherName || "—"}</strong>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Emergency Phone:</span>
                    <strong className="font-mono text-slate-800">{activePrintData.phone || branding.phone}</strong>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Blood Group:</span>
                    <strong className="text-rose-700 font-bold">{activePrintData.bloodGroup || "O+"}</strong>
                  </div>
                </div>

                {/* QR Code & Barcode Pattern */}
                <div className="flex items-center justify-between pt-1">
                  <div className="flex items-center gap-2">
                    <div className="w-14 h-14 bg-white p-1 border border-indigo-900 rounded-lg flex items-center justify-center">
                      <QrCode className="w-12 h-12 text-indigo-950" />
                    </div>
                    <div className="text-[9px] text-slate-400 leading-tight">
                      Scan at gate for
                      <br /><strong className="text-slate-700">instant attendance</strong>
                    </div>
                  </div>

                  <div className="text-right">
                    <div className="text-[9px] font-serif italic text-slate-500">Authorized Sign</div>
                    <div className="w-16 h-0.5 bg-slate-400 mt-4" />
                  </div>
                </div>
              </div>

              {/* Card Footer */}
              <div className="bg-[#4A148C] text-white py-1.5 px-3 text-center text-[8px] font-medium">
                Valid for current session • If found please return to school office
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2. OFFICIAL BANK FEE VOUCHER MODAL */}
      {activePrintModal === 'voucher' && (
        <div className="relative w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200">
          <div className="p-3 bg-slate-900 text-white flex items-center justify-between no-print">
            <span className="text-xs font-bold flex items-center gap-1.5">
              <Building className="w-4 h-4 text-emerald-400" />
              <span>Official Bank Fee Voucher</span>
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={handleDownloadFeeVoucher}
                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-bold flex items-center gap-1 shadow-xs transition"
                title="Download 3-Copy Bank Voucher PDF"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Download PDF</span>
              </button>
              <button
                onClick={handlePrint}
                className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-xs font-bold flex items-center gap-1"
              >
                <Printer className="w-3.5 h-3.5" />
                <span>Print Voucher</span>
              </button>
              <button onClick={closePrintModal} className="p-1 text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="p-6 bg-slate-50 overflow-y-auto max-h-[80vh]">
            <div className="bg-white p-6 rounded-xl border-2 border-slate-300 shadow-md text-xs space-y-4">
              <div className="border-b-2 border-slate-800 pb-3 flex justify-between items-start">
                <div>
                  <h1 className="text-lg font-black text-slate-950 uppercase">{branding.name}</h1>
                  <p className="text-[11px] text-slate-600">{branding.address} • Ph: {branding.phone}</p>
                  <p className="text-[10px] font-bold text-indigo-700 uppercase mt-0.5">Habib Bank / JazzCash / EasyPaisa Voucher</p>
                </div>
                <div className="text-right">
                  <div className="font-mono font-black text-sm text-indigo-900">{activePrintData.voucherNo}</div>
                  <div className="text-[10px] text-slate-500">Issue Date: {activePrintData.createdAt || "2026-10-01"}</div>
                  <div className="text-[11px] font-bold text-rose-600">Due Date: {activePrintData.dueDate || "2026-10-10"}</div>
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 bg-slate-50 p-3 rounded-lg border border-slate-200">
                <div>
                  <span className="text-[9px] text-slate-400 uppercase font-bold block">Student Name</span>
                  <strong className="text-slate-900">{activePrintData.studentName}</strong>
                </div>
                <div>
                  <span className="text-[9px] text-slate-400 uppercase font-bold block">Class & Section</span>
                  <strong className="text-slate-900">{activePrintData.className}</strong>
                </div>
                <div>
                  <span className="text-[9px] text-slate-400 uppercase font-bold block">Billing Month</span>
                  <strong className="text-indigo-900">{activePrintData.month}</strong>
                </div>
                <div>
                  <span className="text-[9px] text-slate-400 uppercase font-bold block">Payment Status</span>
                  <strong className={activePrintData.status === 'paid' ? 'text-emerald-700 uppercase' : 'text-amber-700 uppercase'}>
                    {activePrintData.status || 'Pending'}
                  </strong>
                </div>
              </div>

              <table className="w-full text-left border border-slate-200">
                <thead className="bg-slate-100 border-b border-slate-200 text-[10px] uppercase font-bold text-slate-600">
                  <tr>
                    <th className="py-2 px-3">#</th>
                    <th className="py-2 px-3">Fee Particular / Head</th>
                    <th className="py-2 px-3 text-right">Amount (PKR)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {(activePrintData.items || [{ label: "Monthly Tuition Fee", amount: activePrintData.total }]).map((item: any, i: number) => (
                    <tr key={i}>
                      <td className="py-2 px-3 text-slate-400">{i + 1}</td>
                      <td className="py-2 px-3 font-semibold text-slate-800">{item.label}</td>
                      <td className="py-2 px-3 text-right font-mono font-bold">Rs {item.amount.toLocaleString()}</td>
                    </tr>
                  ))}
                  <tr className="bg-slate-50 font-black text-sm">
                    <td colSpan={2} className="py-2.5 px-3 text-slate-900 uppercase">Total Payable Amount</td>
                    <td className="py-2.5 px-3 text-right text-indigo-950 font-mono text-base">
                      Rs {activePrintData.total.toLocaleString()}
                    </td>
                  </tr>
                </tbody>
              </table>

              <div className="border-t border-slate-200 pt-3 flex flex-col sm:flex-row justify-between gap-4 text-[10px] text-slate-600">
                <div className="space-y-1">
                  <div className="font-bold text-slate-800">Deposit Channels:</div>
                  <div>Account Title: {branding.name}</div>
                  <div>HBL A/C: 0142-7901849201 • JazzCash: 0300-1234567</div>
                  <div className="text-rose-600 font-semibold">Late fee of Rs 50/day applicable after due date.</div>
                </div>
                <div className="text-right sm:text-left space-y-1 max-w-xs">
                  <div className="text-[11px] text-slate-500">Bank Cashier / Authorized Stamp</div>
                  <div className="w-24 h-0.5 bg-slate-400 mt-4 mr-auto" />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 3. ACADEMIC REPORT CARD MODAL */}
      {activePrintModal === 'reportcard' && (
        <div className="relative w-full max-w-3xl bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200">
          <div className="p-3 bg-slate-900 text-white flex items-center justify-between no-print">
            <span className="text-xs font-bold flex items-center gap-1.5">
              <Award className="w-4 h-4 text-amber-400" />
              <span>Official Academic Report Card</span>
            </span>
            <div className="flex items-center gap-2">
              <button
                onClick={handleDownloadReportCard}
                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-bold flex items-center gap-1 shadow-xs transition"
                title="Download Official Result Card PDF"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Download PDF</span>
              </button>
              <button
                onClick={handlePrint}
                className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-xs font-bold flex items-center gap-1"
              >
                <Printer className="w-3.5 h-3.5" />
                <span>Print</span>
              </button>
              <button onClick={closePrintModal} className="p-1 text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="p-6 bg-slate-100 overflow-y-auto max-h-[85vh] flex justify-center">
            <div className="w-full max-w-2xl bg-white p-6 sm:p-8 rounded-xl shadow-lg border border-slate-300 text-xs space-y-5">
              <div className="text-center border-b-2 border-indigo-900 pb-4">
                <div className="w-12 h-12 rounded-xl bg-indigo-900 text-white flex items-center justify-center font-black mx-auto mb-2 text-xl">
                  {branding.name.charAt(0)}
                </div>
                <h1 className="text-xl font-black text-slate-900 tracking-tight uppercase">{branding.name}</h1>
                <p className="text-[11px] text-slate-600">{branding.address} • Contact: {branding.phone}</p>
                <div className="mt-2 inline-block px-4 py-1 rounded-full bg-indigo-50 border border-indigo-200 text-indigo-950 font-bold text-xs uppercase tracking-wider">
                  Official Academic Report Card • Session {branding.session}
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 bg-slate-50 p-3.5 rounded-xl border border-slate-200">
                <div>
                  <span className="text-[9px] uppercase font-bold text-slate-400 block">Student Name</span>
                  <strong className="text-sm text-slate-950 font-bold">{activePrintData.name}</strong>
                </div>
                <div>
                  <span className="text-[9px] uppercase font-bold text-slate-400 block">Roll / Admission #</span>
                  <strong className="font-mono text-indigo-800">Roll {activePrintData.rollNo || "01"} (Adm #{activePrintData.admissionNo || "1001"})</strong>
                </div>
                <div>
                  <span className="text-[9px] uppercase font-bold text-slate-400 block">Class & Section</span>
                  <strong className="text-slate-900">{activePrintData.className || "Class 4 - A"}</strong>
                </div>
                <div>
                  <span className="text-[9px] uppercase font-bold text-slate-400 block">Examination Term</span>
                  <strong className="text-slate-900">Term 1 (Mid-Term 2026)</strong>
                </div>
              </div>

              <table className="w-full text-left border border-slate-200">
                <thead className="bg-[#1A237E] text-white text-[10px] uppercase font-bold">
                  <tr>
                    <th className="py-2.5 px-3">Subject Name</th>
                    <th className="py-2.5 px-3 text-center">Max Marks</th>
                    <th className="py-2.5 px-3 text-center">Marks Obtained</th>
                    <th className="py-2.5 px-3 text-center">Percentage</th>
                    <th className="py-2.5 px-3 text-center">Grade</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {[
                    { subj: "English Language & Comp", max: 100, obt: 94, grade: "A+" },
                    { subj: "Mathematics", max: 100, obt: 98, grade: "A+" },
                    { subj: "Urdu Adab", max: 100, obt: 89, grade: "A" },
                    { subj: "General Science", max: 100, obt: 92, grade: "A+" },
                    { subj: "Islamiat & Quranic Studies", max: 100, obt: 95, grade: "A+" },
                    { subj: "Computer Science", max: 100, obt: 96, grade: "A+" }
                  ].map((row, i) => (
                    <tr key={i} className="hover:bg-slate-50">
                      <td className="py-2 px-3 font-semibold text-slate-900">{row.subj}</td>
                      <td className="py-2 px-3 text-center text-slate-500">{row.max}</td>
                      <td className="py-2 px-3 text-center font-bold text-slate-900">{row.obt}</td>
                      <td className="py-2 px-3 text-center font-mono font-bold text-indigo-700">{row.obt}%</td>
                      <td className="py-2 px-3 text-center font-black text-emerald-700">{row.grade}</td>
                    </tr>
                  ))}
                  <tr className="bg-amber-50/70 border-t-2 border-amber-300 font-black text-xs">
                    <td className="py-3 px-3 uppercase text-slate-900">Grand Total & Overall Grade</td>
                    <td className="py-3 px-3 text-center">600</td>
                    <td className="py-3 px-3 text-center text-slate-950 font-bold">564</td>
                    <td className="py-3 px-3 text-center text-indigo-950 font-black text-sm">94.0%</td>
                    <td className="py-3 px-3 text-center text-emerald-800 text-sm font-black">A+ (Outstanding)</td>
                  </tr>
                </tbody>
              </table>

              <div className="grid grid-cols-2 gap-4 pt-2">
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-1">
                  <div className="text-[10px] uppercase font-bold text-slate-400">Attendance Assessment</div>
                  <div className="text-xs text-slate-700">Attendance: <strong className="text-emerald-700">96.0% (Present)</strong></div>
                  <div className="text-xs text-slate-500">Punctual and consistent throughout the term.</div>
                </div>
                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-1">
                  <div className="text-[10px] uppercase font-bold text-slate-400">Class Incharge Remarks</div>
                  <div className="text-xs text-slate-800 italic">
                    &ldquo;Excellent conduct, keen interest in science experiments, and enthusiastic class participation.&rdquo;
                  </div>
                </div>
              </div>

              <div className="pt-8 flex justify-between items-end text-center text-[11px] text-slate-600">
                <div>
                  <div className="w-32 h-0.5 bg-slate-400 mx-auto mb-1" />
                  <span>Class Teacher</span>
                </div>
                <div>
                  <div className="w-32 h-0.5 bg-slate-400 mx-auto mb-1" />
                  <span>Exam Controller</span>
                </div>
                <div>
                  <div className="w-32 h-0.5 bg-slate-800 mx-auto mb-1" />
                  <span className="font-bold text-slate-900">Principal Signature</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 4. ONLINE FEE PAYMENT GATEWAY MODAL */}
      {activePrintModal === 'payment' && (
        <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-200">
          <div className="p-4 bg-gradient-to-r from-emerald-800 to-slate-900 text-white flex items-center justify-between">
            <h2 className="font-bold text-sm flex items-center gap-2">
              <CreditCard className="w-4 h-4 text-emerald-400" />
              <span>Online Fee Payment Gateway</span>
            </h2>
            <button onClick={closePrintModal}>
              <X className="w-5 h-5 text-slate-300 hover:text-white" />
            </button>
          </div>

          <div className="p-5 space-y-4 text-xs">
            <div className="bg-emerald-50 rounded-xl p-4 border border-emerald-200 text-center">
              <span className="text-[10px] uppercase font-bold text-emerald-800">Total Amount Payable</span>
              <div className="text-2xl font-black text-emerald-950 mt-0.5">
                Rs {(activePrintData.total - (activePrintData.paidAmount || 0)).toLocaleString()}
              </div>
              <div className="text-[11px] text-slate-600 mt-1">
                Student: <strong>{activePrintData.studentName}</strong> • Voucher #{activePrintData.voucherNo}
              </div>
            </div>

            {paymentDone ? (
              <div className="py-6 text-center space-y-3">
                <CheckCircle className="w-16 h-16 text-emerald-500 mx-auto animate-bounce" />
                <h3 className="font-bold text-base text-slate-900">Payment Successfully Recorded!</h3>
                <p className="text-slate-600">
                  Transaction verified and official receipt generated in accounts.
                </p>
                <div className="pt-2">
                  <button
                    onClick={() => {
                      closePrintModal();
                      setPaymentDone(false);
                    }}
                    className="px-6 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-xs"
                  >
                    Done & Close
                  </button>
                </div>
              </div>
            ) : (
              <form onSubmit={handleExecutePayment} className="space-y-4">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Select Payment Method</label>
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { id: 'card', label: 'Debit / Credit Card' },
                      { id: 'jazzcash', label: 'JazzCash Wallet' },
                      { id: 'bank', label: 'Direct Bank Transfer' }
                    ].map(m => (
                      <button
                        type="button"
                        key={m.id}
                        onClick={() => setPaymentMethod(m.id as any)}
                        className={`p-2.5 rounded-xl border text-center font-bold text-[11px] transition ${
                          paymentMethod === m.id
                            ? 'bg-emerald-50 border-emerald-500 text-emerald-950 ring-2 ring-emerald-500/20'
                            : 'border-slate-200 hover:bg-slate-50 text-slate-600'
                        }`}
                      >
                        {m.label}
                      </button>
                    ))}
                  </div>
                </div>

                {paymentMethod === 'card' && (
                  <div className="space-y-2.5">
                    <div>
                      <label className="font-semibold text-slate-700 block mb-0.5">Card Number</label>
                      <input
                        type="text"
                        placeholder="4242 •••• •••• 4242"
                        defaultValue="4242 8192 3847 1029"
                        className="w-full p-2 border border-slate-300 rounded-lg font-mono"
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="font-semibold text-slate-700 block mb-0.5">Expiry</label>
                        <input
                          type="text"
                          placeholder="MM/YY"
                          defaultValue="08/29"
                          className="w-full p-2 border border-slate-300 rounded-lg font-mono text-center"
                        />
                      </div>
                      <div>
                        <label className="font-semibold text-slate-700 block mb-0.5">CVV</label>
                        <input
                          type="password"
                          placeholder="•••"
                          defaultValue="842"
                          className="w-full p-2 border border-slate-300 rounded-lg font-mono text-center"
                        />
                      </div>
                    </div>
                  </div>
                )}

                {paymentMethod === 'jazzcash' && (
                  <div>
                    <label className="font-semibold text-slate-700 block mb-0.5">JazzCash Mobile Wallet #</label>
                    <input
                      type="text"
                      defaultValue="0300-1234567"
                      className="w-full p-2 border border-slate-300 rounded-lg font-mono font-bold text-slate-800"
                    />
                    <span className="text-[10px] text-slate-400 mt-1 block">
                      An approval prompt will be sent to the registered mobile account.
                    </span>
                  </div>
                )}

                {paymentMethod === 'bank' && (
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 text-[11px] text-slate-600 space-y-1">
                    <div>Bank: <strong>Habib Bank Limited (HBL)</strong></div>
                    <div>Account Title: <strong>{branding.name}</strong></div>
                    <div>A/C Number: <strong className="font-mono">0142-7901849201</strong></div>
                  </div>
                )}

                <div className="pt-2">
                  <button
                    type="submit"
                    disabled={isProcessing}
                    className="w-full py-3 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-bold shadow-md transition flex items-center justify-center gap-2"
                  >
                    {isProcessing ? (
                      <span>Verifying with Gateway...</span>
                    ) : (
                      <>
                        <Shield className="w-4 h-4" />
                        <span>Pay Rs {(activePrintData.total - (activePrintData.paidAmount || 0)).toLocaleString()} Now</span>
                      </>
                    )}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

    </div>
  );
};
