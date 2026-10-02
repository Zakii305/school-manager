import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { Family } from '../../types';
import {
  Search, Plus, UserSquare2, Phone, Home,
  X, AlertCircle, Edit3, Trash2
} from 'lucide-react';

export const FamiliesManager: React.FC = () => {
  const { families, students, invoices, addFamily, updateFamily, deleteFamily } = useSchool();
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFamily, setEditingFamily] = useState<Family | null>(null);
  const [formData, setFormData] = useState<Partial<Family>>({});

  // Compute stats per family
  const familyStats = families.map(f => {
    const familyStudents = students.filter(s => s.familyId === f.id || f.childrenIds?.includes(s.id));
    const studentIds = new Set(familyStudents.map(s => s.id));
    const familyInvoices = invoices.filter(inv => studentIds.has(inv.studentId));
    const pendingFee = familyInvoices.reduce((sum, inv) => sum + (inv.total - inv.paidAmount), 0);

    return {
      ...f,
      enrolledStudents: familyStudents,
      childrenCount: familyStudents.length,
      pendingFee
    };
  });

  const filteredFamilies = familyStats.filter(f =>
    f.name.toLowerCase().includes(search.toLowerCase()) ||
    f.fatherName.toLowerCase().includes(search.toLowerCase()) ||
    f.phone.includes(search) ||
    f.fatherCnic.includes(search) ||
    f.familyCode.toLowerCase().includes(search.toLowerCase())
  );

  const handleOpenAdd = () => {
    setEditingFamily(null);
    setFormData({
      familyCode: `FAM-${100 + families.length + 1}`,
      name: "",
      fatherName: "",
      fatherCnic: "",
      phone: "0300-",
      address: "",
      siblingDiscountPercent: 10,
      notes: ""
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (fam: Family) => {
    setEditingFamily(fam);
    setFormData({ ...fam });
    setIsModalOpen(true);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.fatherName) {
      alert("Please provide Family Name and Father Name");
      return;
    }

    if (editingFamily) {
      updateFamily(editingFamily.id, formData);
    } else {
      addFamily({
        familyCode: formData.familyCode || `FAM-${Date.now()}`,
        name: formData.name || "",
        fatherName: formData.fatherName || "",
        fatherCnic: formData.fatherCnic || "",
        phone: formData.phone || "",
        address: formData.address || "",
        siblingDiscountPercent: Number(formData.siblingDiscountPercent) || 10,
        notes: formData.notes || "",
        childrenIds: []
      });
    }
    setIsModalOpen(false);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <UserSquare2 className="w-5 h-5 text-indigo-600" />
            <span>Families & Siblings Management</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Group sibling students for joint invoices, sibling discounts, and consolidated parent communications.
          </p>
        </div>

        <button
          onClick={handleOpenAdd}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold shadow-xs transition"
        >
          <Plus className="w-4 h-4" />
          <span>Add New Family</span>
        </button>
      </div>

      {/* Info Notice */}
      <div className="bg-indigo-50 border border-indigo-200/80 rounded-xl p-3.5 flex items-start gap-3 text-xs text-indigo-900">
        <AlertCircle className="w-4 h-4 text-indigo-600 shrink-0 mt-0.5" />
        <div>
          <span className="font-bold">How it works:</span> Once a family is registered, assign it to each child during admission or profile edit. All sibling fee vouchers will automatically group under the <strong>Family Fee Collection</strong> tab for one-click payment!
        </div>
      </div>

      {/* Search */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between gap-4">
        <div className="flex-1 max-w-md relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Search families by name, father name, phone, CNIC, code..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 text-xs rounded-lg border border-slate-200 bg-slate-50 focus:outline-indigo-500 text-slate-800"
          />
        </div>
        <div className="text-xs text-slate-500 font-medium">
          Showing <span className="font-bold text-slate-900">{filteredFamilies.length}</span> families
        </div>
      </div>

      {/* Family Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredFamilies.map(fam => (
          <div
            key={fam.id}
            className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 hover:border-indigo-300 transition flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between gap-2 border-b border-slate-100 pb-3">
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-sm text-slate-900 leading-snug">{fam.name}</h3>
                    <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-slate-100 text-indigo-700">
                      {fam.familyCode}
                    </span>
                  </div>
                  <div className="text-xs text-slate-500 font-medium mt-0.5">Father: {fam.fatherName}</div>
                </div>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 border border-indigo-200">
                  {fam.childrenCount} {fam.childrenCount === 1 ? 'Child' : 'Children'}
                </span>
              </div>

              <div className="py-3 space-y-2 text-xs text-slate-600">
                <div className="flex items-center gap-2">
                  <Phone className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                  <span>{fam.phone}</span>
                </div>
                {fam.fatherCnic && (
                  <div className="text-slate-500 font-mono text-[11px]">
                    CNIC: {fam.fatherCnic}
                  </div>
                )}
                {fam.address && (
                  <div className="flex items-start gap-2 text-slate-500 text-[11px] line-clamp-1">
                    <Home className="w-3.5 h-3.5 text-slate-400 shrink-0 mt-0.5" />
                    <span>{fam.address}</span>
                  </div>
                )}
              </div>

              {/* Children Enrolled List */}
              <div className="bg-slate-50 rounded-xl p-3 border border-slate-100 mb-3 space-y-1.5">
                <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                  Enrolled Siblings
                </div>
                {fam.enrolledStudents.length === 0 ? (
                  <div className="text-[11px] text-slate-400 italic">No students linked yet</div>
                ) : (
                  fam.enrolledStudents.map(st => (
                    <div key={st.id} className="flex items-center justify-between text-xs">
                      <span className="font-semibold text-slate-800">{st.name}</span>
                      <span className="text-[11px] text-slate-500">{st.className}</span>
                    </div>
                  ))
                )}
              </div>
            </div>

            {/* Bottom: Pending fee & Action */}
            <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
              <div>
                <div className="text-[10px] uppercase font-bold text-slate-400">Total Pending Dues</div>
                <div className={`text-sm font-extrabold ${fam.pendingFee > 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                  Rs {fam.pendingFee.toLocaleString()}
                </div>
              </div>

              <div className="flex items-center gap-1.5">
                <button
                  onClick={() => handleOpenEdit(fam)}
                  className="p-1.5 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                  title="Edit Family"
                >
                  <Edit3 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => {
                    if (confirm(`Delete family ${fam.name}?`)) deleteFamily(fam.id);
                  }}
                  className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                  title="Delete Family"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">
                {editingFamily ? "Edit Family Record" : "Add Family Record"}
              </h2>
              <button onClick={() => setIsModalOpen(false)}>
                <X className="w-5 h-5 text-slate-300 hover:text-white" />
              </button>
            </div>
            <form onSubmit={handleSave} className="p-5 space-y-3.5 text-xs">
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Family Code *</label>
                  <input
                    type="text"
                    required
                    value={formData.familyCode || ''}
                    onChange={e => setFormData({ ...formData, familyCode: e.target.value })}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono font-bold"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Sibling Discount %</label>
                  <input
                    type="number"
                    value={formData.siblingDiscountPercent || 10}
                    onChange={e => setFormData({ ...formData, siblingDiscountPercent: Number(e.target.value) })}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Family Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Raza Family"
                  value={formData.name || ''}
                  onChange={e => setFormData({ ...formData, name: e.target.value })}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Father Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Muhammad Raza"
                  value={formData.fatherName || ''}
                  onChange={e => setFormData({ ...formData, fatherName: e.target.value })}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Father CNIC</label>
                  <input
                    type="text"
                    placeholder="35401-XXXXXXX-X"
                    value={formData.fatherCnic || ''}
                    onChange={e => setFormData({ ...formData, fatherCnic: e.target.value })}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Primary Phone</label>
                  <input
                    type="text"
                    placeholder="0300-XXXXXXX"
                    value={formData.phone || ''}
                    onChange={e => setFormData({ ...formData, phone: e.target.value })}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Residential Address</label>
                <input
                  type="text"
                  placeholder="House, Street, Area, City"
                  value={formData.address || ''}
                  onChange={e => setFormData({ ...formData, address: e.target.value })}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>

              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  Save Family
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
