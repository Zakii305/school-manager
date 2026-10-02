import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { ClassItem, SubjectItem } from '../../types';
import {
  Layers, Plus, Trash2, Edit3, BookOpen,
  X
} from 'lucide-react';

export const ClassesManager: React.FC = () => {
  const { classes, subjects, staff, addClass, updateClass, deleteClass, addSubject, updateSubject, deleteSubject, branding } = useSchool();
  const [selectedClassId, setSelectedClassId] = useState<string>(classes[0]?.id || '');

  // Class modal
  const [isClassModalOpen, setIsClassModalOpen] = useState(false);
  const [editingClassId, setEditingClassId] = useState<string | null>(null);
  const [className, setClassName] = useState('');
  const [classSection, setClassSection] = useState('A');
  const [classTeacherId, setClassTeacherId] = useState(staff[0]?.id || '');

  // Subject modal
  const [isSubjectModalOpen, setIsSubjectModalOpen] = useState(false);
  const [editingSubjectId, setEditingSubjectId] = useState<string | null>(null);
  const [subjectName, setSubjectName] = useState('');
  const [subjectCode, setSubjectCode] = useState('');
  const [subjectTeacherId, setSubjectTeacherId] = useState(staff[0]?.id || '');

  const handleOpenAddClass = () => {
    setEditingClassId(null);
    setClassName('');
    setClassSection('A');
    setClassTeacherId(staff[0]?.id || '');
    setIsClassModalOpen(true);
  };

  const handleOpenEditClass = (c: ClassItem) => {
    setEditingClassId(c.id);
    setClassName(c.name);
    setClassSection(c.section);
    setClassTeacherId(c.classTeacherId || staff[0]?.id || '');
    setIsClassModalOpen(true);
  };

  const handleSaveClass = (e: React.FormEvent) => {
    e.preventDefault();
    if (!className) return;
    const teacher = staff.find(s => s.id === classTeacherId);

    if (editingClassId) {
      updateClass(editingClassId, {
        name: className,
        section: classSection,
        sections: [classSection],
        classTeacherId,
        classTeacherName: teacher?.name || 'Assigned Teacher'
      });
    } else {
      addClass({
        name: className,
        section: classSection,
        sections: [classSection],
        classTeacherId,
        classTeacherName: teacher?.name || 'Assigned Teacher'
      });
    }
    setIsClassModalOpen(false);
  };

  const handleOpenAddSubject = () => {
    setEditingSubjectId(null);
    setSubjectName('');
    setSubjectCode('');
    setSubjectTeacherId(staff[0]?.id || '');
    setIsSubjectModalOpen(true);
  };

  const handleOpenEditSubject = (sub: SubjectItem) => {
    setEditingSubjectId(sub.id);
    setSubjectName(sub.name);
    setSubjectCode(sub.code || '');
    setSubjectTeacherId(sub.teacherId || staff[0]?.id || '');
    setIsSubjectModalOpen(true);
  };

  const handleSaveSubject = (e: React.FormEvent) => {
    e.preventDefault();
    if (!subjectName || !selectedClassId) return;
    const cls = classes.find(c => c.id === selectedClassId);
    const teacher = staff.find(s => s.id === subjectTeacherId);

    if (editingSubjectId) {
      updateSubject(editingSubjectId, {
        name: subjectName,
        code: subjectCode || `SUB-${Math.floor(100 + Math.random() * 900)}`,
        teacherId: subjectTeacherId,
        teacherName: teacher?.name || "Assigned Teacher"
      });
    } else {
      addSubject({
        name: subjectName,
        code: subjectCode || `SUB-${Math.floor(100 + Math.random() * 900)}`,
        classId: selectedClassId,
        className: cls ? `${cls.name} - ${cls.section}` : "Class",
        teacherId: subjectTeacherId,
        teacherName: teacher?.name || "Assigned Teacher"
      });
    }
    setIsSubjectModalOpen(false);
  };

  const selectedClass = classes.find(c => c.id === selectedClassId) || classes[0];
  const classSubjects = subjects.filter(s => s.classId === selectedClass?.id);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <Layers className="w-5 h-5 text-indigo-600" />
            <span>Classes & Academic Subjects</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Classes, sections, assigned class teachers, and subject curriculum for {branding.name}.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleOpenAddClass}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold shadow-xs transition"
          >
            <Plus className="w-4 h-4" />
            <span>Add Class</span>
          </button>
          <button
            onClick={handleOpenAddSubject}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-bold border border-slate-300 transition"
          >
            <BookOpen className="w-4 h-4 text-indigo-600" />
            <span>Add Subject</span>
          </button>
        </div>
      </div>

      {/* Two-Column: Classes on Left, Class Detail & Subjects on Right */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Classes List */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-4 flex flex-col">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-3">
            <div className="text-xs font-bold uppercase text-slate-500 tracking-wider">
              Enrolled Classes ({classes.length})
            </div>
            <button
              onClick={handleOpenAddClass}
              className="text-xs text-indigo-600 font-bold hover:underline"
            >
              + New
            </button>
          </div>

          <div className="space-y-1.5 overflow-y-auto max-h-[550px] pr-1">
            {classes.map(c => {
              const isSelected = selectedClass?.id === c.id;
              const subCount = subjects.filter(s => s.classId === c.id).length;
              return (
                <div
                  key={c.id}
                  onClick={() => setSelectedClassId(c.id)}
                  className={`p-3 rounded-xl cursor-pointer border text-xs transition flex items-center justify-between ${
                    isSelected
                      ? 'bg-indigo-50/80 border-indigo-300 text-indigo-950 font-semibold shadow-xs'
                      : 'border-slate-100 hover:bg-slate-50 text-slate-700'
                  }`}
                >
                  <div>
                    <div className="font-bold text-sm leading-tight text-slate-900">
                      {c.name} - Section {c.section}
                    </div>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      Incharge: {c.classTeacherName || 'Not Assigned'}
                    </div>
                  </div>

                  <div className="flex items-center gap-1.5 text-right">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-600">
                      {subCount} Subj
                    </span>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenEditClass(c);
                      }}
                      className="p-1 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded"
                      title="Edit Class"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        if (confirm(`Delete class ${c.name}?`)) deleteClass(c.id);
                      }}
                      className="p-1 text-slate-400 hover:text-rose-600 rounded"
                      title="Delete Class"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Selected Class Subjects & Teachers */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-5 flex flex-col justify-between">
          <div>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-4 mb-4">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                  Class Curriculum
                </span>
                <h2 className="text-lg font-bold text-slate-900 mt-1">
                  {selectedClass ? `${selectedClass.name} - Section ${selectedClass.section}` : "Select Class"}
                </h2>
                <p className="text-xs text-slate-500">
                  Class Incharge: <strong className="text-slate-800">{selectedClass?.classTeacherName}</strong>
                </p>
              </div>

              <button
                onClick={handleOpenAddSubject}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-bold transition shadow-xs self-start sm:self-auto"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Subject to Class</span>
              </button>
            </div>

            {/* Subjects Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] tracking-wider">
                  <tr>
                    <th className="py-2.5 px-3">Subject Name</th>
                    <th className="py-2.5 px-3">Subject Code</th>
                    <th className="py-2.5 px-3">Assigned Faculty</th>
                    <th className="py-2.5 px-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {classSubjects.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="py-8 text-center text-slate-400">
                        No subjects allocated for this class yet. Click &ldquo;Add Subject to Class&rdquo;.
                      </td>
                    </tr>
                  ) : (
                    classSubjects.map(sub => (
                      <tr key={sub.id} className="hover:bg-slate-50/80 transition">
                        <td className="py-3 px-3 font-bold text-slate-900">
                          {sub.name}
                        </td>
                        <td className="py-3 px-3 font-mono text-slate-500 font-semibold">
                          {sub.code || "—"}
                        </td>
                        <td className="py-3 px-3 text-slate-700 font-medium">
                          {sub.teacherName || "—"}
                        </td>
                        <td className="py-3 px-3 text-right">
                          <div className="flex items-center justify-end gap-1">
                            <button
                              onClick={() => handleOpenEditSubject(sub)}
                              className="p-1 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded"
                              title="Edit Subject"
                            >
                              <Edit3 className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => {
                                if (confirm(`Remove subject ${sub.name}?`)) deleteSubject(sub.id);
                              }}
                              className="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded"
                              title="Delete Subject"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Class Modal */}
      {isClassModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingClassId ? "Edit Class & Section" : "Add New Class & Section"}</h2>
              <button onClick={() => setIsClassModalOpen(false)}>
                <X className="w-5 h-5 text-slate-300 hover:text-white" />
              </button>
            </div>
            <form onSubmit={handleSaveClass} className="p-5 space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Class Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Class 6, Nursery, Grade 10"
                  value={className}
                  onChange={e => setClassName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Section</label>
                <input
                  type="text"
                  placeholder="e.g. A, B, Yellow, Science"
                  value={classSection}
                  onChange={e => setClassSection(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Assigned Class Teacher</label>
                <select
                  value={classTeacherId}
                  onChange={e => setClassTeacherId(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                >
                  {staff.map(s => (
                    <option key={s.id} value={s.id}>{s.name} ({s.designation})</option>
                  ))}
                </select>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsClassModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition"
                >
                  {editingClassId ? "Save Changes" : "Save Class"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Subject Modal */}
      {isSubjectModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">{editingSubjectId ? "Edit Subject Details" : `Add Subject to ${selectedClass?.name}`}</h2>
              <button onClick={() => setIsSubjectModalOpen(false)}>
                <X className="w-5 h-5 text-slate-300 hover:text-white" />
              </button>
            </div>
            <form onSubmit={handleSaveSubject} className="p-5 space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Subject Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Physics, Islamiat, English Comprehension"
                  value={subjectName}
                  onChange={e => setSubjectName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Subject Code</label>
                <input
                  type="text"
                  placeholder="e.g. PHY-101"
                  value={subjectCode}
                  onChange={e => setSubjectCode(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Assigned Subject Teacher</label>
                <select
                  value={subjectTeacherId}
                  onChange={e => setSubjectTeacherId(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                >
                  {staff.map(s => (
                    <option key={s.id} value={s.id}>{s.name} ({s.designation})</option>
                  ))}
                </select>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsSubjectModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition"
                >
                  {editingSubjectId ? "Save Changes" : "Add Subject"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
