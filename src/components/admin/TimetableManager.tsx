import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { TimetableSlot, PeriodTiming } from '../../types';
import {
  Clock, Plus, Calendar, Download, Edit3, Trash2,
  Copy, Layers, BookOpen, Users, Check, X
} from 'lucide-react';
import { exportClassTimetablePDF, exportTeacherTimetablePDF } from '../../utils/pdfExport';

export const TimetableManager: React.FC = () => {
  const {
    classes, subjects, staff, timetable, periodTimings,
    updateTimetableSlot, copyTimetableDay, branding
  } = useSchool();

  const [selectedClassId, setSelectedClassId] = useState(classes[0]?.id || 'c-4');
  const [selectedDay, setSelectedDay] = useState('Monday');
  const [editingSlot, setEditingSlot] = useState<{ day: string; period: number; slot?: TimetableSlot } | null>(null);
  const [slotSubject, setSlotSubject] = useState('');
  const [slotTeacher, setSlotTeacher] = useState('');

  // Copy day state
  const [isCopyModalOpen, setIsCopyModalOpen] = useState(false);
  const [targetDays, setTargetDays] = useState<string[]>(['Tuesday', 'Wednesday', 'Thursday']);

  const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const activeClass = classes.find(c => c.id === selectedClassId) || classes[0];

  const handleOpenEditSlot = (day: string, period: number) => {
    const existing = timetable.find(
      t => t.classId === selectedClassId && t.day === day && t.period === period
    );
    setEditingSlot({ day, period, slot: existing });
    setSlotSubject(existing?.subject || subjects[0]?.name || 'English');
    setSlotTeacher(existing?.teacher || staff[0]?.name || 'Ms. Fatima Noor');
  };

  const handleSaveSlot = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingSlot) return;

    updateTimetableSlot({
      classId: selectedClassId,
      day: editingSlot.day,
      period: editingSlot.period,
      subject: slotSubject,
      teacher: slotTeacher
    });

    setEditingSlot(null);
  };

  const handleExecuteCopy = () => {
    if (targetDays.length === 0) return;
    copyTimetableDay(selectedClassId, selectedDay, targetDays);
    setIsCopyModalOpen(false);
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900">Master Class Timetable</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {activeClass ? `${activeClass.name} - ${activeClass.section}` : 'General'}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Build and optimize class schedules, assign periods, copy across days, and print PDF cards.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => setIsCopyModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold rounded-xl text-xs transition"
          >
            <Copy className="w-3.5 h-3.5" />
            <span>Duplicate Day Schedule</span>
          </button>

          <button
            onClick={() => exportClassTimetablePDF(activeClass, timetable, periodTimings, branding)}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl text-xs shadow-md transition"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Class Timetable PDF</span>
          </button>
        </div>
      </div>

      {/* Class Selector Bar */}
      <div className="flex items-center gap-2 overflow-x-auto p-1.5 bg-white rounded-2xl border border-slate-200 shadow-xs text-xs">
        <span className="text-slate-400 font-bold uppercase tracking-wider text-[10px] px-2 shrink-0">
          Select Class:
        </span>
        {classes.map(c => (
          <button
            key={c.id}
            onClick={() => setSelectedClassId(c.id)}
            className={`px-3 py-1.5 rounded-xl font-bold transition whitespace-nowrap ${
              selectedClassId === c.id
                ? 'bg-indigo-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            {c.name} ({c.section})
          </button>
        ))}
      </div>

      {/* Timetable Grid */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-900 text-white font-bold text-[11px]">
                <th className="py-3 px-4 w-28">Day / Period</th>
                {periodTimings.map(p => (
                  <th key={p.num} className="py-3 px-3 min-w-[120px] text-center border-l border-slate-800">
                    <div>Period {p.num}</div>
                    <div className="text-[10px] text-indigo-300 font-normal">{p.time}</div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {daysOfWeek.map(day => (
                <tr key={day} className="hover:bg-slate-50/60 transition">
                  <td className="py-3.5 px-4 font-bold text-slate-900 bg-slate-50/50">
                    {day}
                  </td>

                  {periodTimings.map(period => {
                    const slot = timetable.find(
                      t => t.classId === selectedClassId && t.day === day && t.period === period.num
                    );

                    return (
                      <td
                        key={period.num}
                        className="py-2 px-2 border-l border-slate-100 text-center relative group"
                      >
                        {slot ? (
                          <div
                            onClick={() => handleOpenEditSlot(day, period.num)}
                            className="p-2 rounded-xl bg-indigo-50/70 border border-indigo-100 hover:border-indigo-300 cursor-pointer transition text-left"
                          >
                            <div className="font-bold text-indigo-900 text-[11px] truncate">
                              {slot.subject}
                            </div>
                            <div className="text-[10px] text-slate-600 truncate mt-0.5">
                              {slot.teacher}
                            </div>
                          </div>
                        ) : (
                          <button
                            onClick={() => handleOpenEditSlot(day, period.num)}
                            className="w-full py-3.5 rounded-xl border border-dashed border-slate-200 text-slate-400 hover:border-indigo-400 hover:text-indigo-600 text-[10px] font-semibold transition"
                          >
                            + Assign
                          </button>
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit Slot Modal */}
      {editingSlot && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-100 relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setEditingSlot(null)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">
              Configure Slot: {editingSlot.day} (Period {editingSlot.period})
            </h2>
            <p className="text-xs text-slate-500 mb-5">
              Assign subject and faculty instructor for this slot.
            </p>

            <form onSubmit={handleSaveSlot} className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">Subject</label>
                <select
                  value={slotSubject}
                  onChange={e => setSlotSubject(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {subjects.map(s => (
                    <option key={s.id} value={s.name}>
                      {s.name} ({s.code || s.className})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">Teacher / Instructor</label>
                <select
                  value={slotTeacher}
                  onChange={e => setSlotTeacher(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-indigo-500"
                >
                  {staff.map(s => (
                    <option key={s.id} value={s.name}>
                      {s.name} - {s.designation}
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setEditingSlot(null)}
                  className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-md"
                >
                  Save Slot
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Copy Day Modal */}
      {isCopyModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-100 relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setIsCopyModalOpen(false)}
              className="absolute top-5 right-5 p-2 rounded-full hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-lg font-bold text-slate-900 mb-1">Duplicate Day Schedule</h2>
            <p className="text-xs text-slate-500 mb-4">
              Copy timetable from <strong className="text-slate-900">{selectedDay}</strong> to other days.
            </p>

            <div className="space-y-2 mb-5">
              <label className="block font-bold text-slate-700 text-xs mb-1">Source Day:</label>
              <select
                value={selectedDay}
                onChange={e => setSelectedDay(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-indigo-500"
              >
                {daysOfWeek.map(d => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </select>

              <label className="block font-bold text-slate-700 text-xs mt-3 mb-1">Apply To Target Days:</label>
              <div className="grid grid-cols-2 gap-2 text-xs">
                {daysOfWeek.filter(d => d !== selectedDay).map(day => (
                  <label
                    key={day}
                    className={`flex items-center gap-2 p-2.5 rounded-xl border cursor-pointer transition ${
                      targetDays.includes(day)
                        ? 'border-indigo-600 bg-indigo-50 text-indigo-900 font-bold'
                        : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={targetDays.includes(day)}
                      onChange={e => {
                        if (e.target.checked) setTargetDays([...targetDays, day]);
                        else setTargetDays(targetDays.filter(d => d !== day));
                      }}
                      className="rounded text-indigo-600"
                    />
                    <span>{day}</span>
                  </label>
                ))}
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100 text-xs">
              <button
                type="button"
                onClick={() => setIsCopyModalOpen(false)}
                className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 font-bold hover:bg-slate-50 transition"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleExecuteCopy}
                className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-md"
              >
                Apply Schedule
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
