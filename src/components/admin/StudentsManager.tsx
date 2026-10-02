import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { Student } from '../../types';
import {
  Users, Plus, Search, Trash2, Edit3, Award,
  DollarSign, X, CheckCircle, AlertCircle, Phone,
  Mail, Calendar, Building, Filter
} from 'lucide-react';
import { exportStudentIDCardPDF } from '../../utils/pdfExport';

export const StudentsManager: React.FC = () => {
  const {
    students, addStudent, updateStudent, deleteStudent,
    classes, families, openPrintModal, branding, invoices
  } = useSchool();

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClassFilter, setSelectedClassFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'left'>('all');
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingStudent, setEditingStudent] = useState<Student | null>(null);

  // Form State
  const [formData, setFormData] = useState({
    name: '',
    fatherName: '',
    rollNo: '',
    admissionNo: `ADM-2026-${Math.floor(100 + Math.random() * 900)}`,
    classId: classes[0]?.id || 'c-4',
    dob: '2014-05-12',
    gender: 'male' as 'male' | 'female' | 'other',
    fatherContact: '',
    address: '',
    monthlyFee: 3500,
    familyId: '',
    status: 'active' as Student['status']
  });

  const resetForm = () => {
    setFormData({
      name: '',
      fatherName: '',
      rollNo: '',
      admissionNo: `ADM-2026-${Math.floor(100 + Math.random() * 900)}`,
      classId: classes[0]?.id || 'c-4',
      dob: '2014-05-12',
      gender: 'male',
      fatherContact: '',
      address: '',
      monthlyFee: 3500,
      familyId: '',
      status: 'active'
    });
    setEditingStudent(null);
  };

  const handleOpenAdd = () => {
    resetForm();
    const maxRoll = students.reduce((acc, s) => {
      const num = parseInt(s.rollNo, 10);
      return !isNaN(num) && num > acc ? num : acc;
    }, 0);
    setFormData(prev => ({
      ...prev,
      rollNo: String(maxRoll + 1).padStart(2, '0')
    }));
    setIsAddModalOpen(true);
  };

  const handleOpenEdit = (student: Student) => {
    setEditingStudent(student);
    setFormData({
      name: student.name,
      fatherName: student.fatherName,
      rollNo: student.rollNo,
      admissionNo: student.admissionNo,
      classId: student.classId,
      dob: student.dob || '2014-05-12',
      gender: student.gender,
      fatherContact: student.fatherContact || '',
      address: student.address || '',
      monthlyFee: student.monthlyFee,
      familyId: student.familyId || '',
      status: student.status
    });
    setIsAddModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.fatherName || !formData.rollNo) {
      alert("Please fill in Student Name, Father's Name, and Roll Number.");
      return;
    }

    const currentClass = classes.find(c => c.id === formData.classId);
    const className = currentClass ? `${currentClass.name} - ${currentClass.section}` : 'Class 4 - A';

    if (editingStudent) {
      updateStudent(editingStudent.id, {
        name: formData.name,
        fatherName: formData.fatherName,
        rollNo: formData.rollNo,
        admissionNo: formData.admissionNo,
        classId: formData.classId,
        className,
        dob: formData.dob,
        gender: formData.gender,
        fatherContact: formData.fatherContact,
        address: formData.address,
        monthlyFee: formData.monthlyFee,
        status: formData.status,
        familyId: formData.familyId || undefined
      });
    } else {
      addStudent({
        name: formData.name,
        fatherName: formData.fatherName,
        rollNo: formData.rollNo,
        admissionNo: formData.admissionNo,
        classId: formData.classId,
        className,
        sessionLabel: branding.session,
        admissionDate: new Date().toISOString().split('T')[0],
        dob: formData.dob,
        gender: formData.gender,
        fatherContact: formData.fatherContact,
        address: formData.address,
        monthlyFee: formData.monthlyFee,
        vanFee: 0,
        admissionFee: 0,
        examFee: 500,
        nationality: 'Pakistani',
        hafizEQuran: 'No',
        status: formData.status,
        familyId: formData.familyId || undefined
      });
    }

    setIsAddModalOpen(false);
    resetForm();
  };

  const handleDelete = (id: string, name: string) => {
    if (confirm(`Are you sure you want to remove student "${name}"?`)) {
      deleteStudent(id);
    }
  };

  // Filtered Students
  const filteredStudents = students.filter(student => {
    const matchesSearch =
      student.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      student.fatherName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      student.rollNo.includes(searchTerm) ||
      (student.fatherContact && student.fatherContact.includes(searchTerm));

    const matchesClass = selectedClassFilter === 'all' || student.classId === selectedClassFilter;
    const matchesStatus = statusFilter === 'all' || student.status === statusFilter;

    return matchesSearch && matchesClass && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900">Students &amp; Admissions</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {students.length} Total Enrolled
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Manage admissions, edit student profiles, link siblings, and generate ID cards &amp; challans.
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-md shadow-indigo-600/20 transition"
        >
          <Plus className="w-4 h-4" />
          <span>New Student Admission</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search by name, father, roll no, or phone..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-500"
          />
        </div>

        <div>
          <select
            value={selectedClassFilter}
            onChange={e => setSelectedClassFilter(e.target.value)}
            className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-800 focus:outline-none focus:border-indigo-500"
          >
            <option value="all">All Classes &amp; Sections</option>
            {classes.map(c => (
              <option key={c.id} value={c.id}>
                {c.name} - Section {c.section}
              </option>
            ))}
          </select>
        </div>

        <div>
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value as any)}
            className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white text-xs text-slate-800 focus:outline-none focus:border-indigo-500"
          >
            <option value="all">All Statuses</option>
            <option value="active">Active Enrolled</option>
            <option value="left">Inactive / Left</option>
          </select>
        </div>
      </div>

      {/* Students Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider text-[10px]">
                <th className="py-3 px-4">Roll</th>
                <th className="py-3 px-4">Student &amp; Guardian</th>
                <th className="py-3 px-4">Class &amp; Section</th>
                <th className="py-3 px-4">Contact</th>
                <th className="py-3 px-4">Monthly Fee</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-700">
              {filteredStudents.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-8 text-center text-slate-400">
                    No students found matching your criteria.
                  </td>
                </tr>
              ) : (
                filteredStudents.map(student => {
                  const studentClass = classes.find(c => c.id === student.classId);
                  const studentInvoice = invoices.find(i => i.studentId === student.id);

                  return (
                    <tr key={student.id} className="hover:bg-slate-50/80 transition">
                      <td className="py-3 px-4 font-mono font-bold text-indigo-700">
                        #{student.rollNo}
                      </td>
                      <td className="py-3 px-4">
                        <div className="font-bold text-slate-900">{student.name}</div>
                        <div className="text-[11px] text-slate-500">
                          S/O or D/O: {student.fatherName}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-800 font-semibold text-[11px]">
                          {studentClass ? `${studentClass.name} (${studentClass.section})` : student.className}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-mono text-[11px] text-slate-600">
                        {student.fatherContact || 'N/A'}
                      </td>
                      <td className="py-3 px-4 font-bold text-emerald-700">
                        {branding.currency} {student.monthlyFee.toLocaleString()}
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            student.status === 'active'
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                              : 'bg-rose-50 text-rose-700 border border-rose-200'
                          }`}
                        >
                          {student.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          {/* ID Card Button */}
                          <button
                            onClick={() => openPrintModal('idcard', student)}
                            className="p-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-700 transition"
                            title="Generate Student ID Card"
                          >
                            <Award className="w-3.5 h-3.5" />
                          </button>

                          {/* Fee Voucher Button */}
                          {studentInvoice && (
                            <button
                              onClick={() => openPrintModal('voucher', studentInvoice)}
                              className="p-1.5 rounded-lg bg-amber-50 hover:bg-amber-100 text-amber-700 transition"
                              title="Print Fee Challan / Voucher"
                            >
                              <DollarSign className="w-3.5 h-3.5" />
                            </button>
                          )}

                          {/* Edit Button */}
                          <button
                            onClick={() => handleOpenEdit(student)}
                            className="p-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 transition"
                            title="Edit Student Profile"
                          >
                            <Edit3 className="w-3.5 h-3.5" />
                          </button>

                          {/* Delete Button */}
                          <button
                            onClick={() => handleDelete(student.id, student.name)}
                            className="p-1.5 rounded-lg bg-rose-50 hover:bg-rose-100 text-rose-600 transition"
                            title="Delete Student"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Admission / Edit Modal */}
      {isAddModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-3xl max-w-xl w-full p-6 shadow-2xl border border-slate-100 relative my-8 animate-in fade-in zoom-in-95">
            <button
              onClick={() => setIsAddModalOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">
              {editingStudent ? 'Edit Student Profile' : 'New Student Admission'}
            </h2>
            <p className="text-xs text-slate-500 mb-5">
              Enter academic credentials, family linkage, and tuition fee details.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4 text-xs">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Student Full Name *</label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={e => setFormData({ ...formData, name: e.target.value })}
                    placeholder="e.g. Zain Raza"
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Father / Guardian Name *</label>
                  <input
                    type="text"
                    required
                    value={formData.fatherName}
                    onChange={e => setFormData({ ...formData, fatherName: e.target.value })}
                    placeholder="e.g. Muhammad Raza"
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Roll Number *</label>
                  <input
                    type="text"
                    required
                    value={formData.rollNo}
                    onChange={e => setFormData({ ...formData, rollNo: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Class &amp; Section</label>
                  <select
                    value={formData.classId}
                    onChange={e => setFormData({ ...formData, classId: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  >
                    {classes.map(c => (
                      <option key={c.id} value={c.id}>
                        {c.name} ({c.section})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Gender</label>
                  <select
                    value={formData.gender}
                    onChange={e => setFormData({ ...formData, gender: e.target.value as any })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Father Mobile Number</label>
                  <input
                    type="tel"
                    value={formData.fatherContact}
                    onChange={e => setFormData({ ...formData, fatherContact: e.target.value })}
                    placeholder="0300-1234567"
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Admission Number</label>
                  <input
                    type="text"
                    value={formData.admissionNo}
                    onChange={e => setFormData({ ...formData, admissionNo: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500 font-mono"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-slate-700 mb-1">Monthly Tuition Fee ({branding.currency})</label>
                  <input
                    type="number"
                    value={formData.monthlyFee}
                    onChange={e => setFormData({ ...formData, monthlyFee: Number(e.target.value) })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  />
                </div>

                <div>
                  <label className="block font-bold text-slate-700 mb-1">Family (For Sibling Discount)</label>
                  <select
                    value={formData.familyId}
                    onChange={e => setFormData({ ...formData, familyId: e.target.value })}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                  >
                    <option value="">-- No Family Linked --</option>
                    {families.map(f => (
                      <option key={f.id} value={f.id}>
                        {f.name} ({f.phone})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Home Address</label>
                <input
                  type="text"
                  value={formData.address}
                  onChange={e => setFormData({ ...formData, address: e.target.value })}
                  placeholder="Street, City, Postal Code"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-md shadow-indigo-600/20"
                >
                  {editingStudent ? 'Save Changes' : 'Confirm Admission'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
