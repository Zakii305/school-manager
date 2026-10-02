import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { Invoice, PaymentReceipt, PayrollItem, Expense } from '../../types';
import {
  DollarSign, Wallet, FileText, Briefcase, Plus,
  Search, CheckCircle, Printer, Download, CreditCard,
  X, AlertTriangle, ArrowUpRight, TrendingUp, Calendar
} from 'lucide-react';
import { exportFeeChallanPDF, exportFamilyChallanPDF, exportFinanceReportPDF } from '../../utils/pdfExport';

export const FinanceManager: React.FC = () => {
  const {
    invoices, generateInvoice,
    receipts, recordFeePayment,
    payroll, runPayrollForMonth, markPayrollPaid,
    expenses, addExpense, deleteExpense,
    students, families, staff, classes, branding, openPrintModal
  } = useSchool();

  const [activeFinanceTab, setActiveFinanceTab] = useState<'fees' | 'family' | 'receipts' | 'payroll' | 'expenses'>('fees');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'paid' | 'pending' | 'overdue'>('all');

  // Modals
  const [isAddInvoiceOpen, setIsAddInvoiceOpen] = useState(false);
  const [isAddExpenseOpen, setIsAddExpenseOpen] = useState(false);
  const [isPayrollModalOpen, setIsPayrollModalOpen] = useState(false);
  const [payrollMonth, setPayrollMonth] = useState('October 2026');

  // New Invoice Form
  const [invoiceForm, setInvoiceForm] = useState({
    studentId: students[0]?.id || '',
    month: 'October 2026',
    tuitionFee: 3500,
    examFee: 500,
    vanFee: 0,
    admissionFee: 0,
    arrears: 0,
    discount: 0,
    dueDate: '2026-10-15'
  });

  // New Expense Form
  const [expenseForm, setExpenseForm] = useState({
    category: 'Utilities & WiFi' as Expense['category'],
    title: 'Electricity & Generator Fuel',
    amount: 12000,
    date: new Date().toISOString().split('T')[0],
    paidTo: 'WAPDA / Local Station',
    paymentMode: 'Online Bank' as Expense['paymentMode'],
    invoiceRef: `INV-EXP-${Math.floor(100 + Math.random() * 900)}`,
    notes: 'Monthly office and classroom electricity dues.'
  });

  // Calculate Totals
  const totalCollected = receipts.reduce((acc, r) => acc + r.amount, 0);
  const totalPending = invoices
    .filter(i => i.status !== 'paid')
    .reduce((acc, i) => acc + (i.total - (i.paidAmount || 0)), 0);
  const totalExpenses = expenses.reduce((acc, e) => acc + e.amount, 0);
  const totalPayroll = payroll.reduce((acc, p) => acc + p.net, 0);

  const handleCreateInvoice = (e: React.FormEvent) => {
    e.preventDefault();
    const st = students.find(s => s.id === invoiceForm.studentId);
    if (!st) return;

    const currentClass = classes.find(c => c.id === st.classId);
    const className = currentClass ? `${currentClass.name} - ${currentClass.section}` : st.className;

    const total =
      invoiceForm.tuitionFee +
      invoiceForm.examFee +
      invoiceForm.vanFee +
      invoiceForm.admissionFee +
      invoiceForm.arrears -
      invoiceForm.discount;

    generateInvoice({
      studentId: st.id,
      studentName: st.name,
      classId: st.classId,
      className,
      month: invoiceForm.month,
      tuitionFee: invoiceForm.tuitionFee,
      vanFee: invoiceForm.vanFee,
      admissionFee: invoiceForm.admissionFee,
      examFee: invoiceForm.examFee,
      arrears: invoiceForm.arrears,
      discount: invoiceForm.discount,
      total: Math.max(0, total),
      paidAmount: 0,
      dueDate: invoiceForm.dueDate,
      items: [
        { label: 'Monthly Tuition Fee', amount: invoiceForm.tuitionFee },
        { label: 'Examination Charges', amount: invoiceForm.examFee }
      ]
    });

    setIsAddInvoiceOpen(false);
  };

  const handleCreateExpense = (e: React.FormEvent) => {
    e.preventDefault();
    if (!expenseForm.title || expenseForm.amount <= 0) return;

    addExpense({
      ...expenseForm,
      approvedBy: 'Campus Admin'
    });

    setIsAddExpenseOpen(false);
  };

  const filteredInvoices = invoices.filter(inv => {
    const matchesSearch =
      inv.studentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      inv.voucherNo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      inv.className.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || inv.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Financial Overview Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Fee Realized</div>
            <div className="text-xl font-black text-emerald-600 mt-1">
              {branding.currency} {totalCollected.toLocaleString()}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">{receipts.length} verified receipts</div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
            <DollarSign className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Pending Dues</div>
            <div className="text-xl font-black text-amber-600 mt-1">
              {branding.currency} {totalPending.toLocaleString()}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">Awaiting bank deposit</div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center font-bold">
            <AlertTriangle className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Staff Payroll</div>
            <div className="text-xl font-black text-indigo-600 mt-1">
              {branding.currency} {totalPayroll.toLocaleString()}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">{payroll.length} staff salaries</div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
            <Briefcase className="w-6 h-6" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex items-center justify-between">
          <div>
            <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">School Expenses</div>
            <div className="text-xl font-black text-rose-600 mt-1">
              {branding.currency} {totalExpenses.toLocaleString()}
            </div>
            <div className="text-[10px] text-slate-400 mt-0.5">{expenses.length} operating vouchers</div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center font-bold">
            <TrendingUp className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Tabs and Actions Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-slate-200 shadow-xs">
        <div className="flex items-center gap-1.5 overflow-x-auto text-xs font-bold">
          {[
            { id: 'fees', label: 'Fee Collection', icon: DollarSign },
            { id: 'family', label: 'Family Vouchers', icon: Wallet },
            { id: 'receipts', label: 'Payment Receipts', icon: FileText },
            { id: 'payroll', label: 'Staff Payroll', icon: Briefcase },
            { id: 'expenses', label: 'Operating Expenses', icon: TrendingUp }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveFinanceTab(tab.id as any)}
              className={`flex items-center gap-1.5 px-3 py-2 rounded-xl transition whitespace-nowrap ${
                activeFinanceTab === tab.id
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              <tab.icon className="w-3.5 h-3.5" />
              <span>{tab.label}</span>
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2">
          {activeFinanceTab === 'fees' && (
            <button
              onClick={() => setIsAddInvoiceOpen(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-md transition"
            >
              <Plus className="w-4 h-4" />
              <span>Generate Fee Challan</span>
            </button>
          )}

          {activeFinanceTab === 'payroll' && (
            <button
              onClick={() => setIsPayrollModalOpen(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-md transition"
            >
              <Plus className="w-4 h-4" />
              <span>Run Payroll</span>
            </button>
          )}

          {activeFinanceTab === 'expenses' && (
            <button
              onClick={() => setIsAddExpenseOpen(true)}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl text-xs shadow-md transition"
            >
              <Plus className="w-4 h-4" />
              <span>Record Expense</span>
            </button>
          )}

          <button
            onClick={() => exportFinanceReportPDF(invoices, receipts, payroll, expenses, branding)}
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl text-xs transition"
            title="Export Monthly Finance Statement PDF"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Finance PDF</span>
          </button>
        </div>
      </div>

      {/* 1. FEE COLLECTION TAB */}
      {activeFinanceTab === 'fees' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="Search by student name or voucher #..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-500"
              />
            </div>
            <div>
              <select
                value={statusFilter}
                onChange={e => setStatusFilter(e.target.value as any)}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-800 focus:outline-none focus:border-indigo-500"
              >
                <option value="all">All Invoice Statuses</option>
                <option value="pending">Pending Payment</option>
                <option value="paid">Paid In Full</option>
                <option value="overdue">Overdue</option>
              </select>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                    <th className="py-3 px-4">Voucher #</th>
                    <th className="py-3 px-4">Student</th>
                    <th className="py-3 px-4">Month</th>
                    <th className="py-3 px-4">Tuition + Misc</th>
                    <th className="py-3 px-4">Payable Amount</th>
                    <th className="py-3 px-4">Due Date</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
                  {filteredInvoices.map(inv => (
                    <tr key={inv.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3 px-4 font-mono font-bold text-indigo-700">
                        {inv.voucherNo}
                      </td>
                      <td className="py-3 px-4">
                        <div className="font-bold text-slate-900">{inv.studentName}</div>
                        <div className="text-[11px] text-slate-400 font-mono">{inv.className}</div>
                      </td>
                      <td className="py-3 px-4 font-semibold">{inv.month}</td>
                      <td className="py-3 px-4">
                        {branding.currency} {inv.tuitionFee} + {inv.examFee + inv.vanFee}
                      </td>
                      <td className="py-3 px-4 font-bold text-slate-900">
                        {branding.currency} {inv.total.toLocaleString()}
                      </td>
                      <td className="py-3 px-4 font-mono text-[11px] text-slate-500">
                        {inv.dueDate}
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            inv.status === 'paid'
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                              : inv.status === 'overdue'
                              ? 'bg-rose-50 text-rose-700 border border-rose-200'
                              : 'bg-amber-50 text-amber-700 border border-amber-200'
                          }`}
                        >
                          {inv.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {/* Print Challan PDF */}
                          <button
                            onClick={() => openPrintModal('voucher', inv)}
                            className="p-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-700 transition"
                            title="Print 3-Part Bank Challan"
                          >
                            <Printer className="w-3.5 h-3.5" />
                          </button>

                          {/* Collect Fee Quick Button */}
                          {inv.status !== 'paid' && (
                            <button
                              onClick={() => openPrintModal('payment', inv)}
                              className="px-2 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[11px] flex items-center gap-1 shadow-xs transition"
                            >
                              <CreditCard className="w-3 h-3" />
                              <span>Collect</span>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* 2. FAMILY VOUCHERS TAB */}
      {activeFinanceTab === 'family' && (
        <div className="space-y-4">
          <div className="bg-amber-50 p-4 rounded-2xl border border-amber-200 text-xs text-amber-900 flex items-start gap-3">
            <Wallet className="w-5 h-5 text-amber-700 shrink-0 mt-0.5" />
            <div>
              <div className="font-bold">Combined Family Fee Challans (Siblings on One Slip)</div>
              <p className="text-amber-800 text-[11px] mt-0.5">
                Banks and parents appreciate paying for all children in one single transaction.
                Select a registered family below to preview and print the multi-child combined voucher.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {families.map(family => {
              const familyStudents = students.filter(s => s.familyId === family.id);
              const familyTotalFee = familyStudents.reduce((acc, s) => acc + s.monthlyFee, 0);

              return (
                <div key={family.id} className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs flex flex-col justify-between">
                  <div>
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-bold uppercase tracking-wider bg-slate-100 px-2 py-0.5 rounded text-slate-600 font-mono">
                        {family.familyCode || family.id}
                      </span>
                      <span className="text-xs font-bold text-indigo-600">
                        {familyStudents.length} Children
                      </span>
                    </div>

                    <h3 className="font-bold text-slate-900 text-sm mt-2">{family.name}</h3>
                    <div className="text-[11px] text-slate-500 font-mono mt-0.5">{family.phone}</div>

                    <div className="mt-3 pt-3 border-t border-slate-100 space-y-1">
                      {familyStudents.map(child => (
                        <div key={child.id} className="flex justify-between text-xs text-slate-700">
                          <span>{child.name} (Roll #{child.rollNo})</span>
                          <span className="font-bold">{branding.currency} {child.monthlyFee}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                    <div>
                      <div className="text-[10px] text-slate-400 uppercase font-bold">Total Family Dues</div>
                      <div className="text-base font-black text-emerald-700">
                        {branding.currency} {familyTotalFee.toLocaleString()}
                      </div>
                    </div>

                    <button
                      onClick={() => exportFamilyChallanPDF(family, familyStudents, branding)}
                      className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs flex items-center gap-1.5 shadow-xs transition"
                    >
                      <Printer className="w-3.5 h-3.5" />
                      <span>Print Family Slip</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 3. PAYMENT RECEIPTS TAB */}
      {activeFinanceTab === 'receipts' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm">Official Bank &amp; Cash Collection Receipts</h3>
            <span className="text-xs text-slate-500">{receipts.length} verified transactions</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3 px-4">Receipt #</th>
                  <th className="py-3 px-4">Date</th>
                  <th className="py-3 px-4">Student</th>
                  <th className="py-3 px-4">Amount Paid</th>
                  <th className="py-3 px-4">Channel</th>
                  <th className="py-3 px-4">Cashier</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
                {receipts.map(rc => (
                  <tr key={rc.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-4 font-mono font-bold text-indigo-700">{rc.receiptNo}</td>
                    <td className="py-3 px-4 font-mono text-slate-500">{rc.date}</td>
                    <td className="py-3 px-4 font-bold text-slate-900">{rc.studentName}</td>
                    <td className="py-3 px-4 font-bold text-emerald-700">
                      {branding.currency} {rc.amount.toLocaleString()}
                    </td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-semibold text-[10px] uppercase">
                        {rc.method}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-600">{rc.cashierName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 4. STAFF PAYROLL TAB */}
      {activeFinanceTab === 'payroll' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between">
            <div>
              <h3 className="font-bold text-slate-900 text-sm">Monthly Faculty &amp; Staff Payroll Register</h3>
              <p className="text-xs text-slate-500 mt-0.5">Automated net disbursement after tax, allowances and deductions.</p>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3 px-4">Staff Member</th>
                  <th className="py-3 px-4">Month</th>
                  <th className="py-3 px-4">Gross Salary</th>
                  <th className="py-3 px-4">Allowances</th>
                  <th className="py-3 px-4">Deductions</th>
                  <th className="py-3 px-4">Net Payable</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
                {payroll.map(pr => (
                  <tr key={pr.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-4 font-bold text-slate-900">{pr.staffName}</td>
                    <td className="py-3 px-4 font-semibold">{pr.month}</td>
                    <td className="py-3 px-4">{branding.currency} {pr.gross.toLocaleString()}</td>
                    <td className="py-3 px-4 text-emerald-600 font-medium">+{branding.currency} {pr.medicalAllowance + pr.houseRentAllowance}</td>
                    <td className="py-3 px-4 text-rose-600 font-medium">-{branding.currency} {pr.taxDeduction + pr.absenceDeduction}</td>
                    <td className="py-3 px-4 font-bold text-slate-900">
                      {branding.currency} {pr.net.toLocaleString()}
                    </td>
                    <td className="py-3 px-4">
                      <span
                        className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                          pr.status === 'Paid'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-amber-50 text-amber-700 border border-amber-200'
                        }`}
                      >
                        {pr.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right">
                      {pr.status !== 'Paid' && (
                        <button
                          onClick={() => markPayrollPaid(pr.id)}
                          className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[10px] shadow-xs transition"
                        >
                          Disburse
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 5. OPERATING EXPENSES TAB */}
      {activeFinanceTab === 'expenses' && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm">Operating &amp; Maintenance Expenses</h3>
            <span className="text-xs text-rose-600 font-bold">
              Total: {branding.currency} {totalExpenses.toLocaleString()}
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                  <th className="py-3 px-4">Date</th>
                  <th className="py-3 px-4">Title / Purpose</th>
                  <th className="py-3 px-4">Category</th>
                  <th className="py-3 px-4">Paid To</th>
                  <th className="py-3 px-4">Amount</th>
                  <th className="py-3 px-4 text-right">Delete</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
                {expenses.map(exp => (
                  <tr key={exp.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 px-4 font-mono text-slate-500">{exp.date}</td>
                    <td className="py-3 px-4 font-bold text-slate-900">{exp.title}</td>
                    <td className="py-3 px-4">
                      <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700 font-semibold text-[10px]">
                        {exp.category}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-600">{exp.paidTo}</td>
                    <td className="py-3 px-4 font-bold text-rose-600">
                      {branding.currency} {exp.amount.toLocaleString()}
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => deleteExpense(exp.id)}
                        className="p-1 text-slate-400 hover:text-rose-600 transition"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal: Generate Challan */}
      {isAddInvoiceOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-100 relative my-8 animate-in fade-in zoom-in-95">
            <button
              onClick={() => setIsAddInvoiceOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">Generate Monthly Fee Challan</h2>
            <p className="text-xs text-slate-500 mb-5">
              Select student and customize tuition and incidental charges.
            </p>

            <form onSubmit={handleCreateInvoice} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Student</label>
                <select
                  value={invoiceForm.studentId}
                  onChange={e => {
                    const st = students.find(s => s.id === e.target.value);
                    setInvoiceForm({
                      ...invoiceForm,
                      studentId: e.target.value,
                      tuitionFee: st ? st.monthlyFee : invoiceForm.tuitionFee
                    });
                  }}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {students.map(s => (
                    <option key={s.id} value={s.id}>
                      {s.name} (Roll #{s.rollNo})
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Fee Month</label>
                  <input
                    type="text"
                    value={invoiceForm.month}
                    onChange={e => setInvoiceForm({ ...invoiceForm, month: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Due Date</label>
                  <input
                    type="date"
                    value={invoiceForm.dueDate}
                    onChange={e => setInvoiceForm({ ...invoiceForm, dueDate: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Tuition Fee ({branding.currency})</label>
                  <input
                    type="number"
                    value={invoiceForm.tuitionFee}
                    onChange={e => setInvoiceForm({ ...invoiceForm, tuitionFee: Number(e.target.value) })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Exam Fee ({branding.currency})</label>
                  <input
                    type="number"
                    value={invoiceForm.examFee}
                    onChange={e => setInvoiceForm({ ...invoiceForm, examFee: Number(e.target.value) })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddInvoiceOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-md"
                >
                  Generate Invoice
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Record Expense */}
      {isAddExpenseOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-100 relative my-8 animate-in fade-in zoom-in-95">
            <button
              onClick={() => setIsAddExpenseOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">Record Campus Operating Expense</h2>
            <p className="text-xs text-slate-500 mb-5">
              Enter expense purpose, recipient, and payment voucher details.
            </p>

            <form onSubmit={handleCreateExpense} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Expense Purpose / Title *</label>
                <input
                  type="text"
                  required
                  value={expenseForm.title}
                  onChange={e => setExpenseForm({ ...expenseForm, title: e.target.value })}
                  placeholder="e.g. Science Laboratory Apparatus"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Category</label>
                  <select
                    value={expenseForm.category}
                    onChange={e => setExpenseForm({ ...expenseForm, category: e.target.value as any })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="Utilities & WiFi">Utilities &amp; WiFi</option>
                    <option value="Campus Maintenance">Campus Maintenance</option>
                    <option value="Stationery & Printing">Stationery &amp; Printing</option>
                    <option value="Sports & Events">Sports &amp; Events</option>
                    <option value="Fuel & Generator">Fuel &amp; Generator</option>
                    <option value="Hospitality">Hospitality</option>
                  </select>
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Amount ({branding.currency}) *</label>
                  <input
                    type="number"
                    required
                    value={expenseForm.amount}
                    onChange={e => setExpenseForm({ ...expenseForm, amount: Number(e.target.value) })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Paid To (Vendor / Person)</label>
                  <input
                    type="text"
                    value={expenseForm.paidTo}
                    onChange={e => setExpenseForm({ ...expenseForm, paidTo: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Payment Date</label>
                  <input
                    type="date"
                    value={expenseForm.date}
                    onChange={e => setExpenseForm({ ...expenseForm, date: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddExpenseOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-bold transition shadow-md"
                >
                  Save Expense
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Run Payroll */}
      {isPayrollModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-sm w-full p-6 shadow-2xl border border-slate-100 relative animate-in fade-in zoom-in-95 text-xs">
            <button
              onClick={() => setIsPayrollModalOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">Process Staff Payroll</h2>
            <p className="text-xs text-slate-500 mb-4">Generate salary slips for all staff members.</p>

            <div className="space-y-3 mb-5">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Billing Month</label>
                <input
                  type="text"
                  value={payrollMonth}
                  onChange={e => setPayrollMonth(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-semibold"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setIsPayrollModalOpen(false)}
                className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => {
                  runPayrollForMonth(payrollMonth);
                  setIsPayrollModalOpen(false);
                }}
                className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-md"
              >
                Process Payroll
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
