import React, { useState, useEffect } from 'react';
import { api } from '../api/client';
import {
  LayerIcon,
  PlusIcon,
  PlayIcon,
  TrashIcon,
  RefreshCwIcon,
  CalendarIcon,
  UserIcon,
  BuildingIcon,
} from '../components/Icons';

export default function ExaminationScheduler() {
  const [exams, setExams] = useState([]);
  const [courses, setCourses] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [facultyList, setFacultyList] = useState([]);
  const [studentGroups, setStudentGroups] = useState([]);
  const [timeSlots, setTimeSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);

  // Filter
  const [filterGroup, setFilterGroup] = useState('');
  const [filterDate, setFilterDate] = useState('');

  // Generation Settings Modal / Form
  const [genStartDate, setGenStartDate] = useState(new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0]);
  const [minSpacing, setMinSpacing] = useState(1);

  // Manual Add Modal
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    courseId: '',
    studentGroupId: '',
    examDate: new Date().toISOString().split('T')[0],
    timeSlotId: '',
    classroomId: '',
    invigilatorId: '',
    durationMinutes: 180,
  });
  const [formError, setFormError] = useState('');

  const loadData = async () => {
    try {
      setLoading(true);
      const [allExams, allCourses, allRooms, allFacs, allGroups, allSlots] = await Promise.all([
        api.getExaminations(),
        api.getCourses(),
        api.getClassrooms(),
        api.getFaculty(),
        api.getStudentGroups(),
        api.getTimeSlots(),
      ]);
      setExams(allExams);
      setCourses(allCourses);
      setClassrooms(allRooms);
      setFacultyList(allFacs);
      setStudentGroups(allGroups);
      setTimeSlots(allSlots);

      if (allCourses.length > 0 && !formData.courseId) {
        setFormData((prev) => ({
          ...prev,
          courseId: allCourses[0].id,
          studentGroupId: allGroups[0]?.id || '',
          classroomId: allRooms[0]?.id || '',
          invigilatorId: allFacs[0]?.id || '',
          timeSlotId: allSlots[0]?.id || '',
        }));
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAutoGenerate = async () => {
    if (window.confirm('Automatically generate a conflict-free examination schedule for all courses?')) {
      try {
        setGenerating(true);
        const result = await api.generateExaminations(genStartDate, minSpacing, true);
        alert(`Successfully scheduled ${result.length} examinations with invigilator duty assignments!`);
        loadData();
      } catch (err) {
        alert(err.message || 'Generation failed');
      } finally {
        setGenerating(false);
      }
    }
  };

  const handleClear = async () => {
    if (window.confirm('Clear all scheduled examinations?')) {
      try {
        await api.clearExaminations();
        loadData();
      } catch (err) {
        alert(err.message || 'Clear failed');
      }
    }
  };

  const handleDelete = async (id, courseName) => {
    if (window.confirm(`Delete examination for ${courseName}?`)) {
      try {
        await api.deleteExamination(id);
        loadData();
      } catch (err) {
        alert(err.message || 'Delete failed');
      }
    }
  };

  const handleManualSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    try {
      await api.createExamination(formData);
      setModalOpen(false);
      loadData();
    } catch (err) {
      setFormError(err.message || 'Failed to schedule exam');
    }
  };

  const filteredExams = exams.filter((ex) => {
    if (filterGroup && String(ex.studentGroupId) !== String(filterGroup)) return false;
    if (filterDate && ex.examDate !== filterDate) return false;
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
            <LayerIcon className="w-5 h-5 text-amber-600" />
            University Examination Scheduling Engine
          </h2>
          <p className="text-xs text-slate-500">
            Automate exam dates, time slots, exam room capacity checks, and faculty invigilator duty assignments
          </p>
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={handleAutoGenerate}
            disabled={generating}
            className="btn-primary text-xs bg-amber-600 hover:bg-amber-700 shadow-sm"
          >
            <PlayIcon className="w-4 h-4" />
            {generating ? 'Scheduling...' : 'Auto-Generate Exam Timetable'}
          </button>
          <button onClick={() => setModalOpen(true)} className="btn-secondary text-xs">
            <PlusIcon className="w-4 h-4" />
            Schedule Single Exam
          </button>
          <button onClick={handleClear} className="btn-danger text-xs">
            <TrashIcon className="w-4 h-4" />
            Clear All
          </button>
          <button onClick={loadData} className="btn-secondary text-xs" title="Refresh">
            <RefreshCwIcon className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Auto-Generation Parameter Controls */}
      <div className="app-card p-4 bg-amber-50/40 border-amber-200 grid grid-cols-1 sm:grid-cols-3 gap-3 items-center text-xs">
        <div>
          <label className="block text-slate-700 font-semibold mb-1">Examination Start Date:</label>
          <input
            type="date"
            value={genStartDate}
            onChange={(e) => setGenStartDate(e.target.value)}
            className="form-input text-xs"
          />
        </div>
        <div>
          <label className="block text-slate-700 font-semibold mb-1">Minimum Spacing Between Exams (Days):</label>
          <input
            type="number"
            min="1"
            max="5"
            value={minSpacing}
            onChange={(e) => setMinSpacing(parseInt(e.target.value) || 1)}
            className="form-input text-xs"
          />
        </div>
        <div className="pt-4">
          <span className="text-[11px] text-slate-500">
            Automatically prevents student double-bookings and distributes invigilation duties evenly across faculty.
          </span>
        </div>
      </div>

      {/* Filters Bar */}
      <div className="app-card p-4 flex flex-col sm:flex-row gap-3 items-center justify-between text-xs">
        <div className="flex items-center gap-2 w-full sm:w-auto">
          <label className="font-medium text-slate-600">Filter by Cohort:</label>
          <select
            value={filterGroup}
            onChange={(e) => setFilterGroup(e.target.value)}
            className="form-input text-xs w-60"
          >
            <option value="">All Student Groups</option>
            {studentGroups.map((g) => (
              <option key={g.id} value={g.id}>
                {g.groupName} ({g.groupCode})
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <label className="font-medium text-slate-600">Filter by Date:</label>
          <input
            type="date"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
            className="form-input text-xs"
          />
          {filterDate && (
            <button onClick={() => setFilterDate('')} className="text-slate-400 hover:text-slate-600 text-xs">
              Reset
            </button>
          )}
        </div>
      </div>

      {/* Examinations Table */}
      <div className="app-card overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-500">Loading examinations...</div>
        ) : filteredExams.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <LayerIcon className="w-8 h-8 text-slate-300 mx-auto" />
            <div className="text-sm font-semibold text-slate-700">No Examinations Scheduled Yet</div>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Click "Auto-Generate Exam Timetable" to produce a spaced, conflict-free examination schedule for all courses.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
                  <th className="p-3.5">Date</th>
                  <th className="p-3.5">Time</th>
                  <th className="p-3.5">Course Code & Title</th>
                  <th className="p-3.5">Student Group</th>
                  <th className="p-3.5">Examination Hall</th>
                  <th className="p-3.5">Assigned Invigilator</th>
                  <th className="p-3.5 text-center">Duration</th>
                  <th className="p-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredExams.map((ex) => (
                  <tr key={ex.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="p-3.5 font-bold text-slate-900 whitespace-nowrap">
                      <div className="flex items-center gap-1.5">
                        <CalendarIcon className="w-3.5 h-3.5 text-amber-600" />
                        <span>{ex.examDate}</span>
                      </div>
                      <span className="text-[10px] text-slate-400 font-normal">{ex.dayOfWeek}</span>
                    </td>
                    <td className="p-3.5 font-mono text-[11px] font-semibold text-slate-700">
                      {ex.startTime?.slice(0, 5)} - {ex.endTime?.slice(0, 5)}
                    </td>
                    <td className="p-3.5">
                      <span className="font-bold text-blue-700 mr-1.5">{ex.courseCode}</span>
                      <span className="font-medium text-slate-800">{ex.courseName}</span>
                    </td>
                    <td className="p-3.5">
                      <span className="badge badge-purple text-[10px]">{ex.groupName}</span>
                      <div className="text-[10px] text-slate-400 mt-0.5">{ex.studentCount} candidates</div>
                    </td>
                    <td className="p-3.5 font-medium text-slate-800">
                      <span className="badge badge-blue text-[10px] mr-1">{ex.roomNumber}</span>
                      <span className="text-slate-500 text-[11px]">({ex.classroomCapacity} capacity)</span>
                    </td>
                    <td className="p-3.5 font-medium text-slate-900">
                      <div className="flex items-center gap-1">
                        <UserIcon className="w-3.5 h-3.5 text-slate-400" />
                        <span>{ex.invigilatorName}</span>
                      </div>
                    </td>
                    <td className="p-3.5 text-center font-semibold text-slate-700">
                      {ex.durationMinutes} mins
                    </td>
                    <td className="p-3.5 text-right">
                      <button
                        onClick={() => handleDelete(ex.id, ex.courseName)}
                        className="p-1.5 rounded hover:bg-rose-100 text-slate-600 hover:text-rose-600"
                        title="Delete exam"
                      >
                        <TrashIcon className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Manual Add Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="app-card w-full max-w-md p-6 bg-white shadow-2xl space-y-4">
            <h3 className="text-sm font-bold text-slate-900 border-b pb-2">Schedule Examination Manually</h3>

            {formError && (
              <div className="p-2.5 rounded bg-rose-50 border border-rose-200 text-rose-700 text-xs">
                {formError}
              </div>
            )}

            <form onSubmit={handleManualSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-700 font-medium mb-1">Course *</label>
                <select
                  required
                  value={formData.courseId}
                  onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
                  className="form-input text-xs"
                >
                  {courses.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.courseCode} - {c.courseName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-700 font-medium mb-1">Student Group *</label>
                <select
                  required
                  value={formData.studentGroupId}
                  onChange={(e) => setFormData({ ...formData, studentGroupId: e.target.value })}
                  className="form-input text-xs"
                >
                  {studentGroups.map((g) => (
                    <option key={g.id} value={g.id}>
                      {g.groupName} ({g.studentCount} students)
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Exam Date *</label>
                  <input
                    type="date"
                    required
                    value={formData.examDate}
                    onChange={(e) => setFormData({ ...formData, examDate: e.target.value })}
                    className="form-input text-xs"
                  />
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Time Slot *</label>
                  <select
                    required
                    value={formData.timeSlotId}
                    onChange={(e) => setFormData({ ...formData, timeSlotId: e.target.value })}
                    className="form-input text-xs"
                  >
                    {timeSlots.filter((s) => !s.break && !s.lunch).map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.dayOfWeek} {s.startTime?.slice(0, 5)} - {s.endTime?.slice(0, 5)}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Examination Hall *</label>
                  <select
                    required
                    value={formData.classroomId}
                    onChange={(e) => setFormData({ ...formData, classroomId: e.target.value })}
                    className="form-input text-xs"
                  >
                    {classrooms.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.roomNumber} ({r.capacity} seats)
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-slate-700 font-medium mb-1">Invigilator *</label>
                  <select
                    required
                    value={formData.invigilatorId}
                    onChange={(e) => setFormData({ ...formData, invigilatorId: e.target.value })}
                    className="form-input text-xs"
                  >
                    {facultyList.map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="btn-secondary text-xs"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary text-xs">
                  Schedule Exam
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
