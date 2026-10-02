import React, { useState } from 'react';
import { useSchool } from '../../context/SchoolContext';
import { Exam, GradeBand, QuestionPaper, SyllabusItem, AcademicSession } from '../../types';
import {
  Award, Plus, Trash2, Edit3,
  FileQuestion, Save, CheckCircle
} from 'lucide-react';

interface ExamsManagerProps {
  initialTab?: 'exams' | 'sessions' | 'marks' | 'grades' | 'papers' | 'syllabus';
}

export const ExamsManager: React.FC<ExamsManagerProps> = ({ initialTab = 'exams' }) => {
  const {
    exams, gradeBands, gradeEntries, classes, subjects,
    students, addExam, updateExam, deleteExam,
    addGradeBand, deleteGradeBand, calculateGrade, saveGradeEntries,
    questionPapers, addQuestionPaper, updateQuestionPaper, deleteQuestionPaper,
    syllabus, deleteSyllabus,
    academicSessions, addAcademicSession, updateAcademicSession, deleteAcademicSession, setCurrentSession,
    branding
  } = useSchool();

  const [activeTab, setActiveTab] = useState<'exams' | 'sessions' | 'marks' | 'grades' | 'papers' | 'syllabus'>(initialTab);

  React.useEffect(() => {
    if (initialTab) {
      setActiveTab(initialTab);
    }
  }, [initialTab]);

  // Exam Form Modal
  const [isExamModalOpen, setIsExamModalOpen] = useState(false);
  const [editingExamId, setEditingExamId] = useState<string | null>(null);
  const [examName, setExamName] = useState('');
  const [examTerm, setExamTerm] = useState('Term 1');
  const [examStart, setExamStart] = useState('2026-10-15');
  const [examEnd, setExamEnd] = useState('2026-10-24');
  const [examMaxMarks, setExamMaxMarks] = useState(100);
  const [examPassingMarks, setExamPassingMarks] = useState(40);
  const [examClassId, setExamClassId] = useState('c-4');
  const [examStatus, setExamStatus] = useState<Exam['status']>('ongoing');

  // Academic Session Modal
  const [isSessionModalOpen, setIsSessionModalOpen] = useState(false);
  const [editingSessionId, setEditingSessionId] = useState<string | null>(null);
  const [sessionLabel, setSessionLabel] = useState('2026-2027');
  const [sessionStart, setSessionStart] = useState('2026-04-01');
  const [sessionEnd, setSessionEnd] = useState('2027-03-31');
  const [sessionIsCurrent, setSessionIsCurrent] = useState(false);
  const [sessionTerms, setSessionTerms] = useState(3);

  // Question Paper Modal
  const [isQPModalOpen, setIsQPModalOpen] = useState(false);
  const [editingQPId, setEditingQPId] = useState<string | null>(null);
  const [qpTitle, setQpTitle] = useState('');
  const [qpSubject, setQpSubject] = useState('Mathematics');
  const [qpClass, setQpClass] = useState('Class 4 - A');
  const [qpTerm, setQpTerm] = useState('Term 1');
  const [qpType, setQpType] = useState('Objective & Subjective');
  const [qpDate, setQpDate] = useState('2026-10-15');
  const [qpDuration, setQpDuration] = useState(120);
  const [qpMax, setQpMax] = useState(100);
  const [qpPass, setQpPass] = useState(40);
  const [qpInstructions, setQpInstructions] = useState('Attempt all questions. Calculators not allowed.');
  const [qpTopics, setQpTopics] = useState('Units 1 to 4');

  // Marks Entry Gradebook State
  const [gradebookExamId, setGradebookExamId] = useState(exams[0]?.id || '');
  const [gradebookClassId, setGradebookClassId] = useState(classes[0]?.id || 'c-4');
  const [gradebookSubjectId, setGradebookSubjectId] = useState(subjects[0]?.id || '');
  const [localMarks, setLocalMarks] = useState<Record<string, number>>({});
  const [marksSaved, setMarksSaved] = useState(false);

  // Grade Band Add Form State
  const [newBandGrade, setNewBandGrade] = useState('');
  const [newBandMin, setNewBandMin] = useState<number>(0);
  const [newBandMax, setNewBandMax] = useState<number>(100);
  const [newBandRemarks, setNewBandRemarks] = useState('');

  const selectedClassStudents = students.filter(s => s.classId === gradebookClassId);
  const selectedClassSubjects = subjects.filter(s => s.classId === gradebookClassId);

  const handleOpenAddExam = () => {
    setEditingExamId(null);
    setExamName('');
    setExamTerm('Term 1');
    setExamStart('2026-10-15');
    setExamEnd('2026-10-24');
    setExamMaxMarks(100);
    setExamPassingMarks(40);
    setExamStatus('ongoing');
    setIsExamModalOpen(true);
  };

  const handleOpenEditExam = (e: Exam) => {
    setEditingExamId(e.id);
    setExamName(e.name);
    setExamTerm(e.term);
    setExamStart(e.startDate);
    setExamEnd(e.endDate);
    setExamMaxMarks(e.maxMarks);
    setExamPassingMarks(e.passingMarks || 40);
    setExamStatus(e.status);
    setIsExamModalOpen(true);
  };

  const handleSaveExam = (e: React.FormEvent) => {
    e.preventDefault();
    if (!examName) return;
    const cls = classes.find(c => c.id === examClassId);
    if (editingExamId) {
      updateExam(editingExamId, {
        name: examName,
        term: examTerm,
        startDate: examStart,
        endDate: examEnd,
        maxMarks: Number(examMaxMarks) || 100,
        passingMarks: Number(examPassingMarks) || 40,
        status: examStatus,
        classId: examClassId,
        className: cls ? `${cls.name} - ${cls.section}` : "Class 4 - A"
      });
    } else {
      addExam({
        sessionId: academicSessions.find(s => s.isCurrent)?.id || "sess-2026",
        name: examName,
        term: examTerm,
        startDate: examStart,
        endDate: examEnd,
        maxMarks: Number(examMaxMarks) || 100,
        passingMarks: Number(examPassingMarks) || 40,
        status: examStatus,
        classId: examClassId,
        className: cls ? `${cls.name} - ${cls.section}` : "Class 4 - A"
      });
    }
    setIsExamModalOpen(false);
  };

  const handleOpenAddSession = () => {
    setEditingSessionId(null);
    setSessionLabel('2027-2028');
    setSessionStart('2027-04-01');
    setSessionEnd('2028-03-31');
    setSessionIsCurrent(false);
    setSessionTerms(3);
    setIsSessionModalOpen(true);
  };

  const handleOpenEditSession = (sess: AcademicSession) => {
    setEditingSessionId(sess.id);
    setSessionLabel(sess.label);
    setSessionStart(sess.startDate);
    setSessionEnd(sess.endDate);
    setSessionIsCurrent(sess.isCurrent);
    setSessionTerms(sess.termCount);
    setIsSessionModalOpen(true);
  };

  const handleSaveSession = (e: React.FormEvent) => {
    e.preventDefault();
    if (!sessionLabel) return;
    if (editingSessionId) {
      updateAcademicSession(editingSessionId, {
        label: sessionLabel,
        startDate: sessionStart,
        endDate: sessionEnd,
        isCurrent: sessionIsCurrent,
        termCount: Number(sessionTerms)
      });
    } else {
      addAcademicSession({
        label: sessionLabel,
        startDate: sessionStart,
        endDate: sessionEnd,
        isCurrent: sessionIsCurrent,
        termCount: Number(sessionTerms)
      });
    }
    setIsSessionModalOpen(false);
  };

  const handleOpenAddQP = () => {
    setEditingQPId(null);
    setQpTitle('');
    setQpSubject('Mathematics');
    setQpClass('Class 4 - A');
    setQpTerm('Term 1');
    setQpType('Objective & Subjective');
    setQpDate('2026-10-15');
    setQpDuration(120);
    setQpMax(100);
    setQpPass(40);
    setQpInstructions('Attempt all questions.');
    setQpTopics('Units 1 to 4');
    setIsQPModalOpen(true);
  };

  const handleOpenEditQP = (qp: QuestionPaper) => {
    setEditingQPId(qp.id);
    setQpTitle(qp.title);
    setQpSubject(qp.subject);
    setQpClass(qp.className);
    setQpTerm(qp.examTerm || 'Term 1');
    setQpType(qp.type);
    setQpDate(qp.date);
    setQpDuration(qp.durationMinutes || 120);
    setQpMax(qp.maxMarks);
    setQpPass(qp.passingMarks || 40);
    setQpInstructions(qp.instructions || '');
    setQpTopics(qp.topicsCovered || '');
    setIsQPModalOpen(true);
  };

  const handleSaveQP = (e: React.FormEvent) => {
    e.preventDefault();
    if (!qpSubject) return;
    if (editingQPId) {
      updateQuestionPaper(editingQPId, {
        title: qpTitle || `${qpSubject} Exam Paper`,
        subject: qpSubject,
        className: qpClass,
        examTerm: qpTerm,
        type: qpType,
        date: qpDate,
        durationMinutes: Number(qpDuration) || 120,
        maxMarks: Number(qpMax) || 100,
        passingMarks: Number(qpPass) || 40,
        instructions: qpInstructions,
        topicsCovered: qpTopics
      });
    } else {
      addQuestionPaper({
        title: qpTitle || `${qpSubject} Exam Paper`,
        subject: qpSubject,
        className: qpClass,
        examTerm: qpTerm,
        type: qpType,
        date: qpDate,
        durationMinutes: Number(qpDuration) || 120,
        maxMarks: Number(qpMax) || 100,
        passingMarks: Number(qpPass) || 40,
        instructions: qpInstructions,
        topicsCovered: qpTopics
      });
    }
    setIsQPModalOpen(false);
  };

  const handleSaveMarks = () => {
    const activeExam = exams.find(e => e.id === gradebookExamId);
    const activeSubj = subjects.find(s => s.id === gradebookSubjectId);
    if (!activeExam || !activeSubj) return;

    const entries = selectedClassStudents.map(student => {
      const marksObtained = localMarks[student.id] !== undefined
        ? localMarks[student.id]
        : (gradeEntries.find(g => g.examId === gradebookExamId && g.subjectId === gradebookSubjectId && g.studentId === student.id)?.marks || 85);
      return {
        examId: gradebookExamId,
        examName: activeExam.name,
        subjectId: gradebookSubjectId,
        subject: activeSubj.name,
        studentId: student.id,
        studentName: student.name,
        classId: gradebookClassId,
        marks: Number(marksObtained),
        total: activeExam.maxMarks
      };
    });

    saveGradeEntries(entries);
    setMarksSaved(true);
    setTimeout(() => setMarksSaved(false), 3000);
  };

  const handleAddGradeBand = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newBandGrade) return;
    addGradeBand({
      grade: newBandGrade,
      minPct: Number(newBandMin),
      maxPct: Number(newBandMax),
      remarks: newBandRemarks || "Satisfactory"
    });
    setNewBandGrade('');
    setNewBandRemarks('');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <h1 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <Award className="w-5 h-5 text-indigo-600" />
            <span>Exams, Sessions & Question Papers Command</span>
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Complete CRUD for Examination terms, Academic Sessions, Question Papers with Edit & Delete, and Gradebook.
          </p>
        </div>

        {/* Tab switchers */}
        <div className="flex flex-wrap items-center gap-1.5 p-1 bg-slate-100 rounded-xl">
          {(['exams', 'sessions', 'marks', 'grades', 'papers', 'syllabus'] as const).map(tabKey => (
            <button
              key={tabKey}
              onClick={() => setActiveTab(tabKey)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition capitalize ${
                activeTab === tabKey ? 'bg-white text-indigo-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              {tabKey === 'exams' ? 'Exams' : tabKey === 'sessions' ? 'Academic Sessions' : tabKey === 'marks' ? 'Enter Marks' : tabKey === 'grades' ? 'Grade Settings' : tabKey === 'papers' ? 'Question Papers' : 'Syllabus'}
            </button>
          ))}
        </div>
      </div>

      {/* SUBTAB 1: EXAMS CRUD */}
      {activeTab === 'exams' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-900">Scheduled Examination Terms ({exams.length})</h2>
            <button
              onClick={handleOpenAddExam}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Create Examination Term</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {exams.map(ex => (
              <div key={ex.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between hover:border-indigo-300 transition">
                <div>
                  <div className="flex items-center justify-between gap-2 border-b border-slate-100 pb-3">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200">
                      {ex.term}
                    </span>
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleOpenEditExam(ex)}
                        className="p-1 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded transition"
                        title="Edit Examination"
                      >
                        <Edit3 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`Delete examination term: ${ex.name}?`)) deleteExam(ex.id);
                        }}
                        className="p-1 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded transition"
                        title="Delete Examination"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  <h3 className="font-bold text-sm text-slate-900 mt-3">{ex.name}</h3>
                  <div className="text-xs text-slate-500 mt-1">
                    Max Marks: <strong className="text-slate-800">{ex.maxMarks}</strong> • Passing: <strong className="text-emerald-700">{ex.passingMarks || 40}</strong>
                  </div>

                  <div className="mt-3 p-2.5 rounded-xl bg-slate-50 border border-slate-100 text-xs text-slate-600 space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Start Date:</span>
                      <span className="font-semibold">{ex.startDate}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">End Date:</span>
                      <span className="font-semibold">{ex.endDate}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between">
                  <button
                    onClick={() => {
                      setGradebookExamId(ex.id);
                      setActiveTab('marks');
                    }}
                    className="text-xs font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
                  >
                    <span>Enter Marks</span>
                    <span>&rarr;</span>
                  </button>
                  <span className={`text-[10px] font-bold uppercase px-2 py-0.5 rounded ${
                    ex.status === 'ongoing' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-700'
                  }`}>
                    {ex.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SUBTAB 2: ACADEMIC SESSIONS CRUD */}
      {activeTab === 'sessions' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-bold text-slate-900">Academic Sessions Directory</h2>
              <p className="text-xs text-slate-500">Manage academic calendar years, term counts, and active current session.</p>
            </div>
            <button
              onClick={handleOpenAddSession}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Add Academic Session</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {academicSessions.map(sess => (
              <div
                key={sess.id}
                className={`bg-white rounded-2xl border p-5 shadow-xs flex flex-col justify-between transition ${
                  sess.isCurrent ? 'border-2 border-emerald-500 ring-2 ring-emerald-500/10' : 'border-slate-200'
                }`}
              >
                <div>
                  <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                    <span className="font-mono text-sm font-black text-slate-900">
                      Session {sess.label}
                    </span>
                    {sess.isCurrent ? (
                      <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 flex items-center gap-1">
                        <CheckCircle className="w-3 h-3" />
                        <span>Current Active</span>
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-400 font-bold uppercase">Archived</span>
                    )}
                  </div>
                  <div className="py-3 space-y-2 text-xs text-slate-600">
                    <div className="flex justify-between">
                      <span className="text-slate-400">Start Date:</span>
                      <strong className="text-slate-800 font-mono">{sess.startDate}</strong>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">End Date:</span>
                      <strong className="text-slate-800 font-mono">{sess.endDate}</strong>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Academic Terms:</span>
                      <strong className="text-indigo-700">{sess.termCount} Terms</strong>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  {!sess.isCurrent ? (
                    <button
                      onClick={() => setCurrentSession(sess.id)}
                      className="font-bold text-emerald-600 hover:text-emerald-800"
                    >
                      Make Current Active
                    </button>
                  ) : (
                    <span className="text-[11px] text-emerald-700 font-semibold">Active Campus Session</span>
                  )}
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => handleOpenEditSession(sess)}
                      className="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                      title="Edit Academic Session"
                    >
                      <Edit3 className="w-4 h-4" />
                    </button>
                    {!sess.isCurrent && (
                      <button
                        onClick={() => {
                          if (confirm(`Delete session ${sess.label}?`)) deleteAcademicSession(sess.id);
                        }}
                        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition"
                        title="Delete Session"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SUBTAB 3: MARKS ENTRY / GRADEBOOK */}
      {activeTab === 'marks' && (
        <div className="space-y-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-3">
              <div>
                <label className="text-[10px] font-bold text-slate-500 uppercase block mb-0.5">Exam Term</label>
                <select
                  value={gradebookExamId}
                  onChange={e => setGradebookExamId(e.target.value)}
                  className="text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-indigo-950 focus:outline-indigo-500"
                >
                  {exams.map(e => (
                    <option key={e.id} value={e.id}>{e.name} ({e.term})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-[10px] font-bold text-slate-500 uppercase block mb-0.5">Class</label>
                <select
                  value={gradebookClassId}
                  onChange={e => setGradebookClassId(e.target.value)}
                  className="text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-indigo-950 focus:outline-indigo-500"
                >
                  {classes.map(c => (
                    <option key={c.id} value={c.id}>{c.name} - Section {c.section}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="text-[10px] font-bold text-slate-500 uppercase block mb-0.5">Subject</label>
                <select
                  value={gradebookSubjectId}
                  onChange={e => setGradebookSubjectId(e.target.value)}
                  className="text-xs font-bold rounded-lg border border-slate-300 bg-slate-50 px-3 py-1.5 text-indigo-950 focus:outline-indigo-500"
                >
                  {selectedClassSubjects.map(s => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <button
              onClick={handleSaveMarks}
              className="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-bold shadow-xs transition"
            >
              <Save className="w-4 h-4" />
              <span>Save All Marks</span>
            </button>
          </div>

          {marksSaved && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs font-bold text-emerald-800">
              Marks recorded and grades auto-calculated from active grade bands!
            </div>
          )}

          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
            <div className="overflow-x-auto overflow-y-auto max-h-[580px]">
              <table className="w-full text-left text-xs min-w-[760px]">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px] tracking-wider sticky top-0 z-10">
                  <tr>
                    <th className="py-3 px-4">Roll</th>
                    <th className="py-3 px-4">Student Name</th>
                    <th className="py-3 px-4">Father Name</th>
                    <th className="py-3 px-4 text-center">Marks Obtained (Max 100)</th>
                    <th className="py-3 px-4 text-center">Percentage</th>
                    <th className="py-3 px-4 text-center">Auto-Calculated Grade</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {selectedClassStudents.map(student => {
                    const existingEntry = gradeEntries.find(g =>
                      g.examId === gradebookExamId &&
                      g.subjectId === gradebookSubjectId &&
                      g.studentId === student.id
                    );
                    const currentMarks = localMarks[student.id] !== undefined
                      ? localMarks[student.id]
                      : (existingEntry?.marks || 88);
                    const pct = Math.round((currentMarks / 100) * 100);
                    const { grade, remarks } = calculateGrade(pct);

                    return (
                      <tr key={student.id} className="hover:bg-slate-50/80 transition">
                        <td className="py-3 px-4 font-mono font-bold text-indigo-700">
                          {student.rollNo || "00"}
                        </td>
                        <td className="py-3 px-4 font-bold text-slate-900">
                          {student.name}
                        </td>
                        <td className="py-3 px-4 text-slate-600">
                          {student.fatherName}
                        </td>
                        <td className="py-3 px-4 text-center">
                          <input
                            type="number"
                            min="0"
                            max="100"
                            value={currentMarks}
                            onChange={e => setLocalMarks({
                              ...localMarks,
                              [student.id]: Number(e.target.value)
                            })}
                            className="w-20 text-center font-bold p-1.5 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono text-sm bg-slate-50"
                          />
                        </td>
                        <td className="py-3 px-4 text-center font-bold text-slate-700 font-mono">
                          {pct}%
                        </td>
                        <td className="py-3 px-4 text-center">
                          <span className={`inline-block px-2.5 py-0.5 rounded text-xs font-black ${
                            grade === 'A+' || grade === 'A'
                              ? 'bg-emerald-100 text-emerald-800'
                              : grade === 'B' || grade === 'C'
                              ? 'bg-indigo-100 text-indigo-800'
                              : 'bg-rose-100 text-rose-800'
                          }`}>
                            {grade} ({remarks})
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SUBTAB 4: GRADE SETTINGS BANDS */}
      {activeTab === 'grades' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4">
            <h2 className="text-sm font-bold text-slate-900 border-b border-slate-100 pb-2">
              Add / Define Grade Band
            </h2>
            <form onSubmit={handleAddGradeBand} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Grade Label *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. A+, A, Distinction"
                  value={newBandGrade}
                  onChange={e => setNewBandGrade(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Min % *</label>
                  <input
                    type="number"
                    required
                    value={newBandMin}
                    onChange={e => setNewBandMin(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Max % *</label>
                  <input
                    type="number"
                    required
                    value={newBandMax}
                    onChange={e => setNewBandMax(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-mono"
                  />
                </div>
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Remarks / Meaning</label>
                <input
                  type="text"
                  placeholder="e.g. Outstanding Performance"
                  value={newBandRemarks}
                  onChange={e => setNewBandRemarks(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500"
                />
              </div>
              <button
                type="submit"
                className="w-full py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition shadow-xs"
              >
                + Add Grade Band
              </button>
            </form>
          </div>

          <div className="md:col-span-2 bg-white rounded-2xl border border-slate-200 shadow-xs p-5">
            <h2 className="text-sm font-bold text-slate-900 border-b border-slate-100 pb-3 mb-3">
              Configured Grade Bands
            </h2>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#F8FAFC] border-b border-slate-200 text-slate-600 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="py-2.5 px-3">Grade</th>
                    <th className="py-2.5 px-3">Min %</th>
                    <th className="py-2.5 px-3">Max %</th>
                    <th className="py-2.5 px-3">Remarks</th>
                    <th className="py-2.5 px-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {gradeBands.map(gb => (
                    <tr key={gb.id} className="hover:bg-slate-50 transition">
                      <td className="py-3 px-3">
                        <span className="font-black text-sm px-2.5 py-0.5 rounded bg-indigo-50 text-indigo-900">
                          {gb.grade}
                        </span>
                      </td>
                      <td className="py-3 px-3 font-mono font-bold text-slate-800">{gb.minPct}%</td>
                      <td className="py-3 px-3 font-mono font-bold text-slate-800">{gb.maxPct}%</td>
                      <td className="py-3 px-3 text-slate-600 font-medium">{gb.remarks}</td>
                      <td className="py-3 px-3 text-right">
                        <button
                          onClick={() => deleteGradeBand(gb.id)}
                          className="p-1 text-slate-400 hover:text-rose-600"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* SUBTAB 5: QUESTION PAPERS */}
      {activeTab === 'papers' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
            <div>
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <FileQuestion className="w-5 h-5 text-indigo-600" />
                <span>Question Papers Repository</span>
              </h2>
              <p className="text-xs text-slate-500 mt-1">
                Both Edit and Delete actions available on every section and card of the question papers.
              </p>
            </div>
            <button
              onClick={handleOpenAddQP}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition shadow-xs"
            >
              <Plus className="w-4 h-4" />
              <span>Create Question Paper</span>
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {questionPapers.map(qp => (
              <div key={qp.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between hover:border-indigo-300 transition space-y-4">
                <div>
                  <div className="flex items-start justify-between gap-2 border-b border-slate-100 pb-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-mono">
                          {qp.className}
                        </span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                          {qp.examTerm || 'Term 1'}
                        </span>
                      </div>
                      <h3 className="font-bold text-base text-slate-900 mt-1.5 leading-snug">
                        {qp.title || `${qp.subject} Examination Paper`}
                      </h3>
                      <div className="text-xs text-indigo-700 font-semibold mt-0.5">
                        Subject: {qp.subject}
                      </div>
                    </div>

                    <div className="flex items-center gap-1 bg-slate-50 p-1 rounded-xl border border-slate-200">
                      <button
                        onClick={() => handleOpenEditQP(qp)}
                        className="p-1.5 text-slate-600 hover:text-indigo-600 hover:bg-white rounded-lg transition"
                        title="Edit Question Paper Details"
                      >
                        <Edit3 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => {
                          if (confirm(`Delete question paper "${qp.title || qp.subject}"?`)) {
                            deleteQuestionPaper(qp.id);
                          }
                        }}
                        className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-white rounded-lg transition"
                        title="Delete Question Paper"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  <div className="py-2 space-y-1.5 text-xs text-slate-600">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Exam Format / Type:</span>
                      <span className="font-semibold text-slate-800">{qp.type}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Scheduled Date:</span>
                      <span className="font-mono text-slate-800">{qp.date}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Duration & Marks:</span>
                      <span className="font-bold text-slate-900">
                        {qp.durationMinutes || 120} Mins • Max: {qp.maxMarks} • Pass: {qp.passingMarks || 40}
                      </span>
                    </div>
                    {qp.topicsCovered && (
                      <div className="mt-2 p-2 bg-slate-50 rounded-lg border border-slate-100 text-[11px]">
                        <span className="font-bold text-slate-700 block">Syllabus Topics Covered:</span>
                        <span className="text-slate-600">{qp.topicsCovered}</span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  <span className="text-[11px] text-slate-400 font-serif italic truncate max-w-[200px]">
                    &ldquo;{qp.instructions || 'Attempt all questions'}&rdquo;
                  </span>
                  <button
                    onClick={() => handleOpenEditQP(qp)}
                    className="text-xs font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1"
                  >
                    <span>Edit Paper</span>
                    <Edit3 className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* SUBTAB 6: SYLLABUS */}
      {activeTab === 'syllabus' && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-base font-bold text-slate-900">Term-Wise Syllabus Outlines</h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {syllabus.map(syl => (
              <div key={syl.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between gap-2 border-b border-slate-100 pb-2 mb-2">
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700">
                      {syl.term}
                    </span>
                    <span className="text-xs text-slate-400 font-semibold">{syl.className}</span>
                  </div>
                  <h3 className="font-bold text-sm text-slate-900">{syl.title}</h3>
                  <div className="text-xs text-indigo-700 font-medium mt-1">Subject: {syl.subject}</div>
                  <p className="text-xs text-slate-600 mt-2">{syl.chapters}</p>
                </div>
                <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  <span className="text-slate-400">Board Compliant Syllabus</span>
                  <button
                    onClick={() => deleteSyllabus(syl.id)}
                    className="text-slate-400 hover:text-rose-600 p-1"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* MODALS */}
      {/* 1. Exam Modal */}
      {isExamModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">
                {editingExamId ? "Edit Examination Term" : "Create New Examination Term"}
              </h2>
              <button onClick={() => setIsExamModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveExam} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Exam Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Mid-Term Examination 2026"
                  value={examName}
                  onChange={e => setExamName(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Term / Category</label>
                  <select
                    value={examTerm}
                    onChange={e => setExamTerm(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg focus:outline-indigo-500 font-semibold"
                  >
                    <option value="Term 1">Term 1 (Mid-Term)</option>
                    <option value="Term 2">Term 2 (Final Examination)</option>
                    <option value="Monthly Test">Monthly Assessment</option>
                    <option value="Send-Up Exam">Send-Up Board Prep</option>
                  </select>
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Status</label>
                  <select
                    value={examStatus}
                    onChange={e => setExamStatus(e.target.value as any)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-semibold"
                  >
                    <option value="upcoming">Upcoming</option>
                    <option value="ongoing">Ongoing</option>
                    <option value="completed">Completed</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Start Date</label>
                  <input
                    type="date"
                    value={examStart}
                    onChange={e => setExamStart(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">End Date</label>
                  <input
                    type="date"
                    value={examEnd}
                    onChange={e => setExamEnd(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Max Marks</label>
                  <input
                    type="number"
                    value={examMaxMarks}
                    onChange={e => setExamMaxMarks(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Passing Marks</label>
                  <input
                    type="number"
                    value={examPassingMarks}
                    onChange={e => setExamPassingMarks(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsExamModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingExamId ? "Save Changes" : "Create Exam"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 2. Academic Session Modal */}
      {isSessionModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-emerald-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">
                {editingSessionId ? "Edit Academic Session" : "Add Academic Session"}
              </h2>
              <button onClick={() => setIsSessionModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveSession} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Session Label * (e.g. 2026-2027)</label>
                <input
                  type="text"
                  required
                  placeholder="2026-2027"
                  value={sessionLabel}
                  onChange={e => setSessionLabel(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg font-mono font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Session Start Date</label>
                  <input
                    type="date"
                    value={sessionStart}
                    onChange={e => setSessionStart(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Session End Date</label>
                  <input
                    type="date"
                    value={sessionEnd}
                    onChange={e => setSessionEnd(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
              </div>
              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="sessCurrent"
                  checked={sessionIsCurrent}
                  onChange={e => setSessionIsCurrent(e.target.checked)}
                  className="rounded text-emerald-600 focus:ring-emerald-500"
                />
                <label htmlFor="sessCurrent" className="font-bold text-slate-800">
                  Set as Current Active Session for {branding.name}
                </label>
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsSessionModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-bold transition shadow-xs"
                >
                  {editingSessionId ? "Save Changes" : "Save Session"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 3. Question Paper Modal */}
      {isQPModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-xs">
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="p-4 bg-gradient-to-r from-indigo-900 to-slate-900 text-white flex items-center justify-between">
              <h2 className="font-bold text-sm">
                {editingQPId ? "Edit Question Paper" : "Create Question Paper"}
              </h2>
              <button onClick={() => setIsQPModalOpen(false)}>
                <span className="text-slate-300 hover:text-white font-bold">✕</span>
              </button>
            </div>
            <form onSubmit={handleSaveQP} className="p-5 space-y-3.5 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Paper Title</label>
                <input
                  type="text"
                  placeholder="e.g. Mathematics Mid-Term Comprehensive Paper"
                  value={qpTitle}
                  onChange={e => setQpTitle(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                />
              </div>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Subject Name *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Mathematics"
                    value={qpSubject}
                    onChange={e => setQpSubject(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Class</label>
                  <input
                    type="text"
                    value={qpClass}
                    onChange={e => setQpClass(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-2.5">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Exam Date</label>
                  <input
                    type="date"
                    value={qpDate}
                    onChange={e => setQpDate(e.target.value)}
                    className="w-full p-2 border border-slate-300 rounded-lg font-mono"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Duration (Mins)</label>
                  <input
                    type="number"
                    value={qpDuration}
                    onChange={e => setQpDuration(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Max Marks</label>
                  <input
                    type="number"
                    value={qpMax}
                    onChange={e => setQpMax(Number(e.target.value))}
                    className="w-full p-2 border border-slate-300 rounded-lg font-bold"
                  />
                </div>
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Syllabus Chapters / Topics</label>
                <input
                  type="text"
                  placeholder="e.g. Fractions, Decimals, Long Division"
                  value={qpTopics}
                  onChange={e => setQpTopics(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Exam Instructions</label>
                <textarea
                  rows={2}
                  value={qpInstructions}
                  onChange={e => setQpInstructions(e.target.value)}
                  className="w-full p-2 border border-slate-300 rounded-lg"
                />
              </div>
              <div className="flex items-center justify-end gap-2.5 pt-3 border-t border-slate-200">
                <button
                  type="button"
                  onClick={() => setIsQPModalOpen(false)}
                  className="px-4 py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold transition shadow-xs"
                >
                  {editingQPId ? "Save Question Paper" : "Create Paper"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
